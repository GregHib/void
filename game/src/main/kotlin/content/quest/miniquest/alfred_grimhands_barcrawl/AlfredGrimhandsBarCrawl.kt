package content.quest.miniquest.alfred_grimhands_barcrawl

import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.messageScroll
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

suspend fun Player.barCrawlDrink(
    target: NPC,
    start: (suspend Player.() -> Unit)? = null,
    effects: suspend Player.() -> Unit = {},
) {
    player<Neutral>("I'm doing Alfred Grimhand's Barcrawl.")
    val bar = Rows.getOrNull("bar_crawl.${target.id}") ?: return
    start?.invoke(this) ?: npc<Neutral>(bar.string("start"))
    if (!inventory.remove("coins", bar.int("price"))) {
        player<Disheartened>(bar.string("insufficient"))
        return
    }
    message(bar.string("give"))
    delay(4)
    message(bar.string("drink"))
    delay(4)
    message(bar.string("effect"))
    delay(4)
    bar.stringOrNull("sign")?.let { message(it) }
    addVarbit("barcrawl_signatures", bar.string("id"))
    effects()
}

val onBarCrawl: Player.(NPC) -> Boolean = filter@{ target ->
    val id = Tables.stringOrNull("bar_crawl.${target.id}.id") ?: return@filter false
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
