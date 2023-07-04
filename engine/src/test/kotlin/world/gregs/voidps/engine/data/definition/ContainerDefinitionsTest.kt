package world.gregs.voidps.engine.data.definition

import io.mockk.mockk
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.yaml.Yaml

internal class ContainerDefinitionsTest : DefinitionsDecoderTest<ContainerDefinition, ContainerDecoder, ContainerDefinitions>() {

    override var decoder: ContainerDecoder = ContainerDecoder()
    override lateinit var definitions: Array<ContainerDefinition>
    override val id: String = "bobs_brilliant_axes"
    override val intId: Int = 1

    override fun expected(): ContainerDefinition {
        return ContainerDefinition(intId,
            stringId = id,
            ids = IntArray(0),
            amounts = IntArray(0),
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
        return ContainerDefinitions(definitions)
    }

    override fun load(definitions: ContainerDefinitions) {
        definitions.load(Yaml(), "../data/definitions/containers.yml", mockk(relaxed = true))
    }
}
