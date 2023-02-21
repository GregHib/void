package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.CharacterList

class CharacterHitActionTask(
    private val characters: CharacterList<*>
) : Runnable {
    override fun run() {
        for (character in characters) {
            if (character.hits.isNotEmpty()) {
                for (hit in character.hits) {
                    character.events.emit(hit)
                }
                character.hits.clear()
            }
//            character.action.tick()
        }
    }
}