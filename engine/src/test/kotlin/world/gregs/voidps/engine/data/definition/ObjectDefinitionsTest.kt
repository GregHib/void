package world.gregs.voidps.engine.data.definition

import io.mockk.every
import io.mockk.mockk
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder

internal class ObjectDefinitionsTest : DefinitionsDecoderTest<ObjectDefinition, ObjectDecoder, ObjectDefinitions>() {

    override var decoder: ObjectDecoder = ObjectDecoder(member = true, lowDetail = false)
    override lateinit var definitions: Array<ObjectDefinition>
    override val id: String = "door_closed"
    override val intId: Int = 3

    override fun expected(): ObjectDefinition {
        return ObjectDefinition(intId, stringId = id, extras = mutableMapOf("examine" to "The door is closed."))
    }

    override fun empty(): ObjectDefinition {
        return ObjectDefinition(-1)
    }

    override fun definitions(): ObjectDefinitions {
        return ObjectDefinitions(definitions)
    }

    override fun load(definitions: ObjectDefinitions) {
        val uri = ItemDefinitionsTest::class.java.getResource("test-object.toml")!!
        val itemDefinitions = mockk<ItemDefinitions>(relaxed = true)
        every { itemDefinitions.get(any<Int>()) } returns ItemDefinition.EMPTY
        every { itemDefinitions.get(any<String>()) } returns ItemDefinition.EMPTY
        definitions.load(uri.path)
    }
}
