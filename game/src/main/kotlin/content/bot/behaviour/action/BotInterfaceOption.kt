package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Condition
import content.bot.behaviour.Reason
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.instruction.InteractInterface
import kotlin.collections.indexOf

data class BotInterfaceOption(val option: String, val id: String, val success: Condition? = null) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState? {
        if (success != null && success.check(bot.player)) {
            return BehaviourState.Success
        }
        val definitions = get<InterfaceDefinitions>()
        val split = id.split(":")
        if (split.size < 2) {
            return BehaviourState.Failed(Reason.Invalid("Invalid interface id '$id'."))
        }
        val (id, component) = split
        val item = split.getOrNull(2)
        val def = definitions.getOrNull(id) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface id $id:$component:$item."))
        val componentId = definitions.getComponentId(id, component) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface component $id:$component:$item."))
        val componentDef = definitions.getComponent(id, component) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface component definition $id:$component:$item."))
        var options = componentDef.options
        if (options == null) {
            options = componentDef.getOrNull("options") ?: emptyArray()
        }
        val index = options.indexOf(option)
        if (index == -1) {
            return BehaviourState.Failed(Reason.Invalid("No interface option $option for $id:$component:$item options=${options.contentToString()}."))
        }
        val itemDef = if (item != null) ItemDefinitions.getOrNull(item) else null

        var inv = InterfaceHandler.getInventory(bot.player, id, component, componentDef)
        if (inv != null && component == "sample") {
            inv = "${inv}_sample"
        }
        var itemSlot = if (item != null && inv != null) bot.player.inventories.inventory(inv).indexOf(item) else -1
        var itemId = itemDef?.id ?: -1
        if (id == "shop") {
            itemId = -1
            itemSlot *= 6
        }
        val valid = world.execute(
            bot.player,
            InteractInterface(
                interfaceId = def.id,
                componentId = componentId,
                itemId = itemId,
                itemSlot = itemSlot,
                option = index,
            ),
        )
        return when {
            !valid -> BehaviourState.Failed(Reason.Invalid("Invalid interaction: ${def.id}:$componentId:${itemDef?.stringId} slot $itemSlot option $index."))
            success == null -> BehaviourState.Wait(1, BehaviourState.Success)
            success.check(bot.player) -> BehaviourState.Success
            else -> BehaviourState.Running
        }
    }
}
