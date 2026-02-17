package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.network.client.instruction.InteractDialogue
import kotlin.collections.indexOf

data class BotDialogueContinue(val option: String, val id: String, val success: Condition? = null) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (success != null && success.check(bot.player)) {
            return BehaviourState.Success
        }
        val split = id.split(":")
        if (split.size < 2) {
            return BehaviourState.Failed(Reason.Invalid("Invalid interface id '$id'."))
        }
        val (id, component) = split
        val item = split.getOrNull(2)
        val def = InterfaceDefinitions.getOrNull(id) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface id $id:$component:$item."))
        val componentId = InterfaceDefinitions.getComponentId(id, component) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface component $id:$component:$item."))
        val componentDef = InterfaceDefinitions.getComponent(id, component) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface component definition $id:$component:$item."))
        var options = componentDef.options
        if (options == null) {
            options = componentDef.getOrNull("options") ?: emptyArray()
        }
        val index = options.indexOf(option)
        val valid = world.execute(
            bot.player,
            InteractDialogue(
                interfaceId = def.id,
                componentId = componentId,
                option = index,
            ),
        )
        if (!valid) {
            return BehaviourState.Failed(Reason.Invalid("Invalid interaction: ${def.id}:$componentId option=$index."))
        }
        return BehaviourState.Wait(1, BehaviourState.Success)
    }
}
