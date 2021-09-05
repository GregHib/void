package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.toTicks
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import java.util.concurrent.TimeUnit
import kotlin.math.floor

fun isStaffOfLight(item: Item?) = item != null && item.name.startsWith("staff_of_light")

on<Registered>({ isStaffOfLight(it.equipped(EquipSlot.Weapon)) }) { player: Player ->
    updateWeapon(player, player.equipped(EquipSlot.Weapon))
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isStaffOfLight(item) }) { player: Player ->
    updateWeapon(player, item)
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isStaffOfLight(oldItem) }) { player: Player ->
    player.stop("power_of_light")
}

fun updateWeapon(player: Player, weapon: Item) {
    player["attack_range"] = 1
    player.weapon = weapon
}

on<CombatSwing>({ !swung() && isStaffOfLight(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("staff_of_light_${player.attackType}")
    player.hit(target)
    delay = 6
}

on<CombatHit>({ isStaffOfLight(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("staff_of_light_block")
}

on<CombatHit>({ it.hasEffect("power_of_light") }, Priority.LOW) { player: Player ->
    player.setGraphic("power_of_light_hit")
}

// Special attack

on<HitDamageModifier>({ type == "melee" && target != null && target.hasEffect("power_of_light") }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 0.5)
}

on<VariableSet>({ key == "special_attack" && to == true && isStaffOfLight(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        return@on
    }
    player.setAnimation("power_of_light")
    player.setGraphic("power_of_light")
    player.start("power_of_light", ticks = TimeUnit.MINUTES.toTicks(1))
    player.specialAttack = false
}

on<EffectStop>({ effect == "power_of_light" }) { player: Player ->
    player.message(Colour.Red { "The power of the light fades. Your resistance to melee attacks returns to normal." })
}