# Bookshop POS — Backend

The backend of a point-of-sale system built for a small bookshop — a Spring Boot
application that owns the database, all business logic, and the REST API. It also serves
the bundled frontend, so the whole system deploys as a single jar.

> For the full picture — what the project is, why it exists, and how the pieces fit
> together — see **[OVERVIEW.md](./OVERVIEW.md)**. This README is about running and building
> the backend specifically.

## Responsibilities

- Transactional sales (a sale, its line items, and the stock deduction all commit together
  or not at all).
- Product catalog with three item types: stocked products, no-stock services, and
  open-price items.
- Inventory tracking with a full movement/audit log.
- Cost and price snapshotting per sale line, and profit reporting derived from it.
- Daily/weekly reports with day drill-down, top items, and per-cashier totals.
- Scheduled per-product discounts applied automatically at checkout.
- Categories and suppliers.
- Authentication (session-based) and role-based authorization (`ADMIN` / `CASHIER`).
- Sales history.
- Scheduled database backups.

## Tech stack

- **Java 17+ / Spring Boot 3.3**
- **Spring Data JPA** (Hibernate)
- **Spring Security**
- **H2** database in file mode (single local file — no separate DB server)
- **Maven**

## Key design decisions

- Money is always `BigDecimal`, never `double`.
- Prices, costs, and applied discounts are **snapshotted onto each sale line** at sale time,
  keeping historical sales and profit accurate when products change later.
- The **server is authoritative on pricing** — the client sends only item ids and
  quantities; the server computes prices, discounts, and totals. Open-price items are a
  narrow, controlled exception.
- Records are **deactivated, not deleted**, so historical sales stay intact.

## Running locally

Requires **Java 17+** and **Maven**.

```bash
mvn spring-boot:run
```

Runs on `http://localhost:8080`. On first run against an empty database a default admin is
seeded so the first login is possible — **change this password immediately after logging
in.**

## Building a deployable jar

The frontend is bundled into the backend for deployment. After building the frontend (see
the frontend repo) and copying its `dist/` output into `src/main/resources/static/`:

```bash
mvn clean package
```

This produces a single runnable jar in `target/` (e.g. `pos-0.1.0.jar`) containing the
backend, the frontend, and an embedded web server.

## Deploying

The target machine needs only a **Java 17+ runtime** — no Maven, no Node, no separate
database.

```bash
# First run only — creates the database tables and seeds the first admin:
java -jar pos-0.1.0.jar --spring.jpa.hibernate.ddl-auto=update

# Every run after that:
java -jar pos-0.1.0.jar
```

The database is a file (`data/posdb.mv.db`) created next to the jar. In normal operation the
app validates the schema rather than altering it (`ddl-auto=validate`), so it never changes
the live database unexpectedly — the one-time `update` flag above is only for creating
tables on a fresh install. Open `http://localhost:8080` to use the system.

### Configuration

Set in `src/main/resources/application.properties`, overridable on the command line
(`--property=value`):

| Property | Purpose |
|---|---|
| `pos.backup.dir` | Where backups are written (point at a synced/cloud folder for off-machine copies) |
| `pos.backup.cron` | Backup schedule |
| `pos.backup.keep` | How many backups to retain |

## Related

- **Frontend repo:** https://github.com/LahiruSandaruwan77/BookShop-POS-Frontend
- **Project overview:** [OVERVIEW.md](./OVERVIEW.md)

---

*A learning project — a bookshop POS built from scratch and running in a real shop.*
