package org.redrune.network.codec.handshake.request

enum class HandshakeRequestType(val opcode: Int) {

    LOGIN(14),

    UPDATE(15),

    LOGIN_CONNECTION(16),

    RECONNECTING_LOGIN(18),

    LOBBY_CONNECTION(19),

    USER_REGISTRATION(22),

    WORLD_LIST_REQUEST(23),

    WORLD_SUITABILITY_CHECK(24),

    GAME_LOGIN_CONTINUE(26),

    EMAIL_AVAILABILITY(28),

    /*     INIT_GAME_CONNECTION = new LoginProt(14, 0);
        GAME_LOGIN = new LoginProt(16, -2);
        LOBBY_LOGIN = new LoginProt(19, -2);
        REQUEST_WORLDLIST = new LoginProt(23, 4);
        CHECK_WORLD_SUITABILITY = new LoginProt(24, -1);
        GAMELOGIN_CONTINUE = new LoginProt(26, 0);
        SSL_WEBCONNECTION = new LoginProt(27, 0);
        CREATE_ACCOUNT_CONNECT = new LoginProt(28, -2);
        INIT_SOCIAL_NETWORK_CONNECTION = new LoginProt(29, -2);
        SOCIAL_NETWORK_LOGIN = new LoginProt(30, -2);
        INIT_DEBUG_CONNECTION = new LoginProt(31, 4);*/


    /* 174 aClass133_3406 = new Class133(14);
                    175 aClass133_3409 = new Class133(15);
                    176 aClass133_3411 = new Class133(16);
                    177 aClass133_3412 = new Class133(17);
                    178 aClass133_3413 = new Class133(19);
                    179 aClass133_3414 = new Class133(22);
                    180 aClass133_3415 = new Class133(23);
                    181 aClass133_3416 = new Class133(24);
                    182 aClass133_3417 = new Class133(26);
                    183 aClass133_3418 = new Class133(27);
                    184 aClass133_3419 = new Class133(28);
                    185 aClass133_3420 = new Class133(29);
                    186 aClass133_3421 = new Class133(30);
*/

    ;

    companion object {
        val types = values().associateBy(HandshakeRequestType::opcode)

        fun valueOf(opcode: Int): HandshakeRequestType? {
            return types[opcode]
        }
    }
}