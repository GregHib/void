package rs.dusk.tools.map.process

import rs.dusk.cache.Cache
import rs.dusk.cache.Configs.SCRIPTS
import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.cache.definition.decoder.ClientScriptDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.tile.TileDecoder
import rs.dusk.tools.Pipeline

class ObjectProcessor(
    private val tileDecoder: TileDecoder,
    private val mapDecoder: GameObjectMapDecoder,
    private val objectDecoder: ObjectDecoder,
    private val xteas: Xteas,
    private val cache: Cache,
    private val mapInfoDecoder: WorldMapInfoDecoder,
    private val scriptDecoder: ClientScriptDecoder
) : Pipeline.Modifier<Region> {
    override fun process(region: Region) {
        val mapData = cache.getFile(5, "m${region.x}_${region.y}") ?: return
        val tiles = tileDecoder.read(mapData)
        val xtea = xteas[region.id]
        val locationData = cache.getFile(5, "l${region.x}_${region.y}", xtea)

        if (locationData == null) {
            println("Missing xteas for region ${region.id} [${xtea?.toList()}].")
            return
        }

        val objects = mapDecoder.read(region.x, region.y, locationData, tiles)
        objects?.forEach { obj ->
            val def = objectDecoder.get(obj.id)
            val mapDefId = def.mapDefinitionId
            if(mapDefId != -1) {
                var scriptId = getScriptId(mapDefId)
                var scriptDef = scriptDecoder.getOrNull(scriptId)
                if (scriptDef == null) {
                    val mapDef = mapInfoDecoder.get(mapDefId)
                    scriptId = getScriptId(mapDef.clientScript)
                    scriptDef = scriptDecoder.getOrNull(scriptId)
                    println(mapDef)
                }
                println("$obj ${mapDefId} ${scriptDef}")
            }
        }
    }

    fun getScriptId(id: Int): Int {
        val context = 17
        var scriptId = cache.getArchiveId(SCRIPTS, context or (id shl 10))
        if(scriptId != -1) {
            return scriptId
        }
        scriptId = cache.getArchiveId(SCRIPTS, (65536 + id shl 10) or context)
        if(scriptId != -1) {
            return scriptId
        }
        scriptId = cache.getArchiveId(SCRIPTS, context or 0x3fffc00)
        return scriptId
    }
}