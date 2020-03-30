package org.redrune.utility.constants.network

/**
 * @author Greg Hibb
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
enum class LoginResponseCodes(val opcode: Int) {
    DATA_CHANGE(0),
    DELAY(1),//Video advertisement
    SUCCESSFUL(2),
    INVALID_CREDENTIALS(3),
    ACCOUNT_DISABLED(4),
    ACCOUNT_ONLINE(5),
    GAME_UPDATED(6),
    WORLD_FULL(7),
    LOGIN_SERVER_OFFLINE(8),
    LOGIN_LIMIT_EXCEEDED(9),//too many connections from your address
    BAD_SESSION_ID(10),
    LOGIN_SERVER_REJECTED_SESSION(11),//Extremely common insecure password
    MEMBERS_ACCOUNT_REQUIRED(12),
    COULD_NOT_COMPLETE_LOGIN(13),
    SERVER_BEING_UPDATED(14),
    RECONNECTING(15),//Error connecting
    LOGIN_ATTEMPTS_EXCEEDED(16),//Too many incorrect login's from your address
    MEMBERS_ONLY_AREA(17),
    INVALID_LOGIN_SERVER(20),
    TRANSFERRING_PROFILE(21);//Error connecting
}