package content.area.kharidian_desert.jaldraocht_pyramid

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import java.util.Properties

class JaldraochtPyramidTest : WorldTest() {

    private fun skipMissing() {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = "true"
        Settings.load(properties)
    }

    @Test
    fun `Enter the pyramid through the tunnel and leave through the portal`() {
        skipMissing()
        val player = createPlayer(Tile(3233, 2887))
        val tunnel = GameObjects.find(Tile(3233, 2888), "jaldraocht_pyramid_tunnel")

        player.objectOption(tunnel, "Enter")
        tick(3)
        assertEquals(Tile(3233, 9313), player.tile)

        val portal = createObject("jaldraocht_pyramid_portal", Tile(3233, 9312))
        player.objectOption(portal, "Enter")
        tick(3)
        assertEquals(Tile(3233, 2887), player.tile)
    }

    @Test
    fun `Tunnel doesn't lead anywhere without desert treasure`() {
        val player = createPlayer(Tile(3233, 2887))
        val tunnel = GameObjects.find(Tile(3233, 2888), "jaldraocht_pyramid_tunnel")

        player.objectOption(tunnel, "Enter")
        tick(3)

        assertEquals(Tile(3233, 2887), player.tile)
        assertTrue(player.containsMessage("This passage does not seem to lead anywhere..."))
    }

    @Test
    fun `Pray at the ancient altar to switch spellbooks`() {
        skipMissing()
        val player = createPlayer(Tile(3233, 9310))
        player.levels.set(Skill.Magic, 50)
        player.levels.set(Skill.Prayer, 10)
        val altar = GameObjects.find(Tile(3232, 9311), "ancient_altar")

        player.objectOption(altar, "Pray-at")
        tick(5)
        assertTrue(player.hasOpen("ancient_spellbook"))
        assertEquals(0, player.levels.get(Skill.Prayer))

        player.objectOption(altar, "Pray-at")
        tick(5)
        assertTrue(player.hasOpen("modern_spellbook"))
    }

    @Test
    fun `Need 50 magic to use the ancient altar`() {
        skipMissing()
        val player = createPlayer(Tile(3233, 9310))
        val altar = GameObjects.find(Tile(3232, 9311), "ancient_altar")

        player.objectOption(altar, "Pray-at")
        tick(3)

        assertTrue(player.hasOpen("modern_spellbook"))
        assertTrue(player.containsMessage("You need a Magic level of at least 50 in order to do this."))
    }
}
