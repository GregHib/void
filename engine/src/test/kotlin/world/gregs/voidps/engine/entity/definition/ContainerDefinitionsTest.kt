package world.gregs.voidps.engine.entity.definition

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.entity.character.contain.StackMode

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

    override fun populated(id: Int): Map<String, Any> {
        return mapOf(
            "id" to id,
            "width" to 2,
            "height" to 3,
            "stack" to StackMode.Always
        )
    }

    override fun definition(id: Int): ContainerDefinition {
        return ContainerDefinition(id, stringId = id.toString())
    }

    override fun definitions(decoder: ContainerDecoder): ContainerDefinitions {
        return ContainerDefinitions(decoder)
    }

    override fun load(definitions: ContainerDefinitions, id: Map<String, Map<String, Any>>, names: Map<Int, String>) {
        definitions.load(id)
        definitions.names = names
    }

}