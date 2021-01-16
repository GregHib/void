package rs.dusk.network.codec.game

import org.koin.dsl.module
import rs.dusk.network.codec.Codec
import rs.dusk.network.codec.game.decode.InterfaceClosedDecoder
import rs.dusk.network.codec.game.decode.PingDecoder
import rs.dusk.network.codec.game.decode.ScreenChangeDecoder
import rs.dusk.network.codec.game.encode.*

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
//        registerDecoder(GameOpcodes.CLAN_CHAT_KICK, ClanChatKickDecoder())
//        registerDecoder(GameOpcodes.CLAN_FORUM_THREAD, ClanForumThreadDecoder())
//        registerDecoder(GameOpcodes.CLAN_NAME, ClanNameDecoder())
//        registerDecoder(GameOpcodes.CLAN_SETTINGS_UPDATE, ClanSettingsUpdateDecoder())
//        registerDecoder(GameOpcodes.CONSOLE_COMMAND, ConsoleCommandDecoder())
//        registerDecoder(GameOpcodes.CUTSCENE_ACTION, CutsceneActionDecoder())
//        registerDecoder(GameOpcodes.DIALOGUE_CONTINUE, DialogueContinueDecoder())
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
        registerDecoder(GameOpcodes.SCREEN_CLOSE, InterfaceClosedDecoder())
//        registerDecoder(GameOpcodes.INTERFACE_ON_FLOOR_ITEM, InterfaceOnFloorItemDecoder())
//        registerDecoder(GameOpcodes.ITEM_ON_ITEM, InterfaceOnInterfaceDecoder())
//        registerDecoder(GameOpcodes.INTERFACE_ON_NPC, InterfaceOnNpcDecoder())
//        registerDecoder(GameOpcodes.ITEM_ON_OBJECT, InterfaceOnObjectDecoder())
//        registerDecoder(GameOpcodes.INTERFACE_ON_PLAYER, InterfaceOnPlayerDecoder())
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_1, InterfaceOptionDecoder(0))
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_2, InterfaceOptionDecoder(1))
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_3, InterfaceOptionDecoder(2))
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_4, InterfaceOptionDecoder(3))
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_5, InterfaceOptionDecoder(4))
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_6, InterfaceOptionDecoder(5))
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_7, InterfaceOptionDecoder(6))
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_8, InterfaceOptionDecoder(7))
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_9, InterfaceOptionDecoder(8))
//        registerDecoder(GameOpcodes.INTERFACE_OPTION_10, InterfaceOptionDecoder(9))
//        registerDecoder(GameOpcodes.SWITCH_INTERFACE_COMPONENTS, InterfaceSwitchComponentsDecoder())
//        registerDecoder(GameOpcodes.KEY_TYPED, KeysPressedDecoder())
//        registerDecoder(GameOpcodes.PING_LATENCY, LatencyDecoder())
//        registerDecoder(GameOpcodes.ONLINE_STATUS, LobbyOnlineStatusDecoder())
//        registerDecoder(GameOpcodes.MOVE_CAMERA, MovedCameraDecoder())
//        registerDecoder(GameOpcodes.MOVE_MOUSE, MovedMouseDecoder())
//        registerDecoder(GameOpcodes.NPC_OPTION_1, NPCOptionDecoder(0))
//        registerDecoder(GameOpcodes.NPC_OPTION_2, NPCOptionDecoder(1))
//        registerDecoder(GameOpcodes.NPC_OPTION_3, NPCOptionDecoder(2))
//        registerDecoder(GameOpcodes.NPC_OPTION_4, NPCOptionDecoder(3))
//        registerDecoder(GameOpcodes.NPC_OPTION_5, NPCOptionDecoder(4))
//        registerDecoder(GameOpcodes.NPC_OPTION_6, NPCOptionDecoder(5))
//        registerDecoder(GameOpcodes.OBJECT_OPTION_1, ObjectOptionDecoder(0))
//        registerDecoder(GameOpcodes.OBJECT_OPTION_2, ObjectOptionDecoder(1))
//        registerDecoder(GameOpcodes.OBJECT_OPTION_3, ObjectOptionDecoder(2))
//        registerDecoder(GameOpcodes.OBJECT_OPTION_4, ObjectOptionDecoder(3))
//        registerDecoder(GameOpcodes.OBJECT_OPTION_5, ObjectOptionDecoder(4))
//        registerDecoder(GameOpcodes.OBJECT_OPTION_6, ObjectOptionDecoder(5))
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
//        registerDecoder(GameOpcodes.DONE_LOADING_REGION, RegionLoadedDecoder())
//        registerDecoder(GameOpcodes.REGION_LOADING, RegionLoadingDecoder())
//        registerDecoder(GameOpcodes.REPORT_ABUSE, ReportAbuseDecoder())
//        registerDecoder(GameOpcodes.RESUME_PLAYER_OBJ_DIALOGUE, ResumeObjDialogueDecoder())
        registerDecoder(GameOpcodes.SCREEN_CHANGE, ScreenChangeDecoder())
//        registerDecoder(GameOpcodes.OTHER_TELEPORT, SecondaryTeleportDecoder())
//        registerDecoder(GameOpcodes.COLOUR_ID, SkillCapeColourDecoder())
//        registerDecoder(GameOpcodes.STRING_ENTRY, StringEntryDecoder())
//        registerDecoder(GameOpcodes.TOOLKIT_PREFERENCES, ToolkitPreferencesDecoder())
//        registerDecoder(GameOpcodes.UNKNOWN, UnknownDecoder())
//        registerDecoder(GameOpcodes.SCRIPT_4701, UnknownScriptDecoder())
//        registerDecoder(GameOpcodes.WALK, WalkMapDecoder())
//        registerDecoder(GameOpcodes.MINI_MAP_WALK, WalkMiniMapDecoder())
//        registerDecoder(GameOpcodes.CLICK, WindowClickDecoder())
//        registerDecoder(GameOpcodes.TOGGLE_FOCUS, WindowFocusDecoder())
//        registerDecoder(GameOpcodes.IN_OUT_SCREEN, WindowHoveredDecoder())
//        registerDecoder(GameOpcodes.REFRESH_WORLDS, WorldListRefreshDecoder())
//        registerDecoder(GameOpcodes.WORLD_MAP_CLICK, WorldMapCloseDecoder())
        count = decoders.size
    }
}