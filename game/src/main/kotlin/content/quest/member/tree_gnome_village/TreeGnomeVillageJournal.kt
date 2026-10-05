package content.quest.member.tree_gnome_village

import content.quest.questStage
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get

fun Player.treeGnomeVillageJournal(): List<String> {
    val stage = questStage("tree_gnome_village")
    val history = mutableListOf<String>()
    if (stage >= 1) history += "I spoke to King Bolren who told me that one of their orbs of protection has been stolen by Khazard troops."
    if (get("tree_gnome_village_montai_met", false)) history += "I spoke to Montai. The orb was in the Khazard stronghold North of the battlefield, but it was secure in there."
    if (stage >= 2) history += "I brought Montai logs to help fortify their defences."
    if (get("tree_gnome_village_briefed", false)) history += "Now their defences were secure, they were ready to use the Gnome Ballista to break through the enemy defences."
    if (stage >= 4) history += "I found the three trackers and used their coordinates to fire the ballista straight into the Khazard stronghold."
    if (stage >= 5) history += "With the stronghold breached by ballista fire, I was able to make my way inside and recover the orb of protection."
    if (stage >= 6) history += "I returned the orb to King Bolren, but while I was busy getting it the Khazard troops invaded the village and stole the remaining two orbs before heading north."
    if (stage >= 8) history += "After a fierce battle I defeated the warlord who'd stolen the orbs, and reclaimed them for the gnome people."
    if (get("tree_gnome_village_ceremony", 0) == 2) history += "I returned the orbs to King Bolren and the gnomes performed their ceremony."
    if (stage == 9) history += "King Bolren rewarded me with a gnome amulet and allowed me to use the spirit tree."

    val current = when {
        stage == 0 -> "I can start this quest by speaking to <red>King Bolren<navy> in <red>Tree Gnome Village<navy>, in the centre of the maze."
        stage == 9 -> "<red>QUEST COMPLETE!"
        get("tree_gnome_village_ceremony", 0) == 2 -> "I should speak to King Bolren now - hopefully he will reward me"
        stage >= 8 -> "I should return them now to <red>King Bolren<navy> where they belong"
        stage >= 6 -> "I should retrieve the <red>two remaining orbs<navy> from the warlord"
        stage == 5 -> "I should return the <red>orb of protection<navy> to <red>King Bolren<navy>."
        stage == 4 -> "With the <red>Khazard stronghold<navy> exposed I should get inside and retrieve the gnomes' <red>orb of protection<navy>"
        stage == 3 || get("tree_gnome_village_briefed", false) -> "I need to head into the <red>battlefield<navy> and find the <red>three Gnome trackers<navy> who have the <red>firing coordinates<navy> and then fire the <red>ballista!<navy>"
        stage == 2 -> "I should speak to Montai again and see what to do next."
        get("tree_gnome_village_montai_met", false) -> "I should bring <red>six normal logs<navy> to <red>Commander Montai<navy>."
        else -> "I should speak to <red>Commander Montai<navy> at the battlefield north of the maze."
    }
    val font = get<FontDefinitions>().get("p12_full")
    val lines = mutableListOf<String>()
    for (paragraph in history) {
        lines += font.splitLines(paragraph, 350).map { "<str>$it" }
        lines += ""
    }
    var colour = "<navy>"
    for (line in font.splitLines(current, 350)) {
        lines += "$colour$line"
        colour = Regex("<(red|navy)>").findAll(line).lastOrNull()?.value ?: colour
    }
    return lines
}
