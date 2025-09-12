package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.FinishRegionLoad
import world.gregs.voidps.network.login.protocol.Decoder

class RegionLoadedDecoder : Decoder(0) {

    override suspend fun decode(packet: Source): Instruction = FinishRegionLoad
}
