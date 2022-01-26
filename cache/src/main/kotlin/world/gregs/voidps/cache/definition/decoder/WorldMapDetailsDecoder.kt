package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.WORLD_MAP
import world.gregs.voidps.cache.definition.data.WorldMapDefinition
import world.gregs.voidps.cache.definition.data.WorldMapSection
import java.util.*

class WorldMapDetailsDecoder(cache: Cache) : DefinitionDecoder<WorldMapDefinition>(cache, WORLD_MAP) {

    val archive = cache.getArchiveId(index, "details")

    override fun getArchive(id: Int) = archive

    override val last: Int
        get() = cache.lastFileId(index, archive)

    override fun create() = WorldMapDefinition()

    override fun readLoop(definition: WorldMapDefinition, buffer: Reader) {
        definition.read(-1, buffer)
    }

    override fun WorldMapDefinition.read(opcode: Int, buffer: Reader) {
        map = buffer.readString()
        name = buffer.readString()
        position = buffer.readInt()
        anInt9542 = buffer.readInt()// Size?
        static = buffer.readUnsignedBoolean()
        anInt9547 = buffer.readUnsignedByte()// Always zero except "Braindeath Island" which is -1
        buffer.readUnsignedByte()

        if (anInt9547 == 255) {
            anInt9547 = 0
        }
        sections = LinkedList()
        val length = buffer.readUnsignedByte()
        repeat(length) {
            sections!!.addLast(
                WorldMapSection(
                    buffer.readUnsignedByte(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort(),
                    buffer.readShort()
                )
            )
        }
    }

    override fun WorldMapDefinition.changeValues() {
        minX = 12800
        minY = 12800
        maxX = 0
        maxY = 0

        sections?.forEach { definition ->
            if (minX > definition.startX) {
                minX = definition.startX
            }
            if (minY > definition.startY) {
                minY = definition.startY
            }
            if (maxX < definition.endX) {
                maxX = definition.endX
            }
            if (maxY < definition.endY) {
                maxY = definition.endY
            }
        }
    }
}