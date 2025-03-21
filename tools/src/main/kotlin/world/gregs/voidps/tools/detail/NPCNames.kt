package world.gregs.voidps.tools.detail

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.yaml.Yaml

/**
 * Dumps unique string identifiers for NPCs using formatted npc definition name plus index for duplicates
 */
private class NPCNames(val decoder: Array<NPCDefinition>) : NameDumper() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Settings.load()
            val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
            val decoder = NPCDecoder(member = true).load(cache)
            val yaml= Yaml()
            val names = NPCNames(decoder)
            names.dump(yaml, "./npc-details.yml", "npc", decoder.lastIndex)
        }
    }

    override fun createName(id: Int): String? {
        return decoder.getOrNull(id)?.name
    }

    override fun createData(id: Int): Map<String, Any> {
        return mutableMapOf("id" to id)
    }

}