package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

interface CharacterMode : PlayerMode, NPCMode {
    override fun tick(player: Player) = tick(player as Character)

    override fun tick(npc: NPC) = tick(npc as Character)

    fun tick(character: Character)
}