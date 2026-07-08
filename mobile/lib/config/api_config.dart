class ApiConfig {
  // Flutter Chrome/Windows: localhost. Android emulator: --dart-define=API_URL=http://10.0.2.2:8081
  // Physical phone: --dart-define=API_URL=http://<your-pc-lan-ip>:8081
  static const String baseUrl = String.fromEnvironment(
    'API_URL',
    defaultValue: 'http://localhost:8081',
  );
}
