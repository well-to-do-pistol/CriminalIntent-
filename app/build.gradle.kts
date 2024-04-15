plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
}

android {
    namespace = "com.bignerdranch.android.criminalintent"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bignerdranch.android.criminalintent"
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

    //Room 架构导出功能需要一个相对于项目目录的路径，它可以在编译时写入架构文件，而不是在设备上运行时写入架构文件。
    //**使用项目相对路径**：常见做法是将架构导出放置在项目中的 `schemas` 目录下。 如果该目录不存在，您可以创建该目录并相应地更新您的“build.gradle”文件。 例如：
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }//此配置告诉 Room 将架构文件保存在项目根目录的“schemas”目录中。 `$projectDir` 是一个 Gradle 属性，指向项目的根目录。


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    //ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    //
    //RecyclerView
    implementation(libs.androidx.recyclerview)
    //
    //Room数据库
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    //
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}