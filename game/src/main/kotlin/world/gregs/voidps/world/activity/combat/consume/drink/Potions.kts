import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.combat.consume.Consumable
import world.gregs.voidps.world.activity.combat.consume.Consume
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.specialAttackEnergy
import world.gregs.voidps.world.interact.entity.player.cure
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy

on<Consume>({ item.id.startsWith("attack_potion") || item.id.startsWith("attack_mix") }) { player: Player ->
    player.levels.boost(Skill.Attack, 3, 0.1)
}

on<Consume>({ item.id.startsWith("strength_potion") || item.id.startsWith("strength_mix") }) { player: Player ->
    player.levels.boost(Skill.Strength, 3, 0.1)
}

on<Consume>({ item.id.startsWith("defence_potion") || item.id.startsWith("defence_mix") }) { player: Player ->
    player.levels.boost(Skill.Defence, 3, 0.1)
}

on<Consume>({ item.id.startsWith("magic_essence") || item.id.startsWith("magic_essence_mix") }) { player: Player ->
    player.levels.boost(Skill.Magic, 3)
}

on<Consume>({ item.id.startsWith("agility_potion") || item.id.startsWith("agility_mix") }) { player: Player ->
    player.levels.boost(Skill.Agility, 3)
}

on<Consume>({ item.id.startsWith("fishing_potion") || item.id.startsWith("fishing_mix") }) { player: Player ->
    player.levels.boost(Skill.Fishing, 3)
}

on<Consume>({ item.id.startsWith("crafting_potion") }) { player: Player ->
    player.levels.boost(Skill.Crafting, 3)
}

on<Consume>({ item.id.startsWith("hunter_potion") || item.id.startsWith("hunting_mix") }) { player: Player ->
    player.levels.boost(Skill.Hunter, 3)
}

on<Consume>({ item.id.startsWith("fletching_potion") }) { player: Player ->
    player.levels.boost(Skill.Fletching, 3)
}

on<Consume>({ item.id.startsWith("super_attack") || item.id.startsWith("super_attack_mix") }) { player: Player ->
    player.levels.boost(Skill.Attack, 5, 0.15)
}

on<Consume>({ item.id.startsWith("super_strength") || item.id.startsWith("super_strength_mix") }) { player: Player ->
    player.levels.boost(Skill.Strength, 5, 0.15)
}

on<Consume>({ item.id.startsWith("super_defence") || item.id.startsWith("super_defence_mix") }) { player: Player ->
    player.levels.boost(Skill.Defence, 5, 0.15)
}

on<Consume>({ item.id.startsWith("combat_potion") || item.id.startsWith("combat_mix") }) { player: Player ->
    player.levels.boost(Skill.Attack, 3, 0.1)
    player.levels.boost(Skill.Strength, 3, 0.1)
}

on<Consume>({ item.id.startsWith("summoning_potion") }) { player: Player ->
    player.levels.boost(Skill.Summoning, 7, 0.25)
//    player.familiar.specialEnergy = (player.familiar.specialEnergy / 100) * 15
}

on<Consume>({ item.id.startsWith("relicyms_balm") || item.id.startsWith("relicyms_mix") }) { player: Player ->
    player.stop("disease")
}

on<Consume>({ item.id.startsWith("sanfew_serum") }) { player: Player ->
    player.stop("disease")
    player.cure()
    player.start("anti-poison", 600, persist = true)
    player.start("sanfew_serum", 1500, persist = true)
    Skill.all.filterNot { it == Skill.Constitution }.forEach { skill ->
        player.levels.restore(skill, 8, if (skill == Skill.Prayer && hasHolyItem(player)) 0.27 else 0.25)
    }
}

on<Consumable>({ item.id.startsWith("zamorak_brew") || item.id.startsWith("zamorak_mix") }) { player: Player ->
    val health = player.levels.get(Skill.Constitution)
    val damage = ((health / 100) * 10) + 20
    if (health - damage < 0) {
        player.message("You need more hitpoints in order to survive the effects of the zamorak brew.")
        cancel()
    }
}

on<Consume>({ item.id.startsWith("zamorak_brew") || item.id.startsWith("zamorak_mix") }) { player: Player ->
    player.levels.boost(Skill.Attack, 2, 0.2)
    player.levels.boost(Skill.Strength, 2, 0.12)
    player.levels.drain(Skill.Defence, 2, 0.1)
    val health = player.levels.get(Skill.Constitution)
    val damage = ((health / 100) * 10) + 20
    hit(player, player, damage)
}

on<Consume>({ item.id.startsWith("saradomin_brew") }) { player: Player ->
    player.levels.boost(Skill.Constitution, 20, 0.15)
    player.levels.boost(Skill.Defence, 2, 0.2)
    player.levels.drain(Skill.Attack, 2, 0.1)
    player.levels.drain(Skill.Strength, 2, 0.1)
    player.levels.drain(Skill.Magic, 2, 0.1)
    player.levels.drain(Skill.Ranged, 2, 0.1)
}

fun hasHolyItem(player: Player) = player.equipped(EquipSlot.Cape).id.startsWith("prayer_cape") || player.hasItem("holy_wrench")

on<Consume>({ item.id.startsWith("prayer_potion") || item.id.startsWith("prayer_mix") }) { player: Player ->
    player.levels.restore(Skill.Prayer, 7, if (hasHolyItem(player)) 0.27 else 0.25)
}

on<Consume>({ item.id.startsWith("antipoison") || item.id.startsWith("antipoison") }) { player: Player ->
    player.cure()
    player.start("anti-poison", 150, persist = true)
}

on<Consume>({ item.id.startsWith("super_antipoison") || item.id.startsWith("super_antipoison_mix") }) { player: Player ->
    player.cure()
    player.start("anti-poison", 600, persist = true)
}

on<Consume>({ item.id.startsWith("antipoison+") || item.id.startsWith("antipoison+_mix") }) { player: Player ->
    player.cure()
    player.start("anti-poison", 900, persist = true)
}

on<Consume>({ item.id.startsWith("antipoison++") }) { player: Player ->
    player.cure()
    player.start("anti-poison", 1200, persist = true)
}

on<Consume>({ item.id.startsWith("restore_potion") || item.id.startsWith("restore_mix") }) { player: Player ->
    player.levels.restore(Skill.Attack, 10, 0.3)
    player.levels.restore(Skill.Strength, 10, 0.3)
    player.levels.restore(Skill.Defence, 10, 0.3)
    player.levels.restore(Skill.Magic, 10, 0.3)
    player.levels.restore(Skill.Ranged, 10, 0.3)
}

on<Consume>({ item.id.startsWith("super_restore") || item.id.startsWith("super_restore_mix") }) { player: Player ->
    Skill.all.filterNot { it == Skill.Constitution }.forEach { skill ->
        player.levels.restore(skill, 8, if (skill == Skill.Prayer && hasHolyItem(player)) 0.27 else 0.25)
    }
}

on<Consume>({ item.id.startsWith("energy_potion") || item.id.startsWith("energy_mix") }) { player: Player ->
    player.runEnergy = (player.runEnergy / 100) * 10
}

on<Consume>({ item.id.startsWith("super_energy") || item.id.startsWith("super_energy_mix") }) { player: Player ->
    player.runEnergy = (player.runEnergy / 100) * 20
}

on<Consumable>({ item.id.startsWith("recover_special") }) { player: Player ->
    if (player.hasEffect("recover_special_delay")) {
        player.message("You may only use this pot once every 30 seconds.")
        cancel()
    }
}

on<Consume>({ item.id.startsWith("recover_special") }) { player: Player ->
    player.specialAttackEnergy = (MAX_SPECIAL_ATTACK / 100) * 25
    val percentage = (player.specialAttackEnergy / MAX_SPECIAL_ATTACK) * 100
    if (percentage == 0) {
        player.message("Your special attack energy is now $percentage%.")
    }
    player.start("recover_special_delay", 50, persist = true)
}