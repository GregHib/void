package world.gregs.voidps.network.codec.game

object GameOpcodes {

    /* Decode */

    const val OBJECT_OPTION_4 = 0 // TODO
    const val JOIN_FRIEND_CHAT = 1 // TODO
    const val OBJECT_OPTION_2 = 2 // TODO
    const val ENTER_INTEGER = 3 // TODO
    const val INTERFACE_OPTION_3 = 4 // TODO
    const val MOVE_CAMERA = 5 // TODO
    const val PING_REPLY = 6 // TODO
    const val CLAN_NAME = 7 // TODO
    const val REMOVE_FRIEND = 8 // TODO
    const val NPC_OPTION_1 = 9 // TODO
    const val INTERFACE_OPTION_8 = 10 // TODO
    const val OBJECT_OPTION_1 = 11 // TODO
    const val WALK = 12 // TODO
    const val RESUME_PLAYER_OBJ_DIALOGUE = 13 // TODO
    const val PLAYER_OPTION_1 = 14 // TODO
    const val RECEIVE_COUNT = 15 // TODO
    const val PING = 0
    const val ADD_IGNORE = 17 // TODO
    const val INTERFACE_OPTION_7 = 18 // TODO
    const val PLAYER_OPTION_10 = 19 // TODO
    const val INTERFACE_OPTION_9 = 20 // TODO
    const val INTERFACE_ON_FLOOR_ITEM = 21 // TODO
    const val COLOUR_ID = 22 // TODO
    const val CHAT_TYPE = 23 // TODO
    const val FLOOR_ITEM_OPTION_3 = 24 // TODO
    const val INTERFACE_OPTION_10 = 25 // TODO
    const val SWITCH_INTERFACE_COMPONENTS = 26 // TODO
    const val FLOOR_ITEM_OPTION_6 = 27 // TODO
    const val NPC_OPTION_5 = 28 // TODO
    const val MOVE_MOUSE = 29 // TODO
    const val QUICK_PUBLIC_MESSAGE = 30 // TODO
    const val NPC_OPTION_3 = 31 // TODO
    const val KICK_FRIEND_CHAT = 32 // TODO
    const val DONE_LOADING_REGION = 33 // TODO
    const val REFRESH_WORLDS = 34 // TODO
    const val FLOOR_ITEM_OPTION_5 = 35 // TODO
    const val PUBLIC_MESSAGE = 36 // TODO
    const val TOOLKIT_PREFERENCES = 37 // TODO
    const val REMOVE_IGNORE = 38 // TODO
    const val AP_COORD_T = 39 // TODO
    const val INTERFACE_ON_PLAYER = 40 // TODO
    const val RANK_FRIEND_CHAT = 41 // TODO
    const val ITEM_ON_OBJECT = 42 // TODO
    const val PLAYER_OPTION_9 = 43 // TODO
    const val CLAN_SETTINGS_UPDATE = 44 // TODO
    const val FLOOR_ITEM_OPTION_1 = 45 // TODO
    const val PLAYER_OPTION_4 = 46 // TODO
    const val OBJECT_OPTION_6 = 47 // TODO
    const val CUTSCENE_ACTION = 48 // TODO
    const val PLAYER_OPTION_7 = 49 // TODO
    const val PLAYER_OPTION_5 = 50 // TODO
    const val ADD_FRIEND = 51 // TODO
    const val INTERFACE_OPTION_4 = 52 // TODO
    const val PLAYER_OPTION_2 = 53 // TODO
    const val DIALOGUE_CONTINUE = 54 // TODO
    const val FLOOR_ITEM_OPTION_2 = 55 // TODO
    const val SCREEN_CLOSE = 70
    const val ONLINE_STATUS = 57 // TODO
    const val UNKNOWN = 58 // TODO
    const val STRING_ENTRY = 59 // TODO
    const val CLAN_CHAT_KICK = 60 // TODO
    const val INTERFACE_OPTION_1 = 61 // TODO
    const val PLAYER_OPTION_8 = 62 // TODO
    const val OTHER_TELEPORT = 63 // TODO
    const val INTERFACE_OPTION_2 = 64 // TODO
    const val INTERFACE_ON_NPC = 65 // TODO
    const val NPC_OPTION_2 = 66 // TODO
    const val NPC_OPTION_4 = 67 // TODO
    const val KEY_TYPED = 68 // TODO
    const val OBJECT_OPTION_5 = 69 // TODO
    const val CONSOLE_COMMAND = 70 // TODO
    const val REGION_LOADING = 71 // TODO
    const val PRIVATE_MESSAGE = 72 // TODO
    const val ITEM_ON_ITEM = 73 // TODO
    const val CLAN_FORUM_THREAD = 74 // TODO
    const val IN_OUT_SCREEN = 75 // TODO
    const val OBJECT_OPTION_3 = 76 // TODO
    const val PLAYER_OPTION_3 = 77 // TODO
    const val REFLECTION_RESPONSE = 78 // TODO
    const val QUICK_PRIVATE_MESSAGE = 79 // TODO
    const val REPORT_ABUSE = 80 // TODO
    const val INTERFACE_OPTION_5 = 81 // TODO
    const val PLAYER_OPTION_6 = 82 // TODO
    const val MINI_MAP_WALK = 83 // TODO
    const val CLICK = 84 // TODO
    const val PING_LATENCY = 85 // TODO
    const val FLOOR_ITEM_OPTION_4 = 86 // TODO
    const val SCREEN_CHANGE = 7
    const val HYPERLINK_TEXT = 88 // TODO
    const val WORLD_MAP_CLICK = 89 // TODO
    const val SCRIPT_4701 = 90 // TODO
    const val INTERFACE_OPTION_6 = 91 // TODO
    const val NPC_OPTION_6 = 92 // TODO
    const val TOGGLE_FOCUS = 93 // TODO

    /* Encode */

    const val PLAYER_OPTION = 1 // TODO
    const val INTERFACE_SPRITE = 2 // TODO
    const val LOGIN_DETAILS = 2 // TODO
    const val LOBBY_DETAILS = 2 // TODO
    const val INTERFACE_COMPONENT_SETTINGS = 34
    const val INTERFACE_REFRESH = 4 // TODO
    const val INTERFACE_OPEN = 37
    const val NPC_UPDATING = 6 // TODO
    const val INTERFACE_SCROLL_VERTICAL = 8 // TODO
    const val INTERFACE_ITEM = 9 // TODO
    const val OBJECT_PRE_FETCH = 11 // TODO
    const val FRIENDS_CHAT_UPDATE = 12 // TODO
    const val RUN_ENERGY = 13 // TODO
    const val CLIENT_VARBIT = 73
    const val FLOOR_ITEM_REMOVE = 16 // TODO
    const val INTERFACE_PLAYER_BODY = 18 // TODO
    const val FRIENDS_QUICK_CHAT_MESSAGE = 20 // TODO
    const val CHUNK_CLEAR = 26 // TODO
    const val INTERFACE_ANIMATION = 23 // TODO
    const val FRIEND_LIST_APPEND = 24 // TODO
    const val CLIENT_PING = 25 // TODO
    const val OBJECT_ADD = 28 // TODO
    const val TILE_TEXT = 32 // TODO
    const val INTERFACE_TEXT = 33 // TODO
    const val INTERFACE_ITEMS = 37 // TODO
    const val CLIENT_VARP_LARGE = 44
    const val FRIENDS_CHAT_MESSAGE = 40 // TODO
    const val GRAPHIC_AREA = 41 // TODO
    const val PRIVATE_QUICK_CHAT_FROM = 42 // TODO
    const val REGION = 43 // TODO
    const val INTERFACE_CUSTOM_HEAD = 44 // TODO
    const val OBJECT_REMOVE = 45 // TODO
    const val UPDATE_CHUNK = 46 // TODO
    const val OBJECT_ANIMATION_SPECIFIC = 47 // TODO
    const val FLOOR_ITEM_ADD = 48 // TODO
    const val SCRIPT = 50 // TODO
    const val LOGOUT = 51 // TODO
    const val CLIENT_VARC_STR = 54 // TODO
    const val IGNORE_LIST = 57// TODO
    const val INTERFACE_MODEL = 58// TODO
    const val LOGOUT_LOBBY = 59// TODO
    const val FLOOR_ITEM_REVEAL = 60// TODO
    const val PROJECTILE_ADD = 62// TODO
    const val MINI_SOUND = 65// TODO
    const val INTERFACE_WINDOW = 102
    const val PLAYER_UPDATING = 69// TODO
    const val INTERFACE_CLOSE = 73// TODO
    const val PRIVATE_CHAT_TO = 77// TODO
    const val INTERFACE_ITEMS_UPDATE = 80// TODO
    const val FLOOR_ITEM_UPDATE = 83// TODO
    const val CLIENT_VARBIT_LARGE = 68
    const val FRIEND_LIST = 85// TODO
    const val WORLD_LIST = 88// TODO
    const val PROJECTILE_DISPLACE = 90// TODO
    const val PUBLIC_CHAT = 91// TODO
    const val SKILL_LEVEL = 47
    const val OBJECT_ANIMATION = 96// TODO
    const val PRIVATE_QUICK_CHAT_TO = 97// TODO
    const val INTERFACE_NPC_HEAD = 98// TODO
    const val OBJECT_CUSTOMISE = 99// TODO
    const val CLIENT_VARP = 7
    const val PLAYER_WEIGHT = 103// TODO
    const val CHAT = 102// TODO
    const val INTERFACE_COMPONENT_POSITION = 104// TODO
    const val IGNORE_LIST_UPDATE = 105// TODO
    const val INTERFACE_PLAYER_OTHER_BODY = 109// TODO
    const val CLIENT_VARC = 3
    const val CLIENT_VARC_LARGE = 16
    const val INTERFACE_PLAYER_HEAD = 114// TODO
    const val INTERFACE_COMPONENT_VISIBILITY = 117// TODO
    const val SOUND_AREA = 119// TODO
    const val PRIVATE_CHAT_FROM = 120// TODO
    const val INTERFACE_COMPONENT_ORIENTATION = 122// TODO
    const val DYNAMIC_REGION = 128// TODO
    const val PRIVATE_STATUS = 134// TODO
    const val FRIEND_LIST_DISCONNECT = 135// TODO

    /* Handshake */
    const val LOGIN_HANDSHAKE = 14
    const val GAME_LOGIN = 16
    const val LOBBY_LOGIN = 19
}