package com.example.receiptscanner.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

/**
 * Supabase client singleton
 * 
 * IMPORTANT: Replace these with your actual Supabase project credentials
 * Get them from: https://app.supabase.com -> Your Project -> Settings -> API
 */
object SupabaseClient {
    // TODO: Replace with your Supabase URL and Anon Key
    private const val SUPABASE_URL = "https://bpaplduibkdvbroagsyr.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJwYXBsZHVpYmtkdmJyb2Fnc3lyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjgyOTQxMjMsImV4cCI6MjA4Mzg3MDEyM30.yqB-KWKoAJEwXHlM7WpVMPa-ml1Cyvm-A-qPsKysnDA"
    
    val client: SupabaseClient? by lazy {
        try {
            createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_ANON_KEY
            ) {
                install(Postgrest)
                install(Storage)
                install(Realtime)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if initialization fails
        }
    }
}

