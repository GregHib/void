package world.gregs.voidps.engine.entity.character.player.chat.friend

import world.gregs.voidps.engine.event.CancellableEvent

data class AddFriend(val friend: String) : CancellableEvent()