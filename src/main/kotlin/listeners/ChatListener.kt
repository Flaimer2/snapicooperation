package ru.snapix.snapicooperation.listeners

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import ru.snapix.library.bukkit.utils.sendMessage
import ru.snapix.library.network.events.bukkit.DisconnectEvent
import ru.snapix.library.utils.message
import ru.snapix.snapicooperation.api.Party
import ru.snapix.snapicooperation.settings.Settings

class ChatListener : Listener {
    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val config = Settings.config.chatPrefix()
        if (!config.enable()) return

        var message = event.message

        if (!config.prefix().any { message.startsWith(it) }) return
        config.prefix().forEach { message = message.removePrefix(it) }

        val party = Party[event.player.name] ?: return

        event.isCancelled = true

        var format = Settings.config.chatFormat()

        format = PlaceholderAPI.setPlaceholders(event.player, format)
        format = format.replace("%message%", message)

        val receivers = listOf(party.leader, *party.players.toTypedArray())
        receivers.sendMessage(format)
    }
}