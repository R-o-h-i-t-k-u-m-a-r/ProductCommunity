
# ProductCommunity - Product Reveiw Platform

![Java](https://img.shields.io/badge/Java-17%2B-blue)

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen)

![Angular](https://img.shields.io/badge/Angular-19-red)

![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

## 📝 Description
Product community is a web application that provides:
- Role based user authentication and authorization
- Adding Product Review
- Requesting for Product Review if no product review is available
- Admin dashboard for management
- Adding Product
- Approving or Rejecting the peding reviews

## ✨ Key Features

### 🔐 Authentication System
- JWT-based secure authentication
- google OAuth authentication for sign-in/sign-up
- Email verification for new registrations
- Password reset functionality
- Role-based access control (User/Admin)

### 🛍️ Product-Community Features
- Product browsing with reviews
- Posting Product reviews
- Requesting for product reviews
- Product Review Requests history

### ⚙️ Admin Dashboard
- Add Product
- Approve or Reject the user pending reviews

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.5
- **Security**: Spring Security + JWT + OAuth2.0
- **Database**: MySQL 8.0
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **Build Tool**: Maven
- **Unit Testing**: Junit5 & Mockito

### Frontend
- **Framework**: Angular 19
- **UI Components**: Angular Material
- **State Management**: NgRx
- **Styling**: Bootstrap 5 + SCSS

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Angular CLI 19
- MySQL 8.0+


## 📸 Screenshots

### Project Homepage
![Product Dashboard](./screenshots/home_page.png)

### Authentication Flow
![Login Page](./screenshots/login_page.png)  
*User login interface*

![Registration](./screenshots/signup_page.png)  
*New user registration form*

### Product Management
| Admin View | User View |
|------------|-----------|
| ![Admin Dashboard](./screenshots/admin_dashboard_page.png) | ![User Dashboard](./screenshots/user_dashboard.png) |

### User Review Comment Post
![Review Comment](./screenshots/commenting_user_product_review.png)

### Product All Reveiws
![All Reveiws](./screenshots/customer_product_reviews.png)

### User Product Reveiw Request
![Product Reveiw Request](./screenshots/request_product_reveiw_page.png)

### Admin Product Add Page
![Add Product](./screenshots/add_product_page.png)

### Admin All Reviews Page
![Pending Reviews](./screenshots/pending_reviews_page.png)

### Admin All Product Reviews Request Page
![Product Pending Reveiw Request](./screenshots/pending_product_reviews_page.png)