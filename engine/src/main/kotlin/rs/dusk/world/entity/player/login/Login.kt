package rs.dusk.world.entity.player.login

import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

data class Login(val name: String, val session: Session? = null, val callback: ((LoginResponse) -> Unit)? = null) : Event<Unit>() {

    fun respond(response: LoginResponse) {
        callback?.invoke(response)
    }

    companion object : EventCompanion<Login>
}