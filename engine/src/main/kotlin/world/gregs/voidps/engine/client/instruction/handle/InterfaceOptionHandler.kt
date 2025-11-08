package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.InterfaceApi
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractInterface

class InterfaceOptionHandler(
    private val handler: InterfaceHandler,
    private val interfaceDefinitions: InterfaceDefinitions,
) : InstructionHandler<InteractInterface>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterface) {
        val (interfaceId, componentId, itemId, itemSlot, option) = instruction

        var (id, component, item, _, options) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return

        if (options == null) {
            options = interfaceDefinitions.getComponent(id, component)?.get("options") ?: emptyArray()
        }

        if (option !in options.indices) {
            logger.info { "Interface option not found [$player, interface=$interfaceId, component=$componentId, option=$option, options=${options.toList()}]" }
            return
        }

        val selectedOption = options.getOrNull(option) ?: ""
        val event = InterfaceOption(
            interfaceComponent = "$id:$component",
            optionIndex = option,
            option = selectedOption,
            item = item,
            itemSlot = itemSlot,
        )
        Script.launch {
            InterfaceApi.option(player, event)
        }
    }
}
