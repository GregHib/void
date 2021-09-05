package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import kotlin.math.floor

fun isGreatAxe(item: Item?) = item != null && (item.name.startsWith("dharoks_greataxe") || item.name == "balmung")

on<Registered>({ isGreatAxe(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isGreatAxe(item) }) { player: Player ->
    updateWeapon(player, item)
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = 1
    player.weapon = weapon
}

on<CombatSwing>({ !swung() && isGreatAxe(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("dharoks_greataxe_${
        when (player.attackType) {
            "smash" -> "smash"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 7
}

on<CombatHit>({ isGreatAxe(it.weapon) }) { player: Player ->
    player.setAnimation("dharoks_greataxe_block")
}

on<HitDamageModifier>({ player -> type == "melee" && weapon?.name?.startsWith("dharoks_greataxe") == true && player.hasEffect("dharoks_set") }, Priority.LOW) { player: Player ->
    val lost = player.levels.getMax(Skill.Constitution) - player.levels.get(Skill.Constitution) / 100.0
    val max = player.levels.getMax(Skill.Constitution) / 100.0
    damage = floor(damage * (1 + lost * max))
}