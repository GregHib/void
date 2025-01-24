package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.world.interact.entity.effect.clearTransform
import world.gregs.voidps.world.interact.entity.effect.movementDelay
import world.gregs.voidps.world.interact.entity.effect.transform
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOptions
import kotlin.random.Random

inventoryOptions("Wear", item = "easter_ring") {
    morph(player, "easter_egg_${Random.nextInt(0, 6)}")
}

inventoryOptions("Wear", item = "ring_of_stone") {
    morph(player, item.id)
}

interfaceOption("Ok", "unmorph", "morph") {
    unmorph(player)
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