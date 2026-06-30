package content.skill.magic.book.lunar

import WorldTest
import containsMessage
import messages
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactOn
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FertileSoilTest : WorldTest() {

    @Test
    fun `Fertilise an untreated patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.levels.set(Skill.Magic, 83)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 3)
        player.inventory.add("nature_rune", 2)
        player.inventory.add("earth_rune", 15)
        player["farming_veg_patch_falador_se"] = "potato_2"
        val patch = GameObjects.find(Tile(3058, 3308), "farming_veg_patch_falador_se")

        player.interactOn(patch, "lunar_spellbook", "fertile_soil")
        tick(5)

        assertTrue(player.containsVarbit("patch_super_compost", "farming_veg_patch_falador_se"))
        assertEquals(87.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't fertilise an already treated patch`() {
        val player = createPlayer(Tile(3057, 3308))
        player.levels.set(Skill.Magic, 83)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 3)
        player.inventory.add("nature_rune", 2)
        player.inventory.add("earth_rune", 15)
        player["farming_veg_patch_falador_se"] = "potato_2"
        player.addVarbit("patch_super_compost", "farming_veg_patch_falador_se")
        val patch = GameObjects.find(Tile(3058, 3308), "farming_veg_patch_falador_se")

        player.interactOn(patch, "lunar_spellbook", "fertile_soil")
        tick(5)

        assertTrue(player.containsMessage("has already been treated with supercompost"))
        assertEquals(3, player.inventory.count("astral_rune"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't fertilise other objects`() {
        val player = createPlayer(Tile(3062, 3310))
        player.levels.set(Skill.Magic, 83)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 3)
        player.inventory.add("nature_rune", 2)
        player.inventory.add("earth_rune", 15)

        val obj = GameObjects.find(Tile(3061, 3311), "oak")
        player.interactOn(obj, "lunar_spellbook", "fertile_soil")
        tick(5)

        assertTrue(player.containsMessage("I don't want to fertilise that!"))
        assertFalse(player.containsVarbit("patch_super_compost", "oak"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
