package content.entity.player.modal.tab

import content.entity.effect.clearTransform
import content.entity.effect.movementDelay
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.queue
import kotlin.random.Random

class Morphing : Script {

    init {
        itemOption("Wear", "easter_ring") {
            morph(this, "easter_egg_${Random.nextInt(0, 6)}")
        }

        itemOption("Wear", "ring_of_stone") { (item) ->
            morph(this, item.id)
        }

        interfaceOption("Ok", "morph:unmorph") {
            unmorph(this)
        }
    }

    fun morph(player: Player, npc: String) {
        player.transform(npc)
        player.movementDelay = Int.MAX_VALUE
        player.softTimers.start("movement_delay")
        player.open("morph")
        player.queue("morph", onCancel = { unmorph(player) }) {
        }
    }

    fun unmorph(player: Player) {
        player.queue.clear()
        player.clearTransform()
        player.movementDelay = 0
        player.close("morph")
    }
}
