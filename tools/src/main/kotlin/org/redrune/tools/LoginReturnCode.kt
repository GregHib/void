package org.redrune.tools

/**
 * Holds the return codes that can be sent to the client when attempting to login.
 *
 * @author Tyluur <itstyluur@gmail.com>
 */
enum class LoginReturnCode(val opcode: Int) {
    /**
     * An unexpected server response occured.
     */
    AD_COUNTDOWN_THEN_END(0),
    /**
     * Could not display advertisement video, logging in in x seconds.
     */
    COULD_NOT_DISPLAY_AD(1),
    /**
     * A successful login.
     */
    SUCCESSFUL(2),
    /**
     * Invalid username or password has been entered.
     */
    INVALID_CREDENTIALS(3),
    /**
     * This account is banned.
     */
    ACCOUNT_DISABLED(4),
    /**
     * This account is already logged in.
     */
    ALREADY_ONLINE(5),
    /**
     * We have updated and client needs to be reloaded.
     */
    UPDATED(6),
    /**
     * The world is full.
     */
    FULL_WORLD(7),
    /**
     * Login server is offline.
     */
    LOGIN_SERVER_OFFLINE(8),
    /**
     * The login limit has been exceeded.
     */
    LOGIN_LIMIT_EXCEEDED(9),
    /**
     * The session key was invalid.
     */
    BAD_SESSION_ID(10),
    /**
     * The password is too weak, and should be improved.
     */
    WEAK_PASSWORD(11),
    /**
     * When trying to connect to a members world while being f2p.
     */
    MEMBERS_WORLD(12),
    /**
     * Could not login.
     */
    BETA_TESTERS_ONLY(13),
    /**
     * The server is currently updating.
     */
    UPDATING(14),
    /**
     * Too many incorrect login attempts from your address.
     */
    TOO_MANY_INCORRECT_LOGINS(16),
    /**
     * We requested an account that was not created. <br></br> This return code opens the account creation screen
     */
    INVALID_ACCOUNT_REQUESTED(17),
    /**
     * This account is locked as it might have been stolen.
     */
    LOCKED(18),
    /**
     * When trying to use fullscreen to login on a free world.
     */
    FULLSCREEN_MEMBERS_ONLY(19),
    /**
     * The login server connected to is invalid.
     */
    INVALID_LOGIN_SERVER(20),
    /**
     * The username logged out recently
     */
    LOGGED_OUT_RECENTLY(21),
    /**
     * The login was malformed
     */
    MALFORMED_LOGIN_PACKET(22),
    /**
     * We couldn't get a reply
     */
    NO_REPLY_FROM_LOGIN_SERVER(23),
    /**
     * When the player's saved file created, but is unable to be loaded.
     */
    ERROR_LOADING_PROFILE(24),
    /**
     * This computer address is disabled as it was used to break our rules.
     */
    IP_BANNED(26);

}
