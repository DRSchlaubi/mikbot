plugins {
    kotlin("jvm")
    application
}

val plugins by configurations.creating

dependencies {
    api(project(":"))
    plugins(project(":core:game-animator"))
    plugins(project(":core:gdpr"))
    plugins(project(":core:database-i18n"))
    plugins(project(":core:redeploy-hook"))

    if (System.getenv("CI") == "TRUE") {
        plugins(project(":utils:verification"))
    }
}
