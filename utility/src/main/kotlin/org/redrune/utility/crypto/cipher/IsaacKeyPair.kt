package org.redrune.utility.crypto.cipher

import org.redrune.core.tools.crypto.IsaacCipher

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 20, 2020
 */
class IsaacKeyPair(seed: IntArray) {
    val inCipher: IsaacCipher = IsaacCipher(seed)
    val outCipher: IsaacCipher

    init {
        for (i in seed.indices)
            seed[i] += 50
        outCipher = IsaacCipher(seed)
    }

}