package world.gregs.voidps.world.map.musicians

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.CheerfulOld
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.TalkingOld
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.PlayerChoice
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc

on<NPCOption>({ operate && target.id == "goblin_musician" && option == "Talk-to" }) { player: Player ->
    choice()
}

suspend fun CharacterContext.choice() {
    choice {
        option<Unsure>("Who are you?") {
            npc<CheerfulOld>("Me? Thump-Thump. Me make thump-thumps with thump-thump drum. Other goblins listen.")
            choice()
        }
        option("Can I ask you some questions about resting?") {
            resting()
        }
        exit()
    }
}

suspend fun CharacterContext.resting() {
    choice("Can I ask you some questions about resting?") {
        option<Unsure>("How does resting work?") {
            npc<TalkingOld>("You stoopid. Goblin sit down, goblin rest, goblin feel better.")
            resting()
        }
        option<Happy>("What's special about resting by a musician?") {
            npc<TalkingOld>("Drumming good! Make you feel better, boom booms make you run longer!")
            resting()
        }
        option<Happy>("Can you summarise the effects for me?") {
            npc<TalkingOld>("Wot? You sit down, you rest. Listen to Thump-Thump is better.")
            resting()
        }
        exit()
    }
}

suspend fun PlayerChoice.exit(): Unit = option<Unsure>("That's all for now.") {
    npc<CheerfulOld>("You listen to boom boom. Good!")
}