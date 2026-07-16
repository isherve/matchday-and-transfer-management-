import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../services/auth_service.dart';
import '../theme/app_theme.dart';

class ProfileScreen extends StatefulWidget {
  final VoidCallback onLogout;
  const ProfileScreen({super.key, required this.onLogout});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  List<Map<String, dynamic>> _diary = [];
  bool _loadingDiary = true;
  final _titleCtrl = TextEditingController();
  final _bodyCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadDiary();
  }

  @override
  void dispose() {
    _titleCtrl.dispose();
    _bodyCtrl.dispose();
    super.dispose();
  }

  Future<void> _loadDiary() async {
    setState(() => _loadingDiary = true);
    try {
      final data = await context.read<AuthService>().api.get('/api/referee/diary') as List;
      setState(() {
        _diary = data.map((e) => Map<String, dynamic>.from(e)).toList();
        _loadingDiary = false;
      });
    } catch (_) {
      setState(() => _loadingDiary = false);
    }
  }

  Future<void> _addDiary() async {
    final title = _titleCtrl.text.trim();
    final body = _bodyCtrl.text.trim();
    if (title.isEmpty || body.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Enter a title and note')));
      return;
    }
    try {
      await context.read<AuthService>().api.post('/api/referee/diary', {
        'title': title,
        'body': body,
      });
      _titleCtrl.clear();
      _bodyCtrl.clear();
      await _loadDiary();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Diary entry saved')));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    final initial = ((auth.displayName ?? 'R').trim().isEmpty ? 'R' : auth.displayName!.trim()[0]).toUpperCase();
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            gradient: const LinearGradient(colors: [AppTheme.brandDark, AppTheme.brand]),
            borderRadius: BorderRadius.circular(20),
          ),
          child: Row(
            children: [
              CircleAvatar(
                radius: 30,
                backgroundColor: Colors.white24,
                child: Text(initial, style: const TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w800)),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(auth.displayName ?? 'Referee',
                        style: const TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.w800)),
                    const SizedBox(height: 4),
                    const Text('Licensed FERWAFA Referee', style: TextStyle(color: Colors.white70)),
                    const SizedBox(height: 6),
                    Text('API: ${auth.apiHost}', style: const TextStyle(color: Colors.white54, fontSize: 11)),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        const Text('How you add data', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
        const SizedBox(height: 8),
        _how('Matches', 'Open a fixture → Prep checklist, Report goals/cards, Comments, Timeline'),
        _how('Schedule', 'Tap any week fixture → same match hub to add prep or report'),
        _how('Stats', 'Auto from your reports + league leaders (submit reports to feed stats)'),
        _how('Alerts', 'Tap to mark read; new alerts appear when you are assigned'),
        _how('Account', 'Add personal duty diary notes below'),
        const SizedBox(height: 16),
        const Text('Duty diary', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
        const SizedBox(height: 8),
        TextField(controller: _titleCtrl, decoration: const InputDecoration(labelText: 'Title')),
        const SizedBox(height: 8),
        TextField(controller: _bodyCtrl, maxLines: 3, decoration: const InputDecoration(labelText: 'Note')),
        const SizedBox(height: 8),
        FilledButton.icon(onPressed: _addDiary, icon: const Icon(Icons.add), label: const Text('Add diary entry')),
        const SizedBox(height: 12),
        if (_loadingDiary)
          const Center(child: Padding(padding: EdgeInsets.all(16), child: CircularProgressIndicator()))
        else if (_diary.isEmpty)
          Text('No diary entries yet', style: TextStyle(color: Colors.grey.shade600))
        else
          ..._diary.map((d) {
            final date = DateTime.tryParse(d['entryDate']?.toString() ?? '');
            return Container(
              margin: const EdgeInsets.only(bottom: 8),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(d['title']?.toString() ?? '', style: const TextStyle(fontWeight: FontWeight.w800)),
                  Text(
                    date != null ? DateFormat('dd MMM yyyy').format(date) : '',
                    style: TextStyle(color: Colors.grey.shade600, fontSize: 12),
                  ),
                  const SizedBox(height: 4),
                  Text(d['body']?.toString() ?? ''),
                ],
              ),
            );
          }),
        const SizedBox(height: 20),
        OutlinedButton.icon(
          onPressed: () async {
            await auth.logout();
            widget.onLogout();
          },
          icon: const Icon(Icons.logout),
          label: const Text('Sign out'),
          style: OutlinedButton.styleFrom(
            foregroundColor: AppTheme.danger,
            side: const BorderSide(color: AppTheme.danger),
            minimumSize: const Size.fromHeight(48),
          ),
        ),
      ],
    );
  }

  Widget _how(String title, String body) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(Icons.arrow_right_alt, color: AppTheme.brand),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(fontWeight: FontWeight.w800)),
                Text(body, style: TextStyle(color: Colors.grey.shade600, fontSize: 13)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
