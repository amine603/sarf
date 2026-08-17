# Master Prompt — Hisabi In-App Text & Numeric Keyboards

Work exclusively inside:

`C:\Users\Joe\Desktop\sarf`

Project identity:

- App: Hisabi
- Package: `com.cash.guide`
- Text-keyboard visual reference:
  `C:\Users\Joe\Desktop\sarf\design\custom-text-keyboard-concept.png`
- Current numeric keyboard: the existing `JournalCompactNumericDock` implementation is the functional and visual source of truth.

## Objective

Preserve the current approved Hisabi version, then implement two custom **in-app Compose keyboards**:

1. A custom French AZERTY text keyboard for title/name fields.
2. The existing compact numeric keyboard for amount fields.

The correct keyboard must open automatically from the field the user taps. The user must also be able to switch between the two keyboard modes intentionally.

This is not an Android system-wide IME. It exists only inside Hisabi.

---

## Phase 0 — Mandatory Git Checkpoint Before Any Code Change

Before editing any source file:

1. Confirm `Get-Location` is exactly `C:\Users\Joe\Desktop\sarf`.
2. Confirm the Gradle project and package are Hisabi / `com.cash.guide`.
3. Run `git status --short` and inspect all current modified and untracked project files.
4. Ensure generated build folders, `.gradle`, `local.properties`, APK outputs, device dumps, and Antigravity brain artifacts are not added.
5. Add the current approved project source, tests, local fonts, design references, and project documentation.
6. Create a checkpoint commit with this exact message:

   `checkpoint: stable ledger before custom keyboards`

7. Record the checkpoint commit hash in the final walkthrough.
8. Verify the working tree state after the checkpoint before beginning keyboard work.

Do not rewrite, squash, amend, or delete this checkpoint later. Its purpose is to make the current approved version recoverable.

If Git cannot create the checkpoint safely, stop before editing and report the blocker.

---

## Strict Scope Lock

Do not redesign or change:

- Ledger rows, paper rules, baseline geometry, fonts, highlighters, or colors.
- The corrected `×` / `✓` row actions.
- Result layout or calculation.
- Dirham/Rial conversion.
- Calculator popup.
- Denomination breakdown.
- Home screen, menus, dialogs, navigation, or saved list data.
- Manual row addition and deletion logic.

Do not add:

- A number row above the text keyboard.
- Suggestion/autocorrect toolbar.
- Emoji, microphone, clipboard, settings, or GIF toolbar.
- A new palette, dark keyboard, Material keycaps, gradients, shadows, or orange.
- A third keyboard mode.

The objective is compact text entry, not a full replacement for Samsung Keyboard features.

---

## 1. Keyboard State Model

Introduce one explicit, saveable keyboard state, for example:

```kotlin
enum class JournalKeyboardMode {
    NONE,
    TEXT,
    NUMBER
}
```

Keep it coordinated with:

- `activeRowId`
- `activeField`
- `keyboardExpanded`
- the title and amount `TextFieldValue` selections

Required automatic behavior:

- Tap a row title/name → focus that title and open the custom `TEXT` keyboard.
- Tap a row amount/price → focus that amount and open the existing `NUMBER` keyboard.
- Only one custom keyboard is visible at a time.
- Neither action may open or briefly flash the Samsung/system IME.
- Tapping the active field again preserves its cursor/selection and reopens its appropriate keyboard if collapsed.
- Pressing the row `✓` or keyboard `OK` commits, clears editing focus, and closes the custom keyboard.
- Android Back first closes the expanded keyboard without losing text; a second Back follows the existing screen behavior.

Preserve this state correctly across rotation/configuration changes using the existing saveable architecture.

---

## 2. Robustly Suppress the System IME

Both title and amount fields must retain:

- Real cursor placement from taps.
- `TextFieldValue.selection`.
- Selection replacement.
- Cursor movement between characters/digits.

But the Samsung/system keyboard must never appear for either field after this feature is enabled.

Use a Compose-supported platform text-input interception/read-only editing strategy compatible with the project’s current Compose version. Do not rely only on calling `keyboardController.hide()` after focus, because that may flash the system keyboard.

Do not break pointer-based cursor placement or accessibility semantics.

---

## 3. Custom French AZERTY Text Keyboard

Create one reusable component, for example:

`JournalTextKeyboardDock`

Use the supplied reference faithfully:

`design/custom-text-keyboard-concept.png`

### Layout

Use French AZERTY ordering with these rows:

```text
A  Z  E  R  T  Y  U  I  O  P
 Q  S  D  F  G  H  J  K  L  M
  ⇧  W  X  C  V  B  N  '  ⌫
123          espace          .   OK ✓
```

The stagger may be optically adjusted for balance, but key hit areas must not overlap.

### Bottom actions

- `123`: switch to the amount field of the same active row and open the numeric keyboard.
- `espace`: insert one space at the current selection.
- `.`: insert a period at the current selection.
- `OK ✓`: call the same existing row-confirmation path as the row’s green `✓`, then close the keyboard.
- `⌫`: delete the selection, or one Unicode code point before the cursor when no text is selected.

### Shift and capitalization

- Empty title starts with one-shot Shift active.
- First typed letter is uppercase, then Shift returns to lowercase.
- Single tap Shift toggles one-shot uppercase.
- Double tap may enable caps lock only if it can be implemented clearly without visual clutter; otherwise omit caps lock.
- Key labels must accurately reflect the active case.

### French accents

Support long-press accent selection at minimum:

- `E`: `é`, `è`, `ê`, `ë`
- `A`: `à`, `â`, `ä`
- `C`: `ç`
- `U`: `ù`, `û`, `ü`
- `I`: `î`, `ï`
- `O`: `ô`, `ö`

Show a small paper-style accent popup above the pressed key, using the existing dusty-pink marker only for the current choice. It must remain inside screen bounds near the left/right edges.

Tap a letter for the normal character. Long press opens accents. Sliding/releasing over an accent inserts it and closes the popup.

### Key behavior

All insertions must operate on `TextFieldValue` correctly:

- Replace selected text.
- Insert at the current cursor.
- Move the cursor after inserted content.
- Preserve composed Unicode characters.
- Long-press backspace repeats deletion at a controlled rate and stops immediately on release/cancel.

---

## 4. Visual Design — Exact Existing Hisabi Colors

The ledger/page background remains unchanged: `JournalPaper` (`#FBF6E8`).

The keyboard surface must reuse the current numeric keyboard token:

`JournalDockBg` (`#F6F0DF`)

Do not invent or hard-code a different beige.

Visual requirements:

- Background behind the keyboard remains the existing app paper.
- Keyboard rises from the bottom as one connected dock.
- Only the top-left and top-right corners are rounded, approximately `10–12dp`.
- Bottom remains edge-to-edge and extends behind the navigation-bar area with no color gap.
- Use the existing graphite ink and handwriting typography.
- Use thin pencil-like separators or extremely quiet key boundaries.
- No floating Material buttons or individual rounded key cards.
- Dusty pink is allowed only for pressed/accent feedback.
- Muted green is allowed for `OK ✓`.
- Keep contrast readable but visually light.

### Compact size

- Thin top handle band: approximately `24–30dp`.
- Letter-key touch targets: minimum `44dp`; target `46–48dp` where screen height permits.
- Avoid fixed total keyboard height when insets or screen size vary.
- Measure the rendered dock and feed its real height into list/content bottom padding.
- The active ledger row and selected result must never be hidden behind the keyboard.
- Use `BringIntoViewRequester` or the existing list scroll system to keep the active row visible.

Do not add a number row above the letters. It contradicts the compact requirement.

---

## 5. Numeric Keyboard Integration

Preserve the existing `JournalCompactNumericDock` layout and styling:

```text
1 | 2 | 3 | ⌫
4 | 5 | 6 | .
7 | 8 | 9 | 0
```

Do not restore arithmetic operators or `=`.

Add one discoverable `ABC` mode-switch control without increasing the dock’s overall height materially.

Preferred placement:

- Put `ABC` at the left side of the existing thin top handle band.
- Keep the collapse/expand chevron centered.
- Balance the right side with equal reserved width so the chevron stays geometrically centered.

When `ABC` is tapped:

- Switch active field to the title of the same row.
- Preserve the amount and its selection.
- Restore the title’s previous selection if available.
- Open the custom text keyboard.

When `123` is tapped from the text keyboard:

- Switch active field to the amount of the same row.
- Preserve the title and its selection.
- Restore the amount’s previous selection if available.
- Open the numeric keyboard.

This field-aware switching prevents letters from being inserted into an amount and makes the mode switch meaningful.

---

## 6. Collapse, Expansion, and Navigation Insets

- Both keyboard modes share the same collapse/expand behavior.
- The chevron collapses only the dock body, not the current edit state.
- In collapsed state, show a compact strip with:
  - mode switch (`ABC` or `123`),
  - centered expand chevron,
  - optional `OK ✓` only if it does not crowd the strip.
- Tapping the active field expands the appropriate mode again.
- Switching mode always expands the destination keyboard.
- Use the current navigation-bar inset strategy so the dock color continues behind gesture or 3-button navigation safely.

---

## 7. Accessibility and Feedback

- Every key must have an accessibility role and French content description.
- Touch targets must remain at least `44×44dp`, preferably `48×48dp`.
- Visual key labels may be smaller than their hit areas.
- Provide restrained pressed-state feedback with existing marker colors.
- Do not use vibration unless the app already has a haptic policy; if used, keep it subtle and use platform-standard key-press haptics.
- `OK`, `123`, `ABC`, Shift, and Backspace states must be announced correctly.

---

## 8. Suggested Architecture

Keep input transformations testable and independent from Composables. Prefer pure functions or a small controller/reducer for:

- insert text at selection;
- backspace by selection/code point;
- switch active field and keyboard mode;
- Shift state;
- accent insertion;
- confirmation.

Avoid duplicating title/amount state inside the keyboard components. The existing row `TextFieldValue` remains the single source of truth.

Reuse the current design tokens and sketch-icon system instead of creating another theme.

---

## 9. Automated Tests

Add unit tests for:

- insertion at start, middle, and end;
- replacing selected text;
- backspace with selection;
- backspace before cursor;
- Unicode/accent deletion without corrupting text;
- one-shot Shift behavior;
- accent insertion (`Marché`, `Naïma` remains intact);
- `123` switches the same row from title to amount;
- `ABC` switches the same row from amount to title;
- mode switching preserves both selections;
- `OK` uses the existing confirmation path;
- numeric input remains restricted to the amount rules;
- rotation/state restoration.

Add Compose/instrumentation tests for:

- tapping title shows only the custom text keyboard;
- tapping amount shows only the custom numeric keyboard;
- Samsung/system IME never appears or flashes;
- text and numeric docks are mutually exclusive;
- accent popup stays within screen bounds;
- active row remains visible above each keyboard;
- Back collapses the keyboard before leaving the screen;
- row `✓` and keyboard `OK` produce the same confirmed state.

Run:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-19'
$env:JAVA_TOOL_OPTIONS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
.\gradlew.bat :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
```

---

## 10. Mandatory Physical-Device Verification

Install on the connected Samsung device and capture genuinely distinct evidence:

1. `keyboard_text_empty_title.png` — empty title, one-shot Shift, custom text keyboard.
2. `keyboard_text_typing.png` — cursor in the middle of a title and inserted character.
3. `keyboard_text_accents.png` — accent popup above `E` with `é/è/ê/ë`.
4. `keyboard_text_marche.png` — confirmed `Marché` with the keyboard closed.
5. `keyboard_numeric_amount.png` — amount active with existing numeric keyboard only.
6. `keyboard_switch_123.png` — same row switched from title to amount.
7. `keyboard_switch_abc.png` — same row switched from amount to title.
8. `keyboard_text_collapsed.png` — compact collapsed text-keyboard strip.
9. `keyboard_numeric_collapsed.png` — compact collapsed numeric-keyboard strip.
10. `keyboard_navigation_inset.png` — dock connected cleanly to the system navigation edge.
11. `keyboard_full_release.png` — clean populated ledger with final keyboard styling.

Also record a short interaction proving:

- title tap never opens Samsung Keyboard;
- amount tap never opens Samsung Keyboard;
- `Marché` can be written using the accent popup;
- cursor insertion/replacement works;
- `123` and `ABC` preserve the same row’s data;
- `OK ✓` confirms and closes the keyboard;
- Android Back collapses before navigating away.

Verify screenshot hashes are distinct before presenting them as separate evidence.

---

## Mandatory Stop

After the checkpoint, implementation, tests, installation, and device verification, stop and report:

- checkpoint commit hash;
- exact files changed after the checkpoint;
- keyboard-state architecture used;
- how system IME suppression was implemented;
- test/build results;
- APK location;
- distinct screenshot/video paths and hashes;
- any deviation from this prompt and why.

Do not redesign another screen or begin unrelated work without explicit approval.
