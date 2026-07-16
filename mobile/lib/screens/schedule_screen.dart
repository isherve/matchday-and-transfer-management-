import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../models/fixture_model.dart';
import '../services/auth_service.dart';
import '../theme/app_theme.dart';
import '../widgets/common.dart';
import 'match_hub_screen.dart';

class ScheduleScreen extends StatefulWidget {
  const ScheduleScreen({super.key});

  @override
  State<ScheduleScreen> createState() => _ScheduleScreenState();
}

class _ScheduleScreenState extends State<ScheduleScreen> {
  List<FixtureModel> _fixtures = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final data = await context.read<AuthService>().api.get('/api/fixtures') as List;
      final list = data.map((e) => FixtureModel.fromJson(Map<String, dynamic>.from(e))).toList()
        ..sort((a, b) {
          final c = a.week.compareTo(b.week);
          return c != 0 ? c : a.matchDate.compareTo(b.matchDate);
        });
      setState(() {
        _fixtures = list;
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString().replaceFirst('Exception: ', '');
        _loading = false;
      });
    }
  }

  Map<int, List<FixtureModel>> get _byWeek {
    final map = <int, List<FixtureModel>>{};
    for (final f in _fixtures) {
      map.putIfAbsent(f.week, () => []).add(f);
    }
    return map;
  }

  @override
  Widget build(BuildContext context) {
    final weeks = _byWeek.keys.toList()..sort();
    return RefreshIndicator(
      onRefresh: _load,
      child: CustomScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Schedule', style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
                  Text('Your assignments by match week', style: TextStyle(color: Colors.grey.shade600)),
                ],
              ),
            ),
          ),
          if (_loading)
            const SliverFillRemaining(child: Center(child: CircularProgressIndicator()))
          else if (_error != null)
            SliverFillRemaining(child: Center(child: Text(_error!, style: const TextStyle(color: AppTheme.danger))))
          else if (weeks.isEmpty)
            const SliverFillRemaining(child: Center(child: Text('No assigned fixtures')))
          else
            SliverList.builder(
              itemCount: weeks.length,
              itemBuilder: (_, wi) {
                final week = weeks[wi];
                final matches = _byWeek[week]!;
                return Padding(
                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                            decoration: BoxDecoration(color: AppTheme.brand, borderRadius: BorderRadius.circular(8)),
                            child: Text('Week $week', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 12)),
                          ),
                          const SizedBox(width: 8),
                          Text('${matches.length} match${matches.length == 1 ? '' : 'es'}',
                              style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
                        ],
                      ),
                      const SizedBox(height: 10),
                      ...matches.map((f) {
                        DateTime? dt;
                        try {
                          dt = DateTime.tryParse('${f.matchDate} ${f.matchTime}');
                        } catch (_) {}
                        return Container(
                          margin: const EdgeInsets.only(bottom: 8),
                          decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(14)),
                          child: ListTile(
                            contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
                            title: Text(f.title, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14)),
                            subtitle: Text(
                              [
                                if (dt != null) DateFormat('EEE d MMM · HH:mm').format(dt) else '${f.matchDate} · ${f.matchTime}',
                                f.stadium,
                              ].join('\n'),
                            ),
                            isThreeLine: true,
                            trailing: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                StatusChip(status: f.status),
                                if (f.homeScore != null)
                                  Padding(
                                    padding: const EdgeInsets.only(top: 6),
                                    child: Text('${f.homeScore}-${f.awayScore}',
                                        style: const TextStyle(fontWeight: FontWeight.w800, color: AppTheme.brand)),
                                  ),
                              ],
                            ),
                            onTap: () => Navigator.push(
                              context,
                              MaterialPageRoute(builder: (_) => MatchHubScreen(fixture: f)),
                            ).then((_) => _load()),
                          ),
                        );
                      }),
                    ],
                  ),
                );
              },
            ),
        ],
      ),
    );
  }
}
