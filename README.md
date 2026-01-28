# Receipt Scanner

An Android app that scans paper receipts with your camera, extracts text with on-device OCR, and stores them locally or in the cloud.

## Features

- **Camera capture** — Take a photo of a receipt using CameraX
- **OCR** — Extract text on-device with Google ML Kit (no internet needed for scanning)
- **Edit & save** — Review, fix, and save merchant, date, and total
- **Local storage** — Receipts saved with Room
- **Supabase sync** — Optional cloud backup and sync across devices (see [SUPABASE_SETUP.md](SUPABASE_SETUP.md))

## Tech Stack

- **Kotlin** with **Jetpack Compose**
- **CameraX** — Camera
- **ML Kit Text Recognition** — On-device OCR
- **Room** — Local SQLite database
- **Supabase** — Postgres, Storage, Realtime (optional)
- **Coil** — Image loading in Compose

## Requirements

- Android Studio (or compatible IDE)
- Android SDK 26+ (min), 35 (target)
- JDK 17

## Getting Started

### 1. Clone and open

```bash
git clone <repo-url>
cd ReceiptScanner
```

Open the project in Android Studio.

### 2. Build and run

- Build: **Build → Make Project**
- Run on device/emulator: **Run → Run 'app'**

### 3. Permissions

The app requests:

- **Camera** — To photograph receipts
- **Internet** — For Supabase sync (if configured)

## Supabase Setup (Optional)

To enable cloud sync:

1. Create a [Supabase](https://supabase.com) project.
2. Add your **Project URL** and **anon key** in `app/src/main/java/com/example/receiptscanner/data/SupabaseClient.kt`.
3. Create the `receipts` table and `receipt-images` Storage bucket in Supabase.

Full steps, SQL, and policy examples: [SUPABASE_SETUP.md](SUPABASE_SETUP.md).

## Project Structure

```
app/src/main/java/com/example/receiptscanner/
├── data/           # Room, Supabase, entities, DAOs, sync
├── navigation/     # Navigation and Screen definitions
├── ui/
│   ├── screens/    # Home, Camera, Preview, Result, List, Detail, Edit
│   └── theme/      # Compose theme, colors, typography
├── utils/          # OCR, image handling, permissions, receipt helpers
├── MainActivity.kt
├── ReceiptScannerApp.kt
├── CameraUtils.kt
```

## License

This project is provided as-is for personal or educational use.
