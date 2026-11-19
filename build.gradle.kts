import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    java
    checkstyle
    pmd
    jacoco
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.shalousun.smart-doc") version "4.0.0"
}

group = "io.github.vodobryshkin"
version = "0.0.1-SNAPSHOT"
description = "mini-exchange"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.paulschwarz:springboot3-dotenv:5.1.0")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.bouncycastle:bcprov-jdk18on:1.87")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "14.4.0"
    configFile = file("config/checkstyle/checkstyle.xml")

    isShowViolations = true
    isIgnoreFailures = false
}

pmd {
    toolVersion = "7.16.0"
    ruleSets = listOf("category/java/errorprone.xml")

    isConsoleOutput = true
    isIgnoreFailures = false
}

jacoco {
    toolVersion = "0.8.16"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)

        html.outputLocation.set(
            layout.buildDirectory.dir("reports/jacoco/test/html")
        )
    }
}

smartdoc {
    configFile = file("src/main/resources/smart-doc.json")
}

tasks.named("build") {
    dependsOn("smartDocOpenApi")
}

tasks.named("smartDocOpenApi") {
    doLast {
        @Suppress("UNCHECKED_CAST")
        fun obj(value: Any?) = value as MutableMap<String, Any?>

        val generated = file("build/docs/openapi/openapi.json")
        val spec = obj(JsonSlurper().parse(generated))
        val paths = obj(spec["paths"])

        fun operation(path: String, method: String) = obj(obj(paths[path])[method])

        for (path in listOf("/orders", "/auth/register")) {
            val responses = obj(operation(path, "post")["responses"])
            val created = obj(responses.remove("200") ?: responses.getValue("201"))
            created["description"] = "Created"
            responses["201"] = created
        }

        val cancel = operation("/orders/{id}", "patch")
        val responses = obj(cancel["responses"])
        responses.remove("200")
        responses["204"] = mapOf("description" to "No Content")
        cancel.remove("requestBody")

        paths["/auth/logout"] = mapOf(
            "post" to mapOf(
                "summary" to "Завершение сессии пользователя",
                "tags" to listOf("AuthController"),
                "responses" to operation("/auth/login", "post")["responses"]
            )
        )

        for (path in listOf("/orders", "/trades")) {
            val parameters = operation(path, "get")["parameters"] as List<*>
            for (parameter in parameters) {
                val param = obj(parameter)
                if (param["name"] == "page" || param["name"] == "size") {
                    val defaultValue = if (param["name"] == "page") 0 else 50
                    param["required"] = false
                    param["example"] = defaultValue
                    obj(param["schema"])["default"] = defaultValue
                }
            }
        }

        val parameters = operation("/instruments/{secCode}/orderbook", "get")["parameters"] as List<*>
        val depth = parameters.map { obj(it) }.first { it["name"] == "depth" }
        depth["example"] = 20
        obj(depth["schema"])["default"] = 20

        val json = JsonOutput.prettyPrint(JsonOutput.toJson(spec))
        generated.writeText(json)
        val published = file("doc/openapi.json")
        published.parentFile.mkdirs()
        published.writeText(json)
    }
}
