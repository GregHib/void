package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.toTicks
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot
import java.util.concurrent.TimeUnit

fun isSpell(spell: String) = spell == "bind" || spell == "snare" || spell == "entangle"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("bind${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("bind_cast")
    player.shoot(name = "bind", target = target, endHeight = 0)
    player["spell_damage"] = when (player.spell) {
        "snare" -> 20.0
        "entangle" -> 50.0
        else -> -1.0
    }
    player["spell_experience"] = when (player.spell) {
        "snare" -> 60.5
        "entangle" -> 91.0
        else -> 30.0
    }
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isSpell(spell) }) { character: Character ->
    val protect = character.hasEffect("prayer_deflect_magic") || character.hasEffect("prayer_protect_from_magic")
    val millis = when (spell) {
        "snare" -> 10000
        "entangle" -> 15000
        else -> 5000
    }
    val duration = TimeUnit.MILLISECONDS.toTicks(if (protect) millis / 2 else millis)
    if (character.hasEffect("freeze")) {
        (source as? Player)?.message("Your target is already held by a magical force.")
    } else if (character.hasEffect("bind_immunity")) {
        (source as? Player)?.message("The target is currently immune to that spell.")
    } else {
        character.start("freeze", duration)
    }
}