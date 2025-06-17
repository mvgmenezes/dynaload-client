
// Aplica o plugin Java padrão do Gradle (compilação, testes, etc.)
//plugins {
//    id("java")
//    id("maven-publish") // <-- Public on Maven repo
//}
//
//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(21))
//    }
//}
//
//// Define o namespace e a versão do projeto
//group = "io.dynaload"
//version = "1.0-SNAPSHOT"
//
//// Define os repositórios para buscar dependências
//repositories {
//    mavenLocal()  // Puxa do ~/.m2
//    mavenCentral() // Repositório remoto padrão
//    flatDir {
//        dirs("libs") // Permite buscar JARs locais dentro da pasta "libs"
//    }
//}
//
//// Define o nome e o caminho do JAR dinâmico que será gerado em tempo de execução
//val dynaloadJarName = "dynaload-models-1.0-SNAPSHOT.jar"
//val dynaloadJarFile = file("libs/$dynaloadJarName")
//
//dependencies {
//    // Adiciona dinamicamente o JAR ao classpath (usando provider para evitar erro em Gradle 8+)
//    implementation(provider { files(dynaloadJarFile) })
//    implementation("io.github.classgraph:classgraph:4.8.165")
//    // Dependências de teste (JUnit 5)
//    testImplementation(platform("org.junit:junit-bom:5.10.0"))
//    testImplementation("org.junit.jupiter:junit-jupiter")
//}
//
//// Configura os testes para usar o JUnit Platform
//tasks.test {
//    useJUnitPlatform()
//}
//
//// Task que empacota todos os arquivos .class da pasta build/dynaload em um JAR
//val packageDynaloadJar by tasks.registering(Jar::class) {
//    archiveBaseName.set("dynaload-models") // Nome base do JAR gerado
//    archiveVersion.set("1.0-SNAPSHOT")     // Versão do JAR gerado
//    destinationDirectory.set(file("libs")) // Salva o JAR dentro de libs/
//    from(fileTree("build/dynaload"))       // Origem: onde os .class gerados pelo client são salvos
//}
//
//// Garante que o JAR seja gerado **antes da compilação Java**
//tasks.named("compileJava") {
//    dependsOn(packageDynaloadJar)
//}
//
//// Garante que o JAR dinâmico seja incluso em qualquer build completo (opcional, mas útil)
//tasks.build {
//    dependsOn(packageDynaloadJar)
//}
//
//publishing {
//    publications {
//        register("mavenJava", MavenPublication::class) {
//            from(components["java"])
//            groupId = "io.dynaload"
//            artifactId = "dynaload-client"
//            version = "1.0-SNAPSHOT"
//        }
//    }
//    repositories {
//        mavenLocal()
//    }
//}


plugins {
    id("java")
    id("maven-publish")
}

group = "io.dynaload"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.classgraph:classgraph:4.8.165")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            groupId = "io.dynaload"
            artifactId = "dynaload-client"
            version = "1.0-SNAPSHOT"
        }
    }
    repositories {
        mavenLocal()
    }
}