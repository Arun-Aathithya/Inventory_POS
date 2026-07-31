# Inventory Management POS — Spring Boot + Web Frontend + Alerts

This extends your original console/Java app into a REST API (Spring Boot)
with a web frontend, plus automatic **email** and **SMS** alerts when a
product's stock drops to or below its `lowstock` threshold.

## What's included

```
pos_backend/          Spring Boot REST API (Java 17, Maven)
  src/main/java/com/inventory/pos/
    model/            Product, User (JPA entities — map to your existing tables)
    repository/       Spring Data JPA repositories
    service/          ProductService, LoginService, AlertService, AlertScheduler
    controller/        REST endpoints (/api/products, /api/auth, /api/alerts, /api/billing)
    dto/               Request/response shapes used by billing (cart, receipt)
  src/main/resources/application.properties   DB + mail + SMS config

pos_frontend/
  index.html           Single-page app (plain HTML/JS, no build step needed)
```

Your original `Inventory.sql` schema is reused as-is — no migration needed.

## 1. Database

Nothing to change — use the same `inventorydb` and `Inventory.sql` you already have.

## 2. Configure alerts (`application.properties`)

**Email (Gmail SMTP example):**
1. Turn on 2-Step Verification on the sending Gmail account.
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Set `spring.mail.username` and `spring.mail.password` (the 16-char app password, not your normal one).
4. Set `alerts.email.to` to the address(es) that should receive alerts.

**SMS (Twilio):**
1. Sign up free at https://www.twilio.com/try-twilio (free trial credit).
2. Copy your Account SID + Auth Token from the Twilio console into `twilio.account-sid` / `twilio.auth-token`.
3. Set `twilio.from-number` to the Twilio number you're given.
4. Set `alerts.sms.to` to the destination number(s), in E.164 format (e.g. `+91XXXXXXXXXX`).
5. **Trial account note:** you can only send SMS to numbers you've verified in the Twilio console — that's fine for a college demo, just verify your own phone.
6. Set `alerts.sms.enabled=true` once configured (it defaults to `false` so the app runs fine even without Twilio set up).

Both channels can be toggled independently via `alerts.email.enabled` / `alerts.sms.enabled`.

## 3. Run the backend

```bash
cd pos_backend
mvn spring-boot:run
```

Runs on `http://localhost:8080`.

## 4. Run the frontend

No build tooling needed — it's a static HTML file. Simplest option:

```bash
cd pos_frontend
python3 -m http.server 5500
```

Then open `http://localhost:5500` in your browser.

(If you'd rather use a real dev server or convert this to React later, the
same `/api/...` endpoints work identically — the frontend is just calling
plain REST/JSON.)

## 5. Default login

Same as your original seed data:
- username: `admin`
- password: `admin123`

## Billing / POS

Ported from your original console app's `BillingService`. Same guarantees as before:
- A checkout builds a bill, inserts each line into `sale_items`, and deducts stock —
  all as one DB transaction. If stock runs out mid-checkout, the whole sale rolls back
  (nothing is half-saved).
- Stock is deducted with the same atomic "only if enough is left" guard as the
  original raw SQL, so two people checking out the same low-stock item at once can't
  oversell it.
- A low-stock alert fires immediately if a sale drops any product to/below its threshold
  (same as the original console app's `checkLowStockAfterSale`).

Endpoints:
- `POST /api/billing/checkout` — body: `{ items: [{productId, quantity}], customerName, customerPhone }`, returns a receipt.
- `GET /api/billing/sales` — full sale history, newest first.
- `GET /api/billing/sales/{billId}` — a single receipt by bill ID.

The frontend's **New Bill** panel builds the cart, sends the checkout request, shows the
returned receipt, and refreshes the product list (so stock/low-stock highlighting update
immediately) and the **Recent Sales** table.

## Editing products

The **Products** table now has an **Edit** button alongside Delete. Clicking it loads that
product's full details (including Weight, MRP, Date of Manufacture, Date of Expiry — not
just the original 5 fields) back into the Add Product form, which switches to "Update
Product" mode and calls `PUT /api/products/{id}`. "Cancel Edit" resets the form back to
add-mode.

## How alerts trigger

- **Real-time**: every time a product is added or updated via the API, `ProductService` checks if it's now low-stock and fires an alert immediately (see `checkAndAlertIfLow`).
- **Scheduled**: `AlertScheduler` also re-checks *all* products on a timer (default every 6 hours, configurable via `alerts.schedule.rate-ms`) — this catches stock changes made outside the app, e.g. direct DB edits or billing/POS sales reducing stock.
- **Manual/demo**: the frontend's "Send Alert Now" button calls `POST /api/alerts/check-now` so you can trigger and demonstrate it on demand (handy for a viva).

## New: innovative enhancements

Everything below was added on top of the original API/frontend to match the
"Inventory Management POS - Innovative Enhancement Ideas" doc. No DB migration
needed — all of it reads from your existing `Product`, `sales`, and `sale_items`
tables.

### Admin Dashboard + Business Analytics
- `GET /api/dashboard/stats` — total products, stock value, today's bills/revenue,
  all-time revenue, distinct customers, low-stock count, expiring-soon count, and
  the 5 most recent sales.
- `GET /api/dashboard/sales-trend?days=7|30|90` — daily revenue/bill-count series
  for the trend chart (7 days = weekly view, 30 = monthly, etc.).
- `GET /api/dashboard/category-revenue` — revenue grouped by product category.
- `GET /api/dashboard/top-products?limit=5` — best sellers by units sold.
- Frontend: a new **Dashboard** tab with stat cards and three Chart.js charts
  (sales trend line, category revenue doughnut, top-products bar).

### AI Sales Prediction / Reorder Suggestions
- `GET /api/predictions/reorder` — for every product, estimates daily sales
  velocity from the last `app.prediction.lookback-days` (default 30) days of
  `sale_items`, projects it against `app.prediction.lead-time-days` (default 7)
  + `app.prediction.safety-buffer-days` (default 3), and flags products that
  will run out before a reorder could arrive, with a suggested reorder quantity.
  This is a transparent moving-average heuristic (not a black-box model) —
  easy to explain in a viva. See `PredictionService`.
- Frontend: **AI Reorder** tab, with a sidebar badge showing the critical count.

### Product Recommendations
- `GET /api/products/{id}/recommendations?limit=4` — "customers who bought this
  also bought..." using co-purchase frequency across past bills, falling back to
  same-category products when there's no sales history yet (e.g. a fresh demo DB).
- Frontend: shows as a "💡 Customers also bought" chip row under the cart on the
  **New Bill** screen, updating whenever you change the selected product.

### Expiry Prediction Dashboard
- `GET /api/products/expiring?days=30` — products expiring within N days,
  colour-coded `expired` / `critical` (≤7 days) / `warning` (≤30 days) / `ok`.
- Frontend: **Expiry Watch** tab with colour-coded rows, and a sidebar badge.

### PDF Invoice
- `GET /api/billing/sales/{billId}/invoice` — generates a printable PDF invoice
  (via OpenPDF) with the shop name, bill/customer details, line items, and total.
- Frontend: "⬇ Download PDF Invoice" button on the receipt after checkout, and a
  "PDF" button per row on the **Sales History** tab.

### Bill Distribution to Customers (Email + WhatsApp — free) / SMS (paid, optional)

**Email (free) — on by default.** After a successful checkout, if the customer
has an email on file (typed at billing, or already saved on their customer
record), `BillingEmailService` emails them a thank-you note with the PDF
invoice attached. It reuses the exact same Gmail SMTP account already set up
for low-stock alerts (`spring.mail.username` / `spring.mail.password`) — no
new sign-up, no per-email cost.
- Toggle: `alerts.email.billing.enabled=true` (default `true`).
- Resend for any past bill: `POST /api/billing/sales/{billId}/resend-email`,
  or the "✉️ Email" / "✉️ Resend Email" button in the frontend.

**WhatsApp (free) — always available, no setup.** Every checkout response
includes a `whatsappShareLink` — a `wa.me` "click to chat" link pre-filled
with the bill total and invoice link. It costs nothing and needs no API key
or account: the frontend shows a "💬 Send via WhatsApp (Free)" button on the
receipt that opens WhatsApp (web or app) with the message ready to send to
the customer's number.
- `app.backend.base-url` controls the base of the invoice PDF link inside the message.

**SMS / WhatsApp Business API (paid, optional, off by default).**
`BillingNotificationService` can also send a fully-automated text via Twilio,
reusing the same Twilio account as the low-stock alerts. This is **not**
free once trial credit runs out — production Twilio use is billed per
message — so it stays off unless you deliberately want to pay for it:
  - `alerts.sms.billing.enabled=true` for SMS.
  - `alerts.whatsapp.enabled=true` for the Twilio WhatsApp Business API (needs
    a WhatsApp-enabled Twilio sender in `twilio.whatsapp-from-number` — the
    Twilio Sandbox number `+14155238886` works for a demo).

### UI enhancements
The frontend (`pos_frontend/index.html`, still plain HTML/JS, no build step) was
restructured into a sidebar-navigated single-page app: **Dashboard, Products,
New Bill, Sales History, Expiry Watch, AI Reorder**. Also added:
- Dark mode toggle (persisted via `localStorage`), glassmorphism login screen,
  toast notifications (replacing `alert()`), fade/slide animations on cards and
  toasts, a product search box, and colour-coded badges for urgency levels.

## Next steps you may want to add

- Add JWT-based auth instead of plain login, if your project requires session security.
- Replace the plain HTML/JS frontend with React if you want component structure — the REST API doesn't need to change.
- The AI prediction is a moving-average heuristic; if you want a "real" ML model for
  the viva, you could swap `PredictionService`'s calculation for a call to a small
  Python (scikit-learn) microservice without changing the `/api/predictions/reorder`
  contract the frontend depends on.
