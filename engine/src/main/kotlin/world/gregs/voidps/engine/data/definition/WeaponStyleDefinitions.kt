package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.WeaponStyleDefinition
import world.gregs.voidps.engine.timedLoad

class WeaponStyleDefinitions : DefinitionsDecoder<WeaponStyleDefinition> {

    override lateinit var definitions: Array<WeaponStyleDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(path: String = Settings["definitions.weapons.styles"]): WeaponStyleDefinitions {
        timedLoad("weapon style definition") {
            val definitions = Array(28) { WeaponStyleDefinition.EMPTY }
            val ids = Object2IntOpenHashMap<String>(28, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    var id = -1
                    val attackTypes = ObjectArrayList<String>()
                    val attackStyles = ObjectArrayList<String>()
                    val combatStyles = ObjectArrayList<String>()
                    while (nextPair()) {
                        when (key()) {
                            "id" -> id = int()
                            "attack_types" -> while (nextElement()) {
                                attackTypes.add(string())
                            }
                            "attack_styles" -> while (nextElement()) {
                                attackStyles.add(string())
                            }
                            "combat_styles" -> while (nextElement()) {
                                combatStyles.add(string())
                            }
                        }
                    }
                    ids[stringId] = id
                    definitions[id] = WeaponStyleDefinition(stringId = stringId, attackTypes = attackTypes.toTypedArray(), attackStyles = attackStyles.toTypedArray(), combatStyles = combatStyles.toTypedArray())
                }
            }
            this.definitions = definitions
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = WeaponStyleDefinition.EMPTY
}