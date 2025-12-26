## Tools

This folder contains helper scripts for preparing assets.

### Prepare Euro images (banknotes + coins) for `assets/europic/`

Because realistic euro banknote/coin images can have usage restrictions, this repo does **not** bundle them.
Instead, you can download images you are allowed to use, then run a script to apply a visible **SPECIMEN**
watermark and standardized sizing, and copy them into the app’s assets.

#### 1) Put your source PNGs here

- Folder: `tools/input/euro_raw/`
- Expected filenames (recommended):
  - Banknotes: `5eur.png`, `10eur.png`, `20eur.png`, `50eur.png`, `100eur.png`, `200eur.png`, `500eur.png`
  - Coins: `1cent.png`, `2cent.png`, `5cent.png`, `10cent.png`, `20cent.png`, `50cent.png`, `1eur.png`, `2eur.png`

#### 2) Run the script (Windows PowerShell)

From repo root:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\prepare_euro_assets.ps1
```

Outputs:
- `app/src/main/assets/europic/*.png`

#### Notes
- This script is **not legal advice**. You are responsible for ensuring your usage complies with applicable rules.




