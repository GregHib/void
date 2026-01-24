package content.entity.effect.toxin

import content.entity.combat.Target
import content.entity.combat.hit.Hit
import content.entity.combat.hit.directHit
import content.skill.ranged.ammo
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.math.sign

val Character.poisoned: Boolean get() = poisonDamage > 0

val Character.antiPoison: Boolean get() = poisonDamage < 0

var Character.poisonDamage: Int
    get() = if (this is Player) get("poison", 0) else this["poison", 0]
    set(value) = if (this is Player) {
        set("poison", value)
    } else {
        this["poison"] = value
    }

fun Character.curePoison(): Boolean {
    val timers = if (this is Player) timers else softTimers
    timers.stop("poison")
    return true
}

fun Character.poison(target: Character, damage: Int) {
    if (target.antiPoison || damage < target.poisonDamage || immune(target)) {
        return
    }
    val timers = if (target is Player) target.timers else target.softTimers
    if (timers.contains("poison") || timers.start("poison")) {
        target.poisonDamage = damage
        target["poison_source"] = this
    }
}

fun Player.antiPoison(minutes: Int) = antiPoison(minutes, TimeUnit.MINUTES)

fun Player.antiPoison(duration: Int, timeUnit: TimeUnit) {
    poisonDamage = -(timeUnit.toTicks(duration) * 2)
    clear("poison_source")
    timers.startIfAbsent("poison")
}

private fun immune(character: Character) = character is NPC && character.def["immune_poison", false] || character is Player && character.equipped(EquipSlot.Shield).id == "anti_poison_totem"

class Poison : Script {

    init {
        // TODO Cure:health_orb:poison
        playerSpawn {
            if (poisonDamage != 0) {
                timers.restart("poison")
            }
        }

        npcSpawn {
            if (poisonDamage != 0) {
                softTimers.restart("poison")
            }
        }

        timerStart("poison", ::start)
        npcTimerStart("poison", ::start)
        timerTick("poison", ::tick)
        npcTimerTick("poison", ::tick)
        timerStop("poison", ::stop)
        npcTimerStop("poison", ::stop)
    }

    fun start(character: Character, restart: Boolean): Int {
        if (immune(character)) {
            return Timer.CANCEL
        }
        if (!restart) {
            (character as? Player)?.message("<green>You have been poisoned.")
        }
        return 30
    }

    fun tick(character: Character): Int {
        val damage = character.poisonDamage
        character.poisonDamage -= damage.sign * 2
        when {
            character.poisonDamage == 0 -> {
                if (damage < 0) {
                    (character as? Player)?.message("<purple>Your poison resistance has worn off.")
                }
                return Timer.CANCEL
            }
            character.poisonDamage == -2 -> (character as? Player)?.message("<purple>Your poison resistance is about to wear off.")
            damage > 0 && character.poisonDamage <= 10 -> {
                character.curePoison()
                return Timer.CANCEL
            }
            damage > 0 -> character.directHit(character["poison_source", character], Target.damageLimitModifiers(character, damage), "poison")
        }
        return Timer.CONTINUE
    }

    fun stop(character: Character, logout: Boolean) {
        character.poisonDamage = 0
        character.clear("poison_source")
    }

    init {
        combatAttack(handler = ::attack)
    }

    fun attack(source: Character, attack: CombatAttack) {
        val (target, damage, type, weapon) = attack
        if (damage <= 0 || source !is Player || !poisoned(weapon.id) && !poisoned(source.ammo)) {
            return
        }
        val poison = 20 + weapon.id.count { it == '+' } * 10
        if (type == "range" && random.nextDouble() < 0.125) {
            source.poison(target, if (source.ammo == "emerald_bolts_e") 50 else poison)
        } else if (Hit.meleeType(type) && random.nextDouble() < 0.25) {
            source.poison(target, poison + 20)
        }
    }

    fun poisoned(id: String) = id.endsWith("_p") || id.endsWith("_p+") || id.endsWith("_p++") || id == "emerald_bolts_e"

}
