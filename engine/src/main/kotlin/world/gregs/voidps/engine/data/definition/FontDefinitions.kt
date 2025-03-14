package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad

class FontDefinitions(
    override var definitions: Array<FontDefinition>
) : DefinitionsDecoder<FontDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = FontDefinition.EMPTY

    fun load(path: String = Settings["definitions.fonts"]): FontDefinitions {
        timedLoad("font extra") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            val reader = object : ConfigReader(50) {
                override fun set(section: String, key: String, value: Any) {
                    val id = (value as Long).toInt()
                    ids[key] = id
                    definitions[id].stringId = key
                }
            }
            Config.decodeFromFile(path, reader)
            this.ids = ids
            ids.size
        }
        return this
    }

}