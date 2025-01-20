package world.gregs.voidps.world.activity.quest.mini

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.event.TargetContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.Sad
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

suspend fun <T> T.barCrawlDrink(
    start: (suspend T.() -> Unit)? = null,
    effects: suspend T.() -> Unit = {},
) where T : TargetContext<Player, NPC>, T : SuspendableContext<Player> {
    player<Talk>("I'm doing Alfred Grimhand's Barcrawl.")
    val info: Map<String, Any> = target.def.getOrNull("bar_crawl") ?: return
    start?.invoke(this) ?: npc<Talk>(info["start"] as String)
    val id = info["id"] as String
    if (!player.inventory.remove("coins", info["price"] as Int)) {
        player<Sad>(info["insufficient"] as String)
        return
    }
    player.message(info["give"] as String)
    player.queue("barcrawl_$id", 4) {
        player.message(info["drink"] as String)
        pause(4)
        player.message(info["effect"] as String)
        pause(4)
        (info["sign"] as? String)?.let { player.message(it) }
        player.addVarbit("barcrawl_signatures", id)
        effects()
    }
}

val barCrawlFilter: TargetContext<Player, NPC>.() -> Boolean = filter@{
    val info: Map<String, Any> = target.def.getOrNull("bar_crawl") ?: return@filter false
    val id = info["id"] as String
    player.quest("alfred_grimhands_barcrawl") == "signatures" && !player.containsVarbit("barcrawl_signatures", id)
}