package ru.snapix.snapicooperation.api

import com.velocitypowered.api.proxy.Player
import ru.snapix.snapicooperation.caches.Parties
import ru.snapix.snapicooperation.plugin
import ru.snapix.snapicooperation.toPlayer

object CooperationApi {
    fun playerInParty(player: Player): Set<Player> {
        return Party[player]?.players?.mapNotNull { it.toPlayer() }?.toSet() ?: emptySet()
    }

    fun playerInParty(): Set<Player> {
        return Parties.values().flatMap { it.players }.mapNotNull { it.toPlayer() }.toSet()
    }

    fun playerWithoutParty(): Set<Player> {
        val list = plugin.server.allPlayers
        list.removeAll(playerInParty())
        return list.toSet()
    }
}