package rs.dusk.engine.entity.character

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CharacterEffectsTest {

    lateinit var character: Character
    lateinit var effects: CharacterEffects

    @BeforeEach
    fun setup() {
        character = mockk(relaxed = true)
        effects = CharacterEffects(character)
    }

    @Test
    fun `Add effect`() {
        var called = false
        val effect = object : Effect("buff") {
            override fun onStart(character: Character) {
                assertEquals(this@CharacterEffectsTest.character, character)
                called = true
            }
        }
        assertTrue(effects.add(effect))
        assertTrue(called)
    }

    @Test
    fun `Override effect of same type`() {
        val effect = object : Effect("buff") {}
        val effect2 = object : Effect("buff") {}
        assertTrue(effects.add(effect))
        assertTrue(effects.add(effect2))
        assertEquals(effect2, effects.getOrNull("buff"))
    }

    @Test
    fun `Add immune effect`() {
        var called = false
        val effect = object : Effect("buff") {
            override fun immune(character: Character): Boolean {
                return true
            }

            override fun onStart(character: Character) {
                called = true
            }
        }
        effects.add(effect)
        assertFalse(called)
    }

    @Test
    fun `Remove effect`() {
        var called = false
        val effect = object : Effect("buff") {
            override fun onFinish(character: Character) {
                assertEquals(this@CharacterEffectsTest.character, character)
                called = true
            }
        }
        assertTrue(effects.add(effect))
        assertTrue(effects.remove(effect))
        assertNull(effects.getOrNull("buff"))
        assertTrue(called)
    }

    @Test
    fun `Can't remove non-active effect`() {
        val effect = object : Effect("buff") {}
        val inActive = object : Effect("buff") {}
        assertTrue(effects.add(effect))
        assertFalse(effects.remove(inActive))
    }

    @Test
    fun `Remove by type`() {
        var called = false
        val effect = object : Effect("buff") {
            override fun onFinish(character: Character) {
                assertEquals(this@CharacterEffectsTest.character, character)
                called = true
            }
        }
        assertTrue(effects.add(effect))
        assertTrue(effects.remove("buff"))
        assertTrue(called)
    }

    @Test
    fun `Get by type`() {
        val effect = object : Effect("buff") {}
        assertTrue(effects.add(effect))
        assertEquals(effect, effects.getOrNull("buff"))
    }

    @Test
    fun `Has by type`() {
        val effect = object : Effect("buff") {}
        assertTrue(effects.add(effect))
        assertTrue(effects.has("buff"))
        assertFalse(effects.has("other"))
    }

    @Test
    fun `Remove all effects`() {
        var called = false
        assertTrue(effects.add(object : Effect("buff") {}))
        assertTrue(effects.add(object : Effect("nerf") {
            override fun onFinish(character: Character) {
                called = true
            }
        }))
        effects.removeAll()
        assertNull(effects.getOrNull("buff"))
        assertNull(effects.getOrNull("nerf"))
        assertTrue(called)
    }
}