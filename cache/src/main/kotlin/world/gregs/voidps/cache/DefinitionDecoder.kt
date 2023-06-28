package world.gregs.voidps.cache

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import java.io.File

abstract class DefinitionDecoder<T : Definition>(internal val cache: Cache, internal val index: Int) {

    open val last: Int
        get() = cache.lastArchiveId(index) * 256 + (cache.archiveCount(index, cache.lastArchiveId(index)))

    val indices: IntRange
        get() = 0..last


    fun loadAll(definitions: Array<T>, getId: (archive: Int, file: Int) -> Int) {
        val temp = File("temp.dat")
        val writer = BufferReader(temp.readBytes())
        while (writer.position() < writer.length) {
            val id = writer.readInt()
            val definition = definitions[id]
            readLoop(definition, writer)
//            definition.changeValues()
        }
    }

    fun getOrNull(id: Int): T? {
        return readData(id)
    }

    open fun get(id: Int) = getOrNull(id) ?: create()

    protected abstract fun create(): T

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