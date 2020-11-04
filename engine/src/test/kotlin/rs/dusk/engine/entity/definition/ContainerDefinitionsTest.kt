package rs.dusk.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import rs.dusk.cache.config.data.ContainerDefinition
import rs.dusk.cache.config.decoder.ContainerDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.character.contain.StackMode
import rs.dusk.engine.entity.definition.load.ContainerDefinitionLoader

internal class ContainerDefinitionsTest : DefinitionsDecoderTest<ContainerDefinition, ContainerDecoder, ContainerDefinitions>() {

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf(
            "id" to id,
            "width" to 2,
            "height" to 3,
            "stack" to "Always"
        )
    }

    override fun populated(id: Int):  Map<String, Any> {
        return mapOf(
            "id" to id,
            "width" to 2,
            "height" to 3,
            "stack" to StackMode.Always
        )
    }

    override fun definition(id: Int): ContainerDefinition {
        return ContainerDefinition(id)
    }

    override fun definitions(decoder: ContainerDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): ContainerDefinitions {
        return ContainerDefinitions(decoder, id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<ContainerDefinitions> {
        return ContainerDefinitionLoader(loader, decoder)
    }

}