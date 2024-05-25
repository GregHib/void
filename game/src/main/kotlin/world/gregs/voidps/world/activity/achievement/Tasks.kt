package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.cache.config.data.StructDefinition
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
import world.gregs.voidps.world.activity.quest.questComplete
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.concurrent.TimeUnit

object Tasks {

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
        return get<VariableDefinitions>().get(id)!!.values.toInt(this.get(id, default))
    }


    fun script_3223(player: Player, arg0: Int, arg1: Int): String {
        var int2: Int
        var str3: String?
        str3 = ""
        int2 = 0
        when (arg0) {
            147, 23, 294, 167, 249 -> if (arg1 == 1) {
                str3 = "You must have access to the fairy ring network to complete this Task."
                if (player["fairy_rings_unlocked", false]) {
                    int2 = 1
                }
            }
            49 -> if (arg1 == 1) {
                str3 = "You must unlock 500 music tracks in order to perform the Air Guitar emote."
                if (player["unlocked_emote_air_guitar", false]) {
                    int2 = 1
                }
            }
            59 -> if (arg1 == 2) {
                str3 = "You must also have completed the Abyss miniquest."
                if (player.questComplete("enter_the_abyss")) {
                    int2 = 1
                }
            }
            107 -> if (arg1 == 1) {
                str3 = "You must have progressed to a certain point in the Dragon Slayer quest."
                if (player.getInt("dragon_slayer", "unstarted") >= 2 && player["dragon_slayer_received_shield", false] || player.questComplete("dragon_slayer")) {
                    int2 = 1
                }
            }
            178 -> if (arg1 == 1) {
                str3 = "You must begin the relevant section of Otto Godblessed's barbarian training."
                if (player["barbarian_training_fishing_unlocked", false]) {
                    int2 = 1
                }
            }
            180 -> if (arg1 == 1) {
                str3 = "You must begin the relevant section of Otto Godblessed's barbarian training."
                if (player["barbarian_training_pyre_unlocked", false]) {
                    int2 = 1
                }
            }
            177 -> if (arg1 == 1) {
                str3 = "You must begin the relevant section of Otto Godblessed's barbarian training."
                if (player["barbarian_training_pyre_unlocked", false]) {
                    int2 = 1
                }
            }
            316 -> if (arg1 == 1) {
                str3 = "You must begin the relevant section of Otto Godblessed's barbarian training."
                if (player["barbarian_training_fishing_unlocked", false]) {
                    int2 = 1
                }
            }
            321 -> if (arg1 == 1) {
                str3 = "You must begin the relevant section of Otto Godblessed's barbarian training."
                if (player["barbarian_training_pyre_unlocked", false]) {
                    int2 = 1
                }
            }
            322 -> if (arg1 == 1) {
                str3 = "You must begin the relevant section of Otto Godblessed's barbarian training."
                if (player["barbarian_training_hasta_unlocked", false]) {
                    int2 = 1
                }
            }
            323 -> if (arg1 == 1) {
                str3 = "You must begin the relevant section of Otto Godblessed's barbarian training."
                if (player["barbarian_training_mix_unlocked", false]) {
                    int2 = 1
                }
            }
            175 -> if (arg1 == 1) {
                str3 = "You must complete the Bar Crawl miniquest."
                if (player.questComplete("bar_crawl_miniquest") || player["bar_crawl_started", false]) {
                    int2 = 1
                }
            }
            331, 219 -> if (arg1 == 2) {
                str3 = "You must have a total combat level of at least 100 to accept an assignment in Shilo Village."
                if (player.summoningCombatLevel >= 100) {
                    int2 = 1
                }
            }
            248 -> if (arg1 == 1) {
                str3 = "You must have completed the Knight Waves in Camelot."
                if (player["knights_waves", 0] == 8) {
                    int2 = 1
                }
            }
            3011, 276 -> if (arg1 == 1) {
                str3 = "You require 33 Quest Points to enter the Champions' Guild."
                if (player["quest_points", 0] >= 33) {
                    int2 = 1
                }
            }
            281 -> if (arg1 == 1) {
                str3 = "You must unlock all four emotes by completing levels of the Stronghold of Security."
                if (player["unlocked_emote_flap", false] && player["unlocked_emote_slap_head", false] && player["unlocked_emote_idea", false] && player["unlocked_emote_stomp", false]) {
                    int2 = 1
                }
            }
            285 -> if (arg1 == 1) {
                str3 = "You must learn the secret of the Senntisten necklace."
                if (player["teleport_to_digsite_with_pendant", false]) {
                    int2 = 1
                }
            }
            289 -> if (arg1 == 1) {
                str3 = "You must have a total combat level of at least 40 to accept an assignment from Vannaka."
                if (player.summoningCombatLevel >= 40) {
                    int2 = 1
                }
            }
            300 -> if (arg1 == 1) {
                str3 = "Completing quests will increase your access to Kudos with the Varrock Museum."
                if (kudosCount(player) >= 153) {
                    int2 = 1
                }
            }
            3000 -> if (arg1 == 2) {
                if (minutes() >= player["varp_451", 0]) {
                    int2 = 1
                }
                str3 = "You may gather the Tears of Guthix once every week."
            } else if (arg1 == 3) {
                if (player["quest_points", 0] >= player["varbit_456", 0] || player["total_experience", 0.0] >= player["varp_450", 0.0]) {
                    int2 = 1
                }
                str3 = "You must have gained a Quest Point or 100,000 total experience to enter Juna's cavern."
            }
            3013, 3001 -> if (arg1 == 1) {
                str3 = "You must have a total combat level of at least 40 to fight for the Void Knights."
                if (player.combatLevel >= 40) {
                    int2 = 1
                }
            }
            3002 -> if (arg1 == 1) {
                str3 = "You must have Larry or Chuck explain the purpose of penguin spying."
                if (player["penguin_hide_and_seek_explained", false]) {
                    int2 = 1
                }
            } else if (arg1 == 2) {
                str3 = "You must have spied on fewer than ten penguins already this week."
                if (player["penguins_found_weekly", 0] < 10) {
                    int2 = 1
                }
            } else if (arg1 == 3) {
                str3 = "You may spy on penguins if your total Penguin Points are less than the maximum of fifty."
                if (player["penguin_points", 0] < 50) {
                    int2 = 1
                }
            }
            12 -> if (arg1 == 1) {
                str3 = "You must have Larry or Chuck explain the purpose of Penguin Hide and Seek."
                if (player["penguin_hide_and_seek_explained", false]) {
                    int2 = 1
                }
            }
            3003 -> if (arg1 == 1) {
                str3 = "You may not chop down more than two evil trees per day."
                if (player["nurture_evil_tree_stage", 0] < 2) {
                    int2 = 1
                }
            }
            3007 -> if (arg1 == 1) {
                str3 = "You may attempt the Agility, Magic and Ranged performances after a week has passed since your last show."
                if (!player["circus_magic", false] || !player["circus_agility", false] || !player["circus_ranged", false]) {
                    int2 = 1
                }
            }
            3008 -> if (arg1 == 2) {
                str3 = "You must wait at least a day since you last faced Bork."
                if (player["bork_defeated_day", 0] != days()) {
                    int2 = 1
                }
            }
            3010 -> if (arg1 == 2) {
                str3 = "At least a week must pass since you last faced the Skeletal Horror."
                if (minutes() < player["varbit_6305", 0]) {
                    int2 = 1
                }
            }
            3012 -> if (arg1 == 1) {
                str3 = "You require 50 Runecrafting to enter the Runecrafters' Guild."
                if (player.hasMax(Skill.Runecrafting, 50)) {
                    int2 = 1
                }
            }
            3015 -> if (arg1 == 2) {
                str3 = "You must have at least 65 Attack or Defence in order to take on a case."
                if (player.hasMax(Skill.Attack, 65) || player.hasMax(Skill.Defence, 65)) {
                    int2 = 1
                }
            }
            3031 -> if (arg1 == 1) {
                str3 = "You must have a total combat level of at least 48 to fight in the Clan Wars."
                if (player.summoningCombatLevel >= 48) {
                    int2 = 1
                }
            }
            3034 -> if (arg1 == 1) {
                str3 = "To enter the Warriors' Guild your Attack or Strength level must be 99, or your combined Attack and Strength levels must total 130 or more."
                if (player.levels.getMax(Skill.Strength) + player.levels.getMax(Skill.Attack) >= 130 || player.hasMax(Skill.Attack, 99) || player.hasMax(Skill.Strength, 99)) {
                    int2 = 1
                }
            }
            else -> {
                str3 = ""
                int2 = 0
            }
        }
        if (int2 == 1) {
            str3 = "<str>$str3"
        }
        return str3
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