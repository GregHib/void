package content.skill.summoning.familiar

import content.skill.summoning.follower
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random
import kotlin.math.ceil

class CompostMound : Script {
    init {
        npcOperate("Interact", "compost_mound_familiar") { (target) ->
            if (target != follower) {
                return@npcOperate
            }
            choice {
                option("Talk-to") {
                    talk()
                }
                option("Boost Farming") {
                    if (levels.get(Skill.Farming) <= levels.getMax(Skill.Farming)) {
                        val boost = ceil(1 + levels.getMax(Skill.Farming) * 0.02).toInt()
                        levels.boost(Skill.Farming, boost)
                    }
                }
            }
        }
    }

    private suspend fun Player.talk() {
        when (random.nextInt(5)) {
            0 -> {
                npc<Neutral>("Oi've gotta braand new comboine 'aarvester!")
                player<Happy>("A what?")
                npc<Neutral>("Well, it's a flat bit a metal wi' a 'andle that I can use ta 'aarvest all combinations o' plaants.")
                player<Happy>("You mean a spade?")
                npc<Neutral>("Aye, 'aat'll be it.")
            }
            1 -> {
                npc<Neutral>("What we be doin' 'ere, zur?")
                player<Happy>("Oh, I have a few things to take care of here, is all.")
                npc<Neutral>("Aye, right ye are, zur. Oi'll be roight there.")
            }
            2 -> {
                npc<Neutral>("Errr...are ye gonna eat that?")
                player<Happy>("Eat what?")
                npc<Neutral>("Y've got summat on yer, goin' wastin'.")
                player<Happy>("Ewwww!")
                npc<Neutral>("So ye don' want it then?")
                player<Happy>("No I do not want it! Nor do I want to put my boot in your mouth for you to clean it off.")
                npc<Neutral>("An' why not?")
                player<Happy>("It'll likely come out dirtier than when I put it in!")
            }
            3 -> {
                npc<Neutral>("Sigh...")
                player<Happy>("What's the matter?")
                npc<Neutral>("Oi'm not 'appy carryin' round these young'uns where we're going.")
                player<Happy>("Young'uns? Oh, the buckets of compost! Well, those wooden containers will keep them safe.")
                npc<Neutral>("'Aah, that be a mighty good point, zur.")
            }
            4 -> {
                npc<Neutral>("Oi wus just a-wonderin'...")
                player<Happy>("Oh! What have you been eating! Your breath is making my eyes water!")
                npc<Neutral>("Oi! Oi'm 'urt by thaat.")
                player<Happy>("Sorry.")
                npc<Neutral>("Oi mean, oi even et some mints earlier.")
                player<Happy>("You did?")
                npc<Neutral>("'At's roight. Oi found some mint plaants in a big pile o' muck, and oi 'ad 'em fer me breakfast.")
                player<Happy>("The mystery resolves itself.")
            }
        }
    }
}
