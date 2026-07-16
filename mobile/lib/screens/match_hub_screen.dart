import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../models/extra_models.dart';
import '../models/fixture_model.dart';
import '../models/report_models.dart';
import '../services/auth_service.dart';
import '../theme/app_theme.dart';
import '../widgets/common.dart';

class MatchHubScreen extends StatefulWidget {
  final FixtureModel fixture;
  final int initialTab;
  const MatchHubScreen({super.key, required this.fixture, this.initialTab = 0});

  @override
  State<MatchHubScreen> createState() => _MatchHubScreenState();
}

class _PlayerEntry {
  final int memberId;
  final String name;
  final int? number;
  final String teamLabel;
  final TextEditingController goalsCtrl;
  final TextEditingController goalMinCtrl;
  final TextEditingController cardMinCtrl;
  String card;
  bool suspended;
  String? suspensionLabel;

  _PlayerEntry({
    required this.memberId,
    required this.name,
    this.number,
    required this.teamLabel,
    int goals = 0,
    int? goalMin,
    this.card = 'NONE',
    int? cardMin,
    this.suspended = false,
    this.suspensionLabel,
  })  : goalsCtrl = TextEditingController(text: goals == 0 ? '' : '$goals'),
        goalMinCtrl = TextEditingController(text: goalMin?.toString() ?? ''),
        cardMinCtrl = TextEditingController(text: cardMin?.toString() ?? '');

  void dispose() {
    goalsCtrl.dispose();
    goalMinCtrl.dispose();
    cardMinCtrl.dispose();
  }
}

class _TimelineEvent {
  final int minute;
  final String label;
  final String detail;
  final Color color;
  _TimelineEvent(this.minute, this.label, this.detail, this.color);
}

class _MatchHubScreenState extends State<MatchHubScreen> with SingleTickerProviderStateMixin {
  static const _templates = [
    'Pitch inspection complete — playable.',
    'Both captains briefed before kick-off.',
    'Crowd behaviour acceptable.',
    'Minor medical delay — play resumed.',
    'Security incident noted — details above.',
    'No further incidents to report.',
  ];

  late final TabController _tabs;
  List<_PlayerEntry> _home = [];
  List<_PlayerEntry> _away = [];
  List<ReportEntry> _entries = [];
  List<ReportComment> _comments = [];
  List<ReportEditLog> _edits = [];
  List<LineupPlayer> _homeLineup = [];
  List<LineupPlayer> _awayLineup = [];
  List<SuspensionInfo> _suspensions = [];
  final _commentCtrl = TextEditingController();
  final _searchCtrl = TextEditingController();
  final _prepNotesCtrl = TextEditingController();
  bool _loading = true;
  bool _submitting = false;
  bool _postingComment = false;
  bool _savingPrep = false;
  String? _error;
  String _search = '';
  Map<String, bool> _prep = {
    'pitchChecked': false,
    'ballsChecked': false,
    'netsChecked': false,
    'captainsBriefed': false,
    'lineupsReceived': false,
    'medicalReady': false,
    'securityOk': false,
  };
  int _prepDone = 0;

  @override
  void initState() {
    super.initState();
    final initial = widget.initialTab.clamp(0, 6);
    _tabs = TabController(length: 7, vsync: this, initialIndex: initial);
    _bootstrap();
  }

  @override
  void dispose() {
    _tabs.dispose();
    _commentCtrl.dispose();
    _searchCtrl.dispose();
    _prepNotesCtrl.dispose();
    for (final p in [..._home, ..._away]) {
      p.dispose();
    }
    super.dispose();
  }

  Future<void> _bootstrap() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final api = context.read<AuthService>().api;
      final f = widget.fixture;

      Future<dynamic> soft(String path, dynamic fallback) async {
        try {
          return await api.get(path);
        } catch (_) {
          return fallback;
        }
      }

      final results = await Future.wait([
        api.get('/api/teams/${f.homeTeamId}/members'),
        api.get('/api/teams/${f.awayTeamId}/members'),
        soft('/api/fixtures/${f.id}/report', <dynamic>[]),
        soft('/api/fixtures/${f.id}/report/comments', <dynamic>[]),
        soft('/api/fixtures/${f.id}/report/edits', <dynamic>[]),
        soft('/api/teams/${f.homeTeamId}/suspensions?fixtureId=${f.id}', <dynamic>[]),
        soft('/api/teams/${f.awayTeamId}/suspensions?fixtureId=${f.id}', <dynamic>[]),
        soft('/api/fixtures/${f.id}/lineup?teamId=${f.homeTeamId}', <dynamic>[]),
        soft('/api/fixtures/${f.id}/lineup?teamId=${f.awayTeamId}', <dynamic>[]),
        soft('/api/fixtures/${f.id}/prep', <String, dynamic>{}),
      ]);

      final existing = (results[2] as List)
          .map((e) => ReportEntry.fromJson(Map<String, dynamic>.from(e as Map)))
          .toList();
      final byId = {for (final e in existing) e.teamMemberId: e};
      final suspensions = [
        ...(results[5] as List).map((e) => SuspensionInfo.fromJson(Map<String, dynamic>.from(e as Map))),
        ...(results[6] as List).map((e) => SuspensionInfo.fromJson(Map<String, dynamic>.from(e as Map))),
      ];
      final suspById = {for (final s in suspensions) s.memberId: s};

      List<_PlayerEntry> mapPlayers(List raw, String teamName) {
        return raw
            .where((m) => m['roleInTeam'] == 'PLAYER')
            .map((m) {
              final id = m['memberId'] as int;
              final ex = byId[id];
              final susp = suspById[id];
              return _PlayerEntry(
                memberId: id,
                name: '${m['fname']} ${m['lname']}',
                number: m['number'],
                teamLabel: teamName,
                goals: ex?.goal ?? 0,
                goalMin: ex?.goalMin,
                card: ex?.card ?? 'NONE',
                cardMin: ex?.cardMin,
                suspended: susp != null,
                suspensionLabel: susp?.reasonLabel,
              );
            })
            .toList();
      }

      for (final p in [..._home, ..._away]) {
        p.dispose();
      }

      final prepRaw = results[9];
      final prep = prepRaw is Map ? Map<String, dynamic>.from(prepRaw) : <String, dynamic>{};

      setState(() {
        _home = mapPlayers(results[0] as List, f.homeTeamName);
        _away = mapPlayers(results[1] as List, f.awayTeamName);
        _entries = existing;
        _comments = (results[3] as List)
            .map((e) => ReportComment.fromJson(Map<String, dynamic>.from(e as Map)))
            .toList();
        _edits = (results[4] as List)
            .map((e) => ReportEditLog.fromJson(Map<String, dynamic>.from(e as Map)))
            .toList();
        _suspensions = suspensions;
        _homeLineup = (results[7] as List)
            .map((e) => LineupPlayer.fromJson(Map<String, dynamic>.from(e as Map)))
            .toList();
        _awayLineup = (results[8] as List)
            .map((e) => LineupPlayer.fromJson(Map<String, dynamic>.from(e as Map)))
            .toList();
        _prep = {
          'pitchChecked': prep['pitchChecked'] == true,
          'ballsChecked': prep['ballsChecked'] == true,
          'netsChecked': prep['netsChecked'] == true,
          'captainsBriefed': prep['captainsBriefed'] == true,
          'lineupsReceived': prep['lineupsReceived'] == true,
          'medicalReady': prep['medicalReady'] == true,
          'securityOk': prep['securityOk'] == true,
        };
        _prepDone = (prep['completedCount'] as num?)?.toInt() ?? _prep.values.where((v) => v).length;
        _prepNotesCtrl.text = prep['notes']?.toString() ?? '';
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString().replaceFirst('Exception: ', '');
        _loading = false;
      });
    }
  }

  Future<void> _submit() async {
    setState(() => _submitting = true);
    try {
      final entries = [..._home, ..._away]
          .where((p) {
            final goals = int.tryParse(p.goalsCtrl.text) ?? 0;
            return goals > 0 || p.card != 'NONE';
          })
          .map((p) => {
                'teamMemberId': p.memberId,
                'goal': int.tryParse(p.goalsCtrl.text) ?? 0,
                'goalMin': int.tryParse(p.goalMinCtrl.text),
                'card': p.card,
                'cardMin': int.tryParse(p.cardMinCtrl.text),
              })
          .toList();

      await context.read<AuthService>().api.post(
            '/api/fixtures/${widget.fixture.id}/report',
            {'entries': entries},
          );
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Report saved. Your name and time were logged.')),
      );
      await _bootstrap();
      _tabs.animateTo(6);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
        );
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  Future<void> _postComment([String? preset]) async {
    final body = (preset ?? _commentCtrl.text).trim();
    if (body.isEmpty) return;
    setState(() => _postingComment = true);
    try {
      await context.read<AuthService>().api.post(
            '/api/fixtures/${widget.fixture.id}/report/comments',
            {'body': body},
          );
      _commentCtrl.clear();
      await _bootstrap();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Comment posted')));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
        );
      }
    } finally {
      if (mounted) setState(() => _postingComment = false);
    }
  }

  String _fmt(DateTime dt) => DateFormat('dd MMM yyyy · HH:mm').format(dt.toLocal());

  List<_TimelineEvent> get _timeline {
    final events = <_TimelineEvent>[];
    for (final e in _entries) {
      if ((e.goal) > 0 && e.goalMin != null) {
        events.add(_TimelineEvent(
          e.goalMin!,
          'GOAL',
          '${e.playerName} (${e.teamName}) · ${e.goal}g',
          AppTheme.accent,
        ));
      }
      if (e.card != 'NONE' && e.cardMin != null) {
        events.add(_TimelineEvent(
          e.cardMin!,
          e.card,
          '${e.playerName} (${e.teamName})',
          e.card == 'RED' ? AppTheme.danger : AppTheme.warn,
        ));
      }
    }
    events.sort((a, b) => a.minute.compareTo(b.minute));
    return events;
  }

  List<_PlayerEntry> _filter(List<_PlayerEntry> list) {
    final q = _search.trim().toLowerCase();
    if (q.isEmpty) return list;
    return list.where((p) {
      return p.name.toLowerCase().contains(q) || '${p.number ?? ''}'.contains(q);
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    final f = widget.fixture;
    return Scaffold(
      appBar: AppBar(
        title: Text('Week ${f.week}'),
        bottom: TabBar(
          controller: _tabs,
          isScrollable: true,
          labelColor: Colors.white,
          unselectedLabelColor: Colors.white70,
          indicatorColor: Colors.white,
          tabs: const [
            Tab(text: 'Overview'),
            Tab(text: 'Prep'),
            Tab(text: 'Timeline'),
            Tab(text: 'Lineups'),
            Tab(text: 'Report'),
            Tab(text: 'Comments'),
            Tab(text: 'History'),
          ],
        ),
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!, style: const TextStyle(color: AppTheme.danger)))
              : TabBarView(
                  controller: _tabs,
                  children: [
                    _overviewTab(f),
                    _prepTab(),
                    _timelineTab(),
                    _lineupsTab(f),
                    _reportTab(f),
                    _commentsTab(),
                    _historyTab(),
                  ],
                ),
    );
  }

  Widget _overviewTab(FixtureModel f) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        ScoreBoard(
          home: f.homeTeamName,
          away: f.awayTeamName,
          homeScore: f.homeScore,
          awayScore: f.awayScore,
          subtitle: '${f.matchDate} · ${f.matchTime}',
        ),
        const SizedBox(height: 16),
        _infoCard([
          ('Stadium', f.stadium),
          ('Status', f.status.replaceAll('_', ' ')),
          ('Report entries', '${_entries.length}'),
          ('Squad players', '${_home.length + _away.length}'),
          ('Suspensions', '${_suspensions.length}'),
          ('Lineup listed', '${_homeLineup.length + _awayLineup.length}'),
          ('Comments', '${_comments.length}'),
        ]),
        if (_entries.isNotEmpty) ...[
          const SizedBox(height: 16),
          const Text('Submitted report', style: TextStyle(fontWeight: FontWeight.w800)),
          const SizedBox(height: 8),
          ..._entries.map(_reportReviewRow),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: () => _tabs.animateTo(4),
            icon: const Icon(Icons.visibility_outlined),
            label: Text(f.canSubmitReport ? 'Open full report editor' : 'Review full report'),
          ),
        ],
        if (_suspensions.isNotEmpty) ...[
          const SizedBox(height: 16),
          const Text('Ineligible / suspended', style: TextStyle(fontWeight: FontWeight.w800)),
          const SizedBox(height: 8),
          ..._suspensions.map((s) => Container(
                margin: const EdgeInsets.only(bottom: 8),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFFFEE2E2),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.block, color: AppTheme.danger, size: 18),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text('#${s.playerNumber ?? '-'} ${s.playerName} · ${s.reasonLabel}',
                          style: const TextStyle(fontWeight: FontWeight.w600)),
                    ),
                  ],
                ),
              )),
        ],
        const SizedBox(height: 16),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            FilledButton.icon(
              onPressed: () => _tabs.animateTo(1),
              icon: const Icon(Icons.checklist),
              label: Text('Matchday prep ($_prepDone/7)'),
            ),
            if (f.canSubmitReport)
              FilledButton.icon(
                onPressed: () => _tabs.animateTo(4),
                icon: const Icon(Icons.edit_note),
                label: Text(f.status == 'REPORTED' ? 'Update report' : 'Start report'),
              ),
            OutlinedButton.icon(
              onPressed: () => _tabs.animateTo(2),
              icon: const Icon(Icons.timeline),
              label: const Text('Timeline'),
            ),
            OutlinedButton.icon(
              onPressed: () => _tabs.animateTo(5),
              icon: const Icon(Icons.chat_bubble_outline),
              label: const Text('Comment'),
            ),
          ],
        ),
      ],
    );
  }

  Future<void> _savePrep() async {
    setState(() => _savingPrep = true);
    try {
      final body = {
        ..._prep,
        'notes': _prepNotesCtrl.text.trim(),
      };
      final saved = await context.read<AuthService>().api.put(
            '/api/fixtures/${widget.fixture.id}/prep',
            body,
          );
      setState(() {
        _prepDone = (saved['completedCount'] as num?)?.toInt() ?? _prep.values.where((v) => v).length;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Matchday prep saved')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
        );
      }
    } finally {
      if (mounted) setState(() => _savingPrep = false);
    }
  }

  Widget _prepTab() {
    final items = <(String, String)>[
      ('pitchChecked', 'Pitch inspected & playable'),
      ('ballsChecked', 'Match balls checked'),
      ('netsChecked', 'Goal nets secured'),
      ('captainsBriefed', 'Captains briefed'),
      ('lineupsReceived', 'Official lineups received'),
      ('medicalReady', 'Medical / stretcher ready'),
      ('securityOk', 'Security / stewarding OK'),
    ];
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text('Matchday checklist', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
        const SizedBox(height: 4),
        Text('Tick items as you complete them. Progress: $_prepDone / 7',
            style: TextStyle(color: Colors.grey.shade600, fontSize: 13)),
        const SizedBox(height: 12),
        ...items.map((it) => CheckboxListTile(
              value: _prep[it.$1] ?? false,
              onChanged: (v) => setState(() {
                _prep[it.$1] = v ?? false;
                _prepDone = _prep.values.where((x) => x).length;
              }),
              title: Text(it.$2),
              controlAffinity: ListTileControlAffinity.leading,
              contentPadding: EdgeInsets.zero,
            )),
        const SizedBox(height: 8),
        TextField(
          controller: _prepNotesCtrl,
          maxLines: 3,
          decoration: const InputDecoration(
            labelText: 'Prep notes',
            hintText: 'Arrive time, weather, stadium contacts…',
          ),
        ),
        const SizedBox(height: 16),
        FilledButton.icon(
          onPressed: _savingPrep ? null : _savePrep,
          icon: _savingPrep
              ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
              : const Icon(Icons.save_outlined),
          label: const Text('Save checklist'),
        ),
      ],
    );
  }

  Widget _infoCard(List<(String, String)> rows) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(16)),
      child: Column(
        children: [
          for (var i = 0; i < rows.length; i++) ...[
            if (i > 0) const Divider(height: 20),
            Row(
              children: [
                Expanded(child: Text(rows[i].$1, style: TextStyle(color: Colors.grey.shade600))),
                Text(rows[i].$2, style: const TextStyle(fontWeight: FontWeight.w700)),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _timelineTab() {
    final events = _timeline;
    if (events.isEmpty) {
      return const Center(child: Text('No timed events yet — submit goals/cards with minutes'));
    }
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: events.length,
      separatorBuilder: (_, __) => const SizedBox(height: 8),
      itemBuilder: (_, i) {
        final e = events[i];
        return Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(14)),
          child: Row(
            children: [
              Container(
                width: 52,
                padding: const EdgeInsets.symmetric(vertical: 8),
                decoration: BoxDecoration(color: e.color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(10)),
                child: Text("${e.minute}'", textAlign: TextAlign.center,
                    style: TextStyle(color: e.color, fontWeight: FontWeight.w800)),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(e.label, style: TextStyle(color: e.color, fontWeight: FontWeight.w800, fontSize: 12)),
                    Text(e.detail, style: const TextStyle(fontWeight: FontWeight.w600)),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _lineupsTab(FixtureModel f) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _lineupBlock(f.homeTeamName, _homeLineup),
        const SizedBox(height: 16),
        _lineupBlock(f.awayTeamName, _awayLineup),
        if (_homeLineup.isEmpty && _awayLineup.isEmpty)
          Padding(
            padding: const EdgeInsets.only(top: 24),
            child: Text('No official lineups submitted for this fixture yet.',
                textAlign: TextAlign.center, style: TextStyle(color: Colors.grey.shade600)),
          ),
      ],
    );
  }

  Widget _lineupBlock(String title, List<LineupPlayer> players) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: const TextStyle(fontWeight: FontWeight.w800, color: AppTheme.brand)),
        const SizedBox(height: 8),
        if (players.isEmpty)
          Text('No lineup', style: TextStyle(color: Colors.grey.shade600))
        else
          ...players.map((p) => Container(
                margin: const EdgeInsets.only(bottom: 6),
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                decoration: BoxDecoration(
                  color: p.suspended ? const Color(0xFFFEE2E2) : Colors.white,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  children: [
                    SizedBox(
                      width: 36,
                      child: Text('#${p.playerNumber ?? '-'}', style: const TextStyle(fontWeight: FontWeight.w800)),
                    ),
                    Expanded(child: Text(p.playerName, style: const TextStyle(fontWeight: FontWeight.w600))),
                    if (p.position != null)
                      Text(p.position!, style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
                    if (p.suspended) ...[
                      const SizedBox(width: 8),
                      const Icon(Icons.block, color: AppTheme.danger, size: 16),
                    ],
                  ],
                ),
              )),
      ],
    );
  }

  Widget _reportReviewRow(ReportEntry e) {
    final bits = <String>[];
    if (e.goal > 0) bits.add('${e.goal} goal${e.goal == 1 ? '' : 's'}${e.goalMin != null ? " (${e.goalMin}')" : ''}');
    if (e.card != 'NONE') bits.add('${e.card}${e.cardMin != null ? " (${e.cardMin}')" : ''}');
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(e.playerName, style: const TextStyle(fontWeight: FontWeight.w700)),
                Text(e.teamName, style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
              ],
            ),
          ),
          Flexible(
            child: Text(
              bits.isEmpty ? e.status : bits.join(' · '),
              textAlign: TextAlign.end,
              style: const TextStyle(fontWeight: FontWeight.w600, color: AppTheme.brand, fontSize: 12),
            ),
          ),
        ],
      ),
    );
  }

  Widget _reportTab(FixtureModel f) {
    // Read-only review for approved / locked matches
    if (!f.canSubmitReport) {
      return ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(color: AppTheme.brandSoft, borderRadius: BorderRadius.circular(12)),
            child: Text(
              'This report is locked (${f.status.replaceAll('_', ' ')}). You can review it below.',
              style: const TextStyle(color: AppTheme.brand, fontWeight: FontWeight.w600),
            ),
          ),
          const SizedBox(height: 16),
          ScoreBoard(
            home: f.homeTeamName,
            away: f.awayTeamName,
            homeScore: f.homeScore,
            awayScore: f.awayScore,
            subtitle: 'Official result',
          ),
          const SizedBox(height: 16),
          Text('Report details', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
          const SizedBox(height: 8),
          if (_entries.isEmpty)
            Text('No report entries were filed for this match.', style: TextStyle(color: Colors.grey.shade600))
          else
            ..._entries.map(_reportReviewRow),
          const SizedBox(height: 12),
          OutlinedButton.icon(
            onPressed: () => _tabs.animateTo(2),
            icon: const Icon(Icons.timeline),
            label: const Text('Open event timeline'),
          ),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: () => _tabs.animateTo(6),
            icon: const Icon(Icons.history),
            label: const Text('Open edit history'),
          ),
        ],
      );
    }

    final home = _filter(_home);
    final away = _filter(_away);
    return Column(
      children: [
        if (_entries.isNotEmpty)
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(color: const Color(0xFFECFDF5), borderRadius: BorderRadius.circular(12)),
              child: Text(
                'Existing report loaded (${_entries.length} entries). Edit below, then submit to update.',
                style: const TextStyle(color: AppTheme.accent, fontWeight: FontWeight.w600, fontSize: 13),
              ),
            ),
          ),
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
          child: TextField(
            controller: _searchCtrl,
            decoration: InputDecoration(
              hintText: 'Search player by name or number',
              prefixIcon: const Icon(Icons.search),
              suffixIcon: _search.isEmpty
                  ? null
                  : IconButton(
                      onPressed: () {
                        _searchCtrl.clear();
                        setState(() => _search = '');
                      },
                      icon: const Icon(Icons.clear),
                    ),
            ),
            onChanged: (v) => setState(() => _search = v),
          ),
        ),
        Expanded(
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Text(f.title, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
              const SizedBox(height: 4),
              Text('Enter goals and cards. Suspended players are flagged.',
                  style: TextStyle(color: Colors.grey.shade600, fontSize: 13)),
              const SizedBox(height: 16),
              _teamSection(f.homeTeamName, home),
              const SizedBox(height: 16),
              _teamSection(f.awayTeamName, away),
            ],
          ),
        ),
        SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
            child: FilledButton(
              onPressed: _submitting ? null : _submit,
              child: _submitting
                  ? const SizedBox(
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                    )
                  : const Text('Submit / update report'),
            ),
          ),
        ),
      ],
    );
  }

  Widget _teamSection(String title, List<_PlayerEntry> players) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: const TextStyle(fontWeight: FontWeight.w800, color: AppTheme.brand)),
        const SizedBox(height: 8),
        if (players.isEmpty)
          Text('No players match search', style: TextStyle(color: Colors.grey.shade600))
        else
          ...players.map(_playerTile),
      ],
    );
  }

  Widget _playerTile(_PlayerEntry p) {
    final active = (int.tryParse(p.goalsCtrl.text) ?? 0) > 0 || p.card != 'NONE';
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: p.suspended ? const Color(0xFFFFF7ED) : Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(
          color: p.suspended
              ? AppTheme.warn.withValues(alpha: 0.5)
              : active
                  ? AppTheme.brand.withValues(alpha: 0.35)
                  : const Color(0xFFE5E7EB),
        ),
      ),
      child: ExpansionTile(
        tilePadding: const EdgeInsets.symmetric(horizontal: 14),
        childrenPadding: const EdgeInsets.fromLTRB(14, 0, 14, 14),
        title: Row(
          children: [
            Expanded(child: Text('#${p.number ?? '-'} ${p.name}', style: const TextStyle(fontWeight: FontWeight.w700))),
            if (p.suspended)
              const Padding(
                padding: EdgeInsets.only(left: 6),
                child: Icon(Icons.gavel, size: 16, color: AppTheme.warn),
              ),
          ],
        ),
        subtitle: Text(
          p.suspended
              ? 'SUSPENDED · ${p.suspensionLabel ?? 'Ineligible'}'
              : (active ? _eventSummary(p) : 'No events'),
          style: TextStyle(color: p.suspended ? AppTheme.warn : Colors.grey.shade600, fontSize: 12),
        ),
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: p.goalsCtrl,
                  decoration: const InputDecoration(labelText: 'Goals'),
                  keyboardType: TextInputType.number,
                  onChanged: (_) => setState(() {}),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: TextField(
                  controller: p.goalMinCtrl,
                  decoration: const InputDecoration(labelText: 'Goal min'),
                  keyboardType: TextInputType.number,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          DropdownButtonFormField<String>(
            value: p.card,
            decoration: const InputDecoration(labelText: 'Card'),
            items: const [
              DropdownMenuItem(value: 'NONE', child: Text('None')),
              DropdownMenuItem(value: 'YELLOW', child: Text('Yellow')),
              DropdownMenuItem(value: 'RED', child: Text('Red')),
            ],
            onChanged: (v) => setState(() => p.card = v ?? 'NONE'),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: p.cardMinCtrl,
            decoration: const InputDecoration(labelText: 'Card minute'),
            keyboardType: TextInputType.number,
          ),
        ],
      ),
    );
  }

  String _eventSummary(_PlayerEntry p) {
    final parts = <String>[];
    final goals = int.tryParse(p.goalsCtrl.text) ?? 0;
    if (goals > 0) parts.add('$goals goal${goals == 1 ? '' : 's'}');
    if (p.card != 'NONE') parts.add(p.card.toLowerCase());
    return parts.join(' · ');
  }

  Widget _commentsTab() {
    return Column(
      children: [
        SizedBox(
          height: 44,
          child: ListView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
            children: [
              for (final t in _templates)
                Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: ActionChip(
                    label: Text(t.length > 28 ? '${t.substring(0, 28)}…' : t, style: const TextStyle(fontSize: 12)),
                    onPressed: _postingComment ? null : () => _postComment(t),
                  ),
                ),
            ],
          ),
        ),
        Expanded(
          child: _comments.isEmpty
              ? const Center(child: Text('No comments yet — try a quick template'))
              : ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: _comments.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 10),
                  itemBuilder: (_, i) {
                    final c = _comments[i];
                    return Container(
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(14)),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              CircleAvatar(
                                radius: 16,
                                backgroundColor: AppTheme.brandSoft,
                                child: Text(
                                  c.authorName.isNotEmpty ? c.authorName[0].toUpperCase() : '?',
                                  style: const TextStyle(color: AppTheme.brand, fontWeight: FontWeight.w800),
                                ),
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(c.authorName, style: const TextStyle(fontWeight: FontWeight.w700)),
                                    Text('${c.authorRole} · ${_fmt(c.createdAt)}',
                                        style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
                                  ],
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 10),
                          Text(c.body),
                        ],
                      ),
                    );
                  },
                ),
        ),
        SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _commentCtrl,
                    minLines: 1,
                    maxLines: 3,
                    decoration: const InputDecoration(hintText: 'Add a comment (name & time saved)'),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  onPressed: _postingComment ? null : () => _postComment(),
                  style: IconButton.styleFrom(backgroundColor: AppTheme.brand),
                  icon: _postingComment
                      ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                      : const Icon(Icons.send),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _historyTab() {
    if (_edits.isEmpty) {
      return const Center(child: Text('No edit history yet'));
    }
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: _edits.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (_, i) {
        final e = _edits[i];
        return Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(14)),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(child: Text(e.editorName, style: const TextStyle(fontWeight: FontWeight.w800))),
                  Text(_fmt(e.createdAt), style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
                ],
              ),
              const SizedBox(height: 4),
              Text('${e.editorRole} · ${e.action}',
                  style: const TextStyle(color: AppTheme.brand, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              Text(e.summary),
            ],
          ),
        );
      },
    );
  }
}
