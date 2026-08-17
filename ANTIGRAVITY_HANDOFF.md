# Antigravity handoff — rebuild `Hisabi`

## Copy/paste prompt

You are continuing an Android app rebuild that is already in progress in this repository. Read this entire handoff first, inspect the current working tree, then continue from the existing files. Do not restore the deleted legacy app and do not discard unrelated user files or reset the repository.

Your goal is to finish a polished, working Moroccan dirham/riyal list calculator that faithfully follows `design/approved-direction.png`. Use `design/inspiration.png` only for its visual system and spacing. Work autonomously through the stages below, fix issues you discover, build the app, and stop only when the requested implementation is complete and verified.

### Product behavior

- The app is Arabic and RTL.
- It supports only Moroccan dirham and Moroccan rial, with the fixed rule `1 dirham = 20 rial`.
- The top segmented tabs select the current input unit: `درهم` or `ريال`.
- A list contains repeatable rows. Each row has:
  - an optional Arabic title, such as a person's name or an item;
  - a required amount/expression.
- `زيد سطر` always appears immediately after the last row.
- Amount fields use the custom compact calculator panel fixed at the bottom.
- The keypad contains digits, decimal point, backspace, `+`, `−`, `×`, `÷`, and `=`.
- The keypad can collapse and expand with a chevron. Collapsing it frees space for more rows.
- Tapping a title uses the normal text keyboard; tapping an amount uses the custom numeric keypad.
- Show a live total in the selected unit and a smaller conversion to the other unit.
- `حسب المجموع` opens a money breakdown using the real Moroccan currency images in `app/src/main/assets/sarfpic/`.
- The app must work fully offline. No ads, exchange-rate API, country selection, onboarding, analytics, or foreign currencies.

### Approved visual direction

Follow `design/approved-direction.png` closely:

- warm ivory background;
- paper-white floating content sheet;
- charcoal rounded calculator dock;
- muted copper/amber accent;
- balanced phone proportions, normal 14–22sp typography, compact 52–56dp rows;
- soft borders and shadows, ample breathing room;
- no green theme, giant cards, giant numbers, avatars, bottom navigation, gradients, or visual clutter.

Treat the mockup as a product direction, not a pixel-perfect source of truth. Preserve usability on different Android screen heights, handle the system IME correctly, and keep tap targets accessible.

## Work already completed

- The legacy Kotlin implementation was removed.
- Foreign-currency asset directories were removed; `sarfpic` was preserved.
- AdMob, Retrofit, OkHttp, DataStore, AppCompat, extended icons, and other unused dependencies were removed from `app/build.gradle.kts`.
- Internet/network permissions, AdMob metadata, and obsolete manifest declarations were removed.
- Old translated string sets were removed; the app name is now `حسابي`.
- New implementation files exist:
  - `app/src/main/java/com/cash/guide/MainActivity.kt`
  - `app/src/main/java/com/cash/guide/domain/MoneyMath.kt`
  - `app/src/main/java/com/cash/guide/ui/MoneyListApp.kt`
  - `app/src/main/java/com/cash/guide/ui/theme/Theme.kt`
  - `app/src/test/java/com/cash/guide/MoneyMathTest.kt`
- `MoneyMath` already implements expression evaluation, dirham/rial conversion, centime-based totals, and greedy denomination breakdown.
- `MoneyListApp` already implements dynamic rows, unit conversion, the custom keypad, collapse/expand states, totals, and a money-breakdown bottom sheet.

## Required continuation stages

### Stage 1 — inspect without undoing

1. Read the files listed above and inspect `git status --short`.
2. Preserve the intentional deletions and current uncommitted user files.
3. Confirm every path referenced by `MoneyMath.denominations` exists. Fix labels or paths if needed.

### Stage 2 — compile and test

1. Run:

   ```powershell
   $env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
   $env:JAVA_TOOL_OPTIONS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
   .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
   ```

2. The previous attempt did not reach Kotlin compilation because Gradle downloads failed with `PKIX path building failed`. A retry with the Windows trust store was started but intentionally interrupted by the user to save quota. If the same SSL issue remains, use a safe local/Gradle certificate workaround or available cached dependencies; do not disable TLS verification globally.
3. Fix all Kotlin, Compose, resource, manifest, and unit-test errors until the build passes.

### Stage 3 — functional review

Verify and fix these cases:

- `50 dirham = 1000 rial` and switching tabs preserves value.
- `600 + 250 + 30 = 880 dirham = 17600 rial`.
- Named rows total correctly: `1200 + 5000 + 2600 = 8800 rial = 440 dirham`.
- Operator precedence and divide-by-zero handling are safe.
- Removing the final row clears it instead of crashing.
- Adding many rows remains scrollable.
- Amount selection follows the active row.
- Title input opens the text IME without leaving the custom keypad awkwardly visible.
- Expanded and collapsed keypad states work on small screens.
- Money breakdown reconstructs the exact centime total, including a 5-centime/one-rial remainder when necessary.
- Rotation/recreation does not produce a crash. Improve state saving if reasonable.

### Stage 4 — visual refinement

Run the app on an emulator/device if available and compare it with `design/approved-direction.png`. Refine only where needed:

- prevent clipping/overlap with system bars and IME;
- keep the keypad compact and fixed to the bottom;
- keep the total slim and readable;
- maintain correct RTL ordering while keeping the numeric keypad naturally ordered;
- make the real banknote/coin images fit without distortion;
- add content descriptions where appropriate.

Do not expand scope with settings, accounts, cloud sync, ads, foreign currencies, or extra screens.

### Stage 5 — cleanup and delivery

1. Remove any remaining obsolete resources or dependencies only after verifying they are unused.
2. Run unit tests and assemble the debug APK again.
3. Report:
   - what was completed;
   - test/build results;
   - the exact APK path;
   - any remaining limitation that genuinely could not be resolved.

Do not commit unless the user explicitly asks for a commit.

## Current build status

Implementation is present but not yet compilation-verified. The only observed blocker so far was Gradle's SSL certificate failure while downloading AndroidX artifacts, not a confirmed source-code error.
