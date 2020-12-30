package rs.dusk.network.codec.update.encode

import io.netty.channel.Channel
import rs.dusk.network.codec.Encoder

class UpdateResponseEncoder : Encoder() {

    fun encode(
        channel: Channel,
        indexId: Int,
        archiveId: Int,
        data: ByteArray,
        compression: Int,
        length: Int,
        attributes: Int
    ) {
        val realLength = if (compression != 0) length + 4 else length
        channel.send(getLength(realLength)) {
            writeByte(indexId)
            writeShort(archiveId)
            writeByte(attributes)
            writeInt(length)
            for (offset in 5 until realLength + 5) {
                if (writerIndex() % 512 == 0) {
                    writeByte(255)
                }
                writeByte(data[offset].toInt())
            }
        }
    }

    private fun getLength(length: Int): Int {
        var count = 8
        for (offset in 5 until length + 5) {
            if (count % 512 == 0) {
                count++
            }
            count++
        }
        return count
    }

}