package world.gregs.voidps.engine.entity.character.effect

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.CharacterEffect
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.npc.flagTransform
import world.gregs.voidps.engine.entity.character.update.visual.npc.transform
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.emote
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.traverse.LargeTraversal
import world.gregs.voidps.engine.path.traverse.MediumTraversal
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.utility.get

data class Transform(val npc: Int) : CharacterEffect("transform") {

    override fun onPlayerStart(player: Player) {
        val definitions = get<NPCDefinitions>()
        val definition = definitions.get(npc)
        player.emote = definition.renderEmote
        player.size = Size(definition.size, definition.size)
        val collisions: Collisions = get()
        player.movement.traversal = when (definition.size) {
            1 -> SmallTraversal(TraversalType.Land, false, collisions)
            2 -> MediumTraversal(TraversalType.Land, false, collisions)
            else -> LargeTraversal(TraversalType.Land, false, player.size, collisions)
        }
        player.appearance.apply {
            transform = npc
            size = definition.size
            idleSound = definition.idleSound
            crawlSound = definition.crawlSound
            walkSound = definition.walkSound
            runSound = definition.runSound
            soundDistance = definition.soundDistance
        }
        player.flagAppearance()
    }

    override fun onPlayerFinish(player: Player) {
        player.emote = 1426
        player.size = Size.TILE
        val collisions: Collisions = get()
        player.movement.traversal = SmallTraversal(TraversalType.Land, false, collisions)
        player.appearance.apply {
            transform = -1
            size = Size.TILE.width
            idleSound = -1
            crawlSound = -1
            walkSound = -1
            runSound = -1
            soundDistance = 0
        }
        player.flagAppearance()
    }

    override fun onNPCStart(npc: NPC) {
        npc.transform.id = this.npc
        npc.flagTransform()
    }

    override fun onNPCFinish(npc: NPC) {
        npc.transform.id = -1
        npc.flagTransform()
    }
}