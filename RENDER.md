# Deploy FERWAFA free on Render

**Cost:** $0 on Render’s **Free** web plan (no credit card for basic deploy).  
Data uses in-memory H2 with seed data — fine for demos; it resets when the service redeploys or restarts.

## One-click deploy (easiest)

1. Open this link (logs in with GitHub if needed):

   **https://render.com/deploy?repo=https://github.com/isherve/matchday-and-transfer-management-**

2. Click **Apply** / **Create Web Service**.
3. Wait ~5–10 minutes for the Docker build (first deploy is slow).
4. Render shows your real URL, e.g.  
   `https://ferwafa-xxxx.onrender.com`

## Open your app

| Page | URL |
|------|-----|
| Login | `https://<your-url>/login` |
| Standings | `https://<your-url>/public/standings` |
| Health | `https://<your-url>/api/health` |

**Do not** use `your-app.up.railway.app` — that was only an example placeholder.

## Logins (seed data)

| Role | User | Password |
|------|------|----------|
| Admin | `admin` | `password` |
| Team | `apr` | `password` |
| Referee | `jhabimana@ferwafa.rw` | `REF001` |

## Free tier notes

- Service **sleeps** after ~15 min idle; first visit may take **30–60 seconds** to wake up.
- Database is **in-memory** — data is lost on restart (seed reloads automatically).
- For **persistent MySQL**, use a paid Render MySQL add-on or Railway MySQL later.

## Manual setup (dashboard)

1. https://dashboard.render.com → **New +** → **Blueprint**
2. Connect GitHub repo `matchday-and-transfer-management-`
3. Render reads `render.yaml` from the repo root
4. **Create** → copy the generated `.onrender.com` URL

## Manual redeploy (optional)

If you connected Render via Blueprint, pushes to `main` redeploy automatically.  
You do **not** need a GitHub Actions workflow for Render.

## Flutter app

```bash
flutter run --dart-define=API_URL=https://<your-url>.onrender.com
```
