package content.bot.action

import content.bot.fact.Fact

interface Behaviour {
    val id: String
    val requires: List<Fact>
    val plan: List<BotAction>
    val produces: Set<Fact>
}