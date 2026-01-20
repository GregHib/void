package world.gregs.voidps.engine.data.types

import org.jetbrains.annotations.TestOnly
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.type.codec.NpcTypeCodec
import world.gregs.voidps.cache.type.data.NpcType
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.param.NpcParams

/**
 * Lookup for [NpcType]
 */
object NpcTypes : ParamTypes<NpcType> {

    override lateinit var types: Array<NpcType>
    override lateinit var ids: MutableMap<String, Int>

    fun load(cache: Cache, files: ConfigFiles) {
        NpcTypeCodec.read(cache, files, "npcs.bin", maxDefCacheSize = 1_200_000)
        NpcParams.read(files, Settings["definitions.npcs"], maxString = 250)
    }

    @TestOnly
    fun set(type: NpcType) {
        types[type.id] = type
        ids[type.stringId] = type.id
    }

    fun clear() {
        types = emptyArray()
        ids.clear()
    }

}