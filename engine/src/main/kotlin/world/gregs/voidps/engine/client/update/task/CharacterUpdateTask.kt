package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.client.update.task.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.task.player.PlayerUpdateTask
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.player.Player

class CharacterUpdateTask(
    iterator: TaskIterator<Player>,
    override val characters: CharacterList<Player>,
    private val playerUpdating: PlayerUpdateTask,
    private val npcUpdating: NPCUpdateTask
) : CharacterTask<Player>(iterator) {

    override fun predicate(character: Player): Boolean {
        return character.client != null
    }

    override fun run() {
        super.run()
        characters.shuffle()
    }

    override fun run(character: Player) {
        playerUpdating.run(character)
        npcUpdating.run(character)
        character.viewport.players.update()
        character.viewport.npcs.update()
    }
}