package content.area.misthalin.draynor_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceBuilder
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

npcOperate("Talk-to", "leela") {
    when(player["prince_ali_rescue", "unstarted"]) {
        "key" -> {
            npc<Talk>("My father sent this key for you. Be careful not to lose it.")
        }
         "leela" -> {
            player<Happy>("I am here to help you free the prince.")
            npc<Talk>("Your employment is known to me. Now, do you know all that we need to make the break?")
            choice {
                disguise()
                key()
                guards()
                equipment()
            }
        }
        else -> {
            player<Happy>("What are you waiting here for?")
            npc<Neutral>("That is no concern of yours, adventurer.")
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.key() {
    option<Talk>("I need to get the key made.") {
        npc<Talk>("Yes, that is most important. There is no way you can get the real key. It is on a chain around Keli's neck. Almost impossible to steal.")
        npc<Talk>("Get some soft clay and get her to show you the key somehow. Then take the print, with bronze, to my father.")
        choice {
            disguise()
            guards()
            equipment()
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.guards() {
    option<Talk>("What can I do with the guards?") {
        npc<Talk>("Most of the guards will be easy. The disguise will get past them. The only guard who will be a problem will be the one at the door.")
        npc<Laugh>("We can discuss this more when you have the rest of the escape kit.")
        choice {
            disguise()
            key()
            equipment()
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.equipment() {
    option<Talk>("I will go and get the rest of the escape equipment.") {
        npc<Shifty>("Good, I shall await your return with everything.")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.disguise() {
    option<Quiz>("I must make a disguise. What do you suggest?") {
        npc<Talk>("Only the lady Keli can wander about outside the jail. The guards will shoot to kill if they see the prince out, so we need a disguise good enough to fool them at a distance.")
        npc<Talk>("You need a wig, maybe made from wool. If you find someone who can work with wool ask them about it. There's a witch nearby who may be able to help you dye it.")
        if (player.inventory.contains("pink_skirt")) {
            npc<Talk>("You have got the skirt, good.")
        }
        npc<Talk>("We still need something to colour the Prince's skin lighter. There's a witch close to here. She knows about many things. She may know some way to make the skin lighter.")
        if (player.inventory.contains("rope")) {
            npc<Shifty>("You have rope I see, to tie up Keli. That will be the most dangerous part of the plan.")
        } else {
            npc<Shifty>("You will still need some rope to tie up Keli, of course. I heard that there's a good rope maker around here.")
        }
        choice {
            key()
            guards()
            equipment()
        }
    }
}