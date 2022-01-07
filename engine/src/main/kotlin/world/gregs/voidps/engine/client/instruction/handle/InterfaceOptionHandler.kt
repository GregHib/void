package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.InterfaceClick
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.instruct.InteractInterface

class InterfaceOptionHandler : InstructionHandler<InteractInterface>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterface) {
        val (interfaceId, componentId, itemId, itemSlot, option) = instruction

        var (id, component, item, container, options) = InterfaceHandler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return

        if (options == null) {
            options = player.interfaceOptions.get(id, component)
        }

        if (option !in options.indices) {
            logger.info { "Interface option not found [$player, interface=$interfaceId, component=$componentId, option=$option, options=${options.toList()}]" }
            return
        }

        val selectedOption = options.getOrNull(option) ?: ""
        val click = InterfaceClick(
            id,
            component,
            option,
            selectedOption,
            item,
            itemSlot,
            container
        )
        player.events.emit(click)
        if (click.cancel) {
            return
        }
        player.events.emit(
            InterfaceOption(
                id,
                component,
                option,
                selectedOption,
                item,
                itemSlot,
                container
            )
        )
    }

}