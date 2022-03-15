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
    pluginId.set("votebot")
}

template {
    files.add("VoteBotInfo.java")
    tokens.put("VERSION", project.version)
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/main/kotlin/"))
        }
    }
}
