package world.gregs.voidps.world.interact.entity.bot

import world.gregs.voidps.ai.Context
import world.gregs.voidps.ai.Decision
import world.gregs.voidps.engine.entity.character.player.Player

class BotContext(val bot: Player) : Context {
    override var last: Decision<*, *>? = null
}