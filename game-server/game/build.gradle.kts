plugins {
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":game-server:cache"))
    implementation(project(":game-server:common"))
    implementation(project(":game-server:database"))
}
