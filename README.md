# FastPOS — Cloud-Based Point-of-Sale System

A full-stack POS system built with **Spring Boot 3** and **React.js**, demonstrating end-to-end Java full-stack development.

## 🚀 Features

- **Product Catalog CRUD** — Full product management with categories, search, and pagination
- **Real-time Cart Management** — In-memory per-user carts with live stock validation
- **Order Processing** — Complete order workflow (PENDING → PAID → COMPLETED)
- **PDF Invoice Generation** — iText7-powered branded invoices
- **JWT Authentication** — Secure BCrypt + JWT Bearer token auth with role-based access
- **Stripe Integration** — PaymentIntent creation, card checkout, webhook confirmation
- **Async Email Delivery** — Thread-pool-backed order confirmation emails
- **Sales Analytics Dashboard** — Date-range filtering, daily revenue charts, top products, category breakdown
- **CSV/JSON Data Export** — Download sales reports in multiple formats

## 🏗️ Architecture

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3, Java 21, Spring Security, Spring Data JPA |
| Frontend | React 18, Vite 5, Recharts, React Router |
| Database | H2 (dev) / PostgreSQL 16 (prod) |
| Payments | Stripe API with webhook verification |
| PDF | iText 8 |
| API Docs | Springdoc OpenAPI / Swagger UI |
| CI/CD | Jenkins, Docker, AWS (EC2/RDS/S3) |

## 📦 Quick Start

### Prerequisites
- Java 21 (JDK)
- Maven 3.9+
- Node.js 22+

### Backend
```bash
cd pos-backend
mvn spring-boot:run
```
Backend runs at http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html
H2 Console: http://localhost:8080/h2-console

### Frontend
```bash
cd pos-frontend
npm install
npm run dev
```
Frontend runs at http://localhost:5173

### Docker
```bash
docker-compose up --build
```

## 🔑 Demo Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@fastpos.com | admin123 |
| Cashier | cashier@fastpos.com | cashier123 |
| Customer | customer@fastpos.com | customer123 |

## 📡 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/auth/register | Register new user | Public |
| POST | /api/auth/login | Login | Public |
| GET | /api/products | List products | Public |
| POST | /api/products | Create product | Admin |
| GET | /api/cart | Get cart | JWT |
| POST | /api/cart/add | Add to cart | JWT |
| POST | /api/orders | Create order | JWT |
| GET | /api/orders | Order history | JWT |
| POST | /api/payments/create-intent | Stripe payment | JWT |
| POST | /api/payments/cash | Cash payment | JWT |
| GET | /api/invoices/{orderId} | Download PDF | JWT |
| GET | /api/analytics/sales | Sales data | Admin/Cashier |
| GET | /api/analytics/export/csv | CSV export | Admin/Cashier |

## 📁 Project Structure

```
pos-backend/    — Spring Boot REST API
pos-frontend/   — React.js SPA
docker-compose.yml
Jenkinsfile
```

## 🧪 Testing

```bash
cd pos-backend
mvn test
```

## License
MIT
