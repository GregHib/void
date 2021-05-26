package world.gregs.voidps.bot

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.contains

val Player.isBot: Boolean
    get() = contains("bot")