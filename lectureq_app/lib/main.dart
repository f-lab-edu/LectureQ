import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'app_theme.dart';

void main() {
  runApp(const ProviderScope(child: MyApp()));
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'LectureQ',
      theme: AppTheme.lightTheme,
      home: const Scaffold(
        body: Center(
          child: Text('LectureQ'),
        ),
      ),
    );
  }
}
