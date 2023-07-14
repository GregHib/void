package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.obj.GameObject

interface TargetObjectContext : CharacterContext {
    val target: GameObject
}