package content.skill.magic.book.modern

import WorldTest
import dialogueOption
import interfaceOnObject
import interfaceOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class OrbChargingTest : WorldTest() {

    val obelisks = listOf(
        "air" to Tile(3087, 3569),
        "water" to Tile(2843, 3422),
        "earth" to Tile(3085, 9932),
        "fire" to Tile(2818, 9828),
    )

    @TestFactory
    fun `Orb charging`() = obelisks.map { (type, tile) ->
        dynamicTest("Charging $type orb") {
            val player = createPlayer(tile)
            player.levels.set(Skill.Magic, 99)
            player.equipment.set(EquipSlot.Weapon.index, "staff_of_$type")
            player.inventory.add("unpowered_orb", 2)
            player.inventory.add("cosmic_rune", 6)

            val obelisk = GameObjects.find(tile.addX(1), "obelisk_of_$type")
            player.interfaceOnObject("modern_spellbook", "charge_${type}_orb", obelisk)
            tick(1)
            player.interfaceOption("skill_creation_amount", "all", "All")
            player.dialogueOption(id = "dialogue_skill_creation", component = "choice1")
            tick(6)

            assertEquals(0, player.inventory.count("unpowered_orb"))
            assertEquals(0, player.inventory.count("cosmic_rune"))
            assertEquals(2, player.inventory.count("${type}_orb"))
            assertNotEquals(0.0, player.experience.get(Skill.Magic))
        }
    }

    @TestFactory
    fun `Can't change without level`() = obelisks.map { (type, tile) ->
        dynamicTest("Can't charge $type orb without level") {
            val player = createPlayer(tile)
            player.levels.set(Skill.Magic, 1)
            player.equipment.set(EquipSlot.Weapon.index, "staff_of_$type")
            player.inventory.add("unpowered_orb")
            player.inventory.add("cosmic_rune", 3)

            val obelisk = GameObjects.find(tile.addX(1), "obelisk_of_$type")
            player.interfaceOnObject("modern_spellbook", "charge_${type}_orb", obelisk)
            tick(1)
            player.dialogueOption(id = "dialogue_skill_creation", component = "choice1")
            tick(2)

            assertEquals(1, player.inventory.count("unpowered_orb"))
            assertEquals(3, player.inventory.count("cosmic_rune"))
            assertEquals(0, player.inventory.count("${type}_orb"))
            assertEquals(0.0, player.experience.get(Skill.Magic))
        }
    }

    @TestFactory
    fun `Can't change without runes`() = obelisks.map { (type, tile) ->
        dynamicTest("Can't charge $type orb without runes") {
            val player = createPlayer(tile)
            player.levels.set(Skill.Magic, 99)
            player.inventory.add("unpowered_orb")
            player.inventory.add("cosmic_rune", 3)

            val obelisk = GameObjects.find(tile.addX(1), "obelisk_of_$type")
            player.interfaceOnObject("modern_spellbook", "charge_${type}_orb", obelisk)
            tick(1)
            player.dialogueOption(id = "dialogue_skill_creation", component = "choice1")
            tick(2)

            assertEquals(1, player.inventory.count("unpowered_orb"))
            assertEquals(3, player.inventory.count("cosmic_rune"))
            assertEquals(0, player.inventory.count("${type}_orb"))
            assertEquals(0.0, player.experience.get(Skill.Magic))
        }
    }
}
