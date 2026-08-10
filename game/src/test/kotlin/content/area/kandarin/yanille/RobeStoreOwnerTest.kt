package content.area.kandarin.yanille

import WorldTest
import content.entity.npc.shop.shop
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill

internal class RobeStoreOwnerTest : WorldTest() {

    @Test
    fun `Base shop below 99 magic`() {
        val player = createPlayer(emptyTile, "base")
        val owner = createNPC("robe_store_owner_yanille", emptyTile.addY(1))
        player.npcOption(owner, "Trade")
        tick(4)
        assertEquals("magic_guild_store_mystic_robes", player.shop())
    }

    @Test
    fun `Skillcape shop with 99 magic`() {
        val player = createPlayer(emptyTile, "mage")
        player.experience.set(Skill.Magic, 14_000_000.0)
        val owner = createNPC("robe_store_owner_yanille", emptyTile.addY(1))
        player.npcOption(owner, "Trade")
        tick(4)
        assertEquals("magic_guild_store_mystic_robes_skillcape", player.shop())
    }

    @Test
    fun `Trimmed shop with a second 99 skill`() {
        val player = createPlayer(emptyTile, "maxed")
        player.experience.set(Skill.Magic, 14_000_000.0)
        player.experience.set(Skill.Cooking, 14_000_000.0)
        val owner = createNPC("robe_store_owner_yanille", emptyTile.addY(1))
        player.npcOption(owner, "Trade")
        tick(4)
        assertEquals("magic_guild_store_mystic_robes_trimmed", player.shop())
    }

    @Test
    fun `Base shop with another 99 but not magic`() {
        val player = createPlayer(emptyTile, "chef")
        player.experience.set(Skill.Cooking, 14_000_000.0)
        val owner = createNPC("robe_store_owner_yanille", emptyTile.addY(1))
        player.npcOption(owner, "Trade")
        tick(4)
        assertEquals("magic_guild_store_mystic_robes", player.shop())
    }
}
