package world.gregs.voidps.engine.data.definition.extra

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionsDecoderTest

internal class ContainerDefinitionsTest : DefinitionsDecoderTest<ContainerDefinition, ContainerDecoder, ContainerDefinitions>() {

    override lateinit var decoder: ContainerDecoder
    override val id: String = "bobs_brilliant_axes"
    override val intId: Int = 1

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun expected(): ContainerDefinition {
        return ContainerDefinition(intId, stringId = id, extras = mapOf("id" to intId, "shop" to true))
    }

    override fun empty(): ContainerDefinition {
        return ContainerDefinition(-1)
    }

    override fun definitions(): ContainerDefinitions {
        return ContainerDefinitions(decoder)
    }

    override fun load(definitions: ContainerDefinitions) {
        definitions.load(FileStorage(), "../data/definitions/containers.yml")
    }
}
