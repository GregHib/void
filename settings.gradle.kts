import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

rootProject.name = "void"

include("game")
include("cache")
include("engine")
include("tools")
include("buffer")
include("network")
include("types")
include("yaml")
include("server")
includeProjects(project(":game"))

fun includeProjects(pluginProject: ProjectDescriptor) {
    val projectPath = pluginProject.projectDir.toPath()
    Files.walk(projectPath).forEach {
        if (!Files.isDirectory(it) || it.name == "src" || it.name == "build") {
            return@forEach
        }
        searchProject(pluginProject.name, projectPath, it)
    }
}

fun searchProject(parentName: String, root: Path, currentPath: Path) {
    val hasBuildFile = Files.exists(currentPath.resolve("build.gradle.kts"))
    if (!hasBuildFile) {
        return
    }
    val relativePath = root.relativize(currentPath)
    val projectName = relativePath.toString().replace(File.separator, ":")
    include("$parentName:$projectName")
}
