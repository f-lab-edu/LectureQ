import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'network_checker.g.dart';

@riverpod
Connectivity connectivity(Ref ref) {
  return Connectivity();
}

@riverpod
Stream<List<ConnectivityResult>> connectivityStatus(Ref ref) {
  final connectivity = ref.watch(connectivityProvider);
  return connectivity.onConnectivityChanged;
}

@riverpod
Future<bool> isConnected(Ref ref) async {
  final connectivity = ref.watch(connectivityProvider);
  final results = await connectivity.checkConnectivity();
  return results.any((r) => r != ConnectivityResult.none);
}
