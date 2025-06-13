package world.gregs.voidps.engine.client.update

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.client.update.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.player.PlayerUpdateTask
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.script.KoinMock

internal class CharacterUpdateTaskTest : KoinMock() {

    private lateinit var task: CharacterUpdateTask
    private lateinit var players: Players
    private lateinit var npcs: NPCs
    private lateinit var playerTask: PlayerUpdateTask
    private lateinit var npcTask: NPCUpdateTask
    private lateinit var batches: ZoneBatchUpdates
    override val modules = listOf(
        module {
            single { NPCs(get(), get(), get(), AreaDefinitions()) }
            single { Players() }
        },
    )

    @BeforeEach
    fun setup() {
        players = mockk(relaxed = true)
        playerTask = mockk(relaxed = true)
        npcs = mockk(relaxed = true)
        npcTask = mockk(relaxed = true)
        batches = mockk(relaxed = true)
        task = spyk(CharacterUpdateTask(SequentialIterator(), players, playerTask, npcTask, batches))
    }

    @Test
    fun `Player with client called`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        every { player.networked } returns true
        every { players.iterator() } returns mutableListOf(player).iterator()
        // When
        task.run()
        // Then
        verify {
            playerTask.run(any())
            npcTask.run(any())
        }
    }

    @Test
    fun `Player without client not called`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        every { player.networked } returns false
        every { players.iterator() } returns mutableListOf(player).iterator()
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            playerTask.run(any())
            npcTask.run(any())
        }
    }
}
