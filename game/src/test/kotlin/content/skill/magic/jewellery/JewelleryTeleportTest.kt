package content.skill.magic.jewellery

import WorldTest
import dialogueOption
import interfaceOption
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory

class JewelleryTeleportTest : WorldTest() {

    @TestFactory
    fun `Teleport worn equipment`() = listOf(
        Triple("pharaohs_sceptre_3", 1, "jalsavrah_teleport"),
        Triple("pharaohs_sceptre_3", 2, "jaleustrophos_teleport"),
        Triple("pharaohs_sceptre_3", 3, "jaldraocht_teleport"),
        Triple("amulet_of_glory_4", 1, "edgeville_teleport"),
        Triple("amulet_of_glory_4", 2, "karamja_teleport"),
        Triple("amulet_of_glory_4", 3, "draynor_village_teleport"),
        Triple("amulet_of_glory_4", 4, "al_kharid_teleport"),
        Triple("combat_bracelet_4", 1, "warriors_guild_teleport"),
        Triple("combat_bracelet_4", 2, "champions_guild_teleport"),
        Triple("combat_bracelet_4", 3, "monastery_teleport"),
        Triple("combat_bracelet_4", 4, "ranging_guild_teleport"),
        Triple("games_necklace_4", 1, "burthorpe_teleport"),
        Triple("games_necklace_4", 2, "barbarian_outpost_teleport"),
        Triple("games_necklace_4", 3, "clan_wars_teleport"),
        Triple("games_necklace_4", 4, "wilderness_volcano_teleport"),
        Triple("games_necklace_4", 5, "burgh_de_rott_teleport"),
        Triple("ring_of_duelling_4", 1, "duel_arena_teleport"),
        Triple("ring_of_duelling_4", 2, "castle_wars_teleport"),
        Triple("ring_of_duelling_4", 3, "mobilising_armies_teleport"),
        Triple("ring_of_duelling_4", 4, "fist_of_guthix_teleport"),
        Triple("skills_necklace_4", 1, "fishing_guild_teleport"),
        Triple("skills_necklace_4", 2, "mining_guild_teleport"),
        Triple("skills_necklace_4", 3, "crafting_guild_teleport"),
        Triple("skills_necklace_4", 4, "cooking_guild_teleport"),
    ).map { (id, index, area) ->
        DynamicTest.dynamicTest("Teleport to $area with $index using $id") {
            val player = createPlayer()
            player["darkness_of_hallowvale"] = "completed"
            val item = Item(id)
            val slot = item.def.slot
            player.equipment.set(slot.index, id)

            player.interfaceOption("worn_equipment", "${slot.name.lowercase()}_slot", "*", optionIndex = index, item)

            tick(5)
            Assertions.assertTrue(player.tile in Areas[area])
        }
    }

    private data class Quad(val id: String, val itemOption: String, val option: String, val area: String)

    @TestFactory
    fun `Teleport inventory item`() = listOf(
        Quad("pharaohs_sceptre_3", "Option1", "line1", "jalsavrah_teleport"),
        Quad("pharaohs_sceptre_3", "Option1", "line2", "jaleustrophos_teleport"),
        Quad("pharaohs_sceptre_3", "Option1", "line3", "jaldraocht_teleport"),
        Quad("amulet_of_glory_4", "Option4", "line1", "edgeville_teleport"),
        Quad("amulet_of_glory_4", "Option4", "line2", "karamja_teleport"),
        Quad("amulet_of_glory_4", "Option4", "line3", "draynor_village_teleport"),
        Quad("amulet_of_glory_4", "Option4", "line4", "al_kharid_teleport"),
        Quad("combat_bracelet_4", "Option4", "line1", "warriors_guild_teleport"),
        Quad("combat_bracelet_4", "Option4", "line2", "champions_guild_teleport"),
        Quad("combat_bracelet_4", "Option4", "line3", "monastery_teleport"),
        Quad("combat_bracelet_4", "Option4", "line4", "ranging_guild_teleport"),
        Quad("games_necklace_4", "Option4", "line1", "burthorpe_teleport"),
        Quad("games_necklace_4", "Option4", "line2", "barbarian_outpost_teleport"),
        Quad("games_necklace_4", "Option4", "line3", "clan_wars_teleport"),
        Quad("games_necklace_4", "Option4", "line4", "wilderness_volcano_teleport"),
        Quad("games_necklace_4", "Option4", "line5", "burgh_de_rott_teleport"),
        Quad("ring_of_duelling_4", "Option4", "line1", "duel_arena_teleport"),
        Quad("ring_of_duelling_4", "Option4", "line2", "castle_wars_teleport"),
        Quad("ring_of_duelling_4", "Option4", "line3", "mobilising_armies_teleport"),
        Quad("ring_of_duelling_4", "Option4", "line4", "fist_of_guthix_teleport"),
        Quad("skills_necklace_4", "Option4", "line1", "fishing_guild_teleport"),
        Quad("skills_necklace_4", "Option4", "line2", "mining_guild_teleport"),
        Quad("skills_necklace_4", "Option4", "line3", "crafting_guild_teleport"),
        Quad("skills_necklace_4", "Option4", "line4", "cooking_guild_teleport"),
    ).map { (id, itemOption, option, area) ->
        DynamicTest.dynamicTest("Teleport to $area using $id") {
            val player = createPlayer()
            player["darkness_of_hallowvale"] = "completed"
            val item = Item(id)
            player.inventory.add(id)

            player.interfaceOption("inventory", "inventory", itemOption, item = item, slot = 0)
            tick()
            player.dialogueOption(option)

            tick(5)
            Assertions.assertTrue(player.tile in Areas[area])
        }
    }
}
