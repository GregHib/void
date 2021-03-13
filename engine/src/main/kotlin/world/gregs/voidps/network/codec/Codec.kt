package world.gregs.voidps.network.codec

import world.gregs.voidps.engine.TimedLoader

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

    fun registerEmptyDecoder(opcode: Int, length: Int) {
        registerDecoder(opcode, object : Decoder(length) {})
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
}