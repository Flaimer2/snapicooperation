package ru.snapix.snapicooperation.api

import org.bukkit.entity.Player
import ru.snapix.library.SnapiLibrary
import ru.snapix.library.network.player.NetworkPlayer
import ru.snapix.library.network.player.OfflineNetworkPlayer
import ru.snapix.library.network.player.OnlineNetworkPlayer
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

    fun playerInFriend(player: Player): Set<NetworkPlayer> {
        val user = User[player]
        return user.friends.map { OfflineNetworkPlayer(it) }.toSet()
    }

    fun playerWithoutFriend(player: Player): Set<NetworkPlayer> {
        val players = SnapiLibrary.getOnlinePlayers().toMutableSet()
        players.removeAll(playerInFriend(player))
        players.remove(OnlineNetworkPlayer(player.name))
        return players.toSet()
    }
}