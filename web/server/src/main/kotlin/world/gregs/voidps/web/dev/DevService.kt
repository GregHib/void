package world.gregs.voidps.web.dev

import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.web.api.model.*
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.combatLevel
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.displayMax
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.displayName
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.id
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.joinedAt
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.playtimeSeconds
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.questPoints
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.rights
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.skillLevel
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.skillXp
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.totalLevel
import world.gregs.voidps.web.hiscores.HiscoresService.Companion.totalXp
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Backs the staff player-management panel from [Storage] on demand. Everything here reflects the
 * account's last save to disk - the webserver has no live connection into a running game world,
 * so there's no way to tell whether the account is currently online, where it is right now, or
 * anything else that only exists in an in-memory `Player` (session state, live chat, moderation
 * actions). Those would need a channel from the game engine into this module that doesn't exist
 * yet; see the class doc on [DevPlayerOverview].
 */
class DevService(
    private val storage: Storage,
) {

    fun searchPlayers(query: String?, limit: Int): DevPlayerSearchResult {
        val accounts = storage.accounts()
        val q = query?.trim()?.lowercase()
        val rows = accounts
            .filter { q.isNullOrEmpty() || it.displayName().lowercase().contains(q) || it.name.lowercase().contains(q) }
            .sortedBy { it.displayName().lowercase() }
            .take(limit)
            .map { save ->
                DevPlayerSearchRow(
                    name = save.displayName(),
                    rights = save.rights(),
                    meta = save.lastSeenLabel(),
                )
            }
        return DevPlayerSearchResult(rows)
    }

    fun overview(name: String): DevPlayerOverview? {
        val save = storage.accounts().find(name) ?: return null
        return DevPlayerOverview(
            name = save.displayName(),
            rights = save.rights(),
            combatLevel = save.combatLevel(),
            totalLevel = save.totalLevel(),
            totalXp = save.totalXp(),
            questPoints = save.questPoints(),
            bossKills = save.kills.values.sum(),
            tile = DevTile(save.tile.x, save.tile.y, save.tile.level),
            joinedAt = save.joinedAt(),
            lastSeenAt = save.lastSeenAt(),
            playtimeHours = save.playtimeSeconds() / 3600.0,
        )
    }

    fun skills(name: String): DevPlayerSkills? {
        val save = storage.accounts().find(name) ?: return null
        val items = Skill.all.map { skill ->
            DevSkillRow(
                skill = skill.id(),
                name = skill.name,
                level = save.skillLevel(skill),
                maxLevel = skill.displayMax(),
                xp = save.skillXp(skill),
                iconUrl = "void/images/skills/${skill.name.lowercase()}.png",
            )
        }
        return DevPlayerSkills(totalLevel = save.totalLevel(), totalXp = save.totalXp(), items = items)
    }

    fun inventories(name: String): DevPlayerInventories? {
        val save = storage.accounts().find(name) ?: return null
        val equipment = (save.inventories["worn_equipment"] ?: emptyArray()).mapIndexedNotNull { index, item ->
            val slot = EQUIPMENT_SLOTS[index] ?: return@mapIndexedNotNull null
            DevEquipmentRow(slot = slot, item = if (item.isEmpty()) "—" else itemName(item))
        }
        val inventory = save.inventories["inventory"] ?: emptyArray()
        val bank = save.inventories["bank"] ?: emptyArray()
        return DevPlayerInventories(
            equipment = equipment,
            inventory = inventory.mapIndexedNotNull { index, item -> if (item.isEmpty()) null else DevItemStack(index, item.id, itemName(item), item.amount) },
            inventorySize = inventory.size,
            bank = bank.mapIndexedNotNull { index, item -> if (item.isEmpty()) null else DevItemStack(index, item.id, itemName(item), item.amount) },
        )
    }

    fun variables(name: String, query: String?): DevPlayerVariables? {
        val save = storage.accounts().find(name) ?: return null
        val q = query?.trim()?.lowercase()
        val rows = save.variables.entries
            .filter { (key, _) -> q.isNullOrEmpty() || key.lowercase().contains(q) }
            .sortedBy { it.key }
            .map { (key, value) -> DevVariableRow(key = key, value = value.toString(), type = typeOf(value)) }
        return DevPlayerVariables(rows)
    }

    fun events(name: String, page: Int, pageSize: Int): DevPlayerEvents? {
        val save = storage.accounts().find(name) ?: return null
        val sorted = save.recentEvents.sortedByDescending { it.time }
        val from = (page * pageSize).coerceIn(0, sorted.size)
        val to = (from + pageSize).coerceIn(from, sorted.size)
        val items = sorted.subList(from, to).map { event ->
            DevEventRow(
                occurredAt = Instant.ofEpochSecond(event.time.toLong()).toString(),
                title = event.title,
                description = event.description,
            )
        }
        return DevPlayerEvents(pagination = Pagination.of(page, pageSize, sorted.size), items = items)
    }

    private fun itemName(item: Item): String = ItemDefinitions.definitions.getOrNull(ItemDefinitions.ids[item.id] ?: -1)?.name?.takeIf { it.isNotBlank() && it != "null" } ?: item.id

    private fun typeOf(value: Any): String = when (value) {
        is Boolean -> "boolean"
        is Int, is Long -> "int"
        is Double, is Float -> "double"
        is List<*> -> "list"
        is Map<*, *> -> "map"
        else -> "string"
    }

    private fun List<PlayerSave>.find(name: String): PlayerSave? =
        firstOrNull { it.displayName().equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true) }

    companion object {
        /** Indexed by [world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot.index]; unused indices (6, 8, 11) are gaps in that enum. */
        private val EQUIPMENT_SLOTS = mapOf(
            0 to "Head", 1 to "Cape", 2 to "Amulet", 3 to "Weapon", 4 to "Body", 5 to "Shield",
            7 to "Legs", 9 to "Hands", 10 to "Feet", 12 to "Ring", 13 to "Ammunition",
        )

        private fun PlayerSave.lastSeenAt(): String? {
            val event = recentEvents.maxByOrNull { it.time } ?: return null
            return Instant.ofEpochSecond(event.time.toLong()).toString()
        }

        private fun PlayerSave.lastSeenLabel(): String {
            val at = lastSeenAt() ?: return "no activity recorded"
            return "last active " + DateTimeFormatter.ofPattern("d MMM yyyy").withZone(ZoneOffset.UTC).format(Instant.parse(at))
        }
    }
}
