package com.blockchain.koin.modules

import android.os.Build
import com.blockchain.network.modules.OkHttpInterceptors
import com.facebook.stetho.okhttp3.StethoInterceptor
import org.koin.dsl.module.applicationContext
import piuk.blockchain.android.BuildConfig
import piuk.blockchain.androidcore.data.api.EnvironmentConfig
import piuk.blockchain.androidcore.data.api.interceptors.ApiInterceptor
import piuk.blockchain.androidcore.data.api.interceptors.UserAgentInterceptor

val apiInterceptorsModule = applicationContext {

    bean {
        val env: EnvironmentConfig = get()
        if (env.shouldShowDebugMenu()) {
            OkHttpInterceptors(
                listOf(
                    // Stetho for debugging network ops via Chrome
                    StethoInterceptor(),
                    // Add logging for debugging purposes
                    ApiInterceptor(),
                    // Add header in all requests
                    UserAgentInterceptor(BuildConfig.VERSION_NAME, Build.VERSION.RELEASE)
                )
            )
        } else {
            OkHttpInterceptors(
                listOf(
                    // Add header in all requests
                    UserAgentInterceptor(BuildConfig.VERSION_NAME, Build.VERSION.RELEASE)
                )
            )
        }
    }
}
