package world.gregs.voidps.engine.data.definition.extra

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoderTest
import world.gregs.yaml.Yaml

internal class NPCDefinitionsTest : DefinitionsDecoderTest<NPCDefinition, NPCDecoder, NPCDefinitions>() {

    override lateinit var definitions: Array<NPCDefinition>
    override val id: String = "hans"
    override val intId: Int = 0

    @BeforeEach
    override fun setup() {
        definitions = mockk(relaxed = true)
        super.setup()
    }

    override fun expected(): NPCDefinition {
        return NPCDefinition(intId, stringId = id, extras = mapOf(
            "id" to intId,
            "race" to "human",
            "examine" to "Servant of the Duke of Lumbridge."
        ))
    }

    override fun empty(): NPCDefinition {
        return NPCDefinition(-1)
    }

    override fun definitions(): NPCDefinitions {
        return NPCDefinitions(definitions)
    }

    override fun load(definitions: NPCDefinitions) {
        definitions.load(Yaml(), "../data/definitions/npcs.yml", mockk(relaxed = true))
    }
}
