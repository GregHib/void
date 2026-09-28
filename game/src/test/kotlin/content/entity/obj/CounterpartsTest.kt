package content.entity.obj

import WorldTest
import content.entity.obj.door.Door
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CounterpartsTest : WorldTest() {

    @Test
    fun `Counterpart params point to existing objects`() {
        val missing = mutableListOf<String>()
        for (definition in ObjectDefinitions.definitions) {
            for (key in listOf("opened", "closed", "stump", "empty", "depleted")) {
                val id: String = definition.getOrNull(key) ?: continue
                if (!ObjectDefinitions.contains(id)) {
                    missing.add("${definition.stringId}.$key = $id")
                }
            }
        }
        assertTrue(missing.isEmpty(), "Unknown counterparts: $missing")
    }

    @Test
    fun `Door opens into a model shared with another door`() {
        val player = createPlayer(Tile(3200, 3200))
        val door = createObject("door_6_closed", Tile(3201, 3200), shape = ObjectShape.WALL_STRAIGHT, rotation = 0)

        Door.openDoor(player, door)

        assertEquals("door_opened", GameObjects.getLayer(Door.tile(door, 1), ObjectLayer.WALL)?.id)
    }
}
