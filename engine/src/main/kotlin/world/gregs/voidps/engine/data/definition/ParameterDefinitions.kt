package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Parameters
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.Params.AMMO_GROUP
import world.gregs.voidps.cache.definition.Params.CATEGORY
import world.gregs.voidps.cache.definition.Params.EQUIP_LEVEL_1
import world.gregs.voidps.cache.definition.Params.EQUIP_LEVEL_2
import world.gregs.voidps.cache.definition.Params.EQUIP_LEVEL_3
import world.gregs.voidps.cache.definition.Params.EQUIP_LEVEL_4
import world.gregs.voidps.cache.definition.Params.EQUIP_LEVEL_5
import world.gregs.voidps.cache.definition.Params.EQUIP_LEVEL_6
import world.gregs.voidps.cache.definition.Params.EQUIP_SKILL_1
import world.gregs.voidps.cache.definition.Params.EQUIP_SKILL_2
import world.gregs.voidps.cache.definition.Params.EQUIP_SKILL_3
import world.gregs.voidps.cache.definition.Params.EQUIP_SKILL_4
import world.gregs.voidps.cache.definition.Params.EQUIP_SKILL_5
import world.gregs.voidps.cache.definition.Params.EQUIP_SKILL_6
import world.gregs.voidps.cache.definition.Params.MAGIC_STRENGTH
import world.gregs.voidps.cache.definition.Params.RANGED_STRENGTH
import world.gregs.voidps.cache.definition.Params.SKILLCAPE_SKILL
import world.gregs.voidps.cache.definition.Params.USE_LEVEL_1
import world.gregs.voidps.cache.definition.Params.USE_LEVEL_2
import world.gregs.voidps.cache.definition.Params.USE_LEVEL_3
import world.gregs.voidps.cache.definition.Params.USE_LEVEL_4
import world.gregs.voidps.cache.definition.Params.USE_LEVEL_5
import world.gregs.voidps.cache.definition.Params.USE_LEVEL_6
import world.gregs.voidps.cache.definition.Params.USE_SKILL_1
import world.gregs.voidps.cache.definition.Params.USE_SKILL_2
import world.gregs.voidps.cache.definition.Params.USE_SKILL_3
import world.gregs.voidps.cache.definition.Params.USE_SKILL_4
import world.gregs.voidps.cache.definition.Params.USE_SKILL_5
import world.gregs.voidps.cache.definition.Params.USE_SKILL_6
import world.gregs.voidps.cache.definition.Params.WORN_OPTION_1
import world.gregs.voidps.cache.definition.Params.WORN_OPTION_2
import world.gregs.voidps.cache.definition.Params.WORN_OPTION_3
import world.gregs.voidps.cache.definition.Params.WORN_OPTION_4
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.ParameterDefinition
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timedLoad

/**
 * Parameters mainly for [ItemDefinitions], [NPCDefinitions], [ObjectDefinitions] and [StructDefinitions]
 */
class ParameterDefinitions(
    private val categoryDefinitions: CategoryDefinitions,
    private val ammoDefinitions: AmmoDefinitions,
) : DefinitionsDecoder<ParameterDefinition>,
    Parameters {

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
    override fun set(extras: MutableMap<Int, Any>, id: Int, value: Any) {
        when (id) {
            EQUIP_SKILL_1, EQUIP_SKILL_2, EQUIP_SKILL_3, EQUIP_SKILL_4, EQUIP_SKILL_5, EQUIP_SKILL_6 -> {
                val map = extras.getOrPut(Params.EQUIP_REQ) { Object2IntOpenHashMap<Skill>(6) } as MutableMap<Skill, Int>
                val skill = Skill.all[value as Int]
                map[skill] = -1
            }
            EQUIP_LEVEL_1, EQUIP_LEVEL_2, EQUIP_LEVEL_3, EQUIP_LEVEL_4, EQUIP_LEVEL_5, EQUIP_LEVEL_6 -> {
                val map = extras.getOrPut(Params.EQUIP_REQ) { Object2IntOpenHashMap<Skill>(6) } as MutableMap<Skill, Int>
                val skill = map.keys.firstOrNull { map[it] == -1 } ?: return logger.warn { "Missing param $id $value" }
                map[skill] = value as Int
            }
            USE_SKILL_1, USE_SKILL_2, USE_SKILL_3, USE_SKILL_4, USE_SKILL_5, USE_SKILL_6 -> {
                val map = extras.getOrPut(Params.SKILL_REQ) { Object2IntOpenHashMap<Skill>(6) } as MutableMap<Skill, Int>
                val skill = Skill.all[value as Int]
                map[skill] = -1
            }
            USE_LEVEL_1, USE_LEVEL_2, USE_LEVEL_3, USE_LEVEL_4, USE_LEVEL_5, USE_LEVEL_6 -> {
                val map = extras.getOrPut(Params.SKILL_REQ) { Object2IntOpenHashMap<Skill>(6) } as MutableMap<Skill, Int>
                val skill = map.keys.firstOrNull { map[it] == -1 } ?: return logger.warn { "Missing param $id $value" }
                map[skill] = value as Int
            }
            RANGED_STRENGTH, MAGIC_STRENGTH -> {
                extras[id] = (value as Int) / 10.0
            }
            SKILLCAPE_SKILL -> extras[id] = Skill.all[value as Int]
            CATEGORY -> {
                val set = ObjectOpenHashSet<String>()
                val int = value as Int
                set.add(categoryDefinitions.get(int).stringId)
                extras[Params.CATEGORIES] = set
            }
            AMMO_GROUP -> {
                val int = value as Int
                extras[id] = ammoDefinitions.get(int).stringId
            }
            WORN_OPTION_1, WORN_OPTION_2, WORN_OPTION_3, WORN_OPTION_4 -> {
                val list = extras.getOrPut(Params.WORN_OPTIONS) { Int2ObjectOpenHashMap<String>(4) } as MutableMap<Int, String>
                list[id - WORN_OPTION_1] = value as String
            }
            else -> super.set(extras, id, value)
        }
    }

    override fun empty() = ParameterDefinition.EMPTY
}
