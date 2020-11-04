package rs.dusk.engine.entity.character.effect

import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.CharacterEffect
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.npc.flagTransform
import rs.dusk.engine.entity.character.update.visual.npc.transform
import rs.dusk.engine.entity.character.update.visual.player.appearance
import rs.dusk.engine.entity.character.update.visual.player.emote
import rs.dusk.engine.entity.character.update.visual.player.flagAppearance
import rs.dusk.engine.entity.definition.NPCDefinitions
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.path.TraversalType
import rs.dusk.engine.path.traverse.LargeTraversal
import rs.dusk.engine.path.traverse.MediumTraversal
import rs.dusk.engine.path.traverse.SmallTraversal
import rs.dusk.utility.get

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
        player.movement.traversal = get<SmallTraversal>()
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