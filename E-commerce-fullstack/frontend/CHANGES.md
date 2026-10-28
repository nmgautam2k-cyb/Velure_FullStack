# Changes

## Created
**Backend** (`backend/`)
- `pom.xml`, `.env.example`, `.gitignore`, `src/main/resources/application.properties`
- `sql/schema.sql`, `sql/sample-data.sql`
- `EcommerceApplication.java`
- `config/`: `AppProperties`, `SecurityConfig`, `WebConfig`, `DataInitializer`
- `security/`: `JwtService`, `JwtAuthenticationFilter`, `UserPrincipal`, `CustomUserDetailsService`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`
- `entity/`: `User`, `Category`, `Product`, `Cart`, `CartItem`, `Order`, `OrderItem`, `WishlistItem`, enums `Role`, `OrderStatus`, `PaymentMethod`, `PaymentStatus`
- `repository/`: `User`, `Category`, `Product`, `Cart`, `CartItem`, `Order`, `OrderItem`, `Wishlist`
- `dto/`: 25 request/response records (incl. `PageResponse`, `SummaryResponse`)
- `service/` + `service/impl/`: Auth, Category, Product, Cart, Order, User, Wishlist; `FileStorageService`, `AdminSummaryService`
- `controller/`: Auth, Product, Category, Cart, Wishlist, Order, AdminOrder, AdminUser, AdminSummary, AdminUpload, Health
- `exception/`: `ApiException` family, `ErrorResponse`, `GlobalExceptionHandler`
- tests: `JwtServiceTest`, `FileStorageServiceTest`, `ApiFlowIntegrationTest`, `application-test.properties`

**Frontend / docs**
- `lib/api.ts` (API client, types, session helpers), `.env.example`
- `postman/Velure.postman_collection.json` (45 requests), `README.md` (rewritten), `CHANGES.md`

## Modified (design, layout and CSS untouched)
- `lib/auth.ts`: requires a token as well as a stored user; logout clears the token
- `app/components/Navbar.tsx`: logout clears the session, shows `fullName`
- `app/(shop)/login`, `register`: call the API. **Register no longer has the Admin/Customer dropdown** (it allowed self-service admin accounts)
- `app/admin/login`: calls the API
- `app/(shop)/shirts|pants|tshirts`: products from the API, server-side cart/wishlist, sort dropdown, loading/error/empty states, out-of-stock state
- `app/(shop)/cart`: server cart, quantity − / + buttons, Cart now continues to `/checkout`
- `app/(shop)/checkout`: collects/validates the address only (no longer creates an order); button reads "Continue to Payment"
- `app/(shop)/payment`: creates the single order; test-mode notice; double-click guard
- `app/(shop)/payment-success`: shows "Order Placed" with order number; no false "Payment Successful" for test/COD
- `app/(shop)/wishlist`: server wishlist
- `app/(shop)/orders`: loads own orders from the API, shows address and payment status
- `app/admin/page.tsx` (summary endpoint), `admin/orders`, `admin/products` (upload + stock field), `admin/users` (fullName, optional password)
- `.gitignore`: keep `.env.example` tracked

## Removed
- `app/api/orders|products|user|users/route.ts` (proxies to json-server on :5000)
- Not deleted but now unused: `db.json`, `db.json.backup`, the `json-server` dependency and `server` / `json-server` npm scripts (safe to remove; left so `npm ci` keeps matching `package-lock.json`)

## Feature checklist

Legend: ✅ implemented in code · 🧪 covered by an automated test or Postman request · ⚠️ **nothing has been executed yet** (no Maven/MySQL/npm in the build environment)

| Feature | Status |
|---|---|
| Register (validation, duplicate email, BCrypt, role forced to customer) | ✅ 🧪 |
| Login + JWT, role check, `/me`, 401 JSON errors | ✅ 🧪 |
| Role-based access (ADMIN / USER) on server | ✅ 🧪 |
| Products: list, get, search, category filter, sort, pagination, recent | ✅ 🧪 (search/sort via Postman only) |
| Admin product create/update/delete, price/stock validation, image upload to disk | ✅ 🧪 (upload via Postman, manual file) |
| Categories CRUD | ✅ 🧪 (create via Postman) |
| Cart: add, view, update qty, remove, clear, server totals, stock checks | ✅ 🧪 |
| Checkout: transactional order, server prices, stock lock + decrement, cart cleared after success | ✅ 🧪 |
| Order history, own-order isolation, admin status update / delete, cancel restocks | ✅ 🧪 |
| Wishlist per account | ✅ 🧪 |
| Admin dashboard summary, users CRUD with safeguards | ✅ (users CRUD: Postman list only) |
| Frontend ↔ API wiring for every page | ✅ (TypeScript syntax-checked only) |
| Real payment gateway | ❌ not built: COD + clearly-labelled test flow instead |
| Compile, `mvn test`, run against MySQL, click through the UI | ⚠️ **to do on your machine** |
