import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume
import world.gregs.voidps.world.activity.combat.consume.drink.antifire
import world.gregs.voidps.world.activity.combat.consume.drink.superAntifire
import world.gregs.voidps.world.interact.entity.player.toxin.antiPoison

on<Consume>({ item.id == "weak_melee_potion" }) { player: Player ->
    player.levels.boost(Skill.Attack, 2, 0.07)
    player.levels.boost(Skill.Strength, 2, 0.07)
}

on<Consume>({ item.id == "weak_magic_potion" }) { player: Player ->
    player.levels.boost(Skill.Magic, 2, 0.07)
}

on<Consume>({ item.id == "weak_range_potion" }) { player: Player ->
    player.levels.boost(Skill.Ranged, 2, 0.07)
}

on<Consume>({ item.id == "weak_defence_potion" }) { player: Player ->
    player.levels.boost(Skill.Defence, 2, 0.07)
}

on<Consume>({ item.id == "weak_stat_restore_potion" }) { player: Player ->
    Skill.all.filterNot { it == Skill.Constitution || it == Skill.Prayer }.forEach { skill ->
        player.levels.restore(skill, 5, 0.12)
    }
}

on<Consume>({ item.id == "antipoison_dungeoneering" }) { player: Player ->
    player.antiPoison(1)
}

on<Consume>({ item.id == "weak_cure_potion" }) { player: Player ->
    player.antiPoison(5)
    player.antifire(5)
}

on<Consume>({ item.id == "weak_rejuvenation_potion" }) { player: Player ->
    player.levels.restore(Skill.Prayer, 4, 0.08)
    player.levels.restore(Skill.Summoning, 4, 0.08)
}

on<Consume>({ item.id == "weak_gatherers_potion" }) { player: Player ->
    player.levels.boost(Skill.Woodcutting, 3, 0.02)
    player.levels.boost(Skill.Mining, 3, 0.02)
    player.levels.boost(Skill.Fishing, 3, 0.02)
}

on<Consume>({ item.id == "weak_artisans_potion" }) { player: Player ->
    player.levels.boost(Skill.Smithing, 3, 0.02)
    player.levels.boost(Skill.Crafting, 3, 0.02)
    player.levels.boost(Skill.Fletching, 3, 0.02)
    player.levels.boost(Skill.Construction, 3, 0.02)
    player.levels.boost(Skill.Firemaking, 3, 0.02)
}

on<Consume>({ item.id == "weak_naturalists_potion" }) { player: Player ->
    player.levels.boost(Skill.Cooking, 3, 0.02)
    player.levels.boost(Skill.Farming, 3, 0.02)
    player.levels.boost(Skill.Herblore, 3, 0.02)
    player.levels.boost(Skill.Runecrafting, 3, 0.02)
}

on<Consume>({ item.id == "weak_survivalists_potion" }) { player: Player ->
    player.levels.boost(Skill.Agility, 3, 0.02)
    player.levels.boost(Skill.Hunter, 3, 0.02)
    player.levels.boost(Skill.Thieving, 3, 0.02)
    player.levels.boost(Skill.Slayer, 3, 0.02)
}


on<Consume>({ item.id == "melee_potion" }) { player: Player ->
    player.levels.boost(Skill.Attack, 3, 0.11)
    player.levels.boost(Skill.Strength, 3, 0.11)
}

on<Consume>({ item.id == "magic_potion_dungeoneering" }) { player: Player ->
    player.levels.boost(Skill.Magic, 3, 0.11)
}

on<Consume>({ item.id == "ranged_potion_dungeoneering" }) { player: Player ->
    player.levels.boost(Skill.Ranged, 3, 0.11)
}

on<Consume>({ item.id == "defence_potion_dungeoneering" }) { player: Player ->
    player.levels.boost(Skill.Defence, 3, 0.11)
}

on<Consume>({ item.id == "stat_restore_potion_dungeoneering" }) { player: Player ->
    Skill.all.filterNot { it == Skill.Constitution || it == Skill.Prayer }.forEach { skill ->
        player.levels.restore(skill, 7, 0.17)
    }
}

on<Consume>({ item.id == "cure_potion" }) { player: Player ->
    player.antiPoison(10)
    player.superAntifire(10)
}

on<Consume>({ item.id == "rejuvenation_potion" }) { player: Player ->
    player.levels.restore(Skill.Prayer, 7, 0.15)
    player.levels.restore(Skill.Summoning, 7, 0.15)
}

on<Consume>({ item.id == "gatherers_potion" }) { player: Player ->
    player.levels.boost(Skill.Woodcutting, 4, 0.04)
    player.levels.boost(Skill.Mining, 4, 0.04)
    player.levels.boost(Skill.Fishing, 4, 0.04)
}

on<Consume>({ item.id == "artisans_potion" }) { player: Player ->
    player.levels.boost(Skill.Smithing, 4, 0.04)
    player.levels.boost(Skill.Crafting, 4, 0.04)
    player.levels.boost(Skill.Fletching, 4, 0.04)
    player.levels.boost(Skill.Construction, 4, 0.04)
    player.levels.boost(Skill.Firemaking, 4, 0.04)
}

on<Consume>({ item.id == "naturalists_potion" }) { player: Player ->
    player.levels.boost(Skill.Cooking, 4, 0.04)
    player.levels.boost(Skill.Farming, 4, 0.04)
    player.levels.boost(Skill.Herblore, 4, 0.04)
    player.levels.boost(Skill.Runecrafting, 4, 0.04)
}

on<Consume>({ item.id == "survivalists_potion" }) { player: Player ->
    player.levels.boost(Skill.Agility, 4, 0.04)
    player.levels.boost(Skill.Hunter, 4, 0.04)
    player.levels.boost(Skill.Thieving, 4, 0.04)
    player.levels.boost(Skill.Slayer, 4, 0.04)
}


on<Consume>({ item.id == "strong_melee_potion" }) { player: Player ->
    player.levels.boost(Skill.Attack, 6, 0.2)
    player.levels.boost(Skill.Strength, 6, 0.2)
}

on<Consume>({ item.id == "strong_magic_potion" }) { player: Player ->
    player.levels.boost(Skill.Magic, 6, 0.2)
}

on<Consume>({ item.id == "strong_ranged_potion" }) { player: Player ->
    player.levels.boost(Skill.Ranged, 6, 0.2)
}

on<Consume>({ item.id == "strong_defence_potion" }) { player: Player ->
    player.levels.boost(Skill.Defence, 6, 0.2)
}

on<Consume>({ item.id == "strong_stat_restore_potion" }) { player: Player ->
    Skill.all.filterNot { it == Skill.Constitution || it == Skill.Prayer }.forEach { skill ->
        player.levels.restore(skill, 10, 0.24)
    }
}

on<Consume>({ item.id == "strong_cure_potion" }) { player: Player ->
    player.antiPoison(20)
    player.superAntifire(20)
}

on<Consume>({ item.id == "strong_rejuvenation_potion" }) { player: Player ->
    player.levels.restore(Skill.Prayer, 10, 0.22)
    player.levels.restore(Skill.Summoning, 10, 0.22)
}

on<Consume>({ item.id == "strong_gatherers_potion" }) { player: Player ->
    player.levels.boost(Skill.Woodcutting, 6, 0.06)
    player.levels.boost(Skill.Mining, 6, 0.06)
    player.levels.boost(Skill.Fishing, 6, 0.06)
}

on<Consume>({ item.id == "strong_artisans_potion" }) { player: Player ->
    player.levels.boost(Skill.Smithing, 6, 0.06)
    player.levels.boost(Skill.Crafting, 6, 0.06)
    player.levels.boost(Skill.Fletching, 6, 0.06)
    player.levels.boost(Skill.Construction, 6, 0.06)
    player.levels.boost(Skill.Firemaking, 6, 0.06)
}

on<Consume>({ item.id == "strong_naturalists_potion" }) { player: Player ->
    player.levels.boost(Skill.Cooking, 6, 0.06)
    player.levels.boost(Skill.Farming, 6, 0.06)
    player.levels.boost(Skill.Herblore, 6, 0.06)
    player.levels.boost(Skill.Runecrafting, 6, 0.06)
}

on<Consume>({ item.id == "strong_survivalists_potion" }) { player: Player ->
    player.levels.boost(Skill.Agility, 6, 0.06)
    player.levels.boost(Skill.Hunter, 6, 0.06)
    player.levels.boost(Skill.Thieving, 6, 0.06)
    player.levels.boost(Skill.Slayer, 6, 0.06)
}
