
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.recetariosocial"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.recetariosocial"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Habilitar compatibilidad Java 8 para Retrofit
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    
    // Eliminamos dependencias de Room ya que usaremos Supabase
    // implementation(libs.room.runtime.jvm)
    // annotationProcessor(libs.room.compiler)
    
    // --- NUEVAS DEPENDENCIAS PARA SUPABASE ---
    // Retrofit (Cliente HTTP)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Convertidor JSON (Gson)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp (Logging y Cliente base)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Glide para cargar imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}