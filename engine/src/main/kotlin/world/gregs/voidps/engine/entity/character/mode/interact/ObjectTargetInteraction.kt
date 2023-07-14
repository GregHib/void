package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.obj.GameObject

abstract class ObjectTargetInteraction : PlayerInteraction() {
    abstract val obj: GameObject
}