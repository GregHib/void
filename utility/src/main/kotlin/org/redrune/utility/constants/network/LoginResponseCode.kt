package org.redrune.utility.constants.network

/**
 * @author Greg Hibb
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
sealed class LoginResponseCode(val opcode: Int) {
    object DataChange : LoginResponseCode(0)
    object Delay : LoginResponseCode(1)//Video advertisement
    object Successful : LoginResponseCode(2)
    object InvalidCredentials : LoginResponseCode(3)
    object AccountDisabled : LoginResponseCode(4)
    object AccountOnline : LoginResponseCode(5)
    object GameUpdated : LoginResponseCode(6)
    object WorldFull : LoginResponseCode(7)
    object LoginServerOffline : LoginResponseCode(8)
    object LoginLimitExceeded : LoginResponseCode(9)//too many connections from your address
    object BadSessionId : LoginResponseCode(10)
    object LoginServerRejectedSession : LoginResponseCode(11)//Extremely common insecure password
    object MembersAccountRequired : LoginResponseCode(12)
    object CouldNotCompleteLogin : LoginResponseCode(13)
    object ServerBeingUpdated : LoginResponseCode(14)
    object Reconnecting : LoginResponseCode(15)//Error connecting
    object LoginAttemptsExceeded : LoginResponseCode(16)//Too many incorrect login's from your address
    object MembersOnlyArea : LoginResponseCode(17)
    object InvalidLoginServer : LoginResponseCode(20)
    object TransferringProfile : LoginResponseCode(21)//Error connecting
}