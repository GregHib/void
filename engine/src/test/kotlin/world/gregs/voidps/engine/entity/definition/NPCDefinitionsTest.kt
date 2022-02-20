package world.gregs.voidps.engine.entity.definition

/* FIXME
internal class NPCDefinitionsTest : DefinitionsDecoderTest<NPCDefinition, NPCDecoder, NPCDefinitions>() {

    override val allowsModification: Boolean = true

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id, "mutable" to 0)
    }

    override fun populated(id: Int): Map<String, Any> {
        return map(id)
    }

    override fun definition(id: Int): NPCDefinition {
        return NPCDefinition(id, stringId = id.toString())
    }

    override fun definitions(decoder: NPCDecoder): NPCDefinitions {
        return NPCDefinitions(decoder)
    }

    override fun load(definitions: NPCDefinitions, id: Map<String, Map<String, Any>>, names: Map<Int, String>) {
        definitions.load(id)
        definitions.names = names
    }
}*/
