package world.gregs.voidps.web.api.service

import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.Account
import world.gregs.voidps.web.api.model.AccountSettings
import world.gregs.voidps.web.api.model.AccountUpdate
import world.gregs.voidps.web.api.model.PasswordChange

/**
 * The signed-in account's own details. Every method takes the caller's own id — these routes never
 * read another account, so there is no viewer parameter to check.
 */
interface AccountService {

    suspend fun account(accountId: String): Account

    /**
     * @throws ApiException.Conflict when the name or email is taken, or a rename cooldown is active
     * @throws ApiException.Validation when a field is malformed
     */
    suspend fun update(accountId: String, update: AccountUpdate): Account

    /**
     * Invalidates every other session on success.
     *
     * @throws ApiException.Unauthorized when the current password is wrong
     * @throws ApiException.Validation when the new password is too weak
     */
    suspend fun changePassword(accountId: String, change: PasswordChange)

    suspend fun settings(accountId: String): AccountSettings

    suspend fun updateSettings(accountId: String, settings: AccountSettings): AccountSettings
}
