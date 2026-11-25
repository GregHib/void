package content.area.kandarin.battlefield

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class LieutenantSchepbur : Script {
    init {
        npcOperate("Talk-to", "lieutenant_schepbur_battlefield") {
            npc<Neutral>("Move into position lads! eh? Who are you and what do you want?")
            player<Neutral>("Who are you then?")
            npc<Neutral>("Lieutenant Shepbur, commanding officer of the new Armoured Tortoise Regiment.")
            player<Neutral>("There's only two tortoises here, that's hardly a regiment.")
            npc<Neutral>("This is just the beginning! Gnome breeders and trainers are already working to expand the number of units.  Soon we'll have hundreds of these beauties, nay thousands! And they will not only carry mages and")
            npc<Neutral>("archers but other fiendish weapons of destruction of gnome devising. An army of giant tortoises will march upon this battlefield and rain the fire of our wrath upon all our enemies! Nothing will be able to stop us!")
            player<Neutral>("Oooookayy...... I'll leave you to it then....")
        }
    }
}
