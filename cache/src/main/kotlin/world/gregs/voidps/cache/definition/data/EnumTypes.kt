package world.gregs.voidps.cache.definition.data

object EnumTypes {
    const val STRING = 's'
    const val INT = 'i'
    const val STRUCT = 'J'
    const val JINGLE = 'j'
    const val ITEM = 'o'
    const val ITEM_2 = 'O'
    const val SPRITE = 'd'
    const val MODEL = 'm'
    const val ID_KIT = 'K'
    const val COMPONENT = 'I'
    const val MAP_AREA = '`'
    const val SKILL = 'S'
    const val TILE = 'C'
    const val CHAT_TYPE = 'c'
    const val ANIM = 'A'
    const val NPC = 'n'
    const val ENUM = 'g'
    const val INV = 'v'

    fun name(char: Char) = when (char) {
        STRING -> "string"
        INT -> "int"
        STRUCT -> "struct"
        JINGLE -> "jingle"
        ITEM -> "item"
        ITEM_2 -> "item"
        SPRITE -> "sprite"
        MODEL -> "model"
        ID_KIT -> "idkit"
        COMPONENT -> "interface"
        MAP_AREA -> "map_area"
        SKILL -> "skill"
        TILE -> "tile"
        CHAT_TYPE -> "chat type"
        ANIM -> "anim"
        NPC -> "npc"
        ENUM -> "enum"
        INV -> "inv"
        else -> "null"
    }

    fun char(name: String) = when (name) {
        "string" -> STRING
        "int" -> INT
        "struct" -> STRUCT
        "jingle" -> JINGLE
        "item" -> ITEM
        "sprite" -> SPRITE
        "model" -> MODEL
        "id_kit" -> ID_KIT
        "interface" -> COMPONENT
        "map_area" -> MAP_AREA
        "skill" -> SKILL
        "tile" -> TILE
        "chat_type" -> CHAT_TYPE
        "anim" -> ANIM
        "npc" -> NPC
        "enum" -> ENUM
        "inv" -> INV
        else -> null
    }
}