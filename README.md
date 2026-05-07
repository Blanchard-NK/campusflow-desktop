# CampusFlow Desktop

> Professional JavaFX desktop client for the CampusFlow University Management System.  
> Dark dashboard theme · REST API integration · MVC architecture · Ready to run.

---

## ⚡ Quick Start

```bash
# 1 — enter the project directory
cd campusflow-desktop

# 2 — run directly (downloads dependencies automatically)
mvn javafx:run

# 3 — OR build a fat JAR then run it
mvn clean package -DskipTests
java -jar target/campusflow-desktop-1.0.0.jar
```

**Login screen:**  
Click **"Lancer en mode démo"** to explore without a running backend.  
Real credentials: `admin@campusflow.fr` / `password`

---

## 📋 Prerequisites

| Tool      | Minimum version |
|-----------|----------------|
| JDK       | 17             |
| Maven     | 3.8            |
| Backend   | CampusFlow API running at `http://localhost:8000` (optional) |

---

## 📁 Project Structure

```
campusflow-desktop/
├── pom.xml                          ← Maven (JavaFX 21, Jackson 2.16)
├── README.md
└── src/main/
    ├── java/
    │   ├── module-info.java         ← Java module descriptor
    │   └── com/campusflow/
    │       ├── app/
    │       │   └── CampusFlowApp.java       ← Entry point (extends Application)
    │       ├── config/
    │       │   └── AppConfig.java           ← BASE_URL, timeouts, demo flag
    │       ├── models/                      ← POJOs + Jackson annotations
    │       │   ├── Student.java
    │       │   ├── Teacher.java
    │       │   ├── Course.java
    │       │   ├── Review.java
    │       │   └── DashboardStats.java      ← Client-side aggregation
    │       ├── api/
    │       │   ├── ApiClient.java           ← Singleton HTTP client (java.net.http)
    │       │   └── ApiException.java        ← Typed exception with HTTP status
    │       ├── services/
    │       │   ├── AuthService.java         ← POST /auth/login → JWT
    │       │   └── DataService.java         ← Fetch + demo fallback
    │       ├── controllers/                 ← FXML controllers (one per page)
    │       │   ├── LoginController.java
    │       │   ├── MainController.java      ← Shell + sidebar navigation
    │       │   ├── DashboardController.java ← 7 charts
    │       │   ├── StudentsController.java
    │       │   ├── TeachersController.java
    │       │   ├── CoursesController.java
    │       │   └── ReviewsController.java   ← Satisfaction analytics
    │       └── utils/
    │           ├── SessionManager.java      ← JWT + user session (singleton)
    │           ├── SceneRouter.java         ← Centralised FXML navigation
    │           └── FxUtils.java             ← Threading helpers + UI factories
    └── resources/
        ├── fxml/
        │   ├── login.fxml                   ← Split-panel login screen
        │   ├── main.fxml                    ← App shell (sidebar + topbar)
        │   ├── dashboard.fxml               ← 7 charts + 5 KPI cards
        │   ├── students.fxml
        │   ├── teachers.fxml
        │   ├── courses.fxml
        │   └── reviews.fxml
        └── styles/
            └── app.css                      ← Full dark theme stylesheet
```

---

## 🌐 API Endpoints

| Method | Path             | Description                  |
|--------|-----------------|------------------------------|
| POST   | `/auth/login`    | Authenticate → receive JWT   |
| GET    | `/api/students`  | List all students            |
| GET    | `/api/teachers`  | List all teachers            |
| GET    | `/api/courses`   | List all formations          |
| GET    | `/api/reviews`   | List student feedback        |

All GET requests send: `Authorization: Bearer <JWT>`

**Supported JSON shapes:**
```json
// Plain array
[{ "id": 1, "first_name": "Emma" ... }]

// Laravel paginated
{ "data": [{ "id": 1, ... }], "meta": { ... } }
```

---

## ⚙️ Configuration

Edit `src/main/java/com/campusflow/config/AppConfig.java`:

```java
public static final String BASE_URL = "http://localhost:8000"; // ← your API
public static final int CONNECT_TIMEOUT = 10;  // seconds
public static final int READ_TIMEOUT    = 30;  // seconds
public static final boolean DEMO_MODE   = true; // show demo button on login
```

---

## 📊 Dashboard Charts

| # | Type       | Data                              |
|---|-----------|-----------------------------------|
| 1 | PieChart   | Students grouped by level         |
| 2 | BarChart   | Enrollment count per formation    |
| 3 | BarChart   | Average grade per course (/20)    |
| 4 | BarChart   | Average satisfaction per course   |
| 5 | LineChart  | Monthly activity trend (reviews)  |
| 6 | KPI labels | Totals: students, teachers, etc.  |
| 7 | Gauge      | Global satisfaction rate (0–100%) |

---

## 🔧 Adding a New Page

1. Create `MyModel.java` in `models/`
2. Add `fetchMyData()` to `DataService`
3. Create `mypage.fxml` in `resources/fxml/`
4. Create `MyPageController.java` implementing `MainController.RefreshableController`
5. Add a nav item in `main.fxml` with `fx:id="navMyPage"`
6. Wire it in `MainController`: add the `@FXML` field, `onNavMyPage()` method, and a case in `navigateTo()`

---

## 🎨 UI Theme

Dark academic dashboard palette:

| Token        | Value     | Used for               |
|-------------|-----------|------------------------|
| Background  | `#0f172a` | Page background        |
| Surface     | `#1e293b` | Cards, sidebar         |
| Border      | `#334155` | Dividers, field borders|
| Text        | `#f1f5f9` | Headings               |
| Muted       | `#64748b` | Labels, placeholders   |
| Primary     | `#2563eb` | Buttons, accents       |
| Success     | `#10b981` | Positive badges        |
| Warning     | `#f59e0b` | Alert badges           |
| Danger      | `#ef4444` | Error states           |
| Violet      | `#8b5cf6` | Master-level accents   |
| Cyan        | `#06b6d4` | BTS-level accents      |

---

## 📝 License

MIT — CampusFlow Desktop Client
