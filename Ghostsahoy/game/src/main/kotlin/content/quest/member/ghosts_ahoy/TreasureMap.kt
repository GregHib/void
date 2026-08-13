package content.quest.member.ghosts_ahoy

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class TreasureMap : Script {
    init {
        itemOption("Read", "treasure_map") {
            open("ahoy_islandmap")
        }

        itemOption("Follow", "treasure_map") {
            if (tile.region.id != 15159) {
                statement("The map is for use elsewhere.")
                return@itemOption
            }
            walkSession()
        }
    }

    private suspend fun Player.walkSession() {
        var direction = 0
        choice("Which direction do you wish to walk?") {
            option("North") { direction = 1 }
            option("East") { direction = 2 }
            option("South") { direction = 3 }
            option("West") { direction = 4 }
        }
        if (direction == 0) return
        var steps = 0
        choice("How far do you wish to walk?") {
            option("1 pace") { steps = 1 }
            option("2 paces") { steps = 2 }
            option("4 paces") { steps = 4 }
            option("10 paces") { steps = 10 }
        }
        if (steps == 0) return
        val targetTile = when (direction) {
            1 -> tile.add(0, steps)
            2 -> tile.add(steps, 0)
            3 -> tile.add(0, -steps)
            else -> tile.add(-steps, 0)
        }
        walkTo(targetTile, forceWalk = false, noCollision = false)
    }
}
