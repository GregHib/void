package world.gregs.voidps.engine.data.definition.extra

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoderTest
import world.gregs.yaml.Yaml

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
        return ContainerDefinition(intId,
            stringId = id,
            extras = mapOf("id" to intId,
                "shop" to true,
                "defaults" to listOf(mapOf("bronze_pickaxe" to 10),
                    mapOf("bronze_hatchet" to 10),
                    mapOf("iron_hatchet" to 10),
                    mapOf("steel_hatchet" to 10),
                    mapOf("iron_battleaxe" to 10),
                    mapOf("steel_battleaxe" to 10),
                    mapOf("mithril_battleaxe" to 10))))
    }

    override fun empty(): ContainerDefinition {
        return ContainerDefinition(-1)
    }

    override fun definitions(): ContainerDefinitions {
        return ContainerDefinitions(decoder)
    }

    override fun load(definitions: ContainerDefinitions) {
        definitions.load(Yaml(), "../data/definitions/containers.yml", mockk(relaxed = true))
    }
}
