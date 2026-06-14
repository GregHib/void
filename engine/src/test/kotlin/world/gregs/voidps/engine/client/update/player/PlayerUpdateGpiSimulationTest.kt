package world.gregs.voidps.engine.client.update.player

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

/**
 * Simulates the full player-info (GPI) cycle between two players and decodes
 * the viewer's bit stream exactly like the 634 client does, to catch any
 * encode/decode desync (which kicks the viewing client with an "nsn" error).
 */
internal class PlayerUpdateGpiSimulationTest : KoinMock() {

    private lateinit var task: PlayerUpdateTask

    private lateinit var mover: Player // "A" - first account, the one walking
    private lateinit var viewer: Player // "B" - second account, receives the updates
    private var moverTile = Tile(3000, 3000)
    private lateinit var moverVisuals: PlayerVisuals
    private lateinit var viewerVisuals: PlayerVisuals
    private lateinit var viewport: Viewport

    @BeforeEach
    fun setup() {
        mockkObject(Players)
        task = PlayerUpdateTask()
        moverVisuals = PlayerVisuals(mockk(relaxed = true))
        viewerVisuals = PlayerVisuals(mockk(relaxed = true))
        mover = mockk(relaxed = true)
        viewer = mockk(relaxed = true)
        every { mover.index } returns 1
        every { viewer.index } returns 2
        every { mover.tile } answers { moverTile }
        every { viewer.tile } returns Tile(3001, 3000)
        every { mover.visuals } returns moverVisuals
        every { viewer.visuals } returns viewerVisuals
        every { mover.client } returns null
        every { viewer.client } returns null
        every { Players.indexed(any()) } returns null
        every { Players.indexed(1) } returns mover
        every { Players.indexed(2) } returns viewer

        // Viewer login state (RegionLoading.loadCallback)
        viewport = Viewport()
        viewport.seen(viewer)
        viewport.seen(mover)
        viewport.players.addSelf(viewer)
        viewerVisuals.flag(0x4) // appearance flagged on login
    }

    @AfterEach
    fun teardown() {
        unmockkObject(Players)
    }

    @Test
    fun `Client decode stays in sync when the other player moves`() {
        val decoder = ClientGpi(self = 2)
        decoder.position(1, moverTile)

        // Tick 1: viewer's first update, mover added as global
        tick(decoder)
        assertEquals(true, decoder.local[1], "Mover should be local to viewer after tick 1")

        // Tick 2: nobody moves
        tick(decoder)

        // Tick 3: mover walks one tile west
        moverTile = moverTile.addX(-1)
        moverVisuals.walkStep = 3
        tick(decoder)
        assertEquals(moverTile, decoder.tile(1), "Client position for mover out of sync after walking")

        // Tick 4: idle again
        tick(decoder)

        // Tick 5: mover runs two tiles east
        moverTile = moverTile.addX(2)
        moverVisuals.walkStep = 4
        moverVisuals.runStep = 4
        tick(decoder)
        assertEquals(moverTile, decoder.tile(1), "Client position for mover out of sync after running")
    }

    @Test
    fun `Client decode stays in sync through teleports and region crossing`() {
        val decoder = ClientGpi(self = 2)
        decoder.position(1, moverTile)
        tick(decoder)
        tick(decoder)

        // Teleport within view
        moverTile = moverTile.add(5, 5)
        moverVisuals.tele = true
        tick(decoder)
        assertEquals(moverTile, decoder.tile(1), "Client position for mover out of sync after teleport")

        // Walk across a region boundary
        moverTile = Tile(3007, 3000)
        decoder.position(1, moverTile)
        viewport.seen(mover)
        tick(decoder)
        moverTile = moverTile.addX(1) // 3008 crosses region 46->47
        moverVisuals.walkStep = 4
        tick(decoder)
        assertEquals(moverTile, decoder.tile(1), "Client position for mover out of sync after region cross")

        // Teleport far away (out of view) and walk - removed then no updates
        moverTile = Tile(3200, 3200)
        moverVisuals.tele = true
        tick(decoder)
        assertEquals(false, decoder.local[1], "Mover should be removed from local view")
        moverTile = moverTile.addX(1)
        moverVisuals.walkStep = 4
        tick(decoder)

        // Teleport back into view
        moverTile = Tile(3002, 3000)
        moverVisuals.tele = true
        tick(decoder)
        tick(decoder)
        assertEquals(true, decoder.local[1], "Mover should be re-added to local view")
    }

    private fun tick(decoder: ClientGpi) {
        val sync = viewport.playerChanges
        val updates = viewport.playerUpdates
        task.processLocals(viewer, sync, updates, viewport.players, viewport, true)
        task.processLocals(viewer, sync, updates, viewport.players, viewport, false)
        task.processGlobals(viewer, sync, updates, viewport.players, viewport, true)
        task.processGlobals(viewer, sync, updates, viewport.players, viewport, false)
        decoder.decode(sync.toArray())
        sync.position(0)
        updates.position(0)
        viewport.shift()
        viewport.players.update()
        // PlayerResetTask at the start of next tick
        moverVisuals.reset()
        viewerVisuals.reset()
    }

    /**
     * Re-implementation of the 634 client's player info decoding
     * (void-client Class348_Sub40_Sub18.method3094, Class286_Sub9.method2177,
     * Class211.method1538, Class318_Sub1_Sub3.method2413)
     */
    private class ClientGpi(self: Int) {
        private val flags = ByteArray(MAX_PLAYERS)
        val local = BooleanArray(MAX_PLAYERS)
        private val tiles = IntArray(MAX_PLAYERS)
        private var locals = mutableListOf<Int>()
        private var globals = mutableListOf<Int>()

        init {
            local[self] = true
            for (i in 1 until MAX_PLAYERS) {
                (if (local[i]) locals else globals).add(i)
            }
        }

        fun position(index: Int, tile: Tile) {
            tiles[index] = tile.id
        }

        fun tile(index: Int): Tile = Tile(tiles[index])

        fun decode(bytes: ByteArray) {
            val reader = ArrayReader(bytes)
            var skip: Int
            // Pass 1: active locals
            reader.startBitAccess()
            skip = 0
            for (index in locals) {
                if (flags[index].toInt() and 0x1 != 0) {
                    continue
                }
                if (skip > 0) {
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                    skip--
                    continue
                }
                if (reader.readBits(1) == 0) {
                    skip = readSkip(reader)
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                } else {
                    readLocal(reader, index)
                }
            }
            reader.stopBitAccess()
            check(skip == 0) { "nsn0: $skip unskipped" }
            // Pass 2: idle locals
            reader.startBitAccess()
            skip = 0
            for (index in locals) {
                if (flags[index].toInt() and 0x1 == 0) {
                    continue
                }
                if (skip > 0) {
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                    skip--
                    continue
                }
                if (reader.readBits(1) == 0) {
                    skip = readSkip(reader)
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                } else {
                    readLocal(reader, index)
                }
            }
            reader.stopBitAccess()
            check(skip == 0) { "nsn1: $skip unskipped" }
            // Pass 3: idle globals
            reader.startBitAccess()
            skip = 0
            for (index in globals) {
                if (flags[index].toInt() and 0x1 == 0) {
                    continue
                }
                if (skip > 0) {
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                    skip--
                    continue
                }
                if (reader.readBits(1) == 0) {
                    skip = readSkip(reader)
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                } else if (readGlobal(reader, index)) {
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                }
            }
            reader.stopBitAccess()
            check(skip == 0) { "nsn2: $skip unskipped" }
            // Pass 4: active globals
            reader.startBitAccess()
            skip = 0
            for (index in globals) {
                if (flags[index].toInt() and 0x1 != 0) {
                    continue
                }
                if (skip > 0) {
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                    skip--
                    continue
                }
                if (reader.readBits(1) == 0) {
                    skip = readSkip(reader)
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                } else if (readGlobal(reader, index)) {
                    flags[index] = (flags[index].toInt() or 0x2).toByte()
                }
            }
            reader.stopBitAccess()
            check(skip == 0) { "nsn3: $skip unskipped" }
            // End of cycle: shift flags, rebuild lists
            for (i in 1 until MAX_PLAYERS) {
                flags[i] = (flags[i].toInt() shr 1).toByte()
            }
            locals = mutableListOf()
            globals = mutableListOf()
            for (i in 1 until MAX_PLAYERS) {
                (if (local[i]) locals else globals).add(i)
            }
        }

        private fun readSkip(reader: ArrayReader): Int = when (reader.readBits(2)) {
            0 -> 0
            1 -> reader.readBits(5)
            2 -> reader.readBits(8)
            else -> reader.readBits(11)
        }

        private fun readLocal(reader: ArrayReader, index: Int) {
            val block = reader.readBits(1) == 1
            when (reader.readBits(2)) {
                0 -> if (!block) {
                    // Removed; re-read as global position update
                    local[index] = false
                    if (reader.readBits(1) != 0) {
                        readGlobal(reader, index)
                    }
                }
                1 -> {
                    val direction = reader.readBits(3)
                    tiles[index] = walk(Tile(tiles[index]), direction).id
                }
                2 -> {
                    val direction = reader.readBits(4)
                    tiles[index] = run(Tile(tiles[index]), direction).id
                }
                3 -> {
                    if (reader.readBits(1) == 0) {
                        val packed = reader.readBits(12)
                        val tile = Tile(tiles[index])
                        var x = (packed shr 5) and 0x1f
                        if (x > 15) {
                            x -= 32
                        }
                        var y = packed and 0x1f
                        if (y > 15) {
                            y -= 32
                        }
                        tiles[index] = tile.add(x, y, (packed shr 10) and 0x3).id
                    } else {
                        val packed = reader.readBits(30)
                        val tile = Tile(tiles[index])
                        var x = (packed shr 14) and 0x3fff
                        if (x > 8191) {
                            x -= 16384
                        }
                        var y = packed and 0x3fff
                        if (y > 8191) {
                            y -= 16384
                        }
                        tiles[index] = tile.add(x, y, (packed shr 28) and 0x3).id
                    }
                }
            }
        }

        /** @return true when the player was added to local players */
        private fun readGlobal(reader: ArrayReader, index: Int): Boolean {
            when (reader.readBits(2)) {
                0 -> {
                    if (reader.readBits(1) != 0) {
                        readGlobal(reader, index)
                    }
                    val x = reader.readBits(6)
                    val y = reader.readBits(6)
                    reader.readBits(1) // appearance in updates block
                    val region = Tile(tiles[index]).regionLevel
                    tiles[index] = Tile(region.x * 64 + x, region.y * 64 + y, region.level).id
                    local[index] = true
                    return true
                }
                1 -> reader.readBits(2) // height change
                2 -> {
                    val packed = reader.readBits(5)
                    val direction = packed and 0x7
                    val level = packed shr 3
                    val region = Tile(tiles[index]).regionLevel
                    val moved = walk(Tile(region.x, region.y), direction)
                    tiles[index] = Tile(moved.x * 64, moved.y * 64, (region.level + level) and 0x3).id
                }
                else -> {
                    val packed = reader.readBits(18)
                    val region = Tile(tiles[index]).regionLevel
                    val x = (region.x + ((packed shr 8) and 0xff)) and 0xff
                    val y = (region.y + (packed and 0xff)) and 0xff
                    val level = (region.level + (packed shr 16)) and 0x3
                    tiles[index] = Tile(x * 64, y * 64, level).id
                }
            }
            return false
        }

        private fun walk(tile: Tile, direction: Int): Tile = when (direction) {
            0 -> tile.add(-1, -1)
            1 -> tile.add(0, -1)
            2 -> tile.add(1, -1)
            3 -> tile.add(-1, 0)
            4 -> tile.add(1, 0)
            5 -> tile.add(-1, 1)
            6 -> tile.add(0, 1)
            else -> tile.add(1, 1)
        }

        private fun run(tile: Tile, direction: Int): Tile = when (direction) {
            0 -> tile.add(-2, -2)
            1 -> tile.add(-1, -2)
            2 -> tile.add(0, -2)
            3 -> tile.add(1, -2)
            4 -> tile.add(2, -2)
            5 -> tile.add(-2, -1)
            6 -> tile.add(2, -1)
            7 -> tile.add(-2, 0)
            8 -> tile.add(2, 0)
            9 -> tile.add(-2, 1)
            10 -> tile.add(2, 1)
            11 -> tile.add(-2, 2)
            12 -> tile.add(-1, 2)
            13 -> tile.add(0, 2)
            14 -> tile.add(1, 2)
            else -> tile.add(2, 2)
        }
    }
}
