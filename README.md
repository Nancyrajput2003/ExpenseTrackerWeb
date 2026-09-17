📊 ExpenseTrackerWeb

A smart and user-friendly Java Expense Tracker web application designed to simplify expense management with expense categorization, sorting, searching, reports, user profiles, and persistent data storage.

📑 Pages

Page	File	Purpose
Router	index.html	Checks profile/session state, redirects to the right page
Sign in	login.html	First-run onboarding: Name, Full Name, Email
Welcome	welcome.html	Greets the user by name, then continues to dashboard
Dashboard	dashboard.html	Add/list/sort/search expenses, monthly/yearly view, category pie chart, top expenses, activity log, reminders
Profile	profile.html	Name/email, today's spend, monthly income, logout, delete account
Each page is a real .html file loaded via normal browser navigation (window.location.href = '...'), not a single-page app with hidden divs. Shared logic (API calls, modal helpers, and the login/session guard) lives in common.js, which every page includes.

🔄 Flow

index.html (no profile) ──> login.html ──> submit ──> welcome.html ──> continue ──> dashboard.html
│                           │
(has profile, no session) ──> welcome.html ─────────────┘                           ├── nav ──> profile.html
(has profile + session)   ──> dashboard.html ───────────────────────────────────────└── nav back ┘
Logout (on the Profile page) clears the session flag and sends you back to welcome.html — your data is untouched, you just see the greeting again before re-entering the dashboard.

Delete Account removes the stored profile and expense data and returns the user to the sign-in page.

✨ Key Application Features

Expense Logging & Categorization: Record expenses dynamically with custom descriptions, amounts, timestamps, and spending categories.

Top Expense Tracking: Instant access to top-value expenses maintained dynamically by a Max-Heap structure for quick budget evaluation.

Activity Logging: Real-time activity log managed through a custom singly linked list and FIFO queue to track recent entries and reminders.

Dynamic Search & Multi-Criteria Filtering: Fast keyword search, category filtering, and binary search by exact transaction date.

Custom Sorting Algorithms: Custom QuickSort and MergeSort routines to organize financial records chronologically or by transaction value.

Java Built-in Web Server: Native HTTP backend built directly on Java's com.sun.net.httpserver.HttpServer.

Local Data Persistence: Automated profile and transaction storage using flat files (expenses.csv and profile.csv) managed via Java File I/O.

Responsive Multi-Page Dashboard: Clean visual Web interface for real-time spend monitoring and seamless interaction.

🛠️ Tech Stack & Core Concepts

Layer	Technologies & Skills
Frontend	HTML5, CSS3, JavaScript (Vanilla ES6, Fetch API, DOM Manipulation, HTML Canvas)
Backend & Networking	Java (JDK 11+), Native HttpServer, Custom JSON Utilities (JsonUtil.java)
Core Java Concepts	Object-Oriented Programming (OOP), Java File I/O, Exception Handling
Data Structures & Algorithms	Custom Singly Linked List, Binary Max-Heap, Circular Array Queue, QuickSort, MergeSort, Linear & Binary Search
Persistence	Java File I/O (FileStorage.java, ProfileStorage.java), Flat CSV File Storage


🏗️ DSA-to-Feature Mapping

The UI itself doesn't label anything with these names (no "HashMap" or "Linked List" headings in the app) — the structures work behind the scenes:

Concept	Where it lives	Feature
Array / Dynamic Array	ArrayList<Expense> in ExpenseManager	Backs the expense list
Sorting	dsa/SortUtils.java — hand-written QuickSort & MergeSort	Dashboard "Sort" control
Searching	dsa/SearchUtils.java — Linear & Binary search, plus linear-search monthly/yearly filters	Keyword search, exact-date search, monthly/yearly view, "today" stat
HashMap	ExpenseManager.fields	Category totals (feeds the pie chart, O(1) lookup on delete)
Linked List	dsa/MyLinkedList.java — custom singly linked list	Activity Log
Queue	dsa/MyQueue.java — custom circular array queue	Reminders
Heap	dsa/MaxHeap.java — custom binary max-heap	Top Expenses

📁 Project Structure

ExpenseTrackerWeb/
├── src/
│   ├── Main.java              # starts the server
│   ├── model/
│   │   ├── Expense.java
│   │   └── UserProfile.java
│   ├── dsa/
│   │   ├── SortUtils.java     (QuickSort + MergeSort)
│   │   ├── SearchUtils.java   (Linear + Binary search, month/year/day filters)
│   │   ├── MyLinkedList.java  List
│   │   ├── MyQueue.java       Queue
│   │   └── MaxHeap.java       Heap
│   ├── core/
│   │   ├── ExpenseManager.java # wires all DSA structures together
│   │   └── ProfileManager.java # single local user profile (onboarding/income/delete)
│   ├── util/
│   │   ├── FileStorage.java   # expenses.csv persistence
│   │   └── ProfileStorage.java# profile.csv persistence
│   └── web/
│       ├── ApiServer.java     # HTTP routing (built-in HTTP server)
│       └── JsonUtil.java      # Tiny hand-rolled JSON reader/writer
└── webroot/                   # router assets
├── index.html             # router logic
├── login.html             # login screen
├── welcome.html           # welcome screen
├── dashboard.html         # dashboard screen
├── profile.html           # profile screen
├── common.js              (shared API helpers + session guard)
└── style.css

🚀 How to Run

Requirements
JDK 11 or higher

IntelliJ IDEA or any Java-compatible IDE

Using IntelliJ IDEA
Open the ExpenseTrackerWeb project.

Make sure the project SDK is configured.

Locate Main.java inside src.

Run Main.java.

Open http://localhost:8080 in your browser.

The application stores profile and expense data locally using CSV files.

Note: Run the application from the project root so that the webroot/, expenses.csv, and profile.csv files are located correctly.

🎯 Learning Outcomes

This project helped me practice:

Core Java and Object-Oriented Programming

Data Structures & Algorithms

Searching and sorting

Java File I/O

HTML, CSS, and JavaScript

DOM manipulation

Basic browser-to-server communication

Building a Java-based web application
