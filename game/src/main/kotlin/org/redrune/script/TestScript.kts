package org.redrune.script

import org.redrune.cache.Cache
import org.redrune.utility.inject

val cache: Cache by inject()

println("Test script loaded with cache: $cache")