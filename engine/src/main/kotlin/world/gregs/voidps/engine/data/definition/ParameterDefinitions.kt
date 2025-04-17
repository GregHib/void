package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Parameters
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.ParameterDefinition
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timedLoad

/**
 * Parameters mainly for [ItemDefinitions], [NPCDefinitions], [ObjectDefinitions] and [StructDefinitions]
 */
class ParameterDefinitions(
    private val categoryDefinitions: CategoryDefinitions,
    private val ammoDefinitions: AmmoDefinitions
) : DefinitionsDecoder<ParameterDefinition>, Parameters {

    override lateinit var definitions: Array<ParameterDefinition>
    override lateinit var ids: Map<String, Int>
    override lateinit var parameters: Map<Int, String>
    private val logger = InlineLogger()

    fun load(path: String = Settings["definitions.parameters"]): ParameterDefinitions {
        timedLoad("parameter definition") {
            val ids = Object2IntOpenHashMap<String>(500, Hash.VERY_FAST_LOAD_FACTOR)
            val parameters = Int2ObjectOpenHashMap<String>(500, Hash.VERY_FAST_LOAD_FACTOR)
            val definitions = Array(2500) { ParameterDefinition.EMPTY }
            Config.fileReader(path) {
                while (nextPair()) {
                    val stringId = key()
                    val id = int()
                    require(!ids.containsKey(stringId)) { "Duplicate parameter id found '$stringId' at $path." }
                    ids[stringId] = id
                    definitions[id].stringId = stringId
                    parameters[id] = stringId
                }
            }
            this.ids = ids
            this.definitions = definitions
            this.parameters = parameters
            ids.size
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(extras: MutableMap<String, Any>, name: String, value: Any) {
        when {
            name.startsWith("equip_skill_") || name.startsWith("equip_level_") -> {
                val map = extras.getOrPut("equip_req") { Object2IntOpenHashMap<Skill>(6) } as MutableMap<Skill, Int>
                if (name.startsWith("equip_skill_")) {
                    val skill = Skill.all[value as Int]
                    map[skill] = -1
                } else {
                    val skill = map.keys.firstOrNull { map[it] == -1 } ?: return logger.warn { "Missing $name $value" }
                    map[skill] = value as Int
                }
            }
            name.startsWith("use_skill_") || name.startsWith("use_level_") -> {
                val map = extras.getOrPut("skill_req") { Object2IntOpenHashMap<Skill>(6) } as MutableMap<Skill, Int>
                if (name.startsWith("use_skill_")) {
                    val skill = Skill.all[value as Int]
                    map[skill] = -1
                } else {
                    val skill = map.keys.firstOrNull { map[it] == -1 } ?: return logger.warn { "Missing $name $value" }
                    map[skill] = value as Int
                }
            }
            name.endsWith("strength") -> {
                extras[name] = (value as Int) / 10.0
            }
            name == "skillcape_skill" -> {
                extras[name] = Skill.all[value as Int]
            }
            name == "category" -> {
                val set = ObjectOpenHashSet<String>()
                val int = value as Int
                set.add(categoryDefinitions.get(int).stringId)
                extras["categories"] = set
            }
            name == "ammo_group" -> {
                val int = value as Int
                extras[name] = ammoDefinitions.get(int).stringId
            }
            name.startsWith("worn_option_") -> {
                val list = extras.getOrPut("worn_options") { Int2ObjectOpenHashMap<String>(4) } as MutableMap<Int, String>
                list[name.removePrefix("worn_option_").toInt() - 1] = value as String
            }
            else -> super.set(extras, name, value)
        }
    }

    override fun empty() = ParameterDefinition.EMPTY

}