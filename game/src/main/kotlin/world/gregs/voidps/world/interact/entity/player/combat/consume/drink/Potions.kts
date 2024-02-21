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

consume("attack_potion*", "attack_mix*") { player ->
    player.levels.boost(Skill.Attack, 3, 0.1)
}

consume("strength_potion*", "strength_mix*") { player ->
    player.levels.boost(Skill.Strength, 3, 0.1)
}

consume("defence_potion*", "defence_mix*") { player ->
    player.levels.boost(Skill.Defence, 3, 0.1)
}

consume("magic_essence*", "magic_essence_mix*") { player ->
    player.levels.boost(Skill.Magic, 3)
}

consume("agility_potion*", "agility_mix*") { player ->
    player.levels.boost(Skill.Agility, 3)
}

consume("fishing_potion*", "fishing_mix*") { player ->
    player.levels.boost(Skill.Fishing, 3)
}

consume("crafting_potion*") { player ->
    player.levels.boost(Skill.Crafting, 3)
}

consume("hunter_potion*", "hunting_mix*") { player ->
    player.levels.boost(Skill.Hunter, 3)
}

consume("fletching_potion*") { player ->
    player.levels.boost(Skill.Fletching, 3)
}

consume("super_attack*", "super_attack_mix*") { player ->
    player.levels.boost(Skill.Attack, 5, 0.15)
}

consume("super_strength*", "super_strength_mix*") { player ->
    player.levels.boost(Skill.Strength, 5, 0.15)
}

consume("super_defence*", "super_defence_mix*") { player ->
    player.levels.boost(Skill.Defence, 5, 0.15)
}

consume("super_magic_potion*", "super_magic_mix*") { player ->
    player.levels.boost(Skill.Magic, 5, 0.15)
}

consume("super_ranging_potion*", "super_ranging_mix*") { player ->
    player.levels.boost(Skill.Ranged, 4, 0.10)
}

consume("combat_potion*", "combat_mix*") { player ->
    player.levels.boost(Skill.Attack, 3, 0.1)
    player.levels.boost(Skill.Strength, 3, 0.1)
}

consume("summoning_potion*") { player ->
    player.levels.boost(Skill.Summoning, 7, 0.25)
//    player.familiar.specialEnergy = (player.familiar.specialEnergy / 100) * 15
}

consume("relicyms_balm*", "relicyms_mix*") { player ->
    player.cureDisease()
}

consume("sanfew_serum*") { player ->
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

consume("zamorak_brew*", "zamorak_mix*") { player ->
    player.levels.boost(Skill.Attack, 2, 0.2)
    player.levels.boost(Skill.Strength, 2, 0.12)
    player.levels.drain(Skill.Defence, 2, 0.1)
    val health = player.levels.get(Skill.Constitution)
    val damage = ((health / 100) * 10) + 20
    player.directHit(damage)
}

consume("saradomin_brew*") { player ->
    player.levels.boost(Skill.Constitution, 20, 0.15)
    player.levels.boost(Skill.Defence, 2, 0.2)
    player.levels.drain(Skill.Attack, 2, 0.1)
    player.levels.drain(Skill.Strength, 2, 0.1)
    player.levels.drain(Skill.Magic, 2, 0.1)
    player.levels.drain(Skill.Ranged, 2, 0.1)
}

fun hasHolyItem(player: Player) = player.equipped(EquipSlot.Cape).id.startsWith("prayer_cape") || player.holdsItem("holy_wrench")

consume("prayer_potion*", "prayer_mix*") { player ->
    player.levels.restore(Skill.Prayer, 7, if (hasHolyItem(player)) 0.27 else 0.25)
}

consume("antipoison*", "antipoison*") { player ->
    player.antiPoison(90, TimeUnit.SECONDS)
}

consume("super_antipoison*", "super_antipoison_mix*") { player ->
    player.antiPoison(6)
}

consume("antipoison+*", "antipoison+_mix*") { player ->
    player.antiPoison(9)
}

consume("antipoison++*") { player ->
    player.antiPoison(12)
}

consume("restore_potion*", "restore_mix*") { player ->
    player.levels.restore(Skill.Attack, 10, 0.3)
    player.levels.restore(Skill.Strength, 10, 0.3)
    player.levels.restore(Skill.Defence, 10, 0.3)
    player.levels.restore(Skill.Magic, 10, 0.3)
    player.levels.restore(Skill.Ranged, 10, 0.3)
}

consume("super_restore*", "super_restore_mix*") { player ->
    Skill.all.filterNot { it == Skill.Constitution }.forEach { skill ->
        player.levels.restore(skill, 8, if (skill == Skill.Prayer && hasHolyItem(player)) 0.27 else 0.25)
    }
}

consume("energy_potion*", "energy_mix*") { player ->
    player.runEnergy = (player.runEnergy / 100) * 10
}

consume("super_energy*", "super_energy_mix*") { player ->
    player.runEnergy = (player.runEnergy / 100) * 20
}