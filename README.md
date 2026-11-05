Flag Quiz Master

Flag Quiz Master is a native Android geography quiz game and interactive encyclopedia covering all 195 sovereign nations recognized worldwide. 

I built this project to master modern Android development—focusing on Clean Architecture, offline-first data persistence, and building responsive, fluid UI entirely with Jetpack Compose.

What it does:
* **5 Game Modes:** Play classic Flag-to-Name, test your visual memory with Country-to-Flag (where options are actual flag images), guess capital cities, or play binary Population Showdowns (Higher vs. Lower).
* **100% Offline-First:** Pre seeded with a complete local SQLite database of all 195 nations and high-res vector SVG flags, so the game works anywhere with zero network latency.
* **Flag Encyclopedia:** Search all 195 nations in real time, filter by continent, star favorites, and tap any nation to view detailed statistics (population, capital, ISO codes, and your quiz accuracy rate).
* **Hand-Crafted Performance Charts:** Built directly on Jetpack Compose's native `Canvas` API to render smooth accuracy trend graphs without heavy third-party charting libraries.
* **Daily Challenge & Streaks:** Generates a globally synchronized 10-question challenge seeded by the calendar date (`toEpochDay()`) with automated streak tracking.

Tech Stack:
* **UI:** Jetpack Compose, Material 3, Custom Canvas, Dynamic Theming (Light/Dark/System)
* **Architecture:** MVVM + Clean Architecture (Domain, Data, Presentation), Unidirectional Data Flow
* **Local Storage:** Room Database (SQLite), Jetpack DataStore (Preferences)
* **Async & Concurrency:** Kotlin Coroutines, Flow, StateFlow
* **Image Loading:** Coil (SVG Vector Decoder)
* **Dependency Injection:** Dagger-Hilt
* **Testing:** JUnit4, MockK
