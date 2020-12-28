package rs.dusk.network.rs.codec.game

import rs.dusk.core.network.codec.Codec
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.decode.*
import rs.dusk.network.rs.codec.game.encode.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object GameCodec : Codec() {

    override fun register() {
        registerDecoders()
        registerEncoders()
    }

    private fun registerDecoders() {
        decoders[GameOpcodes.AP_COORD_T] = APCoordinateMessageDecoder()
        decoders[GameOpcodes.CHAT_TYPE] = ChatTypeMessageDecoder()
        decoders[GameOpcodes.CLAN_CHAT_KICK] = ClanChatKickMessageDecoder()
        decoders[GameOpcodes.CLAN_FORUM_THREAD] = ClanForumThreadMessageDecoder()
        decoders[GameOpcodes.CLAN_NAME] = ClanNameMessageDecoder()
        decoders[GameOpcodes.CLAN_SETTINGS_UPDATE] = ClanSettingsUpdateMessageDecoder()
        decoders[GameOpcodes.CONSOLE_COMMAND] = ConsoleCommandMessageDecoder()
        decoders[GameOpcodes.CUTSCENE_ACTION] = CutsceneActionMessageDecoder()
        decoders[GameOpcodes.DIALOGUE_CONTINUE] = DialogueContinueMessageDecoder()
        decoders[GameOpcodes.FLOOR_ITEM_OPTION_1] = FloorItemOptionMessageDecoder(0)
        decoders[GameOpcodes.FLOOR_ITEM_OPTION_2] = FloorItemOptionMessageDecoder(1)
        decoders[GameOpcodes.FLOOR_ITEM_OPTION_3] = FloorItemOptionMessageDecoder(2)
        decoders[GameOpcodes.FLOOR_ITEM_OPTION_4] = FloorItemOptionMessageDecoder(3)
        decoders[GameOpcodes.FLOOR_ITEM_OPTION_5] = FloorItemOptionMessageDecoder(4)
        decoders[GameOpcodes.FLOOR_ITEM_OPTION_6] = FloorItemOptionMessageDecoder(5)
        decoders[GameOpcodes.JOIN_FRIEND_CHAT] = FriendChatJoinMessageDecoder()
        decoders[GameOpcodes.KICK_FRIEND_CHAT] = FriendChatKickMessageDecoder()
        decoders[GameOpcodes.RANK_FRIEND_CHAT] = FriendChatRankMessageDecoder()
        decoders[GameOpcodes.ADD_FRIEND] = FriendListAddMessageDecoder()
        decoders[GameOpcodes.REMOVE_FRIEND] = FriendListRemoveMessageDecoder()
        decoders[GameOpcodes.HYPERLINK_TEXT] = HyperlinkMessageDecoder()
        decoders[GameOpcodes.ADD_IGNORE] = IgnoreListAddMessageDecoder()
        decoders[GameOpcodes.REMOVE_IGNORE] = IgnoreListRemoveMessageDecoder()
        decoders[GameOpcodes.ENTER_INTEGER] = IntegerEntryMessageDecoder()
        decoders[GameOpcodes.SCREEN_CLOSE] = InterfaceClosedMessageDecoder()
        decoders[GameOpcodes.INTERFACE_ON_FLOOR_ITEM] = InterfaceOnFloorItemMessageDecoder()
        decoders[GameOpcodes.ITEM_ON_ITEM] = InterfaceOnInterfaceMessageDecoder()
        decoders[GameOpcodes.INTERFACE_ON_NPC] = InterfaceOnNpcMessageDecoder()
        decoders[GameOpcodes.ITEM_ON_OBJECT] = InterfaceOnObjectMessageDecoder()
        decoders[GameOpcodes.INTERFACE_ON_PLAYER] = InterfaceOnPlayerMessageDecoder()
        decoders[GameOpcodes.INTERFACE_OPTION_1] = InterfaceOptionMessageDecoder(0)
        decoders[GameOpcodes.INTERFACE_OPTION_2] = InterfaceOptionMessageDecoder(1)
        decoders[GameOpcodes.INTERFACE_OPTION_3] = InterfaceOptionMessageDecoder(2)
        decoders[GameOpcodes.INTERFACE_OPTION_4] = InterfaceOptionMessageDecoder(3)
        decoders[GameOpcodes.INTERFACE_OPTION_5] = InterfaceOptionMessageDecoder(4)
        decoders[GameOpcodes.INTERFACE_OPTION_6] = InterfaceOptionMessageDecoder(5)
        decoders[GameOpcodes.INTERFACE_OPTION_7] = InterfaceOptionMessageDecoder(6)
        decoders[GameOpcodes.INTERFACE_OPTION_8] = InterfaceOptionMessageDecoder(7)
        decoders[GameOpcodes.INTERFACE_OPTION_9] = InterfaceOptionMessageDecoder(8)
        decoders[GameOpcodes.INTERFACE_OPTION_10] = InterfaceOptionMessageDecoder(9)
        decoders[GameOpcodes.SWITCH_INTERFACE_COMPONENTS] = InterfaceSwitchComponentsMessageDecoder()
        decoders[GameOpcodes.KEY_TYPED] = KeysPressedMessageDecoder()
        decoders[GameOpcodes.PING_LATENCY] = LatencyMessageDecoder()
        decoders[GameOpcodes.ONLINE_STATUS] = LobbyOnlineStatusMessageDecoder()
        decoders[GameOpcodes.MOVE_CAMERA] = MovedCameraMessageDecoder()
        decoders[GameOpcodes.MOVE_MOUSE] = MovedMouseMessageDecoder()
        decoders[GameOpcodes.NPC_OPTION_1] = NPCOptionMessageDecoder(0)
        decoders[GameOpcodes.NPC_OPTION_2] = NPCOptionMessageDecoder(1)
        decoders[GameOpcodes.NPC_OPTION_3] = NPCOptionMessageDecoder(2)
        decoders[GameOpcodes.NPC_OPTION_4] = NPCOptionMessageDecoder(3)
        decoders[GameOpcodes.NPC_OPTION_5] = NPCOptionMessageDecoder(4)
        decoders[GameOpcodes.NPC_OPTION_6] = NPCOptionMessageDecoder(5)
        decoders[GameOpcodes.OBJECT_OPTION_1] = ObjectOptionMessageDecoder(0)
        decoders[GameOpcodes.OBJECT_OPTION_2] = ObjectOptionMessageDecoder(1)
        decoders[GameOpcodes.OBJECT_OPTION_3] = ObjectOptionMessageDecoder(2)
        decoders[GameOpcodes.OBJECT_OPTION_4] = ObjectOptionMessageDecoder(3)
        decoders[GameOpcodes.OBJECT_OPTION_5] = ObjectOptionMessageDecoder(4)
        decoders[GameOpcodes.OBJECT_OPTION_6] = ObjectOptionMessageDecoder(5)
        decoders[GameOpcodes.PING] = PingMessageDecoder()
        decoders[GameOpcodes.PING_REPLY] = PingReplyMessageDecoder()
        decoders[GameOpcodes.PLAYER_OPTION_1] = PlayerOptionMessageDecoder(0)
        decoders[GameOpcodes.PLAYER_OPTION_2] = PlayerOptionMessageDecoder(1)
        decoders[GameOpcodes.PLAYER_OPTION_3] = PlayerOptionMessageDecoder(2)
        decoders[GameOpcodes.PLAYER_OPTION_4] = PlayerOptionMessageDecoder(3)
        decoders[GameOpcodes.PLAYER_OPTION_5] = PlayerOptionMessageDecoder(4)
        decoders[GameOpcodes.PLAYER_OPTION_6] = PlayerOptionMessageDecoder(5)
        decoders[GameOpcodes.PLAYER_OPTION_7] = PlayerOptionMessageDecoder(6)
        decoders[GameOpcodes.PLAYER_OPTION_8] = PlayerOptionMessageDecoder(7)
        decoders[GameOpcodes.PLAYER_OPTION_9] = PlayerOptionMessageDecoder(8)
        decoders[GameOpcodes.PLAYER_OPTION_10] = PlayerOptionMessageDecoder(9)
        decoders[GameOpcodes.PRIVATE_MESSAGE] = PrivateMessageDecoder()
        decoders[GameOpcodes.QUICK_PRIVATE_MESSAGE] = PrivateQuickChatMessageDecoder()
        decoders[GameOpcodes.PUBLIC_MESSAGE] = PublicMessageDecoder()
        decoders[GameOpcodes.QUICK_PUBLIC_MESSAGE] = PublicQuickChatMessageDecoder()
        decoders[GameOpcodes.RECEIVE_COUNT] = ReceiveCountMessageDecoder()
        decoders[GameOpcodes.REFLECTION_RESPONSE] = ReflectionResponseMessageDecoder()
        decoders[GameOpcodes.DONE_LOADING_REGION] = RegionLoadedMessageDecoder()
        decoders[GameOpcodes.REGION_LOADING] = RegionLoadingMessageDecoder()
        decoders[GameOpcodes.REPORT_ABUSE] = ReportAbuseMessageDecoder()
        decoders[GameOpcodes.RESUME_PLAYER_OBJ_DIALOGUE] = ResumeObjDialogueMessageDecoder()
        decoders[GameOpcodes.SCREEN_CHANGE] = ScreenChangeMessageDecoder()
        decoders[GameOpcodes.OTHER_TELEPORT] = SecondaryTeleportMessageDecoder()
        decoders[GameOpcodes.COLOUR_ID] = SkillcapeColourMessageDecoder()
        decoders[GameOpcodes.STRING_ENTRY] = StringEntryMessageDecoder()
        decoders[GameOpcodes.TOOLKIT_PREFERENCES] = ToolkitPreferencesMessageDecoder()
        decoders[GameOpcodes.UNKNOWN] = UnknownMessageDecoder()
        decoders[GameOpcodes.SCRIPT_4701] = UnknownScriptMessageDecoder()
        decoders[GameOpcodes.WALK] = WalkMapMessageDecoder()
        decoders[GameOpcodes.MINI_MAP_WALK] = WalkMiniMapMessageDecoder()
        decoders[GameOpcodes.CLICK] = WindowClickMessageDecoder()
        decoders[GameOpcodes.TOGGLE_FOCUS] = WindowFocusMessageDecoder()
        decoders[GameOpcodes.IN_OUT_SCREEN] = WindowHoveredMessageDecoder()
        decoders[GameOpcodes.REFRESH_WORLDS] = WorldListRefreshMessageDecoder()
        decoders[GameOpcodes.WORLD_MAP_CLICK] = WorldMapOpenMessageDecoder()
    }

    fun registerEncoders() {
        registerEncoder(ChatMessageEncoder())
        registerEncoder(ChunkClearMessageEncoder())
        registerEncoder(ChunkUpdateMessageEncoder())
        registerEncoder(ContainerItemsMessageEncoder())
        registerEncoder(ContextMenuOptionMessageEncoder())
        registerEncoder(DynamicMapRegionMessageEncoder())
        registerEncoder(FloorItemAddMessageEncoder())
        registerEncoder(FloorItemRemoveMessageEncoder())
        registerEncoder(FloorItemRevealMessageEncoder())
        registerEncoder(FloorItemUpdateMessageEncoder())
        registerEncoder(GraphicAreaMessageEncoder())
        registerEncoder(InterfaceCloseMessageEncoder())
        registerEncoder(InterfaceHeadNPCMessageEncoder())
        registerEncoder(InterfaceHeadPlayerMessageEncoder())
        registerEncoder(InterfaceItemMessageEncoder())
        registerEncoder(InterfaceItemUpdateMessageEncoder())
        registerEncoder(InterfaceMessageEncoder())
        registerEncoder(InterfaceOpenMessageEncoder())
        registerEncoder(InterfaceSettingsMessageEncoder())
        registerEncoder(InterfaceSpriteMessageEncoder())
        registerEncoder(InterfaceTextMessageEncoder())
        registerEncoder(InterfaceUpdateMessageEncoder())
        registerEncoder(InterfaceVisibilityMessageEncoder())
        registerEncoder(LogoutMessageEncoder())
        registerEncoder(MapRegionMessageEncoder())
        registerEncoder(NPCUpdateMessageEncoder())
        registerEncoder(ObjectAddMessageEncoder())
        registerEncoder(ObjectAnimationMessageEncoder())
        registerEncoder(ObjectAnimationSpecificMessageEncoder())
        registerEncoder(ObjectCustomiseMessageEncoder())
        registerEncoder(ObjectPreloadMessageEncoder())
        registerEncoder(ObjectRemoveMessageEncoder())
        registerEncoder(PlayerUpdateMessageEncoder())
        registerEncoder(ProjectileAddMessageEncoder())
        registerEncoder(ProjectileHalfSquareMessageEncoder())
        registerEncoder(RunEnergyMessageEncoder())
        registerEncoder(ScriptMessageEncoder())
        registerEncoder(SkillLevelMessageEncoder())
        registerEncoder(SoundAreaMessageEncoder())
        registerEncoder(TextTileMessageEncoder())
        registerEncoder(VarbitMessageEncoder())
        registerEncoder(VarcMessageEncoder())
        registerEncoder(VarcStrMessageEncoder())
        registerEncoder(VarpMessageEncoder())
        registerEncoder(WeightMessageEncoder())
        registerEncoder(WorldListResponseMessageEncoder())
    }
}

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class GameMessageEncoder<M : Message> : MessageEncoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class GameMessageDecoder<M : Message>(override var length: Int) : MessageDecoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class GameMessageHandler<M : Message> : MessageHandler<M>()