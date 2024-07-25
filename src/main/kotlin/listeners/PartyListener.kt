package ru.snapix.snapicooperation.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import ru.snapix.library.bukkit.utils.callEvent
import ru.snapix.snapicooperation.api.events.party.*
import ru.snapix.snapicooperation.caches.Parties
import ru.snapix.snapicooperation.settings.Settings

class PartyListener : Listener {
    val message get() = Settings.message.party()

    @EventHandler
    fun removeInvite(event: PartyCreateEvent) {
        val player = event.party.leader
        Parties.values().filter { it.invitations.contains(player) }.forEach {
            it.invitations.remove(player)
            Parties.update(it)
            callEvent(PartyResponseInvitationEvent(player, it, InvitationStatus.DECLINE))
        }
    }

    @EventHandler
    fun onDisband(event: PartyDisbandEvent) {
        val party = event.party
        party.leader.sendMessage(message.disband().leaderDisbandForLeader(), "party_leader" to party.leader.getName())
        party.players.forEach { it.sendMessage(message.disband().leaderDisbandForMembers(), "party_leader" to party.leader.getName()) }
    }

    @EventHandler
    fun onAddUser(event: PartyAddUserEvent) {
        val party = event.party
        val player = event.newPlayer

        party.leader.sendMessage(message.createUser().messageForMembers(), "name" to player.getName())
        party.players.filter { it != player }.forEach {
            it.sendMessage(
                message.createUser().messageForMembers(),
                "party_leader" to party.leader.getName(),
                "name" to player.getName()
            )
        }
        player.sendMessage(message.createUser().messageForAddedPlayer(), "party_leader" to party.leader.getName(), "name" to player.getName())
    }

    @EventHandler
    fun onRemoveUser(event: PartyRemoveUserEvent) {
        val party = event.party
        val player = event.removed

        fun sendMessageToParty(memberMessage: String?, removedMessage: String?) {
            if (memberMessage != null) {
                party.leader.sendMessage(
                    memberMessage,
                    "party_leader" to party.leader.getName(),
                    "name" to player.getName()
                )
                party.players.filter { it != player }.forEach {
                    it.sendMessage(
                        memberMessage,
                        "party_leader" to party.leader.getName(),
                        "name" to player.getName()
                    )
                }
            }
            if (removedMessage != null) {
                player.sendMessage(removedMessage, "party_leader" to party.leader.getName(), "name" to player.getName())
            }
        }

        when (event.reason) {
            RemoveUserReason.USER_REMOVE -> sendMessageToParty(
                message.removeUser().messageForMembers(),
                message.removeUser().messageForRemovedPlayer()
            )

            RemoveUserReason.USER_LEAVE -> sendMessageToParty(
                message.leave().messageForMembers(),
                message.leave().messageForRemovedPlayer()
            )

            RemoveUserReason.PARTY_DISBAND -> {}
        }
    }

    @EventHandler
    fun onChangeLeader(event: PartyChangeLeaderEvent) {
        val oldLeader = event.oldLeader
        val newLeader = event.newLeader
        val party = event.party

        oldLeader.sendMessage(
            message.changeLeader().messageForOldLeader(),
            "party_leader" to party.leader.getName(),
            "name" to oldLeader.getName()
        )
        newLeader.sendMessage(
            message.changeLeader().messageForNewLeader(),
            "party_leader" to party.leader.getName(),
            "name" to oldLeader.getName()
        )
        party.players.filter { it != oldLeader && it != newLeader }.forEach {
            it.sendMessage(
                message.changeLeader().messageForMembers(),
                "party_leader" to party.leader.getName(),
                "name" to oldLeader.getName()
            )
        }
    }

    @EventHandler
    fun onSendInvitation(event: PartySendInvitationEvent) {
        val party = event.party
        val invited = event.receiver

        party.leader.sendMessage(
            message.sendInvitation().messageForSender(),
            "party_leader" to party.leader.getName(),
            "name" to invited.getName()
        )
        invited.sendMessage(message.sendInvitation().messageForInvited(), "party_leader" to party.leader.getName(), "name" to invited.getName())
    }

    @EventHandler
    fun onResponseInvitation(event: PartyResponseInvitationEvent) {
        val config = message.responseInvitation()

        val party = event.party
        val sender = party.leader
        val invited = event.player

        when (event.status) {
            InvitationStatus.ACCEPT -> {
                invited.sendMessage(config.accept().messageForInvited(), "party_leader" to party.leader.getName(), "name" to invited.getName())
            }

            InvitationStatus.DECLINE -> {
                sender.sendMessage(config.decline().messageForSender(), "party_leader" to party.leader.getName(), "name" to invited.getName())
                invited.sendMessage(config.decline().messageForInvited(), "party_leader" to party.leader.getName(), "name" to invited.getName())
            }

            InvitationStatus.IGNORE -> {
                sender.sendMessage(config.ignore().messageForSender(), "party_leader" to party.leader.getName(), "name" to invited.getName())
                invited.sendMessage(config.ignore().messageForInvited(), "party_leader" to party.leader.getName(), "name" to invited.getName())
            }

            InvitationStatus.REMOVE_LEADER -> {
                sender.sendMessage(
                    config.removeLeader().messageForSender(),
                    "party_leader" to party.leader.getName(),
                    "name" to invited.getName()
                )
                invited.sendMessage(config.removeLeader().messageForInvited(), "party_leader" to party.leader.getName(), "name" to invited.getName())
            }
        }
    }
}