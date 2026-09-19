plugins {
	// 版本由根 settings.gradle 的 pluginManagement.plugins 统一提供
	id("fabric-loom")
	`maven-publish`
}

// version / group 由根 build.gradle 的 subprojects {} 统一设置（monorepo：三个模块共用 SSCU 的 group + version）。
// 只保留 archivesName，让 jar 名仍是 Calio-Legacy-<version>.jar，与 SSCU 的产物区分开。

base {
	archivesName.set(project.property("archives_base_name") as String)
}

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

	// PSA: Some older mods, compiled on Loom 0.2.1, might have outdated Maven POMs.
	// You may need to force-disable transitiveness on them.
}

loom {
	accessWidenerPath = file("src/main/resources/calio.accesswidener")
}

tasks.withType<JavaCompile>().configureEach {
	// ensure that the encoding is set to UTF-8, no matter what the system default is
	// this fixes some edge cases with special characters not displaying correctly
	// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
	// If Javadoc is generated, this must be specified in that task too.
	options.encoding = "UTF-8"

	// Minecraft 1.17 (21w19a) upwards uses Java 16.
	options.release = 21
}

val targetJavaVersion = "21"

java {
	val javaVersion = JavaVersion.toVersion(targetJavaVersion)
	if (JavaVersion.current() < javaVersion) {
		toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
	}

	sourceCompatibility = javaVersion
	targetCompatibility = javaVersion

	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()
}

tasks {
	processResources {
		inputs.property("version", project.version)

		filesMatching("fabric.mod.json") {
			expand("version" to project.version)
		}
	}

	jar {
		from("LICENSE") {
			rename { "${it}_${project.base.archivesName.get()}" }
		}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.property("archives_base_name") as String
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {}
}