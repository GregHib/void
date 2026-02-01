package content.bot.action

import content.bot.fact.Condition

interface Behaviour {
    val id: String
    val requires: List<Condition>
    val resolve: List<Condition>
    val actions: List<BotAction>
    val produces: Set<Condition>
}