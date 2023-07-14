package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.obj.GameObject

interface ObjectTargetContext : CharacterContext {
    val target: GameObject
}