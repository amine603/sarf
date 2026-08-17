# Master Prompt — Hisabi Ledger Editing & Calculator Confirmation

You are implementing the final approved refinement of the Hisabi Android app.

## Source of truth

- Project: `C:\Users\Joe\Desktop\sarf`
- Mandatory visual reference: `C:\Users\Joe\Desktop\sarf\design\approved-editing-and-confirm-popup.png`

The approved image is the final source of truth for the visual result. Do not redesign the screen, invent alternative components, or modify unrelated screens.

## Objective

Implement only these refinements:

1. Vertically center text inside the category and Total highlighter strokes.
2. Use one transaction per visible notebook rule, without a blank ruled line between transactions.
3. Provide real cursor placement and text selection inside title and amount fields.
4. Visually distinguish the dark amount from its lighter currency suffix.
5. Remove automatic trailing-row creation.
6. Add rows only when the user taps a centered handwritten `+` control.
7. Keep the calculator popup visually unchanged from the approved image.
8. Make `=` calculate and display the result only.
9. Add `Confirmer` to apply the result to the active row and close the popup.

## Preserve the approved identity

Preserve exactly:

- French LTR layout.
- Warm cream notebook paper and subtle physical texture.
- Thin cool gray-blue paper rules.
- Graphite handwritten ink.
- Existing bundled Patrick Hand / Kalam typography.
- Dusty-pink category highlighter.
- Soft-yellow Total highlighter.
- Double pink underline below the primary total.
- Hand-drawn calculator icon.
- Compact numeric bottom dock.
- Existing popup dimensions, position, surface, outline and proportions.
- Existing Total, DH/rial conversion and denomination breakdown.
- Moroccan conversion rule: `1 DH = 20 rial`.

Do not add Arabic/RTL UI, a top app bar, tabs, decorative doodles, orange accents, Material cards, heavy shadows, gradients, extra section titles or automatic empty rows.

## One baseline system — non-negotiable

Use one shared geometry token such as `JournalRuleSpacing`. Match the visual rhythm in the approved image, approximately 42–44dp between visible rules, then refine through physical-device comparison.

Every visible notebook text baseline must sit directly on its corresponding paper rule:

- `Flous l9or3a`
- Row numbers
- Entry titles
- Amounts
- Currency suffixes
- Delete marks
- Centered add-row `+`
- `Total`
- Primary total
- Secondary conversion

Apply baseline alignment to the actual `Text`, `BasicTextField`, or `innerTextField`, not merely an outer `Row`, `Box`, or container. Calculator keys remain centered inside their cells.

## Highlighter correction

Correct the highlighter geometry behind `Flous l9or3a` and `Total`:

- Center the text vertically inside the marker stroke.
- Keep visible marker space above and below the glyphs.
- Preserve organic, hand-drawn edges.
- Do not use a digital pill or rounded rectangle.
- Use deterministic paths so recomposition does not change the stroke.

## Transaction rows

Display one transaction on every visible ruled line:

```text
1. Naïma        1 200 rial    ×
2. Nadia        5 000 rial    ×
3. Marché       2 600 rial    ×
```

Requirements:

- No unused rule between transactions.
- No double-height visual row.
- Row number, title, amount, suffix and delete mark read as one ledger line.
- Preserve stable internal IDs and use `key(row.id)`.
- Keep adequate touch targets without making rows visually oversized.
- Adjacent row hit areas must not steal each other’s gestures.

## Real cursor and selection behavior

Use `TextFieldValue` or an equivalent state model preserving text, cursor position and selection range. Do not force the cursor to the end after recomposition.

Title editing:

- Tapping the title activates only the title field.
- Open the Android software keyboard.
- Collapse the compact numeric dock.
- Place the cursor at the character position tapped.
- Allow insertion and deletion anywhere in the title.

Amount editing:

- Tapping the amount activates only the amount field.
- Dismiss the software keyboard.
- Expand the compact numeric dock.
- Place a clearly visible cursor inside or after the numeric value.
- Insert numeric-keypad input at the current cursor position.
- Backspace deletes relative to that cursor.
- Prevent malformed decimal input such as `12..3`.

Only one field may appear active at a time.

Active title state may use a very subtle graphite handwritten underline under the title only. Active amount state uses a subtle dusty-pink handwritten underline under the numeric value only. Never highlight the whole row.

## Amount and currency styling

For `5 000 rial`:

- `5 000` uses primary dark graphite.
- `rial` uses lighter muted gray ink.
- The suffix remains readable but secondary.
- The suffix is not part of the editable value.
- The cursor stays inside the numeric field, never after the suffix.

Apply the same hierarchy to primary and secondary totals where appropriate.

## Manual row creation

Remove the automatic trailing empty row completely. Focusing, typing, confirming a calculation or recomposition must never append a row.

Show one centered handwritten `+` directly below the final transaction:

- No label, circle or card.
- It sits naturally on a notebook rule.
- Visible mark remains small and light.
- Invisible touch target is at least 48dp.
- One intentional tap adds exactly one empty row.
- Give the new row the next sequential number.
- Focus its title after creation.
- Scroll only when needed to keep it visible.

If the list is empty, keep the centered `+` available.

Deleting a row deletes only that row, renumbers the visible sequence, preserves stable internal IDs, does not create a replacement, does not transfer focus unexpectedly, and recalculates Total.

## Compact numeric dock

Keep exactly:

```text
1 | 2 | 3 | ⌫
4 | 5 | 6 | .
7 | 8 | 9 | 0
```

- Four equal columns and three compact rows.
- No arithmetic operators or equals key.
- Thin graphite grid with no gaps or individual key cards.
- Preserve the collapse/expand chevron.
- Dock background extends behind the system navigation area.
- Navigation inset applies to inner content only.
- Hide the dock while the calculator popup is visible.

## Calculator popup

Keep the popup exactly as shown in the approved image:

- Same size, position and dimensions.
- Same paper styling, outline and restrained shadow.
- Same close `×`.
- Same dimmed notebook underneath.
- Same expression/result hierarchy.
- Same keypad proportions and pastel operator dabs.

Do not replace it with a default Material `AlertDialog`; use a custom overlay or custom `Dialog` surface.

Exact keypad:

```text
1 | 2 | 3 | +
4 | 5 | 6 | −
7 | 8 | 9 | ×
. | 0 | ⌫ | ÷
= full-width
Confirmer full-width
```

## Separate `=` from `Confirmer`

Pressing `=`:

1. Validates and evaluates the expression safely.
2. Displays the result inside the popup.
3. Does not modify the ledger row.
4. Does not modify Total.
5. Keeps the popup open.

Add the exact French action `Confirmer` below the equals row:

- Full-width inside the popup.
- Dusty-pink organic marker stroke.
- Centered dark handwritten text.
- Minimum 48dp touch height.
- No Material button, gradient or heavy border.
- Balanced paper margin below it.

Pressing `Confirmer`:

1. Requires a valid evaluated result.
2. Writes only the normalized numeric result into the active amount field.
3. Never writes the arithmetic expression into the row.
4. Avoids scientific notation and unnecessary trailing zeros.
5. Recalculates Total and DH/rial conversion.
6. Closes the popup.
7. Keeps the edited row visible.
8. Does not append a row.

If no row is active, open the calculator but keep `Confirmer` disabled until the user explicitly selects or creates a row. Do not silently create one.

Invalid/incomplete expressions or division by zero must show restrained handwritten feedback, keep `Confirmer` disabled, keep the popup open and never mutate the ledger or Total.

The close `×` and Android Back close only the popup and apply nothing. After evaluation, a digit starts a fresh expression while an operator continues from the result.

## State preservation

Keep popup state separate from row-editing state. Preserve safely:

- Rows and stable IDs
- Next row ID
- Category and selected monetary unit
- Active row ID
- Active field type
- Title cursor/selection
- Amount cursor/selection
- Compact dock state
- Popup visibility
- Popup expression
- Popup result
- Popup evaluated state

Rotation must not lose data, duplicate rows, move focus to another row, apply a result, or change the active destination.

## Architecture

Reuse the existing notebook tokens, paper renderer, baseline modifier, offline fonts, sketch-icon system, row state model, `MoneyMath`, Total and breakdown logic. Do not duplicate parsing logic inside Composables or create a competing design system.

Place new drawn icons in the existing sketch-icon component, not the design-token file.

Do not redesign the Home screen, Breakdown Sheet, Options Menu or reset dialog.

## Accessibility

- Interactive controls have at least 48dp touch targets while remaining visually compact.
- Add stable French content descriptions.
- Disabled `Confirmer` exposes disabled semantics and cannot react to taps.
- Add stable test tags/semantics for the calculator icon, title fields, amount fields, add/delete controls, numeric dock, popup, close action, expression, result, equals, `Confirmer`, Total and active row.

## Required tests

Use `src/test` for domain/state tests and `src/androidTest` for Compose interaction tests.

Cover at minimum:

1. One transaction per rule.
2. No automatic trailing row.
3. One `+` tap creates exactly one row.
4. Recomposition does not add rows.
5. Delete creates no replacement.
6. Stable IDs survive deletion/renumbering.
7. Title and amount focus modes are distinct.
8. Cursor can be placed inside title and amount.
9. Numeric insertion/backspace respect cursor position.
10. Double decimal is prevented.
11. Currency suffix is not editable.
12. Compact dock has no arithmetic operators.
13. `600 + 250 + 30 = 880`.
14. `=` changes neither row nor Total.
15. `Confirmer` applies 880, updates Total and closes popup.
16. Confirmation does not add a row.
17. Invalid expression/division by zero changes nothing.
18. Close and Android Back apply nothing.
19. Rotation preserves editing and popup state.
20. DH/rial conversion and denomination breakdown remain correct.

## Build and device verification

Run:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-19'
$env:JAVA_TOOL_OPTIONS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Run connected Compose tests if configured, install the APK on the attached device, and capture screenshots proving:

1. One operation per rule.
2. Correct category and Total highlighter centering.
3. Cursor inside title and amount.
4. Muted currency suffix.
5. Manual centered add-row `+`.
6. Exactly one new row after one tap.
7. Compact keypad and navigation inset.
8. Popup before and after `600 + 250 + 30 = 880`.
9. `Confirmer` applying 880.
10. Invalid expression with disabled confirmation.
11. Close without applying.
12. Baseline alignment close-up.

Compare all captures directly against:

`C:\Users\Joe\Desktop\sarf\design\approved-editing-and-confirm-popup.png`

## Mandatory stop

Implement only this approved calculator-screen refinement. After code changes, tests, APK build, installation, screenshots and a concise walkthrough listing changed files and verified behavior, STOP for user review.

Do not modify other screens, add unrelated features or reinterpret the reference.
