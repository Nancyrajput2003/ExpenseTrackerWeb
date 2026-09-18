# 📊 ExpenseTrackerWeb

A smart and user-friendly Java Expense Tracker web application designed to simplify expense management with expense categorization, sorting, searching, reports, user profiles, and persistent data storage.

## 📑 Pages

| Page | File | Purpose |
| --- | --- | --- |
| **Router** | `index.html` | Checks profile/session state and redirects to the appropriate page |
| **Sign in** | `login.html` | First-run onboarding for user information |
| **Welcome** | `welcome.html` | Greets the user by name and continues to the dashboard |
| **Dashboard** | `dashboard.html` | Add, list, sort, and search expenses; view monthly/yearly data, category charts, top expenses, activity logs, and reminders |
| **Profile** | `profile.html` | View name/email, today's spending, monthly income, logout, and delete account |

Each page is a separate `.html` file loaded through normal browser navigation using `window.location.href`. Shared logic, including API calls, modal helpers, and the login/session guard, is handled through `common.js`.

## 🔄 Flow

**1.** `index.html` → Checks whether a profile exists

**2.** `login.html` → First-run user information

**3.** `welcome.html` → Welcomes the user

**4.** `dashboard.html` → Main expense management dashboard

**5.** `profile.html` → User profile and account management

### Account Actions

- **Logout:** Clears the session flag and returns the user to the welcome page. Stored expense data remains unchanged.
- **Delete Account:** Removes the stored profile and expense data and returns the user to the sign-in page.

## ✨ Key Application Features

- **Expense Logging & Categorization**  
  Record expenses with descriptions, amounts, timestamps, and spending categories.

- **Top Expense Tracking**  
  Maintains top-value expenses using a Max-Heap structure.

- **Activity Logging & Reminders**  
  Uses a custom singly linked list for activity history and a FIFO queue for reminders.

- **Dynamic Search & Filtering**  
  Supports keyword search, category filtering, and binary search for exact transaction dates.

- **Custom Sorting Algorithms**  
  Implements QuickSort and MergeSort for organizing financial records.

- **Java Built-in Web Server**  
  Uses Java's `com.sun.net.httpserver.HttpServer` for browser-to-server communication.

- **Local Data Persistence**  
  Stores profile and expense information in CSV files using Java File I/O.

- **Responsive Multi-Page Dashboard**  
  Provides an interactive web interface for managing and viewing expenses.

## 🛠️ Tech Stack & Core Concepts

| Layer | Technologies & Skills |
| --- | --- |
| **Frontend** | HTML5, CSS3, JavaScript, Fetch API, DOM Manipulation, HTML Canvas |
| **Backend & Networking** | Java, `HttpServer`, custom JSON utilities |
| **Core Java Concepts** | Object-Oriented Programming (OOP), Java File I/O, Exception Handling |
| **Data Structures & Algorithms** | ArrayList, Singly Linked List, Binary Max-Heap, Circular Array Queue, QuickSort, MergeSort, Linear Search, Binary Search |
| **Persistence** | Java File I/O and CSV file storage |

## 🏗️ DSA-to-Feature Mapping

The data structures and algorithms work behind the scenes to support different application features.

| Concept | Where it lives | Feature |
| --- | --- | --- |
| **Array / Dynamic Array** | `ArrayList<Expense>` in `ExpenseManager` | Stores the expense list |
| **Sorting** | `dsa/SortUtils.java` | Dashboard sorting |
| **Searching** | `dsa/SearchUtils.java` | Keyword search, exact-date search, monthly/yearly filters |
| **HashMap** | `ExpenseManager.fields` | Category totals and lookup operations |
| **Linked List** | `dsa/MyLinkedList.java` | Activity Log |
| **Queue** | `dsa/MyQueue.java` | Reminders |
| **Heap** | `dsa/MaxHeap.java` | Top Expenses |

## 📁 Project Structure

<pre>
ExpenseTrackerWeb/
├── src/
│   ├── Main.java
│   ├── model/
│   │   ├── Expense.java
│   │   └── UserProfile.java
│   ├── dsa/
│   │   ├── SortUtils.java
│   │   ├── SearchUtils.java
│   │   ├── MyLinkedList.java
│   │   ├── MyQueue.java
│   │   └── MaxHeap.java
│   ├── core/
│   │   ├── ExpenseManager.java
│   │   └── ProfileManager.java
│   ├── util/
│   │   ├── FileStorage.java
│   │   └── ProfileStorage.java
│   └── web/
│       ├── ApiServer.java
│       └── JsonUtil.java
│
└── webroot/
    ├── index.html
    ├── login.html
    ├── welcome.html
    ├── dashboard.html
    ├── profile.html
    ├── common.js
    └── style.css
</pre>

## 🚀 How to Run

### Requirements

- JDK 11 or higher
- IntelliJ IDEA or any Java-compatible IDE

### Using IntelliJ IDEA

1. Open the `ExpenseTrackerWeb` project.
2. Make sure the project SDK is configured.
3. Locate `Main.java` inside the `src` folder.
4. Run `Main.java`.
5. Open `http://localhost:8080` in your browser.

The application stores profile and expense data locally using CSV files.

> **Note:** Run the application from the project root so that `webroot/`, `expenses.csv`, and `profile.csv` are located correctly.

## 🎯 Learning Outcomes

This project helped me practice:

- Core Java and Object-Oriented Programming
- Data Structures & Algorithms
- Searching and sorting
- Java File I/O
- HTML, CSS, and JavaScript
- DOM manipulation
- Basic browser-to-server communication
- Building a Java-based web application