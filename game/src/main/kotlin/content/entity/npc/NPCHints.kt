package content.entity.npc

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearHint
import world.gregs.voidps.engine.client.hint
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.network.login.protocol.encode.HintArrow

/**
 * Hint arrows sit in one of eight per-player viewport slots and are only handed back by
 * [clearHint], so an arrow left on an npc that dies or times out costs that player a slot for the
 * rest of their session. Marking through [markHint] records the slot on the npc so it can be
 * released when the npc goes away.
 */
class NPCHints : Script {
    init {
        npcDespawn {
            clearHints()
        }
    }
}

/**
 * Points a hint arrow at this npc for [player], releasing it when the npc despawns - which covers
 * dying, since a spawned npc with nowhere to respawn to is removed once its death finishes.
 */
fun NPC.markHint(player: Player, arrow: Int = HintArrow.FILLED) {
    val slots: MutableMap<String, Int> = getOrPut("hint_slots") { mutableMapOf() }
    // Free the old slot first so re-marking reuses it rather than taking a second
    val previous = slots.remove(player.accountName)
    if (previous != null) {
        player.clearHint(previous)
    }
    val slot = player.hint(this, arrow)
    if (slot == -1) {
        return
    }
    slots[player.accountName] = slot
}

/**
 * Hands back every arrow slot [markHint] took for this npc. Safe to call more than once - the
 * second call finds nothing rather than clearing a slot something else has since taken.
 */
fun NPC.clearHints() {
    val slots: MutableMap<String, Int> = get("hint_slots") ?: return
    clear("hint_slots")
    for ((account, slot) in slots) {
        Players.findByAccount(account)?.clearHint(slot)
    }
}
