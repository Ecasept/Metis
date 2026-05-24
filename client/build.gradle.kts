plugins {
    java
    application
}

application {
    mainClass.set("dev.ecasept.unitodo.client.Main")
}

dependencies {
    implementation(project(":shared"))
}