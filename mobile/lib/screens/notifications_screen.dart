import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../models/extra_models.dart';
import '../services/auth_service.dart';
import '../theme/app_theme.dart';

class NotificationsScreen extends StatefulWidget {
  const NotificationsScreen({super.key});

  @override
  State<NotificationsScreen> createState() => NotificationsScreenState();
}

class NotificationsScreenState extends State<NotificationsScreen> {
  List<NotificationModel> _items = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    reload();
  }

  Future<void> reload() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final data = await context.read<AuthService>().api.get('/api/notifications') as List;
      setState(() {
        _items = data.map((e) => NotificationModel.fromJson(Map<String, dynamic>.from(e))).toList();
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString().replaceFirst('Exception: ', '');
        _loading = false;
      });
    }
  }

  Future<void> _markAll() async {
    await context.read<AuthService>().api.put('/api/notifications/read-all');
    await reload();
  }

  Future<void> _markOne(NotificationModel n) async {
    if (!n.read) {
      await context.read<AuthService>().api.put('/api/notifications/${n.id}/read');
    }
    await reload();
  }

  IconData _icon(String type) {
    switch (type) {
      case 'PLAYER_SUSPENDED':
        return Icons.gavel_outlined;
      case 'FIXTURE_POSTPONED':
        return Icons.event_busy_outlined;
      case 'REPORT_SUBMITTED':
      case 'REPORT_APPROVED':
        return Icons.description_outlined;
      default:
        return Icons.notifications_outlined;
    }
  }

  @override
  Widget build(BuildContext context) {
    final unread = _items.where((n) => !n.read).length;
    return RefreshIndicator(
      onRefresh: reload,
      child: CustomScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Alerts', style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
                        Text('$unread unread · assignments, discipline & briefings',
                            style: TextStyle(color: Colors.grey.shade600, fontSize: 13)),
                      ],
                    ),
                  ),
                  if (unread > 0)
                    TextButton(onPressed: _markAll, child: const Text('Mark all read')),
                ],
              ),
            ),
          ),
          if (_loading)
            const SliverFillRemaining(child: Center(child: CircularProgressIndicator()))
          else if (_error != null)
            SliverFillRemaining(child: Center(child: Text(_error!, style: const TextStyle(color: AppTheme.danger))))
          else if (_items.isEmpty)
            const SliverFillRemaining(child: Center(child: Text('No notifications yet')))
          else
            SliverPadding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
              sliver: SliverList.separated(
                itemCount: _items.length,
                separatorBuilder: (_, __) => const SizedBox(height: 10),
                itemBuilder: (_, i) {
                  final n = _items[i];
                  return Material(
                    color: n.read ? Colors.white : AppTheme.brandSoft,
                    borderRadius: BorderRadius.circular(14),
                    child: InkWell(
                      borderRadius: BorderRadius.circular(14),
                      onTap: () => _markOne(n),
                      child: Padding(
                        padding: const EdgeInsets.all(14),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            CircleAvatar(
                              backgroundColor: Colors.white,
                              child: Icon(_icon(n.type), color: AppTheme.brand, size: 20),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Row(
                                    children: [
                                      Expanded(
                                        child: Text(n.title, style: TextStyle(fontWeight: n.read ? FontWeight.w600 : FontWeight.w800)),
                                      ),
                                      if (!n.read)
                                        Container(
                                          width: 8,
                                          height: 8,
                                          decoration: const BoxDecoration(color: AppTheme.brand, shape: BoxShape.circle),
                                        ),
                                    ],
                                  ),
                                  const SizedBox(height: 4),
                                  Text(n.message, style: TextStyle(color: Colors.grey.shade700, fontSize: 13)),
                                  const SizedBox(height: 6),
                                  Text(
                                    '${n.type.replaceAll('_', ' ')} · ${DateFormat('dd MMM · HH:mm').format(n.createdAt.toLocal())}',
                                    style: TextStyle(color: Colors.grey.shade500, fontSize: 11),
                                  ),
                                ],
                              ),
                            ),
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
