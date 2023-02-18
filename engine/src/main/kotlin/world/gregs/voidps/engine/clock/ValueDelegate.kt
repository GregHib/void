package world.gregs.voidps.engine.clock

import world.gregs.voidps.engine.entity.Values

class ValueDelegate(
    val values: Values?
) : MapDelegate {

    override fun get(name: String): Int? {
        return values?.get(name) as? Int
    }

    override fun set(name: String, value: Int) {
        values?.set(name, value)
    }

    override fun remove(name: String) {
        values?.remove(name)
    }
}