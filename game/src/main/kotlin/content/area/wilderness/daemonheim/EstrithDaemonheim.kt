package content.area.wilderness.daemonheim

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.type.random

class EstrithDaemonheim(val patrols: PatrolDefinitions) : Script {
    private val patrolTiles = patrols.get("estrith_daemonheim").waypoints.map { it.first }

    init {
        npcSpawn("estrith_daemonheim") {
            val patrol = patrols.get("estrith_daemonheim")
            if (tile != patrol.waypoints.first().first) {
                return@npcSpawn
            }
            mode = Patrol(this, patrol.waypoints)
            scheduleLine(this, tile)
        }

        npcMoved("estrith_daemonheim", ::talkAtTile)
    }

    fun talkAtTile(npc: NPC, from: Tile) {
        if (npc.tile == from || npc.tile !in patrolTiles || random.nextInt(2) == 1) {
            return
        }
        scheduleLine(npc, npc.tile)
    }

    private fun scheduleLine(npc: NPC, tile: Tile) {
        val line = lineFor(tile)
        npc.queue("estrith_daemonheim_talk", 6) {
            if (this.tile != tile) {
                return@queue
            }
            say(line)
        }
    }

    private fun lineFor(tile: Tile) = when {
        tile.equals(3429, 3695) -> "Soon we'll be home eating red meat and combing innards from our hair."
        tile.equals(3450, 3718) -> "If that smuggler is sneaking about again, I'll have his head on a rusty pike."
        tile.equals(3441, 3693) -> "If I never see another floating eyeball in a hundred years, it'll be too soon."
        else -> genericLine(random.nextInt(5))
    }

    private fun genericLine(index: Int) = when (index) {
        0 -> "Yet another round with nothing interesting happening."
        1 -> "Time to do my rounds, I suppose."
        2 -> "Hey you! I've got my eyes on you. You better not try any funny business around here."
        3 -> "Oh look! Some rocks! Oh, and some more rocks! It can't get more exciting than this."
        else -> "I've got the eyes of a hawk, the ears of a wolf, the speed of a kyatt and an awful day job."
    }
}
