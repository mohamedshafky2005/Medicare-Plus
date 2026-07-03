# Medicare Plus

## Overview

Medicare Plus is a Java-based healthcare management system developed using Java Swing and SQLite. The application helps manage patients, doctors, appointments, notifications, and reports through a user-friendly desktop interface.

## Features

### Patient Management

* Add new patients
* Update patient information
* Delete patient records
* View patient details

### Doctor Management

* Add doctors
* Update doctor information
* Remove doctor records
* View doctor details

### Appointment Management

* Schedule appointments
* Manage appointment records
* Track patient-doctor appointments

### Notification Management

* Doctor notifications
* Patient notifications
* Appointment-related alerts

### Reports

* Generate healthcare reports
* View appointment summaries
* Monitor system records

## Technologies Used

* Java 17
* Java Swing (GUI)
* SQLite Database
* JDBC (SQLite Driver)
* Maven

## Project Structure

```text
Medicare-Plus
│
├── data/
│   └── medicare.db
│
├── src/main/java/
│   └── edu.syntaxsquad.medicare
│       ├── dao/
│       ├── model/
│       ├── service/
│       ├── ui/
│       └── util/
│
├── pom.xml
└── README.md
```

## Architecture

The project follows a layered architecture:

### Model Layer

Contains entity classes:

* Patient
* Doctor
* Appointment
* Notification

### DAO Layer

Handles database operations:

* PatientDAO
* DoctorDAO
* AppointmentDAO
* NotificationDAO

### Service Layer

Contains business logic:

* ReportService
* SchedulerService
* Notification Services

### UI Layer

Provides Java Swing graphical interfaces:

* Patient Panel
* Doctor Panel
* Appointment Panel
* Report Panel
* Notification Panels

### Utility Layer

Database configuration and helper functions.

## Database

The application uses SQLite as the database management system.

Database file:

```text
data/medicare.db
```

Tables are automatically created when the application starts.

## Prerequisites

Before running the project, ensure you have:

* JDK 17 or later
* Maven 3.8+
* IntelliJ IDEA / NetBeans / Eclipse

## Installation

### Clone the Repository

```bash
git clone <repository-url>
```

### Navigate to Project

```bash
cd Medicare-Plus
```

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn exec:java
```

Or run:

```text
Main.java
```

from your IDE.

## Main Modules

1. Patient Management
2. Doctor Management
3. Appointment Scheduling
4. Patient Notifications
5. Doctor Notifications
6. Report Generation

## Future Enhancements

* User authentication
* Role-based access control
* Email notifications
* Online appointment booking
* Dashboard analytics
* PDF report generation

## Authors

Developed by Syntax Squad.

## License

This project is developed for educational and academic purposes.
