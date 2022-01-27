package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.CancellableEvent

data class DeleteFriend(val friend: String) : CancellableEvent()