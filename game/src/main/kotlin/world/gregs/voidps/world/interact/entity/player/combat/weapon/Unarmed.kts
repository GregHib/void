package world.gregs.voidps.world.interact.entity.player.combat.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.attackStyle
import world.gregs.voidps.world.interact.entity.combat.hit
import kotlin.random.Random

on<Registered>({ it.equipped(EquipSlot.Weapon).isEmpty() }) { player: Player ->
    updateWeapon(player)
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && item.isEmpty() }) { player: Player ->
    updateWeapon(player)
}

fun updateWeapon(player: Player) {
    player["attack_range"] = 1
    player["attack_type"] = "melee"
    player.setCombatSwing { target ->
        player.setAnimation(if (player.attackStyle == 1) "player_kick" else "player_punch")
        hit(player, target, Random.nextInt(100 + 1).coerceAtLeast(0), Hit.Mark.Melee)
        4
    }
}