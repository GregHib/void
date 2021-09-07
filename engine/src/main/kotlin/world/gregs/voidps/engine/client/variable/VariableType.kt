package world.gregs.voidps.engine.client.variable

enum class VariableType {
    Varp,
    Varbit,
    Varc,
    Varcstr;

    companion object {
        fun byName(name: String?) = values().firstOrNull { it.name.toLowerCase() == name }
    }
}