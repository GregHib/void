package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder

internal class ObjectDefinitionsTest : DefinitionsDecoderTest<ObjectDefinition, ObjectDecoder, ObjectDefinitions>() {

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun definition(id: Int): ObjectDefinition {
        return ObjectDefinition(id, stringId = id.toString())
    }

    override fun definitions(decoder: ObjectDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): ObjectDefinitions {
        return ObjectDefinitions(decoder).apply {
            load(id)
            this.names = names
        }
    }
}