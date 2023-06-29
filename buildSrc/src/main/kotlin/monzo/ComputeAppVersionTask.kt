package monzo

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

enum class VersionNames {
    /**
     * Version names are generated from `/versioning/version`, `/versioning/rc` and `/versioning/nightly`.
     * Examples: `4.12.0`, `4.12.0-rc1`, `4.12.0-nightlyMon-1`
     *
     * This is the default for builds triggered by our release process (including nightly builds).
     *
     * We intentionally do *not* use this versioning scheme for any other CI builds, to avoid filling up Firebase App
     * Distribution with a bunch of builds with the same version name as release train builds.
     */
    Release,

    /**
     * Version names are generated from the current branch and commit.
     * Examples: `my-branch-6ad2cd07ff`, `my-branch-7312f794be`, `main-c64275946e`
     *
     * This is the default for CI builds. The names of Firebase App Distribution releases are derived from the APK's
     * version name, so using the branch/commit in the version name makes it easier to find a specific release.
     */
    Git,

    /**
     * Version names are generated from `/versioning/version`, with a `local-` prefix.
     * Examples: `local-4.12.0`, `local-4.13.0`
     *
     * This is the default for local builds. We avoid using Git to generate version names for local builds, because:
     *
     * 1) It reduces the effectiveness of the remote build cache. If you check out a new branch called `my-branch` from
     * `main`, ideally every task would hit the remote build cache. However, if we base the version name on the Git
     * branch name, the version name generated locally by `ComputeAppVersionTask` will be different to the version name
     * generated on CI. This means `BuildConfig` will also be different, which means that the inputs to expensive tasks
     * like `app:compileDebugKotlin` will be different, which means those tasks will not hit the remote build
     * cache.
     *
     * 2) It results in unnecessary `app` recompilations whenever you commit locally, for the same reason as above -
     * the version name changes, which means `BuildConfig` changes, which results in a recompile.
     */
    Local
}

@CacheableTask
abstract class ComputeAppVersionTask : DefaultTask() {
    @get:InputFile
    // We only care about the contents of the file, not its name/location.
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val versionFile: RegularFileProperty

    @get:InputFile
    // We only care about the contents of the file, not its name/location.
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val rcFile: RegularFileProperty

    @get:InputFile
    // We only care about the contents of the file, not its name/location.
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val nightlyFile: RegularFileProperty

    @get:Input
    abstract val versionNames: Property<VersionNames>

    // We don't want to track changes to the whole .git directory, only specific files/subdirectories.
    @get:Internal
    abstract val gitDirectory: DirectoryProperty

    @get:Optional
    @get:InputFile
    // We only care about the contents of .git/HEAD, not its name/location.
    @get:PathSensitive(PathSensitivity.NONE)
    @Suppress("unused")
    abstract val gitHeadFile: RegularFileProperty

    @get:Optional
    @get:InputDirectory
    // We care about names, locations and contents in .git/refs/heads. For example, refs/heads/x and refs/heads/y
    // are distinct branches, even if their contents are the same (because they point to the same commit).
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @Suppress("unused")
    abstract val gitHeadsDirectory: DirectoryProperty

    @get:OutputFile
    abstract val versionCodeFile: RegularFileProperty

    @get:OutputFile
    abstract val versionNameFile: RegularFileProperty

    private val workingDir: File
        get() = gitDirectory.asFile.get()

    @TaskAction
    fun execute() {
        val (major, minor, patch) = versionFile.asFile.get().readText().trim().split(".").map { it.toInt() }
        check(major in 0..99) { "major version is very large - are you sure you wanted to set it to this?" }
        check(minor in 0..99) { "minor version is out of range" }
        check(patch in 0..99) { "patch version is out of range" }

        val rc = rcFile.asFile.get().readText().trim().toInt()
        check(rc in 0..10) { "rc is out of range" }

        val nightly = nightlyFile.asFile.get().readText().trim().toInt()
        check(nightly in 0..40)

        val build = if (rc > 0) rc + 40 else nightly
        val versionCode = major * 1_000_000 + minor * 10_000 + patch * 100 + build
        versionCodeFile.asFile.get().writeText(versionCode.toString())

        @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
        val versionName = when (versionNames.get()) {
            VersionNames.Release -> {
                when (rc) {
                    0 -> {
                        val date = bash("git -C $workingDir log -1 --date=format:%Y-%m-%d --format=%ad")
                        "${major}.${minor}.${patch}-nightly-$date"
                    }

                    10 -> "${major}.${minor}.${patch}"
                    else -> "${major}.${minor}.${patch}-rc${rc}"
                }
            }

            VersionNames.Git -> {
                val branch = bash("git -C $workingDir rev-parse --abbrev-ref HEAD")
                val sha = bash("git -C $workingDir rev-parse --short HEAD")
                "$branch-$sha"
            }

            VersionNames.Local -> {
                "local-${major}.${minor}.${patch}"
            }
        }
        versionNameFile.asFile.get().writeText(versionName)
    }
}
