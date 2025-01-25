package content.entity.player.effect

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.toTicks
import content.entity.player.equip.Equipment
import content.entity.combat.Target
import content.skill.prayer.protectMagic
import java.util.concurrent.TimeUnit

object Dragonfire {

    /**
     * Calculate the dragonfire max hit
     * @param special for hitting extra damage if accuracy roll was low or is kbd special dragon breath
     */
    fun maxHit(source: Character, target: Character, special: Boolean): Int {
        if (source is Player && target is NPC && Target.isDragon(target) && !Target.isMetalDragon(target)) {
            return -1
        }
        var type = type(source)
        if (source is Player && target is Player) {
            if (Equipment.antiDragonShield(target)) {
                return if (target.antifire) 10 else if (target.superAntifire) 0 else 30
            }
            return if (target.antifire) 200 else if (target.superAntifire) 0 else 250
        } else if (source is Player) {
            type = "chromatic"
        } else if (target is Player) {
            target.message(if (special) "You're horribly burnt by the dragon fire!" else "You manage to resist some of the dragon fire!")
        }
        return maxHit(
            type = type,
            special = special,
            shield = Equipment.antiDragonShield(target),
            protection = target.protectMagic(),
            potion = when {
                target.superAntifire -> 2
                target.antifire -> 1
                else -> 0
            }
        )
    }

    internal fun maxHit(type: String, special: Boolean, shield: Boolean, protection: Boolean = false, potion: Int = 0): Int {
        val normalFire = type != "king_black_dragon" || !special
        if (potion == 2 && normalFire) {
            return 0
        }

        var max = when {
            type == "king_black_dragon" && !special -> 650
            type == "elvarg" -> when {
                shield && protection && potion == 1 -> 340
                shield && !protection && potion == 0 -> 100
                shield -> 220
                else -> 700
            }
            else -> if (special) 500 else 300
        }
        when {
            type == "king_black_dragon" && (shield || protection) -> {
                max = if (shield) 150 else 200
                if (special) max -= 50
            }
            type == "elvarg" && protection -> max -= 150
            type == "chromatic" && (shield || protection) || type == "metallic" && shield ->
                return if (potion == 1) 0 else if (shield) 50 else 100
        }
        if (potion == 1 && normalFire) {
            max -= 150
        }
        return max
    }

    /**
     * @return chromatic, metallic, king_black_dragon or elvarg
     */
    private fun type(character: Character): String {
        if (character !is NPC || !Target.isDragon(character)) {
            return ""
        }
        return when {
            character.id == "king_black_dragon" || character.id == "elvarg" -> character.id
            Target.isMetalDragon(character) -> "metallic"
            else -> "chromatic"
        }
    }
}

val Character.antifire: Boolean
    get() = get("antifire", 0) > 0

val Character.superAntifire: Boolean
    get() = get("super_antifire", 0) > 0

fun Player.antifire(minutes: Int) {
    set("antifire", TimeUnit.MINUTES.toTicks(minutes) / 30)
    timers.start("fire_resistance")
}

fun Player.superAntifire(minutes: Int) {
    set("super_antifire", TimeUnit.MINUTES.toTicks(minutes) / 20)
    timers.start("fire_immunity")
}
