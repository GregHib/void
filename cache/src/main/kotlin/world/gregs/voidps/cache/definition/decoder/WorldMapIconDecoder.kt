package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.WORLD_MAP
import world.gregs.voidps.cache.definition.data.WorldMapIcon
import world.gregs.voidps.cache.definition.data.WorldMapIconDefinition

class WorldMapIconDecoder : DefinitionDecoder<WorldMapIconDefinition>(WORLD_MAP) {

    private var archive = -1
    var aBoolean1313 = false

    override fun getArchive(id: Int) = archive

    override fun size(cache: Cache): Int {
        return cache.lastFileId(index, archive)
    }

    override fun create() = WorldMapIconDefinition()

    //archive = cache.getArchiveId(index, "${map}_staticelements")


    override fun load(id: Int, cache: Cache, array: Array<WorldMapIconDefinition>) {
        val archive = getArchive(id)
        var length = -1//cache.archiveCount(index, archive)
        var counter = 0
        var index = 0
        if (length > 0) {
            val definition = array[id]
            val icons = mutableListOf<WorldMapIcon>()
            while (length > counter) {
                val data = getData(archive, index++)
                if (data != null) {
                    val buffer = BufferReader(data)
                    val position = buffer.readInt()
                    val iconId = buffer.readShort()
                    val skip = buffer.readUnsignedByte()
                    if (aBoolean1313 && skip == 1) {
                        length--
                    } else {
                        counter++
                        icons.add(WorldMapIcon(iconId, position))
                    }
                }
            }
            definition.icons = icons.toTypedArray()
            definition.changeValues()
        }
    }

    override fun WorldMapIconDefinition.read(opcode: Int, buffer: Reader) {
    }
}