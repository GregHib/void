package content.quest.miniquest.alfred_grimhands_barcrawl

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.entity.character.player.Player
import content.quest.messageScroll
import content.entity.player.inv.inventoryItem

inventoryItem("Read", "barcrawl_card") {
    val signatures: List<String> = player["barcrawl_signatures", emptyList()]
    if (signatures.size == 10) {
        player.message("You are too drunk to be able to read the barcrawl card.")
        return@inventoryItem
    }
    player.messageScroll(
        listOf(
            "${Colours.BLUE.toTag()}The Official Alfred Grimhand Barcrawl!",
            "",
            line("Blue Moon Inn", "uncle_humphreys_gutrot"),
            line("Blurberry's Bar", "fire_toad_blast"),
            line("Dead Man's Chest", "supergrog"),
            line("Dragon Inn", "fire_brandy"),
            line("Flying Horse Inn", "heart_stopper"),
            line("Forester's Arms", "liverbane_ale"),
            line("Jolly Boar Inn", "olde_suspiciouse"),
            line("Karamja Spirits Bar", "ape_bite_liqueur"),
            line("Rising Sun Inn", "hand_of_death_cocktail"),
            line("Rusty Anchor Inn", "black_skull_ale"),
        )
    )
}

fun Context<Player>.line(name: String, id: String): String {
    val complete = player.containsVarbit("barcrawl_signatures", id)
    return "<${Colours.bool(complete)}>$name - ${if (complete) "Completed!" else "Not Completed"}"
}