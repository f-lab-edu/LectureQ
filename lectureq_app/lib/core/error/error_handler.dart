import 'package:dio/dio.dart';

import 'app_exception.dart';

class ErrorHandler {
  ErrorHandler._();

  static AppException handle(DioException exception) {
    switch (exception.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return AppTimeoutException();

      case DioExceptionType.connectionError:
      case DioExceptionType.badCertificate:
        return NetworkException();

      case DioExceptionType.badResponse:
        return _handleBadResponse(exception);

      case DioExceptionType.cancel:
      case DioExceptionType.unknown:
        return UnknownException();
    }
  }

  static ServerException _handleBadResponse(DioException exception) {
    final response = exception.response;
    final data = response?.data;

    if (data is Map<String, dynamic>) {
      return ServerException(
        statusCode: data['status'] as int? ?? response?.statusCode ?? 0,
        message: data['message'] as String? ?? '서버 오류가 발생했습니다',
      );
    }

    return ServerException(
      statusCode: response?.statusCode ?? 0,
      message: '서버 오류가 발생했습니다',
    );
  }
}
