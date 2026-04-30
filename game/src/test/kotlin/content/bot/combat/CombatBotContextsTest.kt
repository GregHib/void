package content.bot.combat

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class CombatBotContextsTest {

    @AfterEach
    fun teardown() {
        CombatBotContexts.clear()
    }

    @Test
    fun `find returns the context whose handles matches`() {
        val cw = StubContext(id = "clan_wars", prefix = "clan_wars_")
        val wild = StubContext(id = "wilderness", prefix = "wilderness_")
        CombatBotContexts.register(cw)
        CombatBotContexts.register(wild)

        val tier = tier("clan_wars_ffa_safe_zerker")
        assertSame(cw, CombatBotContexts.find(tier))
    }

    @Test
    fun `find returns null when no context handles the tier`() {
        CombatBotContexts.register(StubContext(id = "clan_wars", prefix = "clan_wars_"))
        assertNull(CombatBotContexts.find(tier("duel_arena_unstaked")))
    }

    @Test
    fun `forArenaKey routes to the context that exposes that key`() {
        val cw = StubContext(id = "clan_wars", prefix = "clan_wars_", arenaKeys = setOf("clan_wars_ffa_safe", "clan_wars_ffa_dangerous"))
        val wild = StubContext(id = "wilderness", prefix = "wilderness_", arenaKeys = setOf("wilderness_low", "wilderness_high"))
        CombatBotContexts.register(cw)
        CombatBotContexts.register(wild)

        assertSame(cw, CombatBotContexts.forArenaKey("clan_wars_ffa_safe"))
        assertSame(wild, CombatBotContexts.forArenaKey("wilderness_high"))
        assertNull(CombatBotContexts.forArenaKey("pest_control_easy"))
    }

    @Test
    fun `register rejects duplicate ids`() {
        CombatBotContexts.register(StubContext(id = "clan_wars", prefix = "a_"))
        assertThrows(IllegalArgumentException::class.java) {
            CombatBotContexts.register(StubContext(id = "clan_wars", prefix = "b_"))
        }
    }

    @Test
    fun `subscribedAreas unions every contexts interest set`() {
        CombatBotContexts.register(StubContext(id = "clan_wars", prefix = "clan_wars_", subscribed = setOf("clan_wars_teleport")))
        CombatBotContexts.register(StubContext(id = "wilderness", prefix = "wilderness_", subscribed = setOf("edgeville_lever", "ardougne_lever")))

        val areas = CombatBotContexts.subscribedAreas()
        assertEquals(setOf("clan_wars_teleport", "edgeville_lever", "ardougne_lever"), areas)
    }

    @Test
    fun `loadAll calls load on every registered context`() {
        val first = StubContext(id = "first", prefix = "f_")
        val second = StubContext(id = "second", prefix = "s_")
        CombatBotContexts.register(first)
        CombatBotContexts.register(second)

        CombatBotContexts.loadAll()
        assertTrue(first.loaded)
        assertTrue(second.loaded)
    }

    @Test
    fun `unregister removes a context so find no longer matches`() {
        val cw = StubContext(id = "clan_wars", prefix = "clan_wars_")
        CombatBotContexts.register(cw)
        assertNotNull(CombatBotContexts.find(tier("clan_wars_ffa_safe_zerker")))

        assertTrue(CombatBotContexts.unregister(cw))
        assertNull(CombatBotContexts.find(tier("clan_wars_ffa_safe_zerker")))
    }

    private fun tier(activityId: String) = CombatTier(activityId = activityId, levels = emptyMap(), style = "")

    private class StubContext(
        override val id: String,
        private val prefix: String,
        private val arenaKeys: Set<String> = emptySet(),
        private val subscribed: Set<String> = emptySet(),
    ) : CombatBotContext {
        var loaded = false
            private set

        override val subscribedAreas: Set<String> get() = subscribed
        override fun handles(tier: CombatTier): Boolean = tier.activityId.startsWith(prefix)
        override fun load() { loaded = true }
        override fun arenaKeys(): Set<String> = arenaKeys
        override fun arenaSpawn(arenaKey: String): Tile? = null
        override fun arenaTiers(arenaKey: String): List<CombatTier> = emptyList()
        override fun autospawnIntervalTicks(arenaKey: String): Int = 0
        override fun autospawnTarget(arenaKey: String): Int = 0
        override fun arenaContains(arenaKey: String, tier: CombatTier): Boolean = false
        override fun shouldDropItems(player: Player, tier: CombatTier): Boolean = false
        override fun respawnTile(tier: CombatTier): Tile? = null
    }
}
