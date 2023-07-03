package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.WORLD_MAP
import world.gregs.voidps.cache.definition.data.WorldMapDefinition
import world.gregs.voidps.cache.definition.data.WorldMapSection
import java.util.*

class WorldMapDetailsDecoder : DefinitionDecoder<WorldMapDefinition>(WORLD_MAP) {

    val archive = 0//cache.getArchiveId(index, "details")

    override fun getArchive(id: Int) = archive

    override fun size(cache: Cache): Int {
        return cache.lastFileId(index, archive)
    }

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
        for (i in 0 until length) {
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

    override fun changeValues(definitions: Array<WorldMapDefinition>, definition: WorldMapDefinition) {
        definition.minX = 12800
        definition.minY = 12800
        definition.maxX = 0
        definition.maxY = 0

        definition.sections?.forEach { section ->
            if (definition.minX > section.startX) {
                definition.minX = section.startX
            }
            if (definition.minY > section.startY) {
                definition.minY = section.startY
            }
            if (definition.maxX < section.endX) {
                definition.maxX = section.endX
            }
            if (definition.maxY < section.endY) {
                definition.maxY = section.endY
            }
        }
    }
}