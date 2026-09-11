package world.gregs.voidps.web.api.service

import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.AccountSummary
import world.gregs.voidps.web.api.model.AuditEntry
import world.gregs.voidps.web.api.model.BankPage
import world.gregs.voidps.web.api.model.ChatChannel
import world.gregs.voidps.web.api.model.ChatMessage
import world.gregs.voidps.web.api.model.CommandAccepted
import world.gregs.voidps.web.api.model.DevPlayerDetail
import world.gregs.voidps.web.api.model.DevPlayerQuery
import world.gregs.voidps.web.api.model.DevPlayerSkills
import world.gregs.voidps.web.api.model.DevPlayerSummary
import world.gregs.voidps.web.api.model.EquipmentSlot
import world.gregs.voidps.web.api.model.Inventory
import world.gregs.voidps.web.api.model.ModerationRequest
import world.gregs.voidps.web.api.model.ModerationState
import world.gregs.voidps.web.api.model.Page
import world.gregs.voidps.web.api.model.PageRequest
import world.gregs.voidps.web.api.model.PlayerAction
import world.gregs.voidps.web.api.model.PlayerVariable
import world.gregs.voidps.web.api.model.TeleportRequest
import world.gregs.voidps.web.api.model.VariableList
import world.gregs.voidps.web.api.model.VariableScope
import java.time.Instant

/**
 * The player workbench. Staff only.
 *
 * Two things run through the whole interface. Reads take the calling [AccountSummary] so the
 * implementation can redact by rank — an IP is masked below administrator, and reading another
 * account's private chat is itself recorded. Writes take it because every one of them is audited,
 * and the entry they produce is what [audit] later returns.
 *
 * The writes act on live game state, so they hand work to the game loop and return once it is
 * queued rather than once it has happened — hence [CommandAccepted] instead of a result.
 *
 * [playerId] is the account id, or the lowercase hyphenated display name.
 */
interface DevPlayerService {

    suspend fun search(query: DevPlayerQuery): Page<DevPlayerSummary>

    /** @throws ApiException.NotFound when no such account exists */
    suspend fun player(playerId: String, viewer: AccountSummary): DevPlayerDetail

    suspend fun skills(playerId: String): DevPlayerSkills

    /** Every slot, empty ones included. */
    suspend fun equipment(playerId: String): List<EquipmentSlot>

    suspend fun inventory(playerId: String): Inventory

    suspend fun bank(playerId: String, tab: Int? = null, page: PageRequest = PageRequest()): BankPage

    /**
     * Varbits, varps, attributes and flags. Filtering happens here rather than in the client so the
     * panel's count stays right for accounts with more variables than one response carries.
     *
     * A null [scope] or blank [query] is no filter.
     */
    suspend fun variables(playerId: String, query: String? = null, scope: VariableScope? = null): VariableList

    suspend fun activity(playerId: String, since: Instant? = null, page: PageRequest = PageRequest()): Page<PlayerAction>

    suspend fun audit(playerId: String, page: PageRequest = PageRequest()): Page<AuditEntry>

    suspend fun chat(
        playerId: String,
        channel: ChatChannel? = null,
        since: Instant? = null,
        page: PageRequest = PageRequest(),
        viewer: AccountSummary,
    ): Page<ChatMessage>

    /** The account's full live state as one blob, for pasting into a bug report. */
    suspend fun debugDump(playerId: String, viewer: AccountSummary): String

    /**
     * @throws ApiException.PlayerOffline when the account is not connected
     */
    suspend fun kick(playerId: String, reason: String?, staff: AccountSummary): CommandAccepted

    /**
     * @throws ApiException.PlayerOffline when the account is not connected
     * @throws ApiException.Validation when the coordinates are outside the map
     */
    suspend fun teleport(playerId: String, request: TeleportRequest, staff: AccountSummary): CommandAccepted

    /** Moves [staff]'s own character to the account and keeps it there. */
    suspend fun follow(playerId: String, staff: AccountSummary): CommandAccepted

    suspend fun unfollow(playerId: String, staff: AccountSummary)

    /** Active flags, plus the reasons and durations [staff]'s rank may apply. */
    suspend fun moderation(playerId: String, staff: AccountSummary): ModerationState

    /**
     * @throws ApiException.Forbidden when [staff]'s rank does not permit the action
     * @throws ApiException.Conflict when the account already has, or already lacks, the flag
     */
    suspend fun moderate(playerId: String, request: ModerationRequest, staff: AccountSummary): AuditEntry
}
