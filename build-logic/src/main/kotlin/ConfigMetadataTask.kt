import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import world.gregs.config.ConfigLoader
import world.gregs.config.file.FileChanges
import java.nio.file.Path

/**
 * Gradle task which incrementally tracks config files inside a given directory.
 */
abstract class ConfigMetadataTask : DefaultTask() {

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val directories: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    init {
        description = ""
        group = "metadata"
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val start = System.currentTimeMillis()
        val base = output.get().asFile

        val modified = mutableSetOf<Path>()
        val added = mutableSetOf<Path>()
        val removed = mutableSetOf<Path>()
        var count = 0

        for (change in inputChanges.getFileChanges(directories)) {
            when (change.changeType) {
                ChangeType.ADDED -> added.add(change.file.toPath())
                ChangeType.MODIFIED -> modified.add(change.file.toPath())
                ChangeType.REMOVED -> removed.add(change.file.toPath())
            }
            count++
        }

        ConfigLoader.load(base, FileChanges(
            modified = modified,
            added = added,
            removed = removed,
            incremental = inputChanges.isIncremental
        ))
        println("Total task took ${System.currentTimeMillis() - start}ms for $count files")
    }

}
