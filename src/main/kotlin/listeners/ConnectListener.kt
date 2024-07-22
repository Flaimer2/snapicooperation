package ru.snapix.snapicooperation.listeners

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import ru.snapix.snapicooperation.api.Party
import ru.snapix.snapicooperation.api.User

class ConnectListener {
    @Subscribe(order = PostOrder.EARLY)
    fun onLogin(event: LoginEvent) {
        User[event.player] ?: User.create(event.player)
    }

    @Subscribe(order = PostOrder.EARLY)
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