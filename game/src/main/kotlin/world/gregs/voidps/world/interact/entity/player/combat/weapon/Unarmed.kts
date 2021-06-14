package world.gregs.voidps.world.interact.entity.player.combat.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit

on<Registered>({ it.equipped(EquipSlot.Weapon).isEmpty() }) { player: Player ->
    updateWeapon(player)
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && item.isEmpty() }) { player: Player ->
    updateWeapon(player)
}

fun updateWeapon(player: Player) {
    player["attack_range"] = 1
    player["combat_style"] = "melee"
    player.setCombatSwing { target ->
        player.setAnimation(if (player.attackType == "kick") "player_kick" else "player_punch")
        player.hit(target, null)
        4
    }
}