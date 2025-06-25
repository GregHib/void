package world.gregs.voidps.network.login.protocol

import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.login.protocol.decode.*

fun decoders(huffman: Huffman): Array<Decoder?> {
    val array = arrayOfNulls<Decoder>(256)
    array[45] = FloorItemOption1Decoder()
    array[55] = FloorItemOption2Decoder()
    array[24] = FloorItemOption3Decoder()
    array[86] = FloorItemOption4Decoder()
    array[35] = FloorItemOption5Decoder()
    array[70] = ConsoleCommandDecoder()
    array[54] = DialogueContinueDecoder()
    array[3] = IntegerEntryDecoder()
    array[56] = InterfaceClosedDecoder()
    array[73] = InterfaceOnInterfaceDecoder()
    array[65] = InterfaceOnNpcDecoder()
    array[42] = InterfaceOnObjectDecoder()
    array[40] = InterfaceOnPlayerDecoder()
    array[61] = InterfaceOptionDecoder(0)
    array[64] = InterfaceOptionDecoder(1)
    array[4] = InterfaceOptionDecoder(2)
    array[52] = InterfaceOptionDecoder(3)
    array[81] = InterfaceOptionDecoder(4)
    array[91] = InterfaceOptionDecoder(5)
    array[18] = InterfaceOptionDecoder(6)
    array[10] = InterfaceOptionDecoder(7)
    array[20] = InterfaceOptionDecoder(8)
    array[25] = InterfaceOptionDecoder(9)
    array[26] = InterfaceSwitchComponentsDecoder()
    array[5] = MovedCameraDecoder()
    array[68] = KeysPressedDecoder()
    array[29] = MovedMouseDecoder()
    array[9] = NPCOption1Decoder()
    array[66] = NPCOption2Decoder()
    array[31] = NPCOption3Decoder()
    array[67] = NPCOption4Decoder()
    array[28] = NPCOption5Decoder()
    array[92] = NPCExamineDecoder()
    array[11] = ObjectOption1Decoder()
    array[2] = ObjectOption2Decoder()
    array[76] = ObjectOption3Decoder()
    array[0] = ObjectOption4Decoder()
    array[69] = ObjectOption5Decoder()
    array[47] = ObjectExamineDecoder()
    array[16] = PingDecoder()
    array[85] = LatencyDecoder()
    array[14] = PlayerOption1Decoder()
    array[53] = PlayerOption2Decoder()
    array[77] = PlayerOption3Decoder()
    array[46] = PlayerOption4Decoder()
    array[50] = PlayerOption5Decoder()
    array[82] = PlayerOption6Decoder()
    array[49] = PlayerOption7Decoder()
    array[62] = PlayerOption8Decoder()
    array[43] = PlayerOption9Decoder()
    array[19] = PlayerOption10Decoder()
    array[33] = RegionLoadedDecoder()
    // array[71] = RegionLoadingDecoder()
    array[15] = RegionLoadingDecoder()
    array[87] = ScreenChangeDecoder()
//    array[43] = NameEntryDecoder()
    array[12] = WalkMapDecoder()
    array[83] = WalkMiniMapDecoder()
    array[84] = WindowClickDecoder()
    array[93] = WindowFocusDecoder()
    array[21] = InterfaceOnFloorItemDecoder()
    array[27] = FloorItemExamineDecoder()
    array[37] = ClientDetailOptionsStatusDecoder()
    array[75] = SongEndDecoder()
    array[57] = ChatTypeDecoder()
    array[89] = WorldMapClickDecoder()
    array[51] = AddFriendDecoder()
    array[17] = AddIgnoreDecoder()
    array[38] = DeleteIgnoreDecoder()
    array[8] = DeleteFriendDecoder()
    array[30] = PublicQuickChatDecoder()
    array[32] = ClanChatKickDecoder()
    array[79] = PrivateQuickChatDecoder()
    array[1] = ClanChatJoinDecoder()
    array[36] = PublicDecoder(huffman)
    array[23] = ChatSetModeDecoder()
    // array[41] = ClanChatRankDecoder()

    // array[72] = PrivateDecoder(huffman)
    //




//    array[66] = ReportAbuseDecoder()
//    array[13] = AntiCheatDecoder()
//    array[67] = HyperlinkDecoder()
//    array[75] = LobbyOnlineStatusDecoder()
//    array[30] = LobbyWorldListRefreshDecoder()
//    array[74] = ClanChatRankDecoder()
//    array[77] = ReflectionResponseDecoder()
//    array[76] = SecondaryTeleportDecoder()
//    array[1] = StringEntryDecoder()
//    array[40] = APCoordinateDecoder()
//    array[28] = ResumeObjDialogueDecoder()
  //    array[84] = ToolkitPreferencesDecoder()
 //       array[71] = UnknownDecoder()


    // public static final ClientProt SEND_PING_REPLY = new ClientProt(6, 8);
    // public static final ClientProt RESUME_P_NAMEDIALOG = new ClientProt(7, -1);
    // public static final ClientProt RESUME_P_OBJDIALOG = new ClientProt(13, 2);
    // public static final ClientProt TRANSMITVAR_VERIFYID = new ClientProt(15, 4);
    // public static final ClientProt RESUME_P_HSLDIALOG = new ClientProt(22, 2);
    // public static final ClientProt CLIENT_DETAILOPTIONS_STATUS = new ClientProt(37, -1);
    // public static final ClientProt APCOORDT = new ClientProt(39, 12);
    // public static final ClientProt RESUME_P_STRINGDIALOG = new ClientProt(59, -1);
    // public static final ClientProt FACE_SQUARE = new ClientProt(63, 4);
    // public static final ClientProt REFLECTION_CHECK_REPLY = new ClientProt(78, -1);
    // public static final ClientProt SEND_SNAPSHOT = new ClientProt(80, -1);

    return array
}