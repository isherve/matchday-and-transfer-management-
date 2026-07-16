import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/fixture_model.dart';
import '../services/auth_service.dart';
import '../theme/app_theme.dart';

class StatsScreen extends StatefulWidget {
  const StatsScreen({super.key});

  @override
  State<StatsScreen> createState() => _StatsScreenState();
}

class _StatsScreenState extends State<StatsScreen> {
  List<FixtureModel> _fixtures = [];
  List<Map<String, dynamic>> _scorers = [];
  List<Map<String, dynamic>> _cards = [];
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
      final api = context.read<AuthService>().api;
      final results = await Future.wait([
        api.get('/api/fixtures'),
        api.get('/api/reports/top-scorers'),
        api.get('/api/reports/cards-leaderboard'),
      ]);
      setState(() {
        _fixtures = (results[0] as List).map((e) => FixtureModel.fromJson(Map<String, dynamic>.from(e))).toList();
        _scorers = (results[1] as List).map((e) => Map<String, dynamic>.from(e)).toList();
        _cards = (results[2] as List).map((e) => Map<String, dynamic>.from(e)).toList();
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString().replaceFirst('Exception: ', '');
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final approved = _fixtures.where((f) => f.status == 'APPROVED').length;
    final reported = _fixtures.where((f) => f.status == 'REPORTED').length;
    final pending = _fixtures.where((f) => f.canSubmitReport && f.status != 'REPORTED').length;
    final goals = _fixtures.fold<int>(0, (s, f) => s + (f.homeScore ?? 0) + (f.awayScore ?? 0));

    return RefreshIndicator(
      onRefresh: _load,
      child: _loading
          ? ListView(children: const [SizedBox(height: 120), Center(child: CircularProgressIndicator())])
          : _error != null
              ? ListView(children: [Padding(padding: const EdgeInsets.all(24), child: Text(_error!, style: const TextStyle(color: AppTheme.danger)))])
              : ListView(
                  padding: const EdgeInsets.all(16),
                  children: [
                    Text('Insights', style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
                    const SizedBox(height: 4),
                    Text('Your assignments + league leaders', style: TextStyle(color: Colors.grey.shade600)),
                    const SizedBox(height: 10),
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(color: AppTheme.brandSoft, borderRadius: BorderRadius.circular(12)),
                      child: const Text(
                        'Stats fill when you submit match reports. Add personal notes under Account → Duty diary.',
                        style: TextStyle(color: AppTheme.brand, fontSize: 12, fontWeight: FontWeight.w600),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Expanded(child: _tile('Assigned', '${_fixtures.length}', AppTheme.brand)),
                        const SizedBox(width: 8),
                        Expanded(child: _tile('To report', '$pending', AppTheme.warn)),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        Expanded(child: _tile('Submitted', '$reported', AppTheme.warn)),
                        const SizedBox(width: 8),
                        Expanded(child: _tile('Approved', '$approved', AppTheme.accent)),
                      ],
                    ),
                    const SizedBox(height: 8),
                    _tile('Goals in your matches', '$goals', AppTheme.brandDark),
                    const SizedBox(height: 20),
                    const Text('Top scorers', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
                    const SizedBox(height: 8),
                    ..._scorers.take(8).toList().asMap().entries.map((e) {
                      final row = e.value;
                      final name = row['playerName'] ?? row['name'] ?? 'Player';
                      final team = row['teamName'] ?? row['club'] ?? '';
                      final goals = row['goals'] ?? row['goalCount'] ?? row['totalGoals'] ?? 0;
                      return _rankRow(e.key + 1, '$name', '$team', '$goals');
                    }),
                    if (_scorers.isEmpty)
                      Text('No scorers yet', style: TextStyle(color: Colors.grey.shade600)),
                    const SizedBox(height: 20),
                    const Text('Cards leaderboard', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
                    const SizedBox(height: 8),
                    ..._cards.take(8).toList().asMap().entries.map((e) {
                      final row = e.value;
                      final name = row['playerName'] ?? row['name'] ?? 'Player';
                      final team = row['teamName'] ?? row['club'] ?? '';
                      final y = row['yellowCards'] ?? row['yellow'] ?? 0;
                      final r = row['redCards'] ?? row['red'] ?? 0;
                      return _rankRow(e.key + 1, '$name', '$team', '${y}Y / ${r}R');
                    }),
                    if (_cards.isEmpty)
                      Text('No cards recorded yet', style: TextStyle(color: Colors.grey.shade600)),
                  ],
                ),
    );
  }

  Widget _tile(String label, String value, Color color) {
    return Container(
      width: double.infinity,
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

  Widget _rankRow(int rank, String title, String subtitle, String trailing) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
      child: Row(
        children: [
          CircleAvatar(
            radius: 14,
            backgroundColor: AppTheme.brandSoft,
            child: Text('$rank', style: const TextStyle(color: AppTheme.brand, fontWeight: FontWeight.w800, fontSize: 12)),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
                if (subtitle.isNotEmpty) Text(subtitle, style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
              ],
            ),
          ),
          Text(trailing, style: const TextStyle(fontWeight: FontWeight.w800, color: AppTheme.brand)),
        ],
      ),
    );
  }
}
