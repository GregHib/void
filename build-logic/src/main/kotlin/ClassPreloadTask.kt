import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Gradle task which lists the compiled classes in a package which are loaded when scripts are constructed,
 * so they can be loaded in parallel ahead of time.
 *
 * Includes top-level classes and their direct anonymous classes (`Outer$1`) which are the handler lambdas
 * created by script constructors. Deeper nested lambdas (`Outer$1$1`, `Outer$method$1`) are only loaded
 * once a handler runs so are skipped.
 */
abstract class ClassPreloadTask : DefaultTask() {

    @get:InputFiles
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classesDirs: ConfigurableFileCollection

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        description = "Lists compiled script classes to load in parallel on startup"
        group = "metadata"
    }

    @TaskAction
    fun execute() {
        val packagePath = packageName.get().replace('.', '/')
        val names = mutableListOf<String>()
        for (dir in classesDirs.files) {
            val root = dir.resolve(packagePath)
            if (!root.exists()) {
                continue
            }
            root.walkTopDown()
                .filter { it.isFile && it.name.endsWith(".class") }
                .map { it.relativeTo(dir).invariantSeparatorsPath.removeSuffix(".class").replace('/', '.') }
                .filterTo(names) { preload(it) }
        }
        names.sort()
        val file = outputDirectory.get().asFile.resolve("preload.txt")
        file.writeText(names.joinToString("\n"))
    }

    private fun preload(name: String): Boolean {
        val index = name.indexOf('$')
        if (index == -1) {
            return true
        }
        val inner = name.substring(index + 1)
        return inner.isNotEmpty() && inner.all { it.isDigit() }
    }
}
