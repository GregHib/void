package content.area.kandarin.catherby

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class Hickton : Script {

    init {
        npcOperate("Trade", "hickton") {
            openHicktonShop(this)
        }

        npcOperate("Talk-to", "hickton") {
            npc<Neutral>("Welcome to Hickton's Archery Store. Do you want to see my wares?")

            choice {
                option("Can you tell me about your cape?") {
                    player<Neutral>("Can you tell me about your cape?")
                    npc<Neutral>("Certainly! Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
                    npc<Neutral>("Is there anything else I can help you with?")

                    choice {
                        option("I'd like to view your store, please.") {
                            openHicktonShop(this)
                        }
                        option("No thank you.") {
                            player<Neutral>("No thank you.")
                        }
                    }
                }

                option("Yes, please.") {
                    openHicktonShop(this)
                }

                option("No, I prefer to bash things close up.") {
                    player<Neutral>("No, I prefer to bash things close up.")
                }
            }
        }

        bought("fletching_cape") {
            inventory.add("fletching_hood")
        }

        bought("fletching_cape_(t)") {
            inventory.add("fletching_hood")
        }
    }

    // Helper function to open the appropriate shop based on player's skill levels
    fun openHicktonShop(player: Player) {
        when {
            Skill.all.any { it != Skill.Fletching && player.levels.getMax(it) == Level.MAX_LEVEL } -> {
                player.openShop("hicktons_archery_emporium_trimmed")
            }
            player.levels.getMax(Skill.Fletching) == Level.MAX_LEVEL -> {
                player.openShop("hicktons_archery_emporium_skillcape")
            }
            else -> {
                player.openShop("hicktons_archery_emporium")
            }
        }
    }
}
