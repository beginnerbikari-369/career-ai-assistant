plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
	id("com.google.gms.google-services")
}

android {
    namespace = "com.careerai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.careerai"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Load API keys from local.properties
        val localProperties = java.util.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        
        buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties.getProperty("OPENAI_API_KEY", "")}\"")
        buildConfigField("String", "GOOGLE_CALENDAR_CLIENT_ID", "\"${localProperties.getProperty("GOOGLE_CALENDAR_CLIENT_ID", "")}\"")
        
        // Manifest placeholders
        manifestPlaceholders["OPENAI_API_KEY"] = localProperties.getProperty("OPENAI_API_KEY", "")
        manifestPlaceholders["GOOGLE_CALENDAR_CLIENT_ID"] = localProperties.getProperty("GOOGLE_CALENDAR_CLIENT_ID", "")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("com.google.android.material:material:1.11.0")
    
    // Compose BOM and UI
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("com.google.dagger:hilt-compiler:2.48.1")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Work Manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Calendar Integration
    implementation("com.google.api-client:google-api-client-android:2.2.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.json", module = "json")
    }
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.json", module = "json")
    }
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Additional Google APIs
    implementation("com.google.api-client:google-api-client-gson:2.2.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.json", module = "json")
    }
    implementation("com.google.http-client:google-http-client-gson:1.43.3") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }
    implementation("com.google.http-client:google-http-client:1.43.3") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Charts and Analytics
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Date and Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Lifecycle extensions
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.work:work-testing:2.9.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // Debug implementations
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}