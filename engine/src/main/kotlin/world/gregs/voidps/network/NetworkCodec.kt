package world.gregs.voidps.network

import world.gregs.voidps.network.decode.*

class NetworkCodec {

    val decoders: Map<Int, Decoder> = mapOf(
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
        58 to emptyDecoder(4),
        GameOpcodes.CONSOLE_COMMAND to ConsoleCommandDecoder(),
        GameOpcodes.DIALOGUE_CONTINUE to DialogueContinueDecoder(),
        GameOpcodes.FLOOR_ITEM_OPTION_1 to FloorItemOption1Decoder(),
        GameOpcodes.FLOOR_ITEM_OPTION_2 to FloorItemOption2Decoder(),
        GameOpcodes.FLOOR_ITEM_OPTION_3 to FloorItemOption3Decoder(),
        GameOpcodes.FLOOR_ITEM_OPTION_4 to FloorItemOption4Decoder(),
        GameOpcodes.FLOOR_ITEM_OPTION_5 to FloorItemOption5Decoder(),
        GameOpcodes.INTEGER_ENTRY to IntegerEntryDecoder(),
        GameOpcodes.SCREEN_CLOSE to InterfaceClosedDecoder(),
        GameOpcodes.ITEM_ON_ITEM to InterfaceOnInterfaceDecoder(),
        GameOpcodes.INTERFACE_ON_NPC to InterfaceOnNpcDecoder(),
        GameOpcodes.ITEM_ON_OBJECT to InterfaceOnObjectDecoder(),
        GameOpcodes.INTERFACE_ON_PLAYER to InterfaceOnPlayerDecoder(),
        GameOpcodes.INTERFACE_OPTION_1 to InterfaceOptionDecoder(0),
        GameOpcodes.INTERFACE_OPTION_2 to InterfaceOptionDecoder(1),
        GameOpcodes.INTERFACE_OPTION_3 to InterfaceOptionDecoder(2),
        GameOpcodes.INTERFACE_OPTION_4 to InterfaceOptionDecoder(3),
        GameOpcodes.INTERFACE_OPTION_5 to InterfaceOptionDecoder(4),
        GameOpcodes.INTERFACE_OPTION_6 to InterfaceOptionDecoder(5),
        GameOpcodes.INTERFACE_OPTION_7 to InterfaceOptionDecoder(6),
        GameOpcodes.INTERFACE_OPTION_8 to InterfaceOptionDecoder(7),
        GameOpcodes.INTERFACE_OPTION_9 to InterfaceOptionDecoder(8),
        GameOpcodes.INTERFACE_OPTION_10 to InterfaceOptionDecoder(9),
        GameOpcodes.SWITCH_INTERFACE_COMPONENTS to InterfaceSwitchComponentsDecoder(),
        GameOpcodes.KEY_TYPED to KeysPressedDecoder(),
        GameOpcodes.PING_LATENCY to LatencyDecoder(),
        GameOpcodes.MOVE_CAMERA to MovedCameraDecoder(),
        GameOpcodes.MOVE_MOUSE to MovedMouseDecoder(),
        GameOpcodes.NPC_OPTION_1 to NPCOption1Decoder(),
        GameOpcodes.NPC_OPTION_2 to NPCOption2Decoder(),
        GameOpcodes.NPC_OPTION_3 to NPCOption3Decoder(),
        GameOpcodes.NPC_OPTION_4 to NPCOption4Decoder(),
        GameOpcodes.NPC_OPTION_5 to NPCOption5Decoder(),
        GameOpcodes.OBJECT_OPTION_1 to ObjectOption1Decoder(),
        GameOpcodes.OBJECT_OPTION_2 to ObjectOption2Decoder(),
        GameOpcodes.OBJECT_OPTION_3 to ObjectOption3Decoder(),
        GameOpcodes.OBJECT_OPTION_4 to ObjectOption4Decoder(),
        GameOpcodes.OBJECT_OPTION_5 to ObjectOption5Decoder(),
        GameOpcodes.PING to PingDecoder(),
        GameOpcodes.PLAYER_OPTION_1 to PlayerOption1Decoder(),
        GameOpcodes.PLAYER_OPTION_2 to PlayerOption2Decoder(),
        GameOpcodes.PLAYER_OPTION_3 to PlayerOption3Decoder(),
        GameOpcodes.PLAYER_OPTION_4 to PlayerOption4Decoder(),
        GameOpcodes.PLAYER_OPTION_5 to PlayerOption5Decoder(),
        GameOpcodes.PLAYER_OPTION_6 to PlayerOption6Decoder(),
        GameOpcodes.PLAYER_OPTION_7 to PlayerOption7Decoder(),
        GameOpcodes.PLAYER_OPTION_8 to PlayerOption8Decoder(),
        GameOpcodes.DONE_LOADING_REGION to RegionLoadedDecoder(),
        GameOpcodes.REGION_LOADING to RegionLoadingDecoder(),
        GameOpcodes.SCREEN_CHANGE to ScreenChangeDecoder(),
        GameOpcodes.STRING_ENTRY to StringEntryDecoder(),
        GameOpcodes.WALK to WalkMapDecoder(),
        GameOpcodes.MINI_MAP_WALK to WalkMiniMapDecoder(),
        GameOpcodes.CLICK to WindowClickDecoder(),
        GameOpcodes.TOGGLE_FOCUS to WindowFocusDecoder()
//        GameOpcodes.CUTSCENE_ACTION to CutsceneActionDecoder(),
//        GameOpcodes.JOIN_FRIEND_CHAT to FriendChatJoinDecoder(),
//        GameOpcodes.KICK_FRIEND_CHAT to FriendChatKickDecoder(),
//        GameOpcodes.RANK_FRIEND_CHAT to FriendChatRankDecoder(),
//        GameOpcodes.ADD_FRIEND to FriendListAddDecoder(),
//        GameOpcodes.REMOVE_FRIEND to FriendListRemoveDecoder(),
//        GameOpcodes.HYPERLINK_TEXT to HyperlinkDecoder(),
//        GameOpcodes.ADD_IGNORE to IgnoreListAddDecoder(),
//        GameOpcodes.REMOVE_IGNORE to IgnoreListRemoveDecoder(),
//        GameOpcodes.INTERFACE_ON_FLOOR_ITEM to InterfaceOnFloorItemDecoder(),
//        GameOpcodes.ONLINE_STATUS to LobbyOnlineStatusDecoder(),
//        GameOpcodes.PING_REPLY to PingReplyDecoder(),
//        GameOpcodes.PRIVATE_MESSAGE to PrivateDecoder(),
//        GameOpcodes.QUICK_PRIVATE_MESSAGE to PrivateQuickChatDecoder(),
//        GameOpcodes.PUBLIC_MESSAGE to PublicDecoder(),
//        GameOpcodes.QUICK_PUBLIC_MESSAGE to PublicQuickChatDecoder(),
//        GameOpcodes.RECEIVE_COUNT to ReceiveCountDecoder(),
//        GameOpcodes.REFLECTION_RESPONSE to ReflectionResponseDecoder(),
//        GameOpcodes.REPORT_ABUSE to ReportAbuseDecoder(),
//        GameOpcodes.RESUME_PLAYER_OBJ_DIALOGUE to ResumeObjDialogueDecoder(),
//        GameOpcodes.OTHER_TELEPORT to SecondaryTeleportDecoder(),
//        GameOpcodes.COLOUR_ID to SkillCapeColourDecoder(),
//        GameOpcodes.TOOLKIT_PREFERENCES to ToolkitPreferencesDecoder(),
//        GameOpcodes.UNKNOWN to UnknownDecoder(),
//        GameOpcodes.SCRIPT_4701 to UnknownScriptDecoder(),
//        GameOpcodes.IN_OUT_SCREEN to WindowHoveredDecoder(),
//        GameOpcodes.REFRESH_WORLDS to WorldListRefreshDecoder(),
//        GameOpcodes.WORLD_MAP_CLICK to WorldMapCloseDecoder()
    )

    fun registerHandler(opcode: Int, handler: Handler) {
        val decoder = getDecoder(opcode) ?: throw IllegalArgumentException("Missing decoder $opcode $handler")
        if (decoder.handler != null) {
            throw IllegalArgumentException("Cannot have duplicate handlers $opcode $handler")
        }
        decoder.handler = handler
    }

    fun getDecoder(opcode: Int): Decoder? {
        return decoders[opcode]
    }

    companion object {
        private fun emptyDecoder(length: Int) = object : Decoder(length) {}
    }
}