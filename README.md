# Lotto24 Coding Challenge — Draw Results App

A production-grade Android and Kotlin Multiplatform (CMP) application built for the **Lotto24** coding challenge. The application displays the latest draw results, winning numbers, jackpot heights, and next draw dates for Germany's two primary draw-based lotteries: **LOTTO 6aus49** and **Eurojackpot**, along with supplementary games (**Spiel 77** and **SUPER6**).

---

## Interactive Documentation Suite (`docs/`)

The repository includes a dedicated, zero-dependency HTML documentation suite located in the [`docs/`](./docs) folder. Reviewers can open these files directly in any web browser:

* **[docs/index.html](./docs/index.html)**: Central dashboard featuring sidebar navigation, interactive tabs, case study requirements, architecture diagrams, and real-time phase checklists.
* **[docs/requirements.html](./docs/requirements.html)**: Verbatim, 1:1 transcript of the official case study PDF brief.
* **[docs/tech-stack.html](./docs/tech-stack.html)**: In-depth technical stack breakdown, library versions, and rationale.
* **[docs/roadmap.html](./docs/roadmap.html)**: 10-phase micro-roadmap tracking development progress step-by-step.

```bash
# Open documentation in default browser (macOS)
open docs/index.html
```

---

## Architectural Highlights & Engineering Decisions

### 1. Pure Kotlin Clean Architecture & Domain Boundaries
* **Domain Layer (`:shared/domain`)**: Contains zero Android, Compose, or framework dependencies. Consists of pure Kotlin entities (`LotteryDraw`, `Jackpot`), value objects (`Money`, `Currency`), repository contracts (`LotteryRepository`), and focused one-shot suspend use cases (`GetLotteryResultsUseCase`, `LoadFixtureResultsUseCase`).
* **Semantic Money Value Object**: Money is modeled in domain as `Money(val minorUnits: Long, val currency: Currency = Currency.EUR)`. No floating-point Double precision errors and no localized German strings (`"MIO"`, `"€"`) exist in the domain layer.
* **Single Source of Truth**: The domain model carries only the raw drawn numbers (`numbers`, `superNumber`, `euroNumbers`). Derived presentation lists (`balls: ImmutableList<BallUiModel>`) are generated strictly within the presentation mapper (`LotteryUiMapper`) to eliminate state duplication.
* **Decoupled Presentation Layer & Formatters**:
  * Localized German formatters (`CurrencyFormatter`, `LotteryDateTimeFormatter`) reside strictly in `presentation/formatter`.
  * UI models (`LotteryCardUiModel`, `BallUiModel`, `BallUiType`, `SupplementaryGameUiModel`) are decoupled from domain types and annotated with `@Immutable` alongside `ImmutableList` for Compose stability.
* **One-Shot Suspend over Unnecessary Flow**: Repository and UseCase retrieval operations are modeled as idiomatic `suspend fun` returning domain `LotteryResult`, rather than wrapping one-shot REST operations into artificial `Flow` streams.
* **State-Driven Re-entrancy Protection**: `DrawResultsViewModel` protects against concurrent network calls during user interaction using a state-driven guard (`uiState.value !is DrawResultsUiState.Loading && !(uiState.value as? DrawResultsUiState.Success)?.isRefreshing`).

### 2. Data Provenance & In-Memory State Retention
* **Explicit Outcome (`LotteryResult`)**:
  * `Success(data, isFixture)`: Identifies whether data came from the live network endpoint or sample PDF fixtures.
  * `Error(cause, statusCode, cachedData, isCachedDataFixture)`: Preserves previous session draws if a network failure occurs on refresh, enabling the UI to show an offline warning banner rather than wiping the screen.
* **No Silent Fallbacks & 404 Fixture Guards**: If the endpoint returns HTTP 404 or fails, the application does **not** silently disguise 2023 fixture data as live results. An explicit error state is emitted, and `canLoadFixture` is strictly guarded (`statusCode == 404`). For HTTP 500 or network offline errors, `canLoadFixture` is `false`.
* **Cache Provenance Preservation**: The session cache (`LotteryRepositoryImpl`) stores both data and provenance (`CacheEntry(data, isFixture)`). If a user loads sample fixture data, subsequent cache reads without `forceRefresh` retain `isFixture = true`, ensuring sample fixtures are never reported as live winning numbers.

### 3. Timezone Awareness
* Draw times and cutoffs operate in different native timezones:
  * **LOTTO 6aus49**: `Europe/Berlin` (UTC+1 / UTC+2 DST)
  * **Eurojackpot**: `Europe/Helsinki` (UTC+2 / UTC+3 DST)
* The domain model preserves native timezone identifiers (`draw.timeZone`), and `LotteryUiMapper` explicitly resolves the target zone via `LotteryDateTimeFormatter.resolveTimeZone()`. This prevents UTC day-boundary errors (e.g. draws occurring at 22:30 UTC on Friday are Saturday in Helsinki but Friday in Berlin).

### 4. Dependency Injection (Koin 4.x)
* Multiplatform Koin modules: `networkModule`, `dataModule`, `domainModule`, `presentationModule`.
* ViewModel registered using Koin multiplatform ViewModel DSL: `viewModelOf(::DrawResultsViewModel)`.
* **Android**: `Lotto24Application` initializes Koin with `androidContext` and `androidLogger`.
* **iOS**: `MainViewController()` in `iosMain` initializes Koin idempotently via `initKoin()` using Koin's internal `GlobalContext.getOrNull()`, ensuring zero stale mutable state flags across test teardowns or multiple invocations.

---

## Tech Stack

| Component | Library / Framework | Version |
| :--- | :--- | :--- |
| **Language** | Kotlin Multiplatform | `2.4.21` |
| **UI Framework** | Compose Multiplatform (CMP) | `1.12.2` |
| **Android Gradle Plugin** | AGP | `9.5.1` |
| **Compile / Target SDK** | Android SDK | `37` (Min SDK `28`) |
| **Dependency Injection** | Koin Core, Koin Android & Koin Compose | `4.2.2` |
| **Networking** | Ktor Client (ContentNegotiation, Logging, Timeouts) | `3.6.1` |
| **Serialization** | kotlinx-serialization-json | `1.13.0` |
| **Date & Time** | kotlinx-datetime | `0.8.0` |
| **Immutable Collections** | kotlinx-collections-immutable | `0.5.2` |
| **Reactive Streams** | kotlinx-coroutines | `1.11.0` |
| **Testing** | kotlin-test, Turbine, Coroutines Test | `1.2.1` |
| **Android AppWidget** | Jetpack Glance | `1.2.0` |

---

## Project Structure

```
├── androidApp/                       # Android application module (launcher, Glance AppWidget)
│   └── src/main/kotlin/de/lotto24case/
│       ├── Lotto24Application.kt     # Application entrypoint with Koin init
│       └── MainActivity.kt           # Edge-to-edge Compose host activity
├── docs/                             # Interactive HTML documentation suite
│   ├── index.html                    # Dashboard single-page app
│   ├── requirements.html             # Verbatim PDF requirements transcript
│   ├── tech-stack.html               # Technical stack documentation
│   └── roadmap.html                  # Live phase tracking
├── shared/
│   ├── src/commonMain/kotlin/de/lotto24case/
│   │   ├── data/
│   │   │   ├── mapper/               # DTO to Domain mappers (LotteryDtoMapper)
│   │   │   ├── remote/               # Ktor client, DTOs, fixtures, LotteryDataResult
│   │   │   └── repository/           # LotteryRepositoryImpl (in-memory cache & provenance)
│   │   ├── di/                       # Koin modules (Network, Data, Domain, Presentation, AppModule)
│   │   ├── domain/
│   │   │   ├── model/                # Pure Kotlin entities (LotteryDraw, Jackpot, Money, Currency, LotteryResult)
│   │   │   ├── repository/           # LotteryRepository abstraction (suspend API)
│   │   │   └── usecase/              # GetLotteryResultsUseCase, LoadFixtureResultsUseCase
│   │   └── presentation/
│   │       ├── formatter/            # CurrencyFormatter (Money -> MIO €), LotteryDateTimeFormatter
│   │       ├── mapper/               # Domain to UI mappers (LotteryUiMapper)
│   │       ├── model/                # Stable UI models (LotteryCardUiModel, BallUiModel, BallUiType)
│   │       ├── state/                # DrawResultsUiState (Loading, Success, Error)
│   │       ├── theme/                # Aura Lotto Design System (AuraColors, AuraTypography, AuraSpacing)
│   │       ├── ui/
│   │       │   ├── component/        # Atomic components (LotteryBall, JackpotBadge, NextDrawPill, LotteryCard)
│   │       │   ├── preview/          # Light and Dark Compose previews
│   │       │   └── screen/           # DrawResultsScreen (Pull-to-refresh, cards, error/fixture actions)
│   │       └── viewmodel/            # DrawResultsViewModel (UDF, state guard, 404 checks)
│   ├── src/iosMain/kotlin/           # iOS MainViewController with idempotent Koin init
│   └── src/commonTest/kotlin/        # Unit test suite covering domain, data, formatters, and ViewModel
```

---

## Build & Test Commands

### Running Unit Tests
```bash
./gradlew :shared:testAndroidHostTest
```
*Current test suite: **46 unit tests passing** (verified via `./gradlew :shared:testAndroidHostTest --rerun-tasks`), covering:*
* `LotteryRepositoryImplTest`: live fetch, in-memory caching, force refresh, 404 error handling, network loss retention, and provenance preservation across cache hits.
* `DrawResultsViewModelTest`: initial loading, live success, pull-to-refresh flags, `canLoadFixture` guards (`true` strictly for 404, `false` for 500 and offline errors), and re-entrancy protection.
* `LotteryUiMapperTest`: LOTTO 6aus49 ball derivation, Eurojackpot euro numbers, Helsinki vs Berlin timezone boundary mapping, and null safety.
* `CurrencyFormatterTest`: German millions formatting (`"80 MIO €"`), badge formatting (`"80 MIO"`), German decimal commas (`"1,5 MIO €"`), sub-millions (`"950.000 €"`), and safe fallbacks.
* `LotteryDateTimeFormatterTest`: German weekday abbreviations, leap year handling, and timezone day boundaries.
* `KoinModuleTest`: automated dependency graph resolution validation.

### Building the Android App
```bash
./gradlew :androidApp:assembleDebug
```

### Compiling Shared Code for iOS
```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
```

---

## Discussion Points & Architectural Trade-offs

1. **In-Memory Cache vs. SQLite (Room / SQLDelight):**
   * *Decision:* For an app querying two lottery draw cards, an SQLite database introduces unnecessary schema migrations, disk I/O, and driver overhead. An in-memory cache retaining the session's latest successful result fully satisfies the offline requirement (refresh failure keeps showing existing numbers with a warning banner) without persistent disk bloat.
2. **Explicit Fixture Fallback strictly on 404:**
   * *Decision:* Silent fallback to 2023 sample data is hazardous in lottery apps, where users verify actual monetary tickets. On 404, the app explicitly communicates the status and offers a user-initiated button to load sample data with a clear banner.
3. **Decoupled Presentation Models & Value Objects:**
   * *Decision:* Domain entities do not import `@Immutable` or Compose dependencies. Value objects like `Money` encapsulate financial integrity, while presentation formatters handle localized rendering (`"MIO"`, `"€"`).
