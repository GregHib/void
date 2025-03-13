package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.AmmoDefinition
import world.gregs.voidps.engine.timedLoad

/**
 * List of [AmmoDefinition.items] each weapons ammo_group can use ignoring
 * skill requirements and whether inside dungeoneering.
 */
class AmmoDefinitions : DefinitionsDecoder<AmmoDefinition> {

    override lateinit var definitions: Array<AmmoDefinition>
    override lateinit var ids: Map<String, Int>

    @Suppress("UNCHECKED_CAST")
    fun load(path: String = Settings["definitions.ammoGroups"]): AmmoDefinitions {
        timedLoad("ammo definition") {
            val size = 1300
            val ids = Object2IntOpenHashMap<String>(size, 0.25f)
            definitions = Array(size) { AmmoDefinition.EMPTY }
            var count = 0
            val reader = object : ConfigReader(100) {
                override fun set(section: String, key: String, value: Any) {
                    if (key == "id") {
                        val id = (value as Long).toInt()
                        ids[key] = id
                        definitions[id] = AmmoDefinition(id, stringId = key)
                        count++
                    } else if (key == "items") {
                        val id = ids.getInt(key)
                        require(id != -1) { "Unable to find id for '$key' make sure id is the first in the section."}
                        (definitions[id].items as MutableSet<String>).addAll(value as List<String>)
                    }
                }
            }
            Config.decodeFromFile(path, reader)
            this.ids = ids
            count
        }
        return this
    }

    override fun empty() = AmmoDefinition.EMPTY

}