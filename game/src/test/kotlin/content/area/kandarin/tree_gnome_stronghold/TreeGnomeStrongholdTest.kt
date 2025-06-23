package content.area.kandarin.tree_gnome_stronghold

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TreeGnomeStrongholdTest : WorldTest() {

    @Test
    fun `Climb up rock shortcut`() {
        val player = createPlayer(Tile(2486, 3515))
        player.levels.set(Skill.Agility, 37)
        val rocks = objects[Tile(2487, 3515), "gnome_stronghold_shortcut_rock_top"]!!
        player.objectOption(rocks, "Climb")
        tick(8)

        assertEquals(Tile(2489, 3521), player.tile)
    }

    @Test
    fun `Can't climb up rock shortcut without levels`() {
        val player = createPlayer(Tile(2486, 3515))
        player.levels.set(Skill.Agility, 36)
        val rocks = objects[Tile(2487, 3515), "gnome_stronghold_shortcut_rock_top"]!!
        player.objectOption(rocks, "Climb")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 37"))
    }

    @Test
    fun `Climb down rock shortcut`() {
        val player = createPlayer(Tile(2489, 3521))
        player.levels.set(Skill.Agility, 37)
        val rocks = objects[Tile(2489, 3520), "gnome_stronghold_shortcut_rock_bottom"]!!
        player.objectOption(rocks, "Climb")
        tick(8)

        assertEquals(Tile(2486, 3515), player.tile)
    }

    @Test
    fun `Can't climb down rock shortcut without levels`() {
        val player = createPlayer(Tile(2489, 3521))
        player.levels.set(Skill.Agility, 36)
        val rocks = objects[Tile(2489, 3520), "gnome_stronghold_shortcut_rock_bottom"]!!
        player.objectOption(rocks, "Climb")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 37"))
    }
}
