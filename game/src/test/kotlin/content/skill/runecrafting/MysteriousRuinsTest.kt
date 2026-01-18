package content.skill.runecrafting

import WorldTest
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

internal class MysteriousRuinsTest : WorldTest() {

    @TestFactory
    fun `Can enter ruins with talisman`() = RunecraftingTest.altars.map { (type, ruinsTile, altarTile) ->
        dynamicTest("Enter $type ruins with talisman") {
            val tile = Areas["${type}_altar_teleport"].random()
            val player = createPlayer(tile)
            player.inventory.add("${type}_talisman")

            println("Start $tile")
            val ruins = objects.find(ruinsTile, "${type}_altar_ruins")
            player.itemOnObject(ruins, 0)
            tickIf {
                println(player.tile)
                player.tile.region == tile.region
            }

            assertAtAltar(player, type, altarTile)
        }
    }

    @TestFactory
    fun `Can enter ruins with tiara`() = RunecraftingTest.altars.map { (type, ruinsTile, altarTile) ->
        dynamicTest("Enter $type ruins with tiara") {
            val tile = Areas["${type}_altar_teleport"].random()
            val player = createPlayer(tile)
            player.equipment.set(EquipSlot.Hat.index, "${type}_tiara")
            tick(1)

            val ruins = objects.find(ruinsTile, "${type}_altar_ruins")
            player.objectOption(ruins, "Enter", 0)
            tickIf { player.tile.region == tile.region }

            assertAtAltar(player, type, altarTile)
        }
    }

    @TestFactory
    fun `Can enter ruins with omni tiara`() = RunecraftingTest.altars.map { (type, ruinsTile, altarTile) ->
        dynamicTest("Enter $type ruins with omni tiara") {
            val tile = Areas["${type}_altar_teleport"].random()
            val player = createPlayer(tile)
            player.equipment.set(EquipSlot.Hat.index, "omni_tiara")

            val ruins = objects.find(ruinsTile, "${type}_altar_ruins")
            player.objectOption(ruins, "Enter", 0)
            tickIf { player.tile.region == tile.region }

            assertAtAltar(player, type, altarTile)
        }
    }

    @TestFactory
    fun `Can enter ruins with omni staff`() = RunecraftingTest.altars.map { (type, ruinsTile, altarTile) ->
        dynamicTest("Enter $type ruins with omni tiara") {
            val tile = Areas["${type}_altar_teleport"].random()
            val player = createPlayer(tile)
            player.equipment.set(EquipSlot.Weapon.index, "omni_talisman_staff")

            val ruins = objects.find(ruinsTile, "${type}_altar_ruins")
            player.objectOption(ruins, "Enter", 0)
            tick(5)
            tickIf { player.tile.region == tile.region }

            assertAtAltar(player, type, altarTile)
        }
    }

    private fun assertAtAltar(player: Player, type: String, altarTile: Tile) {
        if (type == "chaos") {
            assertEquals(9035, player.tile.region.id)
        } else {
            val distance = player.tile.distanceTo(altarTile)
            assertNotEquals(-1, distance)
            assertTrue(distance < 25)
        }
    }

    @TestFactory
    fun `Cannot enter ruins with no items`() = RunecraftingTest.altars.map { (type, ruinsTile, altarTile) ->
        dynamicTest("Cannot enter $type ruins") {
            val tile = Areas["${type}_altar_teleport"].random()
            val player = createPlayer(tile)

            val ruins = objects.find(ruinsTile, "${type}_altar_ruins")
            player.objectOption(ruins, "Enter", 0)
            tick(4)

            val distance = player.tile.distanceTo(altarTile)
            assertTrue(distance == -1 || distance > 25)
        }
    }
}
