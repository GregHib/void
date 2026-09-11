@file:UseSerializers(InstantSerializer::class)

package world.gregs.voidps.web.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant
import kotlin.time.Duration

/** A row in the player workbench's Results list. */
@Serializable
data class DevPlayerSummary(
    val id: String,
    val name: String,
    val accountNumber: String? = null,
    val state: PlayerState,
    val rank: PlayerRank,
    val world: Int? = null,
    val sessionSeconds: Long? = null,
)

/**
 * The workbench header, Location panel and Current activity panel. Repeats [DevPlayerSummary]'s
 * fields rather than nesting it — the spec composes the two with `allOf`.
 */
@Serializable
data class DevPlayerDetail(
    val id: String,
    val name: String,
    val accountNumber: String? = null,
    val state: PlayerState,
    val rank: PlayerRank,
    val world: Int? = null,
    val sessionSeconds: Long? = null,
    val ip: String? = null,
    val hostname: String? = null,
    val client: String? = null,
    val lastLoginAt: Instant? = null,
    val createdAt: Instant,
    val location: PlayerLocation,
    val activity: PlayerActivity,
)

@Serializable
data class PlayerLocation(
    val region: String,
    val x: Int,
    val y: Int,
    val plane: Int,
    val wildernessLevel: Int? = null,
)

/** [xpProgressPercent] backs the session xp-rate meter. */
@Serializable
data class PlayerActivity(
    val summary: String,
    val detail: String? = null,
    val xpPerHour: Int? = null,
    val xpProgressPercent: Int? = null,
)

@Serializable
enum class PlayerState(val wire: String) {
    @SerialName("online")
    Online("online"),

    @SerialName("offline")
    Offline("offline"),

    @SerialName("restarting")
    Restarting("restarting"),
    ;

    companion object {
        fun of(value: String?): PlayerState? = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) }
    }
}

@Serializable
enum class PlayerRank(val wire: String) {
    @SerialName("free")
    Free("free"),

    @SerialName("member")
    Member("member"),

    @SerialName("moderator")
    Moderator("moderator"),

    @SerialName("administrator")
    Administrator("administrator"),
}

/** Workbench search filters. [text] matches name, account id, IP prefix and world number. */
data class DevPlayerQuery(
    val text: String? = null,
    val state: PlayerState? = null,
    val world: Int? = null,
    val page: PageRequest = PageRequest(),
)

@Serializable
data class DevPlayerSkills(
    val totalLevel: Int,
    val combatLevel: Int,
    val combat: CombatState,
    val skills: List<DevSkill>,
)

@Serializable
data class CombatState(
    val wildernessLevel: Int? = null,
    val specialEnergyPercent: Int = 100,
    val prayerPoints: Int = 0,
    val prayerPointsMax: Int = 0,
    val hitpoints: Int = 0,
    val hitpointsMax: Int = 0,
)

/** [boostedLevel] is only set when boosts or drains put it out of step with [level]. */
@Serializable
data class DevSkill(
    val skill: String,
    val name: String,
    val level: Int,
    val maxLevel: Int = 99,
    val xp: Long = 0,
    val boostedLevel: Int? = null,
)

/** One worn slot. A null [itemName] is an empty slot — the panel prints an em dash. */
@Serializable
data class EquipmentSlot(
    val slot: EquipmentSlotType,
    val itemId: String? = null,
    val itemName: String? = null,
    val quantity: Int = 0,
    val iconUrl: String? = null,
)

@Serializable
enum class EquipmentSlotType(val wire: String) {
    @SerialName("head")
    Head("head"),

    @SerialName("cape")
    Cape("cape"),

    @SerialName("amulet")
    Amulet("amulet"),

    @SerialName("weapon")
    Weapon("weapon"),

    @SerialName("body")
    Body("body"),

    @SerialName("shield")
    Shield("shield"),

    @SerialName("legs")
    Legs("legs"),

    @SerialName("hands")
    Hands("hands"),

    @SerialName("feet")
    Feet("feet"),

    @SerialName("ring")
    Ring("ring"),

    @SerialName("ammunition")
    Ammunition("ammunition"),
}

/** [slots] holds every slot in order, empty ones included, so indices line up with the grid. */
@Serializable
data class Inventory(
    val used: Int,
    val capacity: Int,
    val slots: List<InventorySlot>,
)

@Serializable
data class InventorySlot(
    val index: Int,
    val itemId: String? = null,
    val itemName: String? = null,
    val quantity: Int = 0,
    val iconUrl: String? = null,
)

@Serializable
data class BankPage(
    val pagination: Pagination,
    val totalValue: Long,
    val items: List<BankItem>,
)

/** A null [value] means the item is untradeable, so it has no exchange price. */
@Serializable
data class BankItem(
    val itemId: String,
    val itemName: String,
    val quantity: Long,
    val tab: Int = 0,
    val value: Long? = null,
)

/** Varbits, varps, attributes, config and flags, flattened into one addressable list. */
@Serializable
data class PlayerVariable(
    val key: String,
    val label: String? = null,
    val value: String,
    val type: VariableType,
    val scope: VariableScope,
    val updatedAt: Instant? = null,
)

@Serializable
enum class VariableType(val wire: String) {
    @SerialName("varbit")
    Varbit("varbit"),

    @SerialName("varp")
    Varp("varp"),

    @SerialName("int")
    Int("int"),

    @SerialName("double")
    Double("double"),

    @SerialName("boolean")
    Boolean("boolean"),

    @SerialName("string")
    String("string"),
}

@Serializable
enum class VariableScope(val wire: String) {
    @SerialName("account")
    Account("account"),

    @SerialName("session")
    Session("session"),

    @SerialName("world")
    World("world"),
    ;

    companion object {
        fun of(value: String?): VariableScope? = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) }
    }
}

/** [total] is the count before filtering, so the panel can say "N shown" against it. */
@Serializable
data class VariableList(
    val items: List<PlayerVariable>,
    val total: Int,
)

@Serializable
data class PlayerAction(
    val at: Instant,
    val action: String,
    val detail: String? = null,
)

/** A staff action against an account. Moderation posts create one of these. */
@Serializable
data class AuditEntry(
    val id: String,
    val action: String,
    val reason: String? = null,
    val note: String? = null,
    val staff: String,
    val at: Instant,
    val expiresAt: Instant? = null,
)

@Serializable
data class ChatMessage(
    val at: Instant,
    val channel: ChatChannel,
    val text: String,
    val recipient: String? = null,
)

@Serializable
enum class ChatChannel(val wire: String) {
    @SerialName("public")
    Public("public"),

    @SerialName("clan")
    Clan("clan"),

    @SerialName("private")
    Private("private"),

    @SerialName("game")
    Game("game"),
    ;

    companion object {
        fun of(value: String?): ChatChannel? = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) }
    }
}

/** Empty [flags] is the Moderation panel's "no active flags". */
@Serializable
data class ModerationState(
    val flags: List<ModerationFlag>,
    val reasons: List<ModerationReason>,
    val durations: List<ModerationDuration>,
)

@Serializable
data class ModerationFlag(
    val type: ModerationFlagType,
    val reason: String? = null,
    val staff: String? = null,
    val since: Instant,
    val expiresAt: Instant? = null,
)

@Serializable
enum class ModerationFlagType(val wire: String) {
    @SerialName("mute")
    Mute("mute"),

    @SerialName("jail")
    Jail("jail"),

    @SerialName("ban")
    Ban("ban"),
}

@Serializable
data class ModerationReason(
    val id: String,
    val label: String,
)

/** A null [value] means permanent. */
@Serializable
data class ModerationDuration(
    val value: Duration? = null,
    val label: String,
)

@Serializable
data class ModerationRequest(
    val action: ModerationAction,
    val reason: String,
    val duration: Duration? = null,
    val note: String? = null,
)

@Serializable
enum class ModerationAction(val wire: String) {
    @SerialName("mute")
    Mute("mute"),

    @SerialName("unmute")
    Unmute("unmute"),

    @SerialName("jail")
    Jail("jail"),

    @SerialName("unjail")
    Unjail("unjail"),

    @SerialName("ban")
    Ban("ban"),

    @SerialName("unban")
    Unban("unban"),
}

@Serializable
data class TeleportRequest(
    val x: Int? = null,
    val y: Int? = null,
    val plane: Int = 0,
    val home: Boolean = false,
)

@Serializable
data class KickRequest(val reason: String? = null)

/**
 * The result of a queued in-world action. Game actions run on the next tick, so these report that
 * the request was accepted rather than that it finished.
 */
@Serializable
data class CommandAccepted(
    val accepted: Boolean,
    val message: String? = null,
    val auditId: String? = null,
)
