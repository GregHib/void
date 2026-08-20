package content.skill.herblore

import WorldTest
import itemOnItem
import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals

class CrushingTest : WorldTest() {

    @TestFactory
    fun `Crush an item with a pestle and mortar`() = crushable.map { (ingredient, product) ->
        dynamicTest("Crush ${ingredient.toSentenceCase()}") {
            val player = createPlayer()
            player.inventory.add("pestle_and_mortar")
            player.inventory.add(ingredient)

            player.itemOnItem("pestle_and_mortar", ingredient)
            tick(3)

            assertEquals(1, player.inventory.count(product))
            assertEquals(0, player.inventory.count(ingredient))
            assertEquals(0.0, player.experience.get(Skill.Herblore))
        }
    }

    private companion object {
        private val crushable = listOf(
            "unicorn_horn" to "unicorn_horn_dust",
            "kebbit_teeth" to "kebbit_teeth_dust",
            "blue_dragon_scale" to "dragon_scale_dust",
            "birds_nest_empty" to "crushed_nest",
            "uncut_opal" to "crushed_gem",
            "chocolate_bar" to "chocolate_dust",
            "mud_rune" to "ground_mud_runes",
            "gorak_claws" to "gorak_claw_powder",
            "desert_goat_horn" to "goat_horn_dust",
            "ashes" to "ground_ashes",
            "fishing_bait" to "ground_fishing_bait",
            "seaweed" to "ground_seaweed",
            "raw_karambwan" to "karambwan_paste_raw",
            "cooked_karambwan" to "karambwan_paste_cooked",
            "poison_karambwan" to "karambwan_paste_poison",
            "bat_bones" to "ground_bat_bones",
            "charcoal" to "ground_charcoal",
            "cod" to "ground_cod",
            "kelp_mogre_camp" to "ground_kelp",
            "crab_meat" to "ground_crab_meat",
            "rune_shards" to "rune_dust",
            "astral_rune_shards" to "ground_astral_rune",
            "suqah_tooth" to "ground_tooth",
            "diamond_root" to "diamond_root_dust",
            "dried_thistle" to "ground_thistle",
            "garlic" to "garlic_powder",
        )
    }
}
