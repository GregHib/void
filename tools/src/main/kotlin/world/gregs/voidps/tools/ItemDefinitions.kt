package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.yaml.Yaml

object ItemDefinitions {

    fun test(def: ItemDefinition) = (def.stringId.endsWith("halberd"))

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = ItemDefinitions(ItemDecoder().loadCache(cache)).load(Yaml(), "./data/definitions/items.yml")
        val styles = WeaponStyleDefinitions().load(Yaml(), "./data/definitions/weapon-styles.yml")

        val map = mutableMapOf<Int, MutableList<ItemDefinition>>()
        for (i in decoder.definitions.indices) {
            val def = decoder.getOrNull(i) ?: continue
            val weaponStyle = def.params?.getOrDefault(686, -1) as? Int ?: -1
            if (test(def) && weaponStyle != 15) {
                println("Incorrect style $weaponStyle $def")
            }
            if (weaponStyle != -1) {
                map.getOrPut(weaponStyle) { mutableListOf() }.add(def)
//                println("$i ${def.name} style $weaponStyle")
            }
        }

        for (i in styles.definitions.indices) {
            val style = styles.get(i)
            println("${style.stringId} $i")
            println(style)
            println(map[i]?.map { it.name })
        }
    }
}