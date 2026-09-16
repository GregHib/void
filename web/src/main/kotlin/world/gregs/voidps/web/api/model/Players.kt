package world.gregs.voidps.web.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerProfile(
    val name: String,
    val mode: String,
    val overallRank: Int?,
    val totalLevel: Int,
    val totalXp: Long,
    val combatLevel: Int,
    val bossKills: Int,
    val joinedAt: String?,
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
