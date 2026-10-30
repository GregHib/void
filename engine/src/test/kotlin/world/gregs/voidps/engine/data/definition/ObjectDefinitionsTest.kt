package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import java.io.File

internal class ObjectDefinitionsTest : DefinitionsDecoderTest<ObjectDefinition, ObjectDecoder, ObjectDefinitions>() {

    override var decoder: ObjectDecoder = ObjectDecoder(member = true, lowDetail = false)
    override lateinit var definitions: Array<ObjectDefinition>
    override val id: String = "door_closed"
    override val intId: Int = 3

    override fun expected(): ObjectDefinition = ObjectDefinition(intId, stringId = id, params = mutableMapOf(Params.EXAMINE to "The door is closed."))

    override fun empty(): ObjectDefinition = ObjectDefinition(-1)

    override fun definitions(): ObjectDefinitions = ObjectDefinitions.init(definitions)

    override fun load(definitions: ObjectDefinitions) {
        val uri = ObjectDefinitionsTest::class.java.getResource("test-object.toml")!!
        ObjectDefinitions.load(listOf(uri.path))
    }

    @Test
    fun `Two string ids can't share an int id`(@TempDir dir: File) {
        val file = dir.resolve("duplicate.objs.toml")
        file.writeText("[door_closed]\nid = 3\n\n[door_2_closed]\nid = 3\n")
        val definitions = definitions()

        assertThrows<IllegalArgumentException> {
            definitions.load(listOf(file.path))
        }
    }

    @Test
    fun `Definitions can be reloaded`() {
        val definitions = definitions()
        load(definitions)

        assertDoesNotThrow {
            load(definitions)
        }
    }
}
