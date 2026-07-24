package com.example.daggerpokedex.di

import com.example.daggerpokedex.data.remote.PokeApiService
import com.example.daggerpokedex.di.qualifier.BaseUrl
import com.example.daggerpokedex.di.scope.AppScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Everything needed to talk to the PokéAPI, wired step by step so you can see how
 * Dagger chains providers together.
 *
 * Notice the dependency chain formed purely by method PARAMETERS:
 *
 *   baseUrl(String) ─┐
 *                    ├─> retrofit(OkHttpClient, Moshi, @BaseUrl String) ─> apiService(Retrofit)
 *   okHttpClient ────┤
 *   moshi ───────────┘
 *
 * Dagger reads these signatures and figures out the correct construction order
 * on its own. You never call these methods yourself.
 *
 * Every provider here is `@AppScope`, so each dependency (the OkHttp client, the
 * Retrofit instance, the API) is created ONCE and shared for the whole app —
 * which is exactly what you want for expensive, thread-safe networking objects.
 */
@Module
object NetworkModule {

    @Provides
    @AppScope
    @BaseUrl
    fun provideBaseUrl(): String = "https://pokeapi.co/api/v2/"

    @Provides
    @AppScope
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @AppScope
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @AppScope
    fun provideRetrofit(
        client: OkHttpClient,
        moshi: Moshi,
        @BaseUrl baseUrl: String, // resolved from the qualified provider above
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @AppScope
    fun providePokeApiService(retrofit: Retrofit): PokeApiService =
        retrofit.create(PokeApiService::class.java)
}
