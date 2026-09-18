package world.gregs.voidps.web.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Pagination(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
) {
    companion object {
        fun of(page: Int, pageSize: Int, total: Int) = Pagination(
            page = page,
            pageSize = pageSize,
            total = total,
            totalPages = if (total == 0) 1 else (total + pageSize - 1) / pageSize,
            hasPrevious = page > 0,
            hasNext = (page + 1) * pageSize < total,
        )
    }
}

@Serializable
data class SkillMetadata(val id: String, val name: String, val maxLevel: Int, val iconUrl: String)

@Serializable
data class BossMetadata(val id: String, val name: String)

@Serializable
data class ModeMetadata(val id: String, val name: String)

@Serializable
data class HiscoresMetadata(
    val updatedAt: String,
    val maxTrackedXp: Long,
    val skills: List<SkillMetadata>,
    val bosses: List<BossMetadata>,
    val modes: List<ModeMetadata>,
    val teamSizes: List<Int>,
)

@Serializable
data class OverallRow(val rank: Int, val name: String, val mode: String, val totalLevel: Int, val totalXp: Long)

@Serializable
data class OverallLeaderboard(val pagination: Pagination, val updatedAt: String, val items: List<OverallRow>)

@Serializable
data class SkillRow(val rank: Int, val name: String, val level: Int, val xp: Long)

@Serializable
data class SkillLeaderboard(
    val skill: String,
    val skillName: String,
    val maxLevel: Int,
    val pagination: Pagination,
    val items: List<SkillRow>,
)

@Serializable
data class BossKillRow(val rank: Int, val name: String, val kills: Int)

@Serializable
data class BossKillLeaderboard(val boss: String, val bossName: String, val pagination: Pagination, val items: List<BossKillRow>)

@Serializable
data class BossTimeRow(val rank: Int, val name: String, val teamSize: Int, val timeSeconds: Double)

@Serializable
data class BossTimeLeaderboard(val boss: String, val bossName: String, val pagination: Pagination, val items: List<BossTimeRow>)

@Serializable
data class SkillEntry(val level: Int, val xp: Long)

@Serializable
data class CompareSkillRow(val skill: String, val skillName: String, val a: SkillEntry, val b: SkillEntry, val leader: String, val differenceXp: Long)

@Serializable
data class CompareBossRow(val boss: String, val bossName: String, val aKills: Int, val bKills: Int, val leader: String, val differenceKills: Int)

@Serializable
data class ComparisonSide(val name: String, val mode: String, val rank: Int, val totalLevel: Int, val totalXp: Long, val bossKills: Int)

@Serializable
data class ComparisonSummaryCard(val label: String, val leader: String?, val detail: String)

@Serializable
data class Comparison(
    val a: ComparisonSide,
    val b: ComparisonSide,
    val summary: List<ComparisonSummaryCard>,
    val skills: List<CompareSkillRow>,
    val bosses: List<CompareBossRow>,
)

@Serializable
data class PlayerSuggestion(val name: String, val rank: Int?, val totalLevel: Int, val totalXp: Long, val mode: String)
