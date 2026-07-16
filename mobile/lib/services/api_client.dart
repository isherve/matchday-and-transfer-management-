import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/api_config.dart';
import 'auth_exception.dart';

class ApiClient {
  String? _token;
  void Function()? onUnauthorized;

  void setToken(String? token) => _token = token;

  bool get hasToken => _token != null && _token!.trim().isNotEmpty;

  Map<String, String> get _headers => {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        if (hasToken) 'Authorization': 'Bearer ${_token!.trim()}',
      };

  Future<dynamic> get(String path) async {
    final res = await http.get(Uri.parse('${ApiConfig.baseUrl}$path'), headers: _headers);
    return _handle(res);
  }

  Future<dynamic> post(String path, Map<String, dynamic> body) async {
    final res = await http.post(
      Uri.parse('${ApiConfig.baseUrl}$path'),
      headers: _headers,
      body: jsonEncode(body),
    );
    return _handle(res);
  }

  Future<dynamic> put(String path, [Map<String, dynamic>? body]) async {
    final res = await http.put(
      Uri.parse('${ApiConfig.baseUrl}$path'),
      headers: _headers,
      body: body == null ? null : jsonEncode(body),
    );
    return _handle(res);
  }

  dynamic _handle(http.Response res) {
    if (res.statusCode >= 200 && res.statusCode < 300) {
      if (res.body.isEmpty) return null;
      return jsonDecode(res.body);
    }
    dynamic err = {};
    try {
      if (res.body.isNotEmpty) err = jsonDecode(res.body);
    } catch (_) {}
    final message = (err['message'] ?? 'Request failed (${res.statusCode})').toString();
    if (res.statusCode == 401) {
      onUnauthorized?.call();
      throw AuthException(message, statusCode: 401);
    }
    throw Exception(message);
  }
}
