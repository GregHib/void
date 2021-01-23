package world.gregs.voidps.network.codec.login.encode

import io.netty.channel.Channel
import world.gregs.voidps.buffer.write.writePrefixedString
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.LOBBY_DETAILS
import world.gregs.voidps.network.packet.PacketSize
import java.util.*

class LobbyConfigurationEncoder : Encoder(LOBBY_DETAILS, PacketSize.BYTE) {

    fun encode(
        channel: Channel,
        username: String,
        lastIpAddress: String,
        lastLogin: Long
    ) = channel.send(52 + string(username) + string("127.0.0.1") + 2) {
        val now = System.currentTimeMillis()
        val jag = 1014753880308L
        val sinceJag = (now - jag) / 1000 / 60 / 60 / 24
        val sinceLog = (now - lastLogin) / 1000 / 60 / 60 / 24
        writeByte(0)//Black marks
        writeByte(0)//Muted
        writeByte(0)//3
        writeByte(0)//4
        writeByte(0)//5

        writeLong(lastLogin)//members subscription end
        writeByte(0)//0 not member & no recovery, 1 member & no rec, 2 not member & rec, 3 member & recovery
        writeInt(0)//recovery questions set date

        writeByte(2)// 0 - Not a member, 1 - Membership expires, 2 - Subscription active
        writeInt(0)
        writeByte(0)
        writeInt(0)

        writeShort(1)//recovery questions set
        writeShort(0)//Number of unread messages
        writeShort((sinceJag - sinceLog).toInt())// last logged in date
        writeInt(ipAddressToNumber(lastIpAddress))//Resolve hostname - last login ip

        writeByte(3)//Email registration: 0 - Unregistered, 1 - Pending Parental Confirm, 2 - Pending Confirm, 3 - Registered, 4 - No longer registered, 5 - Blank
        writeShort(0)//Credit card expiration time
        writeShort(0)//Credit card loyalty expiration time
        writeByte(1)//Script 6909

        writePrefixedString(username)

        writeByte(0)//Script 6911
        writeInt(1)//Script 6912
        writeByte(1)//Bool Script 4700
        writeShort(0)//Server port offset id

        writePrefixedString("127.0.0.1")//Server ip address
    }

    companion object {
        private fun ipAddressToNumber(ipAddress: String?): Int {
            val st = StringTokenizer(ipAddress, ".")
            val ip = IntArray(4)
            var i = 0
            while (st.hasMoreTokens()) {
                ip[i++] = st.nextToken().toInt()
            }
            return ip[0] shl 24 or (ip[1] shl 16) or (ip[2] shl 8) or ip[3]
        }
    }

}
