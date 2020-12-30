package rs.dusk.network.codec.login

/**
 * @author Greg Hibb
 * @since February 18, 2020
 */
enum class LoginResponseCode(val opcode: Int) {
    DataChange(0),
    Delay(1),//Video advertisement
    Successful(2),
    InvalidCredentials(3),
    AccountDisabled(4),
    AccountOnline(5),
    GameUpdated(6),
    WorldFull(7),
    LoginServerOffline(8),
    LoginLimitExceeded(9),//Too many connections from your address
    BadSessionId(10),
    LoginServerRejectedSession(11),//Extremely common insecure password
    MembersAccountRequired(12),
    CouldNotCompleteLogin(13),
    ServerBeingUpdated(14),
    Reconnecting(15),//Error connecting
    LoginAttemptsExceeded(16),//Too many incorrect login's from your address
    MembersOnlyArea(17),
    InvalidLoginServer(20),
    TransferringProfile(21)//Error connecting
}