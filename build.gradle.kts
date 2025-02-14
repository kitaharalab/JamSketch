import org.jetbrains.kotlin.compilerRunner.toArgumentStrings

plugins {
    kotlin("jvm") version "2.0.20"
    application

    // deprecated?
    id("edu.sc.seis.launch4j") version "3.0.6"
}


repositories {
    google()
    mavenCentral()
    maven { url = uri("https://artifacts.alfresco.com/nexus/content/repositories/public/") }

    // jahmm
    maven { url = uri("https://maven.scijava.org/content/groups/public/") }
}

application {
    // Define the main class for the application.
    mainClass = "jp.kthrlab.jamsketch.view.JamSketch"
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
            srcDir("$projectDir/resources")
        }
    }
}


dependencies {
//    implementation fileTree(dir: "libs", include: ["*.jar"], exclude: [])
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"), "exclude" to listOf<String>())))


    // cmx dependencies
    // https://mvnrepository.com/artifact/org.apache.commons/commons-math
    implementation("org.apache.commons:commons-math:2.2")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-math3
    implementation("org.apache.commons:commons-math3:3.6.1")
    // https://mvnrepository.com/artifact/be.ac.ulg.montefiore.run.jahmm/jahmm
    implementation("be.ac.ulg.montefiore.run.jahmm:jahmm:0.6.2")

    // https://mvnrepository.com/artifact/org.apache.ivy/ivy
    implementation("org.apache.ivy:ivy:2.4.0")

    // https://mvnrepository.com/artifact/nz.ac.waikato.cms.weka/weka-stable
    implementation("nz.ac.waikato.cms.weka:weka-stable:3.6.14")
    // https://mvnrepository.com/artifact/com.googlecode.javacpp/javacpp
    implementation("com.googlecode.javacpp:javacpp:0.7")
    // https://mvnrepository.com/artifact/com.googlecode.javacv/javacv
    implementation("com.googlecode.javacv:javacv:0.1")
    // https://mvnrepository.com/artifact/it.unimi.dsi/fastutil
    implementation("it.unimi.dsi:fastutil:8.5.6")
    //https://mvnrepository.com/artifact/javazoom/jlayer
    implementation("javazoom:jlayer:1.0.1")
    //https://mvnrepository.com/artifact/commons-logging/commons-logging
    implementation("commons-logging:commons-logging:1.2")

    // TensorFlow
    // https://mvnrepository.com/artifact/org.tensorflow/tensorflow-core-platform
    implementation("org.tensorflow:tensorflow-core-platform:1.0.0-rc.2")

    // added 20221212 yonamine yonamine blow 4 implementation
    implementation("xml-resolver:xml-resolver:1.2")

    // https://mvnrepository.com/artifact/xerces/xercesImpl
    implementation("xerces:xercesImpl:2.12.2")

    // https://mvnrepository.com/artifact/xalan/xalan
    implementation("xalan:xalan:2.7.2")

    // https://mvnrepository.com/artifact/xalan/serializer
    implementation("xalan:serializer:2.7.2")

    // https://mvnrepository.com/artifact/javax.websocket/javax.websocket-api
    compileOnly("javax.websocket:javax.websocket-api:1.1")

    implementation("org.glassfish.tyrus:tyrus-server:2.0.0")
    implementation("org.glassfish.tyrus:tyrus-container-grizzly-server:2.0.0")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.5")

    // Kotlin
    //// JSON
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")

}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.register<JavaExec>("runApp") {
    mainClass.set("jp.kthrlab.jamsketch.view.JamSketchMultichannel")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("jp.kthrlab.jamsketch.view.JamSketchMultichannel")
}

task("printEnv") {
//    println(projectDir)
    println(sourceSets["main"].resources.srcDirs)
//    println("sourceSets.main.output.asPath = ${sourceSets["main"].output.asPath}")
}

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // または DuplicatesStrategy.FAIL
}

val customJarDir = layout.buildDirectory.dir("launch4j")

//tasks.register<Jar>("jamsketchJar") {
val jamsketchJar = task<Jar>("jamsketchJar") {
    //Exclude the duplicate dependencies.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    destinationDirectory.set(file(customJarDir))

//    archiveClassifier.set("all")
    from(sourceSets.main.get().output)

    // Include runtime dependencies
//    dependsOn(configurations.runtimeClasspath)

    exclude("configs/**")
    exclude("expressive/**")
    exclude("images/**")
    exclude("models/**")
    exclude("music/**")
    exclude("tf/**")

//    from({
//        configurations.runtimeClasspath.get().map {
//            if (it.isDirectory) it else zipTree(it)
//        }
//    })

    //Specify the main class for manifest file.
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

// copy config.json before launch4j
val copyConfig = tasks.register<Copy>("copyConfig") {
    from("resources")
    into( layout.buildDirectory.dir("launch4j/resources"))
}

tasks.build {
    dependsOn(jamsketchJar)
}

//Build exe file.
launch4j {
    mainClassName = application.mainClass.get()
    cmdLine.set("\"jp.kthrlab.jamsketch.view.JamSketchMultichannel\"")
//     icon = "${projectDir}/resources/images/ic_launcher.ico"

    //Specify the jar task included in build.gradle.
    setJarTask(jamsketchJar)

    // add classpath
    classpath.add("JamSketch.jar")
    classpath.add("resources".plus(File.separator))
    classpath.add("lib".plus(File.separator).plus("*.jar"))

    // Launch4j by default wraps jars in native executables,
    // you can prevent this by setting <dontWrapJar> to true.
    dontWrapJar = true

    // The default URL only allows downloads up to version 8.
    downloadUrl = "https://www.oracle.com/java/technologies/downloads/"
}

tasks.named("createExe") {
    dependsOn(copyConfig)
}

