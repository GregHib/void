package content.entity.player.modal.tab

import content.entity.effect.clearTransform
import content.entity.effect.movementDelay
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.random.Random

class Morphing : Script {

    init {
        itemOption("Wear", "easter_ring") {
            morph(this, "easter_egg_${Random.nextInt(0, 6)}")
        }

        itemOption("Wear", "bone_brooch") {
            anim("bone_brooch_transform")
            gfx("bone_brooch_transform")
            delay(4)
            morph(this, "bone_pile")
        }

        itemOption("Wear", "ring_of_stone") { (item) ->
            morph(this, item.id)
        }

        interfaceOption("Ok", "morph:unmorph") {
            unmorph(this, playAnimation = true)
        }
    }

    fun morph(player: Player, npc: String) {
        player.transform(npc)
        player.movementDelay = Int.MAX_VALUE
        player.softTimers.start("movement_delay")
        player.open("morph")
        player.walkTrigger {
            unmorph(player)
        }
    }

    fun unmorph(player: Player, playAnimation: Boolean = false) {
        val boneBrooch = player.transform == "bone_pile"
        player.queue.clear()
        if (boneBrooch && playAnimation) {
            player.anim("bone_brooch_unmorph")
        }
        player.clearTransform()
        player.movementDelay = 0
        player.close("morph")
    }
}
