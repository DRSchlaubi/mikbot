import java.util.*

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
    `maven-publish`
    signing
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kord.common)
                api(libs.lavalink.protocol)
                implementation(libs.ktor.resources)
                implementation(libs.kotlinx.serialization.json)
                api(libs.lavakord.sponsorblock)
            }
        }
    }
}

publishing {
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

signing {
    val signingKey = System.getenv("SIGNING_KEY")?.toString()
    val signingPassword = System.getenv("SIGNING_KEY_PASSWORD")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(String(Base64.getDecoder().decode(signingKey)), signingPassword)
        publishing.publications.forEach { sign(it) }
    }
}
