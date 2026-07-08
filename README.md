# FERWAFA Match Day Reporting & Transfer Management System

Production-ready monorepo for the Rwanda Premier (National) League — digitizing match-day reporting and player transfers for FERWAFA.

## Architecture

```
├── backend/          # Java 17 + Spring Boot 3 REST API + Thymeleaf web UI
├── mobile/           # Flutter referee app (Android/iOS)
├── docker-compose.yml
└── README.md
```

## Hosting (free)

| Platform | Cost | Guide |
|----------|------|--------|
| **[Render](https://render.com)** (recommended free) | $0 web tier | [RENDER.md](./RENDER.md) — **one-click:** [Deploy on Render](https://render.com/deploy?repo=https://github.com/isherve/matchday-and-transfer-management-) |
| [Railway](https://railway.app) | Trial credit, then paid | [RAILWAY.md](./RAILWAY.md) |

Vercel cannot run this Java/MySQL stack.

## Prerequisites

- **Java 17+**
- **Maven 3.9+** (or use `backend/mvnw`)
- **Docker & Docker Compose** (for local MySQL)
- **Flutter 3.x** (for referee mobile app)

## Quick Start

### 1. Start MySQL

```bash
docker-compose up -d
```

MySQL runs on `localhost:3306` with database `ferwafa`.

### 2. Configure environment

```bash
cp .env.example .env
# Edit .env if needed (defaults work for local dev)
```

### 3. Run the backend

```bash
cd backend
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows
```

- **Web UI (login):** http://localhost:8080/login
- **Public standings:** http://localhost:8080/public/standings
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **API health:** http://localhost:8080/api/health

### 4. Run the Flutter referee app

```bash
cd mobile
flutter pub get
flutter run
# For physical device, use: flutter run --dart-define=API_URL=http://<your-ip>:8080
```

## Default Credentials

| Role | Username / Email | Password / Access Code |
|------|------------------|------------------------|
| FERWAFA Admin | `admin` | `password` |
| Team Manager (APR) | `apr` | `password` |
| Team Manager (Rayon) | `rayon` | `password` |
| Referee | `jhabimana@ferwafa.rw` | `REF001` |
| Referee | `pniyonzima@ferwafa.rw` | `REF002` |
| Referee | `emuvunyi@ferwafa.rw` | `REF003` |

All 8 teams use username from seed (`apr`, `rayon`, `kiyovu`, `police`, `mukura`, `etincelles`, `bugesera`, `askigali`) with password `password`.

## Seed Data Highlights

- **8 teams** with 18 members each (14 players + 4 staff)
- **3 referees** with access codes
- **3 weeks of fixtures** (Week 1 APPROVED with disciplinary records)
- **Demonstrable suspensions:** APR #10 (red card → suspended Week 2), Rayon #5+#7 (2 yellows → suspended Week 2)
- **Transfers** in all states: REQUESTED, APPROVED, REJECTED, COMPLETED

## Suspension Engine

When an admin approves a match report:

1. **Direct red card** → player suspended for the team's next fixture
2. **2 accumulated yellow cards** (configurable via `YELLOW_CARD_THRESHOLD`) → suspended for next fixture
3. Suspended players are **blocked from lineups** with a clear validation error
4. All suspensions are stored in `disciplinary_record` (auditable)
5. Official **home/away scores** are written to `fixture` from approved goal rows

Verify: Log in as `rayon` → **Lineup** → Week 2 fixture → players #5 and #7 appear in red.

## League Operations (Phases 12–17)

| Feature | Details |
|---------|---------|
| **Match results** | `fixture.home_score` / `away_score` set on report submit/approve |
| **Standings** | `GET /api/reports/standings` — W/D/L, GF/GA/GD, points, club points deductions, H2H tiebreaker |
| **Public table** | http://localhost:8080/public/standings (read-only, no login) |
| **Top scorers / cards** | `GET /api/reports/top-scorers`, `GET /api/reports/cards-leaderboard` |
| **Transfer windows** | `POST /api/transfers` blocked outside open windows; seed includes July 2026 special window for demos |
| **Commissioner reports** | `POST /api/commissioner-reports` — pitch, crowd, security, technical notes |
| **Club sanctions** | `POST /api/club-sanctions` — points deduction / fine / stadium ban (feeds standings) |
| **Notifications** | In-app `GET /api/notifications` for assignment, reports, transfers, suspensions, postponements |
| **Postpone fixture** | `PUT /api/fixtures/{id}/postpone` with new date + reason |
| **Referee conflict** | Same-day double-assignment blocked |
| **Squad / lineup caps** | Configurable via `league_rule` (`MAX_SQUAD_SIZE=30`, `MAX_LINEUP_SIZE=11`) |

## API Endpoints

All endpoints documented at `/swagger-ui`. Key routes:

- `POST /api/auth/login` — Admin/team login
- `POST /api/auth/referee-login` — Referee login
- `GET /api/teams/{teamId}/suspensions?fixtureId=` — Suspended players
- `POST /api/fixtures/{id}/lineup` — Save lineup (validates suspensions)
- `PUT /api/reports/{id}/approve` — Approve report (triggers suspension engine + stores scores)
- `GET /api/reports/standings` — League table
- `GET /api/reports/top-scorers` — Goals leaderboard
- `GET /api/reports/cards-leaderboard` — Cards leaderboard
- `GET /api/transfer-windows` / `/open` — Transfer window calendar
- `POST /api/commissioner-reports` / `POST /api/club-sanctions`
- `GET /api/notifications` — In-app notification center
- `PUT /api/fixtures/{id}/postpone` — Reschedule with audit reason

## Roadmap (v2 — not in this build)

Documented for a later phase; intentionally deferred:

- Email/SMS/push delivery (beyond in-app notifications)
- Full public CMS (news, media packs) — standings/results already public
- Contract expiry alerts, document uploads (ID/medical/contracts)
- Append-only audit log UI, rate limiting, admin 2FA
- Injury/medical tracking (separate from cards)
- Multi-season history, promotion/relegation, youth/reserve leagues
- Trilingual UI (Kinyarwanda / French / English)
- Offline referee sync, attendance/ticketing
- FIFA Connect/TMS export hook
- Referee performance ratings & conflict-of-interest calendar
- Foreign-player quotas / DOB-based minimum-age enforcement at registration

## Running Tests

```bash
cd backend
./mvnw test
```

Tests cover: suspension engine, lineup blocking, transfer windows, standings (+ club points deduction).

## Database Backup & Restore

**Backup:**
```bash
docker exec ferwafa-mysql mysqldump -u ferwafa -pferwafa123 ferwafa > backup_$(date +%Y%m%d).sql
```

**Restore:**
```bash
docker exec -i ferwafa-mysql mysql -u ferwafa -pferwafa123 ferwafa < backup_20250901.sql
```

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend API | Java 17, Spring Boot 3.2, Spring Security, JPA |
| Database | MySQL 8, Flyway migrations |
| Web UI | Thymeleaf, Bootstrap 5, JavaScript |
| Mobile | Flutter (Dart) |
| Auth | JWT, BCrypt, RBAC |
| PDF | OpenPDF |
| API Docs | SpringDoc OpenAPI |

## Project Structure

```
backend/src/main/java/com/ferwafa/
  auth/          # Login, JWT
  team/          # Team CRUD
  member/        # Player/staff registration
  referee/       # Referee management
  fixture/       # Fixtures, lineups, postpone
  report/        # Match reports, PDF exports
  standings/     # League table + leaderboards
  transfer/      # Transfer workflow + windows
  discipline/    # Player suspension engine
  sanction/      # Commissioner reports + club sanctions
  notification/  # In-app notifications
  config/        # Security, CORS, OpenAPI, league rules
  web/           # Thymeleaf + public standings
```

## License

Built for FERWAFA — Rwanda Football Federation.
