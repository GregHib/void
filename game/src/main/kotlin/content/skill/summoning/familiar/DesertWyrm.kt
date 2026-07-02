package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.callFollower
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class DesertWyrm : Script {
    init {
        // Burrow - the wyrm digs underground to the nearest ore rock and pops back up with the ore.
        npcOperate("Burrow", "desert_wyrm_familiar") { (target) ->
            val familiar = follower
            if (familiar == null || target != familiar) {
                return@npcOperate
            }
            val ore = bestBurrowOre()
            if (ore == null) {
                message("There are no rocks around here for the desert wyrm to mine from!")
                return@npcOperate
            }
            familiar.anim("desert_wyrm_burrow")
            familiar.gfx("desert_wyrm_burrow")
            delay(8)
            callFollower()
            follower?.let { FloorItems.add(it.tile, ore, disappearTicks = TimeUnit.MINUTES.toTicks(3), owner = this) }
        }

        npcOperate("Interact", "desert_wyrm_familiar") {
            if (equipped(EquipSlot.Weapon).id.endsWith("pickaxe")) {
                npc<Neutral>("If you have that pick, why make me dig?")
                player<Happy>("Because it's a little quicker and easier on my arms.")
                npc<Neutral>("I should take industrial action over this...")
                player<Happy>("You mean you won't work for me any more?")
                npc<Neutral>("No. It means me and the lads feed you legs-first into some industrial machinery, maybe the Blast Furnace.")
                player<Happy>("I'll just be over here, digging.")
                npc<Neutral>("That's the spirit, lad!")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("This is so unsafe...I should have a hard hat for this work...")
                    player<Happy>("Well, I could get you a rune helm if you like - those are pretty hard.")
                    npc<Neutral>("Keep that up and you'll have the union on your back!")
                }
                1 -> {
                    npc<Neutral>("You can't touch me, I'm part of the union!")
                    player<Happy>("Is that some official \"no-touching\" policy or something?")
                    npc<Neutral>("You really don't get it, do you $name?")
                }
                2 -> {
                    npc<Neutral>("You know, you might want to register with the union.")
                    player<Happy>("What are the benefits?")
                    npc<Neutral>("I stop bugging you to join the union.")
                    player<Happy>("Ask that again later; I'll have to consider that generous proposal.")
                }
                3 -> {
                    npc<Neutral>("Why are you ignoring that good ore seam, mister?")
                    player<Happy>("Which ore seam?")
                    npc<Neutral>("There's a good ore seam right underneath us at this very moment.")
                    player<Happy>("Great! How long will it take for you to get to it?")
                    npc<Neutral>("Five years, give or take.")
                    player<Happy>("Five years!")
                    npc<Neutral>("That's if we go opencast, mind. I could probably reach it in three if I just dug.")
                    player<Happy>("Right. I see. I think I'll skip it thanks.")
                }
            }
        }
    }

    /**
     * The best ore the wyrm can burrow for among the rocks within [radius] tiles, or null if there
     * are none. Like the live game the wyrm only mines the low tiers below silver, and of those picks
     * the highest tier available ([burrowOres], worst to best). A rock is any object with a
     * `rocks.<id>.ores` table entry; depleted rocks have no entry and are skipped.
     */
    private fun Player.bestBurrowOre(radius: Int = 7): String? {
        var best: String? = null
        var bestRank = -1
        for (tile in tile.spiral(radius)) {
            for (obj in GameObjects.at(tile)) {
                val ores = Tables.itemListOrNull("rocks.${obj.id}.ores") ?: continue
                for (ore in ores) {
                    val rank = burrowOres.indexOf(ore)
                    if (rank > bestRank) {
                        bestRank = rank
                        best = ore
                    }
                }
            }
        }
        return best
    }

    private companion object {
        /** Ores the desert wyrm can burrow for - the tiers below silver, worst to best. */
        val burrowOres = listOf("copper_ore", "tin_ore", "clay", "blurite_ore", "iron_ore")
    }
}
