package ch.myniva.gradle.caching.s3.internal

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

abstract class BaseGradleTest {
    protected val gradleRunner = GradleRunner.create().withPluginClasspath()

    @TempDir
    protected lateinit var projectDir: Path

    fun Path.write(text: String) = this.toFile().writeText(text)
    fun Path.read(): String = this.toFile().readText()

    protected fun String.normalizeEol() = replace(Regex("[\r\n]+"), "\n")

    protected fun createSettings(extra: String = "") {
        val cp = gradleRunner.pluginClasspath.joinToString { "'${it.absolutePath}'" }

        projectDir.resolve("settings.gradle").write(
            """
                rootProject.name = 'sample'

                buildscript {
                  dependencies {
                    classpath(files($cp))
                  }
                }

                $extra

                apply plugin: 'ch.myniva.s3-build-cache'
            """
        )
    }

    protected fun prepare(gradleVersion: String, vararg arguments: String) =
        gradleRunner
            .withGradleVersion(gradleVersion)
            .withProjectDir(projectDir.toFile())
            .withArguments(*arguments)
            .forwardOutput()
}
