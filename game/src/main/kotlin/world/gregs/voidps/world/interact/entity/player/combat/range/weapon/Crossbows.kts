package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.random.Random

on<Registered>({ isCrossbow(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateAttackRange(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isCrossbow(item) }) { player: Player ->
    updateAttackRange(player, item)
}

fun updateAttackRange(player: Player, weapon: Item) {
    player["attack_range"] = weapon.def.getOrNull("attack_range") as? Int ?: 7
    player.weapon = weapon
}

fun isCrossbow(item: Item) = item.name.endsWith("crossbow")

on<CombatSwing>({ player -> !swung() && isCrossbow(player.weapon) }, Priority.LOW) { player: Player ->
    val ammo = player.ammo
    if (ammo.endsWith("brutal")) {
        player.setGraphic("brutal_shoot")
    }
    player.setAnimation(if (player.weapon.name == "karils_crossbow") "karils_crossbow_shoot" else "crossbow_shoot")
    val bolt = when {
        ammo == "barbed_bolts" || ammo == "bone_bolts" -> ammo
        ammo.endsWith("brutal") -> "brutal_bolt"
        else -> "crossbow_bolt"
    }
    handleCrossbowEffects(player, ammo, target)
    player.shoot(name = bolt, target = target, delay = 40, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
    val speed = player.weapon.def.getOrNull("attack_speed") as? Int ?: 4
    delay = if (player.attackType == "rapid") speed - 1 else speed
}

fun handleCrossbowEffects(player: Player, ammo: String, target: Character) {
    when (ammo) {
        "opal_bolts_e" -> checkEffect(player, target, "lucky_lightning", 0.05)
        "jade_bolts_e" -> checkEffect(player, target, "earths_fury", 0.05)
        "pearl_bolts_e" -> checkEffect(player, target, "sea_curse", 0.06)
        "topaz_bolts_e" -> checkEffect(player, target, "down_to_earth", 0.04)
        "sapphire_bolts_e" -> checkEffect(player, target, "clear_mind", 0.05)
        "emerald_bolts_e" -> checkEffect(player, target, "magical_poison", if (target is Player) 0.54 else 0.55)
        "ruby_bolts_e" -> checkEffect(player, target, "blood_forfeit", if (target is Player) 0.11 else 0.06)
        "diamond_bolts_e" -> checkEffect(player, target, "armour_piercing", 0.1)
        "dragon_bolts_e" -> checkEffect(player, target, "dragons_breath", 0.06)
        "onyx_bolts_e" -> checkEffect(player, target, "life_leech", if (target is Player) 0.1 else 0.11)
    }
}

fun checkEffect(player: Player, target: Character, effect: String, chance: Double) {
    if (Random.nextDouble() < chance) {
        target.start(effect, 1)
        target.setGraphic(effect)
        player.playSound(effect, delay = 40)
    }
}