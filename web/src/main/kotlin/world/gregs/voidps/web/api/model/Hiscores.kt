@file:UseSerializers(InstantSerializer::class)

package world.gregs.voidps.web.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

/**
 * The skill caps, boss list, account types and team sizes the hiscores page renders its filters
 * from. Serving these replaces the duplicated constants in `Hiscores.kt` and `hiscores.js`.
 */
@Serializable
data class HiscoresMetadata(
    val updatedAt: Instant,
    val nextUpdateAt: Instant? = null,
    val maxTrackedXp: Long = 200_000_000,
    val skills: List<SkillInfo>,
    val bosses: List<BossInfo>,
    val modes: List<ModeInfo>,
    val teamSizes: List<Int>,
)

/** [iconUrl] is served so callers never special-case Constitution's `hitpoints.png` sprite. */
@Serializable
data class SkillInfo(
    val id: String,
    val name: String,
    val maxLevel: Int,
    val iconUrl: String? = null,
)

@Serializable
data class BossInfo(
    val id: String,
    val name: String,
    val baseTimeSeconds: Int? = null,
)

@Serializable
data class ModeInfo(
    val id: AccountMode,
    val name: String,
)

/** Filters shared by the overall and per-skill leaderboards. A null [mode] or [name] is no filter. */
data class LeaderboardQuery(
    val name: String? = null,
    val mode: AccountMode? = null,
    val page: PageRequest = PageRequest(),
)

@Serializable
data class OverallLeaderboard(
    val pagination: Pagination,
    val updatedAt: Instant,
    val items: List<OverallRank>,
)

@Serializable
data class OverallRank(
    val rank: Int,
    val name: String,
    val mode: AccountMode,
    val totalLevel: Int,
    val totalXp: Long,
)

@Serializable
data class SkillLeaderboard(
    val skill: String,
    val skillName: String,
    val maxLevel: Int,
    val pagination: Pagination,
    val items: List<SkillRank>,
)

@Serializable
data class SkillRank(
    val rank: Int,
    val name: String,
    val level: Int,
    val xp: Long,
)

@Serializable
data class BossKillLeaderboard(
    val boss: String,
    val bossName: String,
    val pagination: Pagination,
    val items: List<BossKillRank>,
)

@Serializable
data class BossKillRank(
    val rank: Int,
    val name: String,
    val kills: Int,
)

@Serializable
data class BossTimeLeaderboard(
    val boss: String,
    val bossName: String,
    val pagination: Pagination,
    val items: List<BossTimeRank>,
)

/** [name] is the submitting account; [team] lists every member, the submitter included. */
@Serializable
data class BossTimeRank(
    val rank: Int,
    val name: String,
    val teamSize: Int,
    val team: List<String> = emptyList(),
    val timeSeconds: Double,
    val achievedAt: Instant? = null,
)

@Serializable
data class Comparison(
    val a: ComparisonSide,
    val b: ComparisonSide,
    val summary: List<ComparisonCard>,
    val skills: List<SkillComparison>,
    val bosses: List<BossComparison>,
)

@Serializable
data class ComparisonSide(
    val name: String,
    val mode: AccountMode,
    val rank: Int,
    val totalLevel: Int,
    val totalXp: Long,
    val bossKills: Int = 0,
)

/** One of the four cards above the comparison tables. [leader] is null when the two are level. */
@Serializable
data class ComparisonCard(
    val label: String,
    val leader: String?,
    val detail: String,
)

@Serializable
enum class ComparisonLeader(val wire: String) {
    @SerialName("a")
    A("a"),

    @SerialName("b")
    B("b"),

    @SerialName("tie")
    Tie("tie"),
}

@Serializable
data class SkillComparison(
    val skill: String,
    val skillName: String,
    val a: SkillEntry,
    val b: SkillEntry,
    val leader: ComparisonLeader,
    val differenceXp: Long,
)

@Serializable
data class SkillEntry(
    val level: Int,
    val xp: Long,
)

@Serializable
data class BossComparison(
    val boss: String,
    val bossName: String,
    val aKills: Int,
    val bKills: Int,
    val leader: ComparisonLeader,
    val differenceKills: Int,
)
