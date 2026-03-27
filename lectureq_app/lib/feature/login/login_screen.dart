import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'login_provider.dart';
import 'login_state.dart';

class LoginScreen extends ConsumerWidget {
  const LoginScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final loginState = ref.watch(loginProvider);

    ref.listen(loginProvider, (previous, next) {
      if (next.status == LoginStatus.error && next.errorMessage != null) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(next.errorMessage!),
            behavior: SnackBarBehavior.floating,
          ),
        );
        ref.read(loginProvider.notifier).clearError();
      }
    });

    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32),
          child: Column(
            children: [
              const Spacer(flex: 3),
              _buildLogo(),
              const SizedBox(height: 12),
              _buildSlogan(),
              const SizedBox(height: 32),
              _buildFeatureBadges(),
              const Spacer(flex: 4),
              _buildKakaoLoginButton(context, ref, loginState),
              const SizedBox(height: 48),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildLogo() {
    return const Text(
      'LectureQ',
      style: TextStyle(
        fontSize: 36,
        fontWeight: FontWeight.bold,
        color: Color(0xFF1565C0),
      ),
    );
  }

  Widget _buildSlogan() {
    return const Text(
      '강의를 더 스마트하게',
      style: TextStyle(
        fontSize: 16,
        color: Colors.grey,
      ),
    );
  }

  Widget _buildFeatureBadges() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        _badge('🎙', '녹음'),
        const SizedBox(width: 12),
        _badge('📝', '요약'),
        const SizedBox(width: 12),
        _badge('❓', '질문'),
      ],
    );
  }

  Widget _badge(String emoji, String label) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      decoration: BoxDecoration(
        color: const Color(0xFFE3F2FD),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(emoji, style: const TextStyle(fontSize: 16)),
          const SizedBox(width: 6),
          Text(
            label,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Color(0xFF1565C0),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildKakaoLoginButton(
    BuildContext context,
    WidgetRef ref,
    LoginState loginState,
  ) {
    final isLoading = loginState.status == LoginStatus.loading;

    return SizedBox(
      width: double.infinity,
      height: 52,
      child: ElevatedButton(
        onPressed: isLoading
            ? null
            : () => ref.read(loginProvider.notifier).login(),
        style: ElevatedButton.styleFrom(
          backgroundColor: const Color(0xFFFEE500),
          foregroundColor: Colors.black87,
          disabledBackgroundColor: const Color(0xFFFEE500).withValues(alpha: 0.6),
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
        child: isLoading
            ? const SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(
                  strokeWidth: 2.5,
                  color: Colors.black54,
                ),
              )
            : const Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    '💬',
                    style: TextStyle(fontSize: 20),
                  ),
                  SizedBox(width: 8),
                  Text(
                    '카카오 로그인',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}
