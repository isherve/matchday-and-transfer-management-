class AuthException implements Exception {
  final String message;
  final int statusCode;
  AuthException(this.message, {this.statusCode = 401});
  @override
  String toString() => message;
}
