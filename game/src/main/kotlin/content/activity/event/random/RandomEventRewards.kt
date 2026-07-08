package content.activity.event.random

import content.entity.player.bank.ownsItem
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

/**
 * "Collect the set" reward shared by the costume random events (Drill Demon's camouflage set,
 * Freaky Forester's lederhosen). Awards the first costume piece the player doesn't already own
 * (carried or banked); if they own the whole set, awards [coins] instead. Anything that won't fit
 * drops to the floor beneath them.
 */
fun Player.rewardCostumeOrCoins(vararg pieces: String, coins: Int) {
    val missing = pieces.firstOrNull { !ownsItem(it) }
    if (missing != null) {
        giveOrDrop(missing, 1)
    } else {
        giveOrDrop("coins", coins)
    }
}

/**
 * Award a single weighted roll of the shared `random_event_certer` gem/coin table (Certer, Pillory).
 */
fun Player.rewardCerterLoot() {
    val rows = Tables.get("random_event_certer").rows()
    var roll = random.nextInt(rows.sumOf { it.int("weight") })
    for (row in rows) {
        roll -= row.int("weight")
        if (roll < 0) {
            giveOrDrop(row.item("item"), random.nextInt(row.int("min"), row.int("max") + 1))
            return
        }
    }
}

private fun Player.giveOrDrop(item: String, amount: Int) {
    if (!inventory.add(item, amount)) {
        FloorItems.add(tile, item, amount, disappearTicks = 300, owner = this)
    }
}
