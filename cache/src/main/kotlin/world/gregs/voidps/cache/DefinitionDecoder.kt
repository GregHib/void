package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import java.io.File

abstract class DefinitionDecoder<T : Definition>(internal val cache: Cache, val index: Int) {

    open val last: Int
        get() = cache.lastArchiveId(index) * 256 + (cache.archiveCount(index, cache.lastArchiveId(index)))

    val indices: IntRange
        get() = 0..last

    fun getOrNull(id: Int): T? {
        return readData(id)
    }

    fun loadLive(directory: File): Array<T> {
        val start = System.currentTimeMillis()
        val reader = BufferReader(directory.resolve("index${index}.dat").readBytes())
        val size = reader.readInt()
        val array = create(size)
        while (reader.position() < reader.length) {
            val id = readId(reader)
            val definition = array[id]
            readLoop(definition, reader)
            changeDefValues(definition)
        }
        logger.info { "${this::class.simpleName} loaded $size in ${System.currentTimeMillis() - start}ms" }
        return array
    }

    fun loadCache(cache: Cache): Array<T> {
        val start = System.currentTimeMillis()
        val size = size(cache)
        val array = create(size)
        for (archiveId in cache.getArchives(index)) {
            val files = cache.getArchiveData(index, archiveId) ?: continue
            for ((fileId, file) in files) {
                if (file == null) {
                    continue
                }
                val id = id(archiveId, fileId)
                val definition = array[id]
                readLoop(definition, BufferReader(file))
                changeDefValues(definition)
            }
        }
        logger.info { "${this::class.simpleName} loaded $size in ${System.currentTimeMillis() - start}ms" }
        return array
    }

    open fun get(id: Int) = getOrNull(id) ?: create()

    protected abstract fun create(): T

    open fun create(size: Int): Array<T> {
        return emptyArray<Any>() as Array<T>
    }

    open fun size(cache: Cache): Int {
        return cache.lastArchiveId(index) * 256 + (cache.archiveCount(index, cache.lastArchiveId(index)))
    }

    open fun readId(reader: Reader): Int {
        return 0
    }

    open fun id(archive: Int, file: Int): Int {
        return 0
    }

    open fun changeDefValues(definition: T) {
    }

    protected open fun getData(archive: Int, file: Int): ByteArray? {
        return cache.getFile(index, archive, file)
    }

    protected open fun readData(id: Int): T? {
        val archive = getArchive(id)
        val file = getFile(id)
        val data = getData(archive, file)
        if (data != null) {
            val definition = create()
            definition.id = id
            readLoop(definition, BufferReader(data))
            definition.changeValues()
            return definition
        }
        return null
    }

    open fun readLoop(definition: T, buffer: Reader) {
        while (true) {
            val opcode = buffer.readUnsignedByte()
            if (opcode == 0) {
                break
            }
            definition.read(opcode, buffer)
        }
    }

    open fun getFile(id: Int) = id

    open fun getArchive(id: Int) = id

    protected abstract fun T.read(opcode: Int, buffer: Reader)

    open fun T.changeValues() {
    }

    open fun clear() {
    }

    fun forEach(function: (T) -> Unit) {
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            function.invoke(def)
        }
    }

    companion object {
        private val logger = InlineLogger()

        fun byteToChar(b: Byte): Char {
            var i = 0xff and b.toInt()
            require(i != 0) { "Non cp1252 character 0x" + i.toString(16) + " provided" }
            if (i in 128..159) {
                var char = UNICODE_TABLE[i - 128].code
                if (char == 0) {
                    char = 63
                }
                i = char
            }
            return i.toChar()
        }

        private var UNICODE_TABLE = charArrayOf(
            '\u20ac',
            '\u0000',
            '\u201a',
            '\u0192',
            '\u201e',
            '\u2026',
            '\u2020',
            '\u2021',
            '\u02c6',
            '\u2030',
            '\u0160',
            '\u2039',
            '\u0152',
            '\u0000',
            '\u017d',
            '\u0000',
            '\u0000',
            '\u2018',
            '\u2019',
            '\u201c',
            '\u201d',
            '\u2022',
            '\u2013',
            '\u2014',
            '\u02dc',
            '\u2122',
            '\u0161',
            '\u203a',
            '\u0153',
            '\u0000',
            '\u017e',
            '\u0178'
        )
    }
}