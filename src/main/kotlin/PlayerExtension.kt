package ru.snapix.snapicooperation

import com.velocitypowered.api.proxy.Player
import kotlin.jvm.optionals.getOrNull

fun String.toPlayer(): Player? {
    return plugin.server.getPlayer(this).getOrNull()
}