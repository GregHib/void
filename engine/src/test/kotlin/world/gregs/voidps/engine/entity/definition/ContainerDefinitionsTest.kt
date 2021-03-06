package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.character.contain.StackMode
import world.gregs.voidps.engine.entity.definition.load.ContainerDefinitionLoader

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