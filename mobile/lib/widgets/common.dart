import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

class StatusChip extends StatelessWidget {
  final String status;
  const StatusChip({super.key, required this.status});

  Color get _bg {
    switch (status) {
      case 'APPROVED':
        return const Color(0xFFD1FAE5);
      case 'REPORTED':
        return const Color(0xFFFEF3C7);
      case 'REFEREE_ASSIGNED':
      case 'PLAYED':
        return AppTheme.brandSoft;
      case 'POSTPONED':
        return const Color(0xFFFEE2E2);
      default:
        return const Color(0xFFE5E7EB);
    }
  }

  Color get _fg {
    switch (status) {
      case 'APPROVED':
        return AppTheme.accent;
      case 'REPORTED':
        return AppTheme.warn;
      case 'POSTPONED':
        return AppTheme.danger;
      default:
        return AppTheme.brand;
    }
  }

  String get _label => status.replaceAll('_', ' ');

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(color: _bg, borderRadius: BorderRadius.circular(999)),
      child: Text(_label, style: TextStyle(color: _fg, fontSize: 11, fontWeight: FontWeight.w700)),
    );
  }
}

class ScoreBoard extends StatelessWidget {
  final String home;
  final String away;
  final int? homeScore;
  final int? awayScore;
  final String subtitle;

  const ScoreBoard({
    super.key,
    required this.home,
    required this.away,
    this.homeScore,
    this.awayScore,
    this.subtitle = '',
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [AppTheme.brandDark, AppTheme.brand],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        children: [
          if (subtitle.isNotEmpty)
            Text(subtitle, style: const TextStyle(color: Colors.white70, fontSize: 12)),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(child: Text(home, textAlign: TextAlign.center,
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700))),
              Text(
                homeScore != null && awayScore != null ? '$homeScore – $awayScore' : 'vs',
                style: const TextStyle(color: Colors.white, fontSize: 28, fontWeight: FontWeight.w800),
              ),
              Expanded(child: Text(away, textAlign: TextAlign.center,
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700))),
            ],
          ),
        ],
      ),
    );
  }
}
