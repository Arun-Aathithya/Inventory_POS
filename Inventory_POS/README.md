# Inventory Management POS

A full-stack Point-of-Sale and inventory platform built with **Spring Boot (Java 17)**, **MySQL** and a **JavaScript** web frontend. It grew out of my console-based Inventory Management System into a REST API with a browser dashboard.

## Features
- **Billing / POS**: transactional checkout, atomic stock deduction (no overselling), barcode lookup
- **PDF invoices**: emailed to the customer automatically, plus a one-tap WhatsApp share link
- **Product, customer and supplier management**, including supplier price comparison
- **Automatic alerts**: low-stock and expiry alerts by email / SMS (Twilio) on scheduled jobs
- **Admin dashboard**: sales trend, category revenue and top products (Chart.js)
- **Sales forecasting and reorder suggestions** based on sales velocity and supplier lead time
- **Dynamic pricing suggestions** for slow-moving or near-expiry stock
- **Fraud detection** with explainable statistical rules (large bills, odd quantities, after-hours sales, rapid repeats)
- **Product recommendations** from co-purchase patterns
- **Rule-based chat assistant** that answers business questions from live data

## Tech stack
Java 17 · Spring Boot 3 · Spring Data JPA · MySQL · Maven · JavaScript · HTML/CSS · Chart.js · OpenPDF · SMTP / Twilio

## Project structure
```
pos_backend/    Spring Boot REST API (controllers, services, repositories, models)
pos_frontend/   Single-page web app (index.html)
```

## Run locally
1. Create the database and tables with the SQL schema (`inventorydb`).
2. Set the environment variables below.
3. Start the backend: `cd pos_backend && mvn spring-boot:run` (runs on http://localhost:8080)
4. Start the frontend: `cd pos_frontend && python3 -m http.server 5500`, then open http://localhost:5500

Full backend notes and endpoint details are in [pos_backend/README.md](pos_backend/README.md).

## Configuration (secrets via environment variables)

Passwords and tokens are **not** stored in the repo. Set these environment variables before running the backend:

| Variable | Purpose |
|---|---|
| `DB_USERNAME` | MySQL username (default `root`) |
| `DB_PASSWORD` | MySQL password |
| `MAIL_PASSWORD` | Gmail App Password for alert / invoice emails |
| `TWILIO_AUTH_TOKEN` | Twilio auth token (only if SMS/WhatsApp is enabled) |

Example (Linux/macOS):

```bash
export DB_PASSWORD=your_mysql_password
export MAIL_PASSWORD=your_gmail_app_password
mvn spring-boot:run
```

On Windows PowerShell use `$env:DB_PASSWORD="your_mysql_password"`.
