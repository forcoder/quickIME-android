# Release signing configuration
# Note: For production builds, you need to set up signing keys in GitHub Secrets

android {
    signingConfigs {
        create("release") {
            // In CI/CD, these will be loaded from environment variables
            // Generate a keystore locally and upload as GitHub Secret
            storeFile = file("release-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("KEY_ALIAS") ?: "quickime"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.release
            minifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
