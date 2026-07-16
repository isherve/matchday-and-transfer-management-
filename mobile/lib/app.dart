import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'screens/fixtures_screen.dart';
import 'screens/login_screen.dart';
import 'screens/profile_screen.dart';
import 'screens/splash_screen.dart';
import 'services/auth_service.dart';
import 'theme/app_theme.dart';

class HomeShell extends StatefulWidget {
  const HomeShell({super.key});

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = [
      const FixturesScreen(),
      ProfileScreen(onLogout: () {
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const AppRoot()),
          (_) => false,
        );
      }),
    ];

    return Scaffold(
      appBar: AppBar(
        title: Text(_index == 0 ? 'Match Centre' : 'Account'),
      ),
      body: IndexedStack(index: _index, children: pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.sports_soccer_outlined), selectedIcon: Icon(Icons.sports_soccer), label: 'Matches'),
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: 'Account'),
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
