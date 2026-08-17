# Master Prompt — Hisabi Focused Ledger Refinement

Work only inside:

`C:\Users\Joe\Desktop\sarf`

Project identity:

- Android app: Hisabi
- Package: `com.cash.guide`
- Main approved visual reference for this pass:
  `C:\Users\Joe\Desktop\sarf\design\approved-ledger-refinement-concept.png`
- Previous structural reference:
  `C:\Users\Joe\Desktop\sarf\design\approved-editing-and-confirm-popup.png`

## Objective

Apply one focused refinement pass to the existing French bullet-journal ledger screen. Do not redesign the application. Preserve the current paper identity, handwriting fonts, colors, calculations, row data, calculator popup, breakdown behavior, state preservation, and compact layout.

The new reference is conceptual. Implement its approved hierarchy, but use measured Compose geometry—not approximate image positions. The most important requirement is exact alignment with the real paper-rule coordinate.

## Strict Scope Lock

Do not:

- redesign either keypad;
- create the future custom text keyboard;
- alter calculator-popup layout or arithmetic behavior;
- change conversion rules, denomination breakdown, persistence, navigation, or row math;
- restore the word `Total`, the secondary currency conversion, or the old double underline;
- add Material cards, shadows, orange accents, decorative icons, automatic rows, or new labels;
- modify files outside `C:\Users\Joe\Desktop\sarf`.

## 1. Exact Paper Baseline — Remove the Remaining 1dp Gap

The current implementation deliberately uses a 1dp optical offset above the rule. Remove that offset.

- Use one shared `ruleY` coordinate for the ruled-paper line and the typographic `FirstBaseline`.
- Set the target baseline to exactly `ruleY`, not `ruleY - 1dp` and not an optical approximation.
- Apply the same measured system to row number, title, amount, currency suffix, add-row `+`, category text, and the final result.
- Keep `PlatformTextStyle(includeFontPadding = false)`.
- Do not solve this with arbitrary per-component `offset(y = ...)` values.
- The measured baseline-to-rule delta must be effectively zero, allowing only rasterization tolerance.

Create a temporary debug overlay that draws the real rule and measured baseline in different colors. Capture proof, then disable/remove the overlay from the release build.

## 2. Quiet Focus Treatment — No Long Black Bar

Remove the current strong black title underline and reduce the amount-focus treatment.

Inactive fields:

- Show only the natural blue-gray paper rule.
- No additional underline or bar.

Active empty title field:

- Show the real blinking text cursor.
- Optionally show one very short, subtle blue-gray focus stroke, approximately 18–22dp wide.
- Opacity must be restrained, around 30–40%.

Active title containing text:

- Any focus stroke must follow only the measured visible glyph width.
- Use subtle blue-gray, never graphite black.

Active amount field:

- Keep the real editable cursor and exact selection support.
- Use a very faint dusty-pink stroke only beneath the measured numeric glyph width.
- Suggested stroke: about 1dp and 40–50% alpha.
- Never underline the currency suffix or the entire amount container.

On confirmation, all focus strokes and cursors disappear immediately.

## 3. Explicit Edit Confirmation

Use the existing far-right row action as a state-aware action:

- Inactive row: show the delete mark `×`.
- While either the title or amount of that row is actively edited: replace `×` with a muted-green handwritten checkmark `✓`.

Pressing `✓` must:

1. commit the current `TextFieldValue`, including its selection state;
2. clear title/amount focus;
3. dismiss the Samsung IME if title editing is active;
4. leave/close the active numeric-entry state cleanly if amount editing is active;
5. remove the focus stroke and cursor;
6. render the amount in its inactive grouped form, for example `5\u00A0000 rial`.

For title editing, the Samsung keyboard IME action `Done/Terminé` must perform the same confirmation.

For amount editing, the Samsung keyboard must never appear. Continue using the app's existing numeric keypad. Do not redesign that keypad in this pass.

## 4. Far-Right `×` / `✓` Without Wasting Ledger Width

- Keep `×`; do not replace it with a trash-can icon.
- Draw inactive `×` in restrained muted coral.
- Draw active `✓` in restrained muted green.
- Push the visible mark approximately 6–8dp farther toward the right screen edge.
- Position it as an end overlay so its 48dp touch target does not consume 48dp from the title/amount layout.
- Preserve a full, non-overlapping 48dp accessible touch target.
- Keep enough internal safety padding to avoid clipping on narrow screens.

Critical rule alignment:

- Do not vertically center the visible `×` or `✓` inside the 48dp touch box.
- Anchor the visible mark to the same paper-rule coordinate as the row text.
- The lower endpoints/optical baseline of the handwritten mark must land on `ruleY`, so it does not float above the row.
- Verify this with the same temporary baseline overlay used for text.

## 5. Replace the Old Total Section With One Result

Delete the old visual hierarchy completely:

- remove the word `Total`;
- remove the yellow `Total` label marker;
- remove the secondary conversion such as `440 DH`;
- remove the two pink underline strokes;
- remove the old three-slot total layout if it is no longer needed.

Show only the total in the unit selected by the user:

- Rial mode example: `8\u00A0800 rial`
- Dirham mode example: `440 DH`
- Never show both units simultaneously here.
- Center the result horizontally.
- Use a slightly larger handwritten/numeric style.
- Make the numeric value the visual emphasis; render `rial` or `DH` smaller/lighter.
- Place the complete result over one restrained, organic pale-yellow highlighter stroke sized to natural content bounds.
- Do not use a card, pill, border, shadow, label, or underline.
- Keep the entire result target clickable to open the existing money-breakdown sheet when the total is valid and greater than zero.
- Preserve disabled semantics for zero or invalid totals.

Use the existing French non-breaking-space formatter for all inactive amounts and for the final result. Do not alter raw stored values or arithmetic input.

## 6. Compact Layout and Existing Behaviors

- Preserve manual row addition via the centered handwritten `+`.
- Do not reintroduce automatic trailing rows.
- Keep row heights and overall ledger compact unless a minimal measured adjustment is required for exact baseline alignment.
- Keep the numeric keypad dynamically measured so it never covers the result or active row.
- Keep list auto-scroll/bring-into-view behavior working after focus and confirmation.
- Preserve exact cursor placement between title characters and amount digits.

## Implementation Discipline

Before editing:

1. Confirm `Get-Location` is `C:\Users\Joe\Desktop\sarf`.
2. Inspect the current implementations in `MoneyListApp.kt`, `JournalComponents.kt`, `HisabiRuledPaper.kt`, `JournalDesign.kt`, and `JournalLedgerManager.kt`.
3. Check `git status` and preserve unrelated/user changes.
4. Treat the existing behavior as the source of truth unless this prompt explicitly changes it.

Prefer extending the existing components over creating a second competing design system. Remove obsolete code from the old Total section and strong underline implementation after the replacement is verified.

## Tests

Add or update tests for:

- active row action switches from delete to confirm;
- confirmation commits and clears editing state;
- IME Done confirms title editing;
- amount focus does not summon the system IME;
- confirmed amount renders French grouping without changing raw math state;
- result displays only the selected unit;
- result remains clickable only for valid totals greater than zero;
- delete still works in inactive state;
- existing calculator-popup and arithmetic tests remain unchanged and passing.

Run:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-19'
$env:JAVA_TOOL_OPTIONS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
.\gradlew.bat :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
```

## Mandatory Physical-Device Verification

Install on the connected Samsung device and verify with real taps. Capture genuinely distinct screenshots—do not duplicate one capture under multiple filenames:

1. `refined_inactive_rows.png` — inactive rows with no extra focus bars and coral `×` at the far edge.
2. `refined_title_editing.png` — title cursor, subtle focus treatment, green `✓`, Samsung text IME.
3. `refined_amount_editing.png` — caret between digits, faint pink measured stroke, green `✓`, app numeric keypad only.
4. `refined_amount_confirmed.png` — confirmed grouped amount with no cursor/stroke and restored `×`.
5. `refined_result_rial.png` — centered single result in rial, no label/conversion/underlines.
6. `refined_result_dirham.png` — centered single result in DH only.
7. `refined_baseline_debug.png` — debug proof showing text and `×/✓` anchored to the exact paper rule.
8. `refined_full_release.png` — clean full-screen release state with debug overlay disabled.

Also record a short interaction sequence proving:

- amount tap opens only the app numeric keypad;
- tapping `✓` commits and groups the value;
- title IME Done confirms;
- inactive `×` still deletes the intended row;
- tapping the centered result opens the existing breakdown.

## Mandatory Stop

After implementing, testing, installing, and capturing the evidence, stop. Report:

- exact files changed;
- test/build results;
- APK location;
- distinct screenshot paths;
- any deviation from this prompt and why.

Do not start the future custom text-keyboard work and do not redesign any other screen without explicit approval.
