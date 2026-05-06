# 🚀 InvoKit – Smart Invoice & Business Management System

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=0:4facfe,100:00f2fe&height=200&section=header&text=InvoKit&fontSize=40&fontAlignY=35&animation=fadeIn" />
</p>

<p align="center">
  <img src="https://img.shields.io/github/stars/navneetranjan07/InvoKit?style=for-the-badge" />
  <img src="https://img.shields.io/github/forks/navneetranjan07/InvoKit?style=for-the-badge" />
  <img src="https://img.shields.io/github/license/navneetranjan07/InvoKit?style=for-the-badge" />
  <img src="https://img.shields.io/github/issues/navneetranjan07/InvoKit?style=for-the-badge" />
</p>

---

## 📌 Overview
**InvoKit** is a full-stack invoice and business management system designed for freelancers, startups, and small businesses. It allows users to create, manage, and track invoices, customers, and transactions efficiently.

The project focuses on real-world financial workflows including secure authentication, transaction tracking, and a modern UI.

---

## ✨ Features

### 🔐 Authentication & Security
- JWT-based authentication
- Role-based access (Admin/User)
- Secure login & signup
- Password reset via email
- OTP verification

### 💳 Invoice Management
- Create, update, delete invoices
- Add multiple items per invoice
- Automatic total calculation
- Invoice status tracking

### 👥 Customer Management
- Store customer details
- Link invoices to customers
- Track customer transactions

### 💸 Transactions System
- Transfer money between accounts
- Transaction history with types:
  - `TRANSFER_IN`
  - `TRANSFER_OUT`
- Real-time balance updates

### 📊 Dashboard
- User dashboard with account summary
- Admin dashboard for system overview
- Transaction insights

### 📁 File Handling
- Upload and share files/images
- Attach files to records

### 🎨 Modern UI/UX
- Fully responsive design
- Clean dashboard layout
- Mobile-friendly interface

---

## 🏗️ Tech Stack

<p align="center">

<img src="https://skillicons.dev/icons?i=java,spring,react,tailwind,js,html,css,git,github,vscode" />

</p>

### 🔙 Backend
- Java
- Spring Boot
- Spring Security (JWT)
- Hibernate / JPA
- Oracle DB (11g)

### 🔜 Frontend
- React (Vite)
- Tailwind CSS
- Axios

### ⚙️ Tools
- Git & GitHub
- Postman
- VS Code

---

## 📂 Project Structure

InvoKit/
├── backend/ # Spring Boot application
│ ├── controller/ # REST controllers
│ ├── service/ # Business logic
│ ├── repository/ # Database layer
│ ├── model/ # Entities / Models
│ └── security/ # JWT & security config
│
├── frontend/ # React application
│ ├── components/ # Reusable UI components
│ ├── pages/ # App pages/screens
│ ├── services/ # API calls
│ └── assets/ # Images, styles, etc.
│
└── README.md


---

## ⚙️ Installation & Setup

### Clone Repository
```bash
git clone https://github.com/navneetranjan07/InvoKit.git
```
- cd InvoKit

- Backend Setup (Spring Boot)
- cd backend
  - 1. Configure application.properties:
   - spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
   - spring.datasource.username=your_username
   - spring.datasource.password=your_password
   - spring.jpa.hibernate.ddl-auto=update

  - 2. Run the application:
   - mvn spring-boot:run
---

- Frontend Setup (React)
 - cd frontend
 - npm install
 - npm run dev
---

- API Endpoints (Sample)
 - Method	Endpoint	Description
 - POST	/auth/register	Register user
 - POST	/auth/login	Login
 - GET	/accounts	Get user accounts
 - POST	/transactions	Transfer money
 - GET	/invoices	Fetch invoices
---

- Future Improvements
 - Payment gateway integration
 - Email invoice sending
 - PDF invoice generation
 - Analytics & reports
 - Multi-currency support
 - Audit logs & fraud detection
 - ATM-like transaction simulation
---

- 👨‍💻 Author: 
  Navneet Ranjan
  GitHub: https://github.com/navneetranjan07
---

- ⭐ Show Your Support

- If you like this project
  ⭐ Star the repo | 
  🍴 Fork it | 
  🛠️ Contribute
