plugins {
    id("java")
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

version = findProperty("project.version")!!
group = findProperty("project.group")!!

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "garden-api"
            from(components["java"])
            pom {
                name = "Garden API"
                description = "API for Garden"
                url = "https://tbp.breweryteam.dev/docs/welcome/"
                licenses {
                    license {
                        name = "The MIT license"
                        url =
                            "https://raw.githubusercontent.com/BreweryTeam/Garden/refs/heads/master/LICENSE"
                    }
                }
                scm {
                    connection = "scm:git:git//github.com/BreweryTeam/Garden.git"
                    developerConnection = "scm:git:ssh://github.com:BreweryTeam/Garden.git"
                    url = "https://github.com/BreweryTeam/Garden"
                }
            }
        }
    }
    repositories {
        maven {
            name = "breweryteam"
            url = uri("https://repo.breweryteam.dev/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }

        }
    }
}