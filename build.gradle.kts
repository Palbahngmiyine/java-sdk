import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("org.jetbrains.dokka") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
    `java-library`
    `maven-publish`
    signing
}

group = "net.nurigo"
version = "4.4.0"

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

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:2.0.0")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<ShadowJar>("shadowJar") {
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

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(mapOf("Main-Class" to "net.nurigo.sdk.NurigoApp"))
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf(
        "-opt-in=kotlin.RequiresOptIn",
        "-opt-in=kotlin.time.ExperimentalTime"
    ))
    jvmTarget.set(JvmTarget.JVM_1_8)
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.compilerOptions {
    freeCompilerArgs.set(listOf(
        "-opt-in=kotlin.RequiresOptIn",
        "-opt-in=kotlin.time.ExperimentalTime"
    ))
    jvmTarget.set(JvmTarget.JVM_1_8)
}

tasks.dokkaHtml.configure {
    outputDirectory.set(rootDir.resolve("docs"))
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

            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                name.set("SOLAPI SDK")
                description.set("SOLAPI 및 SOLAPI 계열(쿨에스엠에스 등) 서비스에서 사용되는 문자 발송용 SDK")
                url.set("https://github.com/nurigo/java-sdk")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
