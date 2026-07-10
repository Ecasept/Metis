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

tasks {
    val fatJar = register<Jar>("fatJar") {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest {
            attributes["Main-Class"] = "dev.ecasept.unitodo.server.Main"
        }

        from(sourceSets.main.get().output)

        from(configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) })
    }

    build {
        dependsOn(fatJar)
    }
}