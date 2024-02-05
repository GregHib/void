package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.player.combat.consume.Consumable
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy
import world.gregs.voidps.world.interact.entity.player.toxin.antiDisease
import world.gregs.voidps.world.interact.entity.player.toxin.antiPoison
import world.gregs.voidps.world.interact.entity.player.toxin.cureDisease
import java.util.concurrent.TimeUnit

consume({ item.id.startsWith("attack_potion") || item.id.startsWith("attack_mix") }) { player: Player ->
    player.levels.boost(Skill.Attack, 3, 0.1)
}

consume({ item.id.startsWith("strength_potion") || item.id.startsWith("strength_mix") }) { player: Player ->
    player.levels.boost(Skill.Strength, 3, 0.1)
}

consume({ item.id.startsWith("defence_potion") || item.id.startsWith("defence_mix") }) { player: Player ->
    player.levels.boost(Skill.Defence, 3, 0.1)
}

consume({ item.id.startsWith("magic_essence") || item.id.startsWith("magic_essence_mix") }) { player: Player ->
    player.levels.boost(Skill.Magic, 3)
}

consume({ item.id.startsWith("agility_potion") || item.id.startsWith("agility_mix") }) { player: Player ->
    player.levels.boost(Skill.Agility, 3)
}

consume({ item.id.startsWith("fishing_potion") || item.id.startsWith("fishing_mix") }) { player: Player ->
    player.levels.boost(Skill.Fishing, 3)
}

consume({ item.id.startsWith("crafting_potion") }) { player: Player ->
    player.levels.boost(Skill.Crafting, 3)
}

consume({ item.id.startsWith("hunter_potion") || item.id.startsWith("hunting_mix") }) { player: Player ->
    player.levels.boost(Skill.Hunter, 3)
}

consume({ item.id.startsWith("fletching_potion") }) { player: Player ->
    player.levels.boost(Skill.Fletching, 3)
}

consume({ item.id.startsWith("super_attack") || item.id.startsWith("super_attack_mix") }) { player: Player ->
    player.levels.boost(Skill.Attack, 5, 0.15)
}

consume({ item.id.startsWith("super_strength") || item.id.startsWith("super_strength_mix") }) { player: Player ->
    player.levels.boost(Skill.Strength, 5, 0.15)
}

consume({ item.id.startsWith("super_defence") || item.id.startsWith("super_defence_mix") }) { player: Player ->
    player.levels.boost(Skill.Defence, 5, 0.15)
}

consume({ item.id.startsWith("super_magic_potion") || item.id.startsWith("super_magic_mix") }) { player: Player ->
    player.levels.boost(Skill.Magic, 5, 0.15)
}

consume({ item.id.startsWith("super_ranging_potion") || item.id.startsWith("super_ranging_mix") }) { player: Player ->
    player.levels.boost(Skill.Ranged, 4, 0.10)
}

consume({ item.id.startsWith("combat_potion") || item.id.startsWith("combat_mix") }) { player: Player ->
    player.levels.boost(Skill.Attack, 3, 0.1)
    player.levels.boost(Skill.Strength, 3, 0.1)
}

consume({ item.id.startsWith("summoning_potion") }) { player: Player ->
    player.levels.boost(Skill.Summoning, 7, 0.25)
//    player.familiar.specialEnergy = (player.familiar.specialEnergy / 100) * 15
}

consume({ item.id.startsWith("relicyms_balm") || item.id.startsWith("relicyms_mix") }) { player: Player ->
    player.cureDisease()
}

consume({ item.id.startsWith("sanfew_serum") }) { player: Player ->
    player.antiPoison(6)
    player.antiDisease(15)
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

consume({ item.id.startsWith("zamorak_brew") || item.id.startsWith("zamorak_mix") }) { player: Player ->
    player.levels.boost(Skill.Attack, 2, 0.2)
    player.levels.boost(Skill.Strength, 2, 0.12)
    player.levels.drain(Skill.Defence, 2, 0.1)
    val health = player.levels.get(Skill.Constitution)
    val damage = ((health / 100) * 10) + 20
    player.directHit(damage)
}

consume({ item.id.startsWith("saradomin_brew") }) { player: Player ->
    player.levels.boost(Skill.Constitution, 20, 0.15)
    player.levels.boost(Skill.Defence, 2, 0.2)
    player.levels.drain(Skill.Attack, 2, 0.1)
    player.levels.drain(Skill.Strength, 2, 0.1)
    player.levels.drain(Skill.Magic, 2, 0.1)
    player.levels.drain(Skill.Ranged, 2, 0.1)
}

fun hasHolyItem(player: Player) = player.equipped(EquipSlot.Cape).id.startsWith("prayer_cape") || player.holdsItem("holy_wrench")

consume({ item.id.startsWith("prayer_potion") || item.id.startsWith("prayer_mix") }) { player: Player ->
    player.levels.restore(Skill.Prayer, 7, if (hasHolyItem(player)) 0.27 else 0.25)
}

consume({ item.id.startsWith("antipoison") || item.id.startsWith("antipoison") }) { player: Player ->
    player.antiPoison(90, TimeUnit.SECONDS)
}

consume({ item.id.startsWith("super_antipoison") || item.id.startsWith("super_antipoison_mix") }) { player: Player ->
    player.antiPoison(6)
}

consume({ item.id.startsWith("antipoison+") || item.id.startsWith("antipoison+_mix") }) { player: Player ->
    player.antiPoison(9)
}

consume({ item.id.startsWith("antipoison++") }) { player: Player ->
    player.antiPoison(12)
}

consume({ item.id.startsWith("restore_potion") || item.id.startsWith("restore_mix") }) { player: Player ->
    player.levels.restore(Skill.Attack, 10, 0.3)
    player.levels.restore(Skill.Strength, 10, 0.3)
    player.levels.restore(Skill.Defence, 10, 0.3)
    player.levels.restore(Skill.Magic, 10, 0.3)
    player.levels.restore(Skill.Ranged, 10, 0.3)
}

consume({ item.id.startsWith("super_restore") || item.id.startsWith("super_restore_mix") }) { player: Player ->
    Skill.all.filterNot { it == Skill.Constitution }.forEach { skill ->
        player.levels.restore(skill, 8, if (skill == Skill.Prayer && hasHolyItem(player)) 0.27 else 0.25)
    }
}

consume({ item.id.startsWith("energy_potion") || item.id.startsWith("energy_mix") }) { player: Player ->
    player.runEnergy = (player.runEnergy / 100) * 10
}

consume({ item.id.startsWith("super_energy") || item.id.startsWith("super_energy_mix") }) { player: Player ->
    player.runEnergy = (player.runEnergy / 100) * 20
}