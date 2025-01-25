package content.quest.miniquest.alfred_grimhands_barcrawl

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.event.TargetContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import content.quest.quest
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player

suspend fun <T : TargetInteraction<Player, NPC>> T.barCrawlDrink(
    start: (suspend T.() -> Unit)? = null,
    effects: suspend T.() -> Unit = {},
) {
    player<Talk>("I'm doing Alfred Grimhand's Barcrawl.")
    val info: Map<String, Any> = target.def.getOrNull("bar_crawl") ?: return
    start?.invoke(this) ?: npc<Talk>(info["start"] as String)
    val id = info["id"] as String
    if (!player.inventory.remove("coins", info["price"] as Int)) {
        player<Sad>(info["insufficient"] as String)
        return
    }
    player.message(info["give"] as String)
    delay(4)
    player.message(info["drink"] as String)
    delay(4)
    player.message(info["effect"] as String)
    delay(4)
    (info["sign"] as? String)?.let { player.message(it) }
    player.addVarbit("barcrawl_signatures", id)
    effects()
}

val barCrawlFilter: TargetContext<Player, NPC>.() -> Boolean = filter@{
    val info: Map<String, Any> = target.def.getOrNull("bar_crawl") ?: return@filter false
    val id = info["id"] as String
    player.quest("alfred_grimhands_barcrawl") == "signatures" && !player.containsVarbit("barcrawl_signatures", id)
}