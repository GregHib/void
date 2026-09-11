package world.gregs.voidps.web.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import world.gregs.voidps.web.api.model.Account
import world.gregs.voidps.web.api.model.AccountMode
import world.gregs.voidps.web.api.model.AccountRole
import world.gregs.voidps.web.api.model.AccountSettings
import world.gregs.voidps.web.api.model.AccountSummary
import world.gregs.voidps.web.api.model.AccountUpdate
import world.gregs.voidps.web.api.model.AuditEntry
import world.gregs.voidps.web.api.model.BankPage
import world.gregs.voidps.web.api.model.BossKillLeaderboard
import world.gregs.voidps.web.api.model.BossTimeLeaderboard
import world.gregs.voidps.web.api.model.ChatChannel
import world.gregs.voidps.web.api.model.ChatMessage
import world.gregs.voidps.web.api.model.CommandAccepted
import world.gregs.voidps.web.api.model.CommandResult
import world.gregs.voidps.web.api.model.Comparison
import world.gregs.voidps.web.api.model.ConsoleCommand
import world.gregs.voidps.web.api.model.ConsoleLine
import world.gregs.voidps.web.api.model.Credentials
import world.gregs.voidps.web.api.model.DevPlayerDetail
import world.gregs.voidps.web.api.model.DevPlayerQuery
import world.gregs.voidps.web.api.model.DevPlayerSkills
import world.gregs.voidps.web.api.model.DevPlayerSummary
import world.gregs.voidps.web.api.model.DevStats
import world.gregs.voidps.web.api.model.EquipmentSlot
import world.gregs.voidps.web.api.model.ErrorEntry
import world.gregs.voidps.web.api.model.ErrorQuery
import world.gregs.voidps.web.api.model.HiscoresMetadata
import world.gregs.voidps.web.api.model.Inventory
import world.gregs.voidps.web.api.model.ItemCategory
import world.gregs.voidps.web.api.model.ItemDetail
import world.gregs.voidps.web.api.model.ItemQuery
import world.gregs.voidps.web.api.model.ItemSummary
import world.gregs.voidps.web.api.model.LeaderboardQuery
import world.gregs.voidps.web.api.model.MarketHighlights
import world.gregs.voidps.web.api.model.MarketSummary
import world.gregs.voidps.web.api.model.MetricsHistory
import world.gregs.voidps.web.api.model.MetricsSample
import world.gregs.voidps.web.api.model.ModerationRequest
import world.gregs.voidps.web.api.model.ModerationState
import world.gregs.voidps.web.api.model.OverallLeaderboard
import world.gregs.voidps.web.api.model.Page
import world.gregs.voidps.web.api.model.PageRequest
import world.gregs.voidps.web.api.model.PlayerAction
import world.gregs.voidps.web.api.model.PlayerBosses
import world.gregs.voidps.web.api.model.PlayerEvent
import world.gregs.voidps.web.api.model.PlayerEventType
import world.gregs.voidps.web.api.model.PlayerProfile
import world.gregs.voidps.web.api.model.PlayerQuests
import world.gregs.voidps.web.api.model.PlayerSkills
import world.gregs.voidps.web.api.model.PlayerSuggestion
import world.gregs.voidps.web.api.model.PlayerVariable
import world.gregs.voidps.web.api.model.PriceHistory
import world.gregs.voidps.web.api.model.PriceSnapshot
import world.gregs.voidps.web.api.model.QuestFilter
import world.gregs.voidps.web.api.model.RuntimeInfo
import world.gregs.voidps.web.api.model.Session
import world.gregs.voidps.web.api.model.SkillLeaderboard
import world.gregs.voidps.web.api.model.SkillSort
import world.gregs.voidps.web.api.model.TeleportRequest
import world.gregs.voidps.web.api.model.TickStats
import world.gregs.voidps.web.api.model.Timeframe
import world.gregs.voidps.web.api.model.VariableList
import world.gregs.voidps.web.api.model.VariableScope
import world.gregs.voidps.web.api.model.World
import world.gregs.voidps.web.api.model.WorldList
import world.gregs.voidps.web.api.model.WorldStatus
import world.gregs.voidps.web.api.service.AccountService
import world.gregs.voidps.web.api.service.AuthService
import world.gregs.voidps.web.api.service.DevPlayerService
import world.gregs.voidps.web.api.service.ExchangeService
import world.gregs.voidps.web.api.service.HiscoreService
import world.gregs.voidps.web.api.service.PlayerService
import world.gregs.voidps.web.api.service.TelemetryService
import world.gregs.voidps.web.api.service.WorldService
import java.time.Instant
import kotlin.time.Duration

/**
 * Minimal service implementations for [ApiRoutesTest]. Only the methods the tests reach are
 * implemented; the rest fail loudly, so a test that drifts onto an unexercised path says so rather
 * than quietly passing against a default.
 */
object Fakes {

    const val PLAYER_TOKEN = "token-player"
    const val STAFF_TOKEN = "token-staff"

    private val EPOCH: Instant = Instant.parse("2026-09-11T12:00:00Z")

    /** Records what the last `/events` call resolved its filter to, for the enum-parsing tests. */
    var lastEventType: PlayerEventType? = null

    private val player = AccountSummary(
        id = "1",
        displayName = "Thornwake",
        email = "thornwake@voidps.dev",
        roles = setOf(AccountRole.Player),
        member = true,
    )

    private val staff = AccountSummary(
        id = "2",
        displayName = "rotce",
        email = "rotce@voidps.dev",
        roles = setOf(AccountRole.Administrator),
    )

    fun services() = ApiServices(
        auth = Auth,
        accounts = Accounts,
        hiscores = Hiscores,
        players = Players.also { lastEventType = null },
        exchange = Exchange,
        worlds = Worlds,
        telemetry = Telemetry,
        devPlayers = DevPlayers,
    )

    private object Auth : AuthService {
        override suspend fun login(credentials: Credentials): Session {
            if (credentials.password != "hunter2") {
                throw ApiException.InvalidCredentials()
            }
            return Session(PLAYER_TOKEN, EPOCH.plusSeconds(3600), player)
        }

        override suspend fun logout(token: String) = Unit

        override suspend fun session(token: String): Session? = when (token) {
            PLAYER_TOKEN -> Session(PLAYER_TOKEN, EPOCH.plusSeconds(3600), player)
            STAFF_TOKEN -> Session(STAFF_TOKEN, EPOCH.plusSeconds(3600), staff)
            else -> null
        }

        override suspend fun refresh(token: String): Session = session(token) ?: throw ApiException.Unauthorized()
    }

    private object Accounts : AccountService {
        override suspend fun account(accountId: String) = Account(
            id = player.id,
            displayName = player.displayName,
            email = player.email,
            roles = player.roles,
            member = true,
            createdAt = EPOCH,
            mode = AccountMode.Standard,
        )

        override suspend fun update(accountId: String, update: AccountUpdate): Account = TODO("not exercised")

        override suspend fun changePassword(accountId: String, change: world.gregs.voidps.web.api.model.PasswordChange) = TODO("not exercised")

        override suspend fun settings(accountId: String): AccountSettings = TODO("not exercised")

        override suspend fun updateSettings(accountId: String, settings: AccountSettings): AccountSettings = TODO("not exercised")
    }

    private object Hiscores : HiscoreService {
        override suspend fun metadata(): HiscoresMetadata = TODO("not exercised")

        override suspend fun overall(query: LeaderboardQuery): OverallLeaderboard = TODO("not exercised")

        override suspend fun skill(skill: String, query: LeaderboardQuery): SkillLeaderboard =
            throw ApiException.NotFound("skill", skill)

        override suspend fun bossKills(boss: String, mode: AccountMode?, page: PageRequest): BossKillLeaderboard = TODO("not exercised")

        override suspend fun bossTimes(boss: String, teamSize: Int?, page: PageRequest): BossTimeLeaderboard = TODO("not exercised")

        override suspend fun compare(playerA: String, playerB: String): Comparison = TODO("not exercised")
    }

    private object Players : PlayerService {
        override suspend fun search(query: String?, limit: Int): List<PlayerSuggestion> = emptyList()

        override suspend fun profile(name: String, viewer: AccountSummary?): PlayerProfile = TODO("not exercised")

        override suspend fun skills(name: String, sort: SkillSort, viewer: AccountSummary?): PlayerSkills = TODO("not exercised")

        override suspend fun bosses(name: String, viewer: AccountSummary?): PlayerBosses = TODO("not exercised")

        override suspend fun quests(name: String, status: QuestFilter, viewer: AccountSummary?): PlayerQuests = TODO("not exercised")

        override suspend fun events(
            name: String,
            type: PlayerEventType?,
            since: Instant?,
            page: PageRequest,
            viewer: AccountSummary?,
        ): Page<PlayerEvent> {
            lastEventType = type
            return Page.of(page, total = 42, items = listOf(PlayerEvent("1", PlayerEventType.Skill, "Reached level 99 Cooking.", EPOCH)))
        }
    }

    private object Exchange : ExchangeService {
        override suspend fun summary(): MarketSummary = TODO("not exercised")

        override suspend fun categories(): List<ItemCategory> = TODO("not exercised")

        override suspend fun highlights(limit: Int): MarketHighlights = TODO("not exercised")

        override suspend fun search(query: ItemQuery): Page<ItemSummary> = Page.empty(query.page)

        override suspend fun item(itemId: String): ItemDetail = TODO("not exercised")

        override suspend fun history(itemId: String, timeframe: Timeframe): PriceHistory = TODO("not exercised")

        override suspend fun related(itemId: String, limit: Int): List<ItemSummary> = TODO("not exercised")

        override suspend fun prices(itemIds: List<String>): PriceSnapshot = TODO("not exercised")
    }

    private object Worlds : WorldService {
        override suspend fun worlds(status: WorldStatus?) = WorldList(
            items = listOf(
                World(
                    number = 9,
                    region = "Germany - Falkenstein",
                    members = true,
                    mode = "PvP",
                    players = 812,
                    capacity = 2000,
                    ping = 38,
                    status = WorldStatus.Online,
                ),
            ),
            totalPlayers = 812,
        )
    }

    private object Telemetry : TelemetryService {
        override suspend fun stats(world: Int?) = DevStats(
            playersOnline = 1284,
            uptimeSeconds = 940_000,
            startedAt = EPOCH,
            cpuPercent = 34.0,
            cores = 8,
            heapUsedBytes = 6_500_000_000,
            heapMaxBytes = 12_000_000_000,
            tick = TickStats(currentMs = 38.0, averageMs = 41.0),
        )

        override suspend fun metrics(world: Int?, samples: Int, interval: Duration): MetricsHistory = TODO("not exercised")

        override fun metricsStream(world: Int?): Flow<MetricsSample> = emptyFlow()

        override suspend fun runtime(): RuntimeInfo = TODO("not exercised")

        override suspend fun errors(query: ErrorQuery): Page<ErrorEntry> = Page.empty(query.page)

        override suspend fun console(world: Int?, limit: Int): List<ConsoleLine> = emptyList()

        override fun consoleStream(world: Int?): Flow<ConsoleLine> = emptyFlow()

        override suspend fun run(command: ConsoleCommand, staff: AccountSummary): CommandResult = TODO("not exercised")
    }

    private object DevPlayers : DevPlayerService {
        override suspend fun search(query: DevPlayerQuery): Page<DevPlayerSummary> = Page.empty(query.page)

        override suspend fun player(playerId: String, viewer: AccountSummary): DevPlayerDetail = TODO("not exercised")

        override suspend fun skills(playerId: String): DevPlayerSkills = TODO("not exercised")

        override suspend fun equipment(playerId: String): List<EquipmentSlot> = emptyList()

        override suspend fun inventory(playerId: String): Inventory = TODO("not exercised")

        override suspend fun bank(playerId: String, tab: Int?, page: PageRequest): BankPage = TODO("not exercised")

        override suspend fun variables(playerId: String, query: String?, scope: VariableScope?): VariableList =
            VariableList(emptyList<PlayerVariable>(), 0)

        override suspend fun activity(playerId: String, since: Instant?, page: PageRequest): Page<PlayerAction> = Page.empty(page)

        override suspend fun audit(playerId: String, page: PageRequest): Page<AuditEntry> = Page.empty(page)

        override suspend fun chat(
            playerId: String,
            channel: ChatChannel?,
            since: Instant?,
            page: PageRequest,
            viewer: AccountSummary,
        ): Page<ChatMessage> = Page.empty(page)

        override suspend fun debugDump(playerId: String, viewer: AccountSummary): String = "dump"

        override suspend fun kick(playerId: String, reason: String?, staff: AccountSummary): CommandAccepted = CommandAccepted(true)

        override suspend fun teleport(playerId: String, request: TeleportRequest, staff: AccountSummary): CommandAccepted = CommandAccepted(true)

        override suspend fun follow(playerId: String, staff: AccountSummary): CommandAccepted = CommandAccepted(true)

        override suspend fun unfollow(playerId: String, staff: AccountSummary) = Unit

        override suspend fun moderation(playerId: String, staff: AccountSummary): ModerationState = TODO("not exercised")

        override suspend fun moderate(playerId: String, request: ModerationRequest, staff: AccountSummary): AuditEntry = TODO("not exercised")
    }
}
