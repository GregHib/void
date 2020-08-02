package rs.dusk.engine.client.ui

import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

abstract class InterfaceSendTest : InterfaceTest() {

    abstract fun sendStrings(name: String, component: String): Boolean
    abstract fun sendIdString(id: Int, component: String): Boolean
    abstract fun sendStringId(name: String, id: Int): Boolean
    abstract fun sendIds(id: Int, component: Int): Boolean

    abstract fun expected(inter: Interface, component: Int)

    @Test
    fun `Interface send component player head names`() {
        names["name"] = 1
        val inter = Interface(id = 1, components = mapOf(2 to "component_name"))
        interfaces[1] = inter
        val result = sendStrings("name", "component_name")
        assertTrue(result)
        verify { expected(inter, 2) }
    }

    @Test
    fun `Interface send component player head id and name`() {
        val inter = Interface(id = 1, components = mapOf(2 to "component_name"))
        interfaces[1] = inter
        val result = sendIdString(1, "component_name")
        assertTrue(result)
        verify { expected(inter, 2) }
    }

    @Test
    fun `Interface send component player head name and id`() {
        names["name"] = 1
        val inter = Interface(id = 1, components = mapOf(2 to "component_name"))
        interfaces[1] = inter
        val result = sendStringId("name", 2)
        assertTrue(result)
        verify { expected(inter, 2) }
    }

    @Test
    fun `Interface send component player head ids`() {
        val inter = Interface(id = 1, components = mapOf(2 to "component_name"))
        interfaces[1] = inter
        val result = sendIds(1, 2)
        assertTrue(result)
        verify { expected(inter, 2) }
    }

    @Test
    fun `Interface send player head invalid component`() {
        val inter = Interface(id = 1, components = mapOf(2 to "component_name"))
        interfaces[1] = inter
        val result = manager.sendPlayerHead(1, 3)
        assertFalse(result)
        verify(exactly = 0) { expected(inter, any()) }
    }

}
