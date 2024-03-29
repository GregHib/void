package world.gregs.voidps.world.activity.dnd.treasuretrails

import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.world.activity.bank.ownsItem

itemAdded("clue_scroll_*") { player ->
    if (player.ownsItem(item.id)) {
        val difficulty = item.id.removePrefix("clue_scroll_")
        player["${difficulty}_clue"] = true
    }
}

itemRemoved("clue_scroll_*") { player ->
    if (!player.ownsItem(oldItem.id)) {
        val difficulty = oldItem.id.removePrefix("clue_scroll_")
        player.clear("${difficulty}_clue")
    }
}