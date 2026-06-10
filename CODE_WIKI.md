# Komelia Code Wiki

## 1. Project Overview

**Komelia** is a multi-platform media client for [Komga](https://komga.org/) — a self-hosted media server for comics, manga, and books. Komelia is written primarily in **Kotlin** using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform** for the UI, with native components (C/C++) for image processing, ML inference, and webview rendering. It also ships two standalone **Web UI** projects for EPUB reading and a **browser extension** for the Komf metadata tool.

- **Version**: 0.17.0
- **License**: See project root
- **Supported Platforms**: Android (minSdk 26), Desktop (Linux / Windows), Web (WasmJs)
- **Primary Framework**: Kotlin 2.2.10, Compose Multiplatform 1.8.2, Voyager (navigation), Coil (image loading)
- **Native Dependencies**: libvips (image decode), OnnxRuntime (ML panel detection & upscaling), WebKit2GTK / WebView2 (EPUB webview)

### 1.1 Key Capabilities

- Browse libraries, series, books, collections, and read lists from a Komga server
- Image-based reader with continuous / paged / panel-reading modes, color correction, upscaling, panel detection
- EPUB reader using embedded web UIs rendered inside a native webview
- Multi-user login, cookie-based remember-me auth
- Offline mode with local SQLite persistence (and IndexedDB for Web)
- Plugin support for Komf (Kotlin metadata finder) via settings screens and a browser extension
- Desktop application packaging via MSI / DEB / uber JAR; Android APK / AAB

---

## 2. Overall Architecture

Komelia is a modular **Kotlin Multiplatform** project structured around a **core** module containing all shared UI and business logic, with thin platform-specific entry points (`komelia-app`). Heavy work (image decode, ML, webview) offloads to dedicated native modules exposed through expect/actual declarations.

```
┌──────────────────────────────────────────────────────────┐
│                      komelia-app                          │
│   (Android / Desktop / WasmJs entry points + packaging)   │
├──────────────────────────────────────────────────────────┤
│                      komelia-core                         │
│   UI (Compose), navigation (Voyager), state (view models),│
│   image pipeline, HTTP clients (komga-client, komf-client)│
├──────────────────────┬───────────────────┬────────────────┤
│     komelia-db       │  komelia-image-   │ komelia-onnx-  │
│  (SQLite / IndexedDB)│     decoder       │    runtime     │
├──────────────────────┼───────────────────┼────────────────┤
│    komelia-webview   │ komelia-jni       │ epub-reader-   │
│  (native webviews for│ (JNI helpers for  │   webui        │
│   EPUB rendering)    │   shared libs)    │ (Vue + Svelte) │
└──────────────────────┴───────────────────┴────────────────┘
                                  ▲
                                  │
                        komelia-komf-extension
                        (WasmJs browser extension)
```

### 2.1 Dependency Inversion & Dependency Container

All shared components expose their dependencies through the `DependencyContainer` interface in [komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/DependencyContainer.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/DependencyContainer.kt). Each platform (Android, Desktop, WasmJs) provides its own actual implementation, e.g. [DesktopDependencyContainer.kt](file:///workspace/komelia-core/src/jvmMain/kotlin/io/github/snd_r/komelia/DesktopDependencyContainer.kt).

The container wires:

- **Settings repositories** (`CommonSettingsRepository`, `EpubReaderSettingsRepository`, `ImageReaderSettingsRepository`, `KomfSettingsRepository`)
- **Secrets repository** (credentials/API keys, platform-specific)
- **Color correction & presets repositories**
- **User fonts repository**
- **HTTP clients**: `KomgaClientFactory`, `KomfClientFactory`
- **Image pipeline**: `ImageDecoder`, `BookImageLoader`, `ReaderImageFactory`, `ColorCorrectionStep`
- **ML inference**: `OnnxRuntime`, `KomeliaUpscaler`, `KomeliaPanelDetector`
- **UI helpers**: `AppWindowState`, `AppStrings` (i18n), `AppNotifications`

The `DependencyContainer` is threaded into screens via Compose `CompositionLocals` defined in [CompositionLocals.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/ui/CompositionLocals.kt).

### 2.2 Navigation & Screen Model

Navigation is implemented using **Voyager** (`cafe.adriel.voyager:voyager-navigator`, `voyager-screenmodel`).

- `MainScreen` ([MainScreen.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/ui/MainScreen.kt)) is the top-level screen with platform-aware layouts (bottom nav for mobile, side drawer + app bar for desktop).
- Screen models inherit patterns from `ViewModelFactory` ([ViewModelFactory.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/ViewModelFactory.kt)).
- `MainScreenViewModel` drives library/series/book state and manages the navigation drawer and search bar.

Key screens: `HomeScreen`, `LibraryScreen`, `SeriesScreen`, `BookScreen`, `ImageReaderScreen`, `EpubScreen`, `LoginScreen`, `SettingsScreen`.

---

## 3. Module Directory

### 3.1 `komelia-app/` — Platform Entry Points & Packaging

**Purpose**: Thin entry points per platform + packaging configuration (Android manifest, Compose Desktop application DSL, WasmJs webpack).

Key files:
- [build.gradle.kts](file:///workspace/komelia-app/build.gradle.kts) — KMP targets (android, jvm, wasmJs), Compose Desktop native distributions (MSI, DEB), Android build config.
- [src/androidMain/kotlin/snd/komelia/MainActivity.kt](file:///workspace/komelia-app/src/androidMain/kotlin/snd/komelia/MainActivity.kt) — Android `ComponentActivity`, sets up `Dependencies` and calls `MainView()`.
- [src/jvmMain/kotlin/io/github/snd_r/komelia/main.kt](file:///workspace/komelia-app/src/jvmMain/kotlin/io/github/snd_r/komelia/main.kt) — Desktop JVM entry point using `ComposeWindow`.
- [src/wasmJsMain/kotlin/io/github/snd_r/komelia/main.kt](file:///workspace/komelia-app/src/wasmJsMain/kotlin/io/github/snd_r/komelia/main.kt) — WasmJs entry point mounting `MainView()` into a DOM element.
- [src/androidMain/AndroidManifest.xml](file:///workspace/komelia-app/src/androidMain/AndroidManifest.xml) — Android manifest.

Notable: The `jvm` target uses `compose.desktop.application` DSL with ShenandoahGC JVM args for memory-efficient long sessions.

### 3.2 `komelia-core/` — Shared UI & Business Logic

**Purpose**: 90 % of the application lives here. Organized by feature area under `ui/`, plus technical areas (`image/`, `color/`, `settings/`, `platform/`, `updates/`, `http/`, `fonts/`, `offline/`, `strings/`).

Key sub-packages:

| Package | Role |
|---|---|
| `ui/` | All Compose screens, dialogs, view models |
| `ui/reader/image/` | Image book reader: paged, continuous, panels |
| `ui/reader/epub/` | EPUB reader shell embedding web UIs via `komelia-webview` |
| `ui/home/`, `ui/library/`, `ui/series/`, `ui/book/`, `ui/collection/`, `ui/readlist/` | Browsing screens |
| `ui/settings/` | Settings screens (appearance, reader, Komf, server, users, auth activity, updates) |
| `ui/color/` | Color correction editor UI (curves, levels, presets) |
| `ui/login/` | Komga server login / auth |
| `ui/common/` | Reusable composables (cards, thumbnails, dialogs, menus, pagination, theme) |
| `image/` | Image loading pipeline (Coil mappers, native decoding bridge, panel detector, upscaler, processing steps) |
| `color/` | Color math (lookup tables, histograms, curves, levels) |
| `settings/` | Repository interfaces for settings (expect — implemented by sqlite/wasm modules) |
| `platform/` | `expect` declarations for window state, back press, mouse, scrollbars, title bar, URL handling |
| `updates/` | App updater, OnnxRuntime installer, model downloader |
| `http/` | Persistent cookie store, User-Agent |
| `fonts/` | User-font management |
| `offline/` | In-memory mock Komga client implementation for offline mode |
| `strings/` | i18n string resources (English) |

**Image pipeline entry points**:
- [BookImageLoader.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/image/BookImageLoader.kt) — fetches pages / thumbnails from Komga and decodes them via `ImageDecoder`.
- [ReaderImageFactory.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/image/ReaderImageFactory.kt) — produces `ReaderImage` instances supporting lazy tiling, processing steps, Coil integration.
- [ImageProcessingPipeline.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/image/processing/ImageProcessingPipeline.kt) — composable steps: `ColorCorrectionStep`, `CropBordersStep`.
- [KomeliaPanelDetector.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/image/KomeliaPanelDetector.kt), [KomeliaUpscaler.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/image/KomeliaUpscaler.kt) — ML-based reading enhancements (expect/actual).

### 3.3 `komelia-db/` — Persistence Layer

**Purpose**: Local persistence (settings, color-correction presets, user fonts, Komf configuration). Two implementations:

- `sqlite/` — Desktop & Android, using **JetBrains Exposed** SQL DSL + **SQLite JDBC** driver, migrations via plain SQL files under `composeResources/files/migrations/` (Flyway-style version numbering).
- `wasm/` — Web target using browser **IndexedDB** + `LocalStorage`.

```
komelia-db/
├── shared/     ← abstract SettingsStateActor + repository interfaces
├── sqlite/     ← Exposed repositories + tables + SQL migrations
└── wasm/       ← IndexedDB + LocalStorage repositories
```

Key files:
- [sqlite/.../KomeliaDatabase.kt](file:///workspace/komelia-db/sqlite/src/commonMain/kotlin/snd/komelia/db/KomeliaDatabase.kt) — DB connection, migration runner.
- [sqlite/.../ExposedSettingsRepository.kt](file:///workspace/komelia-db/sqlite/src/commonMain/kotlin/snd/komelia/db/settings/ExposedSettingsRepository.kt) — implements `CommonSettingsRepository` (JSON-encoded setting map).
- [wasm/.../KomeliaDatabase.kt](file:///workspace/komelia-db/wasm/src/wasmJsMain/kotlin/snd/komelia/db/KomeliaDatabase.kt) — IndexedDB setup for Web.
- [shared/.../SettingsStateActor.kt](file:///workspace/komelia-db/shared/src/commonMain/kotlin/snd/komelia/db/SettingsStateActor.kt) — coroutine actor serializing settings access.

### 3.4 `komelia-image-decoder/` — Multi-Backend Image Decode

**Purpose**: Decode images (JPEG, PNG, WebP, HEIF, AVIF, TIFF, JPEG XL) in a platform-optimal way. Exposes a high-level `ImageDecoder` interface plus `KomeliaImage` model with transformation methods (`resize`, `crop`, `histogram`, `mapLookupTable`, `findTrim`).

Three backends:

| Backend | Target | Implementation |
|---|---|---|
| `vips/` (native) | Android, Desktop JVM | C binding to libvips via JNI. See [native/src/vips/komelia_vips.c](file:///workspace/komelia-image-decoder/vips/native/src/vips/komelia_vips.c). Android additionally uses [AndroidBitmap.kt](file:///workspace/komelia-image-decoder/vips/src/androidMain/kotlin/snd/komelia/image/AndroidBitmap.kt) + Skia path for final bitmap rendering. |
| `wasm-image-worker/` | WasmJs | Dedicated Web Worker ([Main.kt](file:///workspace/komelia-image-decoder/wasm-image-worker/src/wasmJsMain/kotlin/snd/komelia/image/wasm/Main.kt)) running an actor-based message loop for decode/resize/crop/histogram requests. Uses the browser Canvas API. |
| `shared/` | - | Defines the common `ImageDecoder` + `KomeliaImage` interfaces. |

The high-level Kotlin wrapper is [VipsImageDecoder.kt](file:///workspace/komelia-image-decoder/vips/src/commonMain/kotlin/snd/komelia/image/VipsImageDecoder.kt), with platform-specific actuals loading the matching JNI `.so`/`.dll`.

### 3.5 `komelia-onnxruntime/` — ML Inference

**Purpose**: Panel detection (Rf-DETR object detection model) and image upscaling. Two modules plus native code.

- `api/` — `expect` declarations: `OnnxRuntime`, `OnnxRuntimeRfDetr`, `OnnxRuntimeUpscaler`, `OnnxRuntimeExecutionProvider`, `DeviceInfo`.
- `jvm/` — JVM/Android `actual` implementations bridging to native via JNI. Library loading in `OnnxRuntimeSharedLibraries`.
- `native/` — C/C++ JNI wrappers:
  - [komelia_onnxruntime.c](file:///workspace/komelia-onnxruntime/native/src/onnxruntime/komelia_onnxruntime.c) — session creation, memory info, tensor I/O.
  - [komelia_ort_rf_detr.c](file:///workspace/komelia-onnxruntime/native/src/onnxruntime/komelia_ort_rf_detr.c) — panel detection inference.
  - [komelia_ort_upscaler.c](file:///workspace/komelia-onnxruntime/native/src/onnxruntime/komelia_ort_upscaler.c) — upscaling inference.
  - `device/komelia_enumerate_devices_*.c` — GPU enumeration backends (CUDA, ROCm, DXGI, Vulkan) for provider selection.

### 3.6 `komelia-webview/` — Native Webviews for EPUB

**Purpose**: Provides a Compose `Webview` composable wrapping platform webviews for rendering EPUB content (which is HTML/CSS/JS).

Targets:
- `jvmMain` — Linux (WebKit2GTK) and Windows (WebView2) via JNI.
- `androidMain` — AndroidX `WebView`.
- `wasmJsMain` — iframe-based `KomeliaWebview` for browser environments.

Native side:
- [native/src/webview2/komelia_webview_webview2.c](file:///workspace/komelia-webview/native/src/webview2/komelia_webview_webview2.c) — Windows backend.
- [native/src/webkit2gtk/komelia_webview_webkit2gtk.c](file:///workspace/komelia-webview/native/src/webkit2gtk/komelia_webview_webkit2gtk.c) — Linux backend, including a request interceptor extension to serve EPUB contents from Kotlin.

Also supports a `RequestInterceptor` to serve Komga book resources without round-tripping to the network.

### 3.7 `komelia-jni/` — Shared JNI Helpers

**Purpose**: Shared JNI utilities (`SharedLibrariesLoader`, `DesktopPlatform`) used by image-decoder/vips and onnxruntime/jvm to locate and load bundled `.so`/`.dll` files.

### 3.8 `epub-reader-webui/` — EPUB Web Readers (JavaScript)

Two separate npm/Vite projects bundled into the app as static resources and loaded inside a `komelia-webview` instance. They are not Kotlin code, they run entirely inside the webview.

#### 3.8.1 `komga-webui/` — Vue 3 + Vuetify EPUB reader

- **Framework**: Vue 3.4, Vuetify 3.6, `@d-i-t-a/reader` (forked version), TypeScript.
- **Build**: `npm run build` produces a single-file Vite bundle.
- **Entry point**: [src/main.ts](file:///workspace/epub-reader-webui/komga-webui/src/main.ts), root component [App.vue](file:///workspace/epub-reader-webui/komga-webui/src/App.vue).
- **Reader component**: [src/components/EpubReader.vue](file:///workspace/epub-reader-webui/komga-webui/src/components/EpubReader.vue) — main reading surface with TOC, settings, shortcuts.
- **Komga bridge**: [src/functions/readium.ts](file:///workspace/epub-reader-webui/komga-webui/src/functions/readium.ts) wraps the Readium-based reader library.
- **i18n**: [src/locales/en.json](file:///workspace/epub-reader-webui/komga-webui/src/locales/en.json).
- **Types**: [src/types/](file:///workspace/epub-reader-webui/komga-webui/src/types/) — Komga API types, reader settings DTOs.

#### 3.8.2 `ttu-ebook-reader/` — Svelte 5 EPUB reader

- **Framework**: Svelte 5, TypeScript, Tailwind CSS, RxJS.
- **Purpose**: Alternate EPUB reader with richer typography, settings, font support.
- **Entry point**: [src/App.svelte](file:///workspace/epub-reader-webui/ttu-ebook-reader/src/App.svelte).
- **Core reader**: [src/lib/components/book-reader/](file:///workspace/epub-reader-webui/ttu-ebook-reader/src/lib/components/book-reader/) — continuous and paginated reading components, bookmark manager, character-stats calculator.
- **Data layer**: [src/lib/data/](file:///workspace/epub-reader-webui/ttu-ebook-reader/src/lib/data/) — state stores, keybinds, logger, theme, view mode, blur mode.
- **Functions**: [src/lib/functions/](file:///workspace/epub-reader-webui/ttu-ebook-reader/src/lib/functions/) — book data loader, CSS parser, RxJS utilities, binary search.

Both readers communicate with the Komelia host via JavaScript <-> Kotlin bridges exposed by `komelia-webview`.

### 3.9 `komelia-komf-extension/` — Browser Extension for Komf

**Purpose**: A Chrome/Chromium-style extension that injects Komf UI controls into Komga and Kavita web UIs, enabling metadata identification jobs without opening the full Komelia app.

Targets (all WasmJs):

- `background/` — service worker.
- `content/` — content script injected into matched pages; renders Compose for Web UI into the page. Contains:
  - [komga/KomgaComponent.kt](file:///workspace/komelia-komf-extension/content/src/wasmJsMain/kotlin/snd/komelia/komga/KomgaComponent.kt) — Komga integration.
  - [kavita/KavitaComponent.kt](file:///workspace/komelia-komf-extension/content/src/wasmJsMain/kotlin/snd/komelia/kavita/KavitaComponent.kt) — Kavita integration.
  - [dialogs/](file:///workspace/komelia-komf-extension/content/src/wasmJsMain/kotlin/snd/komelia/dialogs/) — identify, reset, settings dialogs.
- `popup/` — browser action popup.
- `app/` — manifest + icons + HTML wiring for the extension bundle.
- `shared/` — Chrome extension API wrappers (`chrome.storage`, `chrome.runtime`, `chrome.scripting`) and cross-module messages.

---

## 4. Build System Overview

### 4.1 Gradle / Kotlin DSL

- Root [build.gradle.kts](file:///workspace/build.gradle.kts) defines:
  - `linux-x86_64_copyJniLibs`, `android-arm64_copyJniLibs`, `windows-x86_64_copyJniLibs` tasks that copy compiled native `.so`/`.dll` files into the JVM resources / Android `jniLibs` directory.
  - `komgaNpmBuild` and `ttsuNpmBuild` tasks that trigger the two Vite projects.
  - `buildWebui` task that syncs the resulting bundles to `komelia-core/src/commonMain/composeResources/files/` so they are bundled with the app.
  - `cmakeSystemDeps*` tasks for building native components from source using CMake + Ninja.
- [settings.gradle.kts](file:///workspace/settings.gradle.kts) — includes all KMP modules and `third_party/` dependencies.
- [gradle/libs.versions.toml](file:///workspace/gradle/libs.versions.toml) — centralized version catalog (Kotlin, Compose Multiplatform, Coil, Voyager, Exposed, ktor, okhttp, komga-client, komf-client, etc.).

### 4.2 CMake — Native Superbuild

- Root [CMakeLists.txt](file:///workspace/CMakeLists.txt) drives a **superbuild**: downloads and builds libvips, mozjpeg, libde265, libheif, dav1d, libjxl, highway, spng, webp, tiff, brotli, zlib, expat, glib, OnnxRuntime as external projects, then compiles Komelia's own JNI libraries against them.
- Per-platform `CMakeLists.txt` under each native module (`komelia-image-decoder/vips/native/`, `komelia-onnxruntime/native/`, `komelia-webview/native/`).
- Dockerfiles in the project root (`cmake/linux-x86_64.Dockerfile`, `cmake/android.Dockerfile`) provide reproducible build environments.

### 4.3 Web / Frontend Build

- `epub-reader-webui/komga-webui/` — Vite + Vue 3, `vite-plugin-vuetify`, `vite-plugin-singlefile` (for single-file bundle).
- `epub-reader-webui/ttu-ebook-reader/` — Vite + Svelte 5, `vite-plugin-singlefile`, Tailwind CSS.

---

## 5. Key Classes & Functions

### 5.1 Application Bootstrap

| Class/Function | File | Role |
|---|---|---|
| `DependencyContainer` (interface) | [DependencyContainer.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/DependencyContainer.kt) | Abstract DI contract |
| `AndroidDependencyContainer` | `komelia-core/src/androidMain/...` | Android implementation |
| `DesktopDependencyContainer` | `komelia-core/src/jvmMain/...` | Desktop implementation |
| `WasmDependencyContainer` | `komelia-core/src/wasmJsMain/...` | Web implementation |
| `MainActivity` | `komelia-app/src/androidMain/kotlin/snd/komelia/MainActivity.kt` | Android entry point |
| `main.kt` (desktop) | `komelia-app/src/jvmMain/...` | Compose Desktop `application` |
| `main.kt` (wasm) | `komelia-app/src/wasmJsMain/...` | Browser DOM mount |
| `MainView` | `komelia-core/src/commonMain/.../ui/MainView.kt` | Top-level composable (theme, composition locals, login gating, main screen) |

### 5.2 State & View Model Layer

| Class | File | Role |
|---|---|---|
| `ViewModelFactory` | [ViewModelFactory.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/ViewModelFactory.kt) | Creates all screen view models |
| `MainScreenViewModel` | [MainScreenViewModel.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/ui/MainScreenViewModel.kt) | App-wide nav state, search bar, refresh |
| `ReaderViewModel` | `komelia-core/src/commonMain/.../ui/reader/image/ReaderViewModel.kt` | Image reader state (book pages, mode, progress sync) |
| `EpubReaderViewModel` | `komelia-core/src/commonMain/.../ui/reader/epub/EpubReaderViewModel.kt` | EPUB reader state (webview lifecycle, read progress) |
| `LoginViewModel` | `komelia-core/src/commonMain/.../ui/login/LoginViewModel.kt` | Komga login flow |
| `SettingsStateActor` | [komelia-db/shared/.../SettingsStateActor.kt](file:///workspace/komelia-db/shared/src/commonMain/kotlin/snd/komelia/db/SettingsStateActor.kt) | Serializes settings writes with a coroutine actor |

### 5.3 Image Pipeline

| Class | File | Role |
|---|---|---|
| `ImageDecoder` (interface) | [komelia-image-decoder/shared/.../ImageDecoder.kt](file:///workspace/komelia-image-decoder/shared/src/commonMain/kotlin/snd/komelia/image/ImageDecoder.kt) | Common decoder contract |
| `VipsImageDecoder` | `komelia-image-decoder/vips/.../VipsImageDecoder.kt` | libvips-based decode |
| `WorkerImageDecoder` | `komelia-image-decoder/wasm-image-worker/.../client/WorkerImageDecoder.kt` | Web Worker-based decode |
| `BookImageLoader` | [BookImageLoader.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/image/BookImageLoader.kt) | Fetches + caches pages from Komga |
| `ReaderImageFactory` | [ReaderImageFactory.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/image/ReaderImageFactory.kt) | Produces `ReaderImage` instances |
| `ReaderImage` | `komelia-core/src/commonMain/.../image/ReaderImage.kt` | Lazily-decoded image surface with transformations |
| `TilingReaderImage` | `komelia-core/src/commonMain/.../image/TilingReaderImage.kt` | Tile-based decode for large pages |
| `ImageProcessingPipeline` | `komelia-core/src/commonMain/.../image/processing/ImageProcessingPipeline.kt` | Composable processing steps |
| `ColorCorrectionStep` | `komelia-core/src/commonMain/.../image/processing/ColorCorrectionStep.kt` | Applies curves/levels LUTs |
| `CropBordersStep` | `komelia-core/src/commonMain/.../image/processing/CropBordersStep.kt` | Trims whitespace borders |
| `KomeliaUpscaler` | `komelia-core/src/commonMain/.../image/KomeliaUpscaler.kt` | ML upscaler (expect/actual) |
| `KomeliaPanelDetector` | `komelia-core/src/commonMain/.../image/KomeliaPanelDetector.kt` | Panel detector (expect/actual) |

### 5.4 Color Math

| Class | File | Role |
|---|---|---|
| `ChannelsLut` | `komelia-core/src/commonMain/.../color/ChannelsLut.kt` | Per-channel lookup table |
| `Histogram` | `komelia-core/src/commonMain/.../color/Histogram.kt` | Histogram computation |
| `Curve` / `CurvePoints` | `komelia-core/src/commonMain/.../color/Curve.kt` | Curve control points |
| `Levels` / `LevelsPoints` | `komelia-core/src/commonMain/.../color/Levels.kt` | Levels adjustment |
| `Preset` | `komelia-core/src/commonMain/.../color/Preset.kt` | Preset model (curves + levels) |

### 5.5 EPUB Rendering

| Class / Component | File | Role |
|---|---|---|
| `EpubScreen` | `komelia-core/src/commonMain/.../ui/reader/epub/EpubScreen.kt` | Compose screen wrapping webview |
| `EpubReaderViewModel` | `komelia-core/src/commonMain/.../ui/reader/epub/EpubReaderViewModel.kt` | Webview lifecycle + read progress |
| `KomeliaWebview` (Compose) | `komelia-webview/src/commonMain/.../compose/Webview.kt` | Cross-platform webview composable (expect) |
| `KomeliaWebview` (Jvm) | `komelia-webview/src/jvmMain/.../Webview.jvm.kt` | Desktop actual |
| `KomeliaWebview` (Android) | `komelia-webview/src/androidMain/.../Webview.android.kt` | Android actual |
| `RequestInterceptor` | `komelia-webview/src/commonMain/.../RequestInterceptor.kt` | Intercepts resource requests to serve EPUB contents |

### 5.6 Komf Extension

| Class | File | Role |
|---|---|---|
| `MediaServerComponent` | `komelia-komf-extension/content/.../MediaServerComponent.kt` | Dispatches to Komga/Kavita UI injection |
| `KomgaComponent` | `komelia-komf-extension/content/.../komga/KomgaComponent.kt` | Komga page injection |
| `KavitaComponent` | `komelia-komf-extension/content/.../kavita/KavitaComponent.kt` | Kavita page injection |
| `IdentifyDialog`, `ResetMetadataDialog`, `SettingsDialog` | `komelia-komf-extension/content/.../dialogs/` | Extension UI dialogs |
| `OriginPermissions` | `komelia-komf-extension/shared/.../OriginPermissions.kt` | Host permission management |

---

## 6. Dependency Graph (Intra-Project)

```
komelia-app ──► komelia-core
            └─► komelia-db/{sqlite,wasm}
            └─► komelia-image-decoder/{vips,wasm-image-worker}
            └─► komelia-onnxruntime/jvm
            └─► komelia-webview

komelia-core ──► komelia-image-decoder/shared
             └─► komelia-onnxruntime/api
             └─► komelia-webview
             └─► komelia-db/shared         (via komelia-app target wiring)

komelia-db/shared   <- sqlite
                    <- wasm

komelia-image-decoder/shared  <- vips
                              <- wasm-image-worker

komelia-onnxruntime/api  <- jvm
                         <- native (CMake only, invoked from jvm via JNI)

epub-reader-webui/{komga-webui,ttu-ebook-reader}
        ─────► static assets copied into komelia-core composeResources

komelia-komf-extension/{background,content,popup,app,shared}
        ─────► pure WasmJs, no dependency on other Kotlin modules
                   (they share HTTP + Komf types with komelia-core via common libs)
```

### 6.1 Key External Dependencies

| Dependency | Used for |
|---|---|
| `io.github.snd-r:komga-client` | Typed Komga REST client (auth, libraries, series, books, read progress, tasks) |
| `io.github.snd-r.komf:client` | Komf metadata service REST client |
| `io.coil-kt.coil3:coil-compose` | Async image loading in Compose |
| `cafe.adriel.voyager:voyager-*` | Screen-based navigation, screen model, transitions |
| `io.ktor:ktor-client-*` | HTTP transport, content negotiation |
| `com.squareup.okhttp3:okhttp` | JVM/Android HTTP engine |
| `org.jetbrains.exposed:exposed-*` | SQL DSL for settings DB |
| `org.xerial:sqlite-jdbc` | SQLite driver |
| `org.jetbrains:markdown` | Markdown rendering in Compose |
| `com.mohamedrejeb.richeditor:richeditor-compose` | Rich text editor for metadata editing |
| `sh.calvin.reorderable:reorderable` | Drag-and-drop reordering |
| `com.fleeksoft.ksoup:ksoup` | HTML parsing (book description rendering) |

---

## 7. How to Run & Build the Project

### 7.1 Prerequisites

- **JDK 17** or higher (JDK 21 recommended for desktop)
- **Android SDK** (for Android target) with NDK installed
- **Node.js 18+** and **npm** (for EPUB web UIs)
- **CMake 3.25+**, **Ninja**, **Meson**, **Make** (for native superbuild)
- **Docker** (recommended for reproducible native builds — see `cmake/*.Dockerfile`)

### 7.2 Building Native Dependencies

The recommended path is a Docker superbuild. See instructions in root [README.md](file:///workspace/README.md):

```bash
# Example: build native libs for desktop Linux
docker build -t komelia-build-linux-x86_64 . -f ./cmake/linux-x86_64.Dockerfile
docker run -v "$(pwd):/build" komelia-build-linux-x86_64

# Then copy resulting libs into the Gradle resources
./gradlew linux-x86_64_copyJniLibs
```

If you have the native toolchain installed locally, you can try:

```bash
./gradlew komeliaBuildNonJvmDependencies
```

which runs `cmakeSystemDepsConfigure`, `cmakeSystemDepsBuild`, `cmakeSystemDepsCopyJniLibs`, and `buildWebui`.

### 7.3 Building the EPUB Web UIs

```bash
# Both are invoked automatically by:
./gradlew buildWebui

# Or individually:
(cd epub-reader-webui/komga-webui && npm install && npm run build)
(cd epub-reader-webui/ttu-ebook-reader && npm install && npm run build)
```

Output is synced to `komelia-core/src/commonMain/composeResources/files/` so it ships with the app.

### 7.4 Running the Desktop App (Development)

```bash
./gradlew :komelia-app:run
```

### 7.5 Packaging the Desktop App

```bash
# DEB (Linux)
./gradlew :komelia-app:packageReleaseDeb

# MSI (Windows — typically built on Windows or cross container)
./gradlew :komelia-app:packageReleaseMsi

# Single-file repackaged uber JAR
./gradlew :komelia-app:repackageUberJar
```

Outputs:
- `komelia-app/build/compose/binaries/` — DEB / MSI
- `komelia-app/build/compose/jars/` — uber JAR

### 7.6 Building Android

```bash
# Debug APK
./gradlew :komelia-app:assemble

# Release APK (unsigned)
./gradlew :komelia-app:assembleRelease
```

Outputs:
- `komelia-app/build/outputs/apk/debug/` — debug APK
- `komelia-app/build/outputs/apk/release/` — release APK

### 7.7 Building the Komf Browser Extension

```bash
./gradlew :komelia-komf-extension:app:packageExtension
```

Output: `komelia-komf-extension/app/build/distributions/` — unpack and load as an unpacked extension in Chromium-based browsers (developer mode).

### 7.8 Building the Web (WasmJs) App

```bash
./gradlew :komelia-app:wasmJsBrowserDistribution
```

Output: `komelia-app/build/dist/wasmJs/productionExecutable/` — static site you can serve with any HTTP server.

---

## 8. Data Flow: Reading a Book

1. User navigates `HomeScreen` → `LibraryScreen` → `SeriesScreen` → `BookScreen`. These screens use `KomgaClientFactory` (HTTP) to fetch data, exposed via view models.
2. User opens a book → `ImageReaderScreen` or `EpubScreen` is pushed onto the Voyager navigator.
3. **Image reader**: `BookImageLoader` requests page bytes from Komga via HTTP, streams them to `ImageDecoder` (platform backend: libvips / Web Worker). `ReaderImage` wraps the decoded bitmap, applies `ImageProcessingPipeline` (color correction, optional border crop), and renders with a custom `ScalableContainer` / `Canvas` composable. Optionally `KomeliaPanelDetector` (OnnxRuntime) provides panel rectangles; `KomeliaUpscaler` (OnnxRuntime) upscales the image.
4. **EPUB reader**: `EpubScreen` mounts `KomeliaWebview` pointing at the bundled `epub-reader-webui` static site (Vue or Svelte variant, depending on user settings). The web UI fetches EPUB contents through `RequestInterceptor` which proxies to Komga via Kotlin HTTP clients. Read progress syncs back through the webview → Kotlin bridge.
5. Reading position, bookmarks, and user settings are persisted via `CommonSettingsRepository` / `EpubReaderSettingsRepository` / `ImageReaderSettingsRepository` (SQLite on mobile/desktop, IndexedDB on web).

---

## 9. Persistence Schema

Stored in `komelia-db/sqlite/src/commonMain/composeResources/files/migrations/` as `V1__initial_migration.sql` through `V10__komf_settings.sql` (Flyway-compatible).

Core tables (see [tables/](file:///workspace/komelia-db/sqlite/src/commonMain/kotlin/snd/komelia/db/tables/)):

| Table | Purpose |
|---|---|
| `app_settings` | Scalar app settings (server URL, theme, reader mode, etc.) — JSON-encoded key/value |
| `image_reader_settings` | Per-reader image settings (page layout, scale, filter, panel detection toggle, OnnxRuntime provider) |
| `epub_reader_settings` | EPUB reader variant, font, theme, layout |
| `komf_settings` | Komf integration settings (connection, providers, processing, notifications) |
| `book_color_correction` | Per-book color correction state |
| `color_curve_presets`, `color_levels_presets` | User presets for curves / levels |
| `user_fonts` | User-added font file references |

The Web target mirrors these concepts in IndexedDB (`IDB*Repository.kt`) with the same data model.

---

## 10. Platform Specifics

### 10.1 Android

- `minSdk = 26`, `targetSdk = 36` (from `libs.versions.toml`).
- Native libs (vips, onnxruntime) shipped as `.so` in `jniLibs` per ABI (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`).
- `AndroidDependencyContainer` configures `AppSettingsSerializer` (Proto-based settings), `AndroidSecretsRepository` (encrypted shared prefs / keystore).
- WebView: delegates to AndroidX `WebView` with JS enabled.

### 10.2 Desktop (JVM)

- Supports Linux (GTK4 / WebKit2GTK) and Windows (WebView2) via JNI native libs.
- `DesktopDependencyContainer` wires `SecretService` / `java-keyring` for secrets, `AppDirectories` for file locations.
- Compose Desktop window with fullscreen toggle, custom title bar (`PlatformTitleBar`).

### 10.3 Web (WasmJs)

- Compose for Web (`compose.html`) mounting into `#root`.
- Image decode offloaded to a Web Worker (`wasm-image-worker`) for responsiveness.
- Settings & color correction stored in IndexedDB (`komelia-db/wasm`) and `LocalStorage` respectively.
- EPUB rendering uses an `<iframe>` with the static web UI.

---

## 11. ML Model Deployment

Two OnnxRuntime models are optional, runtime-downloaded features:

1. **Panel detector** — Rf-DETR based model; outputs bounding boxes of comic panels for guided reading. Loaded by `OnnxRuntimeRfDetr` / `JvmOnnxRuntimeRfDetr`.
2. **Upscaler** — single-image super-resolution model for low-res pages. Loaded by `OnnxRuntimeUpscaler` / `JvmOnnxRuntimeUpscaler`.

Both are installed by `OnnxRuntimeInstaller` + `OnnxModelDownloader` under user-configurable directories. Supported execution providers: CPU (always), plus CUDA / ROCm / DirectML / Vulkan where available (discovered via `komelia_enumerate_devices_*`).

---

## 12. Testing Strategy

Currently the project has no dedicated `commonTest` suites in the public code tree. Manual testing is done by running the desktop / Android / web apps. Docker-based native builds provide reproducibility for native code paths.

Recommended areas for adding tests:
- `komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/color/` — pure math (histogram, LUTs).
- `komelia-db/sqlite/` — Exposed repository contracts against an in-memory SQLite.
- `epub-reader-webui/ttu-ebook-reader/src/lib/functions/` — pure TypeScript functions.

---

## 13. Troubleshooting / Common Pitfalls

- **Native libraries not found on Desktop**: run `linux-x86_64_copyJniLibs` (or Windows equivalent) after any CMake rebuild, and ensure `komelia-jni/src/jvmMain/resources/` contains the correct `.so`/`.dll` files.
- **EPUB reader blank screen**: verify `buildWebui` produced output files under `komelia-core/src/commonMain/composeResources/files/`; inspect webview console for JS errors.
- **Web (WasmJs) reader slow**: wasm-image-worker must be served as a separate worker script — dev-server configs in `komelia-app/build.gradle.kts` map `publicPath.js` correctly; double-check when deploying a static host.
- **OnnxRuntime GPU provider not detected**: ensure GPU enumeration libs (`komelia_enumerate_devices_*`) match the installed driver, and that CUDA/ROCm/DirectML runtime is present.
- **Android release crashes**: check ProGuard/R8 rules; native JNI class names are referenced by string from C and must be kept.

---

## 14. Contributing Quick Start

1. Fork and clone the repository.
2. Install JDK 17, Node.js 18+, CMake, Ninja.
3. Bootstrap web UIs: `./gradlew buildWebui`.
4. Build native libs (or use prebuilt releases for faster iteration): Docker superbuild is recommended.
5. Iterate on `komelia-core/src/commonMain/kotlin/...` and run `./gradlew :komelia-app:run` to preview.
6. For the Komf extension: `./gradlew :komelia-komf-extension:app:packageExtension`, load the resulting folder as an unpacked extension in a Chromium browser.

---

*This Code Wiki is a human-maintained companion to the source. Module and class paths listed above are stable but will drift with refactors — always cross-check with the current file tree.*
