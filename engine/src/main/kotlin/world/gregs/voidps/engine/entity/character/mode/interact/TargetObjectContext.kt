package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.obj.GameObject

interface TargetObjectContext : CharacterContext {
    val target: GameObject
}