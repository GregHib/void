package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.definition.Parameters
import world.gregs.voidps.engine.data.config.ParameterDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

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

    fun load(yaml: Yaml = get(), path: String = getProperty("parameterDefinitionsPath")): ParameterDefinitions {
        timedLoad("parameter definition") {
            val size = decode(yaml, path) { id, key, _ ->
                ParameterDefinition(id = id, stringId = key)
            }
            parameters = definitions.associate { it.id to it.stringId }
            size
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
                val int = value as Int
                extras[name] = categoryDefinitions.get(int).stringId
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