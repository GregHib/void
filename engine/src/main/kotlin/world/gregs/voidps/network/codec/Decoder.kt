package world.gregs.voidps.network.codec

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession

abstract class Decoder(val length: Int) {

    var handler: Handler? = null

    open fun decode(session: ClientSession, packet: Reader) {}
}