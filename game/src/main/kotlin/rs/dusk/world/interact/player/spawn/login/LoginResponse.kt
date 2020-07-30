package rs.dusk.world.interact.player.spawn.login

import rs.dusk.engine.model.entity.character.player.Player

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
sealed class LoginResponse(val code: Int) {
    object DataChange : LoginResponse(0)
    object VideoAd : LoginResponse(1)
    data class Success(val player: Player) : LoginResponse(2)
    object InvalidCredentials : LoginResponse(3)
    object AccountDisabled : LoginResponse(4)
    object AccountOnline : LoginResponse(5)
    object GameUpdate : LoginResponse(6)
    object WorldFull : LoginResponse(7)
    object LoginServerOffline : LoginResponse(8)
    object LoginLimitExceeded : LoginResponse(9)
    object BadSessionId : LoginResponse(10)
    object LoginServerRejectedSession : LoginResponse(11)
    object MembersAccountRequired : LoginResponse(12)
    object CouldNotCompleteLogin : LoginResponse(13)
    object ServerBeingUpdated : LoginResponse(14)
    object Reconnecting : LoginResponse(15)
    object LoginAttemptsExceeded : LoginResponse(16)
    object MembersOnlyArea : LoginResponse(17)
    object InvalidLoginServer : LoginResponse(20)
    object TransferringProfile : LoginResponse(21)
}