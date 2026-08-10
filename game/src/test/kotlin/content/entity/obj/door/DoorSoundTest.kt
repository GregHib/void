package content.entity.obj.door

import WorldTest
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.network.login.protocol.encode.zone.SoundAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ZoneUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import kotlin.test.assertEquals

class DoorSoundTest : WorldTest() {

    /**
     * The first gate under the Paterdomus mausoleum, the only "grate" material door.
     * There's no `grate_gate_open` sound for it to resolve to, and reverting a
     * temporary object used to play nothing at all.
     */
    @Test
    fun `Mausoleum gate plays a grate sound when it shuts behind you`() {
        val player = createPlayer(Tile(3405, 9894))
        player["priest_in_peril"] = "go_back"
        val gate = GameObjects.find(Tile(3405, 9895), "pip_underground_door1_closed")
        mockkObject(ZoneBatchUpdates)
        try {
            player.objectOption(gate, "Open")
            tick(6)

            val updates = mutableListOf<ZoneUpdate>()
            verify { ZoneBatchUpdates.add(any<Zone>(), capture(updates)) }
            val sounds = updates.filterIsInstance<SoundAddition>()
            assertEquals(listOf(68), sounds.map { it.id }) // grate_close
        } finally {
            unmockkObject(ZoneBatchUpdates)
        }
    }
}
