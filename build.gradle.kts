import net.minecraftforge.gradle.common.util.RunConfig

plugins {
    java
    `maven-publish`
    id("net.minecraftforge.gradle") version "6.0.+"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    kotlin("jvm") version "2.0.21"
}

val modId: String = project.property("mod_id") as String
val modVersion: String = project.property("mod_version") as String
val modGroupId: String = project.property("mod_group_id") as String
val minecraftVersion: String = project.property("minecraft_version") as String
val forgeVersion: String = project.property("forge_version") as String
val parchmentMc: String = project.property("parchment_minecraft") as String
val parchmentMappings: String = project.property("parchment_mappings") as String
val kffVersion: String = project.property("kff_version") as String

version = modVersion
group = modGroupId

base {
    archivesName = project.property("mod_id") as String
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    maven { url = uri("https://thedarkcolour.github.io/KotlinForForge/") }
    maven { url = uri("https://maven.minecraftforge.net/") }
}

// Configuration whose contents get shaded into the mod jar (GraalJS script engine).
val jsRuntime: Configuration by configurations.creating
configurations["runtimeClasspath"].extendsFrom(jsRuntime)

minecraft {
    mappings("parchment", "$parchmentMappings-$parchmentMc")

    copyIdeResources = true

    runs {
        val configureEach: RunConfig.() -> Unit = {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods { create(modId) { source(sourceSets["main"]) } }
        }
        create("client") { configureEach() }
        create("server") {
            configureEach()
            args("--nogui")
        }
        create("data") {
            configureEach()
            args(
                "--mod", modId,
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
            )
        }
    }
}

sourceSets.named("main") {
    resources.srcDir("src/generated/resources")
}

dependencies {
    minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    // Kotlin for Forge (language provider + kotlin stdlib at runtime).
    implementation("thedarkcolour:kotlinforforge:$kffVersion")

    // GraalJS script engine — RTM/NGTLib vehicle-pack scripts run through the GraalVM polyglot
    // API + GraalJSScriptEngine (JSR-223). Keeping GraalJS (not Nashorn) preserves ES2022+ so
    // third-party packs written for the 1.21.1 GraalJS port stay compatible. GraalVM 23.0.x is the
    // last release line supporting Java 17. Shaded into the jar (Forge does not ship GraalJS).
    // TODO(Phase3 packaging): GraalJS の transitive (truffle-api/graal-sdk/regex/icu4j) shade と
    //   META-INF/services / native-image 設定のマージを検証する。
    implementation("org.graalvm.js:js:23.0.6")
    implementation("org.graalvm.js:js-scriptengine:23.0.6")
    jsRuntime("org.graalvm.js:js:23.0.6")
    jsRuntime("org.graalvm.js:js-scriptengine:23.0.6")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // During the 1.20.1 port, surface every compile error (javac caps at 100 by default).
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "5000", "-Xmaxwarns", "50"))
}

val replaceProperties = mapOf(
    "minecraft_version" to minecraftVersion,
    "minecraft_version_range" to (project.property("minecraft_version_range") as String),
    "forge_version" to forgeVersion,
    "loader_version_range" to (project.property("loader_version_range") as String),
    "mod_id" to modId,
    "mod_name" to (project.property("mod_name") as String),
    "mod_license" to (project.property("mod_license") as String),
    "mod_version" to modVersion,
    "mod_authors" to (project.property("mod_authors") as String),
    "mod_description" to (project.property("mod_description") as String),
)

tasks.withType<ProcessResources>().configureEach {
    inputs.properties(replaceProperties)

    // Keep the same asset exclusions as the original build (bundled RTM models are shipped
    // from assets/minecraft/**, so the duplicate assets/rtm/** copies are dropped).
    exclude("assets/rtm/models/**")
    exclude("assets/rtm/advancements/lay_rail.json")
    exclude("assets/rtm/advancements/drive_train.json")
    exclude("assets/rtm/textures/items/large_rail*.png")
    exclude("assets/rtm/textures/train/**")
    exclude("assets/realtrainmodunofficial/bundled_packs/**")
    exclude("assets/realtrainmodunofficial/vehicle_packs/**")
    exclude("assets/realtrainmodunofficial/models/car/**")
    exclude("assets/realtrainmodunofficial/textures/car/**")
    exclude("assets/realtrainmodunofficial/models/test-model.mqo")

    filesMatching("META-INF/mods.toml") {
        expand(replaceProperties)
    }
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // LGPL-3.0 §4(b): ship a copy of the GPL and this licence.
    from("LICENSE") { rename { "LICENSE_LGPL-3.0.txt" } }
    from("COPYING") { rename { "LICENSE_GPL-3.0.txt" } }

    // Shade the Nashorn engine.
    from(jsRuntime.map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        exclude("module-info.class", "**/module-info.class")
        // ASM is provided by Forge on the classpath; shading it causes a double export.
        exclude("org/objectweb/**")
    }

    finalizedBy("reobfJar")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven { url = uri("file://${project.projectDir}/repo") }
    }
}
