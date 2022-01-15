package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.CancellableEvent

data class AddFriend(val friend: String) : CancellableEvent()