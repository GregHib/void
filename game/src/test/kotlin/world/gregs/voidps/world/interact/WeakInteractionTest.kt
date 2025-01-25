package world.gregs.voidps.world.interact

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.interfaceSwitch
import world.gregs.voidps.world.script.walk
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class WeakInteractionTest : WorldTest() {

    @TestFactory
    fun `Interaction clears dialogue`() = listOf(
        "Interface switch",
        "Remove equipment",
        "Activate prayer",
        "Skill guide",
        "Toggle attack style"
    ).map {
        dynamicTest("$it interaction clears dialogue") {
            val player = createPlayer("player")
            player.inventory.add("ball_of_wool", 2)
            player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
            var cancelled = false
            player.weakQueue("dialogue", onCancel = { cancelled = true }) {
                npc<Pleased>("Bob", "Hello")
            }
            tick()
            assertNotNull(player.dialogue)
            assertTrue(player.queue.contains(ActionPriority.Weak))

            when (it) {
                "Interface switch" -> {
                    val wool = Item("ball_of_wool", 1)
                    player.interfaceSwitch("inventory", "inventory", "inventory", wool, wool, 0, 1)
                }
                "Remove equipment" -> player.interfaceOption("worn_equipment", "weapon_slot", "*", 0, Item("bronze_sword"))
                "Activate prayer" -> player.interfaceOption("prayer_list", "regular_prayers", "Activate", slot = 0)
                "Skill guide" -> player.interfaceOption("stats", "attack", "View")
                "Toggle attack style" -> player.interfaceOption("combat_styles", "style2", "Kick")
            }

            assertNull(player.dialogue)
            assertTrue(cancelled)
        }
    }

    @TestFactory
    fun `Interaction doesn't clear dialogue`() = listOf(
        "Clan chat setup",
        "Audio settings",
        "Music player"
    ).map {
        dynamicTest("$it interaction doesn't clear dialogue") {
            val player = createPlayer("player")
            var cancelled = false
            player.weakQueue("dialogue", onCancel = { cancelled = true }) {
                npc<Pleased>("Bob", "Hello")
            }
            tick()
            assertNotNull(player.dialogue)
            assertTrue(player.queue.contains(ActionPriority.Weak))

            when (it) {
                "Clan chat setup" -> player.interfaceOption("clan_chat", "settings", "Clan Setup")
                "Audio settings" -> player.interfaceOption("options", "audio", "Audio Settings")
                "Music player" -> player.interfaceOption("music_player", "tracks", "Play", slot = 214)
            }

            assertNotNull(player.dialogue)
            assertFalse(cancelled)
        }
    }

    @Test
    fun `Dropping an item doesn't interrupt movement`() {
        val player = createPlayer("player")
        player.inventory.add("vial")

        val target = player.tile.addY(10)
        player.walk(target)
        tick(4)
        player.interfaceOption("inventory", "inventory", "Drop", 4, Item("vial", 1), 0)
        tick(6)

        assertEquals(target, player.tile)
        assertFalse(player.inventory.contains("vial"))
    }
}