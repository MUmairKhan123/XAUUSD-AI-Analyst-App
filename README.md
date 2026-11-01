# XAUUSD AI Analyst (Android)

Kotlin + Jetpack Compose app for a **manual** XM MT5 XAUUSD workflow:
upload H4/H1/M15/M5/M1 screenshots -> generate a detailed SMC prompt -> hand it to ChatGPT
(no API key) -> paste the answer back -> review, risk-check and save locally.

## What is automatic vs manual
| Automatic (in the app) | Manual (you) |
|---|---|
| Screenshot storage and preview | Taking MT5 screenshots |
| Prompt generation, copy, Android share sheet | Attaching/sending the 5 images in ChatGPT |
| Reading BUY/SELL/WAIT + levels from pasted text (editable) | Copying ChatGPT's answer back |
| Risk amount and lot-size maths | Entering XM contract specs, checking spread and news |
| Local history (Room database) | Every trade decision and order in MT5 |

## Limitations (honest list)
- Not connected to ChatGPT, XM or MT5. No live prices, no news feed, no trade execution, no login.
- No INTERNET permission is requested; the app never sends data anywhere.
- Multi-image sharing to ChatGPT depends on Android and the ChatGPT app. If it fails, copy the prompt and attach the five images manually.
- AI chart reading can be wrong. Nothing here is a guarantee or financial advice.
- Lot size is calculated ONLY from XM values you enter (contract size, tick size, tick value, currency, SL distance). Missing values => no lot size is shown.
- This source was written without a compile step in the authoring environment. If Gradle reports an error, send me the log and I will fix it.

## Get an installable APK

### Option A - GitHub Actions (no Android Studio needed)
1. Create a free GitHub account and a new repository.
2. Upload ALL files of this project (keep the `.github` folder) to the `main` branch.
3. Open the repository's **Actions** tab -> **Build APK** -> **Run workflow** (it also runs on every push).
4. When it is green, open the run and download the artifact **XAUUSD-AI-Analyst-APK** (a zip containing `app-debug.apk`).

### Option B - Android Studio
1. Install Android Studio (Koala or newer), choose **Open**, select this folder, wait for Gradle sync (needs internet once).
2. **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
3. APK: `app/build/outputs/apk/debug/app-debug.apk`.
Command line (with Gradle 8.9 and JDK 17): `gradle assembleDebug`.

## Install on your phone
1. Copy `app-debug.apk` to the phone (USB, Drive, Telegram, etc.) and tap it.
2. When asked, allow **Install unknown apps** for the app you opened it from.
3. If Play Protect warns about an unrecognised app, tap **Install anyway** (it is your own build).
4. Open **XAUUSD AI Analyst**.

## How to use
1. **Settings**: check balance (default 100), risk (2% = 2.00), max positions (2), daily loss limit and spread limit. Fill XM contract specs for lot size (MT5: right-click XAUUSD in Market Watch > Specification).
2. **Charts**: add the five screenshots (H4, H1, M15, M5, M1).
3. **Prompt**: tap *Share prompt + screenshots* or *Copy prompt*, then open ChatGPT. If images are missing there, attach all five manually and paste the prompt.
4. **Result**: paste ChatGPT's reply, fix the detected decision/levels if needed, review warnings and the lot calculation, then **Save to history**.
5. **History**: reopen, review or delete past analyses.
Before any trade, check live spread, high-impact news, open positions and daily loss limit yourself. No martingale, grid or averaging down.
