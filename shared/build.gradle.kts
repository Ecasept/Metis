plugins {
    java
}

dependencies {
    implementation(libs.xerial.sqlite)
}

tasks.register("prepareKotlinBuildScriptModel"){}
