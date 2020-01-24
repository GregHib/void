package org.redrune.network.codec.update

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 2:51 a.m.
 */
data class FileRequest(val indexId: Int, val archiveId: Int, val priority: Boolean)