package content.skill.summoning

import WorldTest
import itemOnItem
import itemOnObject
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnchantedHeadgearTest : WorldTest() {

    private fun summoner(tile: Tile = Tile(2523, 3056)): Player {
        val player = createPlayer(tile)
        player.levels.set(Skill.Summoning, 99)
        return player
    }

    @Test
    fun `Using combat scrolls on a helm charges it`() {
        val player = summoner()
        player.inventory.transaction {
            add("antlers", 1)
            add("iron_bull_rush_scroll", 20)
        }

        player.itemOnItem(1, 0) // scrolls onto the antlers
        tick(1)

        assertEquals(1, player.inventory.count("antlers_charged"), "the antlers become charged")
        assertEquals(0, player.inventory.count("antlers"))
        assertEquals(0, player.inventory.count("iron_bull_rush_scroll"), "the scrolls are stored inside")
    }

    @Test
    fun `Non-combat scrolls cannot be stored`() {
        val player = summoner()
        player.inventory.transaction {
            add("antlers", 1)
            add("healing_aura_scroll", 5) // the unicorn's heal - not a combat scroll
        }

        player.itemOnItem(1, 0)
        tick(1)

        assertEquals(1, player.inventory.count("antlers"), "the helm stays plain")
        assertEquals(5, player.inventory.count("healing_aura_scroll"), "the scrolls are refused")
    }

    @Test
    fun `A charged helm emptied on an obelisk returns its scrolls`() {
        val player = summoner(Tile(2528, 3095)) // by the Taverley obelisk region
        val obelisk = createObject("obelisk", player.tile.addX(1))
        player.inventory.transaction {
            add("antlers", 1)
            add("iron_bull_rush_scroll", 15)
        }
        player.itemOnItem(1, 0)
        tick(1)

        player.itemOnObject(obelisk, 0)
        tick(1)

        assertEquals(15, player.inventory.count("iron_bull_rush_scroll"), "the scrolls come back")
        assertEquals(1, player.inventory.count("antlers"), "the helm reverts to plain")
        assertEquals(0, player.inventory.count("antlers_charged"))
    }

    @Test
    fun `A worn enchanted helm supplies scrolls to a special move`() {
        val player = summoner()
        player.inventory.transaction {
            add("antlers", 1)
            add("iron_bull_rush_scroll", 3)
        }
        // Charge the helm, then wear it with no scrolls left in the pack.
        player.itemOnItem(1, 0)
        tick(1)
        player.inventory.remove("antlers_charged")
        player.equipment.set(EquipSlot.Hat.index, "antlers_charged")

        player.summonFamiliar(NPCDefinitions.get("iron_minotaur_familiar"), restart = false)
        tick(2)
        player.set("summoning_special_points_remaining", 60)
        val target = createNPC("giant_rat", player.tile.addY(4))

        assertEquals(3, player.get("enchanted_headgear_count", 0), "the helm holds three scrolls")
        player.castFamiliarSpecial { FamiliarSpecialMoves.npcTarget.getValue("iron_minotaur_familiar").invoke(player, target) }

        assertEquals(2, player.get("enchanted_headgear_count", 0), "the helm supplied one scroll for the cast")
    }
}
