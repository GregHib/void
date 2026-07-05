package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Fishing xp granted per fish the fishing foragers pull out of the water (a tenth of the catch xp,
 * as in the live game).
 */
private val FORAGE_FISHING_XP: Map<String, Map<String, Double>> = mapOf(
    "granite_lobster_familiar" to mapOf("raw_swordfish" to 10.0, "raw_shark" to 11.0),
    "ibis_familiar" to mapOf("raw_tuna" to 8.0, "raw_swordfish" to 10.0),
)

/**
 * Foragers that only roll while their owner is busy with a matching action - the ibis and granite
 * lobster spear their fish alongside the owner, so they only produce while the owner is fishing.
 */
private val FORAGE_CONDITIONS: Map<String, Player.() -> Boolean> = mapOf(
    "ibis_familiar" to { softTimers.contains("fishing") },
    "granite_lobster_familiar" to { softTimers.contains("fishing") },
)

/**
 * Forager familiars (magpie, ...) periodically gather loot from their own `forage_<familiar>`
 * drop table into the familiar's inventory while summoned. The player retrieves it with the
 * familiar's Withdraw/Take option (reusing the beast-of-burden inventory + interface); they can't
 * deposit their own items into a forager.
 *
 * The forage timer is started on summon in [summonFamiliar] and stopped on dismiss.
 */
class Forager(private val dropTables: DropTables) : Script {
    init {
        timerStart("forage") { TimeUnit.SECONDS.toTicks(30) }

        timerTick("forage") {
            if (forageTable(dropTables) == null) {
                return@timerTick Timer.CANCEL
            }
            if (FORAGE_CONDITIONS[follower?.id]?.invoke(this) != false) {
                forage()
            }
            Timer.CONTINUE
        }

        npcOperate("Withdraw", "*_familiar") { (target) ->
            if (target == follower && forageTable(dropTables) != null) {
                if (target[FAMILIAR_CHOPPING, false]) {
                    message(FAMILIAR_BUSY_MESSAGE)
                    return@npcOperate
                }
                openBeastOfBurden()
            }
        }

        npcOperate("Take", "*_familiar") { (target) ->
            if (target == follower && forageTable(dropTables) != null) {
                if (target[FAMILIAR_CHOPPING, false]) {
                    message(FAMILIAR_BUSY_MESSAGE)
                    return@npcOperate
                }
                openBeastOfBurden()
            }
        }
    }

    private fun Player.forage() {
        val table = forageTable(dropTables) ?: return
        ensureBeastOfBurdenInventory()
        if (beastOfBurden.items.count { it.isNotEmpty() } >= beastOfBurdenCapacity) {
            return
        }
        var produced = false
        for (drop in table.roll(player = this)) {
            val item = drop.toItem()
            if (item.isEmpty()) {
                continue
            }
            beastOfBurden.add(item.id, item.amount)
            FORAGE_FISHING_XP[follower?.id]?.get(item.id)?.let { exp(Skill.Fishing, it) }
            produced = true
        }
        if (produced) {
            message("Your familiar has produced an item.")
        }
        if (interfaces.contains("beast_of_burden")) {
            syncBeastOfBurdenInterface()
        }
    }
}
