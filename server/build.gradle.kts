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

tasks.register("prepareKotlinBuildScriptModel"){}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.ecasept.unitodo.server.Main"
    }

    from(project(":shared").sourceSets.main.get().output)
}