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

    fun load(path: String = Settings["definitions.ammoGroups"]): AmmoDefinitions {
        timedLoad("ammo definition") {
            val size = 1300
            val ids = Object2IntOpenHashMap<String>(size, 0.25f)
            definitions = Array(size) { AmmoDefinition.EMPTY }
            Config.fileReader(path) {
                while (nextSection()) {
                    val section = section()
                    var id = -1
                    val set = ObjectOpenHashSet<String>(2)
                    while (nextPair()) {
                        val key = key()
                        when (key) {
                            "id" -> id = int()
                            "items" -> while (nextElement()) {
                                set.add(string())
                            }
                        }
                    }
                    ids[section] = id
                    definitions[id] = AmmoDefinition(id, set, section)
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = AmmoDefinition.EMPTY

}