plugins {
    java
    application
}

application {
    mainClass.set("dev.ecasept.unitodo.server.Main")
}

dependencies {
    implementation(project(":shared"))
}