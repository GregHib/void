package world.gregs.config

import java.io.*
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Methods for reading and writing toml configuration files
 */
object Config {

    fun fileReader(path: String, maxStringLength: Int = 100, block: ConfigReader.() -> Unit) {
        ConfigReader(BufferedInputStream(FileInputStream(path)), maxStringLength, path).use(block)
    }

    fun fileReader(file: File, maxStringLength: Int = 100, block: ConfigReader.() -> Unit) {
        ConfigReader(BufferedInputStream(FileInputStream(file)), maxStringLength, file.path).use(block)
    }

    fun stringReader(string: String, maxStringLength: Int = 100, block: ConfigReader.() -> Unit) {
        ConfigReader(BufferedInputStream(string.byteInputStream()), maxStringLength).use(block)
    }

    fun fileWriter(path: String, block: ConfigWriter.() -> Unit) {
        BufferedWriter(FileWriter(path)).use { output ->
            block.invoke(output)
        }
    }

    fun fileWriter(file: File, block: ConfigWriter.() -> Unit) {
        BufferedWriter(FileWriter(file)).use { output ->
            block.invoke(output)
        }
    }

    fun atomicFileWriter(file: File, block: ConfigWriter.() -> Unit) {
        val temp = File(file.absoluteFile.parentFile, "${file.name}.tmp")
        FileOutputStream(temp).use { stream ->
            val output = BufferedWriter(OutputStreamWriter(stream))
            block.invoke(output)
            output.flush()
            stream.fd.sync()
        }
        try {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    fun stringWriter(block: ConfigWriter.() -> Unit): String {
        val stringWriter = StringWriter()
        BufferedWriter(stringWriter).use { output ->
            block.invoke(output)
        }
        return stringWriter.buffer.toString()
    }
}
