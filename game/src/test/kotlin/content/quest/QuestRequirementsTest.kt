package content.quest

import WorldTest
import containsMessage
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skillCreation
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import java.util.Properties

class QuestRequirementsTest : WorldTest() {

    private fun skipMissing(skip: Boolean) {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = skip.toString()
        Settings.load(properties)
    }

    @Test
    fun `Unimplemented quests count as started and completed when skipping missing`() {
        skipMissing(true)
        val player = createPlayer(Tile(3222, 3218))

        assertTrue(player.questStarted("fairy_tale_ii"))
        assertTrue(player.questCompleted("fairy_tale_ii"))
    }

    @Test
    fun `Unimplemented quests aren't skipped when disabled`() {
        skipMissing(false)
        val player = createPlayer(Tile(3222, 3218))

        assertFalse(player.questStarted("fairy_tale_ii"))
        assertFalse(player.questCompleted("fairy_tale_ii"))
    }

    @Test
    fun `Implemented quests are never skipped`() {
        skipMissing(true)
        val player = createPlayer(Tile(3222, 3218))

        assertFalse(player.questStarted("rune_mysteries"))
        assertFalse(player.questCompleted("rune_mysteries"))
        player["rune_mysteries"] = "started"
        assertTrue(player.questStarted("rune_mysteries"))
        assertFalse(player.questCompleted("rune_mysteries"))
    }

    @Test
    fun `Use fairy ring without fairy tale progress when skipping missing`() {
        skipMissing(true)
        val player = createPlayer(Tile(3129, 3497))
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")
        val fairyRing = GameObjects.find(Tile(3129, 3496), "fairy_ring_edgeville")

        player.objectOption(fairyRing, "Use")
        tick(1)

        assertFalse(player.containsMessage("You don't have permission to use that fairy ring."))
    }

    @Test
    fun `Smelt cannonballs without dwarf cannon when skipping missing`() {
        skipMissing(true)
        val player = createPlayer(Tile(3227, 3255))
        player.levels.set(Skill.Smithing, 35)
        player.inventory.add("steel_bar")
        player.inventory.add("ammo_mould")
        val furnace = GameObjects.find(Tile(3226, 3256), "furnace_lumbridge")

        player.itemOnObject(furnace, 0)
        tick()
        player.skillCreation("cannonball", 1)
        tick(8)

        assertEquals(0, player.inventory.count("steel_bar"))
        assertEquals(4, player.inventory.count("cannonball"))
    }
}
