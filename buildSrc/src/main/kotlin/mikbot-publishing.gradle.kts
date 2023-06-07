import java.util.*

plugins {
    `maven-publish`
    signing
    java
    com.google.cloud.artifactregistry.`gradle-plugin`
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    destinationDirectory.set(buildDir)
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.schlaubi"
            artifactId = "mikbot-${project.name}"
            afterEvaluate {
                version = project.version as String
            }

            from(components["java"])
            artifact(sourcesJar)


            pom {
                name.set("mikbot")
                description.set("A modular framework for building Discord bots")
                url.set("https://github.com/DRSchlaubi/mikmusic")

                organization {
                    name.set("Schlaubi")
                    url.set("https://github.com/DRSchlaubi")
                }

                developers {
                    developer {
                        name.set("Michael Rittmeister")
                    }
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/DRSchlaubi/mikmusic/issues")
                }

                licenses {
                    license {
                        name.set("Apache 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/DRSchlaubi/mikmusic.git")
                    developerConnection.set("scm:git:ssh://git@github.com:DRSchlaubi/mikmusic.git")
                    url.set("https://github.com/DRSchlaubi/mikmusic.git")
                }
            }

            repositories {
                maven("artifactregistry://europe-west3-maven.pkg.dev/mik-music/mikbot") {
                    credentials {
                        username = "_json_key_base64"
                        password = System.getenv("GOOGLE_KEY")?.toByteArray()?.let {
                            Base64.getEncoder().encodeToString(it)
                        }
                    }

                    authentication {
                        create<BasicAuthentication>("basic")
                    }
                }
            }
        }
    }
}

signing {
    val signingKey = System.getenv("SIGNING_KEY")?.toString()
    val signingPassword = System.getenv("SIGNING_KEY_PASSWORD")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(String(Base64.getDecoder().decode(signingKey)), signingPassword)
        sign(publishing.publications["maven"])
    }
}
