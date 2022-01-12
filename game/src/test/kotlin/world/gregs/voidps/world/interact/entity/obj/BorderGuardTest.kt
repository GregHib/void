package world.gregs.voidps.world.interact.entity.obj

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.engine.client.instruction.handle.WalkHandler
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.world.script.WorldMock

internal class BorderGuardTest : WorldMock() {

    private lateinit var collision: Collisions
    private lateinit var barbarianTileData: ByteArray
    private lateinit var barbarianObjectData: ByteArray
    private lateinit var varrockTileData: ByteArray
    private lateinit var varrockObjectData: ByteArray
    private val handler = WalkHandler()


    @BeforeEach
    fun start() {
        collision = get()
    }

    private fun loadBarbarianVillage() {
        // Region 12341
        barbarianTileData = BorderGuardTest::class.java.getResourceAsStream("barbarian_village_tiles.dat")?.readAllBytes()!!
        barbarianObjectData = BorderGuardTest::class.java.getResourceAsStream("barbarian_village_objects.dat")?.readAllBytes()!!
        every { get<Cache>().getFile(Indices.MAPS, "m48_53", any()) } returns barbarianTileData
        every { get<Cache>().getFile(Indices.MAPS, "l48_53", any()) } returns barbarianObjectData
    }

    private fun loadVarrock() {
        // Region 13108
        varrockTileData = BorderGuardTest::class.java.getResourceAsStream("varrock_south_east_tiles.dat")?.readAllBytes()!!
        varrockObjectData = BorderGuardTest::class.java.getResourceAsStream("varrock_south_east_objects.dat")?.readAllBytes()!!
        every { get<Cache>().getFile(Indices.MAPS, "m51_52", any()) } returns varrockTileData
        every { get<Cache>().getFile(Indices.MAPS, "l51_52", any()) } returns varrockObjectData
    }

    @Test
    fun `Can walk west through a vertical border`() = runBlocking(Dispatchers.Default) {
        loadBarbarianVillage()
        val player = createPlayer("player", Tile(3112, 3420))

        handler.validate(player, Walk(3106, 3421))
        tick(8)

        assertEquals(Tile(3106, 3421), player.tile)
    }

    @Test
    fun `Can walk east through a vertical border`() = runBlocking(Dispatchers.Default) {
        loadBarbarianVillage()
        val player = createPlayer("player", Tile(3106, 3421))

        handler.validate(player, Walk(3112, 3420))
        tick(8)

        assertEquals(Tile(3112, 3420), player.tile)
    }

    @Test
    fun `Can walk south through a horizontal border`() = runBlocking(Dispatchers.Default) {
        loadVarrock()
        val player = createPlayer("player", Tile(3292, 3387))

        handler.validate(player, Walk(3293, 3383))
        tick(6)

        assertEquals(Tile(3293, 3383), player.tile)
    }

    @Test
    fun `Can walk north through a horizontal border`() = runBlocking(Dispatchers.Default) {
        loadVarrock()
        val player = createPlayer("player", Tile(3293, 3383))

        handler.validate(player, Walk(3292, 3387))
        tick(6)

        assertEquals(Tile(3292, 3387), player.tile)
    }

}