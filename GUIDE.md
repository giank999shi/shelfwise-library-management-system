# Shelfwise — School Library Management System — Step-by-Step Guide

A Java (Spring Boot) web application with a MySQL database.
Four kinds of users: **Admin, Librarian, Teacher, Student**.

---

## PART 1 — Install the tools (one time only)

| # | Tool | Where to get it | Check |
|---|------|-----------------|-------|
| 1 | **JDK 17 or newer** (e.g. Temurin) | adoptium.net | Open a terminal and run `java -version`. It should say 17 or higher. |
| 2 | **MySQL Community Server 8** | dev.mysql.com/downloads/installer | During setup you will be asked for a **root password**. Write it down. |
| 3 | **IntelliJ IDEA Community** (easiest; Maven is built in) | jetbrains.com/idea | VS Code (Java Extension Pack), Eclipse and NetBeans also work. |

Make sure MySQL is **running**. On Windows: press Start, type *Services*, find **MySQL80**, and check that its status is *Running*.

---

## PART 2 — Open the project

1. **Unzip** `library-system.zip` (right-click → Extract All). Don't run it from inside the zip.
2. Open IntelliJ → **File → Open** → choose the `library-system` folder (the one that contains `pom.xml`) → **Trust Project**.
3. Wait for the progress bar at the bottom to finish ("Resolving Maven dependencies"). The first time it downloads libraries, so you need internet. This can take a few minutes.
4. If IntelliJ shows a popup like *"Maven project detected → Load"*, click it.
5. Check the Java version: **File → Project Structure → Project → SDK** should be 17 or newer.

---

## PART 3 — Put in your MySQL password

1. In the left panel open: `src/main/resources/application.properties`
2. Find this line:
   ```
   spring.datasource.password=CHANGE_ME
   ```
3. Replace `CHANGE_ME` with your MySQL root password. Save the file.

You do **not** need to create the database or tables yourself. The app creates the `school_library` database and all tables the first time it starts.

---

## PART 4 — Run it

1. In the left panel open: `src/main/java/com/school/library/LibraryApplication.java`
2. Click the green **▶** next to `public static void main` → **Run**.
3. Watch the console at the bottom. When you see **`Started LibraryApplication`** it is ready.
4. Open your browser and go to: **http://localhost:8080**

To stop the app, click the red ■ in IntelliJ. To start it again, click ▶.

*(Command-line alternative, if Maven is installed: open a terminal in the project folder and run `mvn spring-boot:run`.)*

---

## PART 4b — Running it in VS Code (instead of IntelliJ)

1. Install **JDK 17+** and **MySQL 8** (Part 1). Make sure MySQL is running.
2. Open VS Code → Extensions (`Ctrl+Shift+X`) → install **Extension Pack for Java** (Microsoft). Optional: also **Spring Boot Extension Pack**.
3. **File → Open Folder** → choose the unzipped `library-system` folder (the one with `pom.xml`) → **Trust**.
4. Wait for the Java extension to finish loading (spinner in the bottom bar). The first time it downloads libraries, so you need internet.
5. Open `src/main/resources/application.properties` and replace `CHANGE_ME` with your MySQL root password. Save.
6. Open `src/main/java/com/school/library/LibraryApplication.java` and click **Run** above `public static void main`.
   (With the Spring Boot pack you can also use the **Spring Boot Dashboard** in the sidebar and click ▶.)
7. When the terminal shows `Started LibraryApplication`, open **http://localhost:8080** in your browser.
8. To stop: click the red ■ (or press `Ctrl+C` in the terminal).

If errors don't clear after loading: `Ctrl+Shift+P` → **Java: Clean Java Language Server Workspace** → Restart.

---

## PART 5 — Demo logins

These are created automatically the first time the app runs.

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Librarian | `librarian` | `librarian123` |
| Teacher | `teacher` | `teacher123` |
| Student | `student` | `student123` |

On the sign-in page, the **"Try a demo account"** cards fill in the username and password for you (you still click **Sign in**).
Once you've created your own accounts, hide those cards by changing this line in `application.properties` to `app.demo-accounts=false`.

Sample data is added too: **members** 1 (Demo Student) and 2 (Demo Teacher), and **books** 1 to 4.

---

## PART 6 — 10-minute guided tour

### As Admin (`admin` / `admin123`)
1. You see the dashboard with live counts (books, members, active loans, due soon, overdue).
2. **Books → + Add book**. Add *Florante at Laura*, author *Francisco Balagtas*, 2 copies. Note its **ID**.
3. **Members → + Add member**. Add *Maria Santos*, type **Student**. Note her **Member ID**.
4. **Accounts → + Add account**. Username `maria`, password `maria123`, role **Student**, linked member *Maria Santos*.
5. Click the **Librarians** tab on the Accounts page. You can add more librarian accounts here.

### As Librarian
6. Click **Logout** (it asks you to confirm), then log in as `librarian` / `librarian123`.
7. **Borrow/Return**: type Maria's Member ID and the book ID, then click **Borrow**. The message shows the due date (students get 7 days, teachers 14).
8. Try borrowing a 4th book for the same student. You get *Borrowing limit reached*. Try borrowing a book with 0 copies left. You get *Book not available*.
9. In the **Active loans** table, click **Return** → confirm. The book's available copies go back up by one.
10. **History**: search by member name or by book title.
11. **Overdue**: lists overdue books and books due within 2 days.
    *To test overdue without waiting a week:* in MySQL Workbench run
    `UPDATE school_library.loans SET due_date = DATE_SUB(CURDATE(), INTERVAL 3 DAY) WHERE id = 1;`
    then reload the Overdue page.
12. **Reports**: open each of the 5 reports. Click **Print**, and **Export CSV** (opens in Excel).

### As Student (`maria` / `maria123`, or `student` / `student123`)
13. The dashboard shows the books you have and whether they are due soon or overdue.
14. **Search** a book and check its available copies. **My History** shows your past loans.
15. Try typing `localhost:8080/staff/books` in the address bar. You get **Access denied**, because students can't manage books.

### Back as Admin
16. **Logs** shows everything that happened: logins, failed logins, borrowing, returning, and every add, edit and delete.

---

## PART 6b — The design (Shelfwise look)

The pages follow your Figma login design: navy-to-blue panel, blue buttons, rounded cards, Inter font, and a left sidebar with icons for every role.
- All colors, spacing and fonts are in one file: `src/main/resources/static/css/style.css`. The colors are listed at the top under `:root`.
- The sidebar and the shared page pieces are in `src/main/resources/templates/fragments.html`.
- The sign-in page is `templates/login.html`. **Remember me** keeps you signed in for 14 days. **Forgot password?** tells people to ask the librarian or admin, who can reset it from **Accounts → Edit**.
- The Inter font loads from Google Fonts, so it needs internet. Without internet the system falls back to a similar font and everything still works.

---

## PART 7 — Who can do what

| Feature | Admin | Librarian | Teacher / Student |
|---|:-:|:-:|:-:|
| Dashboard | ✅ (statistics) | ✅ | ✅ (own loans) |
| Search books, view availability | ✅ | ✅ | ✅ |
| Manage books (add / edit / delete) | ✅ | ✅ | ❌ |
| Manage members (students and teachers) | ✅ | ✅ | ❌ |
| Borrow / Return books | ✅ | ✅ | ❌ |
| Overdue monitoring | ✅ | ✅ | own loans only |
| Borrowing history | all loans | all loans | own only |
| Reports (print / CSV) | ✅ | ✅ | ❌ |
| User accounts and librarians | ✅ | ❌ | ❌ |
| Activity logs | ✅ | ❌ | ❌ |

**Borrowing rules:** Student: 3 books, 7 days. Teacher: 5 books, 14 days. A book counts as "due soon" within 2 days of its due date.

---

## PART 8 — Before you present or really use it

The demo accounts have weak passwords. To replace them:
1. Log in as `admin` → **Accounts → + Add account** → create your own **Admin** account.
2. Log out, log in with your new account, and **delete** (or change the passwords of) `admin`, `librarian`, `teacher` and `student`.
3. Don't delete *every* account: if the accounts table is empty, the demo accounts are created again on the next start.

---

## PART 9 — Troubleshooting

| Problem | Fix |
|---|---|
| `Access denied for user 'root'@'localhost'` | Wrong password in `application.properties`. |
| `Communications link failure` / `Connection refused` | MySQL isn't running. Start the MySQL80 service. |
| `Port 8080 was already in use` | Close the other program, or add `server.port=8081` to `application.properties` and use http://localhost:8081 |
| The page still looks old after a change | Press `Ctrl+F5` in the browser to reload the CSS without the cache. |
| `release version 17 not supported` / `invalid source release` | Your JDK is older than 17. Install JDK 17+ and set it in File → Project Structure → SDK. |
| Red errors / "cannot resolve" everywhere | Maven hasn't finished downloading. Open the **Maven** panel (right side) and click the refresh icon. Check your internet. |
| "Whitelabel Error Page" in the browser | Look at the red text in the console and send it to me. The first line of the error usually shows the cause. |
| You want a clean start | In MySQL Workbench run `DROP DATABASE school_library;` then start the app again. |
| You can't install MySQL right now | Run in **test mode** with no MySQL: *Run → Edit Configurations → Active profiles* → type `h2`, then run. Data is kept in memory and is wiped every time you stop the app. |

---

## PART 10 — Where things are in the code (for your documentation)

| System flow | Code |
|---|---|
| A. Login, role check | `config/SecurityConfig`, `AppUserDetailsService`, `templates/login.html`, `DashboardController` |
| B/C/L. Dashboards | `templates/dashboard-admin.html`, `dashboard-librarian.html`, `dashboard-member.html` |
| D. Book management | `BookAdminController`, `BookService`, `books.html`, `book-form.html` |
| E. Member management | `MemberAdminController`, `MemberService`, `members.html`, `member-form.html` |
| F. Borrowing | `LoanService.borrow`, `LoanController`, `borrow.html` |
| G. Returning | `LoanService.returnBook` |
| H. Overdue monitoring | `LoanService.overdue / dueSoon`, `Loan.isOverdue / isDueSoon`, `overdue.html` |
| I. Book search | `BookController`, `BookService.search`, `search.html` |
| J. Borrowing history | `StaffHistoryController`, `MyController`, `LoanService.searchHistory` |
| K. Reports + CSV | `ReportController`, `ReportService`, `report.html` |
| M. Logout | Logout button in `fragments.html` (with confirmation), `SecurityConfig` |
| Accounts / librarians | `UserAdminController`, `UserService` |
| Activity logs | `ActivityLogService`, `AuthEventLogger`, `logs.html` |
| Design | `static/css/style.css`, `templates/fragments.html` (sidebar, icons, sign-in panel) |
| Database (MySQL) | `model/` (entities) and `repository/`. `docs/reference-schema.sql` shows the 5 tables. |
