import 'package:riverpod_annotation/riverpod_annotation.dart';

import 'login_state.dart';

part 'login_provider.g.dart';

@riverpod
class LoginNotifier extends _$LoginNotifier {
  @override
  LoginState build() {
    return const LoginState();
  }

  /// 카카오 로그인 실행
  /// #16에서 카카오 SDK + BE 통신 로직으로 교체 예정
  Future<void> login() async {
    state = state.copyWith(status: LoginStatus.loading);

    // TODO: #16에서 구현
    // 1. 카카오 SDK로 로그인 → 카카오 Access Token 획득
    // 2. BE /auth/login으로 카카오 토큰 전달 → JWT 수신
    // 3. JWT를 Secure Storage에 저장
    // 4. 홈 화면으로 이동

    await Future.delayed(const Duration(seconds: 1));
    state = state.copyWith(status: LoginStatus.idle);
  }

  void clearError() {
    state = state.copyWith(status: LoginStatus.idle, errorMessage: null);
  }
}
