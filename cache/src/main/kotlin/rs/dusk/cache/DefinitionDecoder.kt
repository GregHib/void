package rs.dusk.cache

import rs.dusk.core.io.read.BufferReader
import rs.dusk.core.io.read.Reader
import rs.dusk.utility.inject
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 06, 2020
 */
abstract class DefinitionDecoder<T : Definition>(internal val index: Int) {

    protected val cache: Cache by inject()

    protected val dataCache = ConcurrentHashMap<Int, T>()

    open val size: Int
        get() = cache.lastIndexId(index) * 256 + (cache.archiveCount(index, cache.lastIndexId(index)))

    fun get(id: Int): T? {
        var value = dataCache[id]
        if (value == null) {
            value = readData(id)
            if (value != null) {
                dataCache[id] = value
            }
        }
        return value
    }

    fun getSafe(id: Int) = get(id) ?: create()

    protected abstract fun create(): T

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

    protected open fun readLoop(definition: T, buffer: Reader) {
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

    protected open fun T.changeValues() {
    }

    open fun clear() {
        dataCache.clear()
    }

    companion object {

        fun byteToChar(b: Byte): Char {
            var i = 0xff and b.toInt()
            require(i != 0) { "Non cp1252 character 0x" + i.toString(16) + " provided" }
            if (i in 128..159) {
                var char = UNICODE_TABLE[i - 128].toInt()
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