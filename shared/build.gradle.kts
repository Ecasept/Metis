plugins {
    java
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
}

tasks.register("prepareKotlinBuildScriptModel"){}
