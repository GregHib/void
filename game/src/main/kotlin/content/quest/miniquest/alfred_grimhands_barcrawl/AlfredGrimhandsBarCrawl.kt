package content.quest.miniquest.alfred_grimhands_barcrawl

import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.messageScroll
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

suspend fun Player.barCrawlDrink(
    target: NPC,
    start: (suspend Player.() -> Unit)? = null,
    effects: suspend Player.() -> Unit = {},
) {
    player<Talk>("I'm doing Alfred Grimhand's Barcrawl.")
    val info: Map<String, Any> = target.def.getOrNull("bar_crawl") ?: return
    start?.invoke(this) ?: npc<Talk>(info["start"] as String)
    val id = info["id"] as String
    if (!inventory.remove("coins", info["price"] as Int)) {
        player<Sad>(info["insufficient"] as String)
        return
    }
    message(info["give"] as String)
    delay(4)
    message(info["drink"] as String)
    delay(4)
    message(info["effect"] as String)
    delay(4)
    (info["sign"] as? String)?.let { message(it) }
    addVarbit("barcrawl_signatures", id)
    effects()
}

val onBarCrawl: Player.(NPC) -> Boolean = filter@{ target ->
    val info: Map<String, Any> = target.def.getOrNull("bar_crawl") ?: return@filter false
    val id = info["id"] as String
    quest("alfred_grimhands_barcrawl") == "signatures" && !containsVarbit("barcrawl_signatures", id)
}

class AlfredGrimhandsBarCrawl : Script {

    init {
        itemOption("Read", "barcrawl_card") {
            val signatures: List<String> = get("barcrawl_signatures", emptyList())
            if (signatures.size == 10) {
                message("You are too drunk to be able to read the barcrawl card.")
                return@itemOption
            }
            messageScroll(
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
                ),
            )
        }
    }

    fun Player.line(name: String, id: String): String {
        val complete = containsVarbit("barcrawl_signatures", id)
        return "<${Colours.bool(complete)}>$name - ${if (complete) "Completed!" else "Not Completed"}"
    }
}
