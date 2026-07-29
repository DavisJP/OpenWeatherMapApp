plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinCompose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.mokkery) apply false
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
