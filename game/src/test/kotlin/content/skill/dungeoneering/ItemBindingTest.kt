package content.skill.dungeoneering

import WorldTest
import dialogueOption
import itemOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class ItemBindingTest : WorldTest() {

    @Test
    fun `Bind item`() {
        val player = createPlayer()
        player.inventory.add("argonite_rapier")

        player.itemOption("Option3", "argonite_rapier")

        assertEquals(0, player.inventory.count("argonite_rapier"))
        assertEquals(1, player.inventory.count("argonite_rapier_bound"))
        assertEquals(1, player.inventories.inventory("dungeoneering_bound").count("argonite_rapier_bound"))
    }

    @Test
    fun `Can't bind more than slots available at dungeoneering level`() {
        val player = createPlayer()
        player.experience.set(Skill.Dungeoneering, Level.experience(50))
        player.inventory.add("argonite_rapier")
        player.inventory.add("blightleaf_shoes")
        player.inventory.add("thigat_shortbow")

        player.itemOption("Option3", "argonite_rapier")
        player.itemOption("Option3", "blightleaf_shoes")
        player.itemOption("Option3", "thigat_shortbow")

        assertEquals(0, player.inventory.count("argonite_rapier"))
        assertEquals(1, player.inventory.count("argonite_rapier_bound"))
        assertEquals(0, player.inventory.count("blightleaf_shoes"))
        assertEquals(1, player.inventory.count("blightleaf_shoes_bound"))
        assertEquals(1, player.inventory.count("thigat_shortbow"))
        val binds = player.inventories.inventory("dungeoneering_bound")
        assertEquals(1, binds.count("argonite_rapier_bound"))
        assertEquals(1, binds.count("blightleaf_shoes_bound"))
        assertEquals(0, binds.count("thigat_shortbow_bound"))
    }

    @Test
    fun `Remove item bind`() {
        val player = createPlayer()
        val binds = player.inventories.inventory("dungeoneering_bound")
        binds.add("argonite_rapier")
        player.inventory.add("argonite_rapier")

        player.itemOption("Drop", "argonite_rapier")
        player.dialogueOption("confirm", "dialogue_confirm_destroy")

        assertEquals(0, player.inventory.count("argonite_rapier"))
        assertEquals(0, binds.count("argonite_rapier_bound"))
    }

    @Test
    fun `Bind runes`() {
        val player = createPlayer()
        player.inventory.add("air_rune_dungeoneering", 25)

        player.itemOption("Option4", "air_rune_dungeoneering")

        assertEquals(0, player.inventory.count("air_rune_dungeoneering"))
        assertEquals(25, player.inventory.count("air_rune_dungeoneering_bound"))
        assertEquals("air_rune_dungeoneering_bound", player["dungeoneering_bound_ammo_id", ""])
        assertEquals(25, player["dungeoneering_bound_ammo_count", 0])
    }

    @Test
    fun `Bind arrows`() {
        val player = createPlayer()
        player.inventory.add("bathus_arrows", 200)

        player.itemOption("Option3", "bathus_arrows")

        assertEquals(75, player.inventory.count("bathus_arrows"))
        assertEquals(125, player.inventory.count("bathus_arrows_bound"))
        assertEquals("bathus_arrows_bound", player["dungeoneering_bound_ammo_id", ""])
        assertEquals(125, player["dungeoneering_bound_ammo_count", 0])
    }

    @Test
    fun `Bind increase ammo`() {
        val player = createPlayer()

        player.inventory.add("air_rune_dungeoneering", 25)
        player.itemOption("Option4", "air_rune_dungeoneering")

        player.inventory.add("air_rune_dungeoneering", 25)
        player.itemOption("Option4", "air_rune_dungeoneering")

        assertEquals(0, player.inventory.count("air_rune_dungeoneering"))
        assertEquals(50, player.inventory.count("air_rune_dungeoneering_bound"))
        assertEquals("air_rune_dungeoneering_bound", player["dungeoneering_bound_ammo_id", ""])
        assertEquals(50, player["dungeoneering_bound_ammo_count", 0])
    }

    @Test
    fun `Can't bind over another ammo type`() {
        val player = createPlayer()

        player.inventory.add("air_rune_dungeoneering", 25)
        player.itemOption("Option4", "air_rune_dungeoneering")

        player.inventory.add("fire_rune_dungeoneering", 25)
        player.itemOption("Option4", "fire_rune_dungeoneering")

        assertEquals(25, player.inventory.count("fire_rune_dungeoneering"))
        assertEquals(0, player.inventory.count("air_rune_dungeoneering"))
        assertEquals(25, player.inventory.count("air_rune_dungeoneering_bound"))
        assertEquals("air_rune_dungeoneering_bound", player["dungeoneering_bound_ammo_id", ""])
        assertEquals(25, player["dungeoneering_bound_ammo_count", 0])
    }

    @Test
    fun `Remove ammo bind`() {
        val player = createPlayer()
        player["dungeoneering_bound_ammo_id"] = "katagon_arrows_bound"
        player["dungeoneering_bound_ammo_count"] = 100
        player.inventory.add("katagon_arrows", 50)
        player.inventory.add("katagon_arrows_bound", 50)

        player.itemOption("Drop", "katagon_arrows_bound")
        player.dialogueOption("confirm", "dialogue_confirm_destroy")

        assertEquals(50, player.inventory.count("katagon_arrows"))
        assertEquals(0, player.inventory.count("katagon_arrows_bound"))

        assertEquals("", player["dungeoneering_bound_ammo_id", ""])
        assertEquals(0, player["dungeoneering_bound_ammo_count", 0])
    }
}
