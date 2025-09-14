package world.gregs.voidps.engine.client.ui.chat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextKtTest {
    @Test
    fun `Test indefinite articles`() {
        assertEquals(" a", "test".an())
        assertEquals(" an", "world/gregs/voidps/engine/event".an())
        assertEquals("", "tests".an())
    }
}
