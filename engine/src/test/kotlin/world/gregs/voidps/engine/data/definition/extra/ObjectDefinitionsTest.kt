package world.gregs.voidps.engine.data.definition.extra

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoderTest
import world.gregs.voidps.engine.data.yaml.YamlParser

internal class ObjectDefinitionsTest : DefinitionsDecoderTest<ObjectDefinition, ObjectDecoder, ObjectDefinitions>() {

    override lateinit var decoder: ObjectDecoder
    override val id: String = "door_closed"
    override val intId: Int = 3

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun expected(): ObjectDefinition {
        return ObjectDefinition(intId, stringId = id, extras = mapOf("id" to intId, "examine" to "The door is closed."))
    }

    override fun empty(): ObjectDefinition {
        return ObjectDefinition(-1)
    }

    override fun definitions(): ObjectDefinitions {
        return ObjectDefinitions(decoder)
    }

    override fun load(definitions: ObjectDefinitions) {
        definitions.load(YamlParser(), "../data/definitions/objects.yml", mockk(relaxed = true))
    }
}
