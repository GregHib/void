package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.load.NPCDefinitionLoader

internal class NPCDefinitionsTest : DefinitionsDecoderTest<NPCDefinition, NPCDecoder, NPCDefinitions>() {

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun definition(id: Int): NPCDefinition {
        return NPCDefinition(id)
    }

    override fun definitions(decoder: NPCDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): NPCDefinitions {
        return NPCDefinitions(decoder, id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<NPCDefinitions> {
        return NPCDefinitionLoader(loader, decoder)
    }
}