import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'screens/fixtures_screen.dart';
import 'screens/login_screen.dart';
import 'screens/notifications_screen.dart';
import 'screens/profile_screen.dart';
import 'screens/schedule_screen.dart';
import 'screens/splash_screen.dart';
import 'screens/stats_screen.dart';
import 'services/auth_service.dart';
import 'theme/app_theme.dart';

class HomeShell extends StatefulWidget {
  const HomeShell({super.key});

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int _index = 0;
  int _unread = 0;
  final _notifKey = GlobalKey<NotificationsScreenState>();
  late final List<Widget> _pages;

  @override
  void initState() {
    super.initState();
    _pages = [
      const FixturesScreen(),
      const ScheduleScreen(),
      const StatsScreen(),
      NotificationsScreen(key: _notifKey),
      ProfileScreen(onLogout: () {
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const AppRoot()),
          (_) => false,
        );
      }),
    ];
    _refreshUnread();
  }

  Future<void> _refreshUnread() async {
    try {
      final data = await context.read<AuthService>().api.get('/api/notifications/unread-count');
      if (!mounted) return;
      setState(() => _unread = (data['count'] as num?)?.toInt() ?? 0);
    } catch (_) {}
  }

  String get _title {
    switch (_index) {
      case 1:
        return 'Schedule';
      case 2:
        return 'Insights';
      case 3:
        return 'Alerts';
      case 4:
        return 'Account';
      default:
        return 'Match Centre';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_title),
        actions: [
          if (_index == 3)
            IconButton(
              tooltip: 'Refresh',
              onPressed: () async {
                await _notifKey.currentState?.reload();
                await _refreshUnread();
              },
              icon: const Icon(Icons.refresh),
            ),
        ],
      ),
      body: IndexedStack(index: _index, children: _pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) async {
          setState(() => _index = i);
          if (i == 3) {
            await _notifKey.currentState?.reload();
          }
          await _refreshUnread();
        },
        destinations: [
          const NavigationDestination(
            icon: Icon(Icons.sports_soccer_outlined),
            selectedIcon: Icon(Icons.sports_soccer),
            label: 'Matches',
          ),
          const NavigationDestination(
            icon: Icon(Icons.calendar_month_outlined),
            selectedIcon: Icon(Icons.calendar_month),
            label: 'Schedule',
          ),
          const NavigationDestination(
            icon: Icon(Icons.insights_outlined),
            selectedIcon: Icon(Icons.insights),
            label: 'Stats',
          ),
          NavigationDestination(
            icon: Badge(
              isLabelVisible: _unread > 0,
              label: Text('$_unread'),
              child: const Icon(Icons.notifications_outlined),
            ),
            selectedIcon: Badge(
              isLabelVisible: _unread > 0,
              label: Text('$_unread'),
              child: const Icon(Icons.notifications),
            ),
            label: 'Alerts',
          ),
          const NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person),
            label: 'Account',
          ),
        ],
      ),
    );
  }
}

class AppRoot extends StatefulWidget {
  const AppRoot({super.key});

  @override
  State<AppRoot> createState() => _AppRootState();
}

class _AppRootState extends State<AppRoot> {
  bool _booting = true;
  bool _showSplash = true;

  @override
  void initState() {
    super.initState();
    _boot();
  }

  Future<void> _boot() async {
    await context.read<AuthService>().init();
    if (mounted) setState(() => _booting = false);
  }

  @override
  Widget build(BuildContext context) {
    if (_showSplash || _booting) {
      return SplashScreen(
        onReady: () {
          if (mounted) setState(() => _showSplash = false);
        },
      );
    }
    final auth = context.watch<AuthService>();
    if (!auth.sessionChecked) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    if (!auth.isLoggedIn) {
      return LoginScreen(onSuccess: () => setState(() {}));
    }
    return const HomeShell();
  }
}

class FerwafaApp extends StatelessWidget {
  const FerwafaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => AuthService(),
      child: MaterialApp(
        title: 'FERWAFA Referee',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.light(),
        home: const AppRoot(),
      ),
    );
  }
}
