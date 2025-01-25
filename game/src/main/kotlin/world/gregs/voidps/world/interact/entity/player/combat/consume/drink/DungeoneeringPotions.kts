package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume
import content.entity.player.effect.antifire
import content.entity.player.effect.superAntifire
import content.entity.effect.toxin.antiPoison

consume("weak_melee_potion") { player ->
    player.levels.boost(Skill.Attack, 2, 0.07)
    player.levels.boost(Skill.Strength, 2, 0.07)
}

consume("weak_magic_potion") { player ->
    player.levels.boost(Skill.Magic, 2, 0.07)
}

consume("weak_range_potion") { player ->
    player.levels.boost(Skill.Ranged, 2, 0.07)
}

consume("weak_defence_potion") { player ->
    player.levels.boost(Skill.Defence, 2, 0.07)
}

consume("weak_stat_restore_potion") { player ->
    Skill.all.filterNot { it == Skill.Constitution || it == Skill.Prayer }.forEach { skill ->
        player.levels.restore(skill, 5, 0.12)
    }
}

consume("antipoison_dungeoneering") { player ->
    player.antiPoison(1)
}

consume("weak_cure_potion") { player ->
    player.antiPoison(5)
    player.antifire(5)
}

consume("weak_rejuvenation_potion") { player ->
    player.levels.restore(Skill.Prayer, 4, 0.08)
    player.levels.restore(Skill.Summoning, 4, 0.08)
}

consume("weak_gatherers_potion") { player ->
    player.levels.boost(Skill.Woodcutting, 3, 0.02)
    player.levels.boost(Skill.Mining, 3, 0.02)
    player.levels.boost(Skill.Fishing, 3, 0.02)
}

consume("weak_artisans_potion") { player ->
    player.levels.boost(Skill.Smithing, 3, 0.02)
    player.levels.boost(Skill.Crafting, 3, 0.02)
    player.levels.boost(Skill.Fletching, 3, 0.02)
    player.levels.boost(Skill.Construction, 3, 0.02)
    player.levels.boost(Skill.Firemaking, 3, 0.02)
}

consume("weak_naturalists_potion") { player ->
    player.levels.boost(Skill.Cooking, 3, 0.02)
    player.levels.boost(Skill.Farming, 3, 0.02)
    player.levels.boost(Skill.Herblore, 3, 0.02)
    player.levels.boost(Skill.Runecrafting, 3, 0.02)
}

consume("weak_survivalists_potion") { player ->
    player.levels.boost(Skill.Agility, 3, 0.02)
    player.levels.boost(Skill.Hunter, 3, 0.02)
    player.levels.boost(Skill.Thieving, 3, 0.02)
    player.levels.boost(Skill.Slayer, 3, 0.02)
}


consume("melee_potion") { player ->
    player.levels.boost(Skill.Attack, 3, 0.11)
    player.levels.boost(Skill.Strength, 3, 0.11)
}

consume("magic_potion_dungeoneering") { player ->
    player.levels.boost(Skill.Magic, 3, 0.11)
}

consume("ranged_potion_dungeoneering") { player ->
    player.levels.boost(Skill.Ranged, 3, 0.11)
}

consume("defence_potion_dungeoneering") { player ->
    player.levels.boost(Skill.Defence, 3, 0.11)
}

consume("stat_restore_potion_dungeoneering") { player ->
    Skill.all.filterNot { it == Skill.Constitution || it == Skill.Prayer }.forEach { skill ->
        player.levels.restore(skill, 7, 0.17)
    }
}

consume("cure_potion") { player ->
    player.antiPoison(10)
    player.superAntifire(10)
}

consume("rejuvenation_potion") { player ->
    player.levels.restore(Skill.Prayer, 7, 0.15)
    player.levels.restore(Skill.Summoning, 7, 0.15)
}

consume("gatherers_potion") { player ->
    player.levels.boost(Skill.Woodcutting, 4, 0.04)
    player.levels.boost(Skill.Mining, 4, 0.04)
    player.levels.boost(Skill.Fishing, 4, 0.04)
}

consume("artisans_potion") { player ->
    player.levels.boost(Skill.Smithing, 4, 0.04)
    player.levels.boost(Skill.Crafting, 4, 0.04)
    player.levels.boost(Skill.Fletching, 4, 0.04)
    player.levels.boost(Skill.Construction, 4, 0.04)
    player.levels.boost(Skill.Firemaking, 4, 0.04)
}

consume("naturalists_potion") { player ->
    player.levels.boost(Skill.Cooking, 4, 0.04)
    player.levels.boost(Skill.Farming, 4, 0.04)
    player.levels.boost(Skill.Herblore, 4, 0.04)
    player.levels.boost(Skill.Runecrafting, 4, 0.04)
}

consume("survivalists_potion") { player ->
    player.levels.boost(Skill.Agility, 4, 0.04)
    player.levels.boost(Skill.Hunter, 4, 0.04)
    player.levels.boost(Skill.Thieving, 4, 0.04)
    player.levels.boost(Skill.Slayer, 4, 0.04)
}


consume("strong_melee_potion") { player ->
    player.levels.boost(Skill.Attack, 6, 0.2)
    player.levels.boost(Skill.Strength, 6, 0.2)
}

consume("strong_magic_potion") { player ->
    player.levels.boost(Skill.Magic, 6, 0.2)
}

consume("strong_ranged_potion") { player ->
    player.levels.boost(Skill.Ranged, 6, 0.2)
}

consume("strong_defence_potion") { player ->
    player.levels.boost(Skill.Defence, 6, 0.2)
}

consume("strong_stat_restore_potion") { player ->
    Skill.all.filterNot { it == Skill.Constitution || it == Skill.Prayer }.forEach { skill ->
        player.levels.restore(skill, 10, 0.24)
    }
}

consume("strong_cure_potion") { player ->
    player.antiPoison(20)
    player.superAntifire(20)
}

consume("strong_rejuvenation_potion") { player ->
    player.levels.restore(Skill.Prayer, 10, 0.22)
    player.levels.restore(Skill.Summoning, 10, 0.22)
}

consume("strong_gatherers_potion") { player ->
    player.levels.boost(Skill.Woodcutting, 6, 0.06)
    player.levels.boost(Skill.Mining, 6, 0.06)
    player.levels.boost(Skill.Fishing, 6, 0.06)
}

consume("strong_artisans_potion") { player ->
    player.levels.boost(Skill.Smithing, 6, 0.06)
    player.levels.boost(Skill.Crafting, 6, 0.06)
    player.levels.boost(Skill.Fletching, 6, 0.06)
    player.levels.boost(Skill.Construction, 6, 0.06)
    player.levels.boost(Skill.Firemaking, 6, 0.06)
}

consume("strong_naturalists_potion") { player ->
    player.levels.boost(Skill.Cooking, 6, 0.06)
    player.levels.boost(Skill.Farming, 6, 0.06)
    player.levels.boost(Skill.Herblore, 6, 0.06)
    player.levels.boost(Skill.Runecrafting, 6, 0.06)
}

consume("strong_survivalists_potion") { player ->
    player.levels.boost(Skill.Agility, 6, 0.06)
    player.levels.boost(Skill.Hunter, 6, 0.06)
    player.levels.boost(Skill.Thieving, 6, 0.06)
    player.levels.boost(Skill.Slayer, 6, 0.06)
}
