package world.gregs.voidps.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.sendContainer
import world.gregs.voidps.engine.contain.swap
import world.gregs.voidps.engine.entity.character.mode.interact.clear
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

val logger = InlineLogger()

on<InterfaceRefreshed>({ id == "inventory" }) { player: Player ->
    player.interfaceOptions.unlockAll(id, "container", 0 until 28)
    player.interfaceOptions.unlock(id, "container", 28 until 56, "Drag")
    player.sendContainer(id)
}

on<InterfaceSwitch>({ id == "inventory" && toId == "inventory" }) { player: Player ->
    if (!player.inventory.swap(fromSlot, toSlot)) {
        logger.info { "Failed switching interface items $this" }
    }
}

on<InterfaceOption>({ id == "inventory" && component == "container" }) { player: Player ->
    val itemDef = item.def
    val equipOption = when (optionIndex) {
        6 -> itemDef.options.getOrNull(3)
        7 -> itemDef.options.getOrNull(4)
        9 -> "Examine"
        else -> itemDef.options.getOrNull(optionIndex)
    }
    if (equipOption == null) {
        logger.info { "Unknown item option $item $optionIndex" }
        return@on
    }
    player.clear()
    player.events.emit(
        ContainerOption(
            player,
            id,
            item,
            itemSlot,
            equipOption
        )
    )
}