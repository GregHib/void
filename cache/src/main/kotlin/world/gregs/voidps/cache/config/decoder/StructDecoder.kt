package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Config.STRUCTS
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.definition.Parameters

class StructDecoder(
    private val parameters: Parameters = Parameters.EMPTY
) : ConfigDecoder<StructDefinition>(STRUCTS) {

    override fun create(size: Int) = Array(size) { StructDefinition(it) }


    override fun load(cache: Cache): Array<StructDefinition> {
        val definitions = super.load(cache)
        // Manually fix values see https://github.com/GregHib/void/issues/540
        val extras = definitions[1330].extras as MutableMap<String, Any>
        extras["1296"] = 3
        extras["1297"] = 19
        extras["1298"] = 2
        extras["1299"] = 37
        return definitions
    }

    override fun StructDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 249) {
            readParameters(buffer, parameters)
        }
    }
}