package content.entity.effect.toxin

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.flagHits
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
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
    if (target.antiDisease || damage < target["disease_damage", 0]) {
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

class Disease : Script {

    init {
        playerSpawn {
            if (diseaseCounter != 0) {
                timers.restart("disease")
            }
        }

        npcSpawn {
            if (diseaseCounter != 0) {
                softTimers.restart("disease")
            }
        }

        timerStart("disease", ::start)
        npcTimerStart("disease", ::start)
        timerTick("disease", ::tick)
        npcTimerTick("disease", ::tick)
        timerStop("disease", ::stop)
        npcTimerStop("disease", ::stop)
    }

    fun start(character: Character, restart: Boolean): Int {
        if (immune(character)) {
            return Timer.CANCEL
        }
        if (!restart && character.diseaseCounter == 0) {
            (character as? Player)?.message("You have been diseased.")
            damage(character)
        }
        return 30
    }

    fun tick(character: Character): Int {
        val diseased = character.diseased
        character.diseaseCounter -= character.diseaseCounter.sign
        when {
            character.diseaseCounter == 0 -> {
                if (!diseased) {
                    (character as? Player)?.message("Your disease resistance has worn off.")
                }
                return Timer.CANCEL
            }
            character.diseaseCounter == -1 -> (character as? Player)?.message("Your disease resistance is about to wear off.")
            diseased -> damage(character)
        }
        return Timer.CONTINUE
    }

    fun stop(character: Character, logout: Boolean) {
        character.diseaseCounter = 0
        character.clear("disease_damage")
        character.clear("disease_source")
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
        val drain = ceil(damage / 5.0).toInt()
        character.sound("disease_hitsplat")
        if (character is Player) {
            val skill = DRAINABLE_SKILLS[random.nextInt(DRAINABLE_SKILLS.size)]
            val current = character.levels.get(skill)
            if (current <= 1) {
                // No skill level left to drain — bite Hitpoints instead.
                character.directHit(source, drain * 10, "disease")
            } else {
                character.levels.drain(skill, drain)
                showDiseaseSplat(character, source, drain * 10)
            }
        } else {
            character.directHit(source, drain * 10, "disease")
        }
    }

    /**
     * Adds a yellow disease hitsplat showing [amount] without deducting Constitution
     * (used when disease drained a stat instead of HP).
     */
    private fun showDiseaseSplat(target: Character, source: Character, amount: Int) {
        val hp = target.levels.get(Skill.Constitution)
        val percentage = target.levels.getPercent(Skill.Constitution, hp, 255.0).toInt()
        target.visuals.hits.add(
            HitSplat(
                amount,
                HitSplat.Mark.Diseased,
                percentage,
                0,
                false,
                if (source is NPC) -source.index else source.index,
                -1,
            ),
        )
        target.flagHits()
    }

    companion object {
        private val DRAINABLE_SKILLS: Array<Skill> = Skill.entries
            .filter { it != Skill.Constitution && it != Skill.Prayer }
            .toTypedArray()
    }
}
