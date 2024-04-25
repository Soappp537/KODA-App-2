val espressoCore = "androidx.test.espresso:espresso-core:3.4.0"
val espressoContrib = "androidx.test.espresso:espresso-contrib:3.4.0"
val junitExt = "androidx.test.ext:junit:1.1.3"
val espressoIntents = "androidx.test.espresso:espresso-intents:3.4.0"
val espressoWeb = "androidx.test.espresso:espresso-web:3.4.0"
val junit = "junit:junit:4.13.2"
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.kodaapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kodaapplication"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures{
        viewBinding = true
    }
    dependencies {
        // Import the Firebase BoM
        implementation(platform("com.google.firebase:firebase-bom:32.8.1"))



        // TODO: Add the dependencies for Firebase products you want to use
        // When using the BoM, don't specify versions in Firebase dependencies
        implementation("com.google.firebase:firebase-analytics")


        // Add the dependencies for any other desired Firebase products
        // https://firebase.google.com/docs/android/setup#available-libraries
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.firebaseui:firebase-ui-auth:8.0.0")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.11.1")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("com.google.firebase:firebase-firestore:24.11.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.ar:core:1.42.0")
    implementation("androidx.test:core-ktx:1.5.0")
    implementation("org.testng:testng:6.9.6")
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0-alpha03")
    implementation ("org.tensorflow:tensorflow-lite:2.8.0")
    // This dependency adds the necessary TF op support.
    implementation ("org.tensorflow:tensorflow-lite-select-tf-ops:2.8.0")

    //noinspection GradleDependency
    testImplementation ("org.robolectric:robolectric:4.5.1")
    testImplementation ("org.robolectric:shadows-framework:4.5.1")
    testImplementation ("androidx.test.ext:junit:1.2.0-alpha03")
    testImplementation ("org.robolectric:annotations:4.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.0-alpha03")
    androidTestImplementation(espressoCore)
    androidTestImplementation(espressoContrib)
    androidTestImplementation(junitExt)
    androidTestImplementation(espressoIntents)
    androidTestImplementation(espressoWeb)
    testImplementation(junit)
    androidTestImplementation ("org.hamcrest:hamcrest-library:1.3")

}

