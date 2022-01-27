package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.CancellableEvent

data class DeleteIgnore(var name: String) : CancellableEvent()