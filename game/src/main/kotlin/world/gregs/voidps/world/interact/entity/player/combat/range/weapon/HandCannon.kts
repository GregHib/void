package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.delay
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
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.math.floor
import kotlin.random.Random
import kotlin.random.nextInt

fun isHandCannon(item: Item?) = item != null && item.name == "hand_cannon"

on<HitDamageModifier>({ type == "range" && special && isHandCannon(weapon) }, Priority.HIGH) { _: Player ->
    damage = floor(damage * Random.nextDouble(0.3, 2.0))
}

on<Registered>({ isHandCannon(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateAttackRange(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isHandCannon(item) }) { player: Player ->
    updateAttackRange(player, item)
}

fun updateAttackRange(player: Player, weapon: Item) {
    player["attack_range"] = 9
    player["attack_speed"] = 6
    player.weapon = weapon
}

on<CombatSwing>({ player -> isHandCannon(player.weapon) }, Priority.HIGH) { player: Player ->
    val ammo = player.equipped(EquipSlot.Ammo)
    val weapon = player.weapon
    if (weapon.def.ammo?.contains(ammo.name) != true) {
        player.message("You can't use that ammo with your bow.")
        delay = -1
        return@on
    }

    if (!player.equipment.remove(ammo.name, if (player.specialAttack) 2 else 1)) {
        player.message("There is no ammo left in your quiver.")
        delay = -1
        return@on
    }

    player.ammo = ammo.name
}

on<CombatSwing>({ player -> !swung() && isHandCannon(player.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("hand_cannon_shoot")
    player.setGraphic("hand_cannon_shoot")
    player.shoot(name = player.ammo, target = target, delay = 40, height = 21, endHeight = target.height, curve = 8)
    player.hit(target)
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
    explode(player, 0.005)
}

on<CombatSwing>({ player -> !swung() && player.specialAttack && isHandCannon(player.weapon) }, Priority.HIGHISH) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("hand_cannon_shoot")
    player.setGraphic("hand_cannon_shoot")
    player.shoot(name = player.ammo, target = target, delay = 40, height = 21, endHeight = target.height, curve = 8)
    player.hit(target)
    delay(player, 2) {
        player.setAnimation("hand_cannon_special")
        player.setGraphic("hand_cannon_special")
        player.shoot(name = player.ammo, target = target, delay = 40, height = 21, endHeight = target.height, curve = 8)
        player.hit(target, delay = if (player.attackType == "rapid") 1 else 2)
    }
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
    explode(player, 0.05)
}

fun explode(player: Player, chance: Double) {
    if (Random.nextDouble() >= chance) {
        return
    }
    player.setAnimation("hand_cannon_explode")
    player.setGraphic("hand_cannon_explode")
    player.equipment.remove(EquipSlot.Weapon.index, "hand_cannon")
    player.weapon = Item.EMPTY
    player.hit(Random.nextInt(10..160))
    player.message("Your hand cannon explodes!")
}