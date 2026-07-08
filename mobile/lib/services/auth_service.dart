import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'api_client.dart';

class AuthService extends ChangeNotifier {
  final ApiClient _api = ApiClient();
  String? _token;
  String? _displayName;
  bool _loading = false;

  String? get token => _token;
  String? get displayName => _displayName;
  bool get isLoggedIn => _token != null;
  bool get loading => _loading;
  ApiClient get api => _api;

  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString('token');
    _displayName = prefs.getString('displayName');
    _api.setToken(_token);
    notifyListeners();
  }

  Future<bool> login(String email, String accessCode) async {
    _loading = true;
    notifyListeners();
    try {
      final data = await _api.post('/api/auth/referee-login', {
        'email': email,
        'accessCode': accessCode,
      });
      _token = data['token'];
      _displayName = data['displayName'];
      _api.setToken(_token);
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('token', _token!);
      await prefs.setString('displayName', _displayName ?? '');
      return true;
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    _token = null;
    _displayName = null;
    _api.setToken(null);
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
    notifyListeners();
  }
}
