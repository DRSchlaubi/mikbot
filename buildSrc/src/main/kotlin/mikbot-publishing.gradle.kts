import java.util.*

plugins {
    `maven-publish`
    signing
    java
}

tasks {
    val sourcesJar = task<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        destinationDirectory.set(buildDir)
        from(sourceSets["main"].allSource)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "dev.schlaubi"
                artifactId = "mikbot-${project.name}"
                version = Project.version

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
                    maven("https://schlaubi.jfrog.io/artifactory/mikbot/") {
                        credentials {
                            username = System.getenv("JFROG_USER")
                            password = System.getenv("JFROG_PASSWORD")
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
        }
        sign(publishing.publications["maven"])
    }
}
