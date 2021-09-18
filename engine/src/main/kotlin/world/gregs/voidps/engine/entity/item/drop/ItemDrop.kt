package world.gregs.voidps.engine.entity.item.drop

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import world.gregs.voidps.engine.entity.item.Item

data class ItemDrop(
    val name: String,
    @get:JsonSerialize(using = RangeSerializer::class)
    val amount: IntRange,
    val chance: Int = 1,
) : Drop {

    init {
        assert(chance > 0) { "Item must have a positive chance." }
    }

    fun toItem(): Item {
        if (name == "nothing" || name.isBlank()) {
            return Item.EMPTY
        }
        return Item(name, amount.random())
    }

    companion object {
        private class RangeSerializer : JsonSerializer<IntRange>() {
            override fun serialize(value: IntRange, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
                if (value.first == value.last) {
                    jsonGenerator.writeObject(value.first)
                } else {
                    jsonGenerator.writeObject("${value.first}-${value.last}")
                }
            }
        }
    }
}