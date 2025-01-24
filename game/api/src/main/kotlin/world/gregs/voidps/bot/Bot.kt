package world.gregs.voidps.bot

import world.gregs.voidps.engine.entity.character.player.Player

val Player.isBot: Boolean
    get() = contains("bot")