package content.area.karamja.tzhaar_city

import content.area.karamja.tzhaar_city.TzHaar.whatDidYouCallMe
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name

class TzHaarFightCaveGuard : Script {
    init {
        npcOperate("Talk-to", "tzhaar_mej_jal") {
            npc<Shifty>("You want help JalYt-${TzHaar.caste(this)}-${name}?")
            choice {
                option<Quiz>("What is this place?") {
                    npc<Neutral>("This is the fight cave, TzHaar-Xil made it for practice, but many JalYt come here to fight too. Just enter the cave and make sure you're prepared.")
                    choice {
                        option<Quiz>("Are there any rules?") {
                            npc<Confused>("Rules? Survival is the only rule in there.")
                            choice {
                                option<Quiz>("Do I win anything?") {
                                    npc<Neutral>("You ask a lot of questions. Might give you TokKul if you last long enough.")
                                    player<Neutral>("...")
                                    npc<Sad>("Before you ask, TokKul is like your Coins.")
                                    npc<Angry>("Gold is like you JalYt, soft and easily broken, we use hard rock forged in fire like TzHaar!")
                                }
                                option<Happy>("Sounds good.")
                            }
                        }
                        option<Happy>("Ok thanks.")
                    }
                }
                whatDidYouCallMe(it.target)
                option<Neutral>("No I'm fine thanks.")
            }
        }
    }
}