plugins {
    id("application")
}

group = "me.exeos"
version = "2.0.0"

repositories {
    mavenCentral()
}

application {
    mainClass = "me.exeos.jvmtpx.Main"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}