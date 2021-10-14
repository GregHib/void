package world.gregs.voidps.world.interact.entity.obj

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.instruction.handle.WalkHandler
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.objectOption

internal class ObjectTest : WorldMock() {

    private lateinit var collision: Collisions
    private lateinit var tileData: ByteArray
    private lateinit var objectData: ByteArray
    private val handler = WalkHandler()


    @BeforeEach
    fun start() {
        collision = get()
        // Region 12850 data
        tileData = ObjectTest::class.java.getResourceAsStream("lumbridge_tiles.dat")?.readAllBytes()!!
        objectData = ObjectTest::class.java.getResourceAsStream("lumbridge_objects.dat")?.readAllBytes()!!
    }

    private fun loadLumbridge() {
        every { get<Cache>().getFile(Indices.MAPS, "m50_50", any()) } returns tileData
        every { get<Cache>().getFile(Indices.MAPS, "l50_50", any()) } returns objectData
    }

    @Test
    fun `Can't walk through a door`() = runBlocking(Dispatchers.Default) {
        loadLumbridge()
        val player = createPlayer("player", Tile(3227, 3214))

        handler.validate(player, Walk(3226, 3214))
        tick(1)

        assertEquals(Tile(3227, 3214), player.tile)
    }

    @Test
    fun `Can open and walk through a door`() = runBlocking(Dispatchers.Default) {
        loadLumbridge()
        every { get<ObjectDecoder>().get(36846) } returns ObjectDefinition(
            id = 36846, // door_627
            name = "Door"
        )
        val player = createPlayer("player", Tile(3227, 3214))
        val door = get<Objects>()[Tile(3226, 3214)].first()

        player.objectOption(door, "Open")
        handler.validate(player, Walk(3226, 3214))
        tick(2)

        assertEquals(Tile(3226, 3214), player.tile)
    }

    @Test
    fun `Ladder ascending`() = runBlocking(Dispatchers.Default) {
        loadLumbridge()
        val player = createPlayer("player", Tile(3229, 3214))
        val ladder = get<Objects>()[Tile(3229, 3213)].first()

        player.objectOption(ladder, "Climb-up")
        tick(1)

        assertEquals(1, player.tile.plane)
    }

    @Test
    fun `Ladder descending`() = runBlocking(Dispatchers.Default) {
        loadLumbridge()
        val player = createPlayer("player", Tile(3229, 3214, 1))
        // The one in Objects has wrong id as configReplace is disabled.
        val ladder = GameObject(intId = 36769, tile = Tile(3229, 3213, 1), type = 22, rotation = 3)
        player.objectOption(ladder, "Climb-down")
        tick(1)

        assertEquals(0, player.tile.plane)
    }

}