# BLACKBOX Crypto v6.3.0 — Panduan Instalasi

Produk ini adalah **platform AI paper-trading** (simulasi, tanpa dana nyata). Eksekusi REAL terkunci dan tidak ada pada versi ini.

## Cara tercepat — Docker (disarankan)

Prasyarat: Docker + Docker Compose, dan Node.js ≥ 20 (hanya untuk menjalankan `npm run setup` sekali; atau isi `.env` manual).

```bash
# 1. Buat .env dengan rahasia acak yang kuat (password Developer ditampilkan SEKALI — catat!)
node scripts/setup.mjs --ai=openrouter --key=SK-OR-ANDA --http
#    --http  : bila Anda mengakses lewat http://IP-SERVER (tanpa HTTPS). Hapus opsi ini bila sudah memakai HTTPS.
#    tanpa --ai/--key: AI memakai mesin aturan lokal (tetap berjalan, tanpa kuota)

# 2. Bangun dan jalankan (database + web + worker paper-trading)
docker compose up -d --build

# 3. Buka http://localhost:3000
# Bootstrap login (first install):
# Developer: developer@blackbox.com / @blackbox123
# Admin:     admin@blackbox.com / @adminblackbox123
# Keduanya wajib ganti password pada login pertama.
docker compose logs -f worker    # lihat aktivitas bot
```

Data tersimpan di volume Docker `blackbox_pg`. Backup: `docker compose exec db pg_dump -U blackbox blackbox > backup.sql`.

## Tanpa Docker (Windows/Linux VPS)

1. Pasang Node.js 20+ dan PostgreSQL 14+; buat database `blackbox` dan user `blackbox`.
2. `npm install`
3. `node scripts/setup.mjs --http` → lalu **sesuaikan `DATABASE_URL`** di `.env` bila password/host database Anda berbeda.
4. `npx prisma migrate deploy` lalu `npm run seed` (membuat Developer/Admin bootstrap, paket Silver/Gold/Platinum/Enterprise, dokumen legal).
5. `npm run build`
6. Jalankan dua proses: `npm start` (web) dan `npm run worker` (mesin paper-trading). Dengan PM2: `pm2 start ecosystem.config.cjs`.

Detail Windows VPS + HTTPS: `docs/DEPLOY-WINDOWS-VPS.md`.

## AI gratis

Tanpa API key sekalipun bot berjalan (mesin aturan lokal). Untuk AI sungguhan, pilih salah satu:

| Opsi | Setup | Batas (diverifikasi Okt 2026) |
|---|---|---|
| **OpenRouter** | daftar di openrouter.ai → buat key → `--ai=openrouter --key=...` (model `openrouter/free` atau berakhiran `:free`) | 20 req/menit; **50 req/hari** tanpa pembelian kredit, 1.000/hari setelah pernah membeli kredit ≥ $10. Model gratis bisa berganti/penuh sewaktu-waktu. |
| **Groq** | daftar di console.groq.com → buat key → `--ai=groq --key=...` (OpenAI-compatible, model `openai/gpt-oss-120b`) | free tier per model (mis. 30 req/menit, 1.000 req/hari untuk model tersebut). |
| **Ollama (lokal)** | pasang Ollama, `ollama pull qwen3:4b`, lalu `--ai=ollama` | tanpa kuota; butuh RAM ≥ 4–8 GB. Pada Docker, `OLLAMA_BASE_URL` otomatis mengarah ke host. |

Syarat free tier dapat berubah; cek halaman resmi provider. Aplikasi sudah membatasi diri agar kuota tidak habis: `AI_DAILY_BUDGET=40`, `AI_PER_MINUTE=12`, `AI_MAX_PER_CYCLE=2`, cache keputusan 15 menit. Bila kuota habis atau provider error, keputusan otomatis dialihkan ke mesin aturan lokal. Endpoint OpenAI-compatible lain dapat dipakai dengan `AI_PROVIDER=openai-compatible`, `AI_BASE_URL`, `AI_API_KEY`, `AI_MODEL`.

Data pasar memakai **DexScreener** (gratis, tanpa key). Bila tidak bisa dijangkau, screener dan bot otomatis memakai feed simulasi (ditandai jelas "SIMULASI" di UI).

## Mengelola pengguna dan paket

Login sebagai Developer → **/admin** → panel *Pengguna & Paket*: aktifkan/suspend user, berikan paket (Silver/Gold/Platinum/Enterprise) dengan masa berlaku, jadikan Admin (Developer saja). User tanpa paket berada pada tier DEMO (1 bot, 3 posisi). Registrasi mandiri ada di `/register`.

### Membuat akun Admin dari terminal
`npm run create-admin` (Docker: `docker compose exec app npx tsx scripts/create-admin.ts`) — meminta nama, email, HP, dan password ≥ 12 karakter; membuat atau menaikkan akun menjadi ADMIN. Developer juga bisa menaikkan/menurunkan peran dari `/developer` (daftar pengguna) atau `/admin`.

## Pengujian

```bash
npm test          # 34 tes: engine, AI, keamanan, otorisasi admin, simulasi 1.500 tick (tanpa jaringan/DB; butuh Node ≥ 22.6)
npm run typecheck # setelah npm install
```

## Hal yang BELUM ada (jujur)

- Eksekusi REAL (CEX/DEX) — terkunci; hanya kerangka koneksi.
- Verifikasi email/HP, access code, KYC submission + admin review, candle analysis CCXT, motion UI, dan trade history detail sudah dibangun. Produksi tetap memerlukan SMTP/Twilio serta review legal/KYC provider sesuai yurisdiksi.
- Hasil `npm run build` penuh dan UI di browser perlu Anda verifikasi di server Anda (lihat `RELEASE-AUDIT.md`).
- Dokumen legal adalah template awal; wajib ditinjau konsultan hukum sebelum dipakai publik.

## v6.0 database migration
After replacing the source with v6.0, run:

```powershell
npm install
npx prisma generate
npx prisma migrate deploy
npm run typecheck
npm run build
```

For live execution, keep `REAL_TRADING_PRODUCTION_READY=false` until exchange-specific certification is complete. Run the reconciliation worker alongside the application:

```powershell
npm run worker
npm run reconcile
```

Optional daily Telegram report:

```powershell
npm run daily-report
```
