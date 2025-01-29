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
        var extras = definitions[1330].extras as MutableMap<String, Any>
        extras["1296"] = 3
        extras["1297"] = 19
        extras["1298"] = 2
        extras["1299"] = 37
        extras = definitions[1337].extras as MutableMap<String, Any>
        extras["1294"] = 62
        extras["1295"] = 8
        extras["1296"] = 14
        extras["1297"] = 13
        extras["1298"] = 13
        extras["1299"] = 10
        extras = definitions[1342].extras as MutableMap<String, Any>
        extras["1294"] = 62
        extras["1295"] = 13
        extras["1296"] = 12
        extras["1297"] = 56
        extras = definitions[1450].extras as MutableMap<String, Any>
        extras["1298"] = 2
        extras["1299"] = 75
        extras["1300"] = 20
        extras["1301"] = 65
        extras = definitions[1346].extras as MutableMap<String, Any>
        extras["1294"] = 13
        extras["1295"] = 60
        extras = definitions[1649].extras as MutableMap<String, Any>
        extras["1296"] = 10
        extras["1297"] = 17
        extras = definitions[1658].extras as MutableMap<String, Any>
        extras["1296"] = 12
        extras["1297"] = 50
        return definitions
    }

    override fun StructDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 249) {
            readParameters(buffer, parameters)
        }
    }
}