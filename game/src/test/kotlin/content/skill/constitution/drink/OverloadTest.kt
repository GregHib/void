package content.skill.constitution.drink

import WorldTest
import itemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class OverloadTest : WorldTest() {

    @Test
    fun `Overload plays the shock animation for every hit`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("overload_4")
        player.experience.set(Skill.Constitution, Level.experience(99))
        player.levels.set(Skill.Constitution, 990)
        val animation = AnimationDefinitions.get("overload").id
        val graphic = GraphicDefinitions.get("overload").id

        player.itemOption("Drink", "overload_4")

        var animations = 0
        var graphics = 0
        repeat(12) {
            if (player.visuals.animation.stand == animation) {
                animations++
            }
            if (player.visuals.secondaryGraphic.id == graphic || player.visuals.primaryGraphic.id == graphic) {
                graphics++
            }
            tick()
        }

        assertEquals(5, animations)
        assertEquals(5, graphics)
        assertTrue(player.levels.get(Skill.Constitution) in 490..495)
    }
}
