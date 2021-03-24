package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.Event

data class Command(val prefix: String, val content: String) : Event