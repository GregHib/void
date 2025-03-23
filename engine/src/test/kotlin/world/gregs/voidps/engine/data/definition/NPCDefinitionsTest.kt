package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder

internal class NPCDefinitionsTest : DefinitionsDecoderTest<NPCDefinition, NPCDecoder, NPCDefinitions>() {

    override var decoder: NPCDecoder = NPCDecoder(member = true)
    override lateinit var definitions: Array<NPCDefinition>
    override val id: String = "hans"
    override val intId: Int = 0

    override fun expected(): NPCDefinition {
        return NPCDefinition(intId, stringId = id, extras = mapOf(
            "race" to "human",
            "wander_radius" to 4,
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
        val uri = NPCDefinitionsTest::class.java.getResource("test-npc.toml")!!
        definitions.load(listOf(uri.path))
    }
}
