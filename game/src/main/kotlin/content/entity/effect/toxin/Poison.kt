package content.entity.effect.toxin

import content.entity.combat.hit.Hit
import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.math.sign

val Character.poisoned: Boolean get() = poisonCounter > 0

val Character.antiPoison: Boolean get() = poisonCounter < 0

var Character.poisonCounter: Int
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
    if (damage < target["poison_damage", 0]) {
        return
    }
    val timers = if (target is Player) target.timers else target.softTimers
    if (timers.contains("poison") || timers.start("poison")) {
        target.poisonCounter = TimeUnit.SECONDS.toTicks(18) / 30
        target["poison_damage"] = damage
        target["poison_source"] = this
    }
}

fun Player.antiPoison(minutes: Int) = antiPoison(minutes, TimeUnit.MINUTES)

fun Player.antiPoison(duration: Int, timeUnit: TimeUnit) {
    poisonCounter = -(timeUnit.toTicks(duration) / 30)
    clear("poison_damage")
    clear("poison_source")
    timers.startIfAbsent("poison")
}

class Poison : Script {

    init {
        playerSpawn {
            if (poisonCounter != 0) {
                timers.restart("poison")
            }
        }

        npcSpawn {
            if (poisonCounter != 0) {
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
        if (character.antiPoison || immune(character)) {
            return Timer.CANCEL
        }
        if (!restart && character.poisonCounter == 0) {
            (character as? Player)?.message("<green>You have been poisoned.")
            damage(character)
        }
        return 30
    }

    fun tick(character: Character): Int {
        val poisoned = character.poisoned
        character.poisonCounter -= character.poisonCounter.sign
        when {
            character.poisonCounter == 0 -> {
                if (!poisoned) {
                    (character as? Player)?.message("<purple>Your poison resistance has worn off.")
                }
                return Timer.CANCEL
            }
            character.poisonCounter == -1 -> (character as? Player)?.message("<purple>Your poison resistance is about to wear off.")
            poisoned -> damage(character)
        }
        return Timer.CONTINUE
    }

    fun stop(character: Character, logout: Boolean) {
        character.poisonCounter = 0
        character.clear("poison_damage")
        character.clear("poison_source")
    }

    init {
        combatAttack(handler = ::attack)
        npcCombatAttack(handler = ::attack)
    }

    fun attack(source: Character, attack: CombatAttack) {
        val (target, damage, type, weapon) = attack
        if (damage <= 0 || !poisonous(source, weapon)) {
            return
        }
        val poison = 20 + weapon.id.count { it == '+' } * 10
        if (type == "range" && random.nextDouble() < 0.125) {
            source.poison(target, if (weapon.id == "emerald_bolts_e") 50 else poison)
        } else if (Hit.meleeType(type) && random.nextDouble() < 0.25) {
            source.poison(target, poison + 20)
        }
    }

    fun immune(character: Character) = character is NPC && character.def["immune_poison", false] || character is Player && character.equipped(EquipSlot.Shield).id == "anti_poison_totem"

    fun damage(character: Character) {
        val damage = character["poison_damage", 0]
        if (damage <= 10) {
            character.curePoison()
            return
        }
        character["poison_damage"] = damage - 2
        val source = character["poison_source", character]
        character.directHit(source, damage, "poison")
    }

    fun isPoisoned(id: String) = id.endsWith("_p") || id.endsWith("_p+") || id.endsWith("_p++") || id == "emerald_bolts_e"

    fun poisonous(source: Character, weapon: Item) = source is Player && isPoisoned(weapon.id)
}
