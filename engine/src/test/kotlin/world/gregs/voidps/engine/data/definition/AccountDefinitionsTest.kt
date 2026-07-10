package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank

class AccountDefinitionsTest {

    private lateinit var definitions: AccountDefinitions

    @BeforeEach
    fun setUp() {
        definitions = AccountDefinitions()
    }

    @Test
    fun `Merge adds new definitions and display names`() {
        val definition = definition("new_account", "Newbie", hash = "hash")

        val count = definitions.merge(mapOf("new_account" to definition), emptyMap()) { false }

        assertEquals(1, count)
        assertSame(definition, definitions.get("Newbie"))
        assertEquals("Newbie", definitions.displayNames["new_account"])
    }

    @Test
    fun `Merge updates existing definition in place`() {
        val existing = definition("account", "Old name", hash = "old_hash")
        definitions.merge(mapOf("account" to existing), emptyMap()) { false }

        val count = definitions.merge(mapOf("account" to definition("account", "New name", hash = "new_hash")), emptyMap()) { false }

        assertEquals(1, count)
        val updated = definitions.get("New name")
        assertSame(existing, updated)
        assertEquals("New name", updated?.displayName)
        assertEquals("new_hash", updated?.passwordHash)
        assertEquals("New name", definitions.displayNames["account"])
    }

    @Test
    fun `Merge skips unchanged definitions`() {
        definitions.merge(mapOf("account" to definition("account", "Name", hash = "hash")), emptyMap()) { false }

        val count = definitions.merge(mapOf("account" to definition("account", "Name", hash = "hash")), emptyMap()) { false }

        assertEquals(0, count)
    }

    @Test
    fun `Merge skips accounts matching predicate`() {
        val existing = definition("online_account", "Online", hash = "memory_hash")
        definitions.merge(mapOf("online_account" to existing), emptyMap()) { false }

        val count = definitions.merge(mapOf("online_account" to definition("online_account", "Online", hash = "storage_hash")), emptyMap()) { account ->
            account == "online_account"
        }

        assertEquals(0, count)
        assertEquals("memory_hash", definitions.get("Online")?.passwordHash)
    }

    @Test
    fun `Merge never removes definitions absent from storage`() {
        definitions.merge(mapOf("account" to definition("account", "Name", hash = "hash")), emptyMap()) { false }

        definitions.merge(emptyMap(), emptyMap()) { false }

        assertEquals("hash", definitions.get("Name")?.passwordHash)
    }

    @Test
    fun `Merge adds new clans`() {
        val clan = clan("owner_account", "Owner")

        definitions.merge(emptyMap(), mapOf("owner_account" to clan)) { false }

        assertSame(clan, definitions.clan("owner_account"))
    }

    @Test
    fun `Merge updates existing clan in place preserving members`() {
        val existing = clan("owner_account", "Owner")
        val member = Player(accountName = "member_account")
        existing.members.add(member)
        definitions.merge(emptyMap(), mapOf("owner_account" to existing)) { false }

        val update = clan("owner_account", "Owner", name = "New clan", joinRank = ClanRank.Friend, friends = mapOf("friend" to ClanRank.Recruit))
        definitions.merge(emptyMap(), mapOf("owner_account" to update)) { false }

        val merged = definitions.clan("owner_account")
        assertSame(existing, merged)
        assertEquals("New clan", merged?.name)
        assertEquals(ClanRank.Friend, merged?.joinRank)
        assertEquals(mapOf("friend" to ClanRank.Recruit), merged?.friends)
        assertEquals(listOf(member), merged?.members)
    }

    @Test
    fun `Merge skips clans whose owner matches predicate`() {
        val clan = clan("online_account", "Online")

        definitions.merge(emptyMap(), mapOf("online_account" to clan)) { account ->
            account == "online_account"
        }

        assertNull(definitions.clan("online_account"))
    }

    private fun definition(account: String, display: String, hash: String) = AccountDefinition(
        accountName = account,
        displayName = display,
        previousName = "",
        passwordHash = hash,
    )

    private fun clan(
        owner: String,
        display: String,
        name: String = "",
        joinRank: ClanRank = ClanRank.Anyone,
        friends: Map<String, ClanRank> = emptyMap(),
    ) = Clan(
        owner = owner,
        ownerDisplayName = display,
        name = name,
        friends = friends,
        ignores = emptyList(),
        joinRank = joinRank,
    )
}
