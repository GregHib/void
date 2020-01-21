/*
package org.redrune.network.packet.outgoing.login

import org.redrune.network.packet.struct.PacketHeader
import org.redrune.tools.func.TextFunc

class LobbyBuilderPacket : OutgoingPacket(2, PacketHeader.BYTE) {

    init {
        // leave the cast because of json boxing.
        // leave the cast because of json boxing.
        val lastLogin = System.currentTimeMillis() */
/*((Number) player.getVariables().getAttribute(AttributeKey.LAST_LONGIN_STAMP, System.currentTimeMillis())).longValue();*//*

        val now = System.currentTimeMillis()
        val jag = 1014753880308L
        val since_jag = (now - jag) / 1000 / 60 / 60 / 24
        val since_log = (now - lastLogin) / 1000 / 60 / 60 / 24
        var lastIp: String = */
/*player.getAttributes().getLastIP()*//*
"127.0.0.1"
       */
/* if (lastIp == null) {
            lastIp = Misc.getIpAddress(player.getSession().getChannel())
        }*//*


        writeByte(2*/
/*player.getDominantRight().getClientRight()*//*
) // rights

        writeByte(0) // blackmarks

        writeByte(0) // muted? (bool)

        writeByte(0) // dunno (bool)

        writeByte(0) // dunno (bool)

        writeLong(0) // members subscription end

        writeByte(0) // 0x1 - if members, 0x2 - subscription

        writeInt(0) // recovery questions set date

        writeByte(2) // 0 - Not a member, 1 - Membership expires, 2 - Subscription active

        writeInt(0)
        writeByte(0)
        writeInt(0)

        writeShort(1) //recovery questions set

        writeShort(0) //Number of unread messages

        writeShort((since_jag - since_log).toInt()) // last logged in date

        writeInt(TextFunc.IPAddressToNumber(lastIp)) //Resolve hostname - last login ip

        writeByte(3) //Email registration: 0 - Unregisted, 1 - Pending Parental Confirm, 2 - Pending Confirm, 3 - Registered, 4 - No longer registered, 5 - Blank

        writeShort(0)
        writeShort(0) // 	loginResponse.AppendShort(0);

        writeByte(1) //   Script 6909

        writeGJString("username"*/
/*player.getUsername()*//*
)

        writeByte(0) // //Script 6911r

        writeInt(0) //  loginResponse.AppendInt(character.Name.StartsWith("#") ? 0 : 1);

        writeByte(0) // Bool Script 4700

        writeShort(1) // worldid


        writeGJString("127.0.0.1")
    }

}*/
