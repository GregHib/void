package world.gregs.voidps.network.codec.game

import org.koin.dsl.module
import world.gregs.voidps.network.codec.Codec
import world.gregs.voidps.network.codec.game.decode.*
import world.gregs.voidps.network.codec.game.encode.*

val gameCodec = module {
    single { ChatEncoder() }
    single { ChunkClearEncoder() }
    single { ChunkUpdateEncoder() }
    single { ContainerItemsEncoder() }
    single { ContextMenuOptionEncoder() }
    single { DynamicMapRegionEncoder() }
    single { FloorItemAddEncoder() }
    single { FloorItemRemoveEncoder() }
    single { FloorItemRevealEncoder() }
    single { FloorItemUpdateEncoder() }
    single { GraphicAreaEncoder() }
    single { InterfaceCloseEncoder() }
    single { InterfaceHeadNPCEncoder() }
    single { InterfaceHeadPlayerEncoder() }
    single { InterfaceItemEncoder() }
    single { InterfaceItemUpdateEncoder() }
    single { InterfaceAnimationEncoder() }
    single { InterfaceOpenEncoder() }
    single { InterfaceSettingsEncoder() }
    single { InterfaceSpriteEncoder() }
    single { InterfaceTextEncoder() }
    single { InterfaceUpdateEncoder() }
    single { InterfaceVisibilityEncoder() }
    single { LogoutLobbyEncoder() }
    single { LogoutEncoder() }
    single { MapRegionEncoder() }
    single { NPCUpdateEncoder() }
    single { ObjectAddEncoder() }
    single { ObjectAnimationEncoder() }
    single { ObjectAnimationSpecificEncoder() }
    single { ObjectCustomiseEncoder() }
    single { ObjectPreloadEncoder() }
    single { ObjectRemoveEncoder() }
    single { PlayerUpdateEncoder() }
    single { ProjectileAddEncoder() }
    single { ProjectileHalfSquareEncoder() }
    single { RunEnergyEncoder() }
    single { ScriptEncoder() }
    single { SkillLevelEncoder() }
    single { SoundAreaEncoder() }
    single { TextTileEncoder() }
    single { VarbitLargeEncoder() }
    single { VarbitEncoder() }
    single { VarcLargeEncoder() }
    single { VarcEncoder() }
    single { VarcStrEncoder() }
    single { VarpLargeEncoder() }
    single { VarpEncoder() }
    single { WeightEncoder() }
}

class GameCodec : Codec() {

    override fun load(args: Array<out Any?>) {
//        registerDecoder(GameOpcodes.AP_COORD_T, APCoordinateDecoder())
//        registerDecoder(GameOpcodes.CHAT_TYPE, ChatTypeDecoder())
        registerEmptyDecoder(13, 2)
//        registerEmptyDecoder(15, 8)
//        registerEmptyDecoder(56, 7)
//        registerEmptyDecoder(53, -1)
        registerEmptyDecoder(74, -1)
        registerEmptyDecoder(61, 11)
        registerEmptyDecoder(77, -1)
        registerEmptyDecoder(66, -1)
        registerEmptyDecoder(19, -1)
        registerEmptyDecoder(18, 3)
//        registerEmptyDecoder(35, 5)
        registerEmptyDecoder(16, 7)
        registerEmptyDecoder(26, 7)
//        registerEmptyDecoder(38, 7)
        registerEmptyDecoder(76, 4)
//        registerEmptyDecoder(59, 8)
        registerEmptyDecoder(24, 7)
        registerEmptyDecoder(41, -1)
        registerEmptyDecoder(46, 2)
//        registerEmptyDecoder(39, 8)
//        registerEmptyDecoder(0, 0)
        registerEmptyDecoder(25, 3)
        registerEmptyDecoder(78, 16)
        registerEmptyDecoder(71, 2)
//        registerEmptyDecoder(60, 8)
        registerEmptyDecoder(12, 3)
        registerEmptyDecoder(51, 3)
//        registerEmptyDecoder(80, 7)
//        registerEmptyDecoder(23, 8)
//        registerEmptyDecoder(42, 8)
//        registerEmptyDecoder(55, 4)
//        registerEmptyDecoder(9, 8)
        registerEmptyDecoder(10, -1)
        registerEmptyDecoder(79, 3)
//        registerEmptyDecoder(82, 18)
//        registerEmptyDecoder(11, 8)
        registerEmptyDecoder(50, -1)
//        registerEmptyDecoder(62, 3)
        registerEmptyDecoder(68, 2)
//        registerEmptyDecoder(47, 4)
//        registerEmptyDecoder(69, -1)
//        registerEmptyDecoder(29, 3)
//        registerEmptyDecoder(4, 0)
        registerEmptyDecoder(43, -1)
        registerEmptyDecoder(34, 15)
        registerEmptyDecoder(40, 12)
        registerEmptyDecoder(72, 16)
        registerEmptyDecoder(32, 4)
        registerEmptyDecoder(75, 3)
        registerEmptyDecoder(54, 15)
//        registerEmptyDecoder(63, 3)
        registerEmptyDecoder(30, 4)
        registerEmptyDecoder(31, 1)
        registerEmptyDecoder(70, 0)
//        registerEmptyDecoder(2, 6)
        registerEmptyDecoder(28, 2)
        registerEmptyDecoder(67, -1)
        registerEmptyDecoder(20, -1)
        registerEmptyDecoder(1, -1)
//        registerEmptyDecoder(83, -1)
//        registerEmptyDecoder(27, 7)
        registerEmptyDecoder(81, 3)
        registerEmptyDecoder(14, -1)
        registerEmptyDecoder(57, 3)
//        registerEmptyDecoder(36, 7)
        registerEmptyDecoder(84, -1)
        registerEmptyDecoder(45, 7)
        registerEmptyDecoder(6, -1)
        registerEmptyDecoder(64, -1)
//        registerEmptyDecoder(33, 8)
//        registerEmptyDecoder(21, 2)
//        registerEmptyDecoder(5, 3)
        registerEmptyDecoder(3, -1)
//        registerEmptyDecoder(17, 8)
        registerEmptyDecoder(44, 3)
        registerEmptyDecoder(48, 11)
        registerEmptyDecoder(7, 6)
        registerEmptyDecoder(37, 2)
        registerEmptyDecoder(73, -1)
        registerEmptyDecoder(52, 4)
        registerEmptyDecoder(58, 4)
//        registerEmptyDecoder(8, 1)
        registerEmptyDecoder(22, 7)
//        registerEmptyDecoder(65, 3)
//        registerEmptyDecoder(49, 6)

//        registerDecoder(GameOpcodes.CLAN_CHAT_KICK, ClanChatKickDecoder())
//        registerDecoder(GameOpcodes.CLAN_FORUM_THREAD, ClanForumThreadDecoder())
//        registerDecoder(GameOpcodes.CLAN_NAME, ClanNameDecoder())
//        registerDecoder(GameOpcodes.CLAN_SETTINGS_UPDATE, ClanSettingsUpdateDecoder())
        registerDecoder(GameOpcodes.CONSOLE_COMMAND, ConsoleCommandDecoder())
//        registerDecoder(GameOpcodes.CUTSCENE_ACTION, CutsceneActionDecoder())
        registerDecoder(GameOpcodes.DIALOGUE_CONTINUE, DialogueContinueDecoder())
//        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_1, FloorItemOptionDecoder(0))
//        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_2, FloorItemOptionDecoder(1))
//        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_3, FloorItemOptionDecoder(2))
//        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_4, FloorItemOptionDecoder(3))
//        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_5, FloorItemOptionDecoder(4))
//        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_6, FloorItemOptionDecoder(5))
//        registerDecoder(GameOpcodes.JOIN_FRIEND_CHAT, FriendChatJoinDecoder())
//        registerDecoder(GameOpcodes.KICK_FRIEND_CHAT, FriendChatKickDecoder())
//        registerDecoder(GameOpcodes.RANK_FRIEND_CHAT, FriendChatRankDecoder())
//        registerDecoder(GameOpcodes.ADD_FRIEND, FriendListAddDecoder())
//        registerDecoder(GameOpcodes.REMOVE_FRIEND, FriendListRemoveDecoder())
//        registerDecoder(GameOpcodes.HYPERLINK_TEXT, HyperlinkDecoder())
//        registerDecoder(GameOpcodes.ADD_IGNORE, IgnoreListAddDecoder())
//        registerDecoder(GameOpcodes.REMOVE_IGNORE, IgnoreListRemoveDecoder())
//        registerDecoder(GameOpcodes.ENTER_INTEGER, IntegerEntryDecoder())
//        registerDecoder(GameOpcodes.SCREEN_CLOSE, InterfaceClosedDecoder())
//        registerDecoder(GameOpcodes.INTERFACE_ON_FLOOR_ITEM, InterfaceOnFloorItemDecoder())
//        registerDecoder(GameOpcodes.ITEM_ON_ITEM, InterfaceOnInterfaceDecoder())
//        registerDecoder(GameOpcodes.INTERFACE_ON_NPC, InterfaceOnNpcDecoder())
//        registerDecoder(GameOpcodes.ITEM_ON_OBJECT, InterfaceOnObjectDecoder())
//        registerDecoder(GameOpcodes.INTERFACE_ON_PLAYER, InterfaceOnPlayerDecoder())
        registerDecoder(GameOpcodes.INTERFACE_OPTION_1, InterfaceOptionDecoder(0))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_2, InterfaceOptionDecoder(1))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_3, InterfaceOptionDecoder(2))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_4, InterfaceOptionDecoder(3))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_5, InterfaceOptionDecoder(4))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_6, InterfaceOptionDecoder(5))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_7, InterfaceOptionDecoder(6))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_8, InterfaceOptionDecoder(7))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_9, InterfaceOptionDecoder(8))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_10, InterfaceOptionDecoder(9))
//        registerDecoder(GameOpcodes.SWITCH_INTERFACE_COMPONENTS, InterfaceSwitchComponentsDecoder())
        registerDecoder(GameOpcodes.KEY_TYPED, KeysPressedDecoder())
        registerDecoder(GameOpcodes.PING_LATENCY, LatencyDecoder())
//        registerDecoder(GameOpcodes.ONLINE_STATUS, LobbyOnlineStatusDecoder())
        registerDecoder(GameOpcodes.MOVE_CAMERA, MovedCameraDecoder())
        registerDecoder(GameOpcodes.MOVE_MOUSE, MovedMouseDecoder())
        registerDecoder(GameOpcodes.NPC_OPTION_1, NPCOption1Decoder())
        registerDecoder(GameOpcodes.NPC_OPTION_2, NPCOption2Decoder())
        registerDecoder(GameOpcodes.NPC_OPTION_3, NPCOption3Decoder())
        registerDecoder(GameOpcodes.NPC_OPTION_4, NPCOption4Decoder())
        registerDecoder(GameOpcodes.NPC_OPTION_5, NPCOption5Decoder())
        registerDecoder(GameOpcodes.OBJECT_OPTION_1, ObjectOption1Decoder())
        registerDecoder(GameOpcodes.OBJECT_OPTION_2, ObjectOption2Decoder())
        registerDecoder(GameOpcodes.OBJECT_OPTION_3, ObjectOption3Decoder())
        registerDecoder(GameOpcodes.OBJECT_OPTION_4, ObjectOption4Decoder())
        registerDecoder(GameOpcodes.OBJECT_OPTION_5, ObjectOption5Decoder())
        registerDecoder(GameOpcodes.PING, PingDecoder())
//        registerDecoder(GameOpcodes.PING_REPLY, PingReplyDecoder())
//        registerDecoder(GameOpcodes.PLAYER_OPTION_1, PlayerOptionDecoder(0))
//        registerDecoder(GameOpcodes.PLAYER_OPTION_2, PlayerOptionDecoder(1))
//        registerDecoder(GameOpcodes.PLAYER_OPTION_3, PlayerOptionDecoder(2))
//        registerDecoder(GameOpcodes.PLAYER_OPTION_4, PlayerOptionDecoder(3))
//        registerDecoder(GameOpcodes.PLAYER_OPTION_5, PlayerOptionDecoder(4))
//        registerDecoder(GameOpcodes.PLAYER_OPTION_6, PlayerOptionDecoder(5))
//        registerDecoder(GameOpcodes.PLAYER_OPTION_7, PlayerOptionDecoder(6))
//        registerDecoder(GameOpcodes.PLAYER_OPTION_8, PlayerOptionDecoder(7))
//        registerDecoder(GameOpcodes.PLAYER_OPTION_9, PlayerOptionDecoder(8))
//        registerDecoder(GameOpcodes.PLAYER_OPTION_10, PlayerOptionDecoder(9))
//        registerDecoder(GameOpcodes.PRIVATE_MESSAGE, PrivateDecoder())
//        registerDecoder(GameOpcodes.QUICK_PRIVATE_MESSAGE, PrivateQuickChatDecoder())
//        registerDecoder(GameOpcodes.PUBLIC_MESSAGE, PublicDecoder())
//        registerDecoder(GameOpcodes.QUICK_PUBLIC_MESSAGE, PublicQuickChatDecoder())
//        registerDecoder(GameOpcodes.RECEIVE_COUNT, ReceiveCountDecoder())
//        registerDecoder(GameOpcodes.REFLECTION_RESPONSE, ReflectionResponseDecoder())
        registerDecoder(GameOpcodes.DONE_LOADING_REGION, RegionLoadedDecoder())
        registerDecoder(GameOpcodes.REGION_LOADING, RegionLoadingDecoder())
//        registerDecoder(GameOpcodes.REPORT_ABUSE, ReportAbuseDecoder())
//        registerDecoder(GameOpcodes.RESUME_PLAYER_OBJ_DIALOGUE, ResumeObjDialogueDecoder())
//        registerDecoder(GameOpcodes.SCREEN_CHANGE, ScreenChangeDecoder())
//        registerDecoder(GameOpcodes.OTHER_TELEPORT, SecondaryTeleportDecoder())
//        registerDecoder(GameOpcodes.COLOUR_ID, SkillCapeColourDecoder())
//        registerDecoder(GameOpcodes.STRING_ENTRY, StringEntryDecoder())
//        registerDecoder(GameOpcodes.TOOLKIT_PREFERENCES, ToolkitPreferencesDecoder())
//        registerDecoder(GameOpcodes.UNKNOWN, UnknownDecoder())
//        registerDecoder(GameOpcodes.SCRIPT_4701, UnknownScriptDecoder())
        registerDecoder(GameOpcodes.WALK, WalkMapDecoder())
        registerDecoder(GameOpcodes.MINI_MAP_WALK, WalkMiniMapDecoder())
        registerDecoder(GameOpcodes.CLICK, WindowClickDecoder())
        registerDecoder(GameOpcodes.TOGGLE_FOCUS, WindowFocusDecoder())
//        registerDecoder(GameOpcodes.IN_OUT_SCREEN, WindowHoveredDecoder())
//        registerDecoder(GameOpcodes.REFRESH_WORLDS, WorldListRefreshDecoder())
//        registerDecoder(GameOpcodes.WORLD_MAP_CLICK, WorldMapCloseDecoder())
        count = decoders.size
    }
}