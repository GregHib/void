package org.redrune.network.codec.file

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 3:11 a.m.
 */
data class FileContents(
    val index: Int,
    val archive: Int,
    val settings: Int,
    val length: Int,
    val data: ByteArray,
    val size: Int,
    val encryption: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileContents

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}