package content.area.asgarnia.burthorpe.rogues_den

import WorldTest
import content.entity.npc.shop.shop
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill

internal class MartinThwaitTest : WorldTest() {

    @Test
    fun `Shop refused below 50 thieving`() {
        val player = createPlayer(emptyTile, "amateur")
        val martin = createNPC("martin_thwait", emptyTile.addY(1))

        player.npcOption(martin, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption(1)

        assertEquals("", player.shop())
    }

    @Test
    fun `Shop refused below 50 agility`() {
        val player = createPlayer(emptyTile, "clumsy")
        player.levels.set(Skill.Thieving, 50)
        val martin = createNPC("martin_thwait", emptyTile.addY(1))

        player.npcOption(martin, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption(1)

        assertEquals("", player.shop())
    }

    @Test
    fun `Shop opens with 50 thieving and 50 agility`() {
        val player = createPlayer(emptyTile, "rogue")
        player.levels.set(Skill.Thieving, 50)
        player.levels.set(Skill.Agility, 50)
        val martin = createNPC("martin_thwait", emptyTile.addY(1))

        player.npcOption(martin, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption(1)
        tick(4)

        assertEquals("martin_thwaits_lost_and_found", player.shop())
    }
}
