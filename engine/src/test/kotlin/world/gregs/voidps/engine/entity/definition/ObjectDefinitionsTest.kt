package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.load.ObjectDefinitionLoader

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
        return ObjectDefinition(id)
    }

    override fun definitions(decoder: ObjectDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): ObjectDefinitions {
        return ObjectDefinitions(decoder, id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<ObjectDefinitions> {
        return ObjectDefinitionLoader(decoder, loader)
    }
}