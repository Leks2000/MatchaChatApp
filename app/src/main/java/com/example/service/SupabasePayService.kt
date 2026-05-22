package com.example.service

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface SupabasePayApi {
    @POST("functions/v1/create_yookassa_checkout")
    suspend fun createYooKassaCheckout(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body request: CheckoutRequest
    ): CheckoutResponse

    @POST("functions/v1/create_yookassa_payment")
    suspend fun createYooKassaPayment(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body request: PaymentRequest
    ): PaymentResponse
}

data class CheckoutRequest(
    val user_id: String,
    val type: String
)

data class CheckoutResponse(
    val confirmation_url: String?,
    val error: String?
)

data class PaymentRequest(
    val payment_token: String,
    val type: String,
    val user_id: String,
    val save_payment_method: Boolean = false
)

data class PaymentResponse(
    val payment_id: String?,
    val status: String?,
    val confirmation_url: String?,
    val error: String?
)

object SupabasePayService {
    private const val TAG = "SupabasePayService"
    
    // Dynamic fallback matching user's active backend and Supabase credentials
    var SUPABASE_URL = "https://aonpcmxaeksfytowthx.supabase.co/"
    var SUPABASE_ANON_KEY = "gsk_placeholder_api_key_value" // Will fallback to user's config key if provided
    
    private val api: SupabasePayApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            
        retrofit.create(SupabasePayApi::class.java)
    }

    suspend fun getCheckoutUrl(userId: String, type: String): String? {
        return try {
            val response = api.createYooKassaCheckout(
                apiKey = SUPABASE_ANON_KEY,
                authHeader = "Bearer $SUPABASE_ANON_KEY",
                request = CheckoutRequest(userId, type)
            )
            response.confirmation_url
        } catch (e: Exception) {
            Log.e(TAG, "Failed creating YooKassa checkout session", e)
            null
        }
    }

    suspend fun executePaymentToken(token: String, userId: String, type: String): PaymentResponse {
        return try {
            api.createYooKassaPayment(
                apiKey = SUPABASE_ANON_KEY,
                authHeader = "Bearer $SUPABASE_ANON_KEY",
                request = PaymentRequest(token, type, userId)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed submitting YooKassa tokenized payment", e)
            PaymentResponse(null, null, null, e.localizedMessage ?: "Unknown network error")
        }
    }
}
