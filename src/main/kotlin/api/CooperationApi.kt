package ru.snapix.snapicooperation.api

import ru.snapix.library.SnapiLibrary
import ru.snapix.library.network.player.NetworkPlayer
import ru.snapix.snapicooperation.caches.Parties

object CooperationApi {
    fun playerInParty(): Set<NetworkPlayer> {
        return Parties.values().flatMap { listOf(it.leader, *it.players.toTypedArray()) }.toSet()
    }

    fun playerWithoutParty(): Set<NetworkPlayer> {
        val players = SnapiLibrary.getOnlinePlayers().toMutableSet()
        players.removeAll(playerInParty())
        return players.toSet()
    }
}