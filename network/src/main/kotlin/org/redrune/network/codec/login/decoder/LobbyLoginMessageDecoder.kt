package org.redrune.network.codec.login.decoder

import com.github.michaelbull.logging.InlineLogger
import org.redrune.network.codec.login.LoginHeader
import org.redrune.network.codec.login.LoginOpcodes
import org.redrune.network.codec.login.message.LobbyLoginMessage
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.packet.PacketReader
import org.redrune.network.model.packet.PacketType

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class LobbyLoginMessageDecoder : MessageDecoder(PacketType.SHORT, intArrayOf(LoginOpcodes.LOBBY_LOGIN)) {

    private val logger = InlineLogger()

    override fun decode(reader: PacketReader): Message {
        val triple = LoginHeader.decode(reader)
        /*if(triple.first != ReturnNode.NORMAL) {
            ctx.clientRespond(triple.first)
            return LobbyLoginMessage()
        }*/
        val password = triple.second!!
        val isaacKeys = triple.third!!

        val username = reader.readString()
        val highDefinition = reader.readBoolean()
        val resizeable = reader.readBoolean()
        reader.skip(24)//TODO
        val settings = reader.readString()
        val affiliate = reader.readInt()
        /*
		for (int index = 0; index < 36; index++) {
			int crc = Cache.STORE.getIndexes()[index] == null ? 0 : Cache.STORE.getIndexes()[index].getCRC();
			int receivedCrc = buffer.readInt();
			if (crc != receivedCrc && index < 32) {
				session.write(new LoginResponseCodePacketBuilder(UPDATED)).addListener(ChannelFutureListener.CLOSE);
				System.out.println("index=" + index + ", crc=" + crc + ", receivedCrc=" + receivedCrc);
				return;
			}
		}
		*/

        logger.info { "username=$username, password=$password"}

        return LobbyLoginMessage()
    }

}