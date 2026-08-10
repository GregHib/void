package content.area.kandarin.yanille

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.skillcapeShop
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class RobeStoreOwner : Script {

    init {
        npcOperate("Trade", "robe_store_owner_yanille") {
            openRobeShop(this)
        }

        npcOperate("Talk-to", "robe_store_owner_yanille") {
            npc<Neutral>("Welcome to the Magic Guild Store. Do you want to see my wares?")

            choice {
                option("Can you tell me about your cape?") {
                    player<Neutral>("Can you tell me about your cape?")
                    npc<Happy>("Certainly! Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
                    npc<Neutral>("Is there anything else I can help you with?")

                    choice {
                        option("I'd like to view your store, please.") {
                            openRobeShop(this)
                        }
                        option("No thank you.") {
                            player<Neutral>("No thank you.")
                        }
                    }
                }

                option("Yes, please.") {
                    openRobeShop(this)
                }

                option("No thank you.") {
                    player<Neutral>("No thank you.")
                }
            }
        }
    }

    fun openRobeShop(player: Player) {
        player.openShop(player.skillcapeShop(Skill.Magic, "magic_guild_store_mystic_robes"))
    }
}
