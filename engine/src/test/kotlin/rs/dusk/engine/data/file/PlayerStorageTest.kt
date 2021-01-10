package rs.dusk.engine.data.file

import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import rs.dusk.engine.entity.character.player.Player

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 04, 2020
 */
@ExtendWith(MockKExtension::class)
internal class PlayerStorageTest {

    /*
        inline functions can't be mocked so we can only test negative here.
     */
    @Test
    fun load() {
        // Given
        mockkStatic(FileLoader::class)
        val storage = PlayerStorage("test")
        // When
        val result = storage.load("name")
        // Then
        assertNull(result)
    }

    @Test
    fun save() {
        // Given
        val loader = mockk<FileLoader>(relaxed = true)
        val storage = PlayerStorage("test")
        val player = mockk<Player>(relaxed = true)
        // When
        storage.save("name", player)
        // Then
        verify { loader.save("test\\name.json", player) }
    }
}