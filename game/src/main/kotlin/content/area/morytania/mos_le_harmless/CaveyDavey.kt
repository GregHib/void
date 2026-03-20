package content.area.morytania.mos_le_harmless

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class CaveyDavey : Script {
    init {
        npcOperate("Talk-to", "cavey_davey") {
            if (equipped(EquipSlot.Amulet).id == "witchwood_icon") {
                npc<Quiz>("Be ye here te deal with the Horrors?")
                player<Neutral>("I might well give it a shot.")
                npc<Sad>("Aye, well, keep yer Icon with ye if ye wants te walk out alive again.")
                player<Neutral>("Don't worry, I will.")
            } else if (inventory.contains("witchwood_icon")) {
                npc<Angry>("Be ye some form of simpleton? Do ye not hear the howlin' of the Horrors?")
                player<Neutral>("Well, I can hear something now you mention it...")
                npc<Scared>("That be them, howlin', always howlin'!")
                npc<Angry>("If ye value yer limbs ye'll put that Icon round yer neck, and hope they don't come out into the light!")
                player<Shifty>("Err, all right, I'll get right on that.")
            } else {
                npc<Angry>("Be ye mad? There be Horrors in this cave!")
                player<Quiz>("What do you mean?")
                npc<Angry>("Have ye ever heard of the sort of evil, flesh-eatin' horrors that dwell in the darkest pits of the world?")
                npc<Scared>("The sort of dark, sanity-breakin' THINGS that cause the livin' to drop to their knees and weep for the fate of all creation?")
                npc<Angry>("Well, have ye?")
                player<Neutral>("Yes, I think I've killed a few of them as well.")
                npc<Neutral>("Well, that's ok then.")
                npc<Neutral>("But, ye'll need a Witchwood Icon from a slayer master if ye want te go in these caves and live.")
                player<Quiz>("Why?")
                npc<Neutral>("Well, ye see them Jungle Horrors? Well down in the caves there be Cave Horrors.")
                npc<Neutral>("They are bigger, badder, meaner, and have a howl that freezes the blood in yer veins.")
                npc<Neutral>("Wearin' earmuffs or a helmet won't work, cos them masks they wear make the sound magical. Only thing that works is wearin' a Witchwood Icon.")
                npc<Neutral>("That is, o'course, if ye can see them, cos if ye don't have any light down there then yer likely te be picked te bones by the insects before the Horrors get ye.")
                player<Neutral>("I see, thanks for the warning.")
                npc<Neutral>("Yer welcome.")
            }
        }

        npcOperate("Talk-to", "monkey_mos_le_harmless") {
            if (equipped(EquipSlot.Amulet).id == "monkeyspeak_amulet") {
                npc<Laugh>("Eeekeek ookeek!")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Laugh>("Arr!")
                    player<Laugh>("Arr!")
                    npc<Laugh>("Arr!")
                    player<Laugh>("Arr!")
                    npc<Laugh>("Arr!")
                    player<Laugh>("Arr!")
                    npc<Laugh>("Arr!")
                    player<Laugh>("Arr!")
                    npc<Bored>("Bored now...")
                }
                1 -> {
                    npc<Scared>("Let me go, can't ye hear them? Howlin' in the dark...")
                    player<Quiz>("What do you mean?")
                    npc<Scared>("I'm not hangin' around te be killed!")
                    npc<Scared>("The Horrors, the Horrors!")
                }
                2 -> npc<Angry>("I'm not goin' back in that brewery, not fer all the Bitternuts I can carry!")
                3 -> {
                    npc<Angry>("Arr! Yer messin with me monkey plunder!")
                    player<Shock>("What?")
                }
                else -> {
                    npc<Shifty>("Are ye here for...the stuff?")
                    player<Quiz>("What?")
                    npc<Shifty>("You know...the 'special' bananas?")
                    player<Neutral>("No...why do you ask?")
                    npc<Shifty>("No reason. Have a nice day.")
                }
            }
        }
    }
}
