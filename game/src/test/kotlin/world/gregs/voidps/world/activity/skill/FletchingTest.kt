package world.gregs.voidps.world.activity.skill

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.dialogueOption
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.itemOnItem

internal class FletchingTest : WorldTest() {

    @Test
    fun `Fletch arrow shafts`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 100)
        player.inventory.add("knife")
        player.inventory.add("logs")

        player.itemOnItem(0, 1)
        tick()
        player.interfaceOption("skill_creation_amount", "increment")
        player.dialogueOption(id = "dialogue_skill_creation", component = "choice1")
        tick(2)

        assertEquals(15, player.inventory.count("arrow_shaft"))
        assertTrue(player.experience.get(Skill.Fletching) > 0)
    }

    @Test
    fun `Fletch headless arrows`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 100)
        player.inventory.add("arrow_shaft", 15)
        player.inventory.add("feather", 15)

        player.itemOnItem(0, 1)
        tick(2)

        assertEquals(15, player.inventory.count("headless_arrow"))
        assertTrue(player.experience.get(Skill.Fletching) > 0)
    }

    @Test
    fun `Fletch bronze arrows`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 100)
        player.inventory.add("headless_arrow", 15)
        player.inventory.add("bronze_arrowtips", 15)

        player.itemOnItem(0, 1)
        tick(2)

        assertEquals(15, player.inventory.count("bronze_arrow"))
        assertTrue(player.experience.get(Skill.Fletching) > 0)
    }

    @Test
    fun `Fletch shortbow unstrung`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 100)
        player.inventory.add("knife")
        player.inventory.add("logs")

        player.itemOnItem(0, 1)
        tick()
        player.interfaceOption("skill_creation_amount", "increment")
        player.dialogueOption(id = "dialogue_skill_creation", component = "choice2")
        tick(3)

        assertEquals(1, player.inventory.count("shortbow_u"))
        assertTrue(player.experience.get(Skill.Fletching) > 0)
    }

    @Test
    fun `String shortbow`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 100)
        player.inventory.add("shortbow_u")
        player.inventory.add("bowstring")

        player.itemOnItem(0, 1)
        tick(2)

        assertEquals(1, player.inventory.count("shortbow"))
        assertTrue(player.experience.get(Skill.Fletching) > 0)
    }

    @Test
    fun `Fletch wooden stock`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 100)
        player.inventory.add("knife")
        player.inventory.add("logs")

        player.itemOnItem(0, 1)
        tick()
        player.interfaceOption("skill_creation_amount", "increment")
        player.dialogueOption(id = "dialogue_skill_creation", component = "choice4")
        tick(3)

        assertEquals(1, player.inventory.count("wooden_stock"))
        assertTrue(player.experience.get(Skill.Fletching) > 0)
    }

    @Test
    fun `Fletch crossbow unstrung`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 100)
        player.inventory.add("bronze_crossbow_u")
        player.inventory.add("crossbow_string")

        player.itemOnItem(0, 1)
        tick(2)

        assertEquals(1, player.inventory.count("bronze_crossbow"))
        assertTrue(player.experience.get(Skill.Fletching) > 0)
    }

    @Test
    fun `Fletch bronze darts`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 100)
        player.inventory.add("bronze_dart_tip", 10)
        player.inventory.add("feather", 10)

        player.itemOnItem(0, 1)
        tick(2)

        assertEquals(10, player.inventory.count("bronze_dart"))
        assertTrue(player.experience.get(Skill.Fletching) > 0)
    }

    @Test
    fun `Fletch bronze bolts`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Fletching, 100)
        player.inventory.add("bronze_bolts_unf", 10)
        player.inventory.add("feather", 10)

        player.itemOnItem(0, 1)
        tick(2)

        assertEquals(10, player.inventory.count("bronze_bolts"))
        assertTrue(player.experience.get(Skill.Fletching) > 0)
    }
}