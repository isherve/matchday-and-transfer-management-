import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'screens/splash_screen.dart';
import 'services/auth_service.dart';

void main() {
  runApp(const FerwafaApp());
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
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF133E8D)),
          useMaterial3: true,
        ),
        home: const SplashScreen(),
      ),
    );
  }
}
