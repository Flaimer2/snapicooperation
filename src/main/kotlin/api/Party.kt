package ru.snapix.snapicooperation.api

import com.velocitypowered.api.proxy.Player
import kotlinx.serialization.Serializable
import ru.snapix.library.callEvent
import ru.snapix.library.message
import ru.snapix.library.runLaterTask
import ru.snapix.snapicooperation.api.events.party.*
import ru.snapix.snapicooperation.caches.Parties
import ru.snapix.snapicooperation.placeholders
import ru.snapix.snapicooperation.plugin
import ru.snapix.snapicooperation.settings.Settings
import ru.snapix.snapicooperation.toPlayer
import kotlin.time.Duration.Companion.seconds

@Serializable
class Party {
    var leader: String
    val players: MutableSet<String>
    val invitations: MutableSet<String>
    val maxSize: Int
    val size get() = players.size + invitations.size
    var date: Long? = null

    internal constructor(
        leader: String,
        players: MutableSet<String>,
        invitations: MutableSet<String>,
        maxSize: Int = 4
    ) {
        this.leader = leader
        this.players = players
        this.invitations = invitations
        this.maxSize = maxSize
    }

    companion object {
        @JvmStatic
        fun create(leader: Player, players: Set<String> = emptySet()): Party {
            val party = Party(leader.username, players.toMutableSet(), mutableSetOf())

            Parties.add(party)

            plugin.callEvent(PartyCreateEvent(party))

            return party
        }

        @JvmStatic
        operator fun get(key: String): Party? {
            return Parties[key]
        }

        @JvmStatic
        operator fun get(player: Player): Party? {
            return Parties.values().find { it.inParty(player) }
        }
    }

    /**
    Перед вызовом метода, нужно вызвать [PartyDisbandEvent]
     */
    fun remove() {
        Parties.remove(this)
        invitations.clear()
        players.mapNotNull { it.toPlayer() }.forEach { plugin.callEvent(PartyRemoveUserEvent(it, this, RemoveUserReason.PARTY_DISBAND)) }
        players.clear()
    }

    fun addPlayer(player: Player) {
        if (players.isEmpty()) date = System.currentTimeMillis()
        players.add(player.username)
        invitations.remove(player.username)
        Parties.update(this)
        plugin.callEvent(PartyCreateUserEvent(player, this))
    }

    fun removePlayer(player: Player) {
        players.remove(player.username)
        Parties.update(this)
        plugin.callEvent(PartyRemoveUserEvent(player, this, RemoveUserReason.USER_REMOVE))
    }

    fun createInvitation(player: Player) {
        if (invitations.add(player.username)) {
            Parties.update(this)
            plugin.callEvent(PartySendInvitationEvent(player, this))
            plugin.server.runLaterTask(Settings.config.invitationDelay().seconds) {
                if (!invitations.contains(player.username)) return@runLaterTask
                invitations.remove(player.username)
                Parties.update(this)
                plugin.callEvent(PartyResponseInvitationEvent(player, this, InvitationStatus.IGNORE))
            }
        }
    }

    fun removeInvitation(player: Player) {
        invitations.remove(player.username)
        Parties.update(this)
        plugin.callEvent(PartyResponseInvitationEvent(player, this, InvitationStatus.REMOVE_LEADER))
    }

    fun isLeader(player: Player): Boolean {
        return leader.equals(player.username, ignoreCase = true)
    }

    fun changeLeader(player: Player) {
        val oldLeader = leader.toPlayer() ?: return
        Parties.remove(this)
        players.remove(player.username)
        players.add(oldLeader.username)
        leader = player.username
        Parties.add(this)
        plugin.callEvent(PartyChangeLeaderEvent(oldLeader, player, this))
    }

    fun inParty(player: Player): Boolean {
        return players.contains(player.username) || isLeader(player)
    }

    fun accept(player: Player) {
        if (!invitations.contains(player.username)) {
            player.message(Settings.message.party().commands().accept().notInvite(), *this.placeholders())
            return
        }
        addPlayer(player)
        plugin.callEvent(PartyResponseInvitationEvent(player, this, InvitationStatus.ACCEPT))
    }

    fun decline(player: Player) {
        if (!invitations.contains(player.username)) {
            player.message(Settings.message.party().commands().deny().notInvite(), *this.placeholders())
            return
        }
        invitations.remove(player.username)
        Parties.update(this)
        plugin.callEvent(PartyResponseInvitationEvent(player, this, InvitationStatus.DECLINE))
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
