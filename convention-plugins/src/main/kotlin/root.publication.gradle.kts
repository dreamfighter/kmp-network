import java.util.Properties

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

allprojects {
    group = "com.github.dreamfighter.multiplatform"
    version = "0.0.5"
}

val localProperties = File(rootDir, "local.properties").inputStream().use {
    Properties().apply { load(it) }
}

/* use this for publishing using plugins io.github.gradle-nexus.publish-plugin
nexusPublishing {
    // Configure maven central repository
    // https://github.com/gradle-nexus/publish-plugin#publishing-to-maven-central-via-sonatype-ossrh
    repositories {

        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            username = localProperties["sonatype.username"].toString()
            password = localProperties["sonatype.password"].toString()
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
*/