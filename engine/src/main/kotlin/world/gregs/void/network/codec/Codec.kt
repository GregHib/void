package world.gregs.void.network.codec

import io.netty.channel.Channel
import io.netty.util.AttributeKey
import world.gregs.void.engine.TimedLoader
import world.gregs.void.network.codec.Codec.Companion.CODEC_KEY
import world.gregs.void.network.codec.Codec.Companion.IN_CIPHER_KEY
import world.gregs.void.network.codec.Codec.Companion.OUT_CIPHER_KEY
import world.gregs.void.network.crypto.IsaacCipher

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class Codec : TimedLoader<Unit>(this::class.java.simpleName){

    val decoders = HashMap<Int, Decoder>()

    fun registerDecoder(opcode: Int, decoder: Decoder) {
        if (decoders[opcode] != null) {
            throw IllegalArgumentException("Cannot have duplicate decoders $decoder $opcode")
        }
        decoders[opcode] = decoder
    }

    fun registerHandler(opcode: Int, handler: Handler) {
        val decoder = getDecoder(opcode) ?: throw IllegalArgumentException("Missing decoder $opcode $handler")
        if (decoder.handler != null) {
            throw IllegalArgumentException("Cannot have duplicate handlers $opcode $handler")
        }
        decoder.handler = handler
    }

    fun getDecoder(opcode: Int): Decoder? {
        return decoders[opcode]
    }

    companion object {
        /**
         * The attribute in the [channel][Channel] that identifies the [codec][Codec]
         */
        val CODEC_KEY: AttributeKey<Codec> = AttributeKey.valueOf("codec.key")
        val IN_CIPHER_KEY: AttributeKey<IsaacCipher> = AttributeKey.valueOf("cipher.in.key")
        val OUT_CIPHER_KEY: AttributeKey<IsaacCipher> = AttributeKey.valueOf("cipher.out.key")
    }
}


fun Channel.getCipherOut(): IsaacCipher? {
    return attr(OUT_CIPHER_KEY).get()
}

fun Channel.setCipherOut(cipher: IsaacCipher?) {
    attr(OUT_CIPHER_KEY).set(cipher)
}

fun Channel.getCipherIn(): IsaacCipher? {
    return attr(IN_CIPHER_KEY).get()
}

fun Channel.setCipherIn(cipher: IsaacCipher?) {
    attr(IN_CIPHER_KEY).set(cipher)
}

/**
 * Getting the codec of the channel
 * @receiver Channel
 */
fun Channel.getCodec(): Codec? {
    return attr(CODEC_KEY).get()
}

/**
 * Setting the codec of the channel
 * @receiver Channel
 */
fun Channel.setCodec(codec: Codec) {
    attr(CODEC_KEY).set(codec)
    setCipherIn(null)
    setCipherOut(null)
}