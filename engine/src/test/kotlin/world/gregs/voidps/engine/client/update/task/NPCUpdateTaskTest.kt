package world.gregs.voidps.engine.client.update.task

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.task.npc.NPCUpdateTask
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCTrackingSet
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.visual.npc.getTurn
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.updateNPCs

/**
 * @author GregHib <greg@gregs.world>
 * @since May 16, 2020
 */
internal class NPCUpdateTaskTest : KoinMock() {

    lateinit var task: NPCUpdateTask
    lateinit var players: Players
    override val modules = listOf(
        eventModule,
        entityListModule
    )

    @BeforeEach
    fun setup() {
        players = mockk()
        task = spyk(NPCUpdateTask(players))
    }

    @Test
    fun `Called for each player with sessions`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        every { players.forEach(any()) } answers {
            val block = arg<(Player) -> Unit>(0)
            block.invoke(player)
        }
        every { players.getAtIndex(any()).hint(Player::class) } returns null
        mockkStatic("world.gregs.voidps.network.encode.NPCUpdateEncoderKt")
        val client: Client = mockk(relaxed = true)
        every { player.client } returns client
        every { client.updateNPCs(any(), any()) } just Runs
        // When
        task.run()
        // Then
        verify {
            task.runAsync(player)
            client.updateNPCs(any(), any())
        }
    }

    @Test
    fun `Player without session not called`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        every { players.forEach(any()) } answers {
            val block = arg<(Player) -> Unit>(0)
            block.invoke(player)
        }
        every {
            hint(Player::class)
            players.getAtIndex(any())
        } returns null
        every { player.client } returns null
        every { task.processLocals(any(), any(), any()) } just Runs
        every { task.processAdditions(any(), any(), any(), any()) } just Runs
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            task.processLocals(any(), any(), any())
            task.processAdditions(any(), any(), any(), any())
        }
    }

    @Test
    fun `Local npc removed`() {
        // Given
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { entities.remove } returns mutableSetOf(npc)
        every { entities.current } returns linkedSetOf(npc)
        every { npc.change } returns LocalChange.Update
        every { npc.visuals } returns mockk(relaxed = true)
        every { npc.visuals.update } returns byteArrayOf()
        // When
        task.processLocals(sync, updates, entities)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 3)
            sync.finishBitAccess()
        }
        verify(exactly = 0) {
            updates.writeBytes(npc.visuals.update!!)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local npc walk and update`(update: Boolean) {
        // Given
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { entities.current } returns linkedSetOf(npc)
        every { npc.change } returns LocalChange.Walk
        val direction = 4
        every { npc.walkDirection } returns direction
        every { npc.visuals.update } returns if (update) byteArrayOf() else null
        // When
        task.processLocals(sync, updates, entities)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 1)
            sync.writeBits(3, direction)
            sync.writeBits(1, update)
            if (update) {
                updates.writeBytes(npc.visuals.update!!)
            }
            sync.finishBitAccess()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local npc crawl and update`(update: Boolean) {
        // Given
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { entities.current } returns linkedSetOf(npc)
        every { npc.change } returns LocalChange.Crawl
        val direction = 4
        every { npc.walkDirection } returns direction
        every { npc.visuals.update } returns if (update) byteArrayOf() else null
        // When
        task.processLocals(sync, updates, entities)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 2)
            sync.writeBits(1, false)
            sync.writeBits(3, direction)
            sync.writeBits(1, update)
            if (update) {
                updates.writeBytes(npc.visuals.update!!)
            }
            sync.finishBitAccess()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local npc run and update`(update: Boolean) {
        // Given
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { entities.current } returns linkedSetOf(npc)
        every { npc.change } returns LocalChange.Run
        val walkDirection = 4
        val runDirection = 8
        every { npc.walkDirection } returns walkDirection
        every { npc.runDirection } returns runDirection
        every { npc.visuals.update } returns if (update) byteArrayOf() else null
        // When
        task.processLocals(sync, updates, entities)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 2)
            sync.writeBits(1, true)
            sync.writeBits(3, walkDirection)
            sync.writeBits(3, runDirection)
            sync.writeBits(1, update)
            if (update) {
                updates.writeBytes(npc.visuals.update!!)
            }
            sync.finishBitAccess()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `NPC Additions`(update: Boolean) {
        // Given
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.npc.TurnKt")
        val client: Player = mockk(relaxed = true)
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val index = 1
        val direction = 12345
        val id = 20
        every { client.tile } returns value(Tile(0, 0))
        every { npc.tile } returns value(Tile(5, 3, 1))
        every { npc.index } returns index
        every { npc.id } returns id
        every { npc.getTurn() } returns mockk(relaxed = true)
        every { npc.getTurn().direction } returns direction
        every { entities.add } returns linkedSetOf(npc)
        every { npc.visuals.addition } returns if (update) byteArrayOf() else null
        // When
        task.processAdditions(sync, updates, client, entities)
        // Then
        verifyOrder {
            sync.writeBits(15, index)
            sync.writeBits(2, 1)
            sync.writeBits(1, false)
            sync.writeBits(5, 35)
            sync.writeBits(5, 37)
            sync.writeBits(3, 2)
            sync.writeBits(1, update)
            sync.writeBits(14, id)
            if (update) {
                updates.writeBytes(npc.visuals.addition!!)
            }
            sync.writeBits(15, -1)
            sync.finishBitAccess()
        }
    }
}