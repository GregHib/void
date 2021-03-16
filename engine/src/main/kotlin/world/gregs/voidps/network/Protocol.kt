package world.gregs.voidps.network

import world.gregs.voidps.network.decode.*
import world.gregs.voidps.network.handle.*

object Protocol {
    const val PLAYER_OPTION = 0
    const val INTERFACE_SPRITE = 75
    const val INTERFACE_COLOUR = 32
    const val INTERFACE_COMPONENT_SETTINGS = 34
    const val INTERFACE_REFRESH = 72
    const val INTERFACE_OPEN = 37
    const val NPC_UPDATING = 118
    const val INTERFACE_SCROLL_VERTICAL = 81
    const val INTERFACE_ITEM = 90
    const val OBJECT_PRE_FETCH = 79
    const val RUN_ENERGY = 4
    const val CLIENT_VARBIT = 73
    const val FLOOR_ITEM_REMOVE = 27
    const val INTERFACE_PLAYER_BODY = 114
    const val CHUNK_CLEAR = 33
    const val INTERFACE_ANIMATION = 24
    const val OBJECT_ADD = 82
    const val TILE_TEXT = 46
    const val INTERFACE_TEXT = 10
    const val INTERFACE_ITEMS = 20
    const val CLIENT_VARP_LARGE = 44
    const val GRAPHIC_AREA = 84
    const val REGION = 19
    const val INTERFACE_CUSTOM_HEAD = 95
    const val OBJECT_REMOVE = 58
    const val UPDATE_CHUNK = 41
    const val OBJECT_ANIMATION_SPECIFIC = 17
    const val FLOOR_ITEM_ADD = 63
    const val SCRIPT = 8
    const val LOGOUT = 55
    const val CLIENT_VARC_STR = 53
    const val INTERFACE_MODEL = 23
    const val FLOOR_ITEM_REVEAL = 109
    const val PROJECTILE_ADD = 15
    const val MINI_SOUND = 11
    const val INTERFACE_WINDOW = 102
    const val PLAYER_UPDATING = 105
    const val INTERFACE_CLOSE = 112
    const val INTERFACE_ITEMS_UPDATE = 28
    const val FLOOR_ITEM_UPDATE = 71
    const val CLIENT_VARBIT_LARGE = 68
    const val PROJECTILE_DISPLACE = 51
    const val SKILL_LEVEL = 47
    const val OBJECT_ANIMATION = 29
    const val INTERFACE_NPC_HEAD = 86
    const val CLIENT_VARP = 7
    const val PLAYER_WEIGHT = 91
    const val CHAT = 52
    const val INTERFACE_COMPONENT_POSITION = 45
    const val INTERFACE_PLAYER_OTHER_BODY = 57
    const val CLIENT_VARC = 3
    const val CLIENT_VARC_LARGE = 16
    const val INTERFACE_PLAYER_HEAD = 77
    const val INTERFACE_COMPONENT_VISIBILITY = 108
    const val SOUND_AREA = 5
    const val INTERFACE_COMPONENT_ORIENTATION = 88
    const val DYNAMIC_REGION = 70
}

val protocol: Map<Int, Decoder> = mapOf(
    22 to FloorItemOption1Decoder(FloorItemOptionHandler()),
    16 to FloorItemOption2Decoder(FloorItemOptionHandler()),
    45 to FloorItemOption3Decoder(FloorItemOptionHandler()),
    24 to FloorItemOption4Decoder(FloorItemOptionHandler()),
    26 to FloorItemOption5Decoder(FloorItemOptionHandler()),

    53 to ConsoleCommandDecoder(ConsoleCommandHandler()),
    2 to DialogueContinueDecoder(DialogueContinueHandler()),

    32 to IntegerEntryDecoder(IntEntryHandler()),
    70 to InterfaceClosedDecoder(InterfaceClosedHandler()),
    72 to InterfaceOnInterfaceDecoder(),
    61 to InterfaceOnNpcDecoder(),
    54 to InterfaceOnObjectDecoder(),
    48 to InterfaceOnPlayerDecoder(),
    23 to InterfaceOptionDecoder(0, InterfaceOptionHandler()),
    59 to InterfaceOptionDecoder(1, InterfaceOptionHandler()),
    9 to InterfaceOptionDecoder(2, InterfaceOptionHandler()),
    15 to InterfaceOptionDecoder(3, InterfaceOptionHandler()),
    17 to InterfaceOptionDecoder(4, InterfaceOptionHandler()),
    39 to InterfaceOptionDecoder(5, InterfaceOptionHandler()),
    33 to InterfaceOptionDecoder(6, InterfaceOptionHandler()),
    60 to InterfaceOptionDecoder(7, InterfaceOptionHandler()),
    11 to InterfaceOptionDecoder(8, InterfaceOptionHandler()),
    42 to InterfaceOptionDecoder(9, InterfaceOptionHandler()),
    78 to InterfaceSwitchComponentsDecoder(InterfaceSwitchHandler()),

    55 to MovedCameraDecoder(),
    69 to KeysPressedDecoder(),
    83 to MovedMouseDecoder(),

    63 to NPCOption1Decoder(NPCOptionHandler()),
    29 to NPCOption2Decoder(NPCOptionHandler()),
    5 to NPCOption3Decoder(NPCOptionHandler()),
    62 to NPCOption4Decoder(NPCOptionHandler()),
    65 to NPCOption5Decoder(NPCOptionHandler()),

    27 to ObjectOption1Decoder(ObjectOptionHandler()),
    36 to ObjectOption2Decoder(ObjectOptionHandler()),
    80 to ObjectOption3Decoder(ObjectOptionHandler()),
    56 to ObjectOption4Decoder(ObjectOptionHandler()),
    38 to ObjectOption5Decoder(ObjectOptionHandler()),

    0 to PingDecoder(),
    21 to LatencyDecoder(),

    25 to PlayerOption1Decoder(PlayerOptionHandler()),
    12 to PlayerOption2Decoder(PlayerOptionHandler()),
    79 to PlayerOption3Decoder(PlayerOptionHandler()),
    44 to PlayerOption4Decoder(PlayerOptionHandler()),
    81 to PlayerOption5Decoder(PlayerOptionHandler()),
    51 to PlayerOption6Decoder(PlayerOptionHandler()),
    57 to PlayerOption7Decoder(PlayerOptionHandler()),
    18 to PlayerOption8Decoder(PlayerOptionHandler()),

    4 to RegionLoadedDecoder(RegionLoadedHandler()),
    47 to RegionLoadingDecoder(),

    7 to ScreenChangeDecoder(ScreenChangeHandler()),
    1 to StringEntryDecoder(StringEntryHandler()),

    35 to WalkMapDecoder(WalkMapHandler()),
    82 to WalkMiniMapDecoder(WalkMiniMapHandler()),

    49 to WindowClickDecoder(),
    8 to WindowFocusDecoder(),

    13 to emptyDecoder(2),
    74 to emptyDecoder(-1),
    77 to emptyDecoder(-1),
    66 to emptyDecoder(-1),
    19 to emptyDecoder(-1),
    76 to emptyDecoder(4),
    41 to emptyDecoder(-1),
    46 to emptyDecoder(2),
    71 to emptyDecoder(2),
    10 to emptyDecoder(-1),
    50 to emptyDecoder(-1),
    68 to emptyDecoder(2),
    43 to emptyDecoder(-1),
    34 to emptyDecoder(15),
    40 to emptyDecoder(12),
    75 to emptyDecoder(3),
    30 to emptyDecoder(4),
    31 to emptyDecoder(1),
    28 to emptyDecoder(2),
    67 to emptyDecoder(-1),
    20 to emptyDecoder(-1),
    14 to emptyDecoder(-1),
    84 to emptyDecoder(-1),
    6 to emptyDecoder(-1),
    64 to emptyDecoder(-1),
    3 to emptyDecoder(-1),
    37 to emptyDecoder(2),
    73 to emptyDecoder(-1),
    52 to emptyDecoder(4),
    58 to emptyDecoder(4)
)

private fun emptyDecoder(length: Int) = object : Decoder(length) {}