import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../models/fixture_model.dart';
import '../models/report_models.dart';
import '../services/auth_service.dart';
import '../theme/app_theme.dart';
import '../widgets/common.dart';

class MatchHubScreen extends StatefulWidget {
  final FixtureModel fixture;
  const MatchHubScreen({super.key, required this.fixture});

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

  _PlayerEntry({
    required this.memberId,
    required this.name,
    this.number,
    required this.teamLabel,
    int goals = 0,
    int? goalMin,
    this.card = 'NONE',
    int? cardMin,
  })  : goalsCtrl = TextEditingController(text: goals == 0 ? '' : '$goals'),
        goalMinCtrl = TextEditingController(text: goalMin?.toString() ?? ''),
        cardMinCtrl = TextEditingController(text: cardMin?.toString() ?? '');

  void dispose() {
    goalsCtrl.dispose();
    goalMinCtrl.dispose();
    cardMinCtrl.dispose();
  }
}

class _MatchHubScreenState extends State<MatchHubScreen> with SingleTickerProviderStateMixin {
  late final TabController _tabs;
  List<_PlayerEntry> _home = [];
  List<_PlayerEntry> _away = [];
  List<ReportComment> _comments = [];
  List<ReportEditLog> _edits = [];
  final _commentCtrl = TextEditingController();
  bool _loading = true;
  bool _submitting = false;
  bool _postingComment = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _tabs = TabController(length: 4, vsync: this);
    _bootstrap();
  }

  @override
  void dispose() {
    _tabs.dispose();
    _commentCtrl.dispose();
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
      final results = await Future.wait([
        api.get('/api/teams/${f.homeTeamId}/members'),
        api.get('/api/teams/${f.awayTeamId}/members'),
        api.get('/api/fixtures/${f.id}/report'),
        api.get('/api/fixtures/${f.id}/report/comments'),
        api.get('/api/fixtures/${f.id}/report/edits'),
      ]);

      final existing = (results[2] as List)
          .map((e) => ReportEntry.fromJson(Map<String, dynamic>.from(e)))
          .toList();
      final byId = {for (final e in existing) e.teamMemberId: e};

      List<_PlayerEntry> mapPlayers(List raw, String teamName) {
        return raw
            .where((m) => m['roleInTeam'] == 'PLAYER')
            .map((m) {
              final id = m['memberId'] as int;
              final ex = byId[id];
              return _PlayerEntry(
                memberId: id,
                name: '${m['fname']} ${m['lname']}',
                number: m['number'],
                teamLabel: teamName,
                goals: ex?.goal ?? 0,
                goalMin: ex?.goalMin,
                card: ex?.card ?? 'NONE',
                cardMin: ex?.cardMin,
              );
            })
            .toList();
      }

      for (final p in [..._home, ..._away]) {
        p.dispose();
      }

      setState(() {
        _home = mapPlayers(results[0] as List, f.homeTeamName);
        _away = mapPlayers(results[1] as List, f.awayTeamName);
        _comments = (results[3] as List)
            .map((e) => ReportComment.fromJson(Map<String, dynamic>.from(e)))
            .toList();
        _edits = (results[4] as List)
            .map((e) => ReportEditLog.fromJson(Map<String, dynamic>.from(e)))
            .toList();
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
      _tabs.animateTo(3);
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

  Future<void> _postComment() async {
    final body = _commentCtrl.text.trim();
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
          ('Players loaded', '${_home.length + _away.length}'),
          ('Comments', '${_comments.length}'),
        ]),
        const SizedBox(height: 16),
        if (f.canSubmitReport)
          FilledButton.icon(
            onPressed: () => _tabs.animateTo(1),
            icon: const Icon(Icons.edit_note),
            label: Text(f.status == 'REPORTED' ? 'Update match report' : 'Start match report'),
          )
        else
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(color: AppTheme.brandSoft, borderRadius: BorderRadius.circular(12)),
            child: Text('Report locked (${f.status.replaceAll('_', ' ')})',
                style: const TextStyle(color: AppTheme.brand, fontWeight: FontWeight.w600)),
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

  Widget _reportTab(FixtureModel f) {
    return Column(
      children: [
        Expanded(
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Text(f.title, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
              const SizedBox(height: 4),
              Text('Enter goals and disciplinary cards. Only players with events are submitted.',
                  style: TextStyle(color: Colors.grey.shade600, fontSize: 13)),
              const SizedBox(height: 16),
              _teamSection(f.homeTeamName, _home),
              const SizedBox(height: 16),
              _teamSection(f.awayTeamName, _away),
            ],
          ),
        ),
        SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
            child: FilledButton(
              onPressed: _submitting || !f.canSubmitReport ? null : _submit,
              child: _submitting
                  ? const SizedBox(
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                    )
                  : Text(f.canSubmitReport ? 'Submit / update report' : 'Report locked'),
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
        ...players.map(_playerTile),
      ],
    );
  }

  Widget _playerTile(_PlayerEntry p) {
    final active = (int.tryParse(p.goalsCtrl.text) ?? 0) > 0 || p.card != 'NONE';
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: active ? AppTheme.brand.withValues(alpha: 0.35) : const Color(0xFFE5E7EB)),
      ),
      child: ExpansionTile(
        tilePadding: const EdgeInsets.symmetric(horizontal: 14),
        childrenPadding: const EdgeInsets.fromLTRB(14, 0, 14, 14),
        title: Text('#${p.number ?? '-'} ${p.name}', style: const TextStyle(fontWeight: FontWeight.w700)),
        subtitle: Text(active ? _eventSummary(p) : 'No events', style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
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
        Expanded(
          child: _comments.isEmpty
              ? const Center(child: Text('No comments yet'))
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
                  onPressed: _postingComment ? null : _postComment,
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
              Text('${e.editorRole} · ${e.action}', style: const TextStyle(color: AppTheme.brand, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              Text(e.summary),
            ],
          ),
        );
      },
    );
  }
}
