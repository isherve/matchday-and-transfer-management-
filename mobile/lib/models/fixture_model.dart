class FixtureModel {
  final int id;
  final int homeTeamId;
  final int awayTeamId;
  final String homeTeamName;
  final String awayTeamName;
  final int week;
  final String matchDate;
  final String matchTime;
  final String stadium;
  final String status;
  final int? homeScore;
  final int? awayScore;

  FixtureModel({
    required this.id,
    required this.homeTeamId,
    required this.awayTeamId,
    required this.homeTeamName,
    required this.awayTeamName,
    required this.week,
    required this.matchDate,
    required this.matchTime,
    required this.stadium,
    required this.status,
    this.homeScore,
    this.awayScore,
  });

  factory FixtureModel.fromJson(Map<String, dynamic> json) => FixtureModel(
        id: json['id'],
        homeTeamId: json['homeTeamId'],
        awayTeamId: json['awayTeamId'],
        homeTeamName: json['homeTeamName'] ?? '',
        awayTeamName: json['awayTeamName'] ?? '',
        week: json['week'],
        matchDate: json['matchDate']?.toString() ?? '',
        matchTime: json['matchTime']?.toString() ?? '',
        stadium: json['stadium'] ?? '',
        status: json['status'] ?? '',
        homeScore: json['homeScore'],
        awayScore: json['awayScore'],
      );

  String get title => '$homeTeamName vs $awayTeamName';

  bool get canSubmitReport =>
      status == 'REFEREE_ASSIGNED' || status == 'PLAYED' || status == 'REPORTED';
}
