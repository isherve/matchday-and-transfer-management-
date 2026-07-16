import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';
import 'api_client.dart';
import 'auth_exception.dart';

class AuthService extends ChangeNotifier {
  final ApiClient _api = ApiClient();
  String? _token;
  String? _displayName;
  bool _loading = false;
  bool _sessionChecked = false;

  AuthService() {
    _api.onUnauthorized = _handleUnauthorized;
  }

  void _handleUnauthorized() {
    _token = null;
    _displayName = null;
    _api.setToken(null);
    SharedPreferences.getInstance().then((prefs) async {
      await prefs.remove('token');
      await prefs.remove('displayName');
      notifyListeners();
    });
  }

  String? get token => _token;
  String? get displayName => _displayName;
  bool get isLoggedIn => _token != null && _token!.trim().isNotEmpty;
  bool get loading => _loading;
  bool get sessionChecked => _sessionChecked;
  ApiClient get api => _api;
  String get apiHost => ApiConfig.baseUrl;

  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    final stored = prefs.getString('token');
    _displayName = prefs.getString('displayName');
    if (stored != null && stored.trim().isNotEmpty) {
      _token = stored.trim();
      _api.setToken(_token);
      final ok = await validateSession();
      if (!ok) {
        _token = null;
        _displayName = null;
        _api.setToken(null);
        await prefs.remove('token');
        await prefs.remove('displayName');
      }
    } else {
      _token = null;
      _api.setToken(null);
    }
    _sessionChecked = true;
    notifyListeners();
  }

  Future<bool> validateSession() async {
    if (!isLoggedIn) return false;
    final prev = _api.onUnauthorized;
    _api.onUnauthorized = null;
    try {
      final me = await _api.get('/api/auth/me');
      if (me is Map && me['displayName'] != null) {
        _displayName = me['displayName'].toString();
        final prefs = await SharedPreferences.getInstance();
        await prefs.setString('displayName', _displayName ?? '');
      }
      return true;
    } on AuthException {
      return false;
    } catch (_) {
      return true;
    } finally {
      _api.onUnauthorized = prev;
    }
  }

  Future<bool> login(String email, String accessCode) async {
    _loading = true;
    notifyListeners();
    final prev = _api.onUnauthorized;
    _api.onUnauthorized = null;
    try {
      final data = await _api.post('/api/auth/referee-login', {
        'email': email,
        'accessCode': accessCode,
      });
      final token = data['token']?.toString();
      if (token == null || token.isEmpty) {
        throw Exception('Login failed: no token returned');
      }
      _token = token;
      _displayName = data['displayName']?.toString();
      _api.setToken(_token);
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('token', _token!);
      await prefs.setString('displayName', _displayName ?? '');
      _sessionChecked = true;
      return true;
    } finally {
      _api.onUnauthorized = prev;
      _loading = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    final prev = _api.onUnauthorized;
    _api.onUnauthorized = null;
    try {
      await _api.post('/api/auth/logout', {});
    } catch (_) {
    } finally {
      _api.onUnauthorized = prev;
    }
    _token = null;
    _displayName = null;
    _api.setToken(null);
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('token');
    await prefs.remove('displayName');
    notifyListeners();
  }
}
