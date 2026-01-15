package content.area.karamja.tzhaar_city

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class TzHaarHealers(
    val npcs: NPCs,
) : Script {
    init {
        npcCondition("weakened_nearby_monsters") {
            val zones = tile.zone.toRectangle(1).toZones()
            for (zone in zones) {
                for (npc in npcs[zone]) {
                    if (npc.levels.get(Skill.Constitution) < npc.levels.getMax(Skill.Constitution) / 2) {
                        return@npcCondition true
                    }
                }
            }
            false
        }

        npcAttack("yt_mej_kot", "heal") {
            val zones = tile.zone.toRectangle(1).toZones()
            for (zone in zones) {
                for (npc in npcs[zone]) {
                    if (npc.levels.get(Skill.Constitution) < npc.levels.getMax(Skill.Constitution) / 2) {
                        heal(npc, 100)
                        return@npcAttack
                    }
                }
            }
        }

        npcCondition("target_is_jad", ::nearJad)
        npcCondition("target_is_player") { it is Player }

        npcAttack("yt_hur_kot", "heal") {
            val jad = npcs[tile.regionLevel].firstOrNull { it.id == "tztok_jad" } ?: return@npcAttack
            heal(jad, 50)
        }
    }

    private fun nearJad(npc: NPC, character: Character): Boolean {
        return character is NPC && character.id == "tztok_jad"
    }

    private fun heal(target: NPC, amount: Int) {
        val amount = target.levels.restore(Skill.Constitution, amount)
        if (amount > 0) {
            target.directHit(amount, "healed")
            target.gfx("tzhaar_heal")
        }
    }
}