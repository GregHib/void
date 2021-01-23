package world.gregs.voidps.world.interact.entity.player.spawn.login

import io.netty.channel.Channel
import world.gregs.voidps.engine.entity.character.player.GameLoginInfo
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventCompanion

data class Login(val name: String, val session: Channel? = null, val callback: ((LoginResponse) -> Unit)? = null, val data: GameLoginInfo? = null) : Event<Unit>() {

    fun respond(response: LoginResponse) {
        callback?.invoke(response)
    }

    companion object : EventCompanion<Login>
}