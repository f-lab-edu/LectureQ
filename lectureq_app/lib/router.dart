import 'package:go_router/go_router.dart';
import 'package:flutter/material.dart';

import 'feature/login/login_screen.dart';

final GoRouter router = GoRouter(
  initialLocation: '/login',
  routes: [
    GoRoute(
      path: '/login',
      builder: (context, state) => const LoginScreen(),
    ),
    GoRoute(
      path: '/',
      builder: (context, state) => const Scaffold(
        body: Center(
          child: Text('LectureQ'),
        ),
      ),
    ),
  ],
);
