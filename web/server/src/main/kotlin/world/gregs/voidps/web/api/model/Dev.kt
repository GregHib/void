package world.gregs.voidps.web.api.model

import kotlinx.serialization.Serializable

@Serializable
data class DevTile(val x: Int, val y: Int, val plane: Int)

/** A row in the staff player search results. */
@Serializable
data class DevPlayerSearchRow(val name: String, val rights: String, val meta: String)

@Serializable
data class DevPlayerSearchResult(val items: List<DevPlayerSearchRow>)

/**
 * The account's saved state, as of its last write to disk. There is no live connection from the
 * webserver into a running game world, so this can't report whether the account is currently
 * online, its live position, or anything else that only exists in a [world.gregs.voidps.engine.entity.character.player.Player]'s
 * in-memory state - everything here is reconstructed from [world.gregs.voidps.engine.data.PlayerSave].
 */
@Serializable
data class DevPlayerOverview(
    val name: String,
    val rights: String,
    val combatLevel: Int,
    val totalLevel: Int,
    val totalXp: Long,
    val questPoints: Int,
    val bossKills: Int,
    val tile: DevTile,
    val joinedAt: String?,
    val lastSeenAt: String?,
    val playtimeHours: Double,
)

@Serializable
data class DevSkillRow(val skill: String, val name: String, val level: Int, val maxLevel: Int, val xp: Long, val iconUrl: String)

@Serializable
data class DevPlayerSkills(val totalLevel: Int, val totalXp: Long, val items: List<DevSkillRow>)

@Serializable
data class DevItemStack(val slot: Int, val id: String, val name: String, val amount: Int)

@Serializable
data class DevEquipmentRow(val slot: String, val item: String)

@Serializable
data class DevPlayerInventories(
    val equipment: List<DevEquipmentRow>,
    val inventory: List<DevItemStack>,
    val inventorySize: Int,
    val bank: List<DevItemStack>,
)

@Serializable
data class DevVariableRow(val key: String, val value: String, val type: String)

@Serializable
data class DevPlayerVariables(val items: List<DevVariableRow>)

@Serializable
data class DevEventRow(val occurredAt: String, val title: String, val description: String)

@Serializable
data class DevPlayerEvents(val pagination: Pagination, val items: List<DevEventRow>)
