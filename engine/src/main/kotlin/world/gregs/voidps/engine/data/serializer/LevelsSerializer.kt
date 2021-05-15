package world.gregs.voidps.engine.data.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import world.gregs.voidps.engine.entity.character.player.skill.Levels

class LevelsSerializer : JsonSerializer<Levels>() {
    override fun serialize(value: Levels, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeObject(value.getOffsets())
    }
}