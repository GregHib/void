package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.get

@JvmInline
value class GameMapObject(val value: Int) {

    constructor(id: Int, type: Int, rotation: Int) : this(value(id, type, rotation))

    constructor(id: String, type: Int, rotation: Int) : this(get<ObjectDefinitions>().get(id).id, type, rotation)

    val id: String
        get() = def.stringId
    val intId: Int
        get() = id(value)
    val type: Int
        get() = type(value)
    val rotation: Int
        get() = rotation(value)
    val group: Int
        get() = ObjectGroup.group(type)
    val def: ObjectDefinition
        get() = get<ObjectDefinitions>().get(intId)

    companion object {
        fun value(id: Int, type: Int, rotation: Int) = rotation + (type shl 2) + (id shl 7)

        fun id(value: Int): Int = value shr 7 and 0x1ffff

        fun type(value: Int): Int = value shr 2 and 0x1f

        fun rotation(value: Int): Int = value and 0x3
    }
}