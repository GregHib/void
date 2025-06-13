package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile
import kotlin.test.*

abstract class AccountStorageTest {

    abstract val storage: AccountStorage

    @Test
    fun `Store an account`() {
        storage.save(listOf(save))

        val account = storage.load(save.name)

        assertNotNull(account)
        assertEquals(save.name, account.name)
        assertEquals(save.password, account.password)
        assertEquals(save.tile, account.tile)
        assertContentEquals(save.experience, account.experience)
        assertContentEquals(save.blocked, account.blocked)
        assertContentEquals(save.levels, account.levels)
        assertEquals(save.male, account.male)
        assertContentEquals(save.looks, account.looks)
        assertContentEquals(save.colours, account.colours)
        assertEquals(save.variables.keys, account.variables.keys)
        for ((key, value) in save.variables) {
            assertEquals(value, account.variables[key])
        }
        assertEquals(save.inventories.keys, account.inventories.keys)
        for ((key, value) in save.inventories) {
            assertContentEquals(value, account.inventories[key])
        }
        assertEquals(save.friends.keys, account.friends.keys)
        for ((friend, rank) in save.friends) {
            assertEquals(rank, account.friends[friend])
        }
        assertContentEquals(save.ignores, account.ignores)
    }

    @Test
    fun `Store an existing account overrides data`() {
        storage.save(listOf(save))

        val override = save.copy(
            tile = Tile(1234, 5432, 1),
            experience = save.experience.clone().apply { this[0] = 10.0 },
            levels = save.levels.clone().apply { this[0] = 10 },
            blocked = save.blocked.toMutableList().apply {
                add(Skill.Attack)
                remove(Skill.Prayer)
            },
            male = false,
            variables = save.variables.toMutableMap().apply { remove("in_wilderness") },
            inventories = save.inventories.toMutableMap().apply { remove("bank") },
            friends = save.friends.toMutableMap().apply {
                put("Bob", ClanRank.Captain)
                remove("Greg")
            },
        )
        storage.save(listOf(override))

        val account = storage.load(save.name)

        assertNotNull(account)
        assertEquals(override.name, account.name)
        assertEquals(override.password, account.password)
        assertEquals(override.tile, account.tile)
        assertContentEquals(override.experience, account.experience)
        assertContentEquals(override.blocked, account.blocked)
        assertContentEquals(override.levels, account.levels)
        assertEquals(override.male, account.male)
        assertContentEquals(override.looks, account.looks)
        assertContentEquals(override.colours, account.colours)
        assertEquals(override.variables.keys, account.variables.keys)
        for ((key, value) in override.variables) {
            assertEquals(value, account.variables[key])
        }
        assertFalse(account.variables.containsKey("in_wilderness"))
        assertEquals(override.inventories.keys, account.inventories.keys)
        for ((key, value) in override.inventories) {
            assertContentEquals(value, account.inventories[key])
        }
        assertFalse(account.inventories.containsKey("bank"))
        assertEquals(override.friends.keys, account.friends.keys)
        for ((friend, rank) in override.friends) {
            assertEquals(rank, account.friends[friend])
        }
        assertFalse(account.friends.containsKey("Greg"))
        assertContentEquals(override.ignores, account.ignores)
    }

    @Test
    fun `Loading non-existent account returns null`() {
        assertNull(storage.load(save.name))
    }

    @Test
    fun `Check if account name exists`() {
        assertFalse(storage.exists(save.name))

        storage.save(listOf(save))

        assertTrue(storage.exists(save.name))
    }

    @Test
    fun `Load clan from stored account`() {
        storage.save(
            listOf(
                save.copy(
                    variables = save.variables.toMutableMap().apply {
                        remove("display_name")
                    },
                ),
            ),
        )

        val clans = storage.clans()

        assertEquals(1, clans.size)
        val clan = clans[save.name]
        assertNotNull(clan)
        assertEquals(save.name, clan.owner)
        assertEquals(save.name, clan.ownerDisplayName)
        assertEquals(save.variables["clan_name"], clan.name)
        assertEquals(save.friends.keys, clan.friends.keys)
        for ((friend, rank) in save.friends) {
            assertEquals(rank, clan.friends[friend])
        }
        assertContentEquals(save.ignores, clan.ignores)
        assertEquals(ClanRank.Owner, clan.joinRank)
        assertEquals(ClanRank.Friend, clan.talkRank)
        assertEquals(ClanRank.Owner, clan.kickRank)
        assertEquals(ClanRank.Friend, clan.lootRank)
        assertTrue(clan.coinShare)
        assertTrue(clan.members.isEmpty())
    }

    @Test
    fun `Load clan default values`() {
        storage.save(listOf(save.copy(variables = mapOf("display_name" to save.name))))

        val clans = storage.clans()

        assertEquals(1, clans.size)
        val clan = clans[save.name]
        assertNotNull(clan)
        assertEquals(save.name, clan.owner)
        assertEquals(save.name, clan.ownerDisplayName)
        assertEquals("", clan.name)
        assertEquals(ClanRank.Anyone, clan.joinRank)
        assertEquals(ClanRank.Anyone, clan.talkRank)
        assertEquals(ClanRank.Corporeal, clan.kickRank)
        assertEquals(ClanRank.None, clan.lootRank)
        assertFalse(clan.coinShare)
        assertTrue(clan.members.isEmpty())
    }

    @Test
    fun `Get names from stored account`() {
        val save = save.copy(
            variables = save.variables.toMutableMap().apply {
                this["name_history"] = listOf("oldest_name", "previous_name")
            },
        )
        storage.save(listOf(save))

        val names = storage.names()

        assertEquals(1, names.size)
        val name = names[save.name.lowercase()]
        assertNotNull(name)
        assertEquals(save.name, name.accountName)
        assertEquals(save.variables["display_name"], name.displayName)
        assertEquals("previous_name", name.previousName)
    }

    @Test
    fun `Load default name values`() {
        val save = save.copy(variables = emptyMap())
        storage.save(listOf(save))

        val names = storage.names()

        assertEquals(1, names.size)
        val name = names[save.name]
        assertNotNull(name)
        assertEquals(save.name, name.accountName)
        assertEquals(save.name, name.displayName)
        assertEquals("", name.previousName)

        storage.save(listOf(save.copy(variables = mapOf("name_history" to emptyList<String>()))))
        assertEquals("", storage.names()[save.name]!!.previousName)
    }

    @Test
    fun `No accounts gives empty names`() {
        assertTrue(storage.names().isEmpty())
    }

    @Test
    fun `No accounts gives empty clans`() {
        assertTrue(storage.clans().isEmpty())
    }

    companion object {
        val save = PlayerSave(
            name = "durial_321",
            password = "abcdefghijklmnopqrstuvwxyz0123456789",
            tile = Tile(2967, 3383, 0),
            experience = DoubleArray(25) { 14000000.0 }.apply {
                this[0] = 8771558.75
                this[1] = 4385776.5
                this[2] = 11805606.5
                this[3] = 8771558.75
                this[4] = 1986068.0
                this[6] = 7944614.25
            },
            blocked = listOf(Skill.Prayer),
            levels = IntArray(25) { 99 }.apply {
                this[0] = 95
                this[1] = 88
                this[2] = 98
                this[3] = 950
                this[4] = 80
                this[6] = 94
            },
            male = true,
            looks = intArrayOf(2, 14, 460, 592, 368, 624, 437),
            colours = intArrayOf(1, 93, 94, 4, 0),
            variables = mapOf(
                "display_name" to "Durial321",
                "in_pvp" to true,
                "in_wilderness" to true,
                "unlocked_music_0" to listOf("scape_summon", "scape_theme"),
                "unlocked_music_1" to listOf("harmony", "fanfare"),
                "xp_counter" to 309664028.75,
                "name_history" to emptyList<String>(),
                "clan_name" to "falador fun",
                "clan_talk_rank" to "Friend",
                "coin_share_setting" to true,
                "clan_loot_rank" to "Friend",
                "clan_kick_rank" to "Owner",
                "clan_join_rank" to "Owner",
                "meaning" to 42,
                "life" to 4124700000L,
                "favourite_numbers" to listOf(11, 42, 64),
            ),
            inventories = mapOf(
                "worn_equipment" to Array(14) { Item.EMPTY }.apply {
                    this[0] = Item("green_partyhat")
                    this[1] = Item("fire_cape")
                    this[2] = Item("amulet_of_fury")
                    this[3] = Item("abyssal_whip")
                    this[4] = Item("ahrims_robe_top")
                    this[5] = Item("toktz_ket_xil")
                    this[7] = Item("ahrims_robe_skirt")
                    this[9] = Item("culinaromancers_gloves_10")
                    this[10] = Item("rock_climbing_boots")
                    this[12] = Item("berserker_ring")
                },
                "inventory" to Array(28) { Item.EMPTY }.apply {
                    this[0] = Item("armadyl_godsword")
                    this[1] = Item("prayer_potion_4")
                },
                "bank" to Array(516) { Item.EMPTY }.apply {
                    this[0] = Item("coins", 420000000)
                },
            ),
            friends = mapOf("Greg" to ClanRank.Friend),
            ignores = listOf("Mod Murdoch"),
        )
    }
}
