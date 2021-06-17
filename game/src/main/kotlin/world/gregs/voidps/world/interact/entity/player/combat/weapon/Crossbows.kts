package world.gregs.voidps.world.interact.entity.player.combat.weapon

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

on<Registered>({ isCrossbow(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isCrossbow(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = weapon.def.getOrNull("attack_range") as? Int ?: 7
    player["weapon"] = weapon
}

fun isCrossbow(item: Item) = item.name.endsWith("crossbow")

on<CombatSwing>({ player -> isCrossbow(player.weapon) }, Priority.HIGH) { player: Player ->
    val required = player["required_ammo", 1]
    val ammo = player.equipped(EquipSlot.Ammo)
    if (!player.equipment.remove(ammo.name, required)) {
        player.message("There is no ammo left in your quiver.")
        player.action.cancel(ActionType.Combat)
        return@on
    }

    val weapon = player.weapon
    if (weapon.def.ammo?.contains(ammo.name) != true) {
        player.message("You can't use that ammo with your bow.")
        player.action.cancel(ActionType.Combat)
        return@on
    }
}

on<CombatSwing>({ player -> !swung() && isCrossbow(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.equipped(EquipSlot.Ammo)
    if (ammo.name.endsWith("brutal")) {
        player.setGraphic("brutal_shoot")
    }
    player.setAnimation("crossbow_shoot")
    val bolt = when {
        ammo.name == "barbed_bolts" || ammo.name == "bone_bolts" -> ammo.name
        ammo.name.endsWith("brutal") -> "brutal_bolt"
        else -> "crossbow_bolt"
    }
    player.shoot(name = bolt, target = target, delay = 40, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
    val speed = player.weapon.def.getOrNull("attack_speed") as? Int ?: 4
    delay = if (player.attackType == "rapid") speed - 1 else speed
}