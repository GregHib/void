package content.skill.summoning

import WorldTest
import content.entity.combat.target
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.network.client.instruction.InteractInterfaceNPC
import world.gregs.voidps.type.Tile
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers the full cast-then-click-npc dispatch of a combat special (Famine, `familiar_details`
 * component `cast_famine` = 107 on interface 662): the familiar engages the target while the
 * owner stays out of combat.
 */
class FamineCastTest : WorldTest() {

    @Test
    fun `Famine cast on an npc sends the familiar at it, not the player`() {
        val player = createPlayer(Tile(2523, 3056))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("ravenous_locust_familiar"), restart = false)
        tick(2) // let the summon queue assign the follower
        player.set("summoning_special_points_remaining", 60)
        player.inventory.transaction { add("famine_scroll", 5) }
        val familiar = player.follower!!
        val rat = createNPC("giant_rat", player.tile.addY(3))

        runTest { player.instructions.send(InteractInterfaceNPC(rat.index, 662, 107, -1, -1)) }
        tick(10)

        assertTrue(familiar.mode is CombatMovement, "the familiar engages the target")
        assertFalse(player.mode is CombatMovement, "the owner must not attack")
        assertNull(player.target, "the owner must not gain a combat target")
    }
}
