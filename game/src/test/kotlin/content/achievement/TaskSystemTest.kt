package content.achievement

import WorldTest
import interfaceOption
import itemOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class TaskSystemTest : WorldTest() {

    @Test
    fun `Dismissing unrelated completed task details doesn't remove pinned task`() {
        val player = createPlayer("achiever", Tile(3207, 3224, 2))
        player["unstable_foundations"] = "completed"

        player.interfaceOption("task_system", "task_6", "Pin/Unpin Task")

        assertEquals(65, player["task_pinned", -1])
        assertEquals(6, player["task_pin_slot", 0])

        // Complete an unrelated task
        val ladder = objects[Tile(3207, 3223, 2), "lumbridge_castle_ladder"]!!
        player.objectOption(ladder, "Climb-up")
        tick(4)
        player.interfaceOption("task_popup", "details", "Details")
        player.interfaceOption("task_system", "ok", "OK")

        assertEquals(65, player["task_pinned", -1])
        assertEquals(6, player["task_pin_slot", 0])
    }

    @Test
    fun `Dismissing the completed task details removes the pinned task`() {
        val player = createPlayer("achiever", Tile(3207, 3224, 2))
        player["unstable_foundations"] = "completed"

        player.interfaceOption("task_system", "task_5", "Pin/Unpin Task")

        assertEquals(64, player["task_pinned", -1])
        assertEquals(5, player["task_pin_slot", 0])

        val ladder = objects[Tile(3207, 3223, 2), "lumbridge_castle_ladder"]!!
        player.objectOption(ladder, "Climb-up")
        tick(4)
        player.interfaceOption("task_popup", "details", "Details")
        player.interfaceOption("task_system", "ok", "OK")

        assertEquals(-1, player["task_pinned", -1])
        assertEquals(0, player["task_pin_slot", 0])
    }

    @Test
    fun `Dismissing the task summary removes the pinned task`() {
        val player = createPlayer("achiever", Tile(3207, 3224, 2))
        player["unstable_foundations"] = "completed"

        player.interfaceOption("task_system", "task_5", "Pin/Unpin Task")

        assertEquals(64, player["task_pinned", -1])
        assertEquals(5, player["task_pin_slot", 0])

        val ladder = objects[Tile(3207, 3223, 2), "lumbridge_castle_ladder"]!!
        player.objectOption(ladder, "Climb-up")
        tick(4)
        player.interfaceOption("task_system", "task_5", "Select Task")
        player.interfaceOption("task_system", "ok", "OK")

        assertEquals(-1, player["task_pinned", -1])
        assertEquals(0, player["task_pin_slot", 0])
    }

    @Test
    fun `Dismissing different task details after pinned doesn't remove pinned task`() {
        val player = createPlayer("achiever", Tile(3207, 3224, 2))
        player["unstable_foundations"] = "completed"

        player.interfaceOption("task_system", "task_5", "Pin/Unpin Task")

        assertEquals(64, player["task_pinned", -1])
        assertEquals(5, player["task_pin_slot", 0])

        // Complete the task
        val ladder = objects[Tile(3207, 3223, 2), "lumbridge_castle_ladder"]!!
        player.objectOption(ladder, "Climb-up")
        tick(4)
        player.tele(3205, 3228, 0)
        tick()
        // Complete a different task
        player.inventory.add("bronze_dagger")
        player.itemOption("Wield", "bronze_dagger")
        // Dismiss
        player.interfaceOption("task_popup", "details", "Details")
        player.interfaceOption("task_system", "ok", "OK")

        assertEquals(64, player["task_pinned", -1])
        assertEquals(5, player["task_pin_slot", 0])
    }
}