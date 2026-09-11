package world.gregs.voidps.web.api.service

import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.Credentials
import world.gregs.voidps.web.api.model.Session

/**
 * Issues and resolves sessions. Every other service that needs to know who is calling is handed
 * an [world.gregs.voidps.web.api.model.AccountSummary] taken from a [Session], never a raw token.
 */
interface AuthService {

    /**
     * @throws ApiException.InvalidCredentials when the account is unknown or the password is wrong
     * @throws ApiException.AccountLocked when the account is banned or locked
     * @throws ApiException.RateLimited after too many failed attempts
     */
    suspend fun login(credentials: Credentials): Session

    /** Invalidates [token]. A no-op for a token that is already gone. */
    suspend fun logout(token: String)

    /** The session for [token], or null when it is unknown or expired. */
    suspend fun session(token: String): Session?

    /**
     * Extends [token]'s lifetime.
     *
     * @throws ApiException.Unauthorized when the token is unknown or expired
     */
    suspend fun refresh(token: String): Session
}
