package content.quest.free.vampire_slayer

import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile

class DraynorCoffin : Script {
    init {
        objectOperate("Open", "coffin") { (target) -> open(target) }
    }

    private fun Player.open(target: GameObject) {
        // Object 158 is also used elsewhere; only this coffin belongs to the quest.
        if (target.tile != Tile(3080, 9785)) {
            message("There is nothing of interest inside.")
            return
        }
        when (quest("vampire_slayer")) {
            "completed" -> message("The coffin is empty apart from a pillow.")
            "armed" -> {
                val existing = get<NPC>("vampire_slayer_count")
                if (existing != null && existing.index != -1 && !existing.hide) {
                    message("Count Draynor is already out of his coffin!")
                    return
                }
                val count = NPCs.add("count_draynor_normal", Tile(3079, 9785), ticks = -1, owner = this)
                set("vampire_slayer_count", count)
                count.softTimers.start("vampire_slayer_count")
                count.interactPlayer(this, "Attack")
                message("Count Draynor emerges from the coffin!")
            }
            else -> message("You should learn how to defeat the vampyre before disturbing his coffin.")
        }
    }
}
