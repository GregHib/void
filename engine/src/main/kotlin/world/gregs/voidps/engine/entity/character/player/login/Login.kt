package world.gregs.voidps.engine.entity.character.player.login

import io.netty.channel.Channel
import world.gregs.voidps.engine.entity.character.player.GameLoginInfo

data class Login(
    val name: String,
    val session: Channel? = null,
    val callback: ((LoginResponse) -> Unit)? = null,
    val data: GameLoginInfo? = null
) {

    fun respond(response: LoginResponse) {
        callback?.invoke(response)
    }
}