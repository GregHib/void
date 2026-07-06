package content.skill.summoning.pet

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

private val SUFFIXES = listOf("taverley", "yanille")

private fun egg(id: String): RowDefinition? = Rows.getOrNull("incubator_eggs.$id")

private fun eggForItem(itemId: String): RowDefinition? = Tables.get("incubator_eggs").rows().firstOrNull { it.item("egg") == itemId }

class Incubator : Script {

    init {
        objectOperate("Inspect", "incubator_*") {
            val suffix = it.target.id.removePrefix("incubator_")
            val eggId = get("incubator_egg_$suffix", "")
            when {
                eggId.isBlank() -> message("The incubator is currently empty.")
                isFinished(suffix) -> message("The egg inside has finished incubating.")
                else -> {
                    val productName = egg(eggId)?.item("product")?.replace('_', ' ') ?: "an"
                    message("There is currently a $productName egg incubating.")
                }
            }
        }

        objectOperate("Take-egg", "incubator_*") {
            val suffix = it.target.id.removePrefix("incubator_")
            if (get("incubator_egg_$suffix", "").isBlank() || !isFinished(suffix)) {
                message("That egg hasn't finished incubating!")
                return@objectOperate
            }
            val def = egg(get("incubator_egg_$suffix", "")) ?: return@objectOperate
            val product = def.item("product")
            if (!inventory.add(product)) {
                message("You don't have enough room in your inventory.")
                return@objectOperate
            }
            clearIncubator(suffix)
            message("You take your ${product.replace('_', ' ')} out of the incubator.")
        }

        itemOnObjectOperate(obj = "incubator_*") { interact ->
            val suffix = interact.target.id.removePrefix("incubator_")
            val def = eggForItem(interact.item.id) ?: return@itemOnObjectOperate
            if (get("incubator_egg_$suffix", "").isNotBlank()) {
                if (isFinished(suffix)) {
                    message("Your previous egg has finished hatching, take it out first.")
                } else {
                    message("You already have an egg in this incubator.")
                }
                return@itemOnObjectOperate
            }
            val level = def.int("summoning_level")
            if (!hasMax(Skill.Summoning, level)) {
                message("You need a Summoning level of $level to incubate this egg.")
                return@itemOnObjectOperate
            }
            if (!inventory.remove(def.item("egg"))) return@itemOnObjectOperate
            set("incubator_egg_$suffix", def.rowId)
            start("incubator_end_$suffix", def.int("incubation_seconds"), base = epochSeconds())
            set("incubator_state_$suffix", "incubating")
            timers.start("incubator_check")
            message("You place the egg in the incubator.")
        }

        timerStart("incubator_check") {
            TimeUnit.SECONDS.toTicks(15)
        }

        timerTick("incubator_check") {
            var active = false
            for (suffix in SUFFIXES) {
                val eggId = get("incubator_egg_$suffix", "")
                if (eggId.isBlank()) continue
                if (!get("incubator_finished_announced_$suffix", false) && isFinished(suffix)) {
                    val productName = egg(eggId)?.item("product")?.replace('_', ' ') ?: "egg"
                    message("<col=00cc00>Your $productName egg has finished hatching.</col>")
                    set("incubator_finished_announced_$suffix", true)
                }
                active = true
            }
            if (!active) Timer.CANCEL else Timer.CONTINUE
        }

        playerSpawn {
            for (suffix in SUFFIXES) {
                if (get("incubator_egg_$suffix", "").isBlank()) continue
                // The state varbit only carries empty/incubating; "finished"
                // is computed from remaining time. Only force state back to
                // "incubating" when the egg is still mid-incubation, so a
                // finished slot keeps whatever state value it logged out with.
                if (!isFinished(suffix)) {
                    set("incubator_state_$suffix", "incubating")
                }
                timers.restart("incubator_check")
            }
        }
    }

    private fun Player.isFinished(suffix: String): Boolean = remaining("incubator_end_$suffix", epochSeconds()) <= 0

    private fun Player.clearIncubator(suffix: String) {
        set("incubator_egg_$suffix", "")
        set("incubator_end_$suffix", 0)
        set("incubator_state_$suffix", "empty")
        set("incubator_finished_announced_$suffix", false)
    }
}
