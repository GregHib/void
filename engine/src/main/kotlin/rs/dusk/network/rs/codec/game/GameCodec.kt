package rs.dusk.network.rs.codec.game

import org.koin.dsl.module
import rs.dusk.core.network.codec.Codec
import rs.dusk.network.rs.codec.game.decode.*
import rs.dusk.network.rs.codec.game.encode.*

val gameCodec = module {
    single { ChatMessageEncoder() }
    single { ChunkClearMessageEncoder() }
    single { ChunkUpdateMessageEncoder() }
    single { ContainerItemsMessageEncoder() }
    single { ContextMenuOptionMessageEncoder() }
    single { DynamicMapRegionMessageEncoder() }
    single { FloorItemAddMessageEncoder() }
    single { FloorItemRemoveMessageEncoder() }
    single { FloorItemRevealMessageEncoder() }
    single { FloorItemUpdateMessageEncoder() }
    single { GraphicAreaMessageEncoder() }
    single { InterfaceCloseMessageEncoder() }
    single { InterfaceHeadNPCMessageEncoder() }
    single { InterfaceHeadPlayerMessageEncoder() }
    single { InterfaceItemMessageEncoder() }
    single { InterfaceItemUpdateMessageEncoder() }
    single { InterfaceAnimationMessageEncoder() }
    single { InterfaceOpenMessageEncoder() }
    single { InterfaceSettingsMessageEncoder() }
    single { InterfaceSpriteMessageEncoder() }
    single { InterfaceTextMessageEncoder() }
    single { InterfaceUpdateMessageEncoder() }
    single { InterfaceVisibilityMessageEncoder() }
    single { LogoutLobbyMessageEncoder() }
    single { LogoutMessageEncoder() }
    single { MapRegionMessageEncoder() }
    single { NPCUpdateMessageEncoder() }
    single { ObjectAddMessageEncoder() }
    single { ObjectAnimationMessageEncoder() }
    single { ObjectAnimationSpecificMessageEncoder() }
    single { ObjectCustomiseMessageEncoder() }
    single { ObjectPreloadMessageEncoder() }
    single { ObjectRemoveMessageEncoder() }
    single { PlayerUpdateMessageEncoder() }
    single { ProjectileAddMessageEncoder() }
    single { ProjectileHalfSquareMessageEncoder() }
    single { RunEnergyMessageEncoder() }
    single { ScriptMessageEncoder() }
    single { SkillLevelMessageEncoder() }
    single { SoundAreaMessageEncoder() }
    single { TextTileMessageEncoder() }
    single { VarbitLargeMessageEncoder() }
    single { VarbitMessageEncoder() }
    single { VarcLargeMessageEncoder() }
    single { VarcMessageEncoder() }
    single { VarcStrMessageEncoder() }
    single { VarpLargeMessageEncoder() }
    single { VarpMessageEncoder() }
    single { WeightMessageEncoder() }
}

class GameCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(GameOpcodes.AP_COORD_T, APCoordinateMessageDecoder())
        registerDecoder(GameOpcodes.CHAT_TYPE, ChatTypeMessageDecoder())
        registerDecoder(GameOpcodes.CLAN_CHAT_KICK, ClanChatKickMessageDecoder())
        registerDecoder(GameOpcodes.CLAN_FORUM_THREAD, ClanForumThreadMessageDecoder())
        registerDecoder(GameOpcodes.CLAN_NAME, ClanNameMessageDecoder())
        registerDecoder(GameOpcodes.CLAN_SETTINGS_UPDATE, ClanSettingsUpdateMessageDecoder())
        registerDecoder(GameOpcodes.CONSOLE_COMMAND, ConsoleCommandMessageDecoder())
        registerDecoder(GameOpcodes.CUTSCENE_ACTION, CutsceneActionMessageDecoder())
        registerDecoder(GameOpcodes.DIALOGUE_CONTINUE, DialogueContinueMessageDecoder())
        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_1, FloorItemOptionMessageDecoder(0))
        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_2, FloorItemOptionMessageDecoder(1))
        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_3, FloorItemOptionMessageDecoder(2))
        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_4, FloorItemOptionMessageDecoder(3))
        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_5, FloorItemOptionMessageDecoder(4))
        registerDecoder(GameOpcodes.FLOOR_ITEM_OPTION_6, FloorItemOptionMessageDecoder(5))
        registerDecoder(GameOpcodes.JOIN_FRIEND_CHAT, FriendChatJoinMessageDecoder())
        registerDecoder(GameOpcodes.KICK_FRIEND_CHAT, FriendChatKickMessageDecoder())
        registerDecoder(GameOpcodes.RANK_FRIEND_CHAT, FriendChatRankMessageDecoder())
        registerDecoder(GameOpcodes.ADD_FRIEND, FriendListAddMessageDecoder())
        registerDecoder(GameOpcodes.REMOVE_FRIEND, FriendListRemoveMessageDecoder())
        registerDecoder(GameOpcodes.HYPERLINK_TEXT, HyperlinkMessageDecoder())
        registerDecoder(GameOpcodes.ADD_IGNORE, IgnoreListAddMessageDecoder())
        registerDecoder(GameOpcodes.REMOVE_IGNORE, IgnoreListRemoveMessageDecoder())
        registerDecoder(GameOpcodes.ENTER_INTEGER, IntegerEntryMessageDecoder())
        registerDecoder(GameOpcodes.SCREEN_CLOSE, InterfaceClosedMessageDecoder())
        registerDecoder(GameOpcodes.INTERFACE_ON_FLOOR_ITEM, InterfaceOnFloorItemMessageDecoder())
        registerDecoder(GameOpcodes.ITEM_ON_ITEM, InterfaceOnInterfaceMessageDecoder())
        registerDecoder(GameOpcodes.INTERFACE_ON_NPC, InterfaceOnNpcMessageDecoder())
        registerDecoder(GameOpcodes.ITEM_ON_OBJECT, InterfaceOnObjectMessageDecoder())
        registerDecoder(GameOpcodes.INTERFACE_ON_PLAYER, InterfaceOnPlayerMessageDecoder())
        registerDecoder(GameOpcodes.INTERFACE_OPTION_1, InterfaceOptionMessageDecoder(0))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_2, InterfaceOptionMessageDecoder(1))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_3, InterfaceOptionMessageDecoder(2))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_4, InterfaceOptionMessageDecoder(3))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_5, InterfaceOptionMessageDecoder(4))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_6, InterfaceOptionMessageDecoder(5))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_7, InterfaceOptionMessageDecoder(6))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_8, InterfaceOptionMessageDecoder(7))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_9, InterfaceOptionMessageDecoder(8))
        registerDecoder(GameOpcodes.INTERFACE_OPTION_10, InterfaceOptionMessageDecoder(9))
        registerDecoder(GameOpcodes.SWITCH_INTERFACE_COMPONENTS, InterfaceSwitchComponentsMessageDecoder())
        registerDecoder(GameOpcodes.KEY_TYPED, KeysPressedMessageDecoder())
        registerDecoder(GameOpcodes.PING_LATENCY, LatencyMessageDecoder())
        registerDecoder(GameOpcodes.ONLINE_STATUS, LobbyOnlineStatusMessageDecoder())
        registerDecoder(GameOpcodes.MOVE_CAMERA, MovedCameraMessageDecoder())
        registerDecoder(GameOpcodes.MOVE_MOUSE, MovedMouseMessageDecoder())
        registerDecoder(GameOpcodes.NPC_OPTION_1, NPCOptionMessageDecoder(0))
        registerDecoder(GameOpcodes.NPC_OPTION_2, NPCOptionMessageDecoder(1))
        registerDecoder(GameOpcodes.NPC_OPTION_3, NPCOptionMessageDecoder(2))
        registerDecoder(GameOpcodes.NPC_OPTION_4, NPCOptionMessageDecoder(3))
        registerDecoder(GameOpcodes.NPC_OPTION_5, NPCOptionMessageDecoder(4))
        registerDecoder(GameOpcodes.NPC_OPTION_6, NPCOptionMessageDecoder(5))
        registerDecoder(GameOpcodes.OBJECT_OPTION_1, ObjectOptionMessageDecoder(0))
        registerDecoder(GameOpcodes.OBJECT_OPTION_2, ObjectOptionMessageDecoder(1))
        registerDecoder(GameOpcodes.OBJECT_OPTION_3, ObjectOptionMessageDecoder(2))
        registerDecoder(GameOpcodes.OBJECT_OPTION_4, ObjectOptionMessageDecoder(3))
        registerDecoder(GameOpcodes.OBJECT_OPTION_5, ObjectOptionMessageDecoder(4))
        registerDecoder(GameOpcodes.OBJECT_OPTION_6, ObjectOptionMessageDecoder(5))
        registerDecoder(GameOpcodes.PING, PingMessageDecoder())
        registerDecoder(GameOpcodes.PING_REPLY, PingReplyMessageDecoder())
        registerDecoder(GameOpcodes.PLAYER_OPTION_1, PlayerOptionMessageDecoder(0))
        registerDecoder(GameOpcodes.PLAYER_OPTION_2, PlayerOptionMessageDecoder(1))
        registerDecoder(GameOpcodes.PLAYER_OPTION_3, PlayerOptionMessageDecoder(2))
        registerDecoder(GameOpcodes.PLAYER_OPTION_4, PlayerOptionMessageDecoder(3))
        registerDecoder(GameOpcodes.PLAYER_OPTION_5, PlayerOptionMessageDecoder(4))
        registerDecoder(GameOpcodes.PLAYER_OPTION_6, PlayerOptionMessageDecoder(5))
        registerDecoder(GameOpcodes.PLAYER_OPTION_7, PlayerOptionMessageDecoder(6))
        registerDecoder(GameOpcodes.PLAYER_OPTION_8, PlayerOptionMessageDecoder(7))
        registerDecoder(GameOpcodes.PLAYER_OPTION_9, PlayerOptionMessageDecoder(8))
        registerDecoder(GameOpcodes.PLAYER_OPTION_10, PlayerOptionMessageDecoder(9))
        registerDecoder(GameOpcodes.PRIVATE_MESSAGE, PrivateMessageDecoder())
        registerDecoder(GameOpcodes.QUICK_PRIVATE_MESSAGE, PrivateQuickChatMessageDecoder())
        registerDecoder(GameOpcodes.PUBLIC_MESSAGE, PublicMessageDecoder())
        registerDecoder(GameOpcodes.QUICK_PUBLIC_MESSAGE, PublicQuickChatMessageDecoder())
        registerDecoder(GameOpcodes.RECEIVE_COUNT, ReceiveCountMessageDecoder())
        registerDecoder(GameOpcodes.REFLECTION_RESPONSE, ReflectionResponseMessageDecoder())
        registerDecoder(GameOpcodes.DONE_LOADING_REGION, RegionLoadedMessageDecoder())
        registerDecoder(GameOpcodes.REGION_LOADING, RegionLoadingMessageDecoder())
        registerDecoder(GameOpcodes.REPORT_ABUSE, ReportAbuseMessageDecoder())
        registerDecoder(GameOpcodes.RESUME_PLAYER_OBJ_DIALOGUE, ResumeObjDialogueMessageDecoder())
        registerDecoder(GameOpcodes.SCREEN_CHANGE, ScreenChangeMessageDecoder())
        registerDecoder(GameOpcodes.OTHER_TELEPORT, SecondaryTeleportMessageDecoder())
        registerDecoder(GameOpcodes.COLOUR_ID, SkillCapeColourMessageDecoder())
        registerDecoder(GameOpcodes.STRING_ENTRY, StringEntryMessageDecoder())
        registerDecoder(GameOpcodes.TOOLKIT_PREFERENCES, ToolkitPreferencesMessageDecoder())
        registerDecoder(GameOpcodes.UNKNOWN, UnknownMessageDecoder())
        registerDecoder(GameOpcodes.SCRIPT_4701, UnknownScriptMessageDecoder())
        registerDecoder(GameOpcodes.WALK, WalkMapMessageDecoder())
        registerDecoder(GameOpcodes.MINI_MAP_WALK, WalkMiniMapMessageDecoder())
        registerDecoder(GameOpcodes.CLICK, WindowClickMessageDecoder())
        registerDecoder(GameOpcodes.TOGGLE_FOCUS, WindowFocusMessageDecoder())
        registerDecoder(GameOpcodes.IN_OUT_SCREEN, WindowHoveredMessageDecoder())
        registerDecoder(GameOpcodes.REFRESH_WORLDS, WorldListRefreshMessageDecoder())
        registerDecoder(GameOpcodes.WORLD_MAP_CLICK, WorldMapCloseMessageDecoder())
        count = decoders.size
    }
}