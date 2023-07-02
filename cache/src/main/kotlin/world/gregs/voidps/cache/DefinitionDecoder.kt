package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader

abstract class DefinitionDecoder<T : Definition>(val index: Int) {

    open fun fileName() = "index${index}.dat"

    open var last: Int = 0
//        get() = cache.lastArchiveId(index) * 256 + (cache.archiveCount(index, cache.lastArchiveId(index)))

    val indices: IntRange
        get() = 0..last

    fun getOrNull(id: Int): T? {
        return readData(id)
    }

    fun load(loader: DefinitionLoader): Array<T> {
        return loader.load(this)
    }

    fun loadCache(cache: Cache): Array<T> {
        return CacheDefinitionLoader(cache).load(this)
    }

    open fun load(cache: Cache, archiveId: Int, fileId: Int, definitions: Array<T>, reader: Reader) {
        val id = id(archiveId, fileId)
        val definition = definitions[id]
        readLoop(definition, reader)
        changeDefValues(definition)
    }

    open fun load(definitions: Array<T>, reader: Reader) {
        val id = readId(reader)
        val definition = definitions[id]
        readLoop(definition, reader)
        changeDefValues(definition)
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
        return reader.readInt()
    }

    open fun id(archive: Int, file: Int): Int {
        return 0
    }

    open fun changeDefValues(definition: T) {
    }

    protected open fun getData(archive: Int, file: Int): ByteArray? {
        return null//cache.getFile(index, archive, file)
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