package rs.dusk.engine.entity.character.contain.detail

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import rs.dusk.cache.config.data.ItemContainerDefinition
import rs.dusk.cache.config.decoder.ItemContainerDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.DefinitionsDecoderTest
import rs.dusk.engine.entity.character.contain.StackMode

internal class ContainerDefinitionsTest : DefinitionsDecoderTest<ItemContainerDefinition, ItemContainerDecoder, ContainerDefinitions>() {

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

    override fun definition(id: Int): ItemContainerDefinition {
        return ItemContainerDefinition(id)
    }

    override fun definitions(decoder: ItemContainerDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): ContainerDefinitions {
        return ContainerDefinitions(decoder, id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<ContainerDefinitions> {
        return ContainerDefinitionLoader(loader, decoder)
    }

}