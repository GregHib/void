package world.gregs.voidps.engine.client.command

import world.gregs.voidps.engine.entity.character.player.PlayerRights

/**
 * Data about a command
 */
data class Command(
    val name: String,
    val rights: PlayerRights = PlayerRights.None,
    val signatures: List<CommandSignature> = emptyList()
) {
    /**
     * Find the signature which closest matches the given [input] arguments
     */
    fun find(input: List<String>): CommandSignature? {
        val matches = signatures.mapNotNull { sig ->
            sig.score(input)?.let { score -> sig to score }
        }
        return matches.maxByOrNull { it.second }?.first
    }
}