package content.area.kandarin.ourania

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class MageOfZamorakOurania : Script {

    init {
        npcOperate("Talk-to", "mage_of_zamorak_ourania") {
            player<Quiz>("What's that ladder next to the altar for?")
            npc<Shifty>("An...archeological dig. Yeah, a dig.")
            npc<Angry>("Why?")
            player<Quiz>("Next to a Chaos Altar? That's a bit odd. Can I go down and have a look?")
            npc<Confused>("Well...I suppose. See, we found this ancient altar, but it was pretty broken so the Z.M.I. sent some of its researchers to try and repair it.")
            player<Neutral>("How'd that work out for them?")
            npc<Happy>("Pretty well - only one died. Still, they got it working...sort of. This ancient technology can be tricky.")
            player<Confused>("What do you mean, 'sort of'? Altars either work or don't work.")
            npc<Sad>("This one works, just not as you'd expect. You put pure essence in, but get random runes back. Some of them we don't even know how to craft!")
            player<Idle>("Sounds like a pretty good deal. Can I give it a go?")
            npc<Scared>("I don't think so. The mages don't like outsiders going in; they'll probably attack you. They only patrol the short path, though, so if you used the long path, through a tunnel near the entrance, you'd probably be safe.")
            player<Idle>("Thanks.")
        }
    }
}
