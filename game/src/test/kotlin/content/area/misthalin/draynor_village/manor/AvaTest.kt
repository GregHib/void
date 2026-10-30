package content.area.misthalin.draynor_village.manor

import WorldTest
import dialogueOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class AvaTest : WorldTest() {

    private fun skipMissing() {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = "true"
        Settings.load(properties)
    }

    @Test
    fun `Buy an attractor`() {
        skipMissing()
        val player = createPlayer(Tile(3093, 3358))
        player.inventory.add("coins", 999)
        val ava = createNPC("ava_2", Tile(3093, 3357))

        player.npcOption(ava, "Buy device")
        tick(2)
        player.dialogueOption(1)
        tick()
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("avas_attractor"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Upgrade an attractor into an accumulator`() {
        skipMissing()
        val player = createPlayer(Tile(3093, 3358))
        player.levels.set(Skill.Ranged, 50)
        player.experience.set(Skill.Ranged, 101_333.0)
        player.inventory.add("coins", 999)
        player.inventory.add("steel_arrow", 75)
        player.inventory.add("avas_attractor")
        val ava = createNPC("ava_2", Tile(3093, 3357))

        player.npcOption(ava, "Buy device")
        tick(2)
        player.dialogueOption(2)
        tick()
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("avas_accumulator"))
        assertEquals(0, player.inventory.count("avas_attractor"))
        assertEquals(0, player.inventory.count("steel_arrow"))
    }

    @Test
    fun `Enter and leave the secret room`() {
        val player = createPlayer(Tile(3097, 3360))
        val sconce = GameObjects.find(Tile(3097, 3360), "draynor_manor_candle_sconce")

        player.objectOption(sconce, "Search")
        tick(6)
        assertEquals(Tile(3096, 3359), player.tile)

        val lever = GameObjects.find(Tile(3096, 3357), "draynor_manor_secret_lever")
        player.objectOption(lever, "Pull")
        tick(8)
        assertEquals(Tile(3097, 3358), player.tile)
    }
}
