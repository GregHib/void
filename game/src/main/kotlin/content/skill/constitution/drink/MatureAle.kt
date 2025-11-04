package content.skill.constitution.drink

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import kotlin.math.ceil

class MatureAle : Script {

    init {
        consumed("asgarnian_ale*") { item, _ ->
            val mature = item.id.endsWith("_m")
            levels.boost(Skill.Strength, if (mature) 3 else 2)
            levels.drain(Skill.Attack, if (mature) 6 else 4)
        }

        consumed("axemans_folly*") { item, _ ->
            val mature = item.id.endsWith("_m")
            levels.boost(Skill.Woodcutting, if (mature) 2 else 1)
            levels.drain(Skill.Attack, if (mature) 4 else 3)
            levels.drain(Skill.Strength, if (mature) 4 else 3)
        }

        consumed("chefs_delight*") { item, _ ->
            val mature = item.id.endsWith("_m")
            val boost = ceil((levels.getMax(Skill.Cooking) + if (mature) 1 else 0) * 0.05).toInt()
            levels.boost(Skill.Cooking, boost)
            levels.drain(Skill.Attack, if (mature) 3 else 2)
            levels.drain(Skill.Strength, if (mature) 3 else 2)
        }

        consumed("*cider") { item, _ ->
            val mature = item.id.startsWith("mature_")
            levels.boost(Skill.Farming, if (mature) 2 else 1)
            levels.drain(Skill.Attack, if (mature) 5 else 2)
            levels.drain(Skill.Strength, if (mature) 5 else 2)
        }

        consumed("dragon_bitter*") { item, _ ->
            val mature = item.id.endsWith("_m")
            levels.boost(Skill.Strength, if (mature) 3 else 2)
            levels.drain(Skill.Attack, if (mature) 6 else 4)
        }

        consumed("dwarven_stout*") { item, _ ->
            val mature = item.id.endsWith("_m")
            levels.boost(Skill.Smithing, if (mature) 2 else 1)
            levels.boost(Skill.Mining, if (mature) 2 else 1)
            levels.drain(Skill.Attack, if (mature) 7 else 2)
            levels.drain(Skill.Strength, if (mature) 7 else 2)
            levels.drain(Skill.Defence, if (mature) 7 else 2)
        }

        consumed("greenmans_ale*") { item, _ ->
            val mature = item.id.endsWith("_m")
            levels.boost(Skill.Herblore, if (mature) 2 else 1)
            levels.drain(Skill.Attack, if (mature) 2 else 3)
            levels.drain(Skill.Strength, if (mature) 2 else 3)
        }

        consumed("slayers_respite*") { item, _ ->
            val mature = item.id.endsWith("_m")
            levels.boost(Skill.Slayer, if (mature) 4 else 2)
            levels.drain(Skill.Attack, 2)
            levels.drain(Skill.Strength, 2)
        }

        consumed("*wizards_mind_bomb") { item, _ ->
            val mature = item.id.startsWith("mature_")
            val boost = (if (levels.getMax(Skill.Magic) < 50) 2 else 3) + if (mature) 1 else 0
            levels.boost(Skill.Magic, boost)
            if (mature) {
                levels.drain(Skill.Attack, 5)
                levels.drain(Skill.Strength, 5)
            } else {
                levels.drain(Skill.Attack, 1, 0.05)
                levels.drain(Skill.Strength, 1, 0.05)
            }
        }
    }
}
