package world.gregs.voidps.engine.data.serial

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.math.BigDecimal

object DoubleSerializer : StdSerializer<Double>(Double::class.java) {
    override fun serialize(value: Double, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(toBigDecimal(value))
    }

    fun toBigDecimal(double: Double): BigDecimal {
        var bigDecimal = double.toBigDecimal()
        if (!bigDecimal.toPlainString().contains(".")) {
            bigDecimal = bigDecimal.setScale(1)
        }
        return bigDecimal
    }
}