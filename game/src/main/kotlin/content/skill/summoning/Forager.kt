package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

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
            forage()
            Timer.CONTINUE
        }

        npcOperate("Withdraw", "*_familiar") { (target) ->
            if (target == follower && forageTable(dropTables) != null) {
                openBeastOfBurden()
            }
        }

        npcOperate("Take", "*_familiar") { (target) ->
            if (target == follower && forageTable(dropTables) != null) {
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
        for (drop in table.roll(player = this)) {
            val item = drop.toItem()
            if (item.isEmpty()) {
                continue
            }
            beastOfBurden.add(item.id, item.amount)
        }
        if (interfaces.contains("beast_of_burden")) {
            syncBeastOfBurdenInterface()
        }
    }
}
