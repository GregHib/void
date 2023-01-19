package world.gregs.voidps.engine.entity.character.mode.interact.option

interface Option {
    companion object {
        operator fun invoke(any: Any): Option = when (any) {
            is String -> StringOptionValue(any)
            else -> EmptyOption
        }
    }
}