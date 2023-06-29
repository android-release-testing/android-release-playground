package monzo

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.VariantOutputConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class VersioningPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configureVersioning()
    }
}

private fun Project.configureVersioning() {
    val versionNames = optionalStringProperty("monzo.versionNames")
            ?.let { value -> VersionNames.values().first { it.name.equals(value, ignoreCase = true) } }
            ?: VersionNames.Local

    val computeAppVersionTaskProvider = tasks.register(
            "computeAppVersion",
            ComputeAppVersionTask::class.java
    ) { task ->
        task.versionFile.set(rootProject.file("versioning/version"))
        task.rcFile.set(rootProject.file("versioning/rc"))
        task.nightlyFile.set(rootProject.file("versioning/nightly"))

        task.versionNames.set(versionNames)
        if (versionNames != VersionNames.Local) {
            task.gitDirectory.set(rootProject.file(".git"))
            task.gitHeadFile.set(rootProject.file(".git/HEAD"))
            task.gitHeadsDirectory.set(rootProject.file(".git/refs/heads"))
        }

        task.versionCodeFile.set(File(buildDir, "computeAppVersion/versionCode"))
        task.versionNameFile.set(File(buildDir, "computeAppVersion/versionName"))
    }

    androidAppComponents {
        // See: https://github.com/android/gradle-recipes/blob/b1be0c1fad7859c69fa8b0c382c49c0e44aa4ddf/BuildSrc/setVersionsFromTask/buildSrc/src/main/kotlin/CustomPlugin.kt
        onVariants { variant ->
            val mainOutput = variant.outputs.single { it.outputType == VariantOutputConfiguration.OutputType.SINGLE }
            mainOutput.versionCode.set(
                    computeAppVersionTaskProvider.flatMap { it.versionCodeFile }
                            .map { it.asFile.readText().toInt() }
            )
            mainOutput.versionName.set(
                    computeAppVersionTaskProvider.flatMap { it.versionNameFile }
                            .map { it.asFile.readText() }
            )
        }
    }
}

private fun Project.optionalStringProperty(key: String): String? {
    return if (hasProperty(key)) property(key).toString() else null
}

private inline fun Project.androidAppComponents(block: ApplicationAndroidComponentsExtension.() -> Unit = {}) {
    extensions.getByType(ApplicationAndroidComponentsExtension::class.java).apply(block)
}
