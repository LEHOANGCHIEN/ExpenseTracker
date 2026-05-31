# 📱 Expense Tracker — Android Development Task Prompts

> **A complete, AI-powered expense tracking app built with Kotlin, Jetpack Compose, Room, and Hilt.**
> This document contains every prompt needed to build the entire application from scratch.
> Feed each task sequentially to your Claude Code agent.

---

## 📑 Table of Contents

1. [How to Use This Document](#how-to-use-this-document)
2. [Project Overview](#project-overview)
3. [Tech Stack](#tech-stack)
4. [Architecture & Project Structure](#architecture--project-structure)
5. [Database Schema](#database-schema)
6. [Design System](#design-system)
7. [AI Strategy](#ai-strategy)
8. [Task List](#task-list)
   - Phase 1: Foundation (Tasks 1–3)
   - Phase 2: Data Layer (Tasks 4–5)
   - Phase 3: Core Features (Tasks 6–11)
   - Phase 4: Analytics & Visualization (Task 12)
   - Phase 5: AI Features (Tasks 13–16)
   - Phase 6: Polish & Production (Tasks 17–19)

---

## How to Use This Document

1. **Open Android Studio.** Create a new empty Compose project named `ExpenseTracker` with package `com.expensetracker.app`, minSdk 26, targetSdk 35.
2. **Open Claude Code** in the project root.
3. **Copy and paste tasks one at a time** in order. Wait for each to finish and verify the build succeeds before moving on.
4. **Before each task**, paste this preamble so the agent has full context:

   > "You are implementing an Android app called Expense Tracker. The full project plan is in `EXPENSE_TRACKER_TASKS.md` at the repo root. Read it first if you haven't already. Now execute the task below. Keep code idiomatic Kotlin, follow MVVM + Clean Architecture, use Hilt for DI, Jetpack Compose for all UI, and Material 3. Always run `./gradlew assembleDebug` after finishing to verify the build."

5. **Place this file** at `<project-root>/EXPENSE_TRACKER_TASKS.md` so the agent can reference it.

---

## Project Overview

**App Name:** Expense Tracker (working title — feel free to rename to "Wally", "Coinly", or similar)

**Purpose:** A modern, beautiful, AI-enhanced personal finance app that helps users track expenses, manage budgets, and gain insights into their spending habits.

**Target Users:** Students, young professionals, anyone wanting to manage personal finances on Android.

**Core Pillars:**
- 💰 **Effortless tracking** — Add transactions in seconds via quick-add, voice, receipt scanning, or natural language.
- 📊 **Insightful visualization** — Beautiful charts and dashboards that make spending patterns obvious.
- 🤖 **AI-powered intelligence** — Gemini and ML Kit do the heavy lifting: auto-categorization, OCR, anomaly detection, and a personal financial advisor chatbot.
- 🎨 **Delightful UI/UX** — Material 3 Expressive, dynamic colors, smooth animations, dark mode.

**Inspiration:** Money Lover, Wallet by BudgetBakers, Spendee, Monefy. Take the best of each and execute with modern Compose + AI.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0+ |
| UI | Jetpack Compose (BOM latest stable), Material 3 |
| Architecture | MVVM + Clean Architecture (Use Cases optional for complex flows) |
| DI | Hilt |
| Local DB | Room |
| Async | Kotlin Coroutines + Flow |
| Navigation | Navigation Compose (type-safe with `kotlinx.serialization`) |
| Preferences | DataStore (Preferences) |
| Charts | Vico (`com.patrykandpatrick.vico:compose-m3`) |
| AI — Cloud | Google Gemini API (`com.google.ai.client.generativeai:generativeai`) |
| AI — On-device | ML Kit Text Recognition (`com.google.mlkit:text-recognition`) |
| Image loading | Coil 3 |
| Date/Time | `java.time` (LocalDate, LocalDateTime) |
| Notifications | WorkManager + Notification API |
| Camera | CameraX |
| Animation | Compose animations, Lottie (`com.airbnb.android:lottie-compose`) |
| Build | Gradle with version catalog (`libs.versions.toml`) |

**Minimum SDK:** 26 (Android 8.0)
**Target SDK:** 35 (Android 15)
**Compile SDK:** 35

---

## Architecture & Project Structure

```
com.expensetracker.app/
├── ExpenseTrackerApp.kt              # @HiltAndroidApp Application class
├── MainActivity.kt                   # Single activity, Compose host
├── core/
│   ├── designsystem/
│   │   ├── theme/                    # Theme.kt, Color.kt, Typography.kt, Shapes.kt
│   │   ├── component/                # Reusable Composables (AppCard, AppButton, etc.)
│   │   └── icon/                     # Custom icon set
│   ├── util/                         # CurrencyFormatter, DateUtils, Extensions
│   └── common/                       # Result wrapper, error handling
├── data/
│   ├── local/
│   │   ├── entity/                   # Room @Entity classes
│   │   ├── dao/                      # Room @Dao interfaces
│   │   ├── database/                 # AppDatabase, Migrations, Converters
│   │   └── datastore/                # UserPreferencesDataStore
│   ├── remote/
│   │   └── gemini/                   # GeminiApiService wrapper
│   ├── mlkit/                        # ML Kit wrappers (OCR)
│   ├── repository/                   # Repository implementations
│   └── mapper/                       # Entity <-> Domain model mappers
├── domain/
│   ├── model/                        # Pure Kotlin domain models
│   └── repository/                   # Repository interfaces
├── feature/
│   ├── home/                         # Dashboard
│   ├── transaction/                  # Add/Edit/List/Detail
│   ├── category/                     # Category management
│   ├── budget/                       # Budget management
│   ├── recurring/                    # Recurring transactions
│   ├── statistics/                   # Charts & analytics
│   ├── ai_assistant/                 # Chatbot screen
│   ├── ocr_scan/                     # Receipt scanner
│   ├── settings/                     # Settings, backup, theme
│   └── onboarding/                   # First-launch flow
├── navigation/                       # NavHost, routes, type-safe destinations
└── di/                               # Hilt modules
```

**Conventions:**
- Each feature folder contains: `screen/` (Composables), `viewmodel/`, optionally `state/` and `event/`.
- Use `sealed interface UiState` and `sealed interface UiEvent` per screen for clean state management.
- ViewModels expose `StateFlow<UiState>` and accept `UiEvent` via `onEvent(event)`.
- Repositories return `Flow<T>` for reactive queries, `suspend fun` for one-shot operations.

---

## Database Schema

### Entities

**Wallet** (multi-account support)
```
id (PK), name, icon, color, initialBalance, currency, createdAt
```

**Category**
```
id (PK), name, icon (emoji or icon name), color, type (INCOME/EXPENSE), isDefault, isArchived
```

**Transaction**
```
id (PK), walletId (FK), categoryId (FK), amount, type (INCOME/EXPENSE/TRANSFER),
note, date, createdAt, updatedAt, photoUri (nullable), location (nullable),
recurringId (FK nullable), parentSplitId (FK nullable, self-reference), tags (List<String>)
```

**Budget**
```
id (PK), categoryId (FK nullable - null means total budget), amount, period (WEEKLY/MONTHLY/YEARLY),
startDate, endDate (nullable for ongoing), alertThreshold (e.g., 0.8 = 80%), isActive
```

**RecurringTransaction**
```
id (PK), walletId (FK), categoryId (FK), amount, type, note,
frequency (DAILY/WEEKLY/MONTHLY/YEARLY), interval (every N), startDate, endDate (nullable),
nextOccurrence, lastProcessed, isActive
```

**AiChatMessage**
```
id (PK), role (USER/ASSISTANT), content, timestamp, sessionId
```

**AiInsight** (cached insights)
```
id (PK), type (MONTHLY_SUMMARY/ANOMALY/RECOMMENDATION), content, periodKey, generatedAt, dismissed
```

### Relationships
- Wallet 1:N Transaction
- Category 1:N Transaction, Category 1:N Budget, Category 1:N RecurringTransaction
- Transaction 1:N Transaction (parent-children for splits)
- RecurringTransaction 1:N Transaction (generated transactions)

### Type Converters
- `LocalDate` ↔ `String` (ISO format)
- `LocalDateTime` ↔ `String` (ISO format)
- `List<String>` ↔ `String` (JSON)
- Enums ↔ `String`

---

## Design System

### Color Palette (Material 3 Expressive)

**Light Theme — Primary brand: Emerald-Teal**
- Primary: `#00897B` (Teal 600)
- Secondary: `#FFA726` (Orange 400) — for income/positive accents
- Tertiary: `#7E57C2` (Deep Purple 400) — for AI features
- Error: `#E53935`
- Surface tones following Material 3

**Category Colors (curated palette of 16):**
`#EF5350, #EC407A, #AB47BC, #7E57C2, #5C6BC0, #42A5F5, #29B6F6, #26C6DA, #26A69A, #66BB6A, #9CCC65, #D4E157, #FFEE58, #FFCA28, #FFA726, #FF7043`

**Income vs Expense:**
- Income green: `#43A047`
- Expense red: `#E53935`

### Typography
Use Material 3 type scale with `Plus Jakarta Sans` or `Inter` (fallback to system).
- Display Large: 57sp / 64sp line / -0.25
- Headline Medium: 28sp / 36sp
- Title Large: 22sp / 28sp
- Body Large: 16sp / 24sp
- Label Medium: 12sp / 16sp / 0.5

### Spacing & Shape
- Spacing scale: 4, 8, 12, 16, 20, 24, 32, 48, 64
- Corner radius: small=8dp, medium=16dp, large=24dp, extraLarge=28dp
- Card elevation: 0–2dp (prefer surface tonal elevation over shadows)

### Motion
- Standard duration: 300ms
- Emphasized: 500ms
- Use `MaterialTheme.motionScheme` if available
- Bottom sheet for quick add — slides up with spring animation
- Shared element transitions for transaction detail navigation
- Subtle shimmer/skeleton loaders

### Component Patterns
- **AppCard** — surface container with 16dp radius, 16dp padding
- **AppButton** — primary, secondary, text variants
- **AppTextField** — outlined with leading icon
- **CategoryChip** — circular colored background with emoji/icon
- **AmountText** — large, with currency, colored by income/expense
- **EmptyState** — Lottie animation + message + CTA

---

## AI Strategy

### Gemini API (Cloud)
Used for tasks requiring natural language understanding and generation.

**Use cases:**
1. **AI Financial Advisor Chatbot** — User asks questions like "How can I save more?" or "Why did I spend so much on food last month?" Gemini answers with context from the user's actual data (passed as part of the system prompt).
2. **Natural Language Transaction Input** — User types "Lunch with mom 250k yesterday" → Gemini parses into structured Transaction (amount, category, date, note).
3. **Smart Auto-Categorization** — When the user adds a transaction with just a note, Gemini suggests the best category.
4. **Monthly Insights** — Generate a personalized summary at month-end ("You spent 30% more on dining than last month — mostly on weekends").
5. **Anomaly Detection** — Flag unusually large transactions or category overspends with explanations.
6. **Budget Recommendations** — Given the last 3 months of data, suggest realistic budget targets.

**Implementation:**
- Library: `com.google.ai.client.generativeai:generativeai:0.9.0`
- Model: `gemini-1.5-flash` (fast, cheap, generous free tier) — fall back to `gemini-2.0-flash` if available.
- API key in `local.properties` as `GEMINI_API_KEY=...`, exposed via `BuildConfig`.
- Wrap calls in a `GeminiApiService` class with retry + timeout.
- For structured output (parsing transactions, generating insights), use `generationConfig { responseMimeType = "application/json" }` and JSON schema.

### ML Kit (On-device)
Used for tasks that should work offline / be free.

**Use cases:**
1. **Receipt OCR Scanning** — User takes a photo of a receipt; ML Kit extracts text; a regex+heuristic parser extracts merchant, total, and date; pre-fills the Add Transaction form.

**Implementation:**
- `com.google.mlkit:text-recognition:16.0.1`
- CameraX for capture
- Heuristic parser: find the largest currency-like number for total; lines with "TOTAL", "AMOUNT", "GRAND TOTAL" boost confidence; first non-trivial line = merchant.

### Combined Flow Example
User scans a receipt:
1. CameraX captures image.
2. ML Kit extracts raw text (on-device, fast, offline).
3. Heuristic parser tries to extract amount/merchant/date.
4. If extraction is ambiguous, send the raw OCR text to Gemini for structured parsing.
5. Show pre-filled Add Transaction screen for user confirmation.

---

# Task List

> Each task below is a **self-contained prompt** to copy-paste to Claude Code.
> Tasks build on each other — execute in order.

---

## Phase 1: Foundation

### Task 1 — Project Setup, Dependencies, and Gradle Configuration

**Prompt to Claude Code:**

> Set up the foundational Gradle configuration for the Expense Tracker app.
>
> **Goals:**
> 1. Configure version catalog `gradle/libs.versions.toml` with all libraries we'll need.
> 2. Configure `build.gradle.kts` (Project and App level) with all required plugins and dependencies.
> 3. Set up `local.properties` placeholder for `GEMINI_API_KEY`.
> 4. Configure BuildConfig to expose the API key.
> 5. Configure ProGuard rules placeholder.
>
> **Dependencies to include in version catalog:**
> - AGP, Kotlin, KSP, Hilt, Compose BOM
> - Compose: ui, material3, material-icons-extended, navigation, lifecycle-runtime-compose, lifecycle-viewmodel-compose
> - Room: runtime, ktx, compiler (KSP)
> - Hilt: android, compiler (KSP), hilt-navigation-compose
> - Kotlinx: coroutines-android, serialization-json
> - DataStore: preferences
> - Gemini: `com.google.ai.client.generativeai:generativeai:0.9.0`
> - ML Kit: `com.google.mlkit:text-recognition:16.0.1`
> - CameraX: camera-camera2, camera-lifecycle, camera-view (1.4.x)
> - Vico charts: `com.patrykandpatrick.vico:compose-m3:2.0.0`
> - Coil 3: `io.coil-kt.coil3:coil-compose:3.0.x`
> - Lottie: `com.airbnb.android:lottie-compose:6.x`
> - WorkManager: `androidx.work:work-runtime-ktx:2.9.x` + `androidx.hilt:hilt-work:1.2.x`
> - Accompanist: permissions
> - Splash screen: `androidx.core:core-splashscreen:1.0.1`
>
> **Plugins to apply in app module:**
> - `com.android.application`
> - `org.jetbrains.kotlin.android`
> - `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.0 compose plugin)
> - `org.jetbrains.kotlin.plugin.serialization`
> - `com.google.devtools.ksp`
> - `com.google.dagger.hilt.android`
>
> **App module config:**
> - `namespace = "com.expensetracker.app"`
> - `applicationId = "com.expensetracker.app"`
> - `minSdk = 26, targetSdk = 35, compileSdk = 35`
> - `versionCode = 1, versionName = "1.0.0"`
> - Enable BuildConfig and Compose
> - `buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")`
>
> **Also:**
> - Create `ExpenseTrackerApp.kt` annotated with `@HiltAndroidApp`.
> - Register it in `AndroidManifest.xml`.
> - Add INTERNET, CAMERA, POST_NOTIFICATIONS, VIBRATE permissions to manifest.
> - Set the Application's `theme` to a Material 3 theme (will be defined in Task 2).
>
> **Acceptance:** Run `./gradlew assembleDebug` — must succeed.

---

### Task 2 — Design System: Theme, Colors, Typography, Components

**Prompt to Claude Code:**

> Build the complete design system. Reference the "Design System" section of `EXPENSE_TRACKER_TASKS.md`.
>
> **Create in `core/designsystem/theme/`:**
> 1. `Color.kt` — Define all color tokens: light scheme, dark scheme, semantic colors (income, expense, warning), the 16-color category palette as a `List<Color>` named `CategoryPalette`.
> 2. `Typography.kt` — Material 3 typography with custom font (use Plus Jakarta Sans via Google Fonts downloadable, fall back to default if it fails).
> 3. `Shapes.kt` — Material 3 shapes with `small=8dp, medium=16dp, large=24dp, extraLarge=28dp`.
> 4. `Theme.kt` — `ExpenseTrackerTheme` Composable supporting:
>    - Light/Dark mode
>    - Dynamic color (Material You) on Android 12+
>    - User override (force light/dark/system)
>
> **Create in `core/designsystem/component/`:**
> 1. `AppCard.kt` — `AppCard(modifier, onClick, content)` — surface with 16dp radius, tonal elevation, optional onClick with ripple.
> 2. `AppButton.kt` — variants: `AppPrimaryButton`, `AppSecondaryButton`, `AppTextButton`. Each supports leading icon, loading state.
> 3. `AppTextField.kt` — outlined text field with leading icon slot, error state, helper text.
> 4. `AmountText.kt` — `AmountText(amount: Double, type: TransactionType, currency: String)` — renders large styled amount with sign and color.
> 5. `CategoryChip.kt` — circular colored background with emoji/icon + label.
> 6. `EmptyState.kt` — illustration (use Lottie or vector), title, message, optional CTA button.
> 7. `LoadingIndicator.kt` — branded circular loader.
> 8. `SectionHeader.kt` — bold title + optional trailing action.
> 9. `ShimmerBox.kt` — skeleton loading placeholder.
>
> **Create in `core/util/`:**
> 1. `CurrencyFormatter.kt` — formats `Double` as currency given a currency code (default VND with grouping but no decimals; USD/EUR with 2 decimals).
> 2. `DateUtils.kt` — relative date formatting ("Today", "Yesterday", "Mar 15"), week/month range helpers.
> 3. `Extensions.kt` — useful Modifier and Flow extensions.
>
> Apply the theme in `MainActivity.kt` (which already uses Compose).
>
> **Acceptance:** Build succeeds. Create a quick `PreviewAll.kt` file with `@Preview` of each component to verify visually.

---

### Task 3 — Navigation Scaffold and Main Shell

**Prompt to Claude Code:**

> Set up navigation and the main app shell.
>
> **Create in `navigation/`:**
> 1. `AppDestinations.kt` — Sealed class hierarchy using `kotlinx.serialization` for type-safe navigation. Include destinations for every screen we'll build:
>    - `Home`, `TransactionList`, `AddEditTransaction(id: Long?)`, `TransactionDetail(id: Long)`
>    - `Categories`, `Budgets`, `Recurring`, `Statistics`
>    - `AiAssistant`, `ReceiptScanner`
>    - `Settings`, `Onboarding`, `WalletManagement`
> 2. `AppNavHost.kt` — `NavHost` with all destinations. Placeholder Composables (`Text("Home")`, etc.) for screens not yet built — we'll replace these as we build features.
>
> **Create `MainActivity.kt`** as a single activity:
> - `@AndroidEntryPoint`
> - Sets content to `ExpenseTrackerTheme { AppShell() }`
> - Installs splash screen
>
> **Create `AppShell.kt`** in `feature/` or `core/`:
> - `Scaffold` with `bottomBar` showing `NavigationBar` with 4 tabs: **Home**, **Transactions**, **Statistics**, **AI** (AI Assistant).
> - The fifth icon (a large FAB in the center) is the **Quick Add** button — opens a `ModalBottomSheet` for adding a transaction quickly.
> - Top app bar adapts per destination (CenterAlignedTopAppBar with title, optional actions).
> - Use `NavigationSuiteScaffold` if you want to support tablet/landscape too — optional.
>
> **Behavior:**
> - Bottom nav uses `NavController.currentBackStackEntryAsState()` to highlight active tab.
> - Quick Add FAB opens a `ModalBottomSheet` with a compact "Add Transaction" form (amount, category picker, type toggle, note, date). Reuse the full form Composable later.
> - Settings, Budgets, Categories, Recurring, ReceiptScanner are accessed via menu items in the top bar or settings screen — they don't need bottom-bar tabs.
>
> **Acceptance:** App launches, you can tap each bottom-nav tab, the FAB opens a stub bottom sheet, and back navigation works correctly.

---

## Phase 2: Data Layer

### Task 4 — Room Database: Entities, DAOs, Database, Migrations

**Prompt to Claude Code:**

> Build the complete Room database layer. Reference "Database Schema" section.
>
> **Create entities in `data/local/entity/`:**
> 1. `WalletEntity` — table `wallets`
> 2. `CategoryEntity` — table `categories`
> 3. `TransactionEntity` — table `transactions`. Indexes on `walletId`, `categoryId`, `date`, `recurringId`, `parentSplitId`. Foreign keys to wallet (CASCADE), category (RESTRICT), parent transaction (CASCADE, nullable self).
> 4. `BudgetEntity` — table `budgets`
> 5. `RecurringTransactionEntity` — table `recurring_transactions`
> 6. `AiChatMessageEntity` — table `ai_chat_messages`
> 7. `AiInsightEntity` — table `ai_insights`
>
> Use `@PrimaryKey(autoGenerate = true) val id: Long = 0`.
> All money amounts: `Double` (we'll store major units; for production-grade you'd use BigDecimal/cents — keep it simple here).
> Enums (`TransactionType`, `BudgetPeriod`, `RecurrenceFrequency`, `ChatRole`, `InsightType`) live in `domain/model/` and are converted via TypeConverters.
>
> **Create `data/local/database/Converters.kt`:**
> Type converters for `LocalDate`, `LocalDateTime`, `List<String>` (JSON via kotlinx.serialization), and each enum.
>
> **Create DAOs in `data/local/dao/`:**
> Each DAO returns `Flow` for observed queries and `suspend fun` for one-shot ops.
> - `WalletDao` — CRUD + observeAll + observeById + getTotalBalance()
> - `CategoryDao` — CRUD + observeByType + observeAll + insertAll
> - `TransactionDao`:
>   - `observeAll()`, `observeByDateRange(start, end)`, `observeByCategory(categoryId)`, `observeByWallet(walletId)`
>   - `observeSplitChildren(parentId)`
>   - `getTotalByTypeAndDateRange(type, start, end)`
>   - `getSumByCategoryAndDateRange(categoryId, start, end)`
>   - `searchByNote(query)`
>   - insert, update, delete
> - `BudgetDao` — CRUD + observeActive + observeByCategory
> - `RecurringTransactionDao` — CRUD + observeActive + getDueRecurringTransactions(asOfDate)
> - `AiChatMessageDao` — observeBySession + insert + deleteSession
> - `AiInsightDao` — observeUndismissed + insert + dismiss
>
> **Create `data/local/database/AppDatabase.kt`:**
> - `@Database` annotation with all entities, `version = 1`, `exportSchema = true`.
> - `@TypeConverters(Converters::class)`.
> - Abstract DAO getters.
>
> **Create `data/local/database/DatabaseSeeder.kt`:**
> - On first run, insert a default wallet "Cash" and a list of default categories:
>   - Expense: Food 🍔, Transport 🚗, Shopping 🛍️, Entertainment 🎬, Bills 💡, Health 🏥, Education 📚, Travel ✈️, Groceries 🛒, Other 📦
>   - Income: Salary 💼, Bonus 🎁, Investment 📈, Other Income 💰
> - Use a `RoomDatabase.Callback` and seed on `onCreate`.
>
> **Create `di/DatabaseModule.kt`:**
> - Hilt module providing `AppDatabase` and each DAO as `@Provides`/`@Singleton`.
>
> **Create `data/local/datastore/UserPreferencesDataStore.kt`:**
> - Wraps DataStore Preferences.
> - Stores: themeMode (SYSTEM/LIGHT/DARK), currency (VND/USD/EUR/...), hasCompletedOnboarding, defaultWalletId, geminiEnabled, monthStartDay (1-28).
> - Exposes `Flow<UserPreferences>` and suspend setters.
>
> **Create `di/DataStoreModule.kt`** for DI.
>
> **Acceptance:** Build succeeds. Add a temporary debug button in Home that inserts a test transaction and logs the read-back Flow.

---

### Task 5 — Domain Models, Repository Interfaces, and Repository Implementations

**Prompt to Claude Code:**

> Build the domain and repository layer.
>
> **Create in `domain/model/`:**
> Pure Kotlin data classes (no Android imports) mirroring entities but cleaner:
> - `Wallet`, `Category`, `Transaction`, `Budget`, `RecurringTransaction`, `AiChatMessage`, `AiInsight`
> - Enums: `TransactionType { INCOME, EXPENSE, TRANSFER }`, `BudgetPeriod`, `RecurrenceFrequency`, `ChatRole`, `InsightType`
> - Helper value classes: `Money(val amount: Double, val currency: String)` (optional)
>
> **Create in `data/mapper/`:**
> Extension functions `Entity.toDomain()` and `Domain.toEntity()` for each model.
>
> **Create in `domain/repository/`** (interfaces):
> - `WalletRepository`, `CategoryRepository`, `TransactionRepository`, `BudgetRepository`, `RecurringTransactionRepository`, `AiRepository`, `PreferencesRepository`
>
> Each interface should expose the operations needed by the UI. Examples:
> ```
> interface TransactionRepository {
>     fun observeAll(): Flow<List<Transaction>>
>     fun observeByDateRange(start: LocalDate, end: LocalDate): Flow<List<Transaction>>
>     fun observeByCategory(categoryId: Long): Flow<List<Transaction>>
>     suspend fun add(transaction: Transaction): Long
>     suspend fun update(transaction: Transaction)
>     suspend fun delete(id: Long)
>     suspend fun getById(id: Long): Transaction?
>     suspend fun split(parentId: Long, children: List<Transaction>)
>     fun getTotalByType(type: TransactionType, start: LocalDate, end: LocalDate): Flow<Double>
>     fun search(query: String): Flow<List<Transaction>>
> }
> ```
>
> **Create implementations in `data/repository/`:**
> Each implementation takes the relevant DAO via constructor injection and delegates, mapping entities to domain models.
>
> **`PreferencesRepositoryImpl`** wraps `UserPreferencesDataStore`.
>
> **`AiRepositoryImpl`** — leave the AI methods as stubs returning placeholder data for now (we'll implement in Tasks 13–16). Interface should already declare:
> ```
> suspend fun sendChatMessage(history: List<AiChatMessage>, userContext: String): Result<String>
> suspend fun parseNaturalLanguageTransaction(text: String, categories: List<Category>): Result<ParsedTransaction>
> suspend fun categorizeTransaction(note: String, amount: Double, categories: List<Category>): Result<Long?>
> suspend fun generateMonthlyInsights(transactions: List<Transaction>, categories: List<Category>): Result<List<AiInsight>>
> ```
>
> **Create `di/RepositoryModule.kt`** binding interfaces to implementations.
>
> **Acceptance:** Build succeeds. Inject `TransactionRepository` into the Home ViewModel (temporary) and observe the list.

---

## Phase 3: Core Features

### Task 6 — Home Dashboard Screen

**Prompt to Claude Code:**

> Build the Home (Dashboard) screen — the first thing users see.
>
> **Create `feature/home/`:**
> - `viewmodel/HomeViewModel.kt`
> - `state/HomeUiState.kt`
> - `screen/HomeScreen.kt`
> - `screen/component/BalanceCard.kt`, `MonthSummaryCard.kt`, `TopCategoriesCard.kt`, `RecentTransactionsList.kt`, `BudgetProgressCard.kt`, `AiInsightCard.kt`
>
> **HomeUiState contains:**
> - `totalBalance: Double`
> - `monthIncome: Double, monthExpense: Double, monthNet: Double`
> - `topCategoriesThisMonth: List<CategorySpending>` (top 5 with percentage)
> - `recentTransactions: List<Transaction>` (last 5)
> - `activeBudgets: List<BudgetProgress>` with spent/limit/percentage
> - `latestInsight: AiInsight?` (most recent undismissed AI insight)
> - `selectedMonth: YearMonth`
> - `isLoading: Boolean`
>
> **HomeViewModel:**
> - Combines flows from `WalletRepository`, `TransactionRepository`, `BudgetRepository`, `AiRepository` (insights).
> - Computes all state values reactively.
> - Exposes `StateFlow<HomeUiState>`.
> - `onEvent`: `ChangeMonth`, `DismissInsight`, `RefreshInsights`, `QuickAddClicked`.
>
> **HomeScreen layout (top → bottom, in a LazyColumn):**
> 1. **Header:** Greeting ("Good morning, Khoa 👋"), month picker, settings/profile icon.
> 2. **Balance Card** — Hero card with gradient background (use the brand teal → secondary). Shows total balance large, with income/expense for the month underneath as small chips.
> 3. **AI Insight Card** (if available) — Card with sparkle icon, the insight text, action buttons ("Tell me more", "Dismiss").
> 4. **Active Budgets** — Horizontal scrolling row of `BudgetProgressCard`s with linear progress bars colored green→yellow→red as utilization increases.
> 5. **Top Categories** — Pie chart (Vico) or list of horizontal bars showing top 5 spending categories for the month.
> 6. **Recent Transactions** — Section header with "See all" → navigates to TransactionList. List the 5 most recent with category icon, note, amount.
> 7. **Quick Actions** — Row of icon buttons: Scan Receipt, AI Chat, Add Recurring.
>
> **Visual polish:**
> - Use animated content transitions when the month changes (`AnimatedContent`).
> - Cards have subtle entrance animation (fade + slide).
> - Pull-to-refresh.
> - If no data, show beautiful empty state with Lottie animation + CTA "Add your first transaction".
>
> **Acceptance:** Home renders with mocked or real data. Smooth animations. Tapping any card navigates appropriately (use NavController).

---

### Task 7 — Transaction CRUD: Add/Edit, List, Detail, Filters, Split

**Prompt to Claude Code:**

> Build all transaction-related screens. This is the most important feature set.
>
> **Files to create in `feature/transaction/`:**
> - `screen/AddEditTransactionScreen.kt` + `AddEditTransactionViewModel.kt` + `AddEditTransactionUiState.kt`
> - `screen/TransactionListScreen.kt` + `TransactionListViewModel.kt` + `TransactionListUiState.kt`
> - `screen/TransactionDetailScreen.kt` + `TransactionDetailViewModel.kt`
> - `screen/component/TransactionListItem.kt`, `CategoryPickerSheet.kt`, `DatePickerDialog.kt`, `AmountKeypad.kt`, `SplitTransactionDialog.kt`, `FilterBottomSheet.kt`
>
> ### AddEditTransactionScreen
> Used for both creating and editing. Receives optional `transactionId: Long?`.
>
> **Layout:**
> - Top: large amount display + custom numeric keypad (with `+`, `-`, `×`, `÷` for quick math). Tap the operation, type next number, see live result. Use `mathjs`-style eval or implement a simple expression parser.
> - Type toggle: segmented button (Expense / Income / Transfer).
> - Category: row of recent categories + "More" → opens `CategoryPickerSheet` (modal bottom sheet with grid of all categories filtered by type).
> - Wallet: dropdown (if multi-wallet).
> - Date: chip showing date, opens DatePickerDialog.
> - Note: text field (multiline, max 200 chars).
> - Photo attachment: button → camera or gallery (CameraX). Show thumbnail.
> - Tags: free-form chip input.
> - Advanced (collapsed by default): Make Recurring → opens recurring config. Split → opens SplitTransactionDialog.
> - Save button (FAB-like at bottom): disabled until amount > 0 and category selected.
>
> **Edit mode extras:**
> - Toolbar action: delete (confirms via dialog).
> - If recurring child: notice "This is part of a recurring schedule. Editing only affects this occurrence."
>
> **Split feature:**
> SplitTransactionDialog lets user divide the amount into N parts. Each part can have its own category and note. The parent transaction is marked, children link back via `parentSplitId`. The summary view shows the parent collapsed with a "Split (3 items)" badge and expands to show children.
>
> ### TransactionListScreen
> - Sticky header section per date group ("Today", "Yesterday", "Mar 15, 2025").
> - Each item: leading category circle, note + category name, trailing amount in green/red.
> - Swipe-to-delete with undo snackbar.
> - Long-press: multi-select for bulk delete or category change.
> - Search bar in top app bar (search by note).
> - Filter button → FilterBottomSheet: by date range, category, wallet, type, min/max amount.
> - Empty state if no results.
>
> ### TransactionDetailScreen
> - Read-only detail view.
> - Shows amount, category (with icon), wallet, date, note, photo (full size, tap to zoom), tags, location, "Created at" timestamp.
> - If split: lists children.
> - If recurring: notice + link to recurring schedule.
> - Edit and Delete buttons.
>
> **State patterns:**
> - Use `sealed interface AddEditUiEvent` and `sealed interface TransactionListUiEvent`.
> - Show loading shimmer while data loads.
>
> **Acceptance:** Full CRUD works end-to-end with the database. Splits persist correctly. Photo attachment saves to internal storage and the URI is stored.

---

### Task 8 — Categories Management

**Prompt to Claude Code:**

> Build category management.
>
> **Files in `feature/category/`:**
> - `screen/CategoriesScreen.kt` + ViewModel + UiState
> - `screen/AddEditCategoryScreen.kt` + ViewModel + UiState
> - `screen/component/IconPickerSheet.kt`, `ColorPickerSheet.kt`
>
> ### CategoriesScreen
> - Two tabs: **Expense** / **Income**.
> - Grid of categories (3 columns), each showing colored circle + emoji/icon + name + (optionally) total spent this month.
> - FAB to add a new category.
> - Long-press on a category: bottom sheet with Edit / Archive / Delete (delete is blocked if transactions exist — offer to archive instead).
> - Drag-to-reorder (use `Modifier.detectDragGesturesAfterLongPress` or `reorderable` library if you prefer).
>
> ### AddEditCategoryScreen
> - Name field.
> - Type toggle (Expense/Income).
> - Icon picker — opens `IconPickerSheet` with a grid of emojis (curated set: 🍔🍕🍣🛒☕🍺🚗🚕✈️🚇⛽🛍️👕💊🏥🎬🎮🎵📚🎓💼💰📈🎁💝🏠💡📱🌐 etc.) and Material icons.
> - Color picker — opens `ColorPickerSheet` showing the 16-color CategoryPalette.
> - Preview chip updates live.
> - Save / Cancel.
>
> **Acceptance:** User can create custom categories with chosen icon/color. They appear in the transaction add flow immediately.

---

### Task 9 — Budget Management

**Prompt to Claude Code:**

> Build budget creation, tracking, and alerts.
>
> **Files in `feature/budget/`:**
> - `screen/BudgetsScreen.kt` + ViewModel + UiState
> - `screen/AddEditBudgetScreen.kt` + ViewModel + UiState
> - `screen/component/BudgetCard.kt`, `BudgetProgressBar.kt`
>
> ### BudgetsScreen
> - List of active budgets.
> - Each budget card:
>   - Title: category name (or "Total Budget" if categoryId is null)
>   - Period label ("This Month", "Mar 1 – Mar 31")
>   - Big amount: spent / total
>   - Linear progress bar with gradient color (green → yellow → red based on percentage)
>   - Remaining amount / "Over by X" if exceeded
>   - Days remaining in period
>   - Average daily spend vs. budget pace
> - FAB to add new budget.
> - Tap a budget → opens detail view (could be the AddEdit screen in read mode with transactions in period).
>
> ### AddEditBudgetScreen
> - Category: dropdown (or "Overall" for total budget).
> - Amount field.
> - Period: segmented (Weekly / Monthly / Yearly / Custom).
> - Start date / end date (auto-calculated for non-custom).
> - Alert threshold slider: "Notify me at X% (default 80%)".
> - Save / Cancel.
>
> **Logic:**
> - Use `TransactionDao.getSumByCategoryAndDateRange` to compute spending.
> - When user updates a transaction, budgets update reactively via Flow.
>
> **Notifications (foundation, full notification in Task 17):**
> - Define a `BudgetAlertChecker` that, given a transaction insert event, checks all active budgets and returns those crossing thresholds. We'll wire up notifications in Task 17.
>
> **Acceptance:** Budgets compute spending correctly, progress bars update in real time after adding a transaction.

---

### Task 10 — Recurring Transactions

**Prompt to Claude Code:**

> Build recurring transactions feature.
>
> **Files in `feature/recurring/`:**
> - `screen/RecurringListScreen.kt` + ViewModel + UiState
> - `screen/AddEditRecurringScreen.kt` + ViewModel + UiState
> - Worker: `data/worker/RecurringTransactionWorker.kt`
>
> ### RecurringListScreen
> - List of all recurring schedules.
> - Each card shows: category, amount, frequency ("Every month on the 1st"), next occurrence date, active toggle.
> - FAB to add new.
> - Tap → AddEditRecurringScreen.
> - Long-press → bottom sheet (pause/resume, delete).
>
> ### AddEditRecurringScreen
> Similar to AddEditTransaction but with:
> - Frequency: Daily / Weekly / Monthly / Yearly
> - Interval: "Every [N] [unit]" (e.g., every 2 weeks)
> - Start date, optional end date
> - Day-of-week (for weekly) or day-of-month (for monthly)
>
> ### RecurringTransactionWorker
> - WorkManager periodic worker (runs daily).
> - For each active recurring transaction where `nextOccurrence <= today`:
>   - Create a new Transaction linked via `recurringId`.
>   - Compute next occurrence based on frequency and interval.
>   - Update `lastProcessed` and `nextOccurrence`.
>   - If `nextOccurrence > endDate`, set `isActive = false`.
> - Show a notification listing newly created transactions.
>
> **Scheduling:**
> - In `ExpenseTrackerApp.onCreate`, enqueue the worker as periodic (every 24h).
> - Also trigger a one-time run on app start to catch missed schedules.
>
> **HiltWorkerFactory:** Configure Hilt for WorkManager.
>
> **Acceptance:** Create a recurring transaction with start=yesterday and frequency=daily. Run the worker manually (via a debug button or just wait/trigger). A new transaction should appear with the recurring link.

---

### Task 11 — Multi-Wallet Management

**Prompt to Claude Code:**

> Build wallet (account) management.
>
> **Files in `feature/wallet/`:**
> - `screen/WalletListScreen.kt` + ViewModel + UiState
> - `screen/AddEditWalletScreen.kt` + ViewModel + UiState
> - `screen/component/WalletCard.kt`
>
> ### WalletListScreen
> - List of wallets as colorful gradient cards.
> - Each card: wallet name, icon, current balance (initialBalance + sum of transactions), currency.
> - FAB to add new wallet.
> - Tap → AddEditWalletScreen (or detail view filtered by wallet).
> - Bottom: total balance across all wallets.
>
> ### AddEditWalletScreen
> - Name, icon picker (cash, bank, credit card, savings, e-wallet — choose icons), color picker, initial balance, currency dropdown.
> - Save / Cancel / Delete (with confirmation, blocked if transactions exist).
>
> ### Transfer between wallets
> Add a `TRANSFER` transaction type:
> - When user picks Transfer in AddEditTransaction, show two wallet pickers (from / to).
> - Create two linked transactions internally OR a single transfer record — decide and document. Simpler: one transaction with type TRANSFER, fromWalletId, toWalletId fields (add these as nullable to entity if needed, run migration).
>
> **Acceptance:** Users can create multiple wallets, transfer between them, and balances update correctly.

---

## Phase 4: Analytics & Visualization

### Task 12 — Statistics Screen with Charts

**Prompt to Claude Code:**

> Build the Statistics screen — the visual analytics powerhouse.
>
> **Files in `feature/statistics/`:**
> - `screen/StatisticsScreen.kt` + ViewModel + UiState
> - `screen/component/SpendingTrendChart.kt` (line), `CategoryPieChart.kt`, `IncomeVsExpenseBarChart.kt`, `DailySpendHeatmap.kt`, `PeriodSelector.kt`, `CategoryBreakdownList.kt`
>
> **Use Vico** (`com.patrykandpatrick.vico:compose-m3`) for line, bar charts. For pie chart, either find a Compose pie chart library or implement custom using `Canvas` (donut chart with category color slices).
>
> **Screen layout (LazyColumn):**
> 1. **PeriodSelector** at top — chips: This Week / This Month / Last Month / This Year / Custom (opens date range picker).
> 2. **Summary card** — Income, Expense, Net for the period. Comparison vs previous period (↑12% from last month).
> 3. **Spending Trend** — line chart of daily spending across the period. X-axis: dates, Y-axis: amount. Smooth curve, gradient fill under the line. Tap a point to see that day's transactions.
> 4. **Income vs Expense** — grouped bar chart by week or month (depending on period).
> 5. **Category Breakdown** — donut chart with category color slices. Center text: total expense. Legend below as a list (CategoryBreakdownList): each row is category, amount, percentage, micro bar.
> 6. **Daily Spending Heatmap** (calendar view) — for monthly period: a calendar grid where each cell's intensity reflects that day's total spending. Tap a day → filtered transaction list.
> 7. **Top Merchants/Notes** — top 5 most frequent notes (treat note as merchant if no proper merchant field).
>
> **Polish:**
> - Animate chart entries (sweep from left for line, grow from bottom for bars, rotate-in for pie slices).
> - All charts respect theme (dark mode colors).
> - Empty period shows friendly message.
>
> **Acceptance:** Charts render with real data, period switching is smooth, all visualizations are responsive and accurate.

---

## Phase 5: AI Features

### Task 13 — Gemini API Integration (Foundation)

**Prompt to Claude Code:**

> Set up the Gemini API integration that all AI features will use.
>
> **Files in `data/remote/gemini/`:**
> 1. `GeminiApiService.kt` — wrapper around `GenerativeModel`.
> 2. `GeminiPrompts.kt` — centralized prompt templates.
> 3. `GeminiModels.kt` — DTOs for structured responses.
>
> **GeminiApiService:**
> ```kotlin
> @Singleton
> class GeminiApiService @Inject constructor(
>     @ApplicationContext private val context: Context
> ) {
>     private val model = GenerativeModel(
>         modelName = "gemini-1.5-flash",
>         apiKey = BuildConfig.GEMINI_API_KEY,
>         generationConfig = generationConfig {
>             temperature = 0.4f
>             topK = 32
>             topP = 0.95f
>             maxOutputTokens = 1024
>         }
>     )
>
>     private val jsonModel = GenerativeModel(
>         modelName = "gemini-1.5-flash",
>         apiKey = BuildConfig.GEMINI_API_KEY,
>         generationConfig = generationConfig {
>             temperature = 0.2f
>             responseMimeType = "application/json"
>         }
>     )
>
>     suspend fun chat(systemInstruction: String, history: List<Content>, userMessage: String): Result<String>
>     suspend fun generateJson(systemInstruction: String, userMessage: String): Result<String>
> }
> ```
>
> Handle errors (network, API key invalid, rate limit, content blocked) and wrap in `Result.success/failure`.
>
> **Add to `di/NetworkModule.kt`** providing `GeminiApiService`.
>
> **Implement `AiRepositoryImpl` properly now** — wire its methods to call `GeminiApiService` with proper prompts. The four methods declared in Task 5 should now work.
>
> **System prompts (place in `GeminiPrompts.kt`):**
>
> **For chat:**
> ```
> You are a personal financial advisor inside an expense tracker app.
> The user's data is provided below as JSON. Use it to give personalized, concrete, and actionable advice.
> Be concise (2-4 sentences typically), friendly, and non-judgmental.
> If you don't know, say so. If a number is involved, format it with the user's currency.
> User context: {USER_CONTEXT_JSON}
> ```
>
> **For parsing natural language:**
> ```
> Parse the user's text into a transaction. Available categories: {CATEGORIES_LIST}.
> Today's date is {TODAY}. The user's currency is {CURRENCY}.
> Return ONLY a JSON object with this schema:
> {
>   "amount": number,
>   "type": "EXPENSE" | "INCOME",
>   "categoryId": number,
>   "note": string,
>   "date": "YYYY-MM-DD",
>   "confidence": number between 0 and 1
> }
> If you can't parse, return {"error": "reason"}.
> ```
>
> **For categorization:**
> ```
> Suggest the best category for this transaction. Categories: {CATEGORIES_LIST}.
> Note: "{NOTE}", Amount: {AMOUNT}.
> Return ONLY: {"categoryId": number, "confidence": number}.
> ```
>
> **For monthly insights:**
> ```
> Analyze the user's spending for {MONTH}. Data: {TRANSACTIONS_SUMMARY_JSON}.
> Compare to previous month: {PREVIOUS_MONTH_JSON}.
> Generate 2-4 insights in JSON array form:
> [{"type": "MONTHLY_SUMMARY" | "ANOMALY" | "RECOMMENDATION", "title": string, "content": string}]
> Be specific, use numbers, focus on actionable observations.
> ```
>
> **Build a "Gemini test" debug screen** (accessible from settings) where you can type a prompt and see the response — useful for verifying the API key works.
>
> **Acceptance:** Set `GEMINI_API_KEY` in local.properties (get a free key at https://aistudio.google.com), build, open debug screen, send a prompt, get a response.

---

### Task 14 — AI Financial Advisor Chatbot

**Prompt to Claude Code:**

> Build the AI Assistant chat screen.
>
> **Files in `feature/ai_assistant/`:**
> - `screen/AiAssistantScreen.kt` + ViewModel + UiState
> - `screen/component/ChatBubble.kt`, `ChatInput.kt`, `SuggestedQuestionChips.kt`, `TypingIndicator.kt`
>
> **AiAssistantUiState:**
> - `messages: List<AiChatMessage>` — observed from DB
> - `isResponding: Boolean`
> - `suggestedQuestions: List<String>` — context-aware suggestions
> - `currentSessionId: String`
>
> **AiAssistantViewModel:**
> - On init: fetch chat history for the current session; build user context (last 30 days of transactions summarized: totals by category, top transactions, current budgets, monthly trends).
> - `onSendMessage(text)`:
>   1. Insert user message into DB (and UI).
>   2. Build conversation history as Gemini `Content` list.
>   3. Build system instruction with fresh user context JSON.
>   4. Call `geminiApiService.chat(...)`.
>   5. Insert assistant message into DB. Handle errors with a "retry" message.
>   6. Refresh suggested questions based on the latest exchange.
>
> **User context summary (JSON, kept small):**
> ```json
> {
>   "currency": "VND",
>   "currentMonth": {
>     "income": 15000000,
>     "expense": 9500000,
>     "byCategory": {"Food": 3200000, "Transport": 1100000, ...}
>   },
>   "lastMonth": { ... },
>   "activeBudgets": [
>     {"category": "Food", "spent": 3200000, "limit": 4000000, "percentage": 80}
>   ],
>   "recurringMonthly": 4500000
> }
> ```
>
> **UI:**
> - Top app bar: title "AI Assistant ✨", action: "New chat" (creates new sessionId), "Clear history".
> - Empty state: greeting, sparkle icon, suggested questions as chips:
>   - "How can I save more money?"
>   - "Where did most of my money go this month?"
>   - "Suggest a realistic food budget"
>   - "Am I spending too much on entertainment?"
> - Message list (LazyColumn, reverseLayout = true): chat bubbles, user-right (primary tinted), assistant-left (surface tinted with sparkle avatar).
> - Markdown rendering for assistant messages (use a Compose markdown library or simple bold/italic parsing).
> - Typing indicator while waiting.
> - Bottom: ChatInput (text field + send button).
>
> **Polish:**
> - Smooth scroll to bottom on new message.
> - Long-press a message to copy.
> - Show timestamp on tap.
>
> **Acceptance:** Real conversation with the AI, contextual to actual user data, persisted across app restarts.

---

### Task 15 — ML Kit Receipt OCR Scanner

**Prompt to Claude Code:**

> Build the receipt scanning feature.
>
> **Files in `feature/ocr_scan/`:**
> - `screen/ReceiptScannerScreen.kt` + ViewModel + UiState
> - `screen/component/CameraPreview.kt`, `OcrResultSheet.kt`
> - `data/mlkit/ReceiptOcrService.kt`
> - `data/mlkit/ReceiptParser.kt`
>
> ### ReceiptOcrService
> ```kotlin
> @Singleton
> class ReceiptOcrService @Inject constructor() {
>     private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
>     suspend fun recognize(uri: Uri, context: Context): Result<String> {
>         val image = InputImage.fromFilePath(context, uri)
>         val result = recognizer.process(image).await()
>         return Result.success(result.text)
>     }
> }
> ```
>
> ### ReceiptParser (heuristic, no AI)
> Given raw OCR text:
> 1. **Amount detection:** Find all numbers that look like currency (regex for "1,234.56" or "1.234.000" patterns, with optional currency symbol). The largest plausible value, or one preceded by "TOTAL"/"AMOUNT"/"GRAND TOTAL"/"TỔNG" (case-insensitive), is the total. Return confidence based on which keyword matched.
> 2. **Date detection:** regex for `dd/MM/yyyy`, `yyyy-MM-dd`, `MM-dd-yyyy`. Closest to common receipt date formats. Default to today if not found.
> 3. **Merchant detection:** First 1–3 lines (usually the header). Strip phone numbers, "Tax Invoice", etc. Use as note.
> 4. Return `ParsedReceipt(amount, date, merchant, rawText, confidence)`.
>
> ### Fallback to Gemini
> If parser confidence < 0.5, send the raw OCR text to Gemini with a prompt:
> ```
> Parse this OCR'd receipt into structured data. Return JSON: {amount, date (YYYY-MM-DD), merchant, currency}.
> Text: {RAW_OCR_TEXT}
> ```
> Merge result.
>
> ### ReceiptScannerScreen
> - Two modes:
>   - **Camera mode** (default): CameraX live preview, shutter button at bottom, gallery icon at left. Capture → process.
>   - **Gallery mode**: Photo picker → process.
> - During processing: full-screen loader with "Reading receipt..."
> - On success: show `OcrResultSheet`:
>   - Detected fields with confidence badges (green=high, yellow=medium, red=low).
>   - User can edit any field.
>   - Suggest a category (call AI categorization for the merchant).
>   - "Use this" → navigates to AddEditTransaction pre-filled.
>   - "Try again" / "Scan another".
> - Request CAMERA permission with rationale.
>
> **Polish:**
> - Show captured image at top of result sheet for reference.
> - Detected text overlay (optional): draw bounding boxes on the captured image.
>
> **Acceptance:** Scan a real receipt, see extracted fields, save as a transaction.

---

### Task 16 — Smart Categorization & Natural Language Input

**Prompt to Claude Code:**

> Build two more AI-powered features that integrate seamlessly into existing screens.
>
> ### Feature A: Smart Auto-Categorization
> In **AddEditTransactionScreen**, after the user types a note and amount:
> - Debounce 600ms.
> - Call `aiRepository.categorizeTransaction(note, amount, categories)`.
> - Show a subtle suggestion chip below the category row: "✨ Suggested: Food" with tap-to-apply.
> - Cache results in memory (note → categoryId) within the ViewModel to avoid repeat calls.
> - Show only if confidence > 0.6.
>
> ### Feature B: Natural Language Quick Add
> Add a new entry point: in the Quick Add bottom sheet (from FAB), include a tab "Natural Language" alongside "Manual".
>
> **NaturalLanguageInput UI:**
> - Text field placeholder: "Describe your expense — e.g. '50k for coffee this morning'"
> - Voice input button (uses platform `SpeechRecognizer` — request RECORD_AUDIO permission).
> - "Parse" button.
> - On parse: call `aiRepository.parseNaturalLanguageTransaction`. Show parsed result as a preview card (amount, category, date, note). User can adjust and save.
> - Example queries shown as chips: "Bought groceries 200k", "Salary 15 million today", "Coffee with friends 80k yesterday".
>
> ### Feature C: Monthly Insights (generation trigger)
> - On the 1st of each month (via WorkManager), generate insights for last month.
> - Run via `MonthlyInsightsWorker` — calls `aiRepository.generateMonthlyInsights`.
> - Stored in `ai_insights` table.
> - Displayed on Home screen (`AiInsightCard`) and a dedicated "Insights" screen accessible from Statistics.
> - Manual trigger: button in Statistics screen "Refresh insights ✨".
>
> ### Feature D: Anomaly Detection (lightweight, no AI required for the math)
> - When a new transaction is added, compute z-score against the user's last 90 days of same-category transactions.
> - If z-score > 2.5 AND amount > a small threshold, flag as anomaly and create an `AiInsight` of type `ANOMALY`.
> - Optionally call Gemini to generate a friendly explanation: "This grocery trip was 3x your usual — was there a special occasion?"
>
> **Acceptance:** Type a note in Add Transaction and watch the AI suggest a category. Type "Lunch 120k" in NL input and watch it parse correctly.

---

## Phase 6: Polish & Production

### Task 17 — Notifications, Reminders, and Budget Alerts

**Prompt to Claude Code:**

> Implement the notification system.
>
> **Notification channels (create in `ExpenseTrackerApp.onCreate`):**
> 1. `BUDGET_ALERTS` — high importance — when a budget threshold is crossed.
> 2. `DAILY_REMINDER` — default — daily "Did you log your expenses today?" at user-configured time.
> 3. `RECURRING` — low importance — when a recurring transaction was auto-created.
> 4. `AI_INSIGHTS` — default — when new monthly insights are ready.
>
> **Files:**
> - `core/notification/NotificationHelper.kt`
> - `data/worker/DailyReminderWorker.kt`, `BudgetCheckWorker.kt`, `MonthlyInsightsWorker.kt`
>
> ### Budget alerts
> - Triggered after every transaction insert/update (in `TransactionRepositoryImpl` — wire a callback or use a domain event).
> - For each active budget that crossed `alertThreshold` (e.g., went from <80% to >=80%, or went from <100% to >=100%), show a notification.
> - Don't re-notify for the same threshold in the same period.
>
> ### Daily reminder
> - WorkManager periodic worker (every day at user's chosen time, default 8 PM).
> - Skip if user already logged a transaction today.
> - Tap → opens Quick Add.
>
> ### Recurring notification
> - Already specified in Task 10 — show after RecurringTransactionWorker runs.
>
> ### Permission
> - Request POST_NOTIFICATIONS permission on Android 13+. Trigger from Onboarding or first-launch dialog.
>
> **Acceptance:** Create a budget for 100k, add an expense of 90k in that category — receive a "You've used 90% of your Food budget" notification.

---

### Task 18 — Onboarding, Settings, Backup/Restore, Dark Mode

**Prompt to Claude Code:**

> Build the onboarding flow and settings screen.
>
> ### Onboarding (`feature/onboarding/`)
> First-launch only. 4 pager screens:
> 1. Welcome — illustration, app name, tagline.
> 2. "Track expenses in seconds" — show Quick Add demo.
> 3. "AI-powered insights" — show chatbot mock.
> 4. Setup — pick currency, monthly start day (1–28), enable notifications, optionally set first budget.
> Use `HorizontalPager` with custom indicators. "Skip" / "Next" / "Get Started".
> On finish: set `hasCompletedOnboarding = true`.
>
> ### Settings (`feature/settings/`)
> - **Profile / Preferences:** Name (optional), currency, month start day.
> - **Appearance:** Theme mode (System / Light / Dark), dynamic color toggle.
> - **AI:** Enable AI features (master toggle), Gemini API key (read-only display, user can edit if they want their own key — store in DataStore, overrides BuildConfig).
> - **Notifications:** Daily reminder on/off + time picker, budget alerts on/off.
> - **Data:**
>   - **Export** to JSON or CSV (Storage Access Framework, save to user-chosen location). Include all transactions, categories, budgets, wallets.
>   - **Import** from JSON — confirm with dialog warning of overwrite.
>   - **Backup to Drive** (stretch goal — can be a placeholder).
>   - **Clear all data** — danger button with confirmation.
> - **About:** App version, developer credit, open source licenses, privacy policy link.
>
> ### Export/Import
> - Use `kotlinx.serialization` to serialize all entities to a JSON file.
> - Schema versioned to handle migrations.
>
> **Acceptance:** Onboarding shows on first launch, settings screen works, theme toggling is instant, export/import roundtrips successfully.

---

### Task 19 — Final Polish: Animations, Empty States, Error Handling, Accessibility

**Prompt to Claude Code:**

> The final polish pass to make the app feel premium.
>
> **Animations:**
> - Shared element transitions: tap a TransactionListItem → expand into TransactionDetailScreen (use Compose's `SharedTransitionLayout`).
> - Bottom sheet slide-up with spring physics on Quick Add.
> - Pie chart: animated rotation-in on first display, smooth re-animate when data changes.
> - Number counters: animate amounts (use `animateFloatAsState` with currency formatter).
> - List item enter animations (staggered fade + slide on first render).
> - Pull-to-refresh on Home and TransactionList.
> - Page transitions in NavHost (slide + fade).
>
> **Empty states:**
> Every list screen and the Home dashboard must have a beautiful empty state. Use Lottie animations from LottieFiles (free) — wallet, piggy bank, charts, sparkles themed. Add a clear CTA.
>
> **Error handling:**
> - Catch all repository exceptions, expose as UiState errors.
> - Show errors as Snackbars or inline cards. Never crash.
> - Network errors (Gemini): "Couldn't reach AI service. Try again?" with retry button.
> - Show offline indicator when no internet and AI features are attempted.
>
> **Loading states:**
> - Replace all generic `CircularProgressIndicator`s with `ShimmerBox` skeletons that mirror the final layout.
>
> **Accessibility:**
> - All interactive elements have content descriptions.
> - Color contrast meets WCAG AA.
> - Support large fonts (test with system font scale 1.5x).
> - Support TalkBack — meaningful screen titles and live region announcements for important changes.
>
> **Performance:**
> - Move heavy computations off main thread.
> - Use `remember` and `derivedStateOf` correctly.
> - Use `LazyColumn` with stable keys.
> - Profile with Layout Inspector and Macrobenchmark if time permits.
>
> **Final touches:**
> - App icon (use Image Asset Studio, design a wallet/coin icon with the brand teal).
> - Splash screen with logo.
> - Adaptive icon (foreground + background).
> - Notification icons (monochrome).
> - Update `strings.xml` — no hardcoded user-facing strings in code.
> - Localization-ready (English + Vietnamese — provide `values-vi/strings.xml`).
>
> **Acceptance:** App feels polished and production-ready. No crashes. Smooth on a real device. Demo flow: open app → see onboarding → set up → add transaction via NL → scan a receipt → ask AI a question → view statistics → set a budget → receive notification.

---

## 🎓 Project Submission Checklist

Before submitting your final project, verify:

- [ ] App builds cleanly: `./gradlew clean assembleDebug` succeeds.
- [ ] No crash on cold start.
- [ ] All CRUD operations work (transaction, category, budget, recurring, wallet).
- [ ] Charts render correctly with data.
- [ ] Gemini API responds (test with a real key).
- [ ] ML Kit OCR works on a real receipt photo.
- [ ] Notifications fire when expected.
- [ ] Dark mode looks good.
- [ ] Export/import works.
- [ ] All screens have empty states.
- [ ] APK size is reasonable (< 50MB).
- [ ] README.md with screenshots, architecture diagram, setup instructions.

### Suggested README structure for submission

1. **Project name & tagline**
2. **Screenshots** (Home, Add Transaction, Statistics, AI Chat, Receipt Scan, Settings)
3. **Features** (bullet list — emphasize AI, UI/UX, multi-feature scope)
4. **Tech stack**
5. **Architecture** (diagram)
6. **AI features explained** (what each does, what model/service)
7. **Setup instructions** (clone, add `GEMINI_API_KEY` to `local.properties`, build)
8. **Demo video / GIF**
9. **Future work**
10. **Acknowledgments**

---

## 💡 Stretch Goals (if time permits)

- **Widget on home screen** — quick balance and add transaction.
- **Wear OS companion app** — quickly log transactions from watch.
- **Cloud sync** — Firebase Auth + Firestore for multi-device sync.
- **Family sharing** — share a wallet with another user.
- **Goals & savings** — "Save 5M VND for laptop by December" with progress tracking.
- **Bill splitting** — Splitwise-style: log a group expense and compute who owes whom.
- **Bank SMS parsing** — read banking SMS (with permission), auto-create transactions from them. Use Gemini or regex parsing.
- **Voice-only mode** — full hands-free interaction for accessibility.

---

## 📚 Reference Links

- Jetpack Compose docs: https://developer.android.com/jetpack/compose
- Material 3 Compose: https://developer.android.com/jetpack/androidx/releases/compose-material3
- Hilt Android: https://developer.android.com/training/dependency-injection/hilt-android
- Room: https://developer.android.com/training/data-storage/room
- Gemini API (Kotlin): https://ai.google.dev/tutorials/android_quickstart
- Get Gemini API key: https://aistudio.google.com/app/apikey
- ML Kit Text Recognition: https://developers.google.com/ml-kit/vision/text-recognition/v2/android
- Vico Charts: https://patrykandpatrick.com/vico
- Lottie Files (free animations): https://lottiefiles.com/featured

---

**End of task document. Good luck with your project! 🚀**
