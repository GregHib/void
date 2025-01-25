package content.skill.ranged.weapon.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerTick
import world.gregs.voidps.engine.timer.npcTimerStop
import content.entity.combat.hit.directHit
import content.entity.combat.hit.hit
import content.skill.ranged.ammo
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot

specialAttack("phantom_strike") { player ->
    val ammo = player.ammo
    player.anim("throw_javelin")
    player.gfx("${ammo}_special")
    val time = player.shoot(id = ammo, target = target)
    val damage = player.hit(target, delay = time)
    if (damage != -1) {
        target["phantom_damage"] = damage
        target["phantom"] = player
        target["phantom_first"] = "start"
        target.softTimers.start(id)
    }
}

characterTimerStart("phantom_strike") {
    interval = 3
}

characterTimerTick("phantom_strike") { character ->
    val remaining = character["phantom_damage", 0]
    val damage = remaining.coerceAtMost(50)
    if (remaining - damage <= 0) {
        cancel()
        return@characterTimerTick
    }
    character["phantom_damage"] = remaining - damage
    val source = character["phantom", character]
    character.directHit(source, damage, "effect")
    (character as? Player)?.message("You ${character.remove("phantom_first") ?: "continue"} to bleed as a result of the javelin strike.")
}

npcTimerStop("phantom_strike") { npc ->
    npc.clear("phantom")
    npc.clear("phantom_damage")
    npc.clear("phantom_first")
}
