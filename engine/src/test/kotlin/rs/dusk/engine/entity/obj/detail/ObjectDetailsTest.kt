package rs.dusk.engine.entity.obj.detail

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.DetailsDecoderTest

internal class ObjectDetailsTest : DetailsDecoderTest<ObjectDefinition, ObjectDecoder, ObjectDetails>() {

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun detail(id: Int): ObjectDefinition {
        return ObjectDefinition(id)
    }

    override fun details(decoder: ObjectDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): ObjectDetails {
        return ObjectDetails(decoder, id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<ObjectDetails> {
        return ObjectDetailsLoader(decoder, loader)
    }
}