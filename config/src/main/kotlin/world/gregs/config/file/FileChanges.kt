package world.gregs.config.file

import java.nio.file.Path

data class FileChanges(
    var incremental: Boolean,
    val added: Set<Path>,
    val modified: Set<Path>,
    val removed: Set<Path>,
)