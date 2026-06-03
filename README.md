<div align="center">

# 💰 ExpenseTracker

**A production-grade personal finance Android application**
built with Jetpack Compose, Clean Architecture, Room, Hilt, Firebase Auth, Firestore, and Gemini AI.

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

**ExpenseTracker** is a fully-featured personal finance management application for Android. It helps users record income and expenses, manage multiple wallets and budgets, visualize spending patterns through rich analytics, and get AI-powered financial advice — all while working completely offline with optional cloud sync and an AI layer.

User accounts are required: the app uses **Firebase Authentication** (email/password) to identify each user, and all financial data (transactions, wallets, categories, budgets) is fully isolated per account. Each user's data is stored in Room (local SQLite) and mirrored to **Cloud Firestore** under `users/{uid}` so it can be restored when signing in on a new device. New users have default categories and a wallet seeded on first sign-in.

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

The application is entirely written in Kotlin with a modern Android tech stack. All core functionality — transaction recording, budget management, statistics, and recurring transactions — works **fully offline**. Authentication (sign-in, sign-up, password reset) requires an internet connection. AI chat, receipt parsing, and monthly insight generation are powered by **Google Gemini 2.5 Flash** and require an internet connection and a Gemini API key.

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

**Language barrier.** Vietnamese users often find financial apps in English confusing. ExpenseTracker defaults to Vietnamese on first launch, with full English localization also available and switchable at any time.

**Data security.** Each user account's financial data is fully isolated from all other users, so personal spending history is never shared across accounts on the same device.

</details>


<details>
<summary><h2 style="display:inline">3. Key Features</h2></summary>

### Authentication & Cloud Sync
- ✅ **Email/password sign-up and sign-in** — Firebase Authentication
- ✅ **Forgot password** — Firebase password reset email flow
- ✅ **Per-user data isolation** — every table is scoped by `userId`; switching accounts shows only that account's data
- ✅ **Write-through push** — every local write (transaction, category, wallet, budget, recurring) is mirrored to Firestore under `users/{uid}/{collection}` on a best-effort basis; if the device is offline the local write still succeeds and Firestore's offline SDK queues the push for when connectivity returns
- ✅ **Pull on sign-in** — when signing into an existing account (especially on a new device), the app fetches all five Firestore collections and upserts them into Room before showing the main app; data is restored automatically
- ✅ **New-user bootstrap** — on first sign-in, a cloud pull is attempted first; default categories (14) and a default wallet (Cash) are seeded only if Firestore has no data for that account (genuinely new user), and the seeded rows are immediately pushed to Firestore so a second device can pull them

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
- ✅ **Full Vietnamese localization** — all strings translated; Vietnamese is the default language on first launch
- ✅ **Animated skeleton loading** (shimmer effect) and Lottie animations
- ✅ **Animated amount transitions** when balances change
- ✅ **4 notification channels** — budget alerts, daily reminders, recurring transactions, AI insights
- ✅ **Background work** — WorkManager tasks survive app restarts and device reboots
- ✅ **Data backup and restore** — full JSON export/import including all wallets, categories, transactions, budgets, and recurring transactions
- ✅ **Splash screen** API integration
- ✅ **Edge-to-edge** display support
- ✅ **Onboarding flow** for first-time users

</details>


<details>
<summary><h2 style="display:inline">4. Application Screens</h2></summary>

### Authentication Screens (pre-login)

| Screen | Description |
|---|---|
| `LoginScreen` | Email + password fields with show/hide toggle, "Forgot password?" link, and a link to Register |
| `RegisterScreen` | Email, password, and confirm-password fields; validates password match and format |
| `ForgotPasswordScreen` | Email input with Firebase password reset email dispatch; shows confirmation on success |

These screens are shown before the user is authenticated. After sign-in or sign-up, the user is navigated to the main app automatically. The auth flow is managed by a separate `AuthNavHost` with its own back stack, so the Back button on Login exits the app rather than returning to a previous screen.

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
- **`SettingsScreen`** — Account section (signed-in email + Sign Out button), theme, language, currency, notifications, default wallet, Gemini API key override
- **`OnboardingScreen`** — Welcome slides introducing core features
- **`GeminiTestScreen`** — Developer tool to verify API key connectivity

### Quick Add Bottom Sheet (global, accessible from all screens)
- **Manual Tab** — Fast amount + note entry, category chip selector
- **AI Parse Tab** — Natural language input with voice recognition button, confidence score display, and parsed transaction preview

</details>


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
│      Includes: AuthRepository · AuthUser · AuthException        │
└───────────────────────────┬─────────────────────────────────────┘
                            │ implemented by
┌───────────────────────────▼─────────────────────────────────────┐
│                        DATA LAYER                               │
│  Room DB · DataStore · Firebase Auth · Firestore · Gemini · ML │
│  WorkManager · CurrentUserProvider · UserBootstrapService       │
│  FirestoreSyncService (push) · FirestoreCloudSyncService (pull) │
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
    │             │             │
    ▼             ▼             ▼
Room DAO    Firebase Auth   Gemini API / ML Kit
    │             │
    ▼             ▼ (auth state change)
Flow<Entity>  Flow<AuthUser?>
    │             │
    ▼ (mapper)    ▼
Flow<DomainModel> ─────────────────┐
    │                              │
    ▼                              ▼
ViewModel (updates StateFlow<UiState>)
    │
    ▼
Composable re-renders
```

### Authentication Gate

`MainActivity` observes `AuthRepository.authStateFlow()`. When `authUser == null` (not signed in), it renders `AuthNavHost` (Login → Register → ForgotPassword). When `authUser != null`, it renders `AppShell` (main app with bottom navigation). This switch is driven entirely by reactive state — no explicit navigation calls are needed on sign-in or sign-out.

### Per-User Data Scoping

Every DAO query that reads data accepts a `userId: String` parameter. `CurrentUserProvider` (a `@Singleton` bound to `AuthRepository.currentUser?.uid`) supplies the current user's UID to every repository. All inserts stamp `userId = currentUserProvider.uid` on the entity. When no user is signed in, `uid` is `""` and all queries return empty results.

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

### Cloud Sync Architecture

Room is the primary source of truth. Firestore is used for two distinct operations:

**Write-through push (`FirestoreSyncService`)** — every repository (`TransactionRepositoryImpl`, `CategoryRepositoryImpl`, etc.) injects `FirestoreSyncService` and calls the appropriate push method after every local DAO write. Push is fire-and-forget: Firestore's offline SDK queues writes locally when offline and delivers them automatically when connectivity is restored. Failures are logged but never surfaced to the user.

**Pull on sign-in (`FirestoreCloudSyncService`)** — `UserBootstrapService.bootstrapIfNeeded(uid)` is called from `AuthRepositoryImpl` on every `signIn`/`signUp` and from `MainActivity` via `LaunchedEffect` when an existing authenticated session is detected on app start. The pull fetches all five collections (wallets → categories → recurring → budgets → transactions, in FK-dependency order) and upserts each document into Room using `@Insert(onConflict = REPLACE)`. All steps are logged under the `FirestoreSync` tag. Exceptions are caught and logged with full stacktraces; the bootstrap always completes.

**DI cycle avoidance** — `FirestoreCloudSyncService` intentionally does not inject `CurrentUserProvider` (which would create the cycle `AuthRepositoryImpl → UserBootstrapService → FirestoreSyncService → CurrentUserProvider → AuthRepository → AuthRepositoryImpl`). Instead, the `uid` is passed explicitly as a parameter.

**Conflict handling** — last-write-wins based on `updatedAt` / `syncedAt` timestamps stored in each Firestore document. Full two-way conflict resolution is not implemented.

**Limitation** — real-time live sync between two devices used simultaneously is **not implemented**. Cross-device data appears after signing in or restarting on the second device. Real-time multi-device sync is listed as future work.

</details>


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
| Vico Charts | 2.0.0 | Bar, line, and custom charts |
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

### Authentication & Cloud
| Technology | Version | Purpose |
|---|---|---|
| Firebase BOM | 34.14.0 | Firebase version management |
| Firebase Auth | via BOM | Email/password authentication |
| Firebase Firestore | via BOM | Per-user cloud database: write-through push from repositories + pull on sign-in to restore data on a new device |
| Google Services Gradle Plugin | 4.4.4 | Processes `google-services.json` |
| kotlinx-coroutines-play-services | 1.9.0 | `await()` extension for Firebase Tasks |

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
│   │   │   │       └── LocaleHelper.kt        # Startup locale (defaults to "vi")
│   │   │   │
│   │   │   ├── data/                          # Data layer implementations
│   │   │   │   ├── bootstrap/
│   │   │   │   │   └── UserBootstrapService.kt  # Seeds default data per new user
│   │   │   │   ├── local/
│   │   │   │   │   ├── CurrentUserProvider.kt  # Supplies uid to all repositories
│   │   │   │   │   ├── dao/                   # 7 DAO interfaces (all userId-scoped)
│   │   │   │   │   ├── database/              # AppDatabase (version 3) + Seeder
│   │   │   │   │   ├── datastore/             # UserPreferencesDataStore
│   │   │   │   │   └── entity/                # 7 Room entities (all have userId)
│   │   │   │   ├── remote/gemini/
│   │   │   │   │   ├── GeminiApiService.kt    # Model config, request methods
│   │   │   │   │   ├── GeminiModels.kt        # Request/Response DTOs
│   │   │   │   │   └── GeminiPrompts.kt       # Prompt engineering
│   │   │   │   ├── mlkit/
│   │   │   │   │   ├── ReceiptOcrService.kt   # ML Kit text recognition
│   │   │   │   │   └── ReceiptParser.kt       # Multi-pass receipt parser
│   │   │   │   ├── mapper/                    # 7 Entity ↔ Domain mappers
│   │   │   │   ├── repository/                # 8 repository implementations
│   │   │   │   │   └── AuthRepositoryImpl.kt  # Firebase Auth + UserBootstrapService
│   │   │   │   ├── worker/                    # 4 WorkManager workers
│   │   │   │   ├── budget/
│   │   │   │   │   └── BudgetAlertChecker.kt
│   │   │   │   └── backup/
│   │   │   │       └── BackupService.kt       # JSON export/import
│   │   │   │
│   │   │   ├── domain/                        # Business rules (pure Kotlin)
│   │   │   │   ├── model/                     # 22 domain models and enums
│   │   │   │   │   ├── AuthUser.kt            # uid + email
│   │   │   │   │   └── AuthException.kt       # Sealed class: typed auth errors
│   │   │   │   └── repository/                # 8 repository interfaces
│   │   │   │       └── AuthRepository.kt      # signIn/signUp/signOut/resetPassword
│   │   │   │
│   │   │   ├── di/                            # Hilt modules
│   │   │   │   ├── AuthModule.kt              # FirebaseAuth + CurrentUserProvider
│   │   │   │   ├── DatabaseModule.kt          # Room + all DAOs
│   │   │   │   ├── DataStoreModule.kt         # DataStore singleton
│   │   │   │   ├── NetworkModule.kt           # Gemini API service
│   │   │   │   └── RepositoryModule.kt        # Interface → Impl bindings
│   │   │   │
│   │   │   ├── feature/                       # Feature modules
│   │   │   │   ├── auth/                      # Login · Register · Forgot Password
│   │   │   │   │   ├── screen/
│   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   ├── RegisterScreen.kt
│   │   │   │   │   │   └── ForgotPasswordScreen.kt
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       └── AuthViewModel.kt
│   │   │   │   ├── home/                      # Dashboard
│   │   │   │   ├── transaction/               # List + Add/Edit + Detail
│   │   │   │   ├── category/                  # List + Add/Edit
│   │   │   │   ├── budget/                    # List + Add/Edit
│   │   │   │   ├── wallet/                    # List + Add/Edit
│   │   │   │   ├── recurring/                 # List + Add/Edit
│   │   │   │   ├── statistics/                # Charts + Analysis
│   │   │   │   ├── ai_assistant/              # Chat + History
│   │   │   │   ├── ocr_scan/                  # Camera + OCR
│   │   │   │   ├── settings/                  # Preferences + Sign Out
│   │   │   │   ├── onboarding/                # First-run flow
│   │   │   │   └── shell/                     # AppShell + Quick Add
│   │   │   │
│   │   │   ├── navigation/
│   │   │   │   ├── AppNavHost.kt              # Main app navigation graph
│   │   │   │   ├── AuthNavHost.kt             # Auth flow (Login/Register/ForgotPw)
│   │   │   │   └── AppDestinations.kt         # Serializable route objects
│   │   │   │
│   │   │   ├── MainActivity.kt                # Auth gate: shows AuthNavHost or AppShell
│   │   │   └── ExpenseTrackerApp.kt
│   │   │
│   │   └── res/
│   │       ├── values/strings.xml             # ~300 English strings
│   │       ├── values-vi/strings.xml          # ~300 Vietnamese strings
│   │       ├── values/colors.xml
│   │       ├── values/themes.xml
│   │       └── drawable / mipmap              # Icons and assets
│   │
│   ├── google-services.json                   # Firebase project config (required)
│   ├── schemas/                               # Room migration JSON schemas
│   └── build.gradle.kts                       # Dependencies + build config
│
├── build.gradle.kts                           # Root build config
├── settings.gradle.kts                        # Module includes
├── gradle.properties                          # Kotlin/AGP flags
└── local.properties                           # GEMINI_API_KEY (not committed)
```

</details>


<details>
<summary><h2 style="display:inline">8. Offline and Online Features</h2></summary>

### Fully Offline Features
All core functionality works without internet access. Data is stored locally using Room SQLite and DataStore.

| Feature | Storage |
|---|---|
| Transaction CRUD | Room (`transactions` table, scoped by userId) |
| Category management | Room (`categories` table, scoped by userId) |
| Budget tracking | Room (`budgets` table, scoped by userId) |
| Wallet management | Room (`wallets` table, scoped by userId) |
| Recurring transaction scheduling | Room + WorkManager |
| Statistics and charts | Computed from local Room queries |
| User preferences (theme, currency, language) | DataStore |
| Previously generated AI insights | Room (`ai_insights` table, scoped by userId) |
| Chat message history | Room (`ai_chat_messages` table, scoped by userId) |
| Backup export / import | Local file system (JSON) |

### Online Features (Require Internet)
| Feature | Service |
|---|---|
| Sign up / Sign in | Firebase Authentication |
| Password reset email | Firebase Authentication |
| Write-through push: mirror local writes to cloud | Firebase Firestore |
| Pull on sign-in: restore data on a new device | Firebase Firestore |
| AI chat with financial context | Gemini 2.5 Flash |
| Natural language transaction parsing | Gemini 2.5 Flash (JSON mode) |
| Transaction auto-categorization | Gemini 2.5 Flash (JSON mode) |
| OCR parsing fallback and enhancement | Gemini 2.5 Flash |
| Monthly spending insights generation | Gemini 2.5 Flash |

### Cloud Sync

The app uses an **offline-first** model: Room SQLite is always the primary source of truth and every feature works fully offline.

**Write-through push** — after each local DAO write in a repository (add, update, or delete), a corresponding call to `FirestoreSyncService` mirrors the change to Firestore under `users/{uid}/{collection}/{id}`. If the device is offline, Firestore's local SDK persistence queues the write and delivers it automatically when connectivity returns. Push failures are logged but never shown to the user.

**Pull on sign-in** — when signing into an existing account (especially on a new device with an empty local database), `UserBootstrapService` fetches all five Firestore collections before deciding whether to seed defaults. All documents are upserted into Room with `@Insert(onConflict = REPLACE)`. This means an existing account's full data — every transaction, category, wallet, budget, and recurring transaction — is restored automatically on the new device.

**What is NOT synced in real-time** — if the same account is open on two devices simultaneously, changes made on one device do not appear instantly on the other. Cross-device data becomes available the next time the user signs in or the app cold-starts on the second device. Real-time multi-device live sync (Firestore snapshot listeners keeping two live UIs in sync) is listed as future work.

**Security** — Firestore security rules restrict each user to reading and writing only their own `users/{uid}` subtree. No user can access another user's data.

### Graceful Degradation
- If `geminiEnabled = false` in settings, all AI features are disabled cleanly.
- If the API key is missing or invalid, error messages are shown in the AI chat; other screens remain unaffected.
- OCR falls back to pure ML Kit parsing if Gemini is unavailable.
- Previously generated AI insights remain accessible from the local database even when offline.
- All financial data (transactions, budgets, etc.) is readable and writable offline once the user is signed in; the auth token persists across restarts.

</details>


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
4. Messages are persisted to Room (`ai_chat_messages` table) with UUID-based session IDs, scoped to the current user's `userId`.
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
4. Saves them to the `ai_insights` Room table, scoped to the current user's `userId`.
5. Shows a notification linking to the AI tab.

**Insight types:** `MONTHLY_SUMMARY`, `ANOMALY`, `RECOMMENDATION`

### Feature 5: Receipt Parsing Enhancement

After ML Kit extracts raw text from a receipt image, Gemini's `buildReceiptParsePrompt()` is used as a fallback and enhancement step to extract structured fields from ambiguous OCR output.

</details>


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


<details>
<summary><h2 style="display:inline">11. Statistics and Analytics</h2></summary>

The `StatisticsScreen` and `StatisticsViewModel` provide comprehensive spending analytics.

### Analysis Periods
| Period | Description |
|---|---|
| This Week | Monday → Sunday of the current week |
| This Month | 1st of current month → today |
| Last Month | Previous calendar month |
| This Year | January 1st → December 31st of the current year |
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
- Color gradient from light (low spend) to dark red (high spend)
- Month heading and weekday initials are locale-aware (Vietnamese when Vi is active)

**Category Breakdown Table**
- Ranked list of categories with amount, percentage of total, and transaction count

### Data Aggregation
All statistics are computed from Room queries scoped to the current user's `userId`:
- `getTotalByTypeAndDateRange()` — aggregate income/expense for a period
- `observeByDateRange()` — per-transaction data for charting
- `getSumByCategoryAndDateRange()` — category-level breakdowns

</details>


<details>
<summary><h2 style="display:inline">12. Localization Support</h2></summary>

The app supports **English (en)** and **Vietnamese (vi)** with complete parity — every user-visible string has a translation in both languages.

### Default Language

**Vietnamese is the default language on first launch**, regardless of the device's system locale. This applies immediately to the Login, Register, and Forgot Password screens — the very first screens a new user sees. The choice persists in `SharedPreferences` via `LocaleHelper` and survives restarts.

### String Resource Statistics
| File | Language | String Count |
|---|---|---|
| `res/values/strings.xml` | English | ~300 |
| `res/values-vi/strings.xml` | Vietnamese | ~300 |

### String Categories Covered
- Navigation labels and tab names
- All screen titles and section headers
- Authentication screens: sign-in, sign-up, forgot password labels and error messages
- Button labels and action strings (Save, Cancel, Delete, Edit, etc.)
- Transaction types (Income / Chi tiêu, Expense / Chi tiêu, Transfer / Chuyển khoản)
- Categories, budget periods, filter labels
- Error messages and validation feedback (including Firebase Auth errors: wrong password, user not found, network error, etc.)
- Notification titles and body text
- Onboarding slide content
- AI assistant prompts and suggestion chips
- Settings labels and descriptions (including Account / Tài khoản, Sign Out / Đăng xuất)
- Receipt scanner status messages
- Chart and statistics labels (period chips, empty states)
- Relative date labels (Today / Hôm nay, Yesterday / Hôm qua)

### Runtime Language Switching

The `LocaleHelper` utility stores the selected language in `SharedPreferences` (key `locale_prefs`) and applies it in `attachBaseContext()` before the Activity is inflated — this means the correct locale is applied on the very first frame, including the Login screen.

- On **first launch**, the `SharedPreferences` key is absent. `LocaleHelper.getLanguage()` returns `"vi"` as the default, so Vietnamese is applied immediately.
- When the user selects English in Settings, `LocaleHelper.setLanguage("en")` writes `"en"` to `SharedPreferences` and `Activity.recreate()` applies the change. That value is read on all subsequent launches.
- The selected language is also mirrored to `UserPreferencesDataStore` so it can be observed reactively by the Settings screen.

### Currency Formatting

`CurrencyFormatter` handles locale-aware currency display:
- Default currency: **VND** (Vietnamese Dong)
- Formats according to the selected currency code stored in preferences
- The AI system prompt always includes the user's currency for consistent AI responses

</details>


<details>
<summary><h2 style="display:inline">13. Database Design</h2></summary>

Room database version **3** with JSON schema export enabled. The database uses `fallbackToDestructiveMigration(dropAllTables = true)` — appropriate for a student project where intentional fresh-start semantics align with the per-user data model.

> **Version history:** v1 → initial schema; v2 → minor additions; v3 → added `userId TEXT NOT NULL DEFAULT ''` column to all seven user-owned tables, with a corresponding index on each, to support per-user data isolation.

### Per-User Data Isolation

Every user-owned table has a `userId TEXT NOT NULL DEFAULT ''` column. All DAO read queries are filtered by `WHERE userId = :userId`. All insert/update operations stamp the current user's Firebase UID via `CurrentUserProvider`. When no user is signed in, the UID is `""` and all queries return empty results — no data leaks between accounts.

### New-User Bootstrap and Cloud Restore

`UserBootstrapService.bootstrapIfNeeded(uid)` is called on every sign-in and sign-up, and also when the app starts with an existing authenticated session. It follows this order:

1. **Fast return** — if `categoryDao.countByUserId(uid) > 0`, local data already exists (normal restart on the same device); returns immediately.
2. **Cloud pull** — if local is empty, `FirestoreCloudSyncService.syncFromCloud(uid)` fetches all five Firestore collections and upserts them into Room.
3. **Existing account** — if categories are now present after the pull, the user's cloud data has been restored; seeding is skipped.
4. **New account** — if categories are still absent (cloud had nothing), the following defaults are seeded and immediately pushed to Firestore so a future second device can pull them rather than re-seeding:
   - **14 default categories** (10 expense: Ăn uống, Di chuyển, Mua sắm, …; 4 income: Lương, Thưởng, Đầu tư, Thu nhập khác)
   - **1 default wallet**: "Cash" (💵, #26A69A, 0 VND)

A `Mutex` prevents concurrent runs of `bootstrapIfNeeded` (e.g., the sign-in coroutine and the `MainActivity` `LaunchedEffect` both firing when auth state changes) — whichever runs second finds `count > 0` and exits immediately.

### Entity Relationship Diagram

```
┌──────────────────┐       ┌──────────────────────┐       ┌────────────────┐
│     wallets      │       │     transactions     │       │   categories   │
├──────────────────┤       ├──────────────────────┤       ├────────────────┤
│ id (PK)          │──────▶│ walletId (FK)        │◀──────│ id (PK)        │
│ userId           │       │ id (PK)              │       │ userId         │
│ name             │       │ userId               │       │ name           │
│ icon             │       │ categoryId (FK)      │       │ icon           │
│ color            │       │ amount               │       │ color          │
│ initialBal       │       │ type (ENUM)          │       │ type (ENUM)    │
│ currency         │       │ note                 │       │ isDefault      │
│ createdAt        │       │ date                 │       │ isArchived     │
└──────────────────┘       │ photoUri             │       └────────────────┘
                           │ location             │
┌──────────────────┐       │ recurringId (FK)     │       ┌──────────────────┐
│     budgets      │       │ parentSplitId (FK)   │       │   recurring_     │
├──────────────────┤       │ tags                 │       │  transactions    │
│ id (PK)          │       │ toWalletId (FK)      │       ├──────────────────┤
│ userId           │       │ createdAt            │  ┌───▶│ id (PK)          │
│ categoryId (FK)  │       │ updatedAt            │  │    │ userId           │
│ amount           │       └──────────────────────┘  │    │ walletId (FK)    │
│ period (ENUM)    │                                  │    │ categoryId (FK)  │
│ startDate        │                                  │    │ amount           │
│ endDate          │                                  │    │ type (ENUM)      │
│ alertThresh      │                                  │    │ frequency        │
│ isActive         │                                  │    │ interval         │
└──────────────────┘                                  │    │ startDate        │
                                                      │    │ endDate          │
┌──────────────────┐   ┌──────────────────────┐       │    │ nextOccur.       │
│   ai_insights    │   │   ai_chat_messages   │       │    │ lastProc.        │
├──────────────────┤   ├──────────────────────┤       │    │ isActive         │
│ id (PK)          │   │ id (PK)              │       │    └──────────────────┘
│ userId           │   │ userId               │       │
│ type (ENUM)      │   │ role (ENUM)          │       │
│ title            │   │ content              │       │
│ content          │   │ timestamp            │       │
│ periodKey        │   │ sessionId            │       │
│ generatedAt      │   └──────────────────────┘       │
│ dismissed        │                                  │
└──────────────────┘   recurring_transactions.id ─────┘
```

### TypeConverters
Room uses custom `TypeConverters` for:
- `LocalDate` ↔ `String` (ISO-8601)
- `LocalDateTime` ↔ `String` (ISO-8601)
- `TransactionType`, `BudgetPeriod`, `RecurrenceFrequency`, `ChatRole`, `InsightType`, `ThemeMode` ↔ `String` (name-based)

### Indexes
```sql
-- All tables
INDEX(userId)                                -- per-user filtering on every table

-- transactions
INDEX(walletId), INDEX(categoryId), INDEX(date),
INDEX(recurringId), INDEX(parentSplitId)

-- budgets
INDEX(categoryId)

-- recurring_transactions
INDEX(walletId), INDEX(categoryId)
```

</details>


<details>
<summary><h2 style="display:inline">14. Design Patterns and Architecture</h2></summary>

### MVVM (Model-View-ViewModel)
Every screen has a corresponding ViewModel that:
- Holds a single `UiState` data class as `MutableStateFlow`
- Exposes it as `StateFlow` to the composable
- Handles all business logic and side effects
- The composable only renders state and fires events

### Clean Architecture (3-Layer)
- **Domain layer:** Pure Kotlin interfaces and models — zero Android imports. Includes `AuthRepository`, `AuthUser`, and the `AuthException` sealed class.
- **Data layer:** Room, DataStore, Firebase Auth, Gemini, ML Kit, WorkManager implementations.
- **Presentation layer:** Composables and ViewModels — depends only on domain interfaces.

### Repository Pattern
Eight repository interfaces decouple the presentation and domain layers from data sources:
`TransactionRepository`, `WalletRepository`, `CategoryRepository`, `BudgetRepository`, `RecurringTransactionRepository`, `AiRepository`, `PreferencesRepository`, `AuthRepository`.

### Authentication and Auth Gate Pattern
`AuthRepository` (interface) / `AuthRepositoryImpl` (Firebase Auth) is injected into `AuthViewModel` for the auth screens and into `MainActivity` for the auth gate. The gate is purely reactive: `MainActivity` collects `authRepository.authStateFlow()` as a `State<AuthUser?>`, and the Compose tree conditionally renders either `AuthNavHost` or `AppShell` with no imperative navigation calls. Signing out triggers `FirebaseAuth.signOut()`, which emits `null` on the auth state flow, which causes `MainActivity` to recompose and display the Login screen automatically.

### Per-User Data Scoping
`CurrentUserProvider` (a `@Singleton`) reads `AuthRepository.currentUser?.uid`, returning `""` when not signed in. All eight data repositories inject this provider. Every DAO SELECT query is filtered by `userId = :userId`; every INSERT/UPDATE stamps `userId = currentUserProvider.uid`. This pattern ensures data isolation without requiring any UI-layer changes.

### Error Typing (Auth)
`AuthException` is a sealed Kotlin class (pure domain model, no Android dependency) with subclasses for each Firebase error category (`WeakPassword`, `EmailAlreadyInUse`, `WrongPassword`, `InvalidEmail`, `UserNotFound`, `NetworkError`, etc.). `AuthRepositoryImpl` maps Firebase exceptions to these typed errors. `AuthViewModel` injects `@ApplicationContext` and maps each subtype to a localized string via `context.getString(R.string.auth_error_*)`. This ensures error messages are always in the user's selected language, regardless of what Firebase returns.

### Dependency Injection (Hilt)
All dependencies are injected via constructor injection:
- `@HiltAndroidApp` on `Application`
- `@AndroidEntryPoint` on `Activity`
- `@HiltViewModel` on all ViewModels
- `@Singleton` for database, DataStore, Firebase Auth, and API service instances
- `@Binds` for interface-to-implementation wiring (including `AuthRepository → AuthRepositoryImpl` and `CurrentUserProvider → CurrentUserProviderImpl`)

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

// Auth routes
@Serializable data object Login
@Serializable data object Register
@Serializable data class ForgotPassword(val prefillEmail: String = "")
```

</details>


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
| `org.jetbrains.kotlinx:kotlinx-coroutines-play-services` | 1.9.0 | Firebase `await()` extension |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.7.3 | JSON serialization |
| **`com.google.firebase:firebase-bom`** | **34.14.0** | **Firebase version management** |
| **`com.google.firebase:firebase-auth`** | **via BOM** | **Email/password authentication** |
| **`com.google.firebase:firebase-firestore`** | **via BOM** | **Per-user cloud database: write-through push + pull on sign-in** |
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

### Gradle Plugins Applied

| Plugin | Version | Where |
|---|---|---|
| `com.android.application` | 9.1.1 (AGP) | `app/build.gradle.kts` |
| `org.jetbrains.kotlin.plugin.compose` | 2.2.10 | `app/build.gradle.kts` |
| `org.jetbrains.kotlin.plugin.serialization` | 2.2.10 | `app/build.gradle.kts` |
| `com.google.devtools.ksp` | 2.2.10-2.0.2 | `app/build.gradle.kts` |
| `com.google.dagger.hilt.android` | 2.59.2 | `app/build.gradle.kts` |
| **`com.google.gms.google-services`** | **4.4.4** | **`app/build.gradle.kts`** |

</details>


<details>
<summary><h2 style="display:inline">16. Installation Guide</h2></summary>

### Prerequisites

- **Android Studio** Ladybug (2024.x) or newer
- **JDK 17** or newer — AGP 9.x and Gradle 9.x require JDK 17 to run builds. The `compileOptions { sourceCompatibility = JavaVersion.VERSION_11 }` in `app/build.gradle.kts` sets the bytecode *target*, not the toolchain version.
- **Android device or emulator** running Android 8.0 (API 26) or higher
- **Gemini API key** (free tier available at [Google AI Studio](https://aistudio.google.com)) — required only for AI features
- **Firebase project with `google-services.json`** — required for authentication; see Step 2b below

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd ExpenseTracker
```

### Step 2a: Configure the Gemini API Key

Create or edit `local.properties` in the project root:

```properties
sdk.dir=/path/to/your/Android/Sdk
GEMINI_API_KEY=your_gemini_api_key_here
```

> The `GEMINI_API_KEY` is injected at compile time via `BuildConfig`. Without it, all AI features will be disabled. The rest of the app works without a key.

### Step 2b: Configure Firebase

The app requires a valid `google-services.json` to compile. The file is not committed to the repository. To obtain one:

1. Go to the [Firebase Console](https://console.firebase.google.com) and create a project (or use an existing one).
2. Add an Android app with package name `com.expensetracker.app`.
3. Download the generated `google-services.json` file.
4. Place it at `app/google-services.json` (alongside `app/build.gradle.kts`).
5. In the Firebase Console, enable **Authentication → Sign-in method → Email/Password**.

> Without `google-services.json`, the Google Services Gradle plugin will fail at build time. No SHA-1 fingerprint or Google Sign-In setup is required.

### Step 2c: Enable Firestore and Configure Security Rules

Cloud sync requires Cloud Firestore to be enabled in your Firebase project:

1. In the Firebase Console, go to **Build → Firestore Database**.
2. Click **Create database**, select **Native mode**, and choose a region.
3. Once provisioned, navigate to the **Rules** tab and replace the default rules with:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null
                        && request.auth.uid == userId;
    }
  }
}
```

4. Click **Publish**.

These rules ensure each signed-in user can only read and write their own `users/{uid}` subtree; no user can access another user's data.

> Without Firestore enabled, the app still works fully offline. Push calls will fail silently (logged as errors), and the pull on sign-in will complete immediately with no data — local seeding of defaults will run as normal for new accounts.

### Step 3: Open in Android Studio

1. Launch Android Studio.
2. Select **File → Open** and navigate to the `ExpenseTracker` folder.
3. Wait for Gradle sync to complete (first sync downloads all dependencies).

### Step 4: Run

1. Connect an Android device or start an emulator (API 26+).
2. Click **Run ▶** or use `Shift+F10`.

</details>


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
- `org.jetbrains.kotlin.android` is **not applied anywhere**. AGP 9.x has built-in Kotlin support, so that plugin is obsolete and replaced by the dedicated `org.jetbrains.kotlin.plugin.compose` and `org.jetbrains.kotlin.plugin.serialization` plugins. The six plugins actually applied in `app/build.gradle.kts` are: `com.android.application`, `org.jetbrains.kotlin.plugin.compose`, `org.jetbrains.kotlin.plugin.serialization`, `com.google.devtools.ksp`, `com.google.dagger.hilt.android`, and `com.google.gms.google-services`.
- Room schema files are exported to `app/schemas/` — include this directory in version control to track database migrations.
- A valid `google-services.json` must be present at `app/google-services.json` before building; the Google Services plugin will fail at configuration time if it is missing.

</details>


<details>
<summary><h2 style="display:inline">18. Future Improvements</h2></summary>

### High Priority
- **Real-time multi-device live sync** — The current Firestore integration uses write-through push and pull-on-sign-in; changes on one device do not appear on another device in real time. Implementing Firestore snapshot listeners to keep a live open session in sync across two devices simultaneously is the natural next step.
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
- **Unit and integration tests** — Current test coverage is minimal; full ViewModel, repository, and auth flow test suites needed
- **Custom recurring frequencies** — "Every 3rd Friday" or "Twice a month" patterns
- **Google Sign-In** — Social login option alongside email/password

</details>


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

### Challenge 6: Runtime Language Switching and First-Launch Default

**Problem:** Android requires an `Activity` restart to change the app locale. The standard `LocaleManager` API (API 33+) doesn't cover the minSdk 26 target. Additionally, the app must display Vietnamese on first launch even before the user has set any preference.

**Solution:** `LocaleHelper` stores the selected language in `SharedPreferences` (not in Firebase/DataStore, so it's always available synchronously). `LocaleHelper.getLanguage()` defaults to `"vi"` when no stored value exists, so the very first call in `attachBaseContext()` applies Vietnamese before any UI is inflated. When the user selects English in Settings, `LocaleHelper.setLanguage("en")` writes to `SharedPreferences` and `Activity.recreate()` re-applies the locale. The value is also mirrored to `UserPreferencesDataStore` for reactive observation in Settings.

### Challenge 7: Per-User Data Isolation Without Foreign Keys to Firebase

**Problem:** Room is a local SQLite database with no awareness of Firebase user identities. Enforcing per-user data isolation at the database level without a cloud foreign key mechanism requires careful design.

**Solution:** Every user-owned entity stores a `userId: String` column set to the Firebase UID at insert time. `CurrentUserProvider` (a `@Singleton` backed by `AuthRepository.currentUser?.uid`) is injected into all repositories. DAOs filter every SELECT query by `WHERE userId = :userId`. When the user signs out, `currentUser` returns `null`, the UID falls back to `""`, and all DAO queries return empty results — data belonging to one account is never visible to another. On sign-in as a different account, the UID changes and the new user's data (or freshly seeded defaults) is shown immediately, without clearing the previous user's rows from the database.

### Challenge 8: Localized Firebase Error Messages

**Problem:** Firebase Authentication throws exceptions with English error messages (and internal error codes like `"ERROR_USER_NOT_FOUND"`). Surfacing these raw to the UI breaks the Vietnamese user experience.

**Solution:** `AuthRepositoryImpl` catches all Firebase exceptions and maps them to subclasses of the `AuthException` sealed class (`WeakPassword`, `EmailAlreadyInUse`, `WrongPassword`, `UserNotFound`, `NetworkError`, etc.) — pure Kotlin with no string content. `AuthViewModel` injects `@ApplicationContext` and maps each `AuthException` subtype to a string resource ID, calling `context.getString(R.string.auth_error_*)`. This keeps the data layer free of UI concerns while ensuring all error messages are fully translated into the active language.

</details>


<details>
<summary><h2 style="display:inline">20. Learning Outcomes</h2></summary>

This project was built as a university final project for the **Mobile Development** course at **UIT (University of Information Technology, Ho Chi Minh City)**. It covers the full Android development lifecycle from architecture design to production-quality UI.

### Technical Skills Acquired

**Android Architecture**
- Implemented Clean Architecture with strict layer separation in a real project, not just in theory
- Applied MVVM with `StateFlow`-based unidirectional data flow throughout 13 feature modules
- Used Hilt for constructor injection across the full stack (Activities, ViewModels, Workers, Repositories)

**Firebase and Authentication**
- Integrated Firebase Authentication (email/password, password reset) into a Clean Architecture app
- Designed a reactive auth gate at the `MainActivity` level using `callbackFlow` and `AuthStateListener`
- Mapped Firebase-specific exception types to domain-level sealed classes to keep the data layer decoupled from the UI
- Implemented per-user data isolation in a local SQLite database using a `userId` column and a `CurrentUserProvider` singleton
- Built a write-through Firestore sync layer: every repository mirrors local DAO writes to Firestore as fire-and-forget push operations, fully transparent to the UI
- Implemented a pull-on-sign-in restore flow with reverse Firestore-to-Room mappers for all five entity types, with per-field null safety and full exception logging
- Diagnosed and solved a Hilt DI cycle (`AuthRepositoryImpl → UserBootstrapService → FirestoreSyncService → CurrentUserProvider → AuthRepository`) by separating push and pull responsibilities into distinct classes with different dependency graphs
- Designed a bootstrap/restore protocol that correctly distinguishes a new account (seed defaults) from an existing account on a new device (pull from cloud) using a pull-first, check-after pattern with a coroutine `Mutex` to prevent concurrent bootstrap races

**Jetpack Compose**
- Built a complete production app entirely in Compose, including custom components, animations, charts, and the camera preview
- Managed complex UI state with `remember`, `rememberSaveable`, `derivedStateOf`, and `LaunchedEffect`
- Integrated Material 3 design tokens, dynamic color, and dark mode

**Data Layer**
- Designed a normalized relational schema with 7 tables, userId-based isolation, and appropriate indexes
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
- Fully localized a non-trivial app (~300 strings) in Vietnamese without breaking any references
- Implemented runtime language switching with locale configuration override applied at `attachBaseContext()` before any UI is inflated
- Set Vietnamese as the default language for first-launch (including pre-login screens) by controlling the `SharedPreferences` fallback

**OCR and ML**
- Combined on-device ML (ML Kit) with cloud AI (Gemini) for a robust two-stage pipeline
- Wrote a multi-pass heuristic parser for real-world receipt data in two languages

### Soft Skills

- **Project scoping** — deciding which features to implement fully vs. stub out
- **Technical writing** — documenting architecture decisions and design rationale
- **Incremental delivery** — building features in vertical slices (data → domain → UI) rather than horizontal layers

</details>


<div align="center">


**ExpenseTracker** · UIT Final Project · Mobile Development

*Le Hoang Chien — lehoangchien32005@gmail.com*

Built with ❤️ and Kotlin

</div>
