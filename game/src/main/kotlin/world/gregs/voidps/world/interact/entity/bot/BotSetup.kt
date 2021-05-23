package world.gregs.voidps.world.interact.entity.bot

import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.utility.get

val Player.isBot: Boolean
    get() = contains("bot")

fun Player.initBot() {
    val bot = Bot(this)
    get<EventHandlerStore>().populate(Bot::class, bot.botEvents)
    this["bot"] = bot
    val e = mutableListOf<Event>()
    this["events"] = e
    events.all = {
        e.add(it)
    }
}