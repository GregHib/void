package content.area.misthalin.barbarian_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class Litara : Script {

    // https://www.youtube.com/watch?v=SDxhq3iCiH0
    init {
        npcOperate("Talk-to", "litara_barbarian_village") {
            npc<Happy>("Hello there. You look lost - are you okay?")
            choice {
                option<Confused>("I'm looking for a stronghold, or something.") {
                    npc<Happy>("Ahh, the Stronghold of Security. It's down there.")
                    statement("Litara points to the hole in the ground that looks like you could squeeze through.")
                    player<Scared>("Looks kind of..deep and dark.")
                    npc<Disheartened>("Yeah, tell that to my brother. He still hasn't come back.")
                    player<Quiz>("Your brother?")
                    npc<Idle>("He's an explorer too. When the miner fell down that hole he'd made and came back babbling about treasure, my brother went to explore. No one has seen him since.")
                    player<Disheartened>("Oh, that's not good.")
                    npc<Neutral>("Lots of people have been down there, but none of them have seen him. Let me know if you do, will you?")
                    player<Neutral>("I'll certainly keep my eyes open.")
                }
                option<Happy>("I'm fine, just passing through.")
            }
        }
    }
}
