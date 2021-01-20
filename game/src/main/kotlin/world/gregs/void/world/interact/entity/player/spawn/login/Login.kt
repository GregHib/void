package world.gregs.void.world.interact.entity.player.spawn.login

import io.netty.channel.Channel
import world.gregs.void.engine.entity.character.player.GameLoginInfo
import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventCompanion

data class Login(val name: String, val session: Channel? = null, val callback: ((LoginResponse) -> Unit)? = null, val data: GameLoginInfo? = null) : Event<Unit>() {

    fun respond(response: LoginResponse) {
        callback?.invoke(response)
    }

    companion object : EventCompanion<Login>
}