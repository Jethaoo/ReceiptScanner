package com.example.receiptscanner.data

import com.example.receiptscanner.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Supabase client singleton.
 *
 * Set `supabase.url` and `supabase.anon.key` in **local.properties** (see SUPABASE_SETUP.md in the project root).
 * Those values are injected into [BuildConfig] at compile time and are not committed to git.
 */
object SupabaseClient {

    private val supabaseUrl: String = BuildConfig.SUPABASE_URL
    private val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY

    val client: SupabaseClient? by lazy {
        if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) {
            null
        } else {
            try {
                createSupabaseClient(
                    supabaseUrl = supabaseUrl,
                    supabaseKey = supabaseAnonKey
                ) {
                    install(Postgrest)
                    install(Storage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
