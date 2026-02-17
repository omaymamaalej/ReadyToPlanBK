#  ReadyToPlan – AI Business Plan Generator (Backend)

##  Overview

**ReadyToPlan** is a backend application built with Spring Boot that automatically generates structured and professional business plans using Artificial Intelligence.

The system collects and processes business data including:

- Company information  
- Products & services  
- Team members  
- Marketing strategies  
- Financial forecasts  

It then generates a complete AI-powered business plan using Google Gemini API.

---

##  Tech Stack

- **Java 11**
- **Spring Boot**
- **MongoDB**
- **RESTful APIs**
- **Google Gemini API**

---

##  Core Features

- Company management
- Product & service management
- Team management
- Marketing module
- Financial Forecast:
  - Revenue calculation
  - Expense calculation
  - Net result computation
- AI-generated business plan
- PDF export functionality

---

##  Getting Started

###  Clone the repository
git clone https://github.com/omaymamaalej/ReadyToPlanBK.git
cd ReadyToPlanBK

### Build the project
mvn clean install
Run the application
mvn spring-boot:run

### Environment Variables
GEMINI_API_KEY=your_api_key
MONGODB_URI=your_mongodb_uri
### API Architecture
The backend exposes REST APIs to:

Manage business entities

Calculate financial forecasts

Generate AI responses

Aggregate a complete business plan
### Author
Omayma Maalej
Master’s Degree in Software Engineering
AI & Full Stack Developer
