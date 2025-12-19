package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import content.achievement.Tasks
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.config.encoder.StructEncoder
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * Some structs accidentally override a previous value
 * This fixes them by checking if they would override, and if so increment their parameter id
 * It assumes that overrides only happen on parameters which are part of a list and incrementing their id won't overflow the list
 */
object FixStructs {

    private const val TASK_SKILL_1 = 1294
    private const val TASK_LEVEL_1 = 1295
    private const val TASK_SKILL_2 = 1296
    private const val TASK_LEVEL_2 = 1297
    private const val TASK_SKILL_3 = 1298
    private const val TASK_LEVEL_3 = 1299

    private const val CLIMBING_THE_WALLS = 1330
    private const val DO_THEY_COME_IN_OTHER_COLOURS = 1337
    private const val MASS_PRODUCTION = 1342
    private const val THE_STONEMASONS = 1346
    private const val AXELL_GREASE = 1450
    private const val IMPETUOUS_IMPULSES = 1649
    private const val GREAT_ORB_PROJECT = 1658

    fun fix(library: CacheLibrary) {
        println("Fixing all structs...")
        val indexId = Index.CONFIGS
        val index = library.index(indexId)

       /*
           Update incorrectly encoded or missing achievement requirements
        */
        val achievementFixes = mapOf(
            CLIMBING_THE_WALLS to mapOf(
                TASK_SKILL_1 to Skill.Agility,
                TASK_LEVEL_1 to 11,
                TASK_SKILL_2 to Skill.Ranged,
                TASK_LEVEL_2 to 19,
                TASK_SKILL_3 to Skill.Strength,
                TASK_LEVEL_3 to 37,
            ),
            DO_THEY_COME_IN_OTHER_COLOURS to mapOf(
                TASK_SKILL_1 to Skill.Mining,
                TASK_LEVEL_1 to 10,
                TASK_SKILL_2 to Skill.Smithing,
                TASK_LEVEL_2 to 13,
            ),
            MASS_PRODUCTION to mapOf(
                TASK_SKILL_1 to Skill.Runecrafting,
                TASK_LEVEL_1 to 56,
            ),
            THE_STONEMASONS to mapOf(
                TASK_SKILL_1 to Skill.Mining,
                TASK_LEVEL_1 to 60,
            ),
            AXELL_GREASE to mapOf(
                TASK_SKILL_1 to Skill.Attack,
                TASK_LEVEL_1 to 75,
                TASK_SKILL_2 to Skill.Strength,
                TASK_LEVEL_2 to 75,
                TASK_SKILL_3 to Skill.Slayer,
                TASK_LEVEL_3 to 65,
            ),
            IMPETUOUS_IMPULSES to mapOf(
                TASK_SKILL_1 to Skill.Hunter,
                TASK_LEVEL_1 to 17,
            ),
            GREAT_ORB_PROJECT to mapOf(
                TASK_SKILL_1 to Skill.Runecrafting,
                TASK_LEVEL_1 to 50,
            ),
        )
        val cache = CacheDelegate(library)
        val decoder = StructDecoder()
        val encoder = StructEncoder((0 until 2000).map { it.toString() to it }.toMap())

        val fixed = mutableListOf<StructDefinition>()
        for ((id, fixes) in achievementFixes) {
            val definition = StructDefinition(id)
            val data = library.data(indexId, decoder.getArchive(id), decoder.getFile(id)) ?: continue
            val buffer = ArrayReader(data)
            decoder.readLoop(definition, buffer)
            val extras = definition.extras!! as MutableMap
            for ((key, value) in fixes) {
                if (value is Skill) {
                    extras[key.toString()] = Tasks.skills.indexOf(value) + 1
                } else {
                    extras[key.toString()] = value
                }
            }
            fixed.add(definition)
        }
        for (definition in fixed) {
            val writer = BufferWriter(500)
            with(encoder) {
                writer.encode(definition)
            }
            val out = writer.toArray()
            val actual = StructDefinition()
            decoder.readLoop(actual, ArrayReader(out))
            library.put(indexId, decoder.getArchive(definition.id), decoder.getFile(definition.id), out)
        }
        index.flag()
        cache.update()
        println("Fixed ${fixed.size} structs definitions.")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val path = "./data/cache/"
        val lib = CacheLibrary(path)

        fix(lib)
    }
}
