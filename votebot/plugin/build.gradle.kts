plugins {
    `mikbot-module`
    `mikbot-plugin`
    `mikbot-template`
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(projects.votebot.common)
    implementation(projects.votebot.chartServiceClient)
    implementation(libs.java.string.similarity)
    ksp(libs.kordex.processor)
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Plugin adding VoteBot functionality")
    bundle.set("votebot")
    pluginId.set("votebot")
}

template {
    packageName.set("space.votebot")
    className.set("VoteBotInfo")
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/main/kotlin/"))
        }
    }
}
