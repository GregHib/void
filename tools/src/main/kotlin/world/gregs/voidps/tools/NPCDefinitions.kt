package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.voidps.engine.data.find
import world.gregs.yaml.Yaml

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val files = configFiles()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val categories = CategoryDefinitions().load(files.find(Settings["definitions.categories"]))
        val ammo = AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"]))
        val parameters = ParameterDefinitions(categories, ammo).load(files.find(Settings["definitions.parameters"]))
        val definitions = NPCDecoder(true, parameters).load(cache)
        val decoder = NPCDefinitions(definitions).load(files.getValue(Settings["definitions.npcs"]))
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.name.contains("Talent scout", ignoreCase = true)) {
                println("$i ${def.name} ${def.extras} ${def.transforms?.contentToString()} ${def.options.contentDeepToString()}")
            }
        }
    }
}