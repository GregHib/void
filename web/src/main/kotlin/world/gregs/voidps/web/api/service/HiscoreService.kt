package world.gregs.voidps.web.api.service

import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.AccountMode
import world.gregs.voidps.web.api.model.BossKillLeaderboard
import world.gregs.voidps.web.api.model.BossTimeLeaderboard
import world.gregs.voidps.web.api.model.Comparison
import world.gregs.voidps.web.api.model.HiscoresMetadata
import world.gregs.voidps.web.api.model.LeaderboardQuery
import world.gregs.voidps.web.api.model.OverallLeaderboard
import world.gregs.voidps.web.api.model.PageRequest
import world.gregs.voidps.web.api.model.SkillLeaderboard

/**
 * Leaderboards and head-to-head comparison. Player profiles are not here — the hiscores player
 * view reads [PlayerService], the same resource the adventurer's log renders.
 *
 * Ranks are snapshots, recalculated on a schedule rather than per request; [metadata] carries the
 * stamp the page prints.
 */
interface HiscoreService {

    /** Skill caps, tracked bosses, account types, team sizes and the last recalculation time. */
    suspend fun metadata(): HiscoresMetadata

    suspend fun overall(query: LeaderboardQuery): OverallLeaderboard

    /** @throws ApiException.NotFound when [skill] is not a tracked skill id */
    suspend fun skill(skill: String, query: LeaderboardQuery): SkillLeaderboard

    /** @throws ApiException.NotFound when [boss] is not a tracked boss id */
    suspend fun bossKills(boss: String, mode: AccountMode?, page: PageRequest): BossKillLeaderboard

    /**
     * Fastest recorded clears. A null [teamSize] returns every size.
     *
     * @throws ApiException.NotFound when [boss] is not a tracked boss id
     */
    suspend fun bossTimes(boss: String, teamSize: Int?, page: PageRequest): BossTimeLeaderboard

    /**
     * Both sides from one snapshot, so the deltas are internally consistent.
     *
     * @throws ApiException.NotFound when either player is unknown or has a private profile
     */
    suspend fun compare(playerA: String, playerB: String): Comparison
}
