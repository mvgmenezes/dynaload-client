//plugins {
//    java
//}
//
//group = "io.dynaload"
//version = "1.0-SNAPSHOT"
//
//repositories {
//    mavenCentral()
//    flatDir {
//        dirs("libs") // onde o JAR dinâmico será copiado
//    }
//}
//
//val dynaloadJarName = "dynaload-models-1.0-SNAPSHOT.jar"
//val dynaloadJarFile = file("libs/$dynaloadJarName")
//
//val dynaload by configurations.creating
//
//
//dependencies {
//    // Adiciona o JAR dinâmico ao classpath em tempo de compilação
//    //implementation(files("libs/dynaload-models.jar"))
//    dynaload(files(dynaloadJarFile))
//    implementation(dynaload)
//
//    testImplementation(platform("org.junit:junit-bom:5.10.0"))
//    testImplementation("org.junit.jupiter:junit-jupiter")
//}
//
//tasks.test {
//    useJUnitPlatform()
//}
//
//// Task: empacota os .class da pasta build/dynaload em um JAR
//val packageDynaloadJar by tasks.registering(Jar::class) {
//    archiveBaseName.set("dynaload-models")
//    destinationDirectory.set(layout.buildDirectory.dir("generated-jars"))
//    from(fileTree("build/dynaload")) // Onde os .class estão sendo salvos
//}
//
//// Task: copia o JAR para a pasta libs/
//val copyDynaloadJarToLibs by tasks.registering(Copy::class) {
//    dependsOn(packageDynaloadJar)
//    from(packageDynaloadJar.map { it.archiveFile })
//    into("libs")
//}
//
//// Task: garante que o JAR seja copiado antes de compilar
//tasks.named("compileJava") {
//    val dynaloadJarName = "dynaload-models-1.0-SNAPSHOT.jar"
//    val dynaloadJarPath = file("libs/$dynaloadJarName")
//
//    doFirst {
//        if (!dynaloadJarPath.exists()) {
//            println("⚠️ Dynaload JAR não encontrado. A task 'copyDynaloadJarToLibs' será forçada.")
//            dependsOn(copyDynaloadJarToLibs) // Adiciona a dependência dinamicamente
//        }
//    }
//
//    // Garantia fixa também, caso o JAR esteja sendo regenerado sempre
//    dependsOn(copyDynaloadJarToLibs)
//}
//
//// (Opcional) Task: inclui a cópia no build geral
//tasks.build {
//    dependsOn(copyDynaloadJarToLibs)
//}



// Aplica o plugin Java padrão do Gradle (compilação, testes, etc.)
plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Define o namespace e a versão do projeto
group = "io.dynaload"
version = "1.0-SNAPSHOT"

// Define os repositórios para buscar dependências
repositories {
    mavenCentral() // Repositório remoto padrão
    flatDir {
        dirs("libs") // Permite buscar JARs locais dentro da pasta "libs"
    }
}

// Define o nome e o caminho do JAR dinâmico que será gerado em tempo de execução
val dynaloadJarName = "dynaload-models-1.0-SNAPSHOT.jar"
val dynaloadJarFile = file("libs/$dynaloadJarName")

dependencies {
    // Adiciona dinamicamente o JAR ao classpath (usando provider para evitar erro em Gradle 8+)
    implementation(provider { files(dynaloadJarFile) })

    // Dependências de teste (JUnit 5)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

// Configura os testes para usar o JUnit Platform
tasks.test {
    useJUnitPlatform()
}

// Task que empacota todos os arquivos .class da pasta build/dynaload em um JAR
val packageDynaloadJar by tasks.registering(Jar::class) {
    archiveBaseName.set("dynaload-models") // Nome base do JAR gerado
    archiveVersion.set("1.0-SNAPSHOT")     // Versão do JAR gerado
    destinationDirectory.set(file("libs")) // Salva o JAR dentro de libs/
    from(fileTree("build/dynaload"))       // Origem: onde os .class gerados pelo client são salvos
}

// Garante que o JAR seja gerado **antes da compilação Java**
tasks.named("compileJava") {
    dependsOn(packageDynaloadJar)
}

// Garante que o JAR dinâmico seja incluso em qualquer build completo (opcional, mas útil)
tasks.build {
    dependsOn(packageDynaloadJar)
}