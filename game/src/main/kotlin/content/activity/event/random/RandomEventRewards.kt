package content.activity.event.random

import content.entity.player.bank.ownsItem
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
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
        addOrDrop(missing)
    } else {
        addOrDrop("coins", coins)
    }
}

/**
 * Award a single weighted roll of a random event loot table, e.g. the shared `random_event_certer`
 * gem/coin table (Certer, Pillory) or `random_event_prison_pete`.
 */
fun Player.rewardEventLoot(table: String) {
    val rows = Tables.get(table).rows()
    var roll = random.nextInt(rows.sumOf { it.int("weight") })
    for (row in rows) {
        roll -= row.int("weight")
        if (roll < 0) {
            addOrDrop(row.item("item"), random.nextInt(row.int("min"), row.int("max") + 1))
            return
        }
    }
}
