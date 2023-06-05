package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.obj.GameObject

abstract class ObjectInteraction : Interaction() {
    abstract val obj: GameObject
}