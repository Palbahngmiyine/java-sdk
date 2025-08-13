import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("org.jetbrains.dokka") version "2.0.0"
    id("org.jetbrains.dokka-javadoc") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.8"
    java
    `java-library`
    `maven-publish`
    signing
}

group = "net.nurigo"
version = "5.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("commons-codec:commons-codec:1.18.0")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.1.0")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:2.0.0")
}

val generatedSrcDir = layout.buildDirectory.dir("generated/source/kotlin")

sourceSets.main.get().java.srcDir(generatedSrcDir)

val generateVersionFile by tasks.register("generateVersionFile") {
    val file = generatedSrcDir.get().file("net/nurigo/sdk/Version.kt")
    outputs.file(file)
    doLast {
        file.asFile.parentFile.mkdirs()
        file.asFile.writeText("""|package net.nurigo.sdk
                               |
                               |internal object Version {
                               |    const val SDK_VERSION = "$version"
                               |}
                               |""".trimMargin())
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateVersionFile)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named("sourcesJar") {
    dependsOn(generateVersionFile)
}

tasks.shadowJar {
    isEnableRelocation = true
    relocationPrefix = "com.solapi.shadow"

    mergeServiceFiles()

    exclude("**/*.kotlin_metadata")
    exclude("**/*.kotlin_builtins")

    archiveClassifier.set("")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf(
        "-opt-in=kotlin.time.ExperimentalTime"
    ))
    jvmTarget.set(JvmTarget.JVM_1_8)
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.compilerOptions {
    freeCompilerArgs.set(listOf(
        "-opt-in=kotlin.time.ExperimentalTime"
    ))
    jvmTarget.set(JvmTarget.JVM_1_8)
}

tasks.withType<DokkaTaskPartial>().configureEach {
    outputDirectory.set(project.rootDir.resolve("docs"))
}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    // v2 작업명에 의존
    val genJavadoc = tasks.named("dokkaGeneratePublicationJavadoc", DokkaGeneratePublicationTask::class)
    dependsOn(genJavadoc)

    // v2 작업의 산출 디렉터리를 JAR에 포함
    from(genJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    val genHtml = tasks.named("dokkaGeneratePublicationHtml", DokkaGeneratePublicationTask::class)
    dependsOn(genHtml)
    from(genHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

val ossusername: String by project
val osspassword: String by project

publishing {
    repositories {
        maven {
            name = "oss"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                username = ossusername
                password = osspassword
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = group.toString()
            artifactId = "sdk"
            version = version

            from(components["shadow"])

            artifact(tasks.named("sourcesJar"))
            artifact(dokkaJavadocJar)

            pom {
                name.set("SOLAPI SDK")
                description.set("SOLAPI 및 SOLAPI 계열(쿨에스엠에스 등) 서비스에서 사용되는 문자 발송용 SDK")
                url.set("https://github.com/nurigo/java-sdk")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("SOLAPI Contact")
                        email.set("contact@SOLAPI.com")
                        organization.set("SOLAPI Inc")
                    }
                    developer {
                        name.set("Lucy Lee")
                        email.set("lucy@solapi.com")
                        organization.set("SOLAPI Inc")
                    }
                    developer {
                        id.set("hosy")
                        name.set("Hosy Lee")
                        email.set("hosy@solapi.com")
                        organization.set("SOLAPI Inc")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/nurigo/java-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com/nurigo/java-sdk.git")
                    url.set("https://github.com/nurigo/java-sdk")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
