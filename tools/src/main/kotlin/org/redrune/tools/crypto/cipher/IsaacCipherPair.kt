package org.redrune.tools.crypto.cipher

/**
 * A pair of two [IsaacCipher] random number generators used as a stream cipher. One takes the role of an encoder
 * for this endpoint, the other takes the role of a decoder for this endpoint.
 *
 * @author Graham
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-18
 */
class IsaacCipherPair(val encodingCipher: IsaacCipher, val decodingCipher: IsaacCipher)