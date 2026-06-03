<div align="center">

# 💰 ExpenseTracker

**A production-grade personal finance Android application**
built with Jetpack Compose, Clean Architecture, Room, Hilt, and Gemini AI.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-blue)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-Academic-lightgrey)](LICENSE)

---

*University Final Project · Mobile Development · UIT (Ho Chi Minh City University of Information Technology)*

</div>

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Problem Statement](#2-problem-statement)
3. [Key Features](#3-key-features)
4. [Application Screens](#4-application-screens)
5. [Technical Architecture](#5-technical-architecture)
6. [Technology Stack](#6-technology-stack)
7. [Project Structure](#7-project-structure)
8. [Offline and Online Features](#8-offline-and-online-features)
9. [AI Features](#9-ai-features)
10. [OCR Receipt Scanning](#10-ocr-receipt-scanning)
11. [Statistics and Analytics](#11-statistics-and-analytics)
12. [Localization Support](#12-localization-support)
13. [Database Design](#13-database-design)
14. [Design Patterns and Architecture](#14-design-patterns-and-architecture)
15. [Libraries and Dependencies](#15-libraries-and-dependencies)
16. [Installation Guide](#16-installation-guide)
17. [Build and Run Instructions](#17-build-and-run-instructions)
18. [Future Improvements](#18-future-improvements)
19. [Challenges and Solutions](#19-challenges-and-solutions)
20. [Learning Outcomes](#20-learning-outcomes)



<details>
<summary><h2 style="display:inline">1. Project Overview</h2></summary>

**ExpenseTracker** is a fully-featured personal finance management application for Android. It helps users record income and expenses, manage multiple wallets and budgets, visualize spending patterns through rich analytics, and get AI-powered financial advice — all while working completely offline with an optional cloud AI layer.

| Attribute | Value |
|---|---|
| **Application ID** | `com.expensetracker.app` |
| **Version** | 1.0.0 (versionCode 1) |
| **Min SDK** | API 26 — Android 8.0 Oreo |
| **Target SDK** | API 35 — Android 15 |
| **Compile SDK** | API 36 |
| **Language** | Kotlin 2.2.10 |
| **UI Toolkit** | Jetpack Compose (BOM 2024.12.01) |
| **Build System** | Gradle 9.1.1 with KSP 2.2.10-2.0.2 |

The application is entirely written in Kotlin with a modern Android tech stack. All core functionality — transaction recording, budget management, statistics, and recurring transactions — works **fully offline**. AI chat, receipt parsing, and monthly insight generation are powered by **Google Gemini 2.5 Flash** and require an internet connection and a Gemini API key.

</details>



<details>
<summary><h2 style="display:inline">2. Problem Statement</h2></summary>

Managing personal finances is a universal need, yet most people fail to track their spending consistently because existing tools are either too complex, lack intelligent assistance, or don't match everyday workflows in Vietnamese contexts.

### Core Problems Addressed

**Manual tracking is tedious.** Users give up when recording expenses requires too many taps. ExpenseTracker addresses this with:
- A persistent floating "Quick Add" button accessible from every screen.
- A natural language input powered by Gemini — type "coffee 35k" and the AI parses the amount, category, and date.
- OCR receipt scanning that reads a photo of a receipt and fills in the transaction automatically.

**People don't know where their money goes.** The app solves this with:
- Visual statistics broken down by category, time period, and wallet.
- AI-generated monthly insights highlighting anomalies and spending trends.
- Budget tracking with proactive alerts at 80%, 90%, and 100% thresholds.

**Recurring expenses are forgotten.** The WorkManager-powered scheduler automatically creates recurring transactions (subscriptions, rent, salaries) on their due dates, even when the app is closed.

**Language barrier.** Vietnamese users often find financial apps in English confusing. ExpenseTracker supports full Vietnamese localization including all strings, currency formatting (VND), and date conventions.

</details>

---

<details>
<summary><h2 style="display:inline">3. Key Features</h2></summary>

### Core Finance Management
- ✅ **Multi-wallet support** — separate wallets for cash, bank accounts, e-wallets
- ✅ **Transaction CRUD** — income, expense, and transfer types with notes, photos, and location
- ✅ **Category management** — custom icons and colors, split by type (Income/Expense)
- ✅ **Budget tracking** — per-category budgets by week, month, year, or custom range
- ✅ **Recurring transactions** — daily, weekly, monthly, yearly schedules with interval support
- ✅ **Split transactions** — parent–child transaction relationships

### Intelligence
- ✅ **Gemini AI chat** — conversational financial advisor with full spending context injected as system prompt
- ✅ **Natural language quick-add** — parse "lunch 80k" into a complete transaction record
- ✅ **Voice input** — Speech Recognition integration in the quick-add sheet
- ✅ **OCR receipt scanning** — ML Kit text recognition + Gemini fallback parsing
- ✅ **Auto-categorization** — Gemini suggests a category when adding transactions
- ✅ **Monthly AI insights** — automatically generated spending analysis, saved to local DB

### Analytics
- ✅ **Income vs. Expense bar chart** (Vico library)
- ✅ **Category pie/donut breakdown**
- ✅ **Spending trend line chart** over time
- ✅ **Daily spend heatmap** — calendar-style intensity view
- ✅ **Multi-period comparison** — this month, last month, quarter, year, custom range

### UX & Platform
- ✅ **Material Design 3** with dynamic color (Material You) on Android 12+
- ✅ **Dark / Light / System theme** selector
- ✅ **Full Vietnamese localization** — all 243 strings translated
- ✅ **Animated skeleton loading** (shimmer effect) and Lottie animations
- ✅ **Animated amount transitions** when balances change
- ✅ **4 notification channels** — budget alerts, daily reminders, recurring transactions, AI insights
- ✅ **Background work** — WorkManager tasks survive app restarts and device reboots
- ✅ **Data backup and restore** — full JSON export/import including all wallets, categories, transactions, budgets, and recurring transactions
- ✅ **Splash screen** API integration
- ✅ **Edge-to-edge** display support
- ✅ **Onboarding flow** for first-time users

</details>

---

<details>
<summary><h2 style="display:inline">4. Application Screens</h2></summary>

### Bottom Navigation (4 Main Tabs)

| Tab | Screen | Description |
|---|---|---|
| 🏠 Home | `HomeScreen` | Dashboard with total balance, income/expense summary, recent transactions, active budgets, and AI insight cards |
| 📋 Transactions | `TransactionListScreen` | Full paginated list with search and filter by date, category, wallet |
| 📊 Statistics | `StatisticsScreen` | Charts and analytics for spending trends, category breakdowns, heatmaps |
| 🤖 AI | `AiAssistantScreen` | Chat interface with financial context, session history, localized suggestion chips |

### Transaction Screens
- **`AddEditTransactionScreen`** — Amount keypad, category picker, wallet picker, date selector, note, photo attachment; supports prefill from Quick Add and OCR
- **`TransactionDetailScreen`** — Full read view with edit/delete actions and split transaction support

### Category Screens
- **`CategoriesScreen`** — Grid of all categories filtered by type
- **`AddEditCategoryScreen`** — Name, icon picker (emoji/icon set), color picker, type selection

### Budget Screens
- **`BudgetsScreen`** — Budget cards with progress bars showing spent vs. limit
- **`AddEditBudgetScreen`** — Amount, category, period (weekly/monthly/yearly/custom), alert threshold

### Wallet Screens
- **`WalletManagementScreen`** — List of wallets with aggregated balances
- **`AddEditWalletScreen`** — Name, icon, color, initial balance, currency

### Recurring Transaction Screens
- **`RecurringListScreen`** — Active and inactive recurring schedules
- **`AddEditRecurringScreen`** — Configure frequency, interval, start/end dates, wallet, category

### Other Screens
- **`ReceiptScannerScreen`** — CameraX live preview with capture, OCR processing, and Gemini parsing fallback
- **`SettingsScreen`** — Theme, language, currency, notifications, default wallet, Gemini API key override
- **`OnboardingScreen`** — Welcome slides introducing core features
- **`GeminiTestScreen`** — Developer tool to verify API key connectivity

### Quick Add Bottom Sheet (global, accessible from all screens)
- **Manual Tab** — Fast amount + note entry, category chip selector
- **AI Parse Tab** — Natural language input with voice recognition button, confidence score display, and parsed transaction preview

</details>

---

<details>
<summary><h2 style="display:inline">5. Technical Architecture</h2></summary>

ExpenseTracker follows **Clean Architecture** with a strict three-layer separation.

```
┌─────────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                          │
│  Composable Screens  ←→  ViewModels  ←→  UI State (StateFlow)  │
│  Jetpack Compose · Hilt · Navigation · Material 3              │
└───────────────────────────┬─────────────────────────────────────┘
                            │ calls domain interfaces
┌───────────────────────────▼─────────────────────────────────────┐
│                       DOMAIN LAYER                              │
│      Repository Interfaces · Domain Models · Business Logic     │
│      Pure Kotlin · No Android dependencies                      │
└───────────────────────────┬─────────────────────────────────────┘
                            │ implemented by
┌───────────────────────────▼─────────────────────────────────────┐
│                        DATA LAYER                               │
│  Room DB · DataStore · Gemini API · ML Kit · WorkManager        │
│  Repository Impls · Mappers · DAOs · DTOs · Workers             │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow (Unidirectional)

```
User Action
    │
    ▼
Composable (UI event)
    │
    ▼
ViewModel (processes event, calls repository)
    │
    ▼
Repository Interface (domain boundary)
    │
    ▼
Repository Implementation (data layer)
    │             │
    ▼             ▼
Room DAO     Gemini API / ML Kit
    │
    ▼
Flow<Entity>
    │
    ▼ (mapper)
Flow<DomainModel>
    │
    ▼
ViewModel (updates StateFlow<UiState>)
    │
    ▼
Composable re-renders
```

### ViewModel State Pattern

All screens follow the single-state-stream pattern:

```kotlin
data class SomeUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null,
)

class SomeViewModel @Inject constructor(...) : ViewModel() {
    private val _state = MutableStateFlow(SomeUiState())
    val state: StateFlow<SomeUiState> = _state

    fun onAction(...) {
        _state.update { it.copy(...) }
    }
}
```

</details>

---

<details>
<summary><h2 style="display:inline">6. Technology Stack</h2></summary>

### Core Platform
| Technology | Version | Purpose |
|---|---|---|
| Kotlin | 2.2.10 | Primary language |
| Android SDK (min/target) | 26 / 35 | Platform targeting |
| Kotlin Coroutines | 1.9.0 | Async, threading |
| Kotlin Serialization | 1.7.3 | JSON, navigation args, backup |
| KSP | 2.2.10-2.0.2 | Compile-time code generation |

### UI Layer
| Technology | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2024.12.01 | Declarative UI framework |
| Material 3 | via BOM | Design system + dynamic color |
| Material Icons Extended | via BOM | Icon library |
| Lottie Compose | 6.6.0 | Vector animations |
| Coil 3 | 3.0.4 | Async image loading |
| Vico Charts | 2.0.0-M3 | Bar, line, and custom charts |
| Accompanist Permissions | 0.36.0 | Runtime permission helpers |

### Navigation
| Technology | Version | Purpose |
|---|---|---|
| Navigation Compose | 2.8.5 | Type-safe navigation with serialization |

### Dependency Injection
| Technology | Version | Purpose |
|---|---|---|
| Hilt Android | 2.59.2 | Full DI framework for Android |
| Hilt Navigation Compose | 1.3.0 | ViewModel injection in composables |
| Hilt Work | 1.3.0 | Worker injection |

### Data Persistence
| Technology | Version | Purpose |
|---|---|---|
| Room Runtime + KTX | 2.8.4 | Local SQLite database (ORM) |
| DataStore Preferences | 1.1.1 | User settings (key-value async) |

### Background Tasks
| Technology | Version | Purpose |
|---|---|---|
| WorkManager KTX | 2.9.1 | Guaranteed background processing |

### AI & Intelligence
| Technology | Version | Purpose |
|---|---|---|
| Google Generative AI SDK | 0.9.0 | Gemini 2.5 Flash integration |
| ML Kit Text Recognition | 16.0.1 | On-device OCR (Latin script) |

### Camera
| Technology | Version | Purpose |
|---|---|---|
| CameraX Camera2 | 1.4.2 | Camera hardware access |
| CameraX Lifecycle | 1.4.2 | Lifecycle-aware camera |
| CameraX View | 1.4.2 | PreviewView composable bridge |

### Lifecycle
| Technology | Version | Purpose |
|---|---|---|
| Lifecycle Runtime KTX | 2.8.7 | Lifecycle-aware coroutines |
| Lifecycle ViewModel Compose | 2.8.7 | ViewModel scoping |
| Lifecycle Runtime Compose | 2.8.7 | `collectAsStateWithLifecycle` |

</details>

---

<details>
<summary><h2 style="display:inline">7. Project Structure</h2></summary>

```
ExpenseTracker/
├── app/
│   ├── src/main/
│   │   ├── java/com/expensetracker/app/
│   │   │   │
│   │   │   ├── core/                          # Shared infrastructure
│   │   │   │   ├── designsystem/
│   │   │   │   │   ├── component/             # 10 reusable Compose components
│   │   │   │   │   │   ├── AmountText.kt
│   │   │   │   │   │   ├── AnimatedAmountText.kt
│   │   │   │   │   │   ├── AppButton.kt
│   │   │   │   │   │   ├── AppCard.kt
│   │   │   │   │   │   ├── AppTextField.kt
│   │   │   │   │   │   ├── CategoryChip.kt
│   │   │   │   │   │   ├── EmptyState.kt
│   │   │   │   │   │   ├── LoadingIndicator.kt
│   │   │   │   │   │   ├── SectionHeader.kt
│   │   │   │   │   │   └── ShimmerBox.kt
│   │   │   │   │   └── theme/                 # Theme, Colors, Typography, Shapes
│   │   │   │   ├── notification/
│   │   │   │   │   └── NotificationHelper.kt  # 4 channels, 4 alert types
│   │   │   │   └── util/
│   │   │   │       ├── CurrencyFormatter.kt
│   │   │   │       ├── DateUtils.kt
│   │   │   │       ├── Extensions.kt
│   │   │   │       └── LocaleHelper.kt
│   │   │   │
│   │   │   ├── data/                          # Data layer implementations
│   │   │   │   ├── local/
│   │   │   │   │   ├── dao/                   # 7 DAO interfaces
│   │   │   │   │   ├── database/              # AppDatabase + Seeder
│   │   │   │   │   ├── datastore/             # UserPreferencesDataStore
│   │   │   │   │   └── entity/                # 7 Room entities
│   │   │   │   ├── remote/gemini/
│   │   │   │   │   ├── GeminiApiService.kt    # Model config, request methods
│   │   │   │   │   ├── GeminiModels.kt        # Request/Response DTOs
│   │   │   │   │   └── GeminiPrompts.kt       # Prompt engineering
│   │   │   │   ├── mlkit/
│   │   │   │   │   ├── ReceiptOcrService.kt   # ML Kit text recognition
│   │   │   │   │   └── ReceiptParser.kt       # Multi-pass receipt parser
│   │   │   │   ├── mapper/                    # 6 Entity ↔ Domain mappers
│   │   │   │   ├── repository/                # 7 repository implementations
│   │   │   │   ├── worker/                    # 4 WorkManager workers
│   │   │   │   ├── budget/
│   │   │   │   │   └── BudgetAlertChecker.kt
│   │   │   │   └── backup/
│   │   │   │       └── BackupService.kt       # JSON export/import
│   │   │   │
│   │   │   ├── domain/                        # Business rules (pure Kotlin)
│   │   │   │   ├── model/                     # 20 domain models and enums
│   │   │   │   └── repository/                # 7 repository interfaces
│   │   │   │
│   │   │   ├── di/                            # Hilt modules
│   │   │   │   ├── DatabaseModule.kt          # Room + all DAOs
│   │   │   │   ├── DataStoreModule.kt         # DataStore singleton
│   │   │   │   ├── NetworkModule.kt           # Gemini API service
│   │   │   │   └── RepositoryModule.kt        # Interface → Impl bindings
│   │   │   │
│   │   │   ├── feature/                       # Feature modules
│   │   │   │   ├── home/                      # Dashboard
│   │   │   │   ├── transaction/               # List + Add/Edit + Detail
│   │   │   │   ├── category/                  # List + Add/Edit
│   │   │   │   ├── budget/                    # List + Add/Edit
│   │   │   │   ├── wallet/                    # List + Add/Edit
│   │   │   │   ├── recurring/                 # List + Add/Edit
│   │   │   │   ├── statistics/                # Charts + Analysis
│   │   │   │   ├── ai_assistant/              # Chat + History
│   │   │   │   ├── ocr_scan/                  # Camera + OCR
│   │   │   │   ├── settings/                  # Preferences
│   │   │   │   ├── onboarding/                # First-run flow
│   │   │   │   └── shell/                     # AppShell + Quick Add
│   │   │   │
│   │   │   ├── navigation/
│   │   │   │   ├── AppNavHost.kt              # Navigation graph
│   │   │   │   └── AppDestinations.kt         # Serializable route objects
│   │   │   │
│   │   │   ├── MainActivity.kt
│   │   │   └── ExpenseTrackerApp.kt
│   │   │
│   │   └── res/
│   │       ├── values/strings.xml             # 243 English strings
│   │       ├── values-vi/strings.xml          # 243 Vietnamese strings
│   │       ├── values/colors.xml
│   │       ├── values/themes.xml
│   │       └── drawable / mipmap              # Icons and assets
│   │
│   ├── schemas/                               # Room migration JSON schemas
│   └── build.gradle.kts                       # Dependencies + build config
│
├── build.gradle.kts                           # Root build config
├── settings.gradle.kts                        # Module includes
├── gradle.properties                          # Kotlin/AGP flags
└── local.properties                           # GEMINI_API_KEY (not committed)
```

</details>

---

<details>
<summary><h2 style="display:inline">8. Offline and Online Features</h2></summary>

### Fully Offline Features
All core functionality works without internet access. Data is stored locally using Room SQLite and DataStore.

| Feature | Storage |
|---|---|
| Transaction CRUD | Room (`transactions` table) |
| Category management | Room (`categories` table) |
| Budget tracking | Room (`budgets` table) |
| Wallet management | Room (`wallets` table) |
| Recurring transaction scheduling | Room + WorkManager |
| Statistics and charts | Computed from local Room queries |
| User preferences (theme, currency, language) | DataStore |
| Previously generated AI insights | Room (`ai_insights` table) |
| Chat message history | Room (`ai_chat_messages` table) |
| Backup export / import | Local file system (JSON) |

### Online Features (Require Gemini API Key)
| Feature | API |
|---|---|
| AI chat with financial context | Gemini 2.5 Flash |
| Natural language transaction parsing | Gemini 2.5 Flash (JSON mode) |
| Transaction auto-categorization | Gemini 2.5 Flash (JSON mode) |
| OCR parsing fallback and enhancement | Gemini 2.5 Flash |
| Monthly spending insights generation | Gemini 2.5 Flash |

### Graceful Degradation
- If `geminiEnabled = false` in settings, all AI features are disabled cleanly.
- If the API key is missing or invalid, error messages are shown in the AI chat; other screens remain unaffected.
- OCR falls back to pure ML Kit parsing if Gemini is unavailable.
- Previously generated AI insights remain accessible from the local database even when offline.

</details>

---

<details>
<summary><h2 style="display:inline">9. AI Features</h2></summary>

The AI integration uses **Google Gemini 2.5 Flash** (`gemini-2.5-flash`) via the official `com.google.ai.client.generativeai` SDK (version 0.9.0).

### Two Model Configurations
The app configures two separate `GenerativeModel` instances:
- **Chat model** (`temperature = 0.4`) — Conversational, slightly creative
- **JSON model** (`temperature = 0.2`) — Deterministic structured output for parsing

### Feature 1: AI Financial Chat

**How it works:**
1. On every message, the ViewModel calls `buildUserContext()` which gathers:
   - Current month's income/expense totals
   - Top 5 spending categories this month
   - Last month's summary for comparison
   - All active budgets with spend percentages
   - Total monthly recurring expense obligations
   - User's preferred currency
2. This JSON context is injected as the system prompt via `GeminiPrompts.buildChatSystemPrompt()`.
3. Full conversation history is sent with each request for multi-turn coherence.
4. Messages are persisted to Room (`ai_chat_messages` table) with UUID-based session IDs.
5. The latest session is automatically restored on next app open.

**UI:** Chat bubbles with markdown-aware rendering, typing indicator animation, session management (new chat, clear history).

### Feature 2: Natural Language Transaction Parsing

Triggered from the Quick Add sheet's "AI" tab and voice input:

```
Input:  "cafe 35k biên hòa sáng nay"
Output: { amount: 35000, type: EXPENSE, categoryId: <food_id>,
          note: "cafe biên hòa", date: <today>, confidence: 0.9 }
```

The prompt (`buildNlParsePrompt`) receives:
- The raw user text
- A list of all available categories with their names and types
- The user's currency

The JSON model returns a `ParsedTransactionDto` that is mapped to a prefilled `AddEditTransaction` screen.

### Feature 3: Auto-Categorization

When creating a transaction with a note but no category selected, Gemini evaluates the note and amount against the available category list and returns a suggested `categoryId` with a confidence score.

### Feature 4: Monthly AI Insights (Background)

The `MonthlyInsightsWorker` runs every 30 days via WorkManager. It:
1. Aggregates the previous month's transactions by category.
2. Sends a structured summary to Gemini with `buildMonthlyInsightsPrompt()`.
3. Receives 2–4 insight items (type, title, content).
4. Saves them to the `ai_insights` Room table.
5. Shows a notification linking to the AI tab.

**Insight types:** `MONTHLY_SUMMARY`, `ANOMALY`, `RECOMMENDATION`

### Feature 5: Receipt Parsing Enhancement

After ML Kit extracts raw text from a receipt image, Gemini's `buildReceiptParsePrompt()` is used as a fallback and enhancement step to extract structured fields from ambiguous OCR output.

</details>

---

<details>
<summary><h2 style="display:inline">10. OCR Receipt Scanning</h2></summary>

### Architecture

```
Camera (CameraX)
    │
    ▼ capture JPEG
ReceiptScannerScreen
    │
    ▼ image URI
ReceiptOcrService (ML Kit)
    │  InputImage from URI
    │  TextRecognizer (Latin script, on-device)
    ▼ raw OCR text (Result<String>)
ReceiptParser
    │  multi-pass heuristic extraction
    ▼ ParsedReceiptResult { amount, date, merchant, confidence }
    │
    ▼ (if confidence < threshold OR missing fields)
GeminiApiService.generateJson()
    │  buildReceiptParsePrompt(rawText)
    ▼ ReceiptParseResultDto
    │
    ▼
AddEditTransactionScreen (prefilled)
```

### ReceiptParser — Multi-Pass Extraction

The `ReceiptParser` class is entirely heuristic-based and handles both Vietnamese and English receipts:

**Amount Extraction (3 passes):**
- **Pass 1 (high confidence):** Searches for unambiguous Vietnamese keywords — `TIỀN THANH TOÁN`, `TỔNG CỘNG`, `THÀNH TIỀN`, `TỔNG PHẢI TRẢ`
- **Pass 2 (medium confidence):** Searches for general keywords — `TOTAL`, `SUBTOTAL`, `TỔNG`, `GRAND TOTAL`
- **Pass 3 (fallback):** Finds the largest numeric value on the receipt that exceeds 999, excluding VAT rates, phone numbers, and invoice reference numbers

**Number Parsing — Dual-format:**
- Vietnamese convention: `1.500.000` (dots as thousands separators)
- Western convention: `1,500.50` (comma as thousands, dot as decimal)

**Date Extraction — 7 patterns:**
- Vietnamese textual: `ngày 15 tháng 06 năm 2024`
- Numeric: `dd/MM/yyyy`, `dd-MM-yyyy`, `MM/dd/yyyy`
- ISO: `yyyy-MM-dd`
- Rejects dates more than 5 years old or in the future

**Merchant Name Extraction:**
- Identifies business name prefixes: `SIÊU THỊ`, `CAFÉ`, `NHÀ HÀNG`, `7-ELEVEN`, etc.
- Filters out receipt footer lines (addresses, phone numbers, invoice codes)

**Confidence Score (0.0–1.0):**
- Amount found via keyword: +0.5
- Amount found via fallback: +0.25
- Date found: +0.25
- Merchant found: +0.25

</details>

---

<details>
<summary><h2 style="display:inline">11. Statistics and Analytics</h2></summary>

The `StatisticsScreen` and `StatisticsViewModel` provide comprehensive spending analytics.

### Analysis Periods
| Period | Description |
|---|---|
| This Month | 1st of current month → today |
| Last Month | Previous calendar month |
| This Quarter | Current Q1/Q2/Q3/Q4 |
| Last Year | Previous full year |
| Custom Range | User-selected start and end date |

### Charts and Visualizations

**Income vs. Expense Bar Chart** (Vico `ColumnChart`)
- Side-by-side bars for income (green) and expense (red)
- Grouped by week or month depending on period length

**Category Breakdown Pie/Donut Chart**
- Proportional spending by category
- Color-coded by category color
- Tappable segments with labels and percentages

**Spending Trend Line Chart** (Vico `LineChart`)
- Daily cumulative spending over the selected period
- Smooth curve with data point markers

**Daily Spend Heatmap**
- Calendar-grid visualization of daily spending intensity
- Color gradient from light (low spend) to dark (high spend)
- Useful for identifying high-spend days or patterns

**Category Breakdown Table**
- Ranked list of categories with amount, percentage of total, and transaction count

### Data Aggregation
All statistics are computed from Room queries using:
- `getTotalByTypeAndDateRange()` — aggregate income/expense for a period
- `observeByDateRange()` — per-transaction data for charting
- `getSumByCategoryAndDateRange()` — category-level breakdowns

</details>

---

<details>
<summary><h2 style="display:inline">12. Localization Support</h2></summary>

The app supports **English (en)** and **Vietnamese (vi)** with complete parity — every user-visible string has a translation in both languages.

### String Resource Statistics
| File | Language | String Count |
|---|---|---|
| `res/values/strings.xml` | English | 243 |
| `res/values-vi/strings.xml` | Vietnamese | 243 |

### String Categories Covered
- Navigation labels and tab names
- All screen titles and section headers
- Button labels and action strings (Save, Cancel, Delete, Edit, etc.)
- Transaction types, categories, budget periods
- Error messages and validation feedback
- Notification titles and body text
- Onboarding slide content
- AI assistant prompts and suggestion chips
- Settings labels and descriptions
- Receipt scanner status messages
- Chart and statistics labels

### Runtime Language Switching

The `LocaleHelper` utility updates the app's `Configuration` at runtime:
- Language is stored in `UserPreferences.language` (DataStore)
- On `MainActivity` start, the stored locale is applied before UI rendering
- Changing language in Settings takes effect on next activity recreation

### Currency Formatting

`CurrencyFormatter` handles locale-aware currency display:
- Default currency: **VND** (Vietnamese Dong)
- Formats according to the selected currency code stored in preferences
- The AI system prompt always includes the user's currency for consistent AI responses

</details>

---

<details>
<summary><h2 style="display:inline">13. Database Design</h2></summary>

Room database version **2** with JSON schema export enabled. The database uses `fallbackToDestructiveMigration()` (appropriate for a prototype; production would use explicit migration scripts).

### Entity Relationship Diagram

```
┌──────────────┐       ┌───────────────────┐       ┌──────────────┐
│   wallets    │       │    transactions   │       │  categories  │
├──────────────┤       ├───────────────────┤       ├──────────────┤
│ id (PK)      │──────▶│ walletId (FK)     │◀──────│ id (PK)      │
│ name         │       │ id (PK)           │       │ name         │
│ icon         │       │ categoryId (FK)   │       │ icon         │
│ color        │       │ amount            │       │ color        │
│ initialBal   │       │ type (ENUM)       │       │ type (ENUM)  │
│ currency     │       │ note              │       │ isDefault    │
│ createdAt    │       │ date              │       │ isArchived   │
└──────────────┘       │ photoUri          │       └──────────────┘
                       │ location          │
┌──────────────┐       │ recurringId (FK)  │       ┌──────────────┐
│   budgets    │       │ parentSplitId(FK) │       │  recurring_  │
├──────────────┤       │ tags              │       │ transactions │
│ id (PK)      │       │ toWalletId (FK)   │       ├──────────────┤
│ categoryId(FK├───┐   │ createdAt         │  ┌───▶│ id (PK)      │
│ amount       │   │   │ updatedAt         │  │    │ walletId (FK)│
│ period(ENUM) │   │   └───────────────────┘  │    │ categoryId(FK│
│ startDate    │   │                           │    │ amount       │
│ endDate      │   └──── categories.id ────────┘    │ type (ENUM)  │
│ alertThresh  │                                    │ frequency    │
│ isActive     │   ┌───────────────────┐            │ interval     │
└──────────────┘   │  ai_chat_messages │            │ startDate    │
                   ├───────────────────┤            │ endDate      │
┌──────────────┐   │ id (PK)           │            │ nextOccur.   │
│  ai_insights │   │ role (ENUM)       │            │ lastProc.    │
├──────────────┤   │ content           │            │ isActive     │
│ id (PK)      │   │ timestamp         │            └──────────────┘
│ type (ENUM)  │   │ sessionId         │
│ title        │   └───────────────────┘
│ content      │
│ periodKey    │
│ generatedAt  │
│ dismissed    │
└──────────────┘
```

### TypeConverters
Room uses custom `TypeConverters` for:
- `LocalDate` ↔ `String` (ISO-8601)
- `LocalDateTime` ↔ `String` (ISO-8601)
- `TransactionType`, `BudgetPeriod`, `RecurrenceFrequency`, `ChatRole`, `InsightType`, `ThemeMode` ↔ `String` (name-based)

### Indexes
```sql
-- transactions table
INDEX(walletId), INDEX(categoryId), INDEX(date),
INDEX(recurringId), INDEX(parentSplitId)

-- budgets table
INDEX(categoryId)

-- recurring_transactions table
INDEX(walletId), INDEX(categoryId)
```

### Database Seeder
On first install, a `RoomDatabase.Callback` seeds default categories (Food, Transport, Shopping, Entertainment, etc.) with Vietnamese and English names, icons, and colors so the app is immediately usable.

</details>

---

<details>
<summary><h2 style="display:inline">14. Design Patterns and Architecture</h2></summary>

### MVVM (Model-View-ViewModel)
Every screen has a corresponding ViewModel that:
- Holds a single `UiState` data class as `MutableStateFlow`
- Exposes it as `StateFlow` to the composable
- Handles all business logic and side effects
- The composable only renders state and fires events

### Clean Architecture (3-Layer)
- **Domain layer:** Pure Kotlin interfaces and models — zero Android imports
- **Data layer:** Room, DataStore, Gemini, ML Kit, WorkManager implementations
- **Presentation layer:** Composables and ViewModels — depends only on domain interfaces

### Repository Pattern
Seven repository interfaces decouple the presentation and domain layers from data sources. Implementations in the data layer can be swapped without touching ViewModel code.

### Dependency Injection (Hilt)
All dependencies are injected via constructor injection:
- `@HiltAndroidApp` on `Application`
- `@AndroidEntryPoint` on `Activity`
- `@HiltViewModel` on all ViewModels
- `@Singleton` for database, DataStore, and API service instances
- `@Binds` for interface-to-implementation wiring

### Observer Pattern (Flow + Compose)
Room DAOs return `Flow<T>`. Repositories expose `Flow<DomainModel>`. ViewModels call `collectAsStateWithLifecycle()` to auto-cancel observation when the composable leaves the composition. This creates a live, reactive data pipeline from the database to the UI.

### Worker Pattern (WorkManager)
Background tasks implement `CoroutineWorker` with Hilt injection via `HiltWorkerFactory`. They are scheduled with `PeriodicWorkRequest` or `OneTimeWorkRequest`, using `ExistingPeriodicWorkPolicy.KEEP` to avoid duplicate scheduling.

### Mapper Pattern
Entity↔Domain conversion is isolated in mapper classes:
- `TransactionMapper`, `CategoryMapper`, `WalletMapper`, `BudgetMapper`, `AiChatMessageMapper`, `AiInsightMapper`, `RecurringTransactionMapper`

### Type-Safe Navigation
Routes are defined as `@Serializable` data objects and data classes, eliminating string-based navigation entirely:

```kotlin
@Serializable
data class AddEditTransaction(
    val id: Long? = null,
    val prefillAmount: Double? = null,
    val prefillNote: String? = null,
    val prefillCategoryId: Long? = null,
)
```

</details>

---

<details>
<summary><h2 style="display:inline">15. Libraries and Dependencies</h2></summary>

### Complete Dependency List

| Library | Version | Category |
|---|---|---|
| `androidx.core:core-ktx` | 1.18.0 | Android core |
| `androidx.activity:activity-compose` | 1.9.3 | Activity integration |
| `androidx.core:core-splashscreen` | 1.0.1 | Splash screen API |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.8.7 | Lifecycle extensions |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.8.7 | Compose lifecycle |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.8.7 | ViewModel in Compose |
| `androidx.compose` BOM | 2024.12.01 | Compose version management |
| `androidx.compose.ui:ui` | via BOM | Compose UI core |
| `androidx.compose.material3:material3` | via BOM | Material Design 3 |
| `androidx.compose.material:material-icons-extended` | via BOM | Extended icon set |
| `androidx.navigation:navigation-compose` | 2.8.5 | Navigation |
| `androidx.room:room-runtime` | 2.8.4 | Room ORM |
| `androidx.room:room-ktx` | 2.8.4 | Room coroutine extensions |
| `androidx.room:room-compiler` (KSP) | 2.8.4 | Room code generation |
| `com.google.dagger:hilt-android` | 2.59.2 | Dependency injection |
| `com.google.dagger:hilt-android-compiler` (KSP) | 2.59.2 | Hilt code generation |
| `androidx.hilt:hilt-navigation-compose` | 1.3.0 | Hilt + Compose nav |
| `androidx.hilt:hilt-work` | 1.3.0 | Hilt + WorkManager |
| `androidx.datastore:datastore-preferences` | 1.1.1 | Async preferences |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.9.0 | Coroutines |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.7.3 | JSON serialization |
| `com.google.ai.client.generativeai:generativeai` | 0.9.0 | Gemini AI SDK |
| `com.google.mlkit:text-recognition` | 16.0.1 | On-device OCR |
| `androidx.camera:camera-camera2` | 1.4.2 | Camera hardware |
| `androidx.camera:camera-lifecycle` | 1.4.2 | Camera lifecycle |
| `androidx.camera:camera-view` | 1.4.2 | Camera preview |
| `com.patrykandpatrick.vico:compose-m3` | 2.0.0 | Charts |
| `io.coil-kt.coil3:coil-compose` | 3.0.4 | Image loading |
| `com.airbnb.android:lottie-compose` | 6.6.0 | Lottie animations |
| `androidx.work:work-runtime-ktx` | 2.9.1 | WorkManager |
| `com.google.accompanist:accompanist-permissions` | 0.36.0 | Runtime permissions |

</details>

---

<details>
<summary><h2 style="display:inline">16. Installation Guide</h2></summary>

### Prerequisites

- **Android Studio** Ladybug (2024.x) or newer
- **JDK 17** or newer — AGP 9.x and Gradle 9.x require JDK 17 to run builds. The `compileOptions { sourceCompatibility = JavaVersion.VERSION_11 }` in `app/build.gradle.kts` sets the bytecode *target*, not the toolchain version.
- **Android device or emulator** running Android 8.0 (API 26) or higher
- **Gemini API key** (free tier available at [Google AI Studio](https://aistudio.google.com)) — required only for AI features

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd ExpenseTracker
```

### Step 2: Configure the API Key

Create or edit `local.properties` in the project root:

```properties
sdk.dir=/path/to/your/Android/Sdk
GEMINI_API_KEY=your_gemini_api_key_here
```

> The `GEMINI_API_KEY` is injected at compile time via `BuildConfig`. Without it, all AI features will be disabled. The rest of the app works without a key.

### Step 3: Open in Android Studio

1. Launch Android Studio.
2. Select **File → Open** and navigate to the `ExpenseTracker` folder.
3. Wait for Gradle sync to complete (first sync downloads all dependencies).

### Step 4: Run

1. Connect an Android device or start an emulator (API 26+).
2. Click **Run ▶** or use `Shift+F10`.

</details>

---

<details>
<summary><h2 style="display:inline">17. Build and Run Instructions</h2></summary>

### Debug Build (Development)

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

```bash
./gradlew assembleRelease
```

> Release builds require a signing keystore. Configure `signingConfigs` in `app/build.gradle.kts` with your keystore path and credentials.

### Run Tests

> **Note:** No application-specific tests have been written. The only test files present are the two Android Studio boilerplate stubs generated at project creation — `ExampleUnitTest` (`addition_isCorrect`) and `ExampleInstrumentedTest` (`useAppContext`). Writing a proper test suite is listed as a future improvement.

The commands below will execute those stubs successfully but do not verify any app logic:

```bash
# Unit tests (runs ExampleUnitTest only)
./gradlew test

# Instrumented tests (requires connected device — runs ExampleInstrumentedTest only)
./gradlew connectedAndroidTest
```

### Install Directly to Device

```bash
./gradlew installDebug
```

### Clean Build

```bash
./gradlew clean assembleDebug
```

### Known Build Notes

- The project uses `android.disallowKotlinSourceSets=false` in `gradle.properties` — this is intentional for KSP compatibility with the current AGP version.
- `org.jetbrains.kotlin.android` is **not applied anywhere**. AGP 9.x has built-in Kotlin support, so that plugin is obsolete and replaced by the dedicated `org.jetbrains.kotlin.plugin.compose` and `org.jetbrains.kotlin.plugin.serialization` plugins. The five plugins actually applied in `app/build.gradle.kts` are: `com.android.application`, `org.jetbrains.kotlin.plugin.compose`, `org.jetbrains.kotlin.plugin.serialization`, `com.google.devtools.ksp`, and `com.google.dagger.hilt.android`.
- Room schema files are exported to `app/schemas/` — include this directory in version control to track database migrations.

</details>

---

<details>
<summary><h2 style="display:inline">18. Future Improvements</h2></summary>

### High Priority
- **Cloud sync** — Back up and sync transactions across devices using Firebase Firestore or a REST API
- **Explicit Room migrations** — Replace `fallbackToDestructiveMigration()` with versioned migration scripts before production
- **Biometric lock** — Fingerprint / face unlock to protect sensitive financial data
- **Widgets** — Home screen balance and quick-add widgets via Glance API
- **CSV/Excel export** — Export transaction history for use in spreadsheets

### Medium Priority
- **Split bill support** — Divide a transaction among multiple people or wallets (the data model already supports parent/child transactions)
- **Debt tracking** — Track money lent or borrowed with reminders
- **Exchange rate integration** — Multi-currency wallets with live exchange rates
- **Transaction photo attachment** — Associate receipt photos with specific transactions (the `photoUri` field exists in the data model)
- **Advanced search** — Full-text search across notes, tags, merchant names
- **Tags and labels** — User-defined tags per transaction for cross-category grouping (the `tags` field exists in the data model)

### Low Priority / Polish
- **Animated charts** — Entry/exit animations for chart data updates
- **Accessibility** — Better content descriptions, larger touch targets, TalkBack testing
- **Tablet layout** — Two-pane adaptive layout for large screens
- **More languages** — Chinese, Japanese, Thai localization
- **Unit and integration tests** — Current test coverage is minimal; full ViewModel and repository test suites needed
- **Custom recurring frequencies** — "Every 3rd Friday" or "Twice a month" patterns

</details>

---

<details>
<summary><h2 style="display:inline">19. Challenges and Solutions</h2></summary>

### Challenge 1: Vietnamese Receipt OCR Accuracy

**Problem:** Vietnamese receipts use mixed number formats (dots as thousands, commas as decimal in some regions), contain a mix of Vietnamese and English text, and often have noisy OCR output with misrecognized characters.

**Solution:** A multi-pass `ReceiptParser` with prioritized keyword matching. Pass 1 targets unambiguous Vietnamese payment keywords. Pass 2 targets general total keywords. Pass 3 falls back to the largest numeric value on the receipt, applying exclusion rules for VAT percentages, phone numbers, and invoice codes. A dual-format number parser handles both Vietnamese (1.500.000) and Western (1,500.50) conventions. When confidence is low, the raw OCR text is sent to Gemini for a second parsing attempt.

### Challenge 2: AI Context Window Management

**Problem:** Sending the full transaction history to Gemini would exceed the context window and incur excessive costs.

**Solution:** Instead of sending raw transactions, the ViewModel builds a compact JSON summary at query time: top-5 spending categories, income/expense totals for current and last month, active budget percentages, and recurring monthly obligations. This JSON summary is injected as the system instruction rather than appended to the chat history, keeping the conversation thread clean.

### Challenge 3: Background Worker Reliability

**Problem:** Android's battery optimization and Doze mode aggressively kill background tasks. Recurring transaction workers that miss their schedule cause incorrect financial records.

**Solution:** WorkManager with `PeriodicWorkRequest` provides OS-level guarantee that work eventually runs, even after reboots. The worker includes a catch-up mechanism: it queries all due recurring transactions (not just those due today) up to a 365-day window, ensuring no transaction is ever missed even after extended offline periods.

### Challenge 4: Reactive UI with Room + Flow

**Problem:** Multiple screens depend on the same underlying data (e.g., the home screen balance reflects transactions added from any other screen). Ensuring consistency without manual refresh is error-prone.

**Solution:** Room's `@Query` methods returning `Flow<T>` create live database observations. Any insert, update, or delete operation automatically triggers a new emission on all active flows. Combined with `collectAsStateWithLifecycle()` in composables, the UI stays in sync with zero manual coordination.

### Challenge 5: Type-Safe Navigation with Complex Arguments

**Problem:** Traditional string-based navigation is fragile. Passing optional parameters (nullable Long, nullable Double) through route strings is messy and error-prone.

**Solution:** Navigation Compose 2.8.x with `@Serializable` data class routes. Each destination is a Kotlin data class with properly typed nullable fields. The Kotlin serialization plugin handles serialization/deserialization of route arguments automatically, catching type mismatches at compile time.

### Challenge 6: Runtime Language Switching

**Problem:** Android requires an `Activity` restart to change the app locale. Forcing a restart mid-session is jarring, and the standard `LocaleManager` API (API 33+) doesn't cover the minSdk 26 target.

**Solution:** `LocaleHelper` updates the `Configuration.locale` of the `Application` context before `setContent{}` in `MainActivity`. When the user changes language in Settings, the preference is saved to DataStore and `MainActivity` is recreated via `recreate()`, applying the new locale on the next lifecycle start.

</details>

---

<details>
<summary><h2 style="display:inline">20. Learning Outcomes</h2></summary>

This project was built as a university final project for the **Mobile Development** course at **UIT (University of Information Technology, Ho Chi Minh City)**. It covers the full Android development lifecycle from architecture design to production-quality UI.

### Technical Skills Acquired

**Android Architecture**
- Implemented Clean Architecture with strict layer separation in a real project, not just in theory
- Applied MVVM with `StateFlow`-based unidirectional data flow throughout 12 feature modules
- Used Hilt for constructor injection across the full stack (Activities, ViewModels, Workers, Repositories)

**Jetpack Compose**
- Built a complete production app entirely in Compose, including custom components, animations, charts, and the camera preview
- Managed complex UI state with `remember`, `rememberSaveable`, `derivedStateOf`, and `LaunchedEffect`
- Integrated Material 3 design tokens, dynamic color, and dark mode

**Data Layer**
- Designed a normalized relational schema with 7 tables and appropriate indexes
- Used Room type converters, database callbacks for seeding, and `Flow`-based reactive queries
- Persisted user preferences with DataStore and designed a comprehensive `UserPreferences` model

**AI Integration**
- Integrated a generative AI SDK into a real product (not a demo)
- Designed effective system prompts and JSON-mode prompts for structured output
- Built a context-injection pattern to give the model personalized financial data without exposing raw rows

**Background Processing**
- Used WorkManager for all four background tasks with proper Hilt injection via `HiltWorkerFactory`
- Implemented periodic and one-time work with deduplication policies
- Handled the daily reminder scheduling with calculated initial delay to target a specific time of day

**Localization**
- Fully localized a non-trivial app (243 strings) in Vietnamese without breaking any references
- Implemented runtime language switching with locale configuration override

**OCR and ML**
- Combined on-device ML (ML Kit) with cloud AI (Gemini) for a robust two-stage pipeline
- Wrote a multi-pass heuristic parser for real-world receipt data in two languages

### Soft Skills

- **Project scoping** — deciding which features to implement fully vs. stub out
- **Technical writing** — documenting architecture decisions and design rationale
- **Incremental delivery** — building features in vertical slices (data → domain → UI) rather than horizontal layers

</details>

---

<div align="center">

---

**ExpenseTracker** · UIT Final Project · Mobile Development

*Le Hoang Chien — lehoangchiena@gmail.com*

Built with ❤️ and Kotlin

</div>
