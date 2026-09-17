# 📊 ExpenseTrackerWeb

Track your expenses. Organize your spending. Understand your finances. ExpenseTrackerWeb combines Java, DSA, HTML, CSS, and JavaScript to provide a simple and interactive expense-management experience.

---

## ✨ Key Application Features

- **Expense Logging & Categorization**: Record expenses dynamically with custom descriptions, amounts, timestamps, and spending categories.
- **High-Priority Spending Insights**: Instant access to top-value expenses maintained dynamically by a Max-Heap structure for quick budget optimization.
- **Activity Logging & Audit Trail**: Real-time activity log managed through a FIFO queue to track recent entries and profile updates.
- **Dynamic Filtering & Search**: Instant keyword search and multi-criteria category filtering powered by handcrafted search algorithms.
- **Chronological & Monetary Sorting**: Flexible record sorting by date or transaction magnitude driven by custom sorting routines.
- **Embedded Web API Server**: Pure Java-based HTTP server utilizing RESTful endpoints to serve dynamic frontend components and exchange JSON data.
- **Local File Data Persistence**: Automatic profile and transaction storage using structured File I/O to retain user financial records across server restarts.
- **Responsive Interactive Dashboard**: Clean visual Web interface for real-time spend monitoring and seamless desktop or mobile user interaction.

---

## 🛠️ Tech Stack & Core Concepts

| Layer | Technologies & Skills |
| :--- | :--- |
| **Frontend** | HTML5, CSS3, JavaScript (Vanilla ES6, Fetch API, DOM Manipulation) |
| **Backend & Networking** | Java (JDK 8+), Core `HttpServer`, Custom JSON Utilities (`JsonUtil`) |
| **Core Java Concepts** | Object-Oriented Programming (OOP), File I/O, Exception Handling |
| **Data Structures & Algorithms** | Custom Singly Linked List, Max-Heap (Priority Queue), FIFO Queue, Custom Search & Sort |
| **Persistence** | Java File I/O (`FileStorage`, `ProfileStorage`), Flat-file JSON Storage |

---

## 🏗️ Project Architecture & Custom DSA Implementation

The application is structured into a clean multi-tier architecture using Core Java, custom Data Structures, HTML, CSS, and JavaScript:

1. 🎨 **Frontend Presentation Layer (`webroot/`)**: Built using HTML5 and CSS3 for page structure and styling, paired with Vanilla JavaScript. Client-side scripts utilize the Fetch API to make asynchronous HTTP requests to backend endpoints and update the DOM dynamically.

2. 🌐 **Web API & Service Layer (`src/web/`)**: Driven by Java's native `com.sun.net.httpserver.HttpServer` (`ApiServer`). It routes incoming HTTP requests, uses custom `JsonUtil` helpers to transform data between JSON formats and Java objects, and directs operations to core managers.

3. 🧱 **Core Business Logic (`src/core/` & `src/model/`)**: Handled by `ExpenseManager` and `ProfileManager`. These classes enforce application rules, process transaction inputs, manage user profiles, and orchestrate underlying data structures.

4. ⚡ **Custom DSA Engine (`src/dsa/`)**:
   -  **`MyLinkedList`**: A custom-implemented singly linked list used for dynamic memory allocation, linear storage, and sequential traversal of transaction records.
   -  **`MaxHeap`**: A custom priority queue structure designed to track high-value transactions, providing priority access to top expenses.
   -  **`MyQueue`**: A handcrafted First-In-First-Out (FIFO) queue that logs user actions and maintains an activity audit stream.
   -  **`SortUtils` & `SearchUtils`**: Custom algorithmic implementations for sorting transactions chronologically or by monetary value, alongside search routines for filtering by category or keyword.

5.  **Data Persistence Layer (`src/util/`)**: Uses Java File I/O (`FileStorage` and `ProfileStorage`) to read and write application states directly to local JSON files, ensuring data persists across application restarts without relying on external databases.

---

## 📁 Repository Structure

```text
ExpenseTrackerWeb/
├── src/
│   ├── core/         # Business logic layer (ExpenseManager, ProfileManager)
│   ├── dsa/          # Custom DSA implementations (MyLinkedList, MaxHeap, MyQueue, Sort, Search)
│   ├── model/        # Data models (Expense, UserProfile)
│   ├── util/         # Data persistence layer (FileStorage, ProfileStorage)
│   ├── web/          # HTTP Server and REST API Handlers (ApiServer, JsonUtil)
│   └── Main.java     # Main execution entry point
└── webroot/          # Frontend Web Assets
    ├── index.html    # Main landing page
    ├── dashboard.html# User dashboard UI
    ├── style.css     # Clean visual styling
    └── *.js          # Vanilla JS scripts (Fetch API & DOM logic)
