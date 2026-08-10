import org.gradle.api.tasks.testing.logging.TestExceptionFormat

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
plugins {
    java
    application
    jacoco
    `project-report`
    alias(libs.plugins.shadow)
    alias(libs.plugins.coveralls)
    alias(libs.plugins.rat)
}

group = "org.apache.freemarker.onlinetester"
version = "0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

val rat by configurations.creating

dependencies {
    implementation(libs.dropwizard)
    implementation(libs.dropwizard.views.freemarker)
    implementation(libs.dropwizard.configurable.assets)
    implementation(libs.dropwizard.bundles.redirect)
    implementation(libs.freemarker)
    implementation(libs.commons.lang3)
    implementation(libs.findbugs)

    testImplementation(libs.dropwizard.testing)
    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.hamcrest)
    testImplementation(libs.jersey.grrizle)

    rat(libs.rat)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Werror")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.apache.freemarker.onlinetester.dropwizard.FreeMarkerOnlineTester",
            "Implementation-Version" to version.toString()
        )
    }
}

tasks.shadowJar {
    dependsOn(tasks.jar)
    archiveVersion.set("")
    mergeServiceFiles()
}

tasks.test {
    testLogging {
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}

application {
    mainClass.set("org.apache.freemarker.onlinetester.dropwizard.FreeMarkerOnlineTester")
}

tasks.named<JavaExec>("run") {
    args("server", "src/main/resources/freemarker-online.yml")
}

tasks.rat {
    inputDir.set(layout.projectDirectory)
    reportDir.set(layout.buildDirectory.dir("reports/rat"))
    verbose.set(true)

    // We can't use Rat's own excludeFile.set(...), as the Gradle doesn't see what inputs we don't have:
    excludes.addAll(
        file("rat-excludes").readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") })
}

tasks.check {
    dependsOn(tasks.jacocoTestReport)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
