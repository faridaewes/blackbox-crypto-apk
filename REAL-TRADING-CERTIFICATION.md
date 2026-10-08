# BLACKBOX Crypto — Real Trading Certification Gate

## Default state
`REAL_TRADING_ENABLED=false` and `REAL_TRADING_PRODUCTION_READY=false` must remain false until an exchange has passed the checklist below.

## Required exchange certification
For each exchange and each market type (SPOT/FUTURES):

1. API authentication succeeds.
2. Withdrawal permission is OFF.
3. IP whitelist is enabled where the exchange supports it.
4. `fetchMarkets` returns the exact symbols used by BLACKBOX.
5. Amount precision, price precision, minimum amount and minimum notional are verified.
6. Balance is fetched and compared with the local precheck.
7. Small-notional market BUY and SELL are tested.
8. Limit BUY/SELL are tested where supported.
9. Open order status is observed.
10. Partial fill is tested or simulated with a supported exchange/testnet.
11. Cancel is tested.
12. Reject/error paths are tested.
13. A network timeout immediately after submission is tested. BLACKBOX must reconcile, never blindly resubmit.
14. Restart recovery is tested while an order is open.
15. Balance and order reconciliation agree with the exchange.
16. Fees and average fill price are persisted.
17. Kill switch/global halt blocks new orders.
18. API revoke blocks new orders.
19. KYC + email + phone verification are required.
20. The exact adapter/exchange is added to `REAL_CERTIFIED_EXCHANGES` only after the above tests pass.

## Enabling real trading
Only after certification:

```env
REAL_TRADING_ENABLED=true
REAL_TRADING_PRODUCTION_READY=true
REAL_CERTIFIED_EXCHANGES=your_exchange_id
```

Do not enable these flags merely because `npm run build` succeeds.

## Database / deployment
The container uses `prisma migrate deploy` only. The v6.1 baseline migration is idempotent for adoption of databases previously initialized with `db push`. Never use `db push --accept-data-loss` in production.

## Safety architecture
`SIGNAL -> RISK -> EXECUTION PRECHECK -> IDEMPOTENCY LOCK -> SUBMIT -> ACK/PARTIAL/FILLED -> RECONCILIATION`.

The exchange remains the source of truth for remote order/fill/balance state.
