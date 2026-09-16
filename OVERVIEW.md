# Bookshop POS — Project Overview

A point-of-sale system built from scratch for a small neighbourhood bookshop, and actually
deployed and running in the shop. This document describes the project as a whole — what it
is, why it exists, how it's put together, and where it's going. For how to run each part,
see the individual repo READMEs (linked at the bottom).

## The problem

The shop sells a mix of things: books and stationery with barcodes, loose items and small
toys without them, grocery items, and services like photocopying and printing that have no
stock at all. Before this system, sales were rung up by hand — no automatic stock tracking,
no record of what sold, no easy answer to "how much did we make today?"

The goal was a single system that could:

- ring up sales quickly at the counter, using a barcode scanner;
- handle products, no-stock services, and variable-price miscellaneous items in one flow;
- track inventory automatically and keep the numbers explainable;
- tell the owner what was sold and how much profit was made;
- run reliably on the shop's single PC, offline, with its data protected.

## What it does

- **Fast, scanner-driven checkout.** Scan or search, build the cart, take payment, print a
  receipt. Designed to be keyboard-first so the cashier rarely reaches for the mouse.
- **Three kinds of sellable item:**
  - *Stocked products* (books, pens) — barcoded, inventory-tracked.
  - *Services* (photocopy, printout) — priced, but no stock.
  - *Open-price items* (miscellaneous toys and sundries) — no fixed price; the cashier
    enters it at sale time, and profit is derived from a stored margin percentage.
- **Inventory with an audit trail.** Every stock change — a sale, a delivery, an
  adjustment — is recorded as a movement with a reason, so the current quantity is always
  explainable, not just a number.
- **Accurate cost and profit.** Each sale line snapshots both the price *and* the cost at
  the moment of sale, so profit reports stay historically correct even after prices or
  supplier costs change.
- **Reports.** Daily and weekly sales and profit summaries, with per-day drill-down,
  top-selling items, and per-cashier totals.
- **Scheduled discounts.** Admins can set percentage or fixed-amount discounts on products
  for a date range (e.g. a seasonal promotion); they apply automatically at checkout.
- **Products, categories, and suppliers.** Products are organized and filterable by both
  category and supplier.
- **Staff accounts and roles.** `ADMIN` (full access) and `CASHIER` (billing only), with
  hashed passwords and a forced password change on first login.
- **Sales history.** Browse and inspect past sales — useful for reviewing activity and
  settling customer queries.
- **Automatic backups.** The database is backed up on a schedule so the shop's data is
  protected.

## How it's built

The system is two codebases that ship as **one runnable program**:

- **Backend** — a Spring Boot application that owns the database, all business logic, and a
  REST API. ([backend repo](#))
- **Frontend** — a React application providing the cashier and admin screens. ([frontend
  repo](#))

For deployment, the built frontend is bundled inside the backend and served as static
files, so the whole system runs as a **single Java jar** — one process, one file to launch,
no separate web server, no internet required. It runs against a local file-based database
(H2), so there's nothing to install on the shop PC beyond a Java runtime.

### Design principles worth knowing

- **Money is always `BigDecimal`, never `double`** — no floating-point rounding on bills.
- **Snapshot the truth at sale time.** Prices, costs, and applied discounts are frozen onto
  each sale line when the sale happens, rather than looked up later — so history never
  silently changes when you edit a product.
- **The server is authoritative on pricing.** The client sends only what and how many; the
  server looks up prices, applies active discounts, and computes totals. The one controlled
  exception is open-price items, and even then the server only honors a client-entered
  price for products explicitly flagged as open-price.
- **Nothing is truly deleted.** Products, users, and similar records are deactivated, not
  removed, so historical sales that reference them stay intact.
- **Derive from the clock, don't mutate on a timer.** "This week", "active discount today",
  and similar are computed live from the current date — there are no scheduled jobs that
  reset state.

## Status and roadmap

The system is **deployed and running in the shop.** It was built incrementally, feature by
feature, with each step verified before moving on.

Planned / possible next work:

- **Returns and voids** — a safe way to reverse a sale (reversing stock and adjusting
  reports together), rather than editing the database by hand.
- **Multi-level discounts** — targeting discounts by category or supplier, not just
  individual products, with a most-specific-wins rule.
- **Inventory cost accounting** — weighted-average cost when stock arrives at a new price,
  instead of overwriting the cost.
- **Thermal receipt printing** — ESC/POS printing on dedicated receipt hardware, alongside
  the current browser-based receipt.
- **Keyboard-flow polish** — hotkeys for services and common actions for an even faster
  counter workflow.

## Repos

- **Backend:** https://github.com/LahiruSandaruwan77/BookShop-POS-Backend
- **Frontend:** https://github.com/LahiruSandaruwan77/BookShop-POS-Frontend

---

*A learning project — built to understand how POS systems work by building one that
actually runs a shop.*
