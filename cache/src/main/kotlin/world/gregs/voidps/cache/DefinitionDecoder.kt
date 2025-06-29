package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import java.nio.BufferUnderflowException

abstract class DefinitionDecoder<T : Definition>(val index: Int) {

    abstract fun create(size: Int): Array<T>

    open fun load(definitions: Array<T>, reader: Reader) {
        val id = readId(reader)
        read(definitions, id, reader)
    }

    open fun readId(reader: Reader) = reader.readInt()

    /**
     * Load from cache
     */
    open fun load(cache: Cache): Array<T> {
        val start = System.currentTimeMillis()
        val size = size(cache) + 1
        val definitions = create(size)
        for (id in 0 until size) {
            try {
                load(definitions, cache, id)
            } catch (e: BufferUnderflowException) {
                logger.error(e) { "Error reading definition $id" }
                throw e
            }
        }
        logger.info { "$size ${this::class.simpleName} definitions loaded in ${System.currentTimeMillis() - start}ms" }
        return definitions
    }

    open fun size(cache: Cache): Int = cache.lastArchiveId(index) * 256 + (cache.fileCount(index, cache.lastArchiveId(index)))

    open fun load(definitions: Array<T>, cache: Cache, id: Int) {
        val archive = getArchive(id)
        val file = getFile(id)
        val data = cache.data(index, archive, file) ?: return
        read(definitions, id, BufferReader(data))
    }

    open fun getFile(id: Int) = id

    open fun getArchive(id: Int) = id

    protected fun read(definitions: Array<T>, id: Int, reader: Reader) {
        val definition = definitions[id]
        readLoop(definition, reader)
        changeValues(definitions, definition)
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

    protected abstract fun T.read(opcode: Int, buffer: Reader)

    open fun changeValues(definitions: Array<T>, definition: T) {
    }

    companion object {
        internal val logger = InlineLogger()

        fun byteToChar(b: Byte): Char {
            var i = 0xff and b.toInt()
            require(i != 0) { "Non cp1252 character 0x" + i.toString(16) + " provided" }
            if (i in 128..159) {
                var char = unicodeTable[i - 128].code
                if (char == 0) {
                    char = 63
                }
                i = char
            }
            return i.toChar()
        }

        fun charToByte(c: Char): Byte {
            val code = c.code

            if (code == 63) {
                throw IllegalArgumentException("Cannot map '?' back to a specific byte")
            }

            when (code) {
                in 0..127 -> return code.toByte()
                in 160..255 -> return code.toByte()
            }

            val indexInTable = unicodeTable.indexOfFirst { it.code == code }
            if (indexInTable != -1) {
                return (128 + indexInTable).toByte()
            }

            throw IllegalArgumentException("Char '$c' (0x${code.toString(16)}) not in CP1252 mapping")
        }

        private var unicodeTable = charArrayOf(
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
            '\u0178',
        )
    }
}
