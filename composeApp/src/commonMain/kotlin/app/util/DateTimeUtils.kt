package app.util

import kotlin.time.Clock

fun nowIsoString(): String = Clock.System.now().toString()
