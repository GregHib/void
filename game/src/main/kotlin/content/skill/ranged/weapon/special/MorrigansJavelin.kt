package content.skill.ranged.weapon.special

import content.entity.combat.hit.directHit
import content.entity.combat.hit.hit
import content.entity.proj.shoot
import content.skill.ranged.ammo
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.*

class MorrigansJavelin : Script {

    init {
        specialAttack("phantom_strike") { target, id ->
            val ammo = ammo
            anim("throw_javelin")
            gfx("${ammo}_special")
            val time = shoot(id = ammo, target = target)
            val damage = hit(target, delay = time)
            if (damage != -1) {
                target["phantom_damage"] = damage
                target["phantom"] = this
                target["phantom_first"] = "start"
                target.softTimers.start(id)
            }
        }
        timerStart("phantom_strike", ::start)
        npcTimerStart("phantom_strike", ::start)
        timerTick("phantom_strike", ::tick)
        npcTimerTick("phantom_strike", ::tick)
        timerStop("phantom_strike", ::stop)
        npcTimerStop("phantom_strike", ::stop)
    }

    fun start(character: Character, restart: Boolean): Int = 3

    fun tick(character: Character): Int {
        val remaining = character["phantom_damage", 0]
        val damage = remaining.coerceAtMost(50)
        if (remaining - damage <= 0) {
            return Timer.CANCEL
        }
        character["phantom_damage"] = remaining - damage
        val source = character["phantom", character]
        character.directHit(source, damage, "effect")
        (character as? Player)?.message("You ${character.remove("phantom_first") ?: "continue"} to bleed as a result of the javelin strike.")
        return Timer.CONTINUE
    }

    fun stop(character: Character, logout: Boolean) {
        character.clear("phantom")
        character.clear("phantom_damage")
        character.clear("phantom_first")
    }
}
