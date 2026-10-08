# BLACKBOX Crypto v5.4 — Windows VPS Master Install

## 1. Prasyarat
- Windows Server 2019/2022+
- Node.js 20+
- PostgreSQL 14+
- Git opsional
- Domain + HTTPS sangat disarankan untuk produksi

## 2. Install dependency
Buka PowerShell di folder project:

```powershell
npm install
```

## 3. Buat environment
```powershell
Copy-Item .env.example .env
node scripts/setup.mjs
```

Isi `.env` terutama:
- `DATABASE_URL`
- `AUTH_SECRET`
- `DATA_ENCRYPTION_KEY`
- bootstrap password default
- `COOKIE_SECURE=true` jika HTTPS
- `SMTP_*` untuk email OTP
- `TWILIO_*` untuk SMS OTP
- `MARKET_EXCHANGE=binance`
- `MARKET_DEFAULT_TYPE=spot`
- `ROUTER_MODE=paper`
- `REAL_TRADING_ENABLED=false`

## 4. Database
```powershell
npx prisma generate
npx prisma migrate deploy
npm run seed
```

Untuk instalasi database kosong yang belum memakai migration history, Gunakan `npx prisma migrate deploy`. Migration baseline v6.1 bersifat idempoten untuk mengadopsi database yang sebelumnya dibuat dengan `db push`; jangan gunakan `db push --accept-data-loss` pada production.

## 5. Build
```powershell
npm run typecheck
npm run build
```

## 6. Jalankan web + worker
Terminal 1:
```powershell
npm start
```

Terminal 2:
```powershell
npm run worker
```

Untuk production gunakan PM2/Windows service dan restart policy.

## 7. Login pertama
Seed membuat:
- Developer: `developer@blackbox.com`
- Password: nilai bootstrap password default

Setelah login Developer diarahkan langsung ke `/developer`.

## 8. Membuat Admin
Developer membuka:
`/developer` → **User & Role Management** → `MAKE ADMIN`.

Admin tidak dapat membuat Admin lain atau Developer. Developer wajib menugaskan USER ke Admin melalui **Admin → User Assignment**. Server tetap memeriksa assignment pada setiap endpoint Admin, jadi aturan tidak bergantung pada UI.

## 9. Access Code
Developer/Admin → **Access Code Generator**:
- pilih tier
- expiry code
- maksimum jumlah pengguna/redemption
- generate

Member → **Access** → masukkan code.

Code hanya disimpan sebagai hash di database. Plaintext code ditampilkan sekali setelah generate.

## 10. Tier market
Default master:
- Silver: 10 pair, SPOT
- Gold: 50 pair, SPOT + FUTURES
- Platinum: 100 pair, SPOT + FUTURES + DEX
- Developer/Admin: akses operasional penuh

Server akan menolak bot yang melebihi jumlah pair atau market type tier.

## 11. Candle analysis
Untuk SPOT/FUTURES, BLACKBOX memakai CCXT public market data. Paper worker dapat menggunakan konfigurasi bot:
- `SPOT`
- `FUTURES`
- `DEX`

Paper Market Lab tersedia di `/dashboard/paper`.

Sinyal candle yang digunakan untuk alasan entry antara lain:
- `DOJI`
- `BULLISH_BREAKOUT`
- `BEARISH_BREAKOUT`
- bullish/bearish candle tanpa breakout

Sinyal teknikal adalah indikator heuristik, bukan jaminan keuntungan.

## 12. Email / SMS verification
Production memerlukan:
- SMTP server untuk email OTP
- Twilio atau adapter SMS yang sesuai untuk phone OTP

Akun member tetap `PENDING` sampai email dan HP diverifikasi.

## 13. KYC
Member membuka `/dashboard/kyc` dan mengirim data KYC. Admin/Developer memproses queue KYC di Admin Console.

Untuk produksi, `documentRef` harus menunjuk ke storage KYC privat/terenkripsi atau provider KYC yang sesuai. Jangan simpan dokumen identitas pada URL publik.

## 14. Legal consent / audit
Registration, setup, bot start, access-code redemption, dan KYC submission menghasilkan audit consent dengan:
- versi dokumen legal
- waktu
- user-agent
- IP yang di-hash
- metadata aksi

Ini memperkuat audit trail, tetapi **bukan jaminan bahwa pengguna tidak dapat menggugat atau bahwa tanggung jawab hukum otomatis gugur**. Dokumen wajib ditinjau penasihat hukum dan disesuaikan dengan struktur usaha/lisensi yang berlaku.

## 15. REAL trading
Master ini tetap **PAPER-FIRST**. Jangan mengaktifkan REAL trading hanya karena UI terlihat siap. Integrasi exchange/wallet dan legal/licensing production harus divalidasi terpisah.
