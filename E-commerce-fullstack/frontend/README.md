# Velure E-commerce: Next.js + Spring Boot + MySQL

Your original Next.js storefront, now backed by a real **Java Spring Boot REST API** and a **MySQL** database
(JWT login, BCrypt passwords, server-side cart and order totals, admin panel on secured endpoints).

```
Browser ──► Next.js (http://localhost:3000)
              │  fetch + "Authorization: Bearer <JWT>"
              ▼
          Spring Boot API (http://localhost:8080)  ──►  MySQL  ecommerce_db
              └─ product images uploaded by the admin are stored on disk in backend/uploads/
```

| Part | Where | Port |
|---|---|---|
| Frontend (Next.js 16, React 19) | project root (`app/`, `lib/`, `public/`) | 3000 |
| Backend (Spring Boot 3.5, Java 21, Maven) | `backend/` | 8080 |
| Database (MySQL 8) | `ecommerce_db` | 3306 |

> **Status, please read:** this code was written and reviewed without access to Maven, MySQL or npm, so it has
> **not been compiled or run end to end yet**. What *was* checked: all Java files parse and have no unresolved
> references to each other, all TypeScript files parse, the Postman collection is valid JSON. Run the steps below
> (and `mvn test`) on your machine and expect to fix a small number of first-run issues. See *Troubleshooting*.

---

## 1. One-time setup (Windows)

1. **JDK 21**: install Temurin 21 (https://adoptium.net), then check: `java -version` (must show 21).
2. **Maven 3.9+**: https://maven.apache.org/download.cgi, add `bin` to PATH, check: `mvn -version`.
   (IntelliJ IDEA has Maven bundled; VS Code needs the "Extension Pack for Java".)
3. **MySQL Server 8 + MySQL Workbench**: https://dev.mysql.com/downloads/installer/. Remember the `root` password you set.
4. **Node.js 20+**: `node -v`.

## 2. Configure the backend (secrets never go in code)

```powershell
cd backend
copy .env.example .env
notepad .env
```

Fill in at least:

| Variable | Meaning |
|---|---|
| `DB_PASSWORD` | your MySQL root password (required, no default) |
| `JWT_SECRET` | 32+ random characters (required, no default). Generate: `openssl rand -base64 48` or any long random string |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | the first admin account, created on first start if no admin exists (min 8 chars) |
| `CORS_ALLOWED_ORIGINS` | defaults to `http://localhost:3000` |

`.env` is git-ignored. The database `ecommerce_db` is created automatically (`createDatabaseIfNotExist=true`).

## 3. Run the backend

```powershell
cd backend
mvn spring-boot:run
```

First start: Hibernate creates the tables, the app seeds the categories (`tshirts`, `shirts`, `pants`) and your admin.
Check it: open http://localhost:8080/api/health → `{"status":"UP"}`.

**Load the sample products** (the 12 from your old `db.json`): open `backend/sql/sample-data.sql` in MySQL Workbench and run it
(or `mysql -u root -p < backend/sql/sample-data.sql`). Re-running it does not create duplicates.

## 4. Run the frontend

```powershell
# project root
copy .env.example .env.local
npm install
npm run dev
```

Open http://localhost:3000. `NEXT_PUBLIC_API_BASE_URL` (in `.env.local`) points at the backend.
The old `json-server` is no longer used.

## 5. Try it and verify in MySQL

1. Register at `/register`, then log in at `/login`.
2. Add products to the cart, go to Cart → Checkout → choose a payment method → Order Placed.
3. In Workbench:

```sql
USE ecommerce_db;
SELECT id, full_name, email, role, LEFT(password, 7) AS hash_prefix FROM users;   -- hash_prefix looks like $2a$10$ (BCrypt)
SELECT id, name, price, stock_quantity FROM products;
SELECT * FROM carts; SELECT * FROM cart_items;
SELECT id, user_id, total_amount, order_status, payment_method, payment_status FROM orders;
SELECT * FROM order_items;                 -- unit_price = the price charged at order time
```

After an order, `products.stock_quantity` drops and the user's `cart_items` rows are gone.
Admin login: `/login` with role "Admin" (or `/admin/login`), using `ADMIN_EMAIL` / `ADMIN_PASSWORD`.

---

## What changed for the user

| Area | Before (json-server) | Now |
|---|---|---|
| Passwords | plain text in `db.json` | BCrypt hash in MySQL, never returned by the API |
| Register | form let anyone pick **Admin** | no role field; server always creates a customer |
| Login | browser downloaded all users and compared passwords | `POST /api/auth/login` → JWT |
| Cart | `localStorage` | MySQL, per user; totals computed by the server; stock checked |
| Checkout | total and prices came from the browser; the checkout page and the payment page each created an order | address page → payment page → **one** transactional server-side order from the saved cart |
| Admin | anyone could call json-server | every `/api/admin/**` call needs an ADMIN token |
| Images | admin images stored as base64 inside the database | uploaded to `backend/uploads/`, the database stores only the URL |

**Payments.** No payment gateway is integrated. *Cash on Delivery* is recorded as `PENDING` and becomes `PAID` when the
admin marks the order delivered. *Google Pay* / *PhonePe* are an explicit **test flow** (`TEST_SIMULATED`, nothing is charged,
and the UI says so). To go live, add a real gateway (e.g. Razorpay) and only set `PAID` after server-side signature verification.

**New features added on top of your original UI**
- Persistent **wishlist** per account (`/api/wishlist`), usable from any device.
- Cart **quantity − / +** buttons (limited by stock) and server-side stock checks everywhere.
- **Sort** dropdown (price low→high / high→low) on category pages, using the sort control you already had in `FilterBar`.
- Admin products: **stock quantity** field; admin dashboard uses one SQL summary query.
- Admin orders show shipping address and payment info; cancelling an order **restores stock**.
- Users admin: optional password on edit, cannot delete yourself or the last admin.

---

## API overview

Base URL `http://localhost:8080`. Errors always look like
`{"timestamp","status","error","message","path","fieldErrors"}`.

| | Endpoint | Access |
|---|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` | public / public / user |
| Products | `GET /api/products?keyword=&category=&categoryId=&sort=&page=&size=`, `GET /api/products/{id}`, `/search?keyword=`, `/recent`, `/category/{id}` | public |
| | `POST /api/products`, `PUT/DELETE /api/products/{id}` | admin |
| Categories | `GET /api/categories[/{id}]` | public |
| | `POST /api/categories`, `PUT/DELETE /api/categories/{id}` | admin |
| Cart | `GET /api/cart`, `POST /api/cart/items`, `PUT/DELETE /api/cart/items/{itemId}`, `DELETE /api/cart` | user |
| Wishlist | `GET /api/wishlist`, `POST/DELETE /api/wishlist/{productId}` | user |
| Orders | `POST /api/orders`, `GET /api/orders/my-orders`, `GET /api/orders/{id}` (own orders only) | user |
| Admin | `GET /api/admin/orders`, `GET/DELETE /api/admin/orders/{id}`, `PATCH /api/admin/orders/{id}/status` | admin |
| | `GET/POST /api/admin/users`, `PUT/DELETE /api/admin/users/{id}`, `GET /api/admin/summary`, `POST /api/admin/uploads` | admin |

Sort values: `newest` (default), `oldest`, `price_asc`, `price_desc`, `name` (`low` / `high` also work). Page size is capped at 100.
Roles in JSON are `"admin"` / `"customer"` (stored as `ADMIN` / `USER`).

## Testing with Postman

Import `postman/Velure.postman_collection.json`. Set the collection variables `adminEmail` / `adminPassword` to your `.env` values and
run the folders in order (Collection Runner works). The login requests store the JWT automatically; to do it by hand, open a request →
**Authorization** → type *Bearer Token* → paste the `token` from the login response. Folder 7 contains requests that **must** be rejected
(401/403): wrong password, customer calling admin endpoints, forged token, etc.

Automated tests: `cd backend && mvn test` (JWT unit tests, image-type detection, and an API flow test on in-memory H2:
register/login, role-escalation attempt, stock decrement, cross-user order access, cancel-restocks, wishlist).

## Security notes

- BCrypt hashing; JWT (HS256, expiry `JWT_EXPIRATION_MINUTES`, default 120); user and role are reloaded from MySQL on every request.
- Role checks are enforced on the server (`SecurityConfig`); hiding admin buttons is only cosmetic.
- Carts and orders are looked up through the *token's* user. Another user's order id returns 404.
- Prices, totals and stock are always read from MySQL inside the order transaction (`SELECT … FOR UPDATE` on the products).
- CORS allows only `CORS_ALLOWED_ORIGINS`. Uploaded files are identified by their bytes, not their name, and stored under random names.
- The JWT is kept in `localStorage` (simple, but readable by any XSS bug in the frontend). For a hardened production setup, move it to an
  `HttpOnly; Secure; SameSite` cookie and add CSRF protection.
- Before production also add: HTTPS, login rate limiting, and a CAPTCHA on registration.

## Production: why migrations instead of `ddl-auto=update`

`spring.jpa.hibernate.ddl-auto=update` is convenient while developing, but it only adds things (never renames or drops), cannot be reviewed
or rolled back, may differ between machines, and can silently apply the wrong change to a live database. In production run
`DDL_AUTO=validate` and manage the schema with versioned migrations (Flyway or Liquibase). `backend/sql/schema.sql` is a ready starting point
for the first migration (it mirrors the entities; I could not run it against MySQL, so diff it with what Hibernate generates first).

## Upgrading Spring Boot

`backend/pom.xml` uses Boot **3.5.x**, the last 3.x line, whose free support ended on 30 June 2026. Bump `<version>` to the newest 3.5 patch
now, and plan a move to Boot 4.x (Jackson 3, renamed starters, see Spring's migration guide) when convenient.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `Could not resolve placeholder 'DB_PASSWORD'` / `'JWT_SECRET'` | `backend/.env` missing or not in the folder you run `mvn` from (must be `backend/`). |
| `JWT_SECRET must be set and at least 32 bytes long` | use a longer secret. |
| `Access denied for user 'root'` / `Communications link failure` | wrong `DB_PASSWORD`, or MySQL service not running (Services → MySQL80). |
| `Unsupported class file major version` / `invalid target release: 21` | `mvn -version` must report Java 21; set `JAVA_HOME`. |
| Browser: *CORS error* | `CORS_ALLOWED_ORIGINS` must match the exact frontend origin (`http://localhost:3000`, not `127.0.0.1`). |
| Pages say "Cannot reach the server" | backend not running or `NEXT_PUBLIC_API_BASE_URL` wrong. Restart `npm run dev` after editing `.env.local`. |
| Product lists are empty | run `backend/sql/sample-data.sql`, or add products in the admin panel. |
| Cannot log in as admin | `ADMIN_PASSWORD` must be set (≥ 8 chars) before a start with **no admin in the table**; an admin is only auto-created when none exists. If you already registered a customer account you want to promote, run `UPDATE users SET role='ADMIN' WHERE email='you@example.com';` in Workbench, then log in again. |
| Old accounts from `db.json` don't work | by design: their passwords were stored in plain text. Register again. |
| Admin images don't show | the backend must be running (`/uploads/**` is served by it); `PUBLIC_BASE_URL` must be reachable from the browser. |
| Port 8080/3000 in use | set `SERVER_PORT` and update `NEXT_PUBLIC_API_BASE_URL` and `PUBLIC_BASE_URL`. |
| 409 when deleting a product | it appears in past orders. Set its stock to 0 instead. |

## Project layout

```
backend/                        Spring Boot API (Maven)
  pom.xml, .env.example, sql/{schema,sample-data}.sql
  src/main/java/com/ecommerce/{config,controller,service,service/impl,repository,entity,dto,security,exception}
  src/test/…                    unit + API flow tests (H2)
app/, lib/, public/             your Next.js frontend (lib/api.ts = central API client)
postman/                        Postman collection
CHANGES.md                      files created / modified / removed + feature checklist
```
