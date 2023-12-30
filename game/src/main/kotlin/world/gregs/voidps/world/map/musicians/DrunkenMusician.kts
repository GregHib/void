package world.gregs.voidps.world.map.musicians

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Drunk
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.RollEyes
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.PlayerChoice
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && target.id == "drunken_musician" && option == "Talk-to" }) { player: Player ->
    choice()
}

suspend fun CharacterContext.choice() {
    choice {
        option<Unsure>("Who are you?") {
            npc<Drunk>("Me? I'sh mooshian! Lemme her help youse relaxsh: sit down, reshst your weery limz an' stuff. You'll feel mush better. Like me, I ffeel great!")
            player<Unsure>("You're drunk, aren't you?")
            npc<Drunk>("I'm jus' relaxshed, mate.")
            player<Unsure>("I'm not sure I want to be as relaxed as you are.")
            npc<Drunk>("Youze'll never be as relaxshed as as I am, I worked hard to get this relaxshed.")
            player<RollEyes>("Clearly...")
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
        option("How does resting work?") {
            player<Unsure>("So how does resting work?")
            npc<Drunk>("Well, youze sit down and resht. Then you feel better. Mush better.")
            npc<Drunk>("If youze are lissening to my relaxshing moozik then iss even bettar. Relaxshing moozik, like mine.")
            player<Unsure>("Right; that's nice and clear. Thanks.")
            resting()
        }
        option<Happy>("What's special about resting by a musician?") {
            npc<Drunk>("Moozik's great! My moozik is the bessht. Mush more relaxshing than those else.")
            resting()
        }
        option<Happy>("Can you summarise the effects for me?") {
            npc<Drunk>("Yeshh, 'course. 'f youze sit down you resht. Moozik make reshting better.")
            resting()
        }
        exit()
    }
}

suspend fun PlayerChoice.exit(): Unit = option<Unsure>("That's all for now") {
    npc<Drunk>("Fanks. Sshtay relaxshed!")
}