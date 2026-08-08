# Requirements

### Overview & Goals
Migrate the dependency injection framework from Koin to Metro 1.4.0 to leverage compile-time safety and KSP-based code generation.

### Scope
- **In Scope**:
    - Adding Metro dependencies and KSP configuration.
    - Defining a central `NoteMarkGraph`.
    - Migrating all service, repository, and UI-related dependencies.
    - Implementing assisted injection for components with runtime parameters (`EditNotePresenter`, `EditNoteStateMachineFactory`).
    - Integrating Metro with WorkManager via a custom `WorkerFactory`.
    - Integrating Metro with Compose via `CompositionLocal`.
    - Complete removal of Koin.
- **Out of Scope**:
    - Functional changes to the app's business logic.
    - Refactoring state management (FlowRedux/Molecule).

### Functional Requirements
- The app must continue to function as before, with all features (login, note list, edit note, sync) operational.
- Presenters must be correctly scoped and retained during recomposition where applicable (using `retain`).
- `NoteSyncWorker` must correctly inject its dependencies via Metro.

# Technical Design

### Current Implementation
- Uses Koin for DI, defined in `AppModule.kt`.
- Uses `single`, `factory`, and `worker` definitions.
- Uses named qualifiers for `DataStore` and `CoroutineDispatcher`.
- Uses `parametersOf` for runtime parameters (e.g., `noteId` for `EditNotePresenter`).
- Injected in Compose using `KoinJavaComponent.get()` within `EntryBuilders`.

### Key Decisions
- **Single App-wide Graph**: A single `NoteMarkGraph` will manage all dependencies for simplicity.
- **Assisted Injection**: Metro's `@AssistedInject` and `@AssistedFactory` will be used for `EditNotePresenter` and `EditNoteStateMachineFactory` to handle `noteId` parameter.
- **CompositionLocal**: The graph will be provided to the Compose tree via `LocalNoteMarkGraph` to allow easy access in `EntryBuilders`.
- **Custom WorkerFactory**: A `MetroWorkerFactory` will be implemented to support injecting dependencies into `NoteSyncWorker`.

### Proposed Changes
- **Dependency Definitions**:
    - Annotate implementation classes with `@Inject constructor`.
    - Use `@Provides` in a `@DependencyGraph` or `@Module` for third-party instances (Ktor `HttpClient`, SQLDelight `SqlDriver`, `DataStore`).
- **Qualifiers**:
    - Define custom annotations like `@AppBackgroundDispatcher`, `@AppBackgroundScope`, `@UserDataStore`, and `@SyncDataStore` to replace Koin's `named()`.
- **Compose Integration**:
    - Wrap the root Composable in `MainActivity` with `CompositionLocalProvider(LocalNoteMarkGraph provides graph)`.
    - Update `EntryBuilders` to use `LocalNoteMarkGraph.current` to retrieve presenters.

### Architecture Diagram
```mermaid
graph TD
    App[NoteMarkApp] -->|Initialize| Graph[NoteMarkGraph]
    Graph -->|Provide| WorkerFactory[MetroWorkerFactory]
    WorkerFactory -->|Instantiate| Worker[NoteSyncWorker]
    
    MainActivity -->|CompositionLocal| Graph
    
    subgraph Compose UI
        EntryBuilder[NoteListEntryBuilder] -->|Access| Graph
        EntryBuilder -->|Get| Presenter[NoteListPresenter]
    end
    
    subgraph Data Layer
        Graph --> Repository[NoteMarkRepositoryImpl]
        Repository --> LocalDS[NoteMarkLocalDataSourceImpl]
        Repository --> RemoteDS[NoteMarkApiDataSourceImpl]
    end
```

### File Structure Changes
- **Added**:
    - `app/src/main/java/com/dhimandasgupta/notemark/app/di/NoteMarkGraph.kt`: The main Metro graph definition.
    - `app/src/main/java/com/dhimandasgupta/notemark/app/di/Qualifiers.kt`: Custom qualifier annotations.
    - `app/src/main/java/com/dhimandasgupta/notemark/app/di/MetroWorkerFactory.kt`: Custom `WorkerFactory` for Metro.
- **Modified**:
    - `app/src/main/java/com/dhimandasgupta/notemark/app/NoteMarkApp.kt`: Initialize Metro graph and WorkManager.
    - `app/src/main/java/com/dhimandasgupta/notemark/ui/activity/MainActivity.kt`: Provide graph via `CompositionLocalProvider`.
    - All `Entry.kt` files in `features/`: Retrieve presenters from Metro graph.
    - All Presenters, Repositories, and DataSources: Add `@Inject constructor()` and remove Koin imports.
    - `NoteSyncWorker.kt`: Use constructor injection.
- **Removed**:
    - `app/src/main/java/com/dhimandasgupta/notemark/app/di/AppModule.kt`: No longer needed.

# Plan

### ✓ Step 1: Add Metro dependencies and configuration
- Update `gradle/libs.versions.toml` to include `metro` version 1.4.0 and the corresponding KSP plugin.
- Add `metro-runtime` and `metro-compiler` (KSP) to `app/build.gradle.kts`.
- Ensure the KSP plugin is correctly applied in the `plugins` block.

### ✓ Step 2: Implement Metro Graph and migrate core dependencies
- Create `NoteMarkGraph.kt` annotated with `@DependencyGraph`.
- Define custom `@Qualifier` annotations for named dependencies: `@AppBackgroundDispatcher`, `@AppBackgroundScope`, `@UserDataStore`, and `@SyncDataStore`.
- Implement Metro modules for `HttpClient`, `Database`, and `DataStore` providers, migrating logic from `AppModule.kt`.
- Add `@Inject` to constructors of Repositories and DataSources.
- Define `@Binds` or `@Provides` in the Graph/Modules for all interfaces.

### ✓ Step 3: Migrate Presenters and StateMachine Factories
- Add `@Inject` constructor to all Presenters (`LauncherPresenter`, `LoginPresenter`, etc.).
- Update `EditNotePresenter` and `EditNoteStateMachineFactory` to use `@AssistedInject` with `noteId`.
- Define `@AssistedFactory` interfaces for assisted components.
- Migrate StateMachineFactories to use `@Inject` where possible.

### ✓ Step 4: Initialize Metro and integrate with WorkManager
- Update `NoteMarkApp.kt` to initialize the `NoteMarkGraph`.
- Implement `MetroWorkerFactory` to handle `NoteSyncWorker` injection via `@AssistedInject`.
- Update `NoteMarkApp.kt` to set the custom `WorkerFactory` in `WorkManager` configuration.
- Update `NoteSyncWorker.kt` to use constructor injection instead of Koin's `by inject`.

### ✓ Step 5: Integrate Metro with Compose UI
- Create `LocalNoteMarkGraph` using `staticCompositionLocalOf`.
- Update `MainActivity.kt` to wrap `NoteMarkRoot` with `CompositionLocalProvider(LocalNoteMarkGraph provides graph)`.
- Update all `EntryBuilder` functions (e.g., `LauncherEntryBuilder`, `NoteEditEntryBuilder`) to retrieve Presenters from the graph using `LocalNoteMarkGraph.current`.
- Remove all Koin `get()` and `parametersOf()` calls from the UI layer.

### ✓ Step 6: Finalize Koin removal and cleanup
- Delete `app/src/main/java/com/dhimandasgupta/notemark/app/di/AppModule.kt`.
- Remove Koin initialization from `NoteMarkApp.kt`.
- Remove Koin imports from all modified files.
- Remove Koin dependencies from `gradle/libs.versions.toml` and `app/build.gradle.kts`.