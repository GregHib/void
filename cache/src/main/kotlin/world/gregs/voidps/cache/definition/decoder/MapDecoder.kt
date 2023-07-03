package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.MAPS
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.MapTile

class MapDecoder(private val xteas: Map<Int, IntArray>) : DefinitionDecoder<MapDefinition>(MAPS) {

    override fun MapDefinition.read(opcode: Int, buffer: Reader) {
        TODO("Not yet implemented")
    }

    override fun create(size: Int) = Array(size) { MapDefinition(it) }

    override fun size(cache: Cache): Int {
        return cache.lastArchiveId(index)
    }

    override fun getArchive(id: Int): Int {
        return id
    }

    override fun getFile(id: Int): Int {
        return 0
    }

    override fun load(cache: Cache, archiveId: Int, fileId: Int, definitions: Array<MapDefinition>, reader: Reader) {
        val definition = definitions[archiveId]
        definition.id = archiveId
        readLoop(definition, reader)
        definition.changeValues(cache)
    }

    override fun load(id: Int, cache: Cache, array: Array<MapDefinition>) {
        val data = cache.getFile(index, "m${id shr 8}_${id and 0xff}", null) ?: return
        array[id].id = id
        load(cache, getArchive(id), getFile(id), array, BufferReader(data))
    }

    override fun readLoop(definition: MapDefinition, buffer: Reader) {
        for (plane in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    var height = 0
                    var attrOpcode = 0
                    var overlayPath = 0
                    var overlayRotation = 0
                    var overlayId = 0
                    var settings = 0
                    var underlayId = 0
                    loop@ while (true) {
                        val config = buffer.readUnsignedByte()
                        if (config == 0) {
                            break@loop
                        } else if (config == 1) {
                            height = buffer.readUnsignedByte()
                            break@loop
                        } else if (config <= 49) {
                            attrOpcode = config
                            overlayId = buffer.readUnsignedByte()
                            overlayPath = (config - 2) / 4
                            overlayRotation = 3 and (config - 2)
                        } else if (config <= 81) {
                            settings = config - 49
                        } else {
                            underlayId = (config - 81) and 0xff
                        }
                    }
                    if (height != 0 || attrOpcode != 0 || overlayPath != 0 || overlayRotation != 0 || overlayId != 0 || settings != 0 || underlayId != 0) {
                        definition.setTile(localX, localY, plane, MapTile(
                            height,
                            attrOpcode,
                            overlayId,
                            overlayPath,
                            overlayRotation,
                            settings,
                            underlayId
                        ))
                    }
                }
            }
        }
    }

    fun MapDefinition.changeValues(cache: Cache) {
        val objectData = cache.getFile(index, "l${id shr 8}_${id and 0xff}", xteas[id]) ?: return
        val reader = BufferReader(objectData)
        var objectId = -1
        while (true) {
            val skip = reader.readLargeSmart()
            if (skip == 0) {
                break
            }
            objectId += skip
            var tile = 0
            while (true) {
                val loc = reader.readSmart()
                if (loc == 0) {
                    break
                }
                tile += loc - 1

                // Data
                val localX = tile shr 6 and 0x3f
                val localY = tile and 0x3f
                var plane = tile shr 12
                val obj = reader.readUnsignedByte()

                // Decrease bridges
                if (getTile(localX, localY, 1).isTile(BRIDGE_TILE)) {
                    plane--
                }

                // Validate plane
                if (plane !in 0 until 4) {
                    continue
                }

                val type = obj shr 2
                val rotation = obj and 0x3

                // Valid object
                objects.add(MapObject(objectId, localX, localY, plane, type, rotation))
            }
        }
    }

    fun getFile(name: String, xteas: IntArray?): ByteArray? {
        return null//cache.getFile(index, name, xteas)
    }

    companion object {
        private const val BRIDGE_TILE = 0x2
    }
}