package content.skill.slayer

import WorldTest
import itemOnItem
import itemOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals

class SlayerHelmetTest : WorldTest() {

    private val parts = arrayOf("black_mask", "earmuffs", "face_mask", "nose_peg", "spiny_helmet")

    @Test
    fun `Assemble a slayer helmet`() {
        val player = createPlayer()
        player.levels.set(Skill.Crafting, 55)
        player["malevolent_masquerade"] = true
        player.inventory.add(*parts)

        player.itemOnItem("nose_peg", "spiny_helmet")
        tick()

        assertEquals(1, player.inventory.count("slayer_helmet"))
        for (part in parts) {
            assertEquals(0, player.inventory.count(part), part)
        }
    }

    @Test
    fun `Can't assemble a slayer helmet without learning how`() {
        val player = createPlayer()
        player.levels.set(Skill.Crafting, 55)
        player.inventory.add(*parts)

        player.itemOnItem("nose_peg", "spiny_helmet")
        tick()

        assertEquals(0, player.inventory.count("slayer_helmet"))
    }

    @Test
    fun `Can't assemble a slayer helmet below 55 crafting`() {
        val player = createPlayer()
        player.levels.set(Skill.Crafting, 54)
        player["malevolent_masquerade"] = true
        player.inventory.add(*parts)

        player.itemOnItem("nose_peg", "spiny_helmet")
        tick()

        assertEquals(0, player.inventory.count("slayer_helmet"))
    }

    @Test
    fun `Can't assemble a slayer helmet with a charged black mask`() {
        val player = createPlayer()
        player.levels.set(Skill.Crafting, 55)
        player["malevolent_masquerade"] = true
        player.inventory.add("black_mask_10", "earmuffs", "face_mask", "nose_peg", "spiny_helmet")

        player.itemOnItem("nose_peg", "spiny_helmet")
        tick()

        assertEquals(0, player.inventory.count("slayer_helmet"))
    }

    @Test
    fun `Earmuffs on a face mask make masked earmuffs`() {
        val player = createPlayer()
        player["malevolent_masquerade"] = true
        player.inventory.add(*parts)

        player.itemOnItem("earmuffs", "face_mask")
        tick()

        assertEquals(1, player.inventory.count("masked_earmuffs"))
        assertEquals(0, player.inventory.count("slayer_helmet"))
    }

    @Test
    fun `Assemble a full slayer helmet`() {
        val player = createPlayer()
        player.inventory.add("slayer_helmet", "hexcrest", "focus_sight")

        player.itemOnItem("hexcrest", "slayer_helmet")
        tick()

        assertEquals(1, player.inventory.count("full_slayer_helmet"))
        assertEquals(0, player.inventory.count("slayer_helmet"))
    }

    @Test
    fun `Disassemble slayer helmets`() {
        val player = createPlayer()
        player.inventory.add("full_slayer_helmet")

        player.itemOption("Disassemble", "full_slayer_helmet")
        player.itemOption("Disassemble", "slayer_helmet")

        for (part in parts + arrayOf("hexcrest", "focus_sight")) {
            assertEquals(1, player.inventory.count(part), part)
        }
        assertEquals(0, player.inventory.count("slayer_helmet"))
    }

    @Test
    fun `Disassemble masked earmuffs`() {
        val player = createPlayer()
        player.inventory.add("masked_earmuffs")

        player.itemOption("Disassemble", "masked_earmuffs")

        assertEquals(1, player.inventory.count("earmuffs"))
        assertEquals(1, player.inventory.count("face_mask"))
    }
}
