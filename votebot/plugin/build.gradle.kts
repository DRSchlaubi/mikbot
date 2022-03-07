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
    implementation("info.debatty", "java-string-similarity", "2.0.0")
    ksp("com.kotlindiscord.kord.extensions", "annotation-processor", "1.5.2-SNAPSHOT")
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
