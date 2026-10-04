package content.area.asgarnia.burthorpe.warriors_guild

import FakeRandom
import WorldTest
import containsMessage
import dialogueContinue
import dialogueOption
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom

internal class WarriorsGuildTest : WorldTest() {

    @Test
    fun `Weak players can't enter the guild`() {
        val door = createObject("warriors_guild_door_closed", emptyTile)
        val player = createPlayer(emptyTile.addX(1), "weakling")

        player.objectOption(door, "Open")
        tick(3)

        assertEquals(emptyTile.addX(1), player.tile)
        assertNotNull(player.dialogue)
    }

    @Test
    fun `Strong players can enter the guild`() {
        val door = createObject("warriors_guild_door_closed", emptyTile)
        val player = createPlayer(emptyTile.addX(1), "strongman")
        player.experience.set(Skill.Attack, 14_000_000.0)

        player.objectOption(door, "Open")
        tick(5)

        assertNotEquals(emptyTile.addX(1), player.tile)
    }

    @Test
    fun `Claim earned tokens`() {
        val player = createPlayer(emptyTile, "claimer")
        player["warriors_guild_tokens"] = 25
        val jimmy = createNPC("jimmy", emptyTile.addY(1))

        player.npcOption(jimmy, "Claim-tokens")
        tick()
        player.dialogueContinue(3)

        assertEquals(25, player.inventory.count("warrior_guild_token"))
        assertEquals(0, player["warriors_guild_tokens", 0])
    }

    @Test
    fun `Animate a full set of armour`() {
        val player = createPlayer(emptyTile, "animator")
        player.inventory.add("bronze_full_helm", "bronze_platebody", "bronze_platelegs")
        val animator = createObject("magical_animator", emptyTile.addY(2))

        player.itemOnObject(animator, player.inventory.indexOf("bronze_platebody"))
        tick(12)

        assertEquals(0, player.inventory.count("bronze_platebody"))
        assertEquals("bronze", player["warriors_guild_animated_armour", ""])
        assertTrue(NPCs.any { it.id == "animated_bronze_armour" })
    }

    @Test
    fun `Can't animate an incomplete set of armour`() {
        val player = createPlayer(emptyTile, "incomplete")
        player.inventory.add("iron_full_helm", "iron_platebody")
        val animator = createObject("magical_animator", emptyTile.addY(2))

        player.itemOnObject(animator, player.inventory.indexOf("iron_platebody"))
        tick(3)

        assertEquals(1, player.inventory.count("iron_platebody"))
        assertTrue(NPCs.none { it.id == "animated_iron_armour" })
    }

    @Test
    fun `Throw a shot for tokens and strength experience`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        val player = createPlayer(emptyTile, "thrower")
        val pile = createObject("shot_put_18lb", emptyTile.addY(1))

        player.objectOption(pile, "Throw")
        tick(3)
        player.dialogueOption(1)
        tick(12)

        assertTrue(player["warriors_guild_tokens", 0] > 0)
        assertTrue(player.experience.get(Skill.Strength) > 0.0)
    }

    @Test
    fun `Can't throw a shot with a weapon equipped`() {
        val player = createPlayer(emptyTile, "armed_thrower")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        val pile = createObject("shot_put_22lb", emptyTile.addY(1))

        player.objectOption(pile, "Throw")
        tick(3)

        assertEquals(0, player["warriors_guild_tokens", 0])
        assertNotNull(player.dialogue)
    }

    @Test
    fun `Balance a keg on your head`() {
        val player = createPlayer(emptyTile, "balancer")
        val kegs = createObject("warriors_guild_kegs", emptyTile.addY(1))

        player.objectOption(kegs, "Pick-up")
        tick(5)

        assertEquals("one_barrel", player.equipped(EquipSlot.Hat).id)
        assertEquals(1, player["warriors_guild_kegs", 0])
    }

    @Test
    fun `Can't enter the cyclopes room without enough tokens`() {
        val door = createObject("cyclopes_room_door_closed", emptyTile)
        val player = createPlayer(emptyTile.addX(-1), "poor")
        player.inventory.add("warrior_guild_token", 50)

        player.objectOption(door, "Open")
        tick(2)

        assertEquals(50, player.inventory.count("warrior_guild_token"))
        assertEquals(emptyTile.addX(-1), player.tile)
    }

    @Test
    fun `Entering the cyclopes room costs tokens`() {
        val door = createObject("cyclopes_room_door_closed", emptyTile)
        val player = createPlayer(emptyTile.addX(-1), "rich")
        player.inventory.add("warrior_guild_token", 100)

        player.objectOption(door, "Open")
        tick(2)
        player.dialogueContinue(2)
        tick(3)

        assertEquals(90, player.inventory.count("warrior_guild_token"))
        assertTrue(player.containsMessage("10 tokens are taken as you enter the room."))
    }
}
