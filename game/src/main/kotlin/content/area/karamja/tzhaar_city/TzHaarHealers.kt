package content.area.karamja.tzhaar_city

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.Timer

class TzHaarHealers : Script {

    init {
        npcCondition("weakened_nearby_monsters") {
            val zones = tile.zone.toRectangle(1).toZones()
            for (zone in zones) {
                for (npc in NPCs.at(zone)) {
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
                for (npc in NPCs.at(zone)) {
                    if (npc.levels.get(Skill.Constitution) < npc.levels.getMax(Skill.Constitution) / 2) {
                        heal(npc, 100)
                        return@npcAttack
                    }
                }
            }
        }

        npcMoved("yt_hur_kot") {
            if (softTimers.contains("yt_hur_kot_heal")) {
                return@npcMoved
            }
            val jad = NPCs.at(tile.regionLevel).firstOrNull { it.id == "tztok_jad" } ?: return@npcMoved
            if (tile.within(jad.tile, 5)) {
                softTimers.start("yt_hur_kot_heal")
            }
        }

        npcTimerStart("yt_hur_kot_heal") { 4 }

        npcTimerTick("yt_hur_kot_heal") {
            val jad = NPCs.at(tile.regionLevel).firstOrNull { it.id == "tztok_jad" } ?: return@npcTimerTick Timer.CONTINUE
            if (!tile.within(jad.tile, 5)) {
                return@npcTimerTick Timer.CONTINUE
            }
            val healed = jad.levels.restore(Skill.Constitution, 50)
            if (healed > 0) {
                anim("yt_hur_kot_heal")
                jad.gfx("tzhaar_heal")
                jad.directHit(50, "healed")
                areaSound("self_heal", tile, radius = 10)
            }
            Timer.CONTINUE
        }
    }

    private fun heal(target: NPC, amount: Int) {
        val amount = target.levels.restore(Skill.Constitution, amount)
        if (amount > 0) {
            target.directHit(amount, "healed")
            target.gfx("tzhaar_heal")
        }
    }
}
