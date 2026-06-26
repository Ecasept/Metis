plugins {
    java
    application
}

application {
    mainClass.set("dev.ecasept.unitodo.client.Main")
}

// Fix IntelliJ warning
tasks.register("prepareKotlinBuildScriptModel"){}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.ecasept.unitodo.client.Main"
    }

    from(project(":shared").sourceSets.main.get().output)
}


val trustAllCertificates: String by project
val baseUrl: String by project


sourceSets {
    create("defaultCerts")
    create("trustAllCerts")
}

dependencies {

    // Add the shared module as a dependency for all source sets
    implementation(project(":shared"))
    "defaultCertsImplementation"(project(":shared"))
    "trustAllCertsImplementation"(project(":shared"))

    // Make the client depend on the corresponding source set
    if (trustAllCertificates.toBoolean()) {
        implementation(sourceSets["trustAllCerts"].output)
    } else {
        implementation(sourceSets["defaultCerts"].output)
    }
}

val generateBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/sources/buildConfig/java/main")

    outputs.dir(outputDir)

    doLast {
        val outputFile = outputDir.get().file("dev/ecasept/unitodo/build/BuildConfig.java").asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText("""
            package dev.ecasept.unitodo.build;

            public class BuildConfig {
                public static final String BASE_URL = "$baseUrl";
            }
        """.trimIndent())
    }
}

java.sourceSets["main"].java.srcDir(generateBuildConfig)
tasks.compileJava {
    dependsOn(generateBuildConfig)
}