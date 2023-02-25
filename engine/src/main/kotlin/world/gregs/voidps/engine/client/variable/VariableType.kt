package world.gregs.voidps.engine.client.variable

import net.pearx.kasechange.toTitleCase

enum class VariableType {
    Varp,
    Varbit,
    Varc,
    Varcstr,
    Custom;

    companion object {
        fun byName(name: String) = valueOf(name.toTitleCase())
    }
}