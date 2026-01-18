package content.skill.magic.book.lunar

import WorldTest
import containsMessage
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactOn
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurePlantTest : WorldTest() {

    @Test
    fun `Cure diseased patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.levels.set(Skill.Magic, 66)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("earth_rune", 8)
        player["farming_veg_patch_falador_se"] = "potato_diseased_2"
        val patch = objects.find(Tile(3058, 3308), "farming_veg_patch_falador_se")

        player.interactOn(patch, "lunar_spellbook", "cure_plant")
        tick(4)

        assertEquals("potato_2", player["farming_veg_patch_falador_se", "empty"])
        assertEquals(60.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't cure healthy patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.levels.set(Skill.Magic, 66)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("earth_rune", 8)
        player["farming_veg_patch_falador_se"] = "potato_2"
        val patch = objects.find(Tile(3058, 3308), "farming_veg_patch_falador_se")

        player.interactOn(patch, "lunar_spellbook", "cure_plant")
        tick(4)

        assertTrue(player.containsMessage("It's growing just fine"))
        assertEquals("potato_2", player["farming_veg_patch_falador_se", "empty"])
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't cure dead patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.levels.set(Skill.Magic, 66)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("earth_rune", 8)
        player["farming_veg_patch_falador_se"] = "potato_2"
        val patch = objects.find(Tile(3058, 3308), "farming_veg_patch_falador_se")

        player.interactOn(patch, "lunar_spellbook", "cure_plant")
        tick(4)

        assertTrue(player.containsMessage("It's growing just fine"))
        assertEquals("potato_2", player["farming_veg_patch_falador_se", "empty"])
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
