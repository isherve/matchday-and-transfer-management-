class NotificationModel {
  final int id;
  final String title;
  final String message;
  final String type;
  final String? relatedEntityType;
  final int? relatedEntityId;
  final bool read;
  final DateTime createdAt;

  NotificationModel({
    required this.id,
    required this.title,
    required this.message,
    required this.type,
    this.relatedEntityType,
    this.relatedEntityId,
    required this.read,
    required this.createdAt,
  });

  factory NotificationModel.fromJson(Map<String, dynamic> json) => NotificationModel(
        id: json['id'],
        title: json['title'] ?? '',
        message: json['message'] ?? '',
        type: json['type']?.toString() ?? '',
        relatedEntityType: json['relatedEntityType']?.toString(),
        relatedEntityId: json['relatedEntityId'],
        read: json['read'] == true,
        createdAt: DateTime.tryParse(json['createdAt']?.toString() ?? '') ?? DateTime.now(),
      );
}

class LineupPlayer {
  final int memberId;
  final String playerName;
  final int? playerNumber;
  final String? position;
  final bool suspended;
  final String? suspensionReason;

  LineupPlayer({
    required this.memberId,
    required this.playerName,
    this.playerNumber,
    this.position,
    this.suspended = false,
    this.suspensionReason,
  });

  factory LineupPlayer.fromJson(Map<String, dynamic> json) => LineupPlayer(
        memberId: json['memberId'],
        playerName: json['playerName'] ?? '',
        playerNumber: json['playerNumber'],
        position: json['position']?.toString(),
        suspended: json['suspended'] == true,
        suspensionReason: json['suspensionReason']?.toString(),
      );
}

class SuspensionInfo {
  final int memberId;
  final String playerName;
  final int? playerNumber;
  final String reasonLabel;

  SuspensionInfo({
    required this.memberId,
    required this.playerName,
    this.playerNumber,
    required this.reasonLabel,
  });

  factory SuspensionInfo.fromJson(Map<String, dynamic> json) => SuspensionInfo(
        memberId: json['memberId'],
        playerName: json['playerName'] ?? '',
        playerNumber: json['playerNumber'],
        reasonLabel: json['reasonLabel']?.toString() ?? json['reason']?.toString() ?? 'Suspended',
      );
}
