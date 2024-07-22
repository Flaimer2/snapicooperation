package ru.snapix.snapicooperation

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import ru.snapix.library.message

fun CommandSource?.chatMessage(sender: Player, text: String) {
    var message = text
    message(message)
}