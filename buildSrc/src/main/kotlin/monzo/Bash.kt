package monzo

import org.gradle.api.GradleException
import java.io.File

fun bash(command: String): String {
    // We use a temporary file for the output stream so we don't block forever when the command produces more output
    // than fits in a pipe buffer. See: https://github.com/monzo/android-app/pull/9727
    val stdout = File.createTempFile("kotlin-scripts", "stdout").apply { deleteOnExit() }
    val process = ProcessBuilder("/bin/bash", "-c", command).apply {
        redirectOutput(stdout)
    }.start()

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw GradleException("'$command' exited with code $exitCode")
    }

    return stdout.readText().trim()
}
