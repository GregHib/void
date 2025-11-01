package world.gregs.voidps.engine.entity.character.mode.interact

interface InteractionType {
    fun hasOperate(): Boolean
    fun hasApproach(): Boolean
    fun operate()
    fun approach()
}