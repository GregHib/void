package content.bot.action

import content.bot.fact.Condition
import content.bot.fact.Requirement

interface Behaviour {
    val id: String
    val requires: List<Requirement<*>>
    val setup: List<Requirement<*>>
    val actions: List<BotAction>
    val produces: Set<Requirement<*>>
}