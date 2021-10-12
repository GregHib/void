package world.gregs.voidps.network

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.decode.*

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
    const val BATCH_UPDATE_CHUNK = 48
    const val OBJECT_ANIMATION_SPECIFIC = 17
    const val FLOOR_ITEM_ADD = 63
    const val SCRIPT = 8
    const val LOGOUT = 55
    const val CLIENT_VARC_STR_LARGE = 53
    const val CLIENT_VARC_STR = 92
    const val INTERFACE_MODEL = 23
    const val FLOOR_ITEM_REVEAL = 109
    const val PROJECTILE_ADD = 15
    const val SOUND_EFFECT = 103
    const val MIDI_SOUND = 106
    const val PLAY_MUSIC = 85
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
    const val MIDI_AREA = 66
    const val SOUND_AREA = 5
    const val INTERFACE_COMPONENT_ORIENTATION = 88
    const val DYNAMIC_REGION = 70
    const val JINGLE = 116

    object Batch {
        const val FLOOR_ITEM_REMOVE = 0
        const val OBJECT_ADD = 1
        const val OBJECT_ANIMATION_SPECIFIC = 2
        const val OBJECT_REMOVE = 3
        const val FLOOR_ITEM_REVEAL = 4
        const val PROJECTILE_ADD = 5
        const val FLOOR_ITEM_ADD = 6
        const val FLOOR_ITEM_UPDATE = 7
        const val OBJECT_PRE_FETCH = 8
        const val PROJECTILE_DISPLACE = 9
        const val MIDI_AREA = 10
        const val SOUND_AREA = 11
        const val GRAPHIC_AREA = 12
        const val TILE_TEXT = 13
    }
}

val protocol: Map<Int, Decoder> = mapOf(
    22 to FloorItemOption1Decoder(),
    16 to FloorItemOption2Decoder(),
    45 to FloorItemOption3Decoder(),
    24 to FloorItemOption4Decoder(),
    26 to FloorItemOption5Decoder(),
    53 to ConsoleCommandDecoder(),
    2 to DialogueContinueDecoder(),
    32 to IntegerEntryDecoder(),
    70 to InterfaceClosedDecoder(),
    72 to InterfaceOnInterfaceDecoder(),
    61 to InterfaceOnNpcDecoder(),
    54 to InterfaceOnObjectDecoder(),
    48 to InterfaceOnPlayerDecoder(),
    23 to InterfaceOptionDecoder(0),
    59 to InterfaceOptionDecoder(1),
    9 to InterfaceOptionDecoder(2),
    15 to InterfaceOptionDecoder(3),
    17 to InterfaceOptionDecoder(4),
    39 to InterfaceOptionDecoder(5),
    33 to InterfaceOptionDecoder(6),
    60 to InterfaceOptionDecoder(7),
    11 to InterfaceOptionDecoder(8),
    42 to InterfaceOptionDecoder(9),
    78 to InterfaceSwitchComponentsDecoder(),
    55 to MovedCameraDecoder(),
    69 to KeysPressedDecoder(),
    83 to MovedMouseDecoder(),
    63 to NPCOption1Decoder(),
    29 to NPCOption2Decoder(),
    5 to NPCOption3Decoder(),
    62 to NPCOption4Decoder(),
    65 to NPCOption5Decoder(),
    27 to ObjectOption1Decoder(),
    36 to ObjectOption2Decoder(),
    80 to ObjectOption3Decoder(),
    56 to ObjectOption4Decoder(),
    38 to ObjectOption5Decoder(),
    0 to PingDecoder(),
    21 to LatencyDecoder(),
    25 to PlayerOption1Decoder(),
    12 to PlayerOption2Decoder(),
    79 to PlayerOption3Decoder(),
    44 to PlayerOption4Decoder(),
    81 to PlayerOption5Decoder(),
    51 to PlayerOption6Decoder(),
    57 to PlayerOption7Decoder(),
    18 to PlayerOption8Decoder(),
    4 to RegionLoadedDecoder(),
    47 to RegionLoadingDecoder(),
    7 to ScreenChangeDecoder(),
    1 to StringEntryDecoder(),
    35 to WalkMapDecoder(),
    82 to WalkMiniMapDecoder(),
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

private fun emptyDecoder(length: Int) = object : Decoder(length) {
    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
    }
}