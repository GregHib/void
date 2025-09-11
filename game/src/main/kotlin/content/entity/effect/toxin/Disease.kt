package content.entity.effect.toxin

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.characterSpawn
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerStop
import world.gregs.voidps.engine.timer.characterTimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit
import kotlin.math.sign

val Character.diseased: Boolean get() = diseaseCounter > 0

val Character.antiDisease: Boolean get() = diseaseCounter < 0

var Character.diseaseCounter: Int
    get() = if (this is Player) get("disease", 0) else this["disease", 0]
    set(value) = if (this is Player) {
        set("disease", value)
    } else {
        this["disease"] = value
    }

fun Character.cureDisease(): Boolean {
    val timers = if (this is Player) timers else softTimers
    timers.stop("disease")
    return true
}

fun Character.disease(target: Character, damage: Int) {
    if (damage < target["disease_damage", 0]) {
        return
    }
    val timers = if (target is Player) target.timers else target.softTimers
    if (timers.contains("disease") || timers.start("disease")) {
        target.diseaseCounter = TimeUnit.SECONDS.toTicks(18) / 30
        target["disease_damage"] = damage
        target["disease_source"] = this
    }
}

fun Player.antiDisease(minutes: Int) = antiDisease(minutes, TimeUnit.MINUTES)

fun Player.antiDisease(duration: Int, timeUnit: TimeUnit) {
    diseaseCounter = -(timeUnit.toTicks(duration) / 30)
    clear("disease_damage")
    clear("disease_source")
    timers.startIfAbsent("disease")
}

@Script
class Disease {

    init {
        characterSpawn { character ->
            if (character.diseaseCounter != 0) {
                val timers = if (character is Player) character.timers else character.softTimers
                timers.restart("disease")
            }
        }

        characterTimerStart("disease") { character ->
            if (character.antiDisease || immune(character)) {
                cancel()
                return@characterTimerStart
            }
            if (!restart && character.diseaseCounter == 0) {
                (character as? Player)?.message("You have been diseased.")
                damage(character)
            }
            interval = 30
        }

        characterTimerTick("disease") { character ->
            val diseased = character.diseased
            character.diseaseCounter -= character.diseaseCounter.sign
            when {
                character.diseaseCounter == 0 -> {
                    if (!diseased) {
                        (character as? Player)?.message("Your disease resistance has worn off.")
                    }
                    cancel()
                    return@characterTimerTick
                }
                character.diseaseCounter == -1 -> (character as? Player)?.message("Your disease resistance is about to wear off.")
                diseased -> damage(character)
            }
        }

        characterTimerStop("disease") { character ->
            character.diseaseCounter = 0
            character.clear("disease_damage")
            character.clear("disease_source")
        }

        adminCommand("disease [damage]", "toggle hitting player with disease") {
            val damage = content.toIntOrNull() ?: 100
            if (player.diseased || damage < 0) {
                player.cureDisease()
            } else {
                player.disease(player, damage)
            }
        }
    }

    fun immune(character: Character) = character is NPC &&
        character.def["immune_disease", false] ||
        character is Player &&
        character.equipped(EquipSlot.Hands).id == "inoculation_brace"

    fun damage(character: Character) {
        val damage = character["disease_damage", 0]
        if (damage <= 10) {
            character.cureDisease()
            return
        }
        character["disease_damage"] = damage - 2
        val source = character["disease_source", character]
        character.directHit(source, damage, "disease")
    }
}
