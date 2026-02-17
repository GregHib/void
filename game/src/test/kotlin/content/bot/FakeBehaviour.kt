package content.bot

import content.bot.behaviour.Behaviour
import content.bot.behaviour.Condition
import content.bot.behaviour.action.BotAction

class FakeBehaviour : Behaviour {
    override val id: String = ""
    override val timeout: Int = 100
    override val requires: List<Condition> = emptyList()
    override val setup: List<Condition> = emptyList()
    override val actions: List<BotAction> = emptyList()
    override val produces: Set<String> = emptySet()
}
