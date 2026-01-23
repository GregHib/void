package world.gregs.config.file

import java.io.File
import java.nio.file.Path

interface FileCodec {
    fun added(base: File, added: List<Path>)
    fun removed(base: File, removed: List<Path>)
    fun modified(base: File, modified: List<Path>)
}
