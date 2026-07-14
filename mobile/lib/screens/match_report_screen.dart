import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/fixture_model.dart';
import '../services/auth_service.dart';

class MatchReportScreen extends StatefulWidget {
  final FixtureModel fixture;
  const MatchReportScreen({super.key, required this.fixture});

  @override
  State<MatchReportScreen> createState() => _MatchReportScreenState();
}

class _PlayerEntry {
  final int memberId;
  final String name;
  final int? number;
  final String teamLabel;
  int goals = 0;
  int? goalMin;
  String card = 'NONE';
  int? cardMin;

  _PlayerEntry({
    required this.memberId,
    required this.name,
    this.number,
    required this.teamLabel,
  });
}

class _MatchReportScreenState extends State<MatchReportScreen> {
  List<_PlayerEntry> _players = [];
  bool _loading = true;
  bool _submitting = false;

  @override
  void initState() {
    super.initState();
    _loadPlayers();
  }

  Future<void> _loadPlayers() async {
    try {
      final auth = context.read<AuthService>();
      final teams = [
        (widget.fixture.homeTeamId, widget.fixture.homeTeamName),
        (widget.fixture.awayTeamId, widget.fixture.awayTeamName),
      ];
      final List<_PlayerEntry> all = [];
      for (final (teamId, teamName) in teams) {
        final members = await auth.api.get('/api/teams/$teamId/members') as List;
        for (final m in members) {
          if (m['roleInTeam'] == 'PLAYER') {
            all.add(_PlayerEntry(
              memberId: m['memberId'],
              name: '${m['fname']} ${m['lname']}',
              number: m['number'],
              teamLabel: teamName,
            ));
          }
        }
      }
      setState(() {
        _players = all;
        _loading = false;
      });
    } catch (e) {
      setState(() => _loading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
      }
    }
  }

  Future<void> _submit() async {
    setState(() => _submitting = true);
    try {
      final auth = context.read<AuthService>();
      final entries = _players.where((p) => p.goals > 0 || p.card != 'NONE').map((p) => {
            'teamMemberId': p.memberId,
            'goal': p.goals,
            'goalMin': p.goalMin,
            'card': p.card,
            'cardMin': p.cardMin,
          }).toList();

      await auth.api.post('/api/fixtures/${widget.fixture.id}/report', {'entries': entries});
      if (!mounted) return;
      showDialog(
        context: context,
        builder: (_) => AlertDialog(
          title: const Text('Report Submitted'),
          content: const Text('Match report has been submitted successfully.'),
          actions: [
            TextButton(
              onPressed: () => Navigator.popUntil(context, (r) => r.isFirst),
              child: const Text('OK'),
            ),
          ],
        ),
      );
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
      }
    } finally {
      setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Week ${widget.fixture.week} Report'),
        backgroundColor: const Color(0xFF133E8D),
        foregroundColor: Colors.white,
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : Column(
              children: [
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: Text(widget.fixture.title, style: Theme.of(context).textTheme.titleLarge),
                ),
                if (_players.isEmpty)
                  const Expanded(child: Center(child: Text('No players found for this fixture')))
                else
                  Expanded(
                    child: ListView.builder(
                      itemCount: _players.length,
                      itemBuilder: (_, i) => _buildPlayerCard(_players[i]),
                    ),
                  ),
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: FilledButton(
                    onPressed: _submitting || !widget.fixture.canSubmitReport ? null : _submit,
                    style: FilledButton.styleFrom(
                      backgroundColor: const Color(0xFF133E8D),
                      minimumSize: const Size(double.infinity, 48),
                    ),
                    child: _submitting
                        ? const CircularProgressIndicator(color: Colors.white)
                        : Text(widget.fixture.canSubmitReport ? 'Submit Report' : 'Report locked (${widget.fixture.status})'),
                  ),
                ),
              ],
            ),
    );
  }

  Widget _buildPlayerCard(_PlayerEntry p) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: ExpansionTile(
        title: Text('#${p.number ?? '-'} ${p.name}'),
        subtitle: Text(p.teamLabel),
        children: [
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              children: [
                Row(children: [
                  Expanded(
                    child: TextField(
                      decoration: const InputDecoration(labelText: 'Goals', border: OutlineInputBorder()),
                      keyboardType: TextInputType.number,
                      onChanged: (v) => p.goals = int.tryParse(v) ?? 0,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: TextField(
                      decoration: const InputDecoration(labelText: 'Goal Min', border: OutlineInputBorder()),
                      keyboardType: TextInputType.number,
                      onChanged: (v) => p.goalMin = int.tryParse(v),
                    ),
                  ),
                ]),
                const SizedBox(height: 8),
                DropdownButtonFormField<String>(
                  value: p.card,
                  decoration: const InputDecoration(labelText: 'Card', border: OutlineInputBorder()),
                  items: ['NONE', 'YELLOW', 'RED']
                      .map((c) => DropdownMenuItem(value: c, child: Text(c)))
                      .toList(),
                  onChanged: (v) => setState(() => p.card = v ?? 'NONE'),
                ),
                const SizedBox(height: 8),
                TextField(
                  decoration: const InputDecoration(labelText: 'Card Minute', border: OutlineInputBorder()),
                  keyboardType: TextInputType.number,
                  onChanged: (v) => p.cardMin = int.tryParse(v),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
