class ReportEntry {
  final int? reportId;
  final int teamMemberId;
  final String playerName;
  final String teamName;
  final int goal;
  final int? goalMin;
  final String card;
  final int? cardMin;
  final String status;

  ReportEntry({
    this.reportId,
    required this.teamMemberId,
    required this.playerName,
    required this.teamName,
    required this.goal,
    this.goalMin,
    required this.card,
    this.cardMin,
    required this.status,
  });

  factory ReportEntry.fromJson(Map<String, dynamic> json) => ReportEntry(
        reportId: json['reportId'],
        teamMemberId: json['teamMemberId'],
        playerName: json['playerName'] ?? '',
        teamName: json['teamName'] ?? '',
        goal: json['goal'] ?? 0,
        goalMin: json['goalMin'],
        card: json['card']?.toString() ?? 'NONE',
        cardMin: json['cardMin'],
        status: json['status']?.toString() ?? '',
      );
}

class ReportComment {
  final int id;
  final String body;
  final String authorRole;
  final String authorName;
  final DateTime createdAt;

  ReportComment({
    required this.id,
    required this.body,
    required this.authorRole,
    required this.authorName,
    required this.createdAt,
  });

  factory ReportComment.fromJson(Map<String, dynamic> json) => ReportComment(
        id: json['id'],
        body: json['body'] ?? '',
        authorRole: json['authorRole'] ?? '',
        authorName: json['authorName'] ?? '',
        createdAt: DateTime.tryParse(json['createdAt']?.toString() ?? '') ?? DateTime.now(),
      );
}

class ReportEditLog {
  final int id;
  final String editorRole;
  final String editorName;
  final String action;
  final String summary;
  final DateTime createdAt;

  ReportEditLog({
    required this.id,
    required this.editorRole,
    required this.editorName,
    required this.action,
    required this.summary,
    required this.createdAt,
  });

  factory ReportEditLog.fromJson(Map<String, dynamic> json) => ReportEditLog(
        id: json['id'],
        editorRole: json['editorRole'] ?? '',
        editorName: json['editorName'] ?? '',
        action: json['action'] ?? '',
        summary: json['summary'] ?? '',
        createdAt: DateTime.tryParse(json['createdAt']?.toString() ?? '') ?? DateTime.now(),
      );
}
