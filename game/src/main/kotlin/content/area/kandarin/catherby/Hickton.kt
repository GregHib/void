package content.area.kandarin.catherby

import content.entity.npc.shop.buy.itemBought
import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

@Script
class Hickton {

    init {
        npcOperate("Trade", "hickton") {
            openHicktonShop(player)
        }

        npcOperate("Talk-to", "hickton") {
            npc<Talk>("Welcome to Hickton's Archery Store. Do you want to see my wares?")

            choice {
                option("Can you tell me about your cape?") {
                    player<Talk>("Can you tell me about your cape?")
                    npc<Talk>("Certainly! Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
                    npc<Talk>("Is there anything else I can help you with?")

                    choice {
                        option("I'd like to view your store, please.") {
                            openHicktonShop(player)
                        }
                        option("No thank you.") {
                            player<Talk>("No thank you.")
                        }
                    }
                }

                option("Yes, please.") {
                    openHicktonShop(player)
                }

                option("No, I prefer to bash things close up.") {
                    player<Talk>("No, I prefer to bash things close up.")
                }
            }
        }

        itemBought("fletching_cape") { player ->
            player.inventory.add("fletching_hood")
        }

        itemBought("fletching_cape_(t)") { player ->
            player.inventory.add("fletching_hood")
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
