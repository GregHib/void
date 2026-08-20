package content.area.misthalin.edgeville.monastery

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AbbotLangleyTest : WorldTest() {

    @Test
    fun `Join the order with spent prayer points`() {
        val (player, langley) = setup("langley_drained")
        player.experience.set(Skill.Prayer, Level.experience(31))
        player.levels.set(Skill.Prayer, 1)

        player.askToJoin(langley)

        assertTrue(player["edgeville_monastery_order_member", false])
    }

    @Test
    fun `Can't join the order below thirty one prayer`() {
        val (player, langley) = setup("langley_devotion")
        player.experience.set(Skill.Prayer, Level.experience(30))

        player.askToJoin(langley)

        assertFalse(player["edgeville_monastery_order_member", false])
    }

    @Test
    fun `Climb the monastery ladder after joining the order`() {
        val player = createPlayer(Tile(3058, 3483), "langley_ladder")
        player.experience.set(Skill.Prayer, Level.experience(31))
        player.levels.set(Skill.Prayer, 1)
        val ladder = GameObjects.find(Tile(3057, 3483), "monastery_ladder_up")

        player.objectOption(ladder, "Climb-up")
        tick(6)
        player.dialogueContinue() // "I'm sorry but only members of our order are allowed..."
        player.dialogueOption(1) // "Well can I join your order?"
        player.dialogueContinue(2) // repeated player line + Langley accepting
        tick(4)

        assertTrue(player["edgeville_monastery_order_member", false])
        assertEquals(1, player.tile.level)
    }

    private fun setup(name: String): Pair<Player, NPC> {
        val player = createPlayer(emptyTile, name)
        val langley = createNPC("abbot_langley", emptyTile.addX(1))
        tick()
        return player to langley
    }

    private fun Player.askToJoin(langley: NPC) {
        npcOption(langley, "Talk-to")
        tick(2)
        dialogueContinue() // "Greetings traveller."
        dialogueOption(3) // "How do I get further into the monastery?"
        dialogueContinue(2) // repeated player line + Langley's refusal
        dialogueOption(1) // "Well can I join your order?"
        dialogueContinue(2)
        tick()
    }

    private companion object {
        private val emptyTile = Tile(3200, 3200)
    }
}
