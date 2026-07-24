plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP runs the Dagger annotation processor. Dagger reads our @Component /
    // @Module / @Inject annotations at build time and GENERATES the code that
    // wires the object graph together (e.g. DaggerAppComponent).
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.daggerpokedex"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.daggerpokedex"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    // Compile Kotlin against a JDK 17 toolchain. Gradle auto-provisions it if the
    // machine only has a newer JDK, which keeps Dagger's generated code portable.
    jvmToolchain(17)

    compilerOptions {
        // Apply annotations like Moshi's @Json to both the constructor parameter
        // and the generated property (the forward-compatible Kotlin behaviour).
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    // --- Compose UI ----------------------------------------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.animation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    // --- Dagger --------------------------------------------------------------
    // `dagger` is the runtime library (@Inject, @Provides, Provider, Lazy...).
    implementation(libs.dagger)
    // `dagger-compiler` is the code generator; it only runs at build time, so it
    // is attached to KSP rather than shipped inside the APK.
    ksp(libs.dagger.compiler)

    // --- Networking ----------------------------------------------------------
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // --- Images --------------------------------------------------------------
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // --- Tests ---------------------------------------------------------------
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
