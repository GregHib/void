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
import world.gregs.voidps.engine.data.definition.EnumDefinitions
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
    val npcId = target.def.id
    start?.invoke(this) ?: npc<Neutral>(EnumDefinitions.get("bar_crawl_start").string(npcId))
    val id = EnumDefinitions.get("bar_crawl_ids").string(npcId)
    val price = EnumDefinitions.get("bar_crawl_prices").int(npcId)
    if (!inventory.remove("coins", price)) {
        player<Disheartened>(EnumDefinitions.get("bar_crawl_insufficient").string(npcId))
        return
    }
    message(EnumDefinitions.get("bar_crawl_give").string(npcId))
    delay(4)
    message(EnumDefinitions.get("bar_crawl_drink").string(npcId))
    delay(4)
    message(EnumDefinitions.get("bar_crawl_effect").string(npcId))
    delay(4)
    EnumDefinitions.get("bar_crawl_sign").stringOrNull(npcId)?.let { message(it) }
    addVarbit("barcrawl_signatures", id)
    effects()
}

val onBarCrawl: Player.(NPC) -> Boolean = filter@{ target ->
    val id = EnumDefinitions.get("bar_crawl_ids").stringOrNull(target.def.id) ?: return@filter false
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
