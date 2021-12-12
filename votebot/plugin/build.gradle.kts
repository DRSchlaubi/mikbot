plugins {
    `mikbot-module`
    `mikbot-plugin`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(projects.votebot.common)
    implementation(projects.votebot.chartServiceClient)
}
