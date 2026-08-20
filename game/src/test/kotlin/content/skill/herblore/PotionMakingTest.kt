package content.skill.herblore

import WorldTest
import itemOnItem
import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals

class PotionMakingTest : WorldTest() {

    private val herbs = listOf(
        "clean_guam",
        "clean_marrentill",
        "clean_tarromin",
        "clean_harralander",
        "clean_ranarr",
        "clean_irit",
        "clean_avantoe",
        "clean_kwuarm",
        "clean_cadantine",
        "clean_dwarf_weed",
        "clean_torstol",
        "clean_lantadyme",
        "clean_toadflax",
        "clean_snapdragon",
        "clean_wergali",
        "clean_spirit_weed",
    )

    private val potions = listOf(
        listOf("guam_potion_unf", "eye_of_newt", "attack_potion_3"),
        listOf("marrentill_potion_unf", "unicorn_horn_dust", "antipoison_3"),
        listOf("tarromin_potion_unf", "limpwurt_root", "strength_potion_3"),
        listOf("harralander_potion_unf", "red_spiders_eggs", "restore_potion_3"),
        listOf("harralander_potion_unf", "chocolate_dust", "energy_potion_3"),
        listOf("harralander_potion_unf", "goat_horn_dust", "combat_potion_3"),
        listOf("ranarr_potion_unf", "white_berries", "defence_potion_3"),
        listOf("ranarr_potion_unf", "snape_grass", "prayer_potion_3"),
        listOf("irit_potion_unf", "eye_of_newt", "super_attack_3"),
        listOf("avantoe_potion_unf", "mort_myre_fungus", "super_energy_3"),
        listOf("kwuarm_potion_unf", "limpwurt_root", "super_strength_3"),
        listOf("snapdragon_potion_unf", "red_spiders_eggs", "super_restore_3"),
        listOf("cadantine_potion_unf", "white_berries", "super_defence_3"),
        listOf("lantadyme_potion_unf", "dragon_scale_dust", "antifire_3"),
        listOf("dwarf_weed_potion_unf", "wine_of_zamorak", "super_ranging_potion_3"),
        listOf("torstol_potion_unf", "jangerberries", "zamorak_brew_3"),
        listOf("toadflax_potion_unf", "crushed_nest", "saradomin_brew_3"),
        listOf("wergali_potion_unf", "frog_spawn", "crafting_potion_3"),
        listOf("weapon_poison+_unf", "red_spiders_eggs", "weapon_poison+"),
        listOf("weapon_poison++_unf", "poison_ivy_berries", "weapon_poison++"),
        listOf("antifire_3", "phoenix_feather", "super_antifire_3"),
        listOf("samaden_potion_unf", "zamorak_vine", "zamoraks_favour_3"),
        listOf("antipoison+_2", "caviar", "antidote+_mix_2"),
        listOf("prayer_potion_3", "wyvern_bonemeal", "super_prayer_3"),
        listOf("super_energy_3", "papaya_fruit", "recover_special_3"),
        listOf("extreme_attack_3", "extreme_strength_3", "extreme_defence_3", "extreme_magic_3", "extreme_ranging_3", "clean_torstol", "overload_3"),
    )

    private val experience = listOf(
        listOf("harralander_potion_unf", "red_spiders_eggs", "restore_potion_3") to 62.5,
        listOf("harralander_potion_unf", "goat_horn_dust", "combat_potion_3") to 84.0,
        listOf("weapon_poison+_unf", "red_spiders_eggs", "weapon_poison+") to 165.0,
        listOf("antifire_3", "phoenix_feather", "super_antifire_3") to 210.0,
        listOf("antipoison_2", "roe", "antipoison_mix_2") to 12.0,
        listOf("antipoison+_2", "caviar", "antidote+_mix_2") to 52.0,
    )

    @TestFactory
    fun `Create potion`() = potions.map { items ->
        dynamicTest("Create ${items.last().toSentenceCase()}") {
            val player = createPlayer()
            player.levels.set(Skill.Herblore, 99)
            for (item in items.dropLast(1)) {
                player.inventory.add(item)
            }

            player.itemOnItem(0, 1)
            tick(2)

            assertEquals(1, player.inventory.count(items.last()))
            for (item in items.dropLast(1)) {
                assertEquals(0, player.inventory.count(item))
            }
            assertNotEquals(0.0, player.experience.get(Skill.Herblore))
        }
    }

    @TestFactory
    fun `Potions give their listed experience`() = experience.map { (items, xp) ->
        dynamicTest("Create ${items.last().toSentenceCase()}") {
            val player = createPlayer()
            player.levels.set(Skill.Herblore, 99)
            for (item in items.dropLast(1)) {
                player.inventory.add(item)
            }

            player.itemOnItem(0, 1)
            tick(2)

            assertEquals(1, player.inventory.count(items.last()))
            assertEquals(xp, player.experience.get(Skill.Herblore))
        }
    }

    @Test
    fun `Extreme ranging takes five grenwall spikes`() {
        val player = createPlayer()
        player.levels.set(Skill.Herblore, 99)
        player.inventory.add("super_ranging_potion_3")
        player.inventory.add("grenwall_spikes", 7)

        player.itemOnItem(0, 1)
        tick(2)

        assertEquals(1, player.inventory.count("extreme_ranging_3"))
        assertEquals(2, player.inventory.count("grenwall_spikes"))
        assertEquals(260.0, player.experience.get(Skill.Herblore))
    }

    @TestFactory
    fun `Barbarian mixes need their listed level`() = listOf(
        Triple("super_magic_potion_2", "super_magic_mix_2", 83),
        Triple("super_ranging_potion_2", "super_ranging_mix_2", 80),
        Triple("defence_potion_2", "defence_mix_2", 33),
    ).map { (potion, mix, level) ->
        dynamicTest("Mix ${mix.toSentenceCase()}") {
            val player = createPlayer()
            player.levels.set(Skill.Herblore, level - 1)
            player.inventory.add(potion)
            player.inventory.add("caviar")

            player.itemOnItem(0, 1)
            tick(2)

            assertEquals(0, player.inventory.count(mix))
            assertEquals(1, player.inventory.count(potion))
        }
    }

    @TestFactory
    fun `Unfinished potions give no experience`() = listOf(
        Triple("coconut_milk", "clean_toadflax", "antipoison+_unf"),
        Triple("coconut_milk", "cactus_spine", "weapon_poison+_unf"),
        Triple("juju_vial_of_water", "clean_erzille", "erzille_potion_unf"),
        Triple("star_flower", "vial_of_water", "magic_essence_unf"),
    ).map { (first, second, unfinished) ->
        dynamicTest("Create ${unfinished.toSentenceCase()}") {
            val player = createPlayer()
            player.levels.set(Skill.Herblore, 99)
            player.inventory.add(first)
            player.inventory.add(second)

            player.itemOnItem(0, 1)
            tick(2)

            assertEquals(1, player.inventory.count(unfinished))
            assertEquals(0.0, player.experience.get(Skill.Herblore))
        }
    }

    @Test
    fun `Antipoison+ mix decants into two doses`() {
        val player = createPlayer()
        player.inventory.add("antidote+_mix_1", 2)

        player.itemOnItem(0, 1)
        tick(2)

        assertEquals(1, player.inventory.count("antidote+_mix_2"))
        assertEquals(1, player.inventory.count("vial"))
    }

    @TestFactory
    fun `Create unfinished potion`() = herbs.map { herb ->
        dynamicTest("Create unfinished ${herb.removePrefix("clean_")}") {
            val player = createPlayer()
            player.levels.set(Skill.Herblore, 99)
            player.inventory.add(herb)
            player.inventory.add("vial_of_water")

            player.itemOnItem(0, 1)
            tick(2)

            assertEquals(1, player.inventory.count("${herb.removePrefix("clean_")}_potion_unf"))
            assertEquals(0, player.inventory.count(herb))
            assertEquals(0, player.inventory.count("vial_of_water"))
            assertEquals(0.0, player.experience.get(Skill.Herblore))
        }
    }
}
