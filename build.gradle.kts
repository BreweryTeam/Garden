import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.papermc.hangarpublishplugin.model.Platforms
import java.net.HttpURLConnection
import java.net.URI

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.modrinth.minotaur") version "2.8.7"
    id("de.eldoria.plugin-yml.bukkit") version "0.7.1"
}

group = "dev.jsinco.brewery.garden"
version = "4.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.jsinco.dev/releases")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.dre.brewery:BreweryX:3.4.10-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.8")
    compileOnly("org.xerial:sqlite-jdbc:3.47.2.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // TODO implement mockbukkit things again // thorin
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:dev-5640d74cf")
    testImplementation("org.xerial:sqlite-jdbc:3.47.2.0")
}


tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    register("publishRelease") {
        dependsOn(modrinth)
        finalizedBy("publishPluginPublicationToHangar")

        doLast {
            val webhook = DiscordWebhook(System.getenv("DISCORD_WEBHOOK") ?: return@doLast, false)
            webhook.message = "@everyone"
            webhook.embedTitle = "BreweryGarden - v${project.version}"
            webhook.embedDescription = readChangeLog()
            webhook.embedThumbnailUrl =
                "https://cdn.modrinth.com/data/3TaOMjJ9/5e44a541ba38ce5d8567207a4b75183658756d57_96.webp"
            webhook.send()
        }
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.unset()
    }

    test {
        useJUnitPlatform()
    }
}

bukkit {
    main = "dev.jsinco.brewery.garden.BreweryGarden"
    foliaSupported = false
    apiVersion = "1.21"
    authors = listOf("Jsinco", "Thorinwasher")
    name = rootProject.name
    commands {
        register("garden")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

hangarPublish {
    publications.register("plugin") {
        version.set(project.version.toString())
        channel.set("Release")
        id.set(project.name)
        apiKey.set(System.getenv("HANGAR_TOKEN") ?: return@register)
        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.jar.flatMap { it.archiveFile })
                platformVersions.set(listOf("1.21.3", "1.21.4"))
            }
        }
        changelog.set(readChangeLog())
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN") ?: return@modrinth)
    projectId.set(project.name.lowercase())
    versionNumber.set(project.version.toString())
    versionType.set("release")
    uploadFile.set(tasks.jar)
    loaders.addAll("paper", "purpur", "folia")
    gameVersions.addAll("1.21.3", "1.21.4")
    changelog.set(readChangeLog())
}

fun readChangeLog(): String {
    val text: String = System.getenv("CHANGELOG") ?: file("CHANGELOG.md").run {
        if (exists()) readText() else "No Changelog found."
    }
    return text.replace("\${version}", project.version.toString())
}

class DiscordWebhook(
    val webhookUrl: String,
    var defaultThumbnail: Boolean = true
) {

    companion object {
        private const val MAX_EMBED_DESCRIPTION_LENGTH = 4096
    }

    var message: String = "content"
    var username: String = "BreweryX Updates"
    var avatarUrl: String = "https://github.com/breweryteam.png"
    var embedTitle: String = "Embed Title"
    var embedDescription: String = "Embed Description"
    var embedColor: String = "F5E083"
    var embedThumbnailUrl: String? = if (defaultThumbnail) avatarUrl else null
    var embedImageUrl: String? = null

    private fun hexStringToInt(hex: String): Int {
        val hexWithoutPrefix = hex.removePrefix("#")
        return hexWithoutPrefix.toInt(16)
    }

    private fun buildToJson(): String {
        val json = JsonObject()
        json.addProperty("username", username)
        json.addProperty("avatar_url", avatarUrl)
        json.addProperty("content", message)

        val embed = JsonObject()
        embed.addProperty("title", embedTitle)
        embed.addProperty("description", embedDescription)
        embed.addProperty("color", hexStringToInt(embedColor))

        embedThumbnailUrl?.let {
            val thumbnail = JsonObject()
            thumbnail.addProperty("url", it)
            embed.add("thumbnail", thumbnail)
        }

        embedImageUrl?.let {
            val image = JsonObject()
            image.addProperty("url", it)
            embed.add("image", image)
        }

        val embeds = JsonArray()
        createEmbeds().forEach(embeds::add)

        json.add("embeds", embeds)
        return json.toString()
    }

    private fun createEmbeds(): List<JsonObject> {
        if (embedDescription.length <= MAX_EMBED_DESCRIPTION_LENGTH) {
            return listOf(JsonObject().apply {
                addProperty("title", embedTitle)
                addProperty("description", embedDescription)
                addProperty("color", embedColor.toInt(16))
                embedThumbnailUrl?.let {
                    val thumbnail = JsonObject()
                    thumbnail.addProperty("url", it)
                    add("thumbnail", thumbnail)
                }
                embedImageUrl?.let {
                    val image = JsonObject()
                    image.addProperty("url", it)
                    add("image", image)
                }
            })
        }
        val embeds = mutableListOf<JsonObject>()
        var description = embedDescription
        while (description.isNotEmpty()) {
            val chunkLength = minOf(MAX_EMBED_DESCRIPTION_LENGTH, description.length)
            val chunk = description.substring(0, chunkLength)
            description = description.substring(chunkLength)
            embeds.add(JsonObject().apply {
                addProperty("title", embedTitle)
                addProperty("description", chunk)
                addProperty("color", embedColor.toInt(16))
                embedThumbnailUrl?.let {
                    val thumbnail = JsonObject()
                    thumbnail.addProperty("url", it)
                    add("thumbnail", thumbnail)
                }
                embedImageUrl?.let {
                    val image = JsonObject()
                    image.addProperty("url", it)
                    add("image", image)
                }
            })
        }
        return embeds
    }

    fun send() {
        val url = URI(webhookUrl).toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.outputStream.use { outputStream ->
            outputStream.write(buildToJson().toByteArray())

            val responseCode = connection.responseCode
            println("POST Response Code :: $responseCode")
            if (responseCode == HttpURLConnection.HTTP_OK) {
                println("Message sent successfully.")
            } else {
                println("Failed to send message.")
            }
        }
    }
}