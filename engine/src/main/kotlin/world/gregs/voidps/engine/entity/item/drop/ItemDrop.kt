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
    override val chance: Int,
) : Drop {

    fun toItem(): Item {
        if (name == "nothing" || name == "") {
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