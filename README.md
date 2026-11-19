# This is an experiment with [FlowRedux](https://github.com/freeletics/FlowRedux) and [Molecule](https://github.com/cashapp/molecule)

## Building the Project

A comprehensive build script `build.sh` is provided in the project root:

```bash
# Display help and available options
./build.sh --help

# Run full pipeline: clean, stability check, lint, tests, and assemble
./build.sh full

# Build debug APK
./build.sh debug

# Build release APK and App Bundle (AAB)
./build.sh release

# Run all quality checks (stability, lint, unit tests)
./build.sh check

# Run unit tests only
./build.sh test
```

## Project Structure

```
NoteMark
├── app                        # Application module: MainActivity, Metro DI graph (NoteMarkGraph),
│                              # Navigation 3 root (NoteMarkRoot), deep links, WorkManager sync
├── build-logic/convention     # Gradle convention plugins (android app/library, compose, jvm library)
│
├── common-core                # Pure JVM: coroutine/Flow helpers, date & string extensions, DI qualifiers
├── common-android             # Android helpers: connectivity, sync scheduler, app shortcuts, status bar
├── common-compose             # Design system: theme, colors, typography, shared UI components
├── common-data                # Data layer: repositories, SQLDelight (notes), Proto DataStore (user, sync),
│                              # Ktor HTTP client & NoteMark API
├── common-navigation-api      # Navigation 3 NavKeys and the EntryInstaller contract
├── common-state-event-api     # App-wide AppState / AppAction / Event contracts
├── common-state-event-impl    # AppStateMachineFactory (FlowRedux) and the singleton AppStateStore
├── common-presenter           # AppPresenter over AppStateStore (not yet consumed by other modules)
│
├── feat-launcher              # Splash / routing based on login state
├── feat-login                 # Login screen
├── feat-registration          # Registration screen
├── feat-notelist              # Paged note list, delete dialog, list-detail pane
├── feat-addnote               # Create note
├── feat-editnote              # Edit note
└── feat-settings              # Sync interval, sync now, logout
```

Every feature module follows the same shape:

| File | Role |
|---|---|
| `*EntryInstaller` | Registers the screen's `NavKey` entry; contributed to the DI graph via `@ContributesIntoSet(AppScope::class)` |
| `*Entry` | Composable that runs the presenter with Molecule and wires navigation callbacks |
| `*Pane` | Stateless UI that renders the `UiModel` and emits actions |
| `*Presenter` | `@Composable` presenter that turns state-machine state into a `UiModel` and forwards actions |
| `*StateMachineFactory` | FlowRedux state machine holding the screen's business logic |

## Data Flow

Unidirectional: the UI sends actions down, state flows back up.

**Legend:** double circle = lives in `:app`, circle = lives in a feature module, rectangle = lives in a common module, stadium = the user, cylinder = external backend.

```mermaid
flowchart TB
    User([User])

    subgraph UI["Feature module (UI)"]
        Pane(("*Pane<br/>(stateless Compose UI)"))
        Entry(("*Entry<br/>(launchMolecule)"))
        Presenter(("*Presenter<br/>(@Composable uiModel())"))
    end

    subgraph SM["State machines (FlowRedux)"]
        FeatureSM(("Feature StateMachine<br/>(Login, NoteList, AddNote, …)"))
        AppStore["AppStateStore<br/>(app-wide AppStateMachine, singleton)"]
    end

    subgraph Data["common-data"]
        Repos["Repositories<br/>NoteMark · User · Sync"]
        Api["NoteMarkApi<br/>(Ktor client)"]
        Local["NoteMarkLocalDataSource<br/>(SQLDelight)"]
        Store["UserDataSource / NoteSyncDataSource<br/>(Proto DataStore)"]
    end

    Remote[(NoteMark backend)]
    Android["Android services<br/>Connectivity · Shortcuts · SyncScheduler"]
    Worker((("NoteSyncWorker → NoteSyncer<br/>(WorkManager, app module)")))

    User -- "tap / type" --> Pane
    Pane -- "Action" --> Presenter
    Presenter -- "dispatch(Action)" --> FeatureSM
    Presenter -- "dispatch(AppAction)" --> AppStore
    FeatureSM --> Repos
    FeatureSM --> Api
    AppStore --> Repos
    AppStore --> Android
    Repos --> Local
    Repos --> Store
    Repos --> Api
    Api <--> Remote
    Worker --> Repos

    Local -. "Flow / PagingData" .-> Repos
    Store -. "Flow<User>, Flow<Sync>" .-> Repos
    Repos -. "Flow" .-> FeatureSM
    Repos -. "Flow" .-> AppStore
    FeatureSM -. "StateFlow<State>" .-> Presenter
    AppStore -. "StateFlow<AppState>" .-> Presenter
    Presenter -. "UiModel" .-> Entry
    Entry -. "UiModel" .-> Pane
    Pane -. "navigate" .-> Nav((("NavBackStack<br/>(Navigation 3, app module)")))
```

- **Solid arrows**: actions and calls flowing down.
- **Dotted arrows**: state flowing back up as `Flow` / `StateFlow`.
- `AppStateStore` is the single app-wide state (login status, connectivity, sync settings, one-shot `AppEvent`s) shared by `feat-launcher`, `feat-notelist` and `feat-settings`.
- Screen-specific logic lives in each feature's own `*StateMachineFactory`, created per presenter via `launchIn(scope)`.

## Module Dependency Diagram

Project-module `implementation` dependencies (test-only dependencies omitted).

**Legend:** double circle = application module, circle = feature module, rectangle = common/library module.

```mermaid
flowchart TD
    app(((":app")))

    subgraph features["Feature modules"]
        launcher((":feat-launcher"))
        login((":feat-login"))
        registration((":feat-registration"))
        notelist((":feat-notelist"))
        addnote((":feat-addnote"))
        editnote((":feat-editnote"))
        settings((":feat-settings"))
    end

    subgraph common["Common modules"]
        presenter[":common-presenter"]
        seImpl[":common-state-event-impl"]
        seApi[":common-state-event-api"]
        nav[":common-navigation-api"]
        data[":common-data"]
        compose[":common-compose"]
        android[":common-android"]
        core[":common-core"]
    end

    app --> features
    app --> seImpl & seApi & nav & data & compose & android & core

    launcher --> android & compose & data & nav & seApi & seImpl
    notelist --> android & compose & core & data & nav & seApi & seImpl
    settings --> android & compose & core & data & nav & seApi & seImpl
    login --> android & compose & core & data & nav
    registration --> android & compose & core & data & nav
    addnote --> android & compose & core & data & nav
    editnote --> android & compose & core & data & nav

    presenter --> seApi & seImpl
    seImpl --> android & core & data & seApi
    seApi --> android
    data --> core
```

Key points:

- `:app` is the only module that depends on feature modules. Features never depend on each other; they navigate through `NavKey`s defined in `:common-navigation-api`.
- Feature navigation entries reach `:app` through Metro multibindings (`Set<EntryInstaller>`), not through direct references.
- `:common-core` is a pure JVM module at the bottom of the graph.
- Build configuration is shared through the convention plugins in `build-logic` (`notemark.android.application`, `notemark.android.library`, `notemark.android.compose`, `notemark.jvm.library`).
