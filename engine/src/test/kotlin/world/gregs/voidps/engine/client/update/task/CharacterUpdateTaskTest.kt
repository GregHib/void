package world.gregs.voidps.engine.client.update.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.update.task.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.task.player.PlayerPostUpdateTask
import world.gregs.voidps.engine.client.update.task.player.PlayerUpdateTask
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.script.KoinMock

internal class CharacterUpdateTaskTest : KoinMock() {

    private lateinit var task: CharacterUpdateTask
    private lateinit var players: Players
    private lateinit var playerTask: PlayerUpdateTask
    private lateinit var npcTask: NPCUpdateTask
    private lateinit var postTask: PlayerPostUpdateTask
    override val modules = listOf(
        eventModule,
        entityListModule
    )

    @BeforeEach
    fun setup() {
        players = mockk(relaxed = true)
        playerTask = mockk(relaxed = true)
        npcTask = mockk(relaxed = true)
        postTask = mockk(relaxed = true)
        task = spyk(CharacterUpdateTask(SequentialIterator(), players, playerTask, npcTask, postTask))
    }

    @Test
    fun `Player with client called`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        every { player.client } returns mockk()
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
        every { player.client } returns null
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