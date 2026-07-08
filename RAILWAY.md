# Deploy FERWAFA on Railway (Spring Boot + MySQL)

## Why Railway
Vercel cannot run this Java/MySQL stack. Railway runs the Dockerized Spring Boot app and a MySQL plugin together.

## One-time setup (Dashboard — ~5 minutes)

1. Open https://railway.app and sign in with **GitHub** (`isherve`).
2. **New Project** → **Deploy from GitHub repo** → select `matchday-and-transfer-management-`.
3. Railway may detect `railway.toml` (Dockerfile at `backend/Dockerfile`).
4. In the same project: **Add Service** → **Database** → **MySQL**.
5. Open the **backend** service → **Variables** → **Add variable reference** / **Connect** MySQL so these exist (Railway often auto-injects them when linked):
   - `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`
6. Also set on the backend service:

```
JWT_SECRET=<long-random-string-at-least-32-chars>
JWT_EXPIRATION_MS=86400000
YELLOW_CARD_THRESHOLD=2
CORS_ORIGINS=https://*.up.railway.app
```

7. Backend service → **Settings** → **Networking** → **Generate Domain**.
8. Update `CORS_ORIGINS` to your exact public URL, e.g. `https://ferwafa-production.up.railway.app`.
9. Redeploy if needed. Wait for health check `/api/health`.

## CLI alternative

```bash
npm i -g @railway/cli
railway login
cd matchday-and-transfer-management-
railway init
railway add --database mysql
railway up
railway domain
```

## After deploy

| Page | URL |
|------|-----|
| Login | `https://<your-domain>/login` |
| Standings | `https://<your-domain>/public/standings` |
| Health | `https://<your-domain>/api/health` |
| Swagger | `https://<your-domain>/swagger-ui/index.html` |

Default logins (seed): `admin` / `password`, teams `apr`… / `password`, referee `jhabimana@ferwafa.rw` / `REF001`.

## Flutter app

```bash
flutter run --dart-define=API_URL=https://<your-domain>
```
