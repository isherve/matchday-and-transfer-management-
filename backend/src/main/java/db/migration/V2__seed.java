package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class V2__seed extends BaseJavaMigration {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        if (isAlreadySeeded(conn)) {
            return;
        }
        String pwd = encoder.encode("password");
        String refCode1 = encoder.encode("REF001");

        exec(conn, """
            INSERT INTO users (fname, lname, username, password_hash, role, created_at, updated_at)
            VALUES ('FERWAFA', 'Administrator', 'admin', ?, 'ADMIN', NOW(), NOW())
            """, pwd);

        String[] teams = {
            "APR FC", "Rayon Sports", "Kiyovu Sports", "Police FC",
            "Mukura Victory", "Etincelles FC", "Bugesera FC", "AS Kigali"
        };
        String[] usernames = {"apr", "rayon", "kiyovu", "police", "mukura", "etincelles", "bugesera", "askigali"};
        String[] stadiums = {"Kigali Stadium", "Amahoro Stadium", "Kigali Stadium", "Police Stadium",
            "Huye Stadium", "Rubavu Stadium", "Bugesera Stadium", "Nyamirambo Stadium"};
        String[] logos = {
            "/img/teams/apr.png", "/img/teams/rayon.png", "/img/teams/kiyovu.png", "/img/teams/police.png",
            "/img/teams/mukura.png", "/img/teams/etincelles.png", "/img/teams/bugesera.png", "/img/teams/askigali.png"
        };

        for (int i = 0; i < teams.length; i++) {
            exec(conn, """
                INSERT INTO team (name, logo, stadium, username, password_hash, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, NOW(), NOW())
                """, teams[i], logos[i], stadiums[i], usernames[i], pwd);
        }

        exec(conn, """
            INSERT INTO referee (fname, lname, image, email, access_code_hash, created_at, updated_at)
            VALUES ('Jean', 'Habimana', NULL, 'jhabimana@ferwafa.rw', ?, NOW(), NOW())
            """, refCode1);
        exec(conn, """
            INSERT INTO referee (fname, lname, image, email, access_code_hash, created_at, updated_at)
            VALUES ('Patrick', 'Niyonzima', NULL, 'pniyonzima@ferwafa.rw', ?, NOW(), NOW())
            """, encoder.encode("REF002"));
        exec(conn, """
            INSERT INTO referee (fname, lname, image, email, access_code_hash, created_at, updated_at)
            VALUES ('Eric', 'Muvunyi', NULL, 'emuvunyi@ferwafa.rw', ?, NOW(), NOW())
            """, encoder.encode("REF003"));

        String[] positions = {"GK", "DF", "DF", "DF", "DF", "MF", "MF", "MF", "MF", "MF", "FW", "FW", "FW", "FW"};
        String[] firstNames = {"Eric", "Patrick", "Jean", "Paul", "David", "Samuel", "Kevin", "Olivier",
            "Fabrice", "Innocent", "Didier", "Bruce", "Herve", "Yves", "Claude", "Emmanuel", "Moise", "Alex"};
        String[] lastNames = {"Niyonzima", "Habimana", "Mukiza", "Rwanda", "Uwimana", "Bizimana",
            "Nshimiyimana", "Iradukunda", "Mugisha", "Hakizimana", "Ndayisaba", "Manishimwe",
            "Twagirumukiza", "Niyibizi", "Sibomana", "Gasana", "Rukundo", "Niyonsaba"};

        for (int t = 1; t <= 8; t++) {
            for (int p = 0; p < 18; p++) {
                String role = p < 14 ? "PLAYER" : "STAFF";
                String pos = p < 14 ? positions[p % 14] : null;
                Integer number = p < 14 ? p + 1 : null;
                String post = p >= 14 ? switch (p) {
                    case 14 -> "Coach";
                    case 15 -> "Assistant Coach";
                    case 16 -> "Physio";
                    default -> "Manager";
                } : null;
                exec(conn, """
                    INSERT INTO team_member (fname, lname, number, role_in_team, post, position, contract, team_id, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, '2025/26', ?, NOW(), NOW())
                    """, firstNames[p], lastNames[(p + t) % lastNames.length],
                    number, role, post, pos, t);
            }
        }

        int[][] week1 = {{1, 2}, {3, 4}, {5, 6}, {7, 8}};
        int[][] week2 = {{2, 1}, {4, 3}, {6, 5}, {8, 7}};
        int[][] week3 = {{1, 3}, {2, 4}, {5, 7}, {6, 8}};

        insertFixtures(conn, week1, 1, "2025/26", "APPROVED", 1);
        insertFixtures(conn, week2, 2, "2025/26", "REPORTED", 2);
        insertFixtures(conn, week3, 3, "2025/26", "REFEREE_ASSIGNED", 3);

        long aprP10 = memberId(conn, 1, 10);
        long rayonP5 = memberId(conn, 2, 5);
        long rayonP7 = memberId(conn, 2, 7);
        long fixture1 = 1L;

        // Week 1 — approved (APR 2-1 Rayon)
        insertReport(conn, fixture1, aprP10, 0, null, "RED", 67, 1, "APPROVED", 1);
        insertReport(conn, fixture1, rayonP5, 0, null, "YELLOW", 23, 1, "APPROVED", 1);
        insertReport(conn, fixture1, rayonP7, 0, null, "YELLOW", 78, 1, "APPROVED", 1);
        insertReport(conn, fixture1, memberId(conn, 1, 11), 2, 34, "NONE", null, 1, "APPROVED", 1);
        insertReport(conn, fixture1, memberId(conn, 2, 11), 1, 56, "NONE", null, 1, "APPROVED", 1);

        // Week 2 — submitted, awaiting admin approval (fixtures 5–8)
        // Fixture 5: Rayon Sports vs APR FC
        insertReport(conn, 5L, memberId(conn, 2, 11), 1, 18, "NONE", null, 2, "SUBMITTED", 2);
        insertReport(conn, 5L, memberId(conn, 1, 9), 1, 61, "YELLOW", 61, 2, "SUBMITTED", 2);
        insertReport(conn, 5L, memberId(conn, 2, 4), 0, null, "YELLOW", 40, 2, "SUBMITTED", 2);
        // Fixture 6: Police FC vs Kiyovu Sports
        insertReport(conn, 6L, memberId(conn, 4, 10), 2, 12, "NONE", null, 2, "SUBMITTED", 2);
        insertReport(conn, 6L, memberId(conn, 3, 11), 1, 77, "NONE", null, 2, "SUBMITTED", 2);
        insertReport(conn, 6L, memberId(conn, 4, 6), 0, null, "RED", 88, 2, "SUBMITTED", 2);
        // Fixture 7: Etincelles FC vs Mukura Victory
        insertReport(conn, 7L, memberId(conn, 6, 11), 0, null, "YELLOW", 33, 2, "SUBMITTED", 2);
        insertReport(conn, 7L, memberId(conn, 5, 8), 0, null, "YELLOW", 55, 2, "SUBMITTED", 2);
        // Fixture 8: AS Kigali vs Bugesera FC
        insertReport(conn, 8L, memberId(conn, 8, 11), 3, 9, "NONE", null, 2, "SUBMITTED", 2);
        insertReport(conn, 8L, memberId(conn, 7, 10), 1, 50, "YELLOW", 50, 2, "SUBMITTED", 2);
        insertReport(conn, 8L, memberId(conn, 8, 5), 0, null, "YELLOW", 70, 2, "SUBMITTED", 2);

        insertDiscipline(conn, aprP10, fixture1, 1, "RED", 67, "RED_CARD", 5, false);
        insertDiscipline(conn, rayonP5, fixture1, 1, "YELLOW", 23, null, null, false);
        insertDiscipline(conn, rayonP7, fixture1, 1, "YELLOW", 78, "TWO_YELLOWS", 5, false);

        exec(conn, """
            INSERT INTO transfer (member_id, team_from_id, team_to_id, post, request_date, status, created_at, updated_at)
            VALUES (?, 3, 4, 'MF', '2025-09-01', 'REQUESTED', NOW(), NOW())
            """, memberId(conn, 3, 3));

        exec(conn, """
            INSERT INTO transfer (member_id, team_from_id, team_to_id, post, request_date, approval_date, status, created_at, updated_at)
            VALUES (?, 5, 1, 'FW', '2025-08-15', '2025-08-20', 'APPROVED', NOW(), NOW())
            """, memberId(conn, 5, 8));

        exec(conn, """
            INSERT INTO transfer (member_id, team_from_id, team_to_id, post, request_date, approval_date, completed_date, status, created_at, updated_at)
            VALUES (?, 7, 2, 'DF', '2025-07-01', '2025-07-05', '2025-07-10', 'COMPLETED', NOW(), NOW())
            """, memberId(conn, 7, 2));

        exec(conn, """
            INSERT INTO transfer (member_id, team_from_id, team_to_id, post, request_date, rejected_date, status, created_at, updated_at)
            VALUES (?, 4, 6, 'GK', '2025-08-01', '2025-08-05', 'REJECTED', NOW(), NOW())
            """, memberId(conn, 4, 6));
    }

    private boolean isAlreadySeeded(Connection conn) throws Exception {
        try (var ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = 'admin'");
             var rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void insertFixtures(Connection conn, int[][] pairs, int week, String season, String status, int refereeId) throws Exception {
        LocalDate base = LocalDate.of(2025, 9, 1).plusWeeks(week - 1L);
        for (int i = 0; i < pairs.length; i++) {
            exec(conn, """
                INSERT INTO fixture (home_team_id, away_team_id, week, stadium, match_date, match_time, season, status, referee_id, created_at, updated_at)
                VALUES (?, ?, ?, 'Kigali Stadium', ?, '15:00:00', ?, ?, ?, NOW(), NOW())
                """, pairs[i][0], pairs[i][1], week, base.plusDays(i * 2L).toString(), season, status, refereeId);
        }
    }

    private long memberId(Connection conn, int teamId, int number) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
            "SELECT member_id FROM team_member WHERE team_id = ? AND number = ? LIMIT 1")) {
            ps.setInt(1, teamId);
            ps.setInt(2, number);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
            throw new IllegalStateException("Member not found team=" + teamId + " num=" + number);
        }
    }

    private void insertReport(Connection conn, long fixtureId, long memberId, int goal, Integer goalMin,
                              String card, Integer cardMin, int week, String status, long refereeId) throws Exception {
        exec(conn, """
            INSERT INTO match_report (fixture_id, team_member_id, goal, goal_min, card, card_min, week, status, submitted_by_referee_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """, fixtureId, memberId, goal, goalMin, card, cardMin, week, status, refereeId);
    }

    private void insertDiscipline(Connection conn, long memberId, long fixtureId, int week, String cardType,
                                  Integer cardMin, String suspensionReason, Integer suspensionFixtureId,
                                  boolean served) throws Exception {
        exec(conn, """
            INSERT INTO disciplinary_record (team_member_id, fixture_id, week, card_type, card_min, suspension_reason, suspension_fixture_id, suspension_served, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """, memberId, fixtureId, week, cardType, cardMin, suspensionReason, suspensionFixtureId, served);
    }

    private void exec(Connection conn, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        }
    }
}
