package content.achievement

import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.engine.client.variable.BooleanValues
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.character.player.summoningCombatLevel
import world.gregs.voidps.engine.get
import content.quest.questComplete
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.concurrent.TimeUnit

object Tasks {

    fun isCompleted(player: Player, id: String): Boolean {
        val variable = get<VariableDefinitions>().get(id)?.values ?: return false
        return when (variable) {
            is BooleanValues -> player[id, false]
            is MapValues -> player[id, "unstarted"] == "completed"
            else -> false
        }
    }

    class TaskIterator {
        lateinit var definition: StructDefinition
        var index: Int = -1
        var skip: Boolean = false
    }

    fun <R> forEach(areaId: Int, block: TaskIterator.() -> R?): R? {
        val enumDefinitions: EnumDefinitions = get()
        val structDefinitions: StructDefinitions = get()
        var next = enumDefinitions.get("task_area_start_indices").getInt(areaId)
        val structs = enumDefinitions.get("task_structs")
        val iterator = TaskIterator()
        while (next != 4091 && next != 450 && next != 4094) {
            val struct = structs.getInt(next)
            iterator.definition = structDefinitions.getOrNull(struct) ?: break
            iterator.index = next
            iterator.skip = false
            val result = block.invoke(iterator)
            if (result != null) {
                return result
            }
            if (!iterator.skip) {
                next = iterator.definition["task_next_index", 4091]
            }
        }
        return null
    }

    fun hasRequirements(player: Player, definition: StructDefinition): Boolean {
        for (i in 1..10) {
            val req = definition["task_skill_$i", -1]
            val value = definition["task_level_$i", 1]
            when (req) {
                -1 -> break
                63 -> return hasRequirements(player, definition.id)
                62 -> {
                    val quest = get<QuestDefinitions>().get(value)
                    if (player.questComplete(quest.stringId)) {
                        return false
                    }
                }
                else -> {
                    val skill = skills[req - 1]
                    if (!player.hasMax(skill, value)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private val skills = listOf(
        Skill.Attack,
        Skill.Strength,
        Skill.Ranged,
        Skill.Magic,
        Skill.Defence,
        Skill.Constitution,
        Skill.Prayer,
        Skill.Agility,
        Skill.Herblore,
        Skill.Thieving,
        Skill.Crafting,
        Skill.Runecrafting,
        Skill.Mining,
        Skill.Smithing,
        Skill.Fishing,
        Skill.Cooking,
        Skill.Firemaking,
        Skill.Woodcutting,
        Skill.Fletching,
        Skill.Slayer,
        Skill.Farming,
        Skill.Construction,
        Skill.Hunter,
        Skill.Summoning,
        Skill.Dungeoneering,
    )

    // Missing quests: 199, 200, 229
    // Missing skills: 302, 305
    // Extra skills: 3005, 3012
    private fun hasRequirements(player: Player, id: Int): Boolean {
        return when (id) {
            12 -> player["penguin_hide_and_seek_explained", false]
            23 -> player["fairy_rings_unlocked", false]
            49 -> player["unlocked_emote_air_guitar", false]
            59 -> player.questComplete("into_the_abyss")
            147, 167 -> player["fairy_rings_unlocked", false]
            107 -> player.getInt("dragon_slayer", "unstarted") >= 2 && player["dragon_slayer_received_shield", false] || player.questComplete("dragon_slayer")
            219 -> player.combatLevel >= 100
            331 -> player.summoningCombatLevel >= 100
            276, 3011 -> player["quest_points", 0] >= 33
            281 -> player["unlocked_emote_flap", false] && !player["unlocked_emote_slap_head", false] && !player["unlocked_emote_idea", false] && player["unlocked_emote_stomp", false]
            289 -> player.combatLevel >= 40
            300 -> kudosCount(player) >= 153
            3000 -> minutes() >= player["varp_451", 0] && !(player["quest_points", 0] < player["varbit_456", 0] && player["total_xp_earned", 0.0] < player["varp_450", 0.0])
            3001 -> player.summoningCombatLevel >= 40
            3002 -> player["penguins_found_weekly", 0] != 10 && player["penguin_points", 0] != 50 && player["penguin_hide_and_seek_explained", 0] != 0
            3003 -> player["nurture_evil_tree_stage", 0] < 2
            3007 -> !player["circus_magic", false] || !player["circus_agility", false] || !player["circus_ranged", false]
            3008 -> player["bork_defeated_day", 0] != days()
            3010 -> minutes() < player["skeletal_horror_respawn_minute", 0]
            3013 -> player.summoningCombatLevel >= 40
            3015 -> player.hasMax(Skill.Attack, 65) || player.hasMax(Skill.Defence, 65)
            3031 -> player.summoningCombatLevel >= 48
            3034 -> player.levels.getMax(Skill.Strength) + player.levels.getMax(Skill.Attack) >= 130 || !player.hasMax(Skill.Attack, 99) || !player.hasMax(Skill.Strength, 99)
            3500 -> unstableFoundationsStage(player) == 3500
            3501 -> unstableFoundationsStage(player) == 3501
            3505 -> unstableFoundationsStage(player) == 3505
            3511 -> unstableFoundationsStage(player) == 3511
            3502 -> unstableFoundationsStage(player) == 3502
            3503 -> unstableFoundationsStage(player) == 3503
            3504 -> unstableFoundationsStage(player) == 3504
            3506 -> unstableFoundationsStage(player) == 3506
            3508 -> unstableFoundationsStage(player) == 3508
            3509 -> unstableFoundationsStage(player) == 3509
            3507 -> unstableFoundationsStage(player) == 3507
            3523 -> unstableFoundationsStage(player) == 3523
            3510 -> unstableFoundationsStage(player) == 3510
            3512 -> unstableFoundationsStage(player) == 3512
            3513 -> unstableFoundationsStage(player) == 3513
            3514 -> unstableFoundationsStage(player) == 3514
            3515 -> player.getInt("unstable_foundations", "unstarted") in 135..160 && player["varbit_6495", 0] != 0 && player["varbit_6495", 0] != 1
            3516 -> player.getInt("unstable_foundations", "unstarted") in 135..160 && player["varbit_6495", 0] != 2 && player["varbit_6495", 0] != 3
            3517 -> player.getInt("unstable_foundations", "unstarted") >= 140 && player["varbit_6495", 0] != 0 && player["varbit_6495", 0] != 1 && !player.has(Skill.Woodcutting, 2)
            3518 -> player.getInt("unstable_foundations", "unstarted") >= 140 && player["varbit_6495", 0] != 2 && player["varbit_6495", 0] != 3 && !player.has(Skill.Mining, 2)
            3519 -> unstableFoundationsStage(player) == 3519
            3520 -> unstableFoundationsStage(player) == 3521 && player["varbit_6494", 0] <= 2
            3521 -> unstableFoundationsStage(player) == 3521 && player["varbit_6494", 0] == 5
            else -> true
        }
    }

    private fun Player.getInt(id: String, default: String): Int {
        return get<VariableDefinitions>().get(id)!!.values.toInt(this[id, default])
    }

    private fun minutes() = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis())

    private fun days(): Int {
        val now = Calendar.getInstance()
        now.set(HOUR_OF_DAY, 12)
        return TimeUnit.MILLISECONDS.toDays(now.timeInMillis).toInt()
    }

    private fun kudosCount(player: Player): Int {
        var kudos = 88
        if (player["varbit_5387", 0] == 250) {
            kudos += 5
        }
        if (player["varbit_2561", 0] >= 3) {
            kudos += 5
        }
        if (player["varp_131", 0] >= 9) {
            kudos += 5
        }
        if (player["varbit_358", 0] == 15) {
            kudos += 10
        }
        if (player["varbit_6001", 0] >= 45) {
            kudos += 10
        }
        if (player["varp_150", 0] >= 160) {
            kudos += 5
        }
        if (player["varp_223", 0] >= 9) {
            kudos += 5
        }
        if (player["varbit_1990", 0] >= 430) {
            kudos += 5
        }
        if (player["varbit_1383", 0] >= 4) {
            kudos += 5
        }
        if (player["varbit_5075", 0] == 20) {
            kudos += 5
        }
        if (player["varp_14", 0] >= 7) {
            kudos += 5
        }
        if (player["varp_112", 0] >= 7) {
            kudos += 5
        }
        if (player["varp_302", 0] >= 61) {
            kudos += 5
        }
        if (player["varp_63", 0] >= 6) {
            kudos += 5
        }
        if (player["varp_145", 0] >= 7 || player["varp_146", 0] >= 4) {
            kudos += 5
        }
        if (player["varbit_1028", 0] >= 70) {
            kudos += 5
        }
        if (player["varp_26", 0] >= 80) {
            kudos += 5
        }
        if (player["varbit_3523", 0] >= 150) {
            kudos += 5
        }
        return kudos
    }

    private fun unstableFoundationsStage(player: Player): Int {
        val stage = player.getInt("unstable_foundations", "unstarted")
        when (stage) {
            7 -> return 3500
            9 -> return 3500
            10 -> return 3501
            15 -> return 3502
            20 -> return 3503
            25 -> return 3503
            30 -> return 3503
            35 -> return 3504
            40 -> return 3504
            45 -> return 3505
            46 -> return 3505
            47 -> return 3505
            48 -> return 3505
            49 -> return 3505
            50 -> return 3506
            55 -> return 3506
            56 -> return 3506
            57 -> return 3506
            60 -> return 3508
            62 -> return 3509
            65 -> return 3507
            70 -> return 3523
            75 -> return 3507
            80 -> return 3510
            82 -> return 3510
            83 -> return 3510
            90 -> return 3510
            93 -> return 3511
            95 -> return 3511
            96 -> return 3512
            98 -> return 3512
            100 -> return 3512
            105 -> return 3512
            110 -> return 3512
            115 -> return 3512
            120 -> return 3513
            123 -> return 3513
            125 -> return 3513
            126 -> return 3513
            127 -> return 3513
            128 -> return 3514
            130 -> return 3514
            135 -> return 3514
            140 -> return 3514
            145 -> return 3514
            150 -> return 3514
            155 -> return 3514
            160 -> return 3514
            165 -> return 3514
            170 -> return 3519
            1000 -> return 3521
        }
        return 4094
    }
}