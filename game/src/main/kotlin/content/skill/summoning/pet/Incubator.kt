package content.skill.summoning.pet

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

private data class IncubatorRegion(val varbit: String, val suffix: String)

/**
 * Per-region wiring: cache varbit name (drives the client-side incubator
 * transform) + persistence suffix used for `incubator_egg_<suffix>` /
 * `incubator_end_<suffix>` variables (declared in incubator.vars.toml).
 * Varbit values: 0 = empty, 1 = incubating, 2 = finished.
 */
private val regionVarbits = mapOf(
    11573 to IncubatorRegion("taverley_incubator_state", "taverley"),
    10288 to IncubatorRegion("yanille_incubator_state", "yanille"),
)

private fun eggKey(suffix: String) = "incubator_egg_$suffix"
private fun endKey(suffix: String) = "incubator_end_$suffix"

private const val EMPTY = 0
private const val INCUBATING = 1
private const val FINISHED = 2

class Incubator(private val definitions: IncubatorDefinitions) : Script {

    init {
        objectOperate("Inspect", "incubator_*") {
            val region = regionVarbits[tile.region.id]
            if (region == null) {
                message("This incubator isn't ready to be used.")
                return@objectOperate
            }
            when (currentState(region)) {
                FINISHED -> message("The egg inside has finished incubating.")
                INCUBATING -> {
                    val eggId = get(eggKey(region.suffix), "")
                    val productName = definitions.get(eggId)?.product?.replace('_', ' ') ?: "an"
                    message("There is currently a $productName egg incubating.")
                }
                else -> message("The incubator is currently empty.")
            }
        }

        objectOperate("Take-egg", "incubator_*") {
            val region = regionVarbits[tile.region.id] ?: return@objectOperate
            if (currentState(region) != FINISHED) {
                message("That egg hasn't finished incubating!")
                return@objectOperate
            }
            val eggId = get(eggKey(region.suffix), "")
            val def = definitions.get(eggId) ?: return@objectOperate
            if (!inventory.add(def.product)) {
                message("You don't have enough room in your inventory.")
                return@objectOperate
            }
            clearIncubator(region)
            message("You take your ${def.product.replace('_', ' ')} out of the incubator.")
        }

        itemOnObjectOperate(obj = "incubator_*") { interact ->
            val region = regionVarbits[tile.region.id]
            if (region == null) {
                message("This incubator isn't ready to be used.")
                return@itemOnObjectOperate
            }
            val def = definitions.forEgg(interact.item.id) ?: return@itemOnObjectOperate
            if (currentState(region) != EMPTY) {
                message("You already have an egg in this incubator.")
                return@itemOnObjectOperate
            }
            if (!has(Skill.Summoning, def.summoningLevel)) {
                message("You need a Summoning level of ${def.summoningLevel} to incubate this egg.")
                return@itemOnObjectOperate
            }
            if (!inventory.remove(def.egg)) return@itemOnObjectOperate
            set(eggKey(region.suffix), def.id)
            set(endKey(region.suffix), System.currentTimeMillis() + def.incubationSeconds * 1000L)
            set(region.varbit, INCUBATING)
            timers.start("incubator_check")
            message("You place the egg in the incubator.")
        }

        timerStart("incubator_check") {
            TimeUnit.SECONDS.toTicks(15)
        }

        timerTick("incubator_check") {
            var active = false
            val now = System.currentTimeMillis()
            for (region in regionVarbits.values) {
                val eggId = get(eggKey(region.suffix), "")
                if (eggId.isBlank()) continue
                val state = get<Int>(region.varbit) ?: EMPTY
                if (state == FINISHED) {
                    active = true
                    continue
                }
                val end = get(endKey(region.suffix), 0L)
                if (now >= end) {
                    val productName = definitions.get(eggId)?.product?.replace('_', ' ') ?: "egg"
                    message("<col=00cc00>Your $productName egg has finished hatching.</col>")
                    set(region.varbit, FINISHED)
                }
                active = true
            }
            if (!active) Timer.CANCEL else Timer.CONTINUE
        }

        playerSpawn {
            for (region in regionVarbits.values) {
                if (get(eggKey(region.suffix), "").isNotBlank()) {
                    timers.start("incubator_check")
                    return@playerSpawn
                }
                // Ensure stale varbit doesn't show occupied incubator after a wipe.
                val state = get<Int>(region.varbit) ?: EMPTY
                if (state != EMPTY) set(region.varbit, EMPTY)
            }
        }
    }

    private fun Player.currentState(region: IncubatorRegion): Int {
        val eggId = get(eggKey(region.suffix), "")
        if (eggId.isBlank()) return EMPTY
        val state = get<Int>(region.varbit) ?: INCUBATING
        if (state == FINISHED) return FINISHED
        if (System.currentTimeMillis() >= get(endKey(region.suffix), 0L)) return FINISHED
        return INCUBATING
    }

    private fun Player.clearIncubator(region: IncubatorRegion) {
        set(eggKey(region.suffix), "")
        set(endKey(region.suffix), 0L)
        set(region.varbit, EMPTY)
    }
}
