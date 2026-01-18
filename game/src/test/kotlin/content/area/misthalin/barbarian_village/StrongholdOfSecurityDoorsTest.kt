package content.area.misthalin.barbarian_village

import FakeRandom
import WorldTest
import content.entity.player.dialogue.continueDialogue
import intEntry
import messages
import net.pearx.kasechange.toSentenceCase
import objectOption
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.suspend.ContinueSuspension
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class StrongholdOfSecurityDoorsTest : WorldTest() {

    @TestFactory
    fun `Doors give questions`() = listOf(
        Tile(1878, 5223) to "gate_of_war",
        Tile(2044, 5239) to "rickety_door",
        Tile(2148, 5299) to "oozing_barrier",
        Tile(2356, 5221) to "portal_of_death",
    ).map { (tile, id) ->
        dynamicTest("${id.toSentenceCase()} door gives questions") {
            val player = createPlayer(tile)
            val door = objects.find(tile, id)

            player["stronghold_safe_space"] = true
            player.objectOption(door, "Open")
            tick()

            assertNotNull(player.dialogue)
        }
    }

    @TestFactory
    fun `Enter through doors`() = listOf(
        Tile(1878, 5223) to "gate_of_war",
        Tile(2044, 5239) to "rickety_door",
        Tile(2148, 5299) to "oozing_barrier",
        Tile(2356, 5221) to "portal_of_death",
    ).map { (tile, id) ->
        dynamicTest("Enter through ${id.toSentenceCase()} door") {
            val player = createPlayer(tile)
            val door = objects.find(tile, id)

            player["stronghold_safe_space"] = false
            player.objectOption(door, "Open")
            tick(5)

            println(player.messages)
            println(player.tile)
            assertNull(player.dialogue)
            assertNotEquals(tile, player.tile)
        }
    }

    @TestFactory
    fun `Answer correct questions`() = listOf(3, 1, 2, 3, 1, 2, 1, 3, 3, 3, 1, 2, 1, 1, 3, 2, 2, 2, 2, 2, 2, 1, 1, 3, 1, 3, 2, 2, 3).mapIndexed { index, answer ->
        dynamicTest("Answer question $index with option $answer") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int): Int = index
            })
            val tile = Tile(1878, 5223)
            val player = createPlayer(tile)
            val door = objects.find(tile, "gate_of_war")
            player["stronghold_safe_space"] = true

            player.objectOption(door, "Open")
            tick()
            var counter = 0
            while (player.dialogueSuspension is ContinueSuspension && counter++ < 10) {
                player.continueDialogue()
                tick()
            }
            player.intEntry(answer)

            counter = 0
            while (player.dialogueSuspension is ContinueSuspension && counter++ < 10) {
                player.continueDialogue()
                tick()
            }

            assertNotEquals(tile, player.tile)
        }
    }
}
