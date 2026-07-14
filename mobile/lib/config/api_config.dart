import 'package:flutter/foundation.dart';

class ApiConfig {
  /// Prefer --dart-define=API_URL=... when set.
  /// On Flutter web, defaults to the same host as the page (links referee app to the Spring API).
  /// On mobile/desktop without define, uses local backend.
  static String get baseUrl {
    const fromEnv = String.fromEnvironment('API_URL', defaultValue: '');
    if (fromEnv.isNotEmpty) {
      return fromEnv.endsWith('/') ? fromEnv.substring(0, fromEnv.length - 1) : fromEnv;
    }
    if (kIsWeb) {
      return Uri.base.origin;
    }
    return 'http://localhost:8081';
  }
}
