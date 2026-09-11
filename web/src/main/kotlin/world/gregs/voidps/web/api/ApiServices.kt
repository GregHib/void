package world.gregs.voidps.web.api

import world.gregs.voidps.web.api.service.AccountService
import world.gregs.voidps.web.api.service.AuthService
import world.gregs.voidps.web.api.service.DevPlayerService
import world.gregs.voidps.web.api.service.ExchangeService
import world.gregs.voidps.web.api.service.HiscoreService
import world.gregs.voidps.web.api.service.PlayerService
import world.gregs.voidps.web.api.service.TelemetryService
import world.gregs.voidps.web.api.service.WorldService

/**
 * Everything the HTTP layer needs to serve the API. Implementations are supplied by the host — the
 * game server binds these to live world state, a test binds fakes — so nothing under `api/route`
 * depends on where the data comes from.
 */
class ApiServices(
    val auth: AuthService,
    val accounts: AccountService,
    val hiscores: HiscoreService,
    val players: PlayerService,
    val exchange: ExchangeService,
    val worlds: WorldService,
    val telemetry: TelemetryService,
    val devPlayers: DevPlayerService,
)
