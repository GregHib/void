package rs.dusk.world.interact.entity.player.spawn.login

import io.netty.channel.Channel
import rs.dusk.engine.entity.character.player.GameLoginInfo
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

data class Login(val name: String, val session: Channel? = null, val callback: ((LoginResponse) -> Unit)? = null, val data: GameLoginInfo? = null) : Event<Unit>() {

    fun respond(response: LoginResponse) {
        callback?.invoke(response)
    }

    companion object : EventCompanion<Login>
}