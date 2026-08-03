package content.area.asgarnia.burthorpe.rogues_den

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.skillcapeMastered
import content.entity.player.dialogue.skillcapeShop
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has

class MartinThwait : Script {
    init {
        npcOperate("Talk-to", "martin_thwait") {
            npc<Shifty>("You know it's sometimes funny how things work out, I lose some gold but find an item, or I lose an item and find some gold... no-one ever knows what's gone where ya know.")
            choice {
                option("Yeah I know what you mean, found anything recently?") {
                    lostAndFound()
                }
                option<Quiz>("Can you tell me about your cape?") {
                    npc<Happy>("Certainly! Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
                    npc<Neutral>("The Cape of Thieving provides a nice boost to your chances of pickpocketing when worn. Is there anything else I can help you with?")
                    choice {
                        option("Have you found anything recently?") {
                            lostAndFound()
                        }
                        option("No thank you.")
                    }
                }
                option<Shock>("Okay... I'll be going now.")
            }
        }
    }

    suspend fun Player.lostAndFound() {
        when {
            skillcapeMastered(Skill.Thieving) || (has(Skill.Thieving, 50) && has(Skill.Agility, 50)) -> openShop(skillcapeShop(Skill.Thieving, "martin_thwaits_lost_and_found"))
            !has(Skill.Thieving, 50) -> npc<Sad>("Sorry, mate. Train up your Thieving skill to at least 50 and I might be able to help you out.")
            else -> npc<Sad>("Sorry, mate. Train up your Agility skill to at least 50 and I might be able to help you out.")
        }
    }
}
