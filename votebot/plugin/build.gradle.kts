plugins {
    `mikbot-module`
    `mikbot-plugin`
    `mikbot-template`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(projects.votebot.common)
    implementation(projects.votebot.chartServiceClient)
}

mikbotPlugin {
    description.set("Plugin adding VoteBot functionality")
    pluginId.set("votebot")
}

template {
    files.add("VoteBotInfo.java")
    tokens.put("VERSION", project.version)
}
