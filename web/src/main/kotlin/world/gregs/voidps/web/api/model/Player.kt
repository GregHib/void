@file:UseSerializers(InstantSerializer::class)

package world.gregs.voidps.web.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

/** A typeahead hit — the compare combo boxes and the log's "Find a log" panel both render these. */
@Serializable
data class PlayerSuggestion(
    val name: String,
    val rank: Int? = null,
    val totalLevel: Int = 0,
    val totalXp: Long = 0,
    val world: Int? = null,
    val mode: AccountMode = AccountMode.Standard,
    val clan: String? = null,
)

/**
 * The account header shared by the adventurer's log hero band and the hiscores player view.
 * A null [world] means offline.
 */
@Serializable
data class PlayerProfile(
    val name: String,
    val member: Boolean,
    val mode: AccountMode,
    val world: Int? = null,
    val gameMode: String? = null,
    val clan: String? = null,
    val overallRank: Int? = null,
    val totalLevel: Int,
    val totalXp: Long,
    val combatLevel: Int,
    val questPoints: Int,
    val questPointsMax: Int,
    val maxedSkills: Int = 0,
    val bossKills: Int = 0,
    val timePlayedHours: Double = 0.0,
    val joinedAt: Instant,
    val lastSeenAt: Instant? = null,
    val milestones: List<Milestone> = emptyList(),
)

/** A pre-formatted label/value pair for the log's Milestones panel. */
@Serializable
data class Milestone(
    val label: String,
    val value: String,
)

@Serializable
data class PlayerSkills(
    val totalLevel: Int,
    val totalXp: Long,
    val maxedCount: Int,
    val items: List<PlayerSkill>,
)

/** [progressPercent] is level over cap, resolved here so every meter on the site agrees. */
@Serializable
data class PlayerSkill(
    val skill: String,
    val name: String,
    val level: Int,
    val maxLevel: Int,
    val xp: Long,
    val rank: Int? = null,
    val progressPercent: Int,
    val iconUrl: String? = null,
)

/** How the log's Skills panel is ordered. */
enum class SkillSort(val wire: String) {
    Level("level"),
    Name("name"),
    ;

    companion object {
        fun of(value: String?): SkillSort = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) } ?: Level
    }
}

@Serializable
data class PlayerBosses(
    val totalKills: Int,
    val items: List<PlayerBoss>,
)

/** A null [rank] renders as "unranked"; a null [fastestSeconds] as an em dash. */
@Serializable
data class PlayerBoss(
    val boss: String,
    val name: String,
    val kills: Int,
    val rank: Int? = null,
    val fastestSeconds: Double? = null,
    val lastKillAt: Instant? = null,
)

@Serializable
data class PlayerQuests(
    val completed: Int,
    val total: Int,
    val questPoints: Int,
    val questPointsMax: Int,
    val items: List<PlayerQuest>,
)

@Serializable
data class PlayerQuest(
    val id: String,
    val name: String,
    val difficulty: QuestDifficulty,
    val status: QuestStatus,
    val questPoints: Int = 0,
    val durationMinutes: Int? = null,
    val completedAt: Instant? = null,
)

@Serializable
enum class QuestDifficulty(val wire: String) {
    @SerialName("novice")
    Novice("novice"),

    @SerialName("intermediate")
    Intermediate("intermediate"),

    @SerialName("experienced")
    Experienced("experienced"),

    @SerialName("master")
    Master("master"),

    @SerialName("grandmaster")
    Grandmaster("grandmaster"),
}

@Serializable
enum class QuestStatus(val wire: String) {
    @SerialName("complete")
    Complete("complete"),

    @SerialName("started")
    Started("started"),

    @SerialName("not-started")
    NotStarted("not-started"),
}

/** The `status` query on the quest log. [All] passes every quest through. */
enum class QuestFilter(val wire: String) {
    All("all"),
    Complete("complete"),
    Incomplete("incomplete"),
    ;

    companion object {
        fun of(value: String?): QuestFilter = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) } ?: Complete
    }
}

/** An entry in the log's "Recent activity" feed. */
@Serializable
data class PlayerEvent(
    val id: String,
    val type: PlayerEventType,
    val text: String,
    val occurredAt: Instant,
)

/** Matches the activity panel's filter tabs. A null filter is the "All" tab. */
@Serializable
enum class PlayerEventType(val wire: String) {
    @SerialName("skill")
    Skill("skill"),

    @SerialName("quest")
    Quest("quest"),

    @SerialName("combat")
    Combat("combat"),

    @SerialName("account")
    Account("account"),
    ;

    companion object {
        fun of(value: String?): PlayerEventType? = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) }
    }
}
