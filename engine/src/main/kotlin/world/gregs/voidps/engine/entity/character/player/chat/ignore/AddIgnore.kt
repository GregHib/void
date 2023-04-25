package world.gregs.voidps.engine.entity.character.player.chat.ignore

import world.gregs.voidps.engine.event.CancellableEvent

data class AddIgnore(val name: String) : CancellableEvent()