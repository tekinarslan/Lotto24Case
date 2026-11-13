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

### 1. Pure Kotlin Clean Architecture
* **Domain Layer (`:shared/domain`)**: Contains zero Android, Compose, or framework dependencies. Consists of pure Kotlin entities (`LotteryDraw`, `Jackpot`, `BallType`), repository contracts (`LotteryRepository`), and focused one-shot suspend use cases (`GetLotteryResultsUseCase`, `LoadFixtureResultsUseCase`).
* **Single Source of Truth**: The domain model carries only the raw drawn numbers (`numbers`, `superNumber`, `euroNumbers`). Derived presentation lists (`balls: ImmutableList<BallUiModel>`) are generated strictly within the presentation mapper (`LotteryUiMapper`) to eliminate state duplication.
* **Strict Presentation Decoupling**: UI models (`LotteryCardUiModel`, `BallUiModel`, `BallUiType`, `SupplementaryGameUiModel`) are fully separated from domain entities and annotated with `@Immutable` alongside `ImmutableList` for Compose recomposition stability.
* **One-Shot Suspend over Unnecessary Flow**: Repository and UseCase retrieval functions are modeled as idiomatic `suspend fun` rather than wrapping one-shot REST operations into artificial `Flow` streams. Active `Job` cancellation on user retry/refresh eliminates duplicate network requests.

### 2. Data Provenance & In-Memory State Retention
* **Explicit Outcome (`LotteryResult`)**:
  * `Success(data, isFixture)`: Identifies whether data came from the live network endpoint or sample PDF fixtures.
  * `Error(cause, statusCode, cachedData, isCachedDataFixture)`: Preserves previous session draws if a network failure occurs on refresh, enabling the UI to show an offline warning banner rather than wiping the screen.
* **No Silent Fallbacks**: If the legacy endpoint returns HTTP 404 or fails, the application does **not** silently disguise 2023 fixture data as live results. An explicit error state is emitted, allowing the user to demand sample fixtures via a *"Beispieldaten laden"* button.
* **Cache Provenance Preservation**: The session cache (`LotteryRepositoryImpl`) stores both data and origin (`CacheEntry(data, isFixture)`). If a user loads sample fixture data, subsequent cache reads retain `isFixture = true`, ensuring sample data is never mistaken for live winning numbers.

### 3. Timezone Awareness
* Draw times and cutoffs operate in different native timezones:
  * **LOTTO 6aus49**: `Europe/Berlin` (UTC+1 / UTC+2 DST)
  * **Eurojackpot**: `Europe/Helsinki` (UTC+2 / UTC+3 DST)
* The domain model preserves native timezone identifiers (`draw.timeZone`), and `LotteryUiMapper` explicitly resolves the target zone via `LotteryDateTimeFormatter.resolveTimeZone()`. This prevents UTC day-boundary errors (e.g. draws occurring at 22:30 UTC on Friday are Saturday in Helsinki but Friday in Berlin).

### 4. Unidirectional Data Flow (UDF) & Koin DI
* `DrawResultsViewModel` exposes a single `StateFlow<DrawResultsUiState>` (`Loading`, `Success`, `Error`).
* **Active Job Guards**: Pull-to-refresh and manual retry cancel active in-flight coroutines (`fetchJob?.cancel()`) to prevent parallel execution and race conditions.
* **Koin 4.x ViewModel DSL**: Registered with `viewModelOf(::DrawResultsViewModel)` in `PresentationModule`.

---

## Tech Stack

| Component | Library / Framework | Version |
| :--- | :--- | :--- |
| **Language** | Kotlin Multiplatform | `2.1.20-RC` / `2.1.0` |
| **UI Framework** | Compose Multiplatform (CMP) | `1.8.0-alpha01` |
| **Dependency Injection** | Koin Core & ViewModel Compose | `4.0.2` |
| **Networking** | Ktor Client (ContentNegotiation, Logging, Timeouts) | `3.1.1` |
| **Serialization** | kotlinx-serialization-json | `1.8.0` |
| **Date & Time** | kotlinx-datetime | `0.6.2` |
| **Immutable Collections** | kotlinx-collections-immutable | `0.3.8` |
| **Reactive Streams** | kotlinx-coroutines | `1.10.1` |
| **Testing** | kotlin-test, Turbine, Coroutines Test | `1.2.0` |
| **Android AppWidget** | Jetpack Glance | `1.1.1` |

---

## Project Structure

```
├── androidApp/                       # Android application module (launcher, Glance AppWidget)
├── docs/                             # Interactive HTML documentation dashboard
│   ├── index.html                    # Dashboard single-page app
│   ├── requirements.html             # PDF requirements transcript
│   ├── tech-stack.html               # Technical stack documentation
│   └── roadmap.html                  # Live phase tracking
├── shared/
│   ├── src/commonMain/kotlin/de/lotto24case/
│   │   ├── data/
│   │   │   ├── mapper/               # DTO to Domain mappers (LotteryDtoMapper)
│   │   │   ├── remote/               # Ktor client, DTOs, fixtures, LotteryDataResult
│   │   │   └── repository/           # LotteryRepositoryImpl (in-memory cache & provenance)
│   │   ├── di/                       # Koin modules (Network, Data, Domain, Presentation)
│   │   ├── domain/
│   │   │   ├── model/                # Pure Kotlin entities (LotteryDraw, Jackpot, BallType, LotteryResult)
│   │   │   ├── repository/           # LotteryRepository abstraction
│   │   │   ├── usecase/              # GetLotteryResultsUseCase, LoadFixtureResultsUseCase
│   │   │   └── util/                 # CurrencyFormatter, LotteryDateTimeFormatter
│   │   └── presentation/
│   │       ├── mapper/               # Domain to UI mappers (LotteryUiMapper)
│   │       ├── model/                # Stable UI models (LotteryCardUiModel, BallUiModel, BallUiType)
│   │       ├── state/                # DrawResultsUiState (Loading, Success, Error)
│   │       ├── theme/                # Design system colors, typography, shapes
│   │       ├── ui/                   # Compose atomic components (LotteryBall, LotteryCard, Badges)
│   │       └── viewmodel/            # DrawResultsViewModel with UDF
│   └── src/commonTest/kotlin/        # 41 unit tests covering domain, data, formatters, and ViewModel
```

---

## Build & Test Commands

### Running Unit Tests
```bash
./gradlew :shared:testAndroidHostTest
```
*Current test suite: **41 unit tests passing**, covering:*
* `LotteryRepositoryImplTest`: live fetch, in-memory caching, force refresh, 404 error, network loss retention, and provenance preservation across cache hits.
* `DrawResultsViewModelTest`: initial loading, live success, pull-to-refresh flags, `canLoadFixture` guards (true for 404, false for 500/offline), and in-flight job cancellation.
* `LotteryUiMapperTest`: 6aus49 ball derivation, Eurojackpot euro numbers, Helsinki vs Berlin timezone boundary mapping, and null safety.
* `CurrencyFormatterTest`: German millions formatting (`"80 MIO €"`), badge formatting (`"80 MIO"`), German decimal commas (`"1,5 MIO €"`), sub-millions (`"950.000 €"`), and safe fallbacks.
* `LotteryDateTimeFormatterTest`: German weekday abbreviations, leap year handling, and timezone day boundaries.
* `KoinModuleTest`: automated dependency graph resolution validation.

### Building the Android App
```bash
./gradlew :androidApp:assembleDebug
```

---

## Discussion Points & Architectural Trade-offs

1. **In-Memory Cache vs. SQLite (Room / SQLDelight):**
   * *Decision:* For an app querying two lottery draw cards, an SQLite database introduces unnecessary schema migrations, disk I/O, and driver overhead. An in-memory cache retaining the session's latest successful result fully solves the offline requirement (refresh failure keeps showing existing numbers with a warning banner) without persistent disk bloat.
2. **Explicit Fixture Fallback on 404:**
   * *Decision:* Silent fallback to 2023 sample data is hazardous in lottery apps, where users verify actual monetary tickets. On 404, the app explicitly communicates the status and offers a user-initiated button to load sample data with a clear banner.
3. **Decoupled Presentation Models:**
   * *Decision:* Domain entities do not import `@Immutable` or Compose dependencies. UI models are independently defined and stable, preserving clean architectural boundaries.
