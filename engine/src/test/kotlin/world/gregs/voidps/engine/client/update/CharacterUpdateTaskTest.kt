package world.gregs.voidps.engine.client.update

import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.client.update.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.player.PlayerUpdateTask
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.client.DummyClient

internal class CharacterUpdateTaskTest : KoinMock() {

    private lateinit var task: CharacterUpdateTask
    private lateinit var playerTask: PlayerUpdateTask
    private lateinit var npcTask: NPCUpdateTask

    @BeforeEach
    fun setup() {
        Players.clear()
        playerTask = mockk(relaxed = true)
        npcTask = mockk(relaxed = true)
        ZoneBatchUpdates.clear()
        task = spyk(CharacterUpdateTask(SequentialIterator(), playerTask, npcTask))
    }

    @Test
    fun `Player with client called`() {
        // Given
        Players.add(Player(index = 1, client = DummyClient(), viewport = Viewport()))
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
        Players.add(Player(index = 1))
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            playerTask.run(any())
            npcTask.run(any())
        }
    }
}
