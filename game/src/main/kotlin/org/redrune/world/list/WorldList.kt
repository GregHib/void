package org.redrune.world.list

import org.redrune.world.list.WorldListFlags.FLAG_HIGHLIGHT
import org.redrune.world.list.WorldListFlags.FLAG_LOOTSHARE
import org.redrune.world.list.WorldListFlags.FLAG_MEMBERS

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 22, 2020
 */
object WorldList {


    /**
     * The map of world entries
     */
    val worlds = HashMap<Int, WorldListEntry>()

    init {
        worlds[1] = WorldListEntry(
            "Game Server",
            "127.0.0.1",
            38,
            (FLAG_MEMBERS xor FLAG_LOOTSHARE xor FLAG_HIGHLIGHT),
            "Canada",
            true
        )
    }

}