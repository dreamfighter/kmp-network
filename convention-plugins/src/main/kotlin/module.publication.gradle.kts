import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import java.util.Properties

plugins {
    `maven-publish`
    signing
}

val localProperties = File(rootDir, "local.properties").inputStream().use {
    Properties().apply { load(it) }
}

publishing {
    repositories {
        maven {
            credentials {
                username = localProperties["nexus.username"].toString()
                password = localProperties["nexus.password"].toString()
            }
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("https://nexus.bandung.dev/repository/maven-bd/")
            //url = Url("https://nexus.bandung.dev/repository/maven-bd/")
        }
    }
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("Kotlin Multiplatform Auth")
            description.set("Dummy library to test deployment to Maven Central")
            url.set("https://github.com/dreamfighter/multiplatform-auth")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("JetBrains")
                    name.set("JetBrains Team")
                    organization.set("JetBrains")
                    organizationUrl.set("https://www.jetbrains.com")
                }
            }
            scm {
                url.set("https://github.com/dreamfighter/multiplatform-api")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}
