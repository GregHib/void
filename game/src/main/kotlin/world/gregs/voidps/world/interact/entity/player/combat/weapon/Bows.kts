package world.gregs.voidps.world.interact.entity.player.combat.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.ammo
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.height
import world.gregs.voidps.world.interact.entity.combat.rangeHit
import world.gregs.voidps.world.interact.entity.proj.shoot

on<Registered>({ isBow(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isBow(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun isBow(item: Item) = item.name.endsWith("bow") && !item.name.endsWith("crossbow")

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = weapon.def.getOrNull("attack_range") as? Int ?: 7
    player["combat_style"] = "ranged"
    player.setCombatSwing { target ->
        val ammo = player.equipped(EquipSlot.Ammo)
        if (ammo.isEmpty()) {
            player.message("There is no ammo left in your quiver.")
            return@setCombatSwing -1
        }
        if (weapon.def.ammo?.contains(ammo.name) != true) {
            player.message("You can't use that ammo with your bow.")
            return@setCombatSwing -1
        }
        player.setAnimation("bow_shoot")
        player.setGraphic("${ammo.name}_shoot", height = 100)
        player.shoot(name = ammo.name, target = target, delay = 40, height = 43, endHeight = target.height, curve = 8)
        rangeHit(player, target)
        val speed = weapon.def.getOrNull("attack_speed") as? Int ?: 4
        if (player.attackType == "rapid") speed - 1 else speed
    }
}