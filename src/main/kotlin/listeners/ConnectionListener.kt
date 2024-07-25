package ru.snapix.snapicooperation.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import ru.snapix.library.network.events.bukkit.DisconnectEvent
import ru.snapix.snapicooperation.api.Party

class ConnectionListener : Listener {
    @EventHandler
    fun onDisconnect(event: DisconnectEvent) {
        val player = event.player
        val party = Party[player] ?: return

        if (party.isLeader(player)) {
            party.remove()
        } else {
            party.removePlayer(player)
        }
    }
}