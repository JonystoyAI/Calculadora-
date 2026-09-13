package com.example.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

@JsonClass(generateAdapter = true)
data class CurrencyResponse(
    val result: String,
    val base_code: String,
    val rates: Map<String, Double>
)

interface CurrencyApiService {
    @GET("v6/latest/USD")
    suspend fun getLatestRates(): CurrencyResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://open.er-api.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: CurrencyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CurrencyApiService::class.java)
    }
}
