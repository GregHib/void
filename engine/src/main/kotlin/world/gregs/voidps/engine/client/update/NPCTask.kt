package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.mode.Wander.Companion.wanders
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.npc.flagTransform
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class NPCTask(
    iterator: TaskIterator<NPC>,
    override val characters: Iterable<NPC> = NPCs,
) : CharacterTask<NPC>(iterator) {

    override fun run(character: NPC) {
        checkDelay(character)
        lifecycle(character)
        if (character.mode == EmptyMode) {
            // An idle familiar (its owner still has it as their follower) resumes following its
            // owner rather than wandering or standing still after a fight ends.
            val ownerIndex = character["owner_index", -1]
            val owner = if (ownerIndex != -1) Players.indexed(ownerIndex) else null
            if (owner != null && owner.get("follower_index", -1) == character.index) {
                character.mode = Follow(character, owner)
            } else if (wanders(character)) {
                character.mode = Wander(character)
            }
        }
        healthRegen(character)
        character.softTimers.run()
        character.queue.tick()
        character.mode.tick()
        checkTileFacing(character)
    }

    private fun lifecycle(npc: NPC) {
        if (npc.contains("delay")) {
            return
        }
        if (npc.lifecycle == 0) {
            return
        }
        if (npc.lifecycle > 0) {
            if (--npc.lifecycle == 0) {
                if (npc.hide) {
                    // Respawn
                    reset(npc)
                    Spawn.npc(npc)
                } else {
                    // Revert
                    npc.visuals.transform.id = npc.def.id
                    npc.flagTransform()
                    npc.clear("transform_id")
                }
            }
        } else if (++npc.lifecycle == 0) {
            // Despawn
            NPCs.remove(npc)
        }
    }

    private fun reset(npc: NPC) {
        npc.clearAnim()
        npc.hide = false
        npc.clear("dead")
        npc.mode = EmptyMode
        npc.levels.clear()
        val respawn = npc.get<Tile>("respawn_tile") ?: return
        npc.tele(respawn)
        val dir = npc.get<Direction>("respawn_direction") ?: return
        npc.face(dir)
    }

    private fun healthRegen(character: NPC) {
        if (!character.hasClock("under_attack") && character.regenCounter++ >= character.def["regen_rate_ticks", 25] && character.levels.get(Skill.Constitution) < character.levels.getMax(Skill.Constitution)) {
            character.levels.restore(Skill.Constitution, 10)
            character.regenCounter = 0
        }
    }
}
