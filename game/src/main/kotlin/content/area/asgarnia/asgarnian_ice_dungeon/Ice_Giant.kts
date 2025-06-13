package content.area.asgarnia.asgarnian_ice_dungeon

import content.entity.combat.hit.hit
import content.entity.combat.npcCombatSwing
import content.entity.sound.sound

// Handle attack sound and animation (when Ice Giant attacks)
npcCombatSwing("ice_giant") { npc ->
    npc.anim("ice_giant_attack") // Replace with your actual attack animation
    val attackSound = "ice_giant_attack" // Random attack sound: _0 or _1
    target.sound(attackSound) // Play the attack sound

    // Perform the hit on the target (NPC always hits here)
    npc.hit(target, type = "melee")

    // Handle the hit sound after the attack is made
    val hitSound = "ice_giant_hit" // Random hit sound: _0 or _1
    target.sound(hitSound) // Play the hit sound after the hit
}
