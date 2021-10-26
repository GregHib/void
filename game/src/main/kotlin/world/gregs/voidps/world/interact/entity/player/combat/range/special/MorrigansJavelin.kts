package world.gregs.voidps.world.interact.entity.player.combat.range.special

import kotlinx.coroutines.Job
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.math.max

fun isJavelin(weapon: Item?) = weapon != null && (weapon.id.startsWith("morrigans_javelin"))

on<CombatSwing>({ player -> !swung() && player.specialAttack && isJavelin(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    val ammo = player.ammo
    player.setAnimation("throw_javelin")
    player.setGraphic("${ammo}_special")
    player.shoot(id = ammo, target = target)
    val damage = player.hit(target)
    if (damage != -1) {
        target["phantom_damage"] = damage
        target["phantom"] = player
        target.start("phantom_strike")
    }
}

on<EffectStart>({ effect == "phantom_strike" }) { character: Character ->
    var first = true
    character["phantom_strike_job"] = delay(character, 3, true) {
        val damage = max(50, character["phantom_damage", 0])
        if (damage <= 0) {
            character.stop(effect)
            return@delay
        }
        hit(character["phantom", character], character, damage, "effect")
        if (character is Player) {
            if (first) {
                character.message("You start to bleed as a result of the javelin strike.")
                first = false
            } else {
                character.message("You continue to bleed as a result of the javelin strike.")
            }
        }
    }
}

on<EffectStop>({ effect == "phantom_strike" }) { character: NPC ->
    character.remove<Job>("phantom_strike_job")?.cancel()
    character.clear("phantom")
    character.clear("phantom_damage")
}
