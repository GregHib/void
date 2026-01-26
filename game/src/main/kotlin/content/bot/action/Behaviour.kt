package content.bot.action

import content.bot.req.Requirement

interface Behaviour {
    val id: String
    val requirements: List<Requirement>
    val plan: List<BotAction>
}