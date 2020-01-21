package org.redrune.network.codec.js5

/**
 * Representing a request for a file from the server
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
data class FileRequest(val indexId: Int, val archiveId: Int, val priority: Boolean)