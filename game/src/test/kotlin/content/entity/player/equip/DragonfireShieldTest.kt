package content.entity.player.equip

import FakeRandom
import WorldTest
import content.entity.combat.hit.directHit
import content.entity.combat.target
import content.entity.player.effect.Dragonfire
import content.entity.player.effect.antifire
import content.entity.player.effect.skullCounter
import content.entity.player.effect.superAntifire
import content.skill.prayer.getActivePrayerVarKey
import dialogueContinue
import interfaceOption
import itemOnObject
import itemOption
import messages
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class DragonfireShieldTest : WorldTest() {

    private val charged = "dragonfire_shield_charged"
    private val uncharged = "dragonfire_shield_uncharged"
    private val shield = EquipSlot.Shield.index

    @Test
    fun `Absorbing dragonfire charges an uncharged shield`() {
        val player = createPlayer(emptyTile)
        val dragon = spawn("green_dragon", emptyTile.addY(2))
        player.equipment.set(shield, uncharged)

        player.directHit(dragon, 100, "dragonfire")

        assertEquals(charged, player.equipment[shield].id)
        assertEquals(1, player.equipment.charges(player, shield))
    }

    @Test
    fun `Absorbing more dragonfire adds charges up to the maximum`() {
        val player = createPlayer(emptyTile)
        val dragon = spawn("green_dragon", emptyTile.addY(2))
        player.equipment.set(shield, charged, 49)

        player.directHit(dragon, 100, "dragonfire")
        assertEquals(50, player.equipment.charges(player, shield))

        player.directHit(dragon, 100, "dragonfire")
        assertEquals(50, player.equipment.charges(player, shield))
    }

    @Test
    fun `Charges add to the shields defensive bonuses`() {
        val player = createPlayer(emptyTile)
        val dragon = spawn("green_dragon", emptyTile.addY(2))
        player.equipment.set(shield, uncharged)
        val defence = player["stab_defence", 0]

        player.directHit(dragon, 100, "dragonfire")

        assertEquals(defence + 1, player["stab_defence", 0])
        assertEquals(1, player.equipment.charges(player, shield))
    }

    @Test
    fun `Emptying a shield releases every charge`() {
        val player = createPlayer()
        player.inventory.set(0, charged, 20)

        player.itemOption("Empty", charged, slot = 0)

        assertEquals(uncharged, player.inventory[0].id)
    }

    @Test
    fun `Discharging spends a charge and rolls the shields maximum hit`() {
        val player = createPlayer(emptyTile)
        val rat = spawn("giant_rat", emptyTile.addY(1))
        player.equipment.set(shield, charged, 20)
        player.target = rat

        player.activate()

        assertEquals(19, player.equipment.charges(player, shield))
        assertEquals(290, player["max_hit", 0])
        assertTrue(player.hasClock("dragonfire_shield_cooldown"))
    }

    @Test
    fun `Dragons are immune to the shields dragonfire`() {
        val player = createPlayer(emptyTile)
        val dragon = spawn("green_dragon", emptyTile.addY(2))
        player.equipment.set(shield, charged, 20)
        player.target = dragon
        player["max_hit"] = 99

        player.activate()

        assertEquals(19, player.equipment.charges(player, shield))
        assertEquals(0, player["max_hit", 0])
    }

    @Test
    fun `Discharging twice is blocked by the cooldown`() {
        val player = createPlayer(emptyTile)
        val rat = spawn("giant_rat", emptyTile.addY(1))
        player.equipment.set(shield, charged, 20)
        player.target = rat

        player.activate()
        player.activate()

        assertEquals(19, player.equipment.charges(player, shield))
    }

    @Test
    fun `An uncharged shield can't be discharged`() {
        val player = createPlayer(emptyTile)
        val rat = spawn("giant_rat", emptyTile.addY(1))
        player.equipment.set(shield, uncharged)
        player.target = rat

        player.activate()

        assertFalse(player.hasClock("dragonfire_shield_cooldown"))
    }

    @Test
    fun `Attaching a draconic visage to an anti-dragon shield creates an uncharged shield`() {
        val player = createPlayer(emptyTile)
        val anvil = createObject("anvil", emptyTile.addY(1))
        player.levels.set(Skill.Smithing, 90)
        player.inventory.add("draconic_visage", "anti_dragon_shield", "hammer")
        val experience = player.experience.get(Skill.Smithing)

        player.itemOnObject(anvil, player.inventory.indexOf("draconic_visage"))
        tick(2)
        player.dialogueContinue()
        tick(6)
        player.dialogueContinue()

        assertTrue(player.inventory.contains(uncharged))
        assertFalse(player.inventory.contains("draconic_visage"))
        assertFalse(player.inventory.contains("anti_dragon_shield"))
        assertEquals(experience + 2000.0, player.experience.get(Skill.Smithing))
    }

    @Test
    fun `Attaching a visage requires ninety smithing`() {
        val player = createPlayer(emptyTile)
        val anvil = createObject("anvil", emptyTile.addY(1))
        player.levels.set(Skill.Smithing, 89)
        player.inventory.add("draconic_visage", "anti_dragon_shield", "hammer")

        player.itemOnObject(anvil, player.inventory.indexOf("draconic_visage"))
        tick(2)

        assertFalse(player.inventory.contains(uncharged))
        assertTrue(player.inventory.contains("draconic_visage"))
        assertTrue(player.inventory.contains("anti_dragon_shield"))
    }

    @Test
    fun `A dropped shield loses its charges on death`() {
        val player = createPlayer(emptyTile)
        player.equipment.set(shield, charged, 20)
        player.skullCounter = 20

        player.levels.set(Skill.Constitution, 0)
        tick(10)

        assertTrue(FloorItems.at(emptyTile).any { it.id == uncharged })
        assertTrue(FloorItems.at(emptyTile).none { it.id == charged })
    }

    @Test
    fun `The discharge fires after the shield winds up`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int) = until - 1
        })
        val player = createPlayer(emptyTile)
        val rat = spawn("giant_rat", emptyTile.addY(4))
        player.equipment.set(shield, charged, 20)
        player.target = rat
        val health = rat.levels.get(Skill.Constitution)

        player.interfaceOption("worn_equipment", "shield_slot", "*", optionIndex = 1, item = player.equipment[shield])
        tick(1)

        assertEquals(health, rat.levels.get(Skill.Constitution), "the shield is still winding up")

        tick(2)

        assertEquals(health, rat.levels.get(Skill.Constitution), "the shield is still winding up")

        tick(1)

        assertTrue(rat.levels.get(Skill.Constitution) < health, "the fire lands once the wind up finishes")
    }

    @TestFactory
    fun `Only the strongest dragonfire protection is reported`() = listOf(
        "Your potion heavily protects you from the dragon's fire." to { player: Player ->
            player.superAntifire(1)
            player.equipment.set(shield, charged, 20)
        },
        "Your shield and potion fully protect you from the heat of the dragon's breath." to { player: Player ->
            player.antifire(1)
            player.equipment.set(shield, charged, 20)
        },
        "Your shield manages to block some of the dragon's breath." to { player: Player ->
            player.equipment.set(shield, charged, 20)
        },
        "Your potion slightly protects you from the heat of the dragon's breath." to { player: Player ->
            player.antifire(1)
        },
        "Your prayers help resist some of the dragonfire!" to { player: Player ->
            player.addVarbit(player.getActivePrayerVarKey(), "protect_from_magic")
        },
        "You are hit by the dragon's fiery breath." to { _: Player -> },
    ).map { (expected, setup) ->
        DynamicTest.dynamicTest(expected) {
            val player = createPlayer(emptyTile)
            val dragon = spawn("green_dragon", emptyTile.addY(2))
            setup(player)

            Dragonfire.maxHit(dragon, player, success = true)

            assertEquals(listOf(expected), player.messages.filter { it in messages }, "exactly one message is sent")
        }
    }

    private fun spawn(id: String, tile: Tile): NPC {
        val npc = createNPC(id, tile)
        tick()
        return npc
    }

    private fun Player.activate() {
        interfaceOption("worn_equipment", "shield_slot", "*", optionIndex = 1, item = equipment[shield])
        tick(2)
    }

    private companion object {
        private val emptyTile = Tile(3200, 3200)

        private val messages = setOf(
            "Your potion heavily protects you from the dragon's fire.",
            "Your shield and potion fully protect you from the heat of the dragon's breath.",
            "Your shield manages to block some of the dragon's breath.",
            "Your potion slightly protects you from the heat of the dragon's breath.",
            "Your prayers help resist some of the dragonfire!",
            "You are hit by the dragon's fiery breath.",
        )
    }
}
