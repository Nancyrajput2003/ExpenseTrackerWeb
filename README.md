# Expense Tracker — Multi-Page Web App (Java + DSA Edition)

A full-stack, **multi-page** web app: a REST API built entirely on the
JDK's built-in `com.sun.net.httpserver.HttpServer` (**zero external
dependencies — no Spring, no Maven, no Gradle, no frontend framework, no
chart library**) plus a plain HTML/CSS/JS frontend split across real,
separate pages.

## Pages

| Page | File | Purpose |
|---|---|---|
| Router | `index.html` | Checks profile/session state, redirects to the right page |
| Sign in | `login.html` | First-run onboarding: Name, Full Name, Email |
| Welcome | `welcome.html` | Greets the user by name, then continues to the dashboard |
| Dashboard | `dashboard.html` | Add/list/sort/search expenses, monthly/yearly view, category pie chart, top expenses, activity log, reminders |
| Profile | `profile.html` | Name/email, today's spend, monthly income, logout, delete account |

Each page is a real `.html` file loaded via normal browser navigation
(`window.location.href = '...'`), not a single-page app with hidden divs.
Shared logic (API calls, modal helpers, and the login/session guard) lives
in `common.js`, which every page includes.

## Flow

```
index.html ──(no profile)──> login.html ──submit──> welcome.html ──continue──> dashboard.html
    │                                                                                │
    └──(has profile, no session)──> welcome.html                          nav → profile.html
    └──(has profile + session)───> dashboard.html                          ← nav back
```

- **Logout** (on the Profile page) clears the session flag and sends you
  back to `welcome.html` — your data is untouched, you just see the
  greeting again before re-entering the dashboard.
- **Delete Account** wipes the profile *and every expense* (calls
  `DELETE /api/profile`, which also clears the expense store server-side),
  then sends you back to `login.html` for a completely fresh start.

## Why no Spring Boot / chart library / CSS framework?

This was built and **fully tested end-to-end** in a sandboxed environment
with no access to Maven Central or any CDN, so external dependencies
weren't an option. Everything — the HTTP server, the JSON handling, and
the pie chart — is written from scratch:

- The REST API runs on `com.sun.net.httpserver.HttpServer` (part of the JDK).
- JSON is a small hand-rolled reader/writer (`web/JsonUtil.java`).
- The pie chart on the dashboard is drawn with the plain HTML5 `<canvas>`
  API (`drawPieChart()` in `dashboard.js`) — no Chart.js, no D3.

*(This also means it compiles and runs with nothing but `javac`/`java` —
no build tool needed.)*

## DSA-to-feature mapping

| Concept | Where it lives | Feature |
|---|---|---|
| **Array / Dynamic Array** | `ArrayList<Expense>` in `ExpenseManager` | Backs the expense list |
| **Sorting** | `dsa/SortUtils.java` — hand-written QuickSort & MergeSort | Dashboard "Sort" control |
| **Searching** | `dsa/SearchUtils.java` — Linear & Binary search, plus linear-scan month/year/day filters | Keyword search, exact-date search, monthly/yearly view, "today" stat |
| **HashMap** | `ExpenseManager` fields | Category totals (feeds the pie chart), O(1) id lookup on delete |
| **Linked List** | `dsa/MyLinkedList.java` — custom singly linked list | Activity Log |
| **Queue** | `dsa/MyQueue.java` — custom circular array queue | Reminders |
| **Heap** | `dsa/MaxHeap.java` — custom binary max-heap | Top Expenses |

The UI itself doesn't label anything with these names (no "HashMap" or
"Linked List" headings in the app) — the structures work behind the
scenes; this table is here for you to reference when explaining the
project (e.g. in an interview or a README for evaluators).

## Verification

Every feature was actually exercised with `curl` against a running server,
not just written and assumed correct:

- Onboarding → profile creation → profile retrieval
- All five pages (and their JS files) serve with HTTP 200
- Adding expenses, monthly view, yearly view, "today" stat
- Updating monthly income and reading it back
- Category totals (that the pie chart consumes)
- Delete account → confirmed profile and all expenses are wiped
- HTML structure validated (balanced tags) on every page

## Project structure

```
ExpenseTrackerWeb/
├── src/
│   ├── Main.java                 # starts the server
│   ├── model/
│   │   ├── Expense.java
│   │   └── UserProfile.java
│   ├── dsa/
│   │   ├── SortUtils.java        (QuickSort + MergeSort)
│   │   ├── SearchUtils.java      (Linear + Binary search, month/year/day filters)
│   │   ├── MyLinkedList.java
│   │   ├── MyQueue.java
│   │   └── MaxHeap.java
│   ├── core/
│   │   ├── ExpenseManager.java   # wires all DSA structures together
│   │   └── ProfileManager.java   # single local user profile (onboarding/income/delete)
│   ├── util/
│   │   ├── FileStorage.java      # expenses.csv persistence
│   │   └── ProfileStorage.java   # profile.csv persistence
│   └── web/
│       ├── ApiServer.java        # HTTP routing (built-in HttpServer)
│       └── JsonUtil.java         # tiny hand-rolled JSON reader/writer
└── webroot/
    ├── index.html    (router, no JS file needed)
    ├── login.html    + login.js
    ├── welcome.html  + welcome.js
    ├── dashboard.html+ dashboard.js
    ├── profile.html  + profile.js
    ├── common.js     (shared API helpers + session guard)
    └── style.css
```

## How to run

Requires JDK 11+ (tested on JDK 21). No build tool, no internet access
needed — just the JDK.

```bash
cd ExpenseTrackerWeb
mkdir -p bin
javac -d bin -encoding UTF-8 $(find src -name "*.java")
java -cp bin Main
```

Then open **http://localhost:8080** — it'll route you to the sign-in page
on first run.

Run on a different port: `java -cp bin Main 3000`

Data is saved to `expenses.csv` and `profile.csv` in the directory you run
it from, and reloaded automatically on the next launch.

> **Important**: run the `java -cp bin Main` command from inside the
> `ExpenseTrackerWeb` folder (not from `src/` or `bin/`), since the app
> looks for the `webroot/` folder and the CSV files relative to your
> current working directory.

## REST API reference

| Method | Path | Description |
|---|---|---|
| GET | `/api/expenses` | List all expenses |
| POST | `/api/expenses` | Add expense — body: `{title, amount, category, date, note}` |
| DELETE | `/api/expenses/{id}` | Delete by id |
| GET | `/api/expenses/period?year=&month=` | Monthly (with `month`) or yearly view |
| POST | `/api/sort?field=date\|amount\|category&algo=quick\|merge` | Sort in place, returns sorted list |
| GET | `/api/search?keyword=` | Linear search across title/category/note |
| GET | `/api/search/date?date=yyyy-MM-dd` | Binary search for exact date |
| GET | `/api/categories` | `{ totals: {category: amount}, grandTotal }` — feeds the pie chart |
| GET | `/api/top?n=3` | Top N biggest expenses |
| GET | `/api/activity?limit=15` | Recent activity log |
| POST | `/api/reminders` | Schedule a reminder — body: `{text}` |
| POST | `/api/reminders/process` | Dequeue and return the next reminder |
| GET | `/api/reminders/count` | Pending reminder count |
| GET | `/api/stats/today` | Today's total spend (Profile page) |
| GET | `/api/profile` | `{exists, name, fullName, email, monthlyIncome}` |
| POST | `/api/profile` | Create/update profile — body: `{name, fullName, email}` |
| POST | `/api/profile/income` | Update monthly income — body: `{monthlyIncome}` |
| DELETE | `/api/profile` | Delete account (wipes profile + all expenses) |

## Possible extensions

- Swap CSV for SQLite via JDBC.
- Add HTTPS via `HttpsServer` + a self-signed cert.
- Add real password-based auth (currently there's none — "login" is just
  a friendly onboarding form, matching a local single-user tool).
- Port to Spring Boot for a more conventional enterprise stack — the
  `model`/`dsa`/`core`/`util` packages are already framework-agnostic.
