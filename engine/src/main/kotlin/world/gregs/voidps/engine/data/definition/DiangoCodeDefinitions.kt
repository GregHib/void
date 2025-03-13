package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.DiangoCodeDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.timedLoad

class DiangoCodeDefinitions {

    private lateinit var definitions: Map<String, DiangoCodeDefinition>

    fun get(code: String) = getOrNull(code) ?: DiangoCodeDefinition.EMPTY

    fun getOrNull(code: String) = definitions[code]

    @Suppress("UNCHECKED_CAST")
    fun load(path: String = Settings["definitions.diangoCodes"], itemDefinitions: ItemDefinitions? = null): DiangoCodeDefinitions {
        timedLoad("diango code definition") {
            val definitions = Object2ObjectOpenHashMap<String, DiangoCodeDefinition>(1, Hash.VERY_FAST_LOAD_FACTOR)
            val reader = object : ConfigReader(50) {
                override fun set(section: String, key: String, value: Any) {
                    if (section.startsWith("diango")) {
                        val stringId = section.removePrefix("diango")
                        when (key) {
                            "variable" -> definitions[stringId] = definitions[stringId]?.copy(variable = value as String) ?: DiangoCodeDefinition(variable = value as String)
                            "add" -> {
                                val list = value as List<String>
                                val items = ObjectArrayList<Item>(list.size)
                                for (id in list) {
                                    require(itemDefinitions == null || itemDefinitions.contains(id)) { "Invalid diango item id: $id" }
                                    items.add(Item(id, 1))
                                }
                                definitions[stringId] = definitions[stringId]?.copy(add = items) ?: DiangoCodeDefinition(add = items)
                            }
                        }
                    }
                }
            }
            Config.decodeFromFile(path, reader)
            this.definitions = definitions
            definitions.size
        }
        return this
    }

    companion object {
        private val logger = InlineLogger()
    }

}