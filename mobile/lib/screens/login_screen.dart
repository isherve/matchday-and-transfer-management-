import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/auth_service.dart';
import 'fixtures_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _emailController = TextEditingController(text: 'jhabimana@ferwafa.rw');
  final _codeController = TextEditingController(text: 'REF001');
  String? _error;

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Spacer(),
              Center(
                child: Image.asset('assets/images/ferwafa-logo.png', width: 96, height: 96),
              ),
              const SizedBox(height: 16),
              const Text('FERWAFA Referee', textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF133E8D))),
              const SizedBox(height: 8),
              const Text('Sign in with your email and access code',
                  textAlign: TextAlign.center, style: TextStyle(color: Colors.black54)),
              const SizedBox(height: 32),
              TextField(
                controller: _emailController,
                decoration: const InputDecoration(labelText: 'Email', border: OutlineInputBorder()),
                keyboardType: TextInputType.emailAddress,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _codeController,
                decoration: const InputDecoration(labelText: 'Access Code', border: OutlineInputBorder()),
                obscureText: true,
              ),
              if (_error != null) ...[
                const SizedBox(height: 12),
                Text(_error!, style: const TextStyle(color: Colors.red)),
              ],
              const SizedBox(height: 24),
              FilledButton(
                onPressed: auth.loading ? null : () async {
                  setState(() => _error = null);
                  try {
                    await auth.login(_emailController.text.trim(), _codeController.text);
                    if (!context.mounted) return;
                    Navigator.pushReplacement(context,
                        MaterialPageRoute(builder: (_) => const FixturesScreen()));
                  } catch (e) {
                    setState(() => _error = e.toString());
                  }
                },
                style: FilledButton.styleFrom(backgroundColor: const Color(0xFF133E8D), padding: const EdgeInsets.all(16)),
                child: auth.loading
                    ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                    : const Text('Login'),
              ),
              const Spacer(),
            ],
          ),
        ),
      ),
    );
  }
}
