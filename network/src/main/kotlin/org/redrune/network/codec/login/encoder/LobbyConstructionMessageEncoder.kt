package org.redrune.network.codec.login.encoder

import com.github.michaelbull.logging.InlineLogger
import org.redrune.network.codec.login.message.LobbyConstructionMessage
import org.redrune.network.model.message.MessageEncoder
import org.redrune.network.model.packet.PacketBuilder
import org.redrune.tools.func.NetworkFunc
import sun.audio.AudioPlayer.player
import java.util.*


/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 10, 2020
 */
class LobbyConstructionMessageEncoder : MessageEncoder<LobbyConstructionMessage>() {

    override fun encode(out: PacketBuilder, msg: LobbyConstructionMessage) {
        // leave the cast because of json boxing.
        val lastLogin =  System.currentTimeMillis()
        val now = System.currentTimeMillis()
        val jag = 1014753880308L
        val since_jag = (now - jag) / 1000 / 60 / 60 / 24
        val since_log = (now - lastLogin) / 1000 / 60 / 60 / 24
        var lastIp: String = msg.lastIPAddress

        out.writeByte(msg.clientRight) // rights
        out.writeByte(0) // blackmarks
        out.writeByte(0) // muted? (bool)
        out.writeByte(0) // dunno (bool)
        out.writeByte(0) // dunno (bool)

        out.writeLong(Date().time) // members subscription end
        out.writeByte(0) // 0x1 - if members, 0x2 - subscription
        out.writeInt(0) // recovery questions set date

        out.writeByte(2) // 0 - Not a member, 1 - Membership expires, 2 - Subscription active
        out.writeInt(0)
        out.writeByte(0)
        out.writeInt(0)

        out.writeShort(1) //recovery questions set
        out.writeShort(0) //Number of unread messages

        out.writeShort((since_jag - since_log).toInt()) // last logged in date
        out.writeInt(NetworkFunc.IPAddressToNumber(lastIp)) //Resolve hostname - last login ip

        out.writeByte(3) //Email registration: 0 - Unregisted, 1 - Pending Parental Confirm, 2 - Pending Confirm, 3 - Registered, 4 - No longer registered, 5 - Blank
        out.writeShort(0)
        out.writeShort(0) // 	loginResponse.AppendShort(0);

        out.writeByte(1) //   Script 6909

        out.writeGJString(msg.username)

        out.writeByte(0) // //Script 6911r
        out.writeInt(0) //  loginResponse.AppendInt(character.Name.StartsWith("#") ? 0 : 1);
        out.writeByte(0) // Bool Script 4700
        out.writeShort(1) // TODO: specify world id worldid

        out.writeGJString("127.0.0.1")
    }
}