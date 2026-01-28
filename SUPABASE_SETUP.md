# Supabase Sync Setup Guide

## Overview
Your Receipt Scanner app now includes Supabase sync functionality! This allows you to:
- Sync receipts across multiple devices
- Backup receipts to the cloud
- Access receipts from anywhere

## Setup Steps

### 1. Create a Supabase Project

1. Go to [https://app.supabase.com](https://app.supabase.com)
2. Sign up or log in
3. Click "New Project"
4. Fill in:
   - **Name**: ReceiptScanner (or any name you prefer)
   - **Database Password**: Choose a strong password (save it!)
   - **Region**: Choose closest to you
5. Click "Create new project"
6. Wait for project to be created (2-3 minutes)

### 2. Get Your API Credentials

1. In your Supabase project dashboard, go to **Settings** → **API**
2. Copy these values:
   - **Project URL** (looks like: `https://xxxxx.supabase.co`)
   - **anon/public key** (long string starting with `eyJ...`)

### 3. Configure the App

1. Open `app/src/main/java/com/example/receiptscanner/data/SupabaseClient.kt`
2. Replace the placeholder values:
   ```kotlin
   private const val SUPABASE_URL = "YOUR_SUPABASE_URL"  // Your Project URL
   private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"  // Your anon key
   ```

### 4. Create the Database Table

1. In Supabase dashboard, go to **SQL Editor**
2. Click "New Query"
3. Paste this SQL and run it:

```sql
-- Create receipts table
CREATE TABLE receipts (
    id TEXT PRIMARY KEY,
    merchant TEXT NOT NULL,
    date TEXT,
    total TEXT,
    image_url TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- Enable Row Level Security (RLS)
ALTER TABLE receipts ENABLE ROW LEVEL SECURITY;

-- Create policy to allow all operations (for now)
-- In production, you should add proper authentication
CREATE POLICY "Allow all operations" ON receipts
    FOR ALL
    USING (true)
    WITH CHECK (true);

-- Create index for faster queries
CREATE INDEX idx_receipts_created_at ON receipts(created_at DESC);
```

### 5. Create Storage Bucket

1. In Supabase dashboard, go to **Storage**
2. Click "New bucket"
3. Name: `receipt-images`
4. **Public bucket**: ✅ Check this (or configure proper access policies)
5. Click "Create bucket"

### 6. Configure Storage Policies

1. Go to **Storage** → **Policies** → `receipt-images`
2. Click "New Policy"
3. Select "For full customization"
4. Name: `Allow all operations`
5. Policy definition:
   ```sql
   (bucket_id = 'receipt-images'::text)
   ```
6. Check all operations (SELECT, INSERT, UPDATE, DELETE)
7. Click "Review" → "Save policy"

## Features

### Automatic Sync
- When you save a receipt, it automatically syncs to Supabase
- When you update a receipt, changes sync automatically
- When you delete a receipt, it's removed from Supabase

### Manual Sync
- Tap the refresh icon (🔄) in the receipt list to sync all unsynced receipts
- Synced receipts show a ✓ checkmark

### Sync Status
- Receipts show a ✓ if they're synced
- Unsynced receipts will sync automatically when you save/update them
- If sync fails, the receipt is still saved locally

## Troubleshooting

### Sync Not Working?
1. Check your Supabase URL and API key in `SupabaseClient.kt`
2. Verify the database table exists and has correct structure
3. Check that Storage bucket `receipt-images` exists
4. Check network connectivity

### Images Not Uploading?
1. Verify Storage bucket is created
2. Check Storage policies allow uploads
3. Check file size limits (Supabase default is 50MB)

### Database Migration Issues?
If you get database errors, you may need to:
1. Uninstall and reinstall the app (this resets local database)
2. Or manually update the database schema

## Security Notes

⚠️ **Important**: The current setup allows public access. For production:

1. **Add Authentication**: Implement Supabase Auth
2. **Row Level Security**: Update RLS policies to restrict access per user
3. **Storage Policies**: Restrict image access to authenticated users only
4. **Use Service Role Key**: For server-side operations (never expose in client)

## Next Steps

- Add user authentication
- Implement conflict resolution for multi-device sync
- Add sync status indicator in UI
- Add background sync with WorkManager
- Add sync error retry mechanism

## Support

For Supabase help:
- [Supabase Docs](https://supabase.com/docs)
- [Supabase Discord](https://discord.supabase.com)

