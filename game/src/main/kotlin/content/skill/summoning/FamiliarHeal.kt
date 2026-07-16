package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Life points each healing familiar restores to its owner every 15 seconds while summoned. Read live
 * from the active follower, so the heal follows whichever familiar is out and stops the moment it's
 * dismissed (matching how [FAMILIAR_BOOSTS] gates its passive boosts).
 */
val FAMILIAR_HEAL_LIFEPOINTS: Map<String, Int> = mapOf(
    "void_spinner_familiar" to 100,
    "bunyip_familiar" to 20,
)

/** Graphic played on the owner when the familiar's passive heal actually restores life points. */
private val FAMILIAR_HEAL_GFX: Map<String, String> = mapOf(
    "bunyip_familiar" to "bunyip_heal",
)

/**
 * Healing familiars (Void spinner, ...) restore their owner's life points at a fixed 15s interval.
 * [levels.restore] no-ops once the owner is at full life points, so the timer keeps running
 * harmlessly until they take damage again.
 *
 * The heal timer is started on summon in [summonFamiliar] (when the familiar heals) and stopped on
 * dismiss.
 */
class FamiliarHeal : Script {
    init {
        timerStart("familiar_heal") { TimeUnit.SECONDS.toTicks(15) }

        timerTick("familiar_heal") {
            val amount = FAMILIAR_HEAL_LIFEPOINTS[follower?.id] ?: return@timerTick Timer.CANCEL
            if (levels.restore(Skill.Constitution, amount) > 0) {
                FAMILIAR_HEAL_GFX[follower?.id]?.let { gfx(it) }
            }
            Timer.CONTINUE
        }
    }
}
