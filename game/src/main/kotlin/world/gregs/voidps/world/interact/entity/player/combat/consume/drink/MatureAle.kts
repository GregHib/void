package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume
import kotlin.math.ceil

consume({ item.id.startsWith("asgarnian_ale") }) { player: Player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Strength, if (mature) 3 else 2)
    player.levels.drain(Skill.Attack, if (mature) 6 else 4)
}

consume({ item.id.startsWith("axemans_folly") }) { player: Player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Woodcutting, if (mature) 2 else 1)
    player.levels.drain(Skill.Attack, if (mature) 4 else 3)
    player.levels.drain(Skill.Strength, if (mature) 4 else 3)
}

consume({ item.id.startsWith("chefs_delight") }) { player: Player ->
    val mature = item.id.endsWith("_m")
    val boost = ceil((player.levels.getMax(Skill.Cooking) + if (mature) 1 else 0) * 0.05).toInt()
    player.levels.boost(Skill.Cooking, boost)
    player.levels.drain(Skill.Attack, if (mature) 3 else 2)
    player.levels.drain(Skill.Strength, if (mature) 3 else 2)
}

consume({ item.id.endsWith("cider") }) { player: Player ->
    val mature = item.id.startsWith("mature_")
    player.levels.boost(Skill.Farming, if (mature) 2 else 1)
    player.levels.drain(Skill.Attack, if (mature) 5 else 2)
    player.levels.drain(Skill.Strength, if (mature) 5 else 2)
}

consume({ item.id.startsWith("dragon_bitter") }) { player: Player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Strength, if (mature) 3 else 2)
    player.levels.drain(Skill.Attack, if (mature) 6 else 4)
}

consume({ item.id.startsWith("dwarven_stout") }) { player: Player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Smithing, if (mature) 2 else 1)
    player.levels.boost(Skill.Mining, if (mature) 2 else 1)
    player.levels.drain(Skill.Attack, if (mature) 7 else 2)
    player.levels.drain(Skill.Strength, if (mature) 7 else 2)
    player.levels.drain(Skill.Defence, if (mature) 7 else 2)
}

consume({ item.id.startsWith("greenmans_ale") }) { player: Player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Herblore, if (mature) 2 else 1)
    player.levels.drain(Skill.Attack, if (mature) 2 else 3)
    player.levels.drain(Skill.Strength, if (mature) 2 else 3)
}

consume({ item.id.startsWith("slayers_respite") }) { player: Player ->
    val mature = item.id.endsWith("_m")
    player.levels.boost(Skill.Slayer, if (mature) 4 else 2)
    player.levels.drain(Skill.Attack, 2)
    player.levels.drain(Skill.Strength, 2)
}

consume({ item.id.endsWith("wizards_mind_bomb") }) { player: Player ->
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