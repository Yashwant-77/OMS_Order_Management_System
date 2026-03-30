# 🛒 Order Management System (OMS)

## 📌 Overview

This is a full-stack **Order Management System** built using **Angular (frontend)** and **Spring Boot (backend)**.
The system allows users to manage products, create orders, and track order status efficiently.

---

## 🚀 Tech Stack

### 🔹 Frontend

* Angular
* TypeScript
* HTML, CSS

### 🔹 Backend

* Spring Boot
* Spring Data JPA
* Spring Security
* REST APIs

### 🔹 Database

* MySQL

---

## 📂 Project Structure

```
OMS_ORDER_MANAGEMENT_SYSTEM/
├── backend/
│   └── OMS_backend_springboot/
├── frontend/
│   └── OMS_frontend_angular/
```

---

## ⚙️ Setup Instructions

### 🔹 1. Clone the Repository

```
git clone https://github.com/your-username/OMS_ORDER_MANAGEMENT_SYSTEM.git
cd OMS_ORDER_MANAGEMENT_SYSTEM
```

---

### 🔹 2. Backend Setup (Spring Boot)

1. Open the backend project in Eclipse or IntelliJ
2. Navigate to:

```
backend/OMS_backend_springboot
```

3. Configure database in `application.properties`
4. Run the Spring Boot application

Backend will run on:

```
http://localhost:8080
```

---

### 🔹 3. Frontend Setup (Angular)

```
cd frontend/OMS_frontend_angular
npm install
ng serve
```

Frontend will run on:

```
http://localhost:4200
```

---

## 🔗 API Communication

Frontend communicates with backend via REST APIs:

```
http://localhost:8080/api/*
```

---

## ✨ Features

* 👤 User Management
* 📦 Product Management
* 🧾 Order Creation
* 📊 Order Tracking
* 🔐 Authentication & Authorization

---

## 📌 Future Enhancements

* 💳 Payment Integration
* 📧 Email Notifications
* 📊 Admin Dashboard
* 🐳 Docker Deployment

---

## 👨‍💻 Author

**Yashwant Chouhan**

---

## ⭐ Notes

* Ensure backend is running before starting frontend
* Update database credentials before running backend
* Use proper API endpoints for integration
