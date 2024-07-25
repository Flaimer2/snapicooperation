package ru.snapix.snapicooperation.api

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import ru.snapix.library.bukkit.utils.callEvent
import ru.snapix.library.network.player.NetworkPlayer
import ru.snapix.library.network.player.OfflineNetworkPlayer
import ru.snapix.library.network.player.OnlineNetworkPlayer
import ru.snapix.snapicooperation.api.events.party.*
import ru.snapix.snapicooperation.caches.Parties
import ru.snapix.snapicooperation.plugin
import ru.snapix.snapicooperation.settings.Settings
import kotlin.time.Duration.Companion.seconds

@Serializable
class Party {
    var leader: NetworkPlayer
    val players: MutableSet<NetworkPlayer>
    val invitations: MutableSet<NetworkPlayer>
    val maxSize: Int
    val size get() = players.size + invitations.size + 1
    var date: Long? = null

    internal constructor(
        leader: NetworkPlayer,
        players: MutableSet<NetworkPlayer>,
        invitations: MutableSet<NetworkPlayer>,
        maxSize: Int = 4,
    ) {
        this.leader = leader
        this.players = players
        this.invitations = invitations
        this.maxSize = maxSize
    }

    companion object {
        @JvmStatic
        fun create(leader: Player, players: Set<String> = emptySet()): Party {
            val party = Party(
                OnlineNetworkPlayer(leader.name),
                players.map { OnlineNetworkPlayer(it) }.toMutableSet(),
                mutableSetOf()
            )

            Parties.add(party)

            callEvent(PartyCreateEvent(party))

            return party
        }

        @JvmStatic
        operator fun get(key: String): Party? {
            return Parties.values().find { it.inParty(OfflineNetworkPlayer(key)) }
        }

        @JvmStatic
        operator fun get(player: NetworkPlayer): Party? {
            return Parties.values().find { it.inParty(player) }
        }
    }

    fun remove() {
        callEvent(PartyDisbandEvent(leader, this))
        Parties.remove(this)
        invitations.clear()
        players.map { it }.forEach { callEvent(PartyRemoveUserEvent(it, this, RemoveUserReason.PARTY_DISBAND)) }
        players.clear()
    }

    fun addPlayer(player: NetworkPlayer) {
        if (players.isEmpty()) date = System.currentTimeMillis()
        players.add(player)
        invitations.remove(player)
        Parties.update(this)
        callEvent(PartyAddUserEvent(player, this))
    }

    fun removePlayer(player: NetworkPlayer) {
        players.remove(player)
        Parties.update(this)
        callEvent(PartyRemoveUserEvent(player, this, RemoveUserReason.USER_REMOVE))
    }

    fun leave(player: NetworkPlayer) {
        players.remove(player)
        Parties.update(this)
        callEvent(PartyRemoveUserEvent(player, this, RemoveUserReason.USER_LEAVE))
    }

    fun createInvitation(player: NetworkPlayer) {
        if (invitations.add(player)) {
            Parties.update(this)
            callEvent(PartySendInvitationEvent(player, this))
            plugin.server.scheduler.runTaskLater(plugin, {
                val party = Party[leader] ?: return@runTaskLater
                if (party.invitations.contains(player)) {
                    party.invitations.remove(player)
                    Parties.update(party)
                    callEvent(PartyResponseInvitationEvent(player, party, InvitationStatus.IGNORE))
                }
            }, Settings.config.invitationDelay().seconds.inWholeSeconds * 20)
        }
    }

    fun removeInvitation(player: NetworkPlayer) {
        invitations.remove(player)
        Parties.update(this)
        callEvent(PartyResponseInvitationEvent(player, this, InvitationStatus.REMOVE_LEADER))
    }

    fun isLeader(player: NetworkPlayer): Boolean {
        return leader == player
    }

    fun isLeader(name: String): Boolean {
        return leader == OfflineNetworkPlayer(name)
    }

    fun changeLeader(player: NetworkPlayer) {
        val oldLeader = leader
        Parties.remove(this)
        players.remove(player)
        players.add(oldLeader)
        leader = player
        Parties.add(this)
        callEvent(PartyChangeLeaderEvent(oldLeader, player, this))
    }

    fun inParty(player: NetworkPlayer): Boolean {
        return players.contains(player) || isLeader(player)
    }

    fun accept(player: NetworkPlayer) {
        if (!invitations.contains(player)) {
            player.sendMessage(
                Settings.message.party().commands().accept().notInvite(),
                "party_leader" to leader.getName()
            )
            return
        }
        addPlayer(player)
        callEvent(PartyResponseInvitationEvent(player, this, InvitationStatus.ACCEPT))
    }

    fun decline(player: NetworkPlayer) {
        if (!invitations.contains(player)) {
            player.sendMessage(
                Settings.message.party().commands().deny().notInvite(),
                "party_leader" to leader.getName()
            )
            return
        }
        invitations.remove(player)
        Parties.update(this)
        callEvent(PartyResponseInvitationEvent(player, this, InvitationStatus.DECLINE))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Party) return false
        return other.leader == leader
    }

    override fun hashCode(): Int {
        var result = leader.hashCode()
        result = 31 * result + players.hashCode()
        result = 31 * result + invitations.hashCode()
        result = 31 * result + maxSize
        result = 31 * result + (date?.hashCode() ?: 0)
        return result
    }
}
