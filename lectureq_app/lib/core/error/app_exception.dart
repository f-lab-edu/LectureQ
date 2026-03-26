sealed class AppException implements Exception {
  String get message;
}

class NetworkException extends AppException {
  @override
  final String message;

  NetworkException([this.message = '네트워크 연결을 확인해주세요']);
}

class ServerException extends AppException {
  final int statusCode;

  @override
  final String message;

  ServerException({required this.statusCode, required this.message});
}

class AppTimeoutException extends AppException {
  @override
  final String message;

  AppTimeoutException([this.message = '요청 시간이 초과되었습니다']);
}

class UnknownException extends AppException {
  @override
  final String message;

  UnknownException([this.message = '알 수 없는 오류가 발생했습니다']);
}
