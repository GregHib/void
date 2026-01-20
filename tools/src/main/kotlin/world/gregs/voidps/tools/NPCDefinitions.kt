package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoderFull
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.voidps.engine.data.types.NpcTypes

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val files = configFiles()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])

        val start = System.currentTimeMillis()
//        for (i in 0 until 1) {
            NpcTypes.load(cache, files)
//        }
        println("Startup took ${System.currentTimeMillis() - start}ms")
        println(NpcTypes.get(1))

        val categories = CategoryDefinitions().load(files.find(Settings["definitions.categories"]))
        val ammo = AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"]))
        val parameters = ParameterDefinitions(categories, ammo).load(files.find(Settings["definitions.parameters"]))
        val definitions = NPCDecoder(true, parameters).load(cache)
        val full = NPCDecoderFull().load(cache)
        NPCDefinitions.init(definitions).load(files.getValue(Settings["definitions.npcs"]))
        val renderAnimations = RenderAnimationDecoder().load(cache)
        for (i in NPCDefinitions.definitions.indices) {
            val def = NPCDefinitions.getOrNull(i) ?: continue
            val other = NpcTypes.getOrNull(i)
            if (other == null) {
                continue
            }
//            if (def.name.contains("wizard", ignoreCase = true)) {
//                println(def)
//                val att = def["att", 0]
//                val str = def["str", 0]
//                val defence = def["def", 0]
//                val hp = def["hitpoints", 0]
//                val stabDef = def["stab_defence", 0]
//                val slashDef = def["slash_defence", 0]
//                val crushDef = def["crush_defence", 0]
//                val strengthBonus = def["strength", 0.0]
//                val attackBonus = def["attack_bonus", 0]
//
//                val averageLevel = floor((att + str + defence + min(hp, 20_000)) / 4.0).toInt()// 650
//                val averageDefBonus = floor((stabDef + slashDef + crushDef) / 3.0).toInt() // 80
//                val xpBonus = 1 + 0.025 * floor((39 * averageLevel * (averageDefBonus + strengthBonus + attackBonus))/ 200000.0).toInt()
////                println("Bonus: ${xpBonus}")
////                println("Actual: ${2.0 - xpBonus}")
//            }
        }
    }
}
