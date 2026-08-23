package content.skill.thieving

import FakeRandom
import WorldTest
import containsMessage
import content.entity.effect.stunned
import npcOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class PickpocketingTest : WorldTest() {

    @Test
    fun `Successfully pickpocket`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(emptyTile)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(3, player.inventory.count("coins"))
        assertEquals(8.0, player.experience.get(Skill.Thieving))
        assertFalse(player.stunned)
    }

    @Test
    fun `Successfully pickpocket multiple loot`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Thieving, 11)
        player.levels.set(Skill.Agility, 1)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(6, player.inventory.count("coins"))
        assertEquals(8.0, player.experience.get(Skill.Thieving))
        assertFalse(player.stunned)
    }

    @Test
    fun `Fail to pickpocket`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer(emptyTile)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(0, player.inventory.count("coins"))
        assertEquals(0.0, player.experience.get(Skill.Thieving))
        assertTrue(player.stunned)
    }

    @Test
    fun `Can't pickpocket with full inventory`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("cheese", 28)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(0, player.inventory.count("coins"))
        assertEquals(0.0, player.experience.get(Skill.Thieving))
        assertFalse(player.stunned)
    }

    @Test
    fun `Male H A M member requires level twenty thieving`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Thieving, 19)
        val member = createNPC("ham_member_ham_cave", emptyTile.addY(1))

        player.npcOption(member, "Pickpocket")
        tick(4)

        assertFalse(player.containsMessage("You attempt to pick the H.A.M. male follower's pocket."))
        assertEquals(0.0, player.experience.get(Skill.Thieving))
        assertFalse(player.stunned)
    }

    @Test
    fun `Female and male H A M members use different pickpocket xp`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val femalePlayer = createPlayer(emptyTile, "ham-female")
        femalePlayer.levels.set(Skill.Thieving, 15)
        val femaleMember = createNPC("ham_member_ham_cave_2", emptyTile.addY(1))

        femalePlayer.npcOption(femaleMember, "Pickpocket")
        tick(4)

        assertEquals(18.5, femalePlayer.experience.get(Skill.Thieving))

        val malePlayer = createPlayer(emptyTile.addX(10), "ham-male")
        malePlayer.levels.set(Skill.Thieving, 20)
        val maleMember = createNPC("ham_member_ham_cave", emptyTile.add(10, 1))

        malePlayer.npcOption(maleMember, "Pickpocket")
        tick(4)

        assertEquals(22.2, malePlayer.experience.get(Skill.Thieving))
    }

    @Test
    fun `Ham member three is not pickpocketable`() {
        assertNull(Tables.stringOrNull("pickpocket.ham_member_ham_cave_3.type"))
    }
}
