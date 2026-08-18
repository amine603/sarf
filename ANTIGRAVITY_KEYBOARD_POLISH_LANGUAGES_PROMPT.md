# Focused Master Prompt — Keyboard Behavior, Shared Visual System & Three Languages

Work exclusively inside:

`C:\Users\Joe\Desktop\sarf`

Target app identity:

- App label: `حسابي`
- Gradle project: `sarf`
- Module: `:app`
- Source namespace: `com.cash.guide`
- Debug application ID: `com.tajir.sarf.debug`
- Launcher activity: `com.cash.guide.MainActivity`

This is a focused second pass over the existing custom-keyboard implementation. Do not open or modify another project.

## Objective

Keep the current keyboard appearance that the user approved, then complete the missing standard keyboard behavior, unify the numeric keyboard visually with the text keyboard, and support exactly three text layouts:

1. French — AZERTY
2. English — QWERTY
3. Arabic — Arabic layout with RTL-aware text handling

Do not add any other language.

---

## Phase 0 — Preserve the Current Keyboard Version First

The current custom-keyboard changes are not yet committed. Before editing:

1. Run `Get-Location` and verify it equals `C:\Users\Joe\Desktop\sarf`.
2. Verify `namespace = "com.cash.guide"` and `applicationId = "com.tajir.sarf"`.
3. Run `git status --short` and inspect every changed/untracked file.
4. Exclude `scratch/`, build outputs, `.gradle`, `local.properties`, APKs, temporary screenshots, and Antigravity brain artifacts.
5. Add only the current keyboard source, tests, and intentional project files.
6. Create this checkpoint commit before making the corrections below:

   `checkpoint: custom keyboard visual baseline before polish`

7. Record the commit hash. Do not amend, squash, delete, or rewrite this checkpoint.

This checkpoint is the rollback point if the new divider or visual unification is rejected.

If the checkpoint cannot be created safely, stop before editing.

---

## Strict Scope Lock

Do not change:

- Ledger rows and baseline system.
- Category/header design.
- Result calculation or marker design.
- Corrected row `×` / `✓` behavior.
- Dirham/Rial logic.
- Calculator popup.
- Breakdown sheet.
- Home screen, dialogs, menus, navigation, or persistence.
- Manual row creation/deletion.

Do not re-enable Samsung/system IME.

Do not add:

- Suggestions or autocorrect bar.
- Emoji, GIF, microphone, clipboard, or settings toolbar.
- A permanent number row above letters.
- More than the three approved languages.
- A new color palette, Material keycaps, dark keyboard, shadows, or gradients.

---

## 1. Backspace: Tap, Hold, Repeat, and Acceleration

Current problem: holding Backspace deletes only one character. Implement standard keyboard behavior.

Required behavior:

- Normal tap deletes exactly one selection or grapheme/code point before the cursor.
- Long press starts repeating only after an initial hold delay of approximately `350–450ms`.
- Begin repeating at approximately `80–100ms` per deletion.
- After a sustained hold of roughly `1.2–1.5s`, accelerate carefully to approximately `40–55ms` per deletion.
- Stop immediately on pointer release, pointer cancel, key disposal, keyboard collapse, mode switch, or focus change.
- Never leave a deletion coroutine/job running after the gesture ends.
- If text is selected, delete the full selection first, then continue backward only if the user is still holding.
- Delete Unicode safely. Do not split surrogate pairs, combining marks, emoji sequences, or Arabic combining characters.
- Prefer grapheme-cluster deletion where the available Android/Java APIs permit it.

Implement repeat behavior through a lifecycle-safe controller/gesture state, not a blocking loop.

Add restrained pressed feedback while the key is held. Do not add a new color.

---

## 2. Shift State Machine: One-Shot and Caps Lock

Current problem: Shift works only once and double tap does not enable persistent uppercase.

Use the existing `JournalShiftMode` enum properly:

```kotlin
OFF
ONE_SHOT
CAPS_LOCK
```

Required Latin behavior:

- Empty title starts in `ONE_SHOT`.
- A single tap while `OFF` enables `ONE_SHOT`.
- After typing one letter in `ONE_SHOT`, return to `OFF`.
- Double tap Shift within a defined `250–350ms` window enables `CAPS_LOCK`.
- While `CAPS_LOCK` is active, all Latin letters remain uppercase.
- Tapping Shift once while `CAPS_LOCK` is active returns to `OFF`.
- Switching between French and English preserves the intended Shift mode.
- Switching to Arabic hides/disables Latin Shift semantics.
- Returning from Arabic to a Latin layout restores a sensible Latin Shift state without changing existing text.

Visual states:

- `OFF`: normal graphite Shift icon.
- `ONE_SHOT`: one subtle dusty-pink marker dab.
- `CAPS_LOCK`: stronger but restrained double-marker/underline or small lock indicator using the same existing pink.
- Do not introduce a new accent color.

Use a testable event reducer or controller. Do not detect double tap using fragile global timestamps inside the Composable only.

---

## 3. Supported Languages and Layout State

Support exactly:

```kotlin
enum class JournalKeyboardLanguage {
    FRENCH,
    ENGLISH,
    ARABIC
}
```

Save and restore the selected language through configuration changes.

Default language: `FRENCH`.

### Language switch UI

In the expanded text keyboard’s thin top handle band:

- Left: compact language label (`FR`, `EN`, or `ع`).
- Center: collapse chevron, geometrically centered on the screen.
- Right: equal-width transparent spacer so the chevron remains centered.

Interactions:

- Tap the language label to cycle: `FR → EN → ع → FR`.
- Long press the label to open a small, paper-style chooser with only:
  - `Français`
  - `English`
  - `العربية`
- The chooser must use the current paper palette, no Material shadow, and remain inside screen bounds.
- Switching language must preserve the active row, title text, selection, and keyboard expansion.

The numeric keyboard’s `ABC` button returns to the last selected text language.

Do not use a globe icon if the textual language indicator is clearer.

---

## 4. French Layout — AZERTY

Use:

```text
A Z E R T Y U I O P
 Q S D F G H J K L M
  ⇧ W X C V B N ' ⌫
123       espace       .   OK ✓
```

Preserve the current French accent choices:

- `E`: é è ê ë
- `A`: à â ä
- `C`: ç
- `U`: ù û ü
- `I`: î ï
- `O`: ô ö

Uppercase mode must provide their uppercase equivalents.

---

## 5. English Layout — QWERTY

Use:

```text
Q W E R T Y U I O P
 A S D F G H J K L
  ⇧ Z X C V B N M ' ⌫
123        space       .   OK ✓
```

- Use the same Shift and Caps Lock state machine.
- The spacebar label is `space`.
- Do not show French accent hints by default in English mode.
- Preserve apostrophe and period for names.

---

## 6. Arabic Layout and RTL Handling

Use a familiar Arabic mobile letter order. Keep the rows visually RTL while the surrounding screen remains LTR/French.

Recommended layout:

```text
ض ص ث ق ف غ ع ه خ ح ج
 ش س ي ب ل ا ت ن م ك ط
  ئ ء ؤ ر لا ى ة و ز ظ ⌫
123       مسافة       ،   OK ✓
```

Requirements:

- Apply `LayoutDirection.Rtl` only to Arabic letter rows where required; do not flip the whole app or the action positions unexpectedly.
- Backspace must remain visually and behaviorally predictable at the edge.
- Spacebar label: `مسافة`.
- Use Arabic comma `،` in Arabic mode.
- Arabic has no uppercase Shift. Replace/hide Shift in Arabic mode rather than showing a meaningless control.
- Use the bundled Tajawal font for Arabic key labels.
- Use the current journal handwriting font for French/English keys.
- Input must use proper Unicode Arabic characters and rely on Compose shaping/bidi rendering.
- Mixed text such as Arabic names containing Latin digits must preserve correct cursor movement and selection.

Provide useful Arabic long-press alternatives without overcrowding:

- `ا`: أ إ آ
- `ي`: ى ئ
- `و`: ؤ
- `ه`: ة

Do not add a full diacritics keyboard in this phase.

---

## 7. Accent/Alternative Popup Correction

Current problem: the accent popup uses a fixed `Alignment.TopCenter` and fixed offset, so it is not actually anchored above the pressed key.

Correct it:

- Anchor the popup to the actual pressed key bounds.
- Position it immediately above that key.
- Clamp horizontally so it never leaves the screen near `A`, `P`, or Arabic edge keys.
- Use the same `JournalDockBg`/paper palette.
- Remove the current heavy Material-like shadow.
- Use a thin paper-rule border or no border if contrast is sufficient.
- Current choice may use a subtle dusty-pink marker.
- Long press opens the alternatives.
- Allow tap selection at minimum.
- If slide-to-select is implemented, it must handle pointer cancel correctly; do not fake or report it if only tap selection exists.

---

## 8. Shared Visual System for Text and Numeric Keyboards

Current observation: the text keyboard looks softer and more integrated than the numeric keyboard. Refactor both to use one shared visual scaffold rather than maintaining two unrelated dock styles.

Create/reuse shared components such as:

- `JournalKeyboardDockSurface`
- `JournalKeyboardHandleBar`
- `JournalKeyboardKeyCell`
- shared pressed-state feedback
- shared navigation-inset handling
- shared top-corner shape

Locked tokens:

- Page: `JournalPaper`
- Dock: existing `JournalDockBg` (`#F6F0DF`)
- Ink: existing graphite token
- Rules/dividers: existing `JournalRule`
- Confirm: existing muted green
- Pressed/Shift: existing dusty pink

### Numeric keyboard visual update

Keep its exact functional layout:

```text
1 | 2 | 3 | ⌫
4 | 5 | 6 | .
7 | 8 | 9 | 0
```

Keep `ABC` at the left of the thin handle band and keep the chevron centered.

Change only its visual treatment to match the text keyboard:

- Same dock background and top rounding.
- Same gentle key press feedback.
- Same lightweight typography rhythm.
- Replace the heavy dark outer grid with softer pencil dividers consistent with the text keyboard.
- Preserve clear key boundaries and large hit areas.
- Do not increase its height materially.

---

## 9. Subtle Divider Before the Utility Row

The user wants clearer separation between letter rows and the utility row containing language/number switch, Space, punctuation, and `OK`.

Add one subtle horizontal divider immediately above the bottom utility row:

- Thickness approximately `0.6–0.8dp`.
- Use `JournalRule` with restrained alpha.
- It may be slightly clearer than the separators between letter rows, but must remain light.
- Do not box every key heavily.
- Do not add a card or shadow around the utility row.

Apply a corresponding visual rhythm to the numeric keyboard only where it improves consistency.

This is an experimental visual refinement. Because the current version is checkpointed, it must remain easy to revert if physical-device review rejects it.

---

## 10. Standard Interaction Requirements

Preserve and verify:

- Insert at cursor.
- Replace selected text.
- Correct cursor advancement.
- Tap Backspace.
- Hold/repeat Backspace.
- One-shot Shift.
- Double-tap Caps Lock.
- Accent/alternative long press.
- `123` switches from title to amount on the same row.
- `ABC` returns to the title and last selected text language.
- `OK ✓` uses the same confirmation path as the row `✓`.
- Collapse/expand preserves edit state.
- Android Back collapses before leaving the screen.
- No Samsung/system keyboard flash.
- Active row remains visible above either dock.
- Keyboard background extends safely through gesture and 3-button navigation insets.

Do not claim swipe typing, autocorrect, suggestions, or slide-to-select unless they are genuinely implemented and verified.

---

## 11. Automated Tests

Add tests for:

### Backspace

- tap deletes one grapheme/selection;
- hold begins only after delay;
- repeat accelerates;
- release/cancel stops immediately;
- disposal/mode switch cancels repeat;
- emoji, combining accents, and Arabic combining characters are not corrupted.

### Shift

- OFF → single tap → ONE_SHOT;
- ONE_SHOT → type letter → OFF;
- double tap → CAPS_LOCK;
- CAPS_LOCK remains after multiple letters;
- tap CAPS_LOCK → OFF;
- Arabic mode does not expose Latin Shift behavior.

### Languages

- French layout is AZERTY;
- English layout is QWERTY;
- Arabic layout inserts correct Unicode characters;
- language cycling order is FR → EN → AR → FR;
- long-press chooser selects each language;
- language switch preserves text and selection;
- `ABC` restores last selected language;
- Arabic/Latin mixed cursor editing works.

### Shared UI behavior

- title shows text keyboard only;
- amount shows numeric keyboard only;
- system IME never appears;
- dock modes are mutually exclusive;
- collapsed/expanded state remains correct;
- OK and row ✓ confirm identically.

Run:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-19'
$env:JAVA_TOOL_OPTIONS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
.\gradlew.bat :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
```

---

## 12. Mandatory Physical-Device Verification

Use clean, reset test data. Do not present screenshots containing accidental strings such as `Marchémarch` or concatenated test amounts as final evidence.

Capture genuinely distinct screenshots:

1. `polish_fr_azerty.png`
2. `polish_en_qwerty.png`
3. `polish_ar_arabic.png`
4. `polish_shift_one_shot.png`
5. `polish_caps_lock.png`
6. `polish_fr_accents_anchored.png`
7. `polish_ar_alternatives_anchored.png`
8. `polish_backspace_hold_before.png`
9. `polish_backspace_hold_after.png`
10. `polish_numeric_shared_style.png`
11. `polish_utility_divider.png`
12. `polish_text_collapsed.png`
13. `polish_numeric_collapsed.png`
14. `polish_navigation_insets.png`
15. `polish_clean_full_release.png`

Record a short video proving:

- holding Backspace continuously deletes and accelerates;
- release stops immediately;
- single Shift affects one character;
- double-tap Shift enables persistent Caps Lock;
- FR/EN/AR switching preserves the title and selection;
- Arabic shaping and cursor editing work;
- `123` and `ABC` keep the same row;
- no system IME appears;
- divider and shared numeric style remain visually light.

Verify screenshot hashes are unique and that every filename matches its real content.

---

## Mandatory Stop

After implementation, tests, APK installation, and device verification, stop and report:

- pre-polish checkpoint commit hash;
- exact files changed after the checkpoint;
- Backspace repeat timing and cancellation design;
- Shift/Caps Lock state machine design;
- language/layout architecture;
- how Arabic RTL and mixed text were handled;
- shared keyboard components created;
- tests/build results;
- APK path;
- clean screenshot/video paths and hashes;
- any unimplemented or partially implemented behavior stated honestly;
- any deviation from this prompt and why.

Do not continue to another screen or add another language without explicit approval.
