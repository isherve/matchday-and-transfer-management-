import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/fixture_model.dart';
import '../services/auth_service.dart';
import '../theme/app_theme.dart';
import '../widgets/common.dart';
import 'match_hub_screen.dart';

class FixturesScreen extends StatefulWidget {
  const FixturesScreen({super.key});

  @override
  State<FixturesScreen> createState() => _FixturesScreenState();
}

class _FixturesScreenState extends State<FixturesScreen> {
  List<FixtureModel> _fixtures = [];
  bool _loading = true;
  String? _error;
  bool _authError = false;
  String _filter = 'ALL';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
      _authError = false;
    });
    try {
      final auth = context.read<AuthService>();
      if (!auth.isLoggedIn) {
        setState(() {
          _error = 'Session expired. Please sign in again.';
          _authError = true;
          _loading = false;
        });
        return;
      }
      final data = await auth.api.get('/api/fixtures') as List;
      setState(() {
        _fixtures = data.map((e) => FixtureModel.fromJson(Map<String, dynamic>.from(e))).toList();
        _loading = false;
      });
    } catch (e) {
      final msg = e.toString().replaceFirst('Exception: ', '');
      setState(() {
        _error = msg;
        _authError = msg.toLowerCase().contains('auth') || msg.toLowerCase().contains('unauthorized');
        _loading = false;
      });
    }
  }

  List<FixtureModel> get _filtered {
    if (_filter == 'ALL') return _fixtures;
    if (_filter == 'ACTION') {
      return _fixtures.where((f) => f.canSubmitReport).toList();
    }
    return _fixtures.where((f) => f.status == _filter).toList();
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    final overdue = _fixtures.where((f) => f.canSubmitReport && f.status != 'REPORTED' && f.status != 'APPROVED').length;
    final reported = _fixtures.where((f) => f.status == 'REPORTED' || f.status == 'APPROVED').length;

    return RefreshIndicator(
      onRefresh: _load,
      child: CustomScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Hello, ${auth.displayName?.split(' ').first ?? 'Referee'}',
                      style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
                  const SizedBox(height: 4),
                  Text('Assigned matches, reports & matchday tools', style: TextStyle(color: Colors.grey.shade600)),
                  if (_fixtures.any((f) => f.week >= 4)) ...[
                    const SizedBox(height: 10),
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(color: AppTheme.brandSoft, borderRadius: BorderRadius.circular(12)),
                      child: const Text(
                        'Demo: Week 4 fixtures are ready — open a REFEREE ASSIGNED / PLAYED match to submit a live report.',
                        style: TextStyle(color: AppTheme.brand, fontSize: 12, fontWeight: FontWeight.w600),
                      ),
                    ),
                  ],
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      Expanded(child: _StatTile(label: 'Assigned', value: '${_fixtures.length}', color: AppTheme.brand)),
                      const SizedBox(width: 10),
                      Expanded(child: _StatTile(label: 'To report', value: '$overdue', color: AppTheme.warn)),
                      const SizedBox(width: 10),
                      Expanded(child: _StatTile(label: 'Filed', value: '$reported', color: AppTheme.accent)),
                    ],
                  ),
                  const SizedBox(height: 16),
                  SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: Row(
                      children: [
                        for (final f in const [
                          ('ALL', 'All'),
                          ('ACTION', 'Needs action'),
                          ('REPORTED', 'Submitted'),
                          ('APPROVED', 'Approved'),
                        ])
                          Padding(
                            padding: const EdgeInsets.only(right: 8),
                            child: ChoiceChip(
                              label: Text(f.$2),
                              selected: _filter == f.$1,
                              onSelected: (_) => setState(() => _filter = f.$1),
                              selectedColor: AppTheme.brandSoft,
                              labelStyle: TextStyle(
                                color: _filter == f.$1 ? AppTheme.brand : Colors.black87,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 8),
                ],
              ),
            ),
          ),
          if (_loading)
            const SliverFillRemaining(child: Center(child: CircularProgressIndicator()))
          else if (_error != null)
            SliverFillRemaining(
              child: Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(_authError ? Icons.lock_outline : Icons.error_outline,
                          size: 40, color: AppTheme.danger),
                      const SizedBox(height: 12),
                      Text(_error!, textAlign: TextAlign.center, style: const TextStyle(color: AppTheme.danger)),
                      const SizedBox(height: 8),
                      Text(
                        _authError
                            ? 'Your session ended (common after a server restart). Sign in again to load matches.'
                            : 'Check your connection and try again.',
                        textAlign: TextAlign.center,
                        style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
                      ),
                      const SizedBox(height: 16),
                      if (_authError)
                        FilledButton.icon(
                          onPressed: () async {
                            await context.read<AuthService>().logout();
                          },
                          icon: const Icon(Icons.login),
                          label: const Text('Sign in again'),
                        )
                      else
                        FilledButton(onPressed: _load, child: const Text('Retry')),
                    ],
                  ),
                ),
              ),
            )
          else if (_filtered.isEmpty)
            const SliverFillRemaining(child: Center(child: Text('No matches in this filter')))
          else
            SliverPadding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
              sliver: SliverList.separated(
                itemCount: _filtered.length,
                separatorBuilder: (_, __) => const SizedBox(height: 12),
                itemBuilder: (_, i) {
                  final f = _filtered[i];
                  return Material(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(16),
                    child: InkWell(
                      borderRadius: BorderRadius.circular(16),
                      onTap: () async {
                        await Navigator.push(
                          context,
                          MaterialPageRoute(builder: (_) => MatchHubScreen(fixture: f)),
                        );
                        _load();
                      },
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Container(
                                  width: 40,
                                  height: 40,
                                  alignment: Alignment.center,
                                  decoration: BoxDecoration(
                                    color: AppTheme.brandSoft,
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: Text('W${f.week}',
                                      style: const TextStyle(color: AppTheme.brand, fontWeight: FontWeight.w800)),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(f.title, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15)),
                                      const SizedBox(height: 2),
                                      Text('${f.matchDate} · ${f.matchTime}',
                                          style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
                                    ],
                                  ),
                                ),
                                StatusChip(status: f.status),
                              ],
                            ),
                            const SizedBox(height: 12),
                            Row(
                              children: [
                                Icon(Icons.stadium_outlined, size: 16, color: Colors.grey.shade600),
                                const SizedBox(width: 6),
                                Expanded(
                                  child: Text(f.stadium, style: TextStyle(color: Colors.grey.shade700, fontSize: 13)),
                                ),
                                if (f.homeScore != null)
                                  Text('${f.homeScore} – ${f.awayScore}',
                                      style: const TextStyle(fontWeight: FontWeight.w800, color: AppTheme.brand)),
                              ],
                            ),
                            if (f.canSubmitReport || f.status == 'APPROVED') ...[
                              const SizedBox(height: 12),
                              Row(
                                children: [
                                  Expanded(
                                    child: OutlinedButton.icon(
                                      onPressed: () => Navigator.push(
                                        context,
                                        MaterialPageRoute(
                                          builder: (_) => MatchHubScreen(fixture: f, initialTab: 1),
                                        ),
                                      ).then((_) => _load()),
                                      icon: const Icon(Icons.checklist, size: 18),
                                      label: const Text('Prep'),
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: FilledButton.icon(
                                      onPressed: f.canSubmitReport
                                          ? () => Navigator.push(
                                                context,
                                                MaterialPageRoute(
                                                  builder: (_) => MatchHubScreen(fixture: f, initialTab: 4),
                                                ),
                                              ).then((_) => _load())
                                          : null,
                                      icon: const Icon(Icons.edit_note, size: 18),
                                      label: Text(f.status == 'REPORTED' ? 'Update' : 'Report'),
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ],
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }
}

class _StatTile extends StatelessWidget {
  final String label;
  final String value;
  final Color color;
  const _StatTile({required this.label, required this.value, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(14)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(value, style: TextStyle(fontSize: 22, fontWeight: FontWeight.w800, color: color)),
          Text(label, style: TextStyle(fontSize: 12, color: Colors.grey.shade600)),
        ],
      ),
    );
  }
}
