package content.skill.constitution.drink

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.skill.constitution.consume
import kotlin.math.ceil

consume("asgarnian_ale*") { player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Strength, if (mature) 3 else 2)
    player.levels.drain(Skill.Attack, if (mature) 6 else 4)
}

consume("axemans_folly*") { player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Woodcutting, if (mature) 2 else 1)
    player.levels.drain(Skill.Attack, if (mature) 4 else 3)
    player.levels.drain(Skill.Strength, if (mature) 4 else 3)
}

consume("chefs_delight*") { player ->
    val mature = item.id.endsWith("_m")
    val boost = ceil((player.levels.getMax(Skill.Cooking) + if (mature) 1 else 0) * 0.05).toInt()
    player.levels.boost(Skill.Cooking, boost)
    player.levels.drain(Skill.Attack, if (mature) 3 else 2)
    player.levels.drain(Skill.Strength, if (mature) 3 else 2)
}

consume("*cider") { player ->
    val mature = item.id.startsWith("mature_")
    player.levels.boost(Skill.Farming, if (mature) 2 else 1)
    player.levels.drain(Skill.Attack, if (mature) 5 else 2)
    player.levels.drain(Skill.Strength, if (mature) 5 else 2)
}

consume("dragon_bitter*") { player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Strength, if (mature) 3 else 2)
    player.levels.drain(Skill.Attack, if (mature) 6 else 4)
}

consume("dwarven_stout*") { player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Smithing, if (mature) 2 else 1)
    player.levels.boost(Skill.Mining, if (mature) 2 else 1)
    player.levels.drain(Skill.Attack, if (mature) 7 else 2)
    player.levels.drain(Skill.Strength, if (mature) 7 else 2)
    player.levels.drain(Skill.Defence, if (mature) 7 else 2)
}

consume("greenmans_ale*") { player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Herblore, if (mature) 2 else 1)
    player.levels.drain(Skill.Attack, if (mature) 2 else 3)
    player.levels.drain(Skill.Strength, if (mature) 2 else 3)
}

consume("slayers_respite*") { player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Slayer, if (mature) 4 else 2)
    player.levels.drain(Skill.Attack, 2)
    player.levels.drain(Skill.Strength, 2)
}

consume("*wizards_mind_bomb") { player ->
    val mature = item.id.startsWith("mature_")
    val boost = (if (player.levels.getMax(Skill.Magic) < 50) 2 else 3) + if (mature) 1 else 0
    player.levels.boost(Skill.Magic, boost)
    if (mature) {
        player.levels.drain(Skill.Attack, 5)
        player.levels.drain(Skill.Strength, 5)
    } else {
        player.levels.drain(Skill.Attack, 1, 0.05)
        player.levels.drain(Skill.Strength, 1, 0.05)
    }
}