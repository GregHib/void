package rs.dusk.tools.map.process

import rs.dusk.cache.Cache
import rs.dusk.cache.Configs.SCRIPTS
import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.cache.definition.decoder.ClientScriptDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectLoc
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.tile.TileData
import rs.dusk.engine.map.region.tile.TileDecoder

class ObjectIconHoverProcessor(
    tileDecoder: TileDecoder,
    mapDecoder: GameObjectMapDecoder,
    private val objectDecoder: ObjectDecoder,
    xteas: Xteas,
    private val cache: Cache,
    private val mapInfoDecoder: WorldMapInfoDecoder,
    private val scriptDecoder: ClientScriptDecoder
) : ObjectProcessor(tileDecoder, mapDecoder, xteas, cache) {
    val data = mutableMapOf<GameObjectLoc, List<String>>()
    override fun process(region: Region, tiles: Array<Array<Array<TileData?>>>, objects: List<GameObjectLoc>) {
        objects.forEach { obj ->
            val def = objectDecoder.get(obj.id)
            val mapDefId = def.mapDefinitionId
            if (mapDefId != -1) {
                var scriptId = getScriptId(mapDefId)
                var scriptDef = scriptDecoder.getOrNull(scriptId)
                if (scriptDef == null) {
                    val mapDef = mapInfoDecoder.get(mapDefId)
                    scriptId = getScriptId(mapDef.clientScript)
                    scriptDef = scriptDecoder.getOrNull(scriptId)
                }
                if (scriptDef != null && scriptDef.instructions.contains(3)) {
                    data[obj] = scriptDef.instructions.mapIndexed { index, i -> if (i == 3) scriptDef.stringOperands?.get(index) else null }.filterNotNull()
                }
            }
        }
    }

    fun getScriptId(id: Int): Int {
        val context = 17
        var scriptId = cache.getArchiveId(SCRIPTS, context or (id shl 10))
        if (scriptId != -1) {
            return scriptId
        }
        scriptId = cache.getArchiveId(SCRIPTS, (65536 + id shl 10) or context)
        if (scriptId != -1) {
            return scriptId
        }
        scriptId = cache.getArchiveId(SCRIPTS, context or 0x3fffc00)
        return scriptId
    }
}