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

tasks.register("prepareKotlinBuildScriptModel"){}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.ecasept.unitodo.client.Main"
    }

    from(project(":shared").sourceSets.main.get().output)
}





sourceSets {
    main {
    }
    create("dev") {
        java.srcDir("src/dev/java")
        compileClasspath += sourceSets["main"].compileClasspath
        runtimeClasspath += sourceSets["main"].runtimeClasspath
    }
}

// Create tasks for each
tasks.register<JavaExec>("runDev") {
    classpath = sourceSets["dev"].runtimeClasspath + sourceSets["main"].runtimeClasspath
    mainClass.set("dev.ecasept.unitodo.client.Main")
}
