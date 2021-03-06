package world.gregs.voidps.engine.entity.character.player.login

import world.gregs.voidps.engine.entity.character.player.GameLoginInfo
import world.gregs.voidps.network.ClientSession

data class Login(
    val name: String,
    val session: ClientSession? = null,
    val callback: ((LoginResponse) -> Unit)? = null,
    val data: GameLoginInfo? = null
) {

    fun respond(response: LoginResponse) {
        callback?.invoke(response)
    }
}