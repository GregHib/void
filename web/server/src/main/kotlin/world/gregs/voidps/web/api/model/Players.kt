package world.gregs.voidps.web.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Milestone(val label: String, val value: String)

@Serializable
data class PlayerProfile(
    val name: String,
    val mode: String,
    /** "none", "mod" or "admin" - mirrors [world.gregs.voidps.engine.entity.character.player.PlayerRights]. */
    val rights: String,
    val overallRank: Int?,
    val totalLevel: Int,
    val totalXp: Long,
    val combatLevel: Int,
    val questPoints: Int,
    val questPointsMax: Int,
    val maxedSkills: Int,
    val bossKills: Int,
    val timePlayedHours: Double,
    val joinedAt: String?,
    val lastSeenAt: String?,
    val milestones: List<Milestone>,
)

@Serializable
data class PlayerSkillRow(
    val skill: String,
    val name: String,
    val level: Int,
    val maxLevel: Int,
    val xp: Long,
    val rank: Int?,
    val progressPercent: Int,
    val iconUrl: String,
)

@Serializable
data class PlayerSkills(val totalLevel: Int, val totalXp: Long, val maxedCount: Int, val items: List<PlayerSkillRow>)

@Serializable
data class PlayerBossRow(val boss: String, val name: String, val kills: Int, val rank: Int?, val fastestSeconds: Double?)

@Serializable
data class PlayerBosses(val totalKills: Int, val items: List<PlayerBossRow>)

@Serializable
data class PlayerQuestRow(
    val id: String,
    val name: String,
    val difficulty: String,
    val status: String,
    val questPoints: Int,
    val completedAt: String?,
)

@Serializable
data class PlayerQuests(val completed: Int, val total: Int, val questPoints: Int, val questPointsMax: Int, val items: List<PlayerQuestRow>)

@Serializable
data class PlayerEventRow(val id: String, val type: String, val text: String, val occurredAt: String)

@Serializable
data class PlayerEventPage(val pagination: Pagination, val items: List<PlayerEventRow>)
