package ru.snapix.snapicooperation.listeners

import com.velocitypowered.api.event.Subscribe
import ru.snapix.library.message
import ru.snapix.snapicooperation.api.events.party.*
import ru.snapix.snapicooperation.placeholders
import ru.snapix.snapicooperation.settings.Settings
import ru.snapix.snapicooperation.toPlayer

class PartyListener {
    val message get() = Settings.message.party()

    @Subscribe
    fun onDisband(event: PartyDisbandEvent) {
        val party = event.party
        when (event.reason) {
            DisbandReason.DISABLE_PLUGIN -> {
                party.leader.toPlayer().message(message.disband().disablePlugin())
                party.players.mapNotNull { it.toPlayer() }.forEach { it.message(message.disband().disablePlugin(), *party.placeholders()) }
            }

            DisbandReason.USER_DISBAND -> {
                party.leader.toPlayer().message(message.disband().leaderDisbandForLeader(), *party.placeholders())
                party.players.mapNotNull { it.toPlayer() }.forEach { it.message(message.disband().leaderDisbandForMembers(), *party.placeholders()) }
            }
        }
    }

    @Subscribe
    fun onCreateUser(event: PartyCreateUserEvent) {
        val party = event.party
        val player = event.player

        party.leader.toPlayer().message(message.createUser().messageForMembers())
        party.players.mapNotNull { it.toPlayer() }.filter { it != player }.forEach {
            it.message(
                message.createUser().messageForMembers(),
                *party.placeholders(),
                "name" to player.username
            )
        }
        player.message(message.createUser().messageForAddedPlayer(), *party.placeholders(), "name" to player.username)
    }

    @Subscribe
    fun onRemoveUser(event: PartyRemoveUserEvent) {
        val party = event.party
        val player = event.player

        party.leader.toPlayer().message(message.removeUser().messageForMembers(),
            *party.placeholders(),
            "name" to player.username)
        party.players.mapNotNull { it.toPlayer() }.filter { it != player }.forEach {
            it.message(
                message.removeUser().messageForMembers(),
                *party.placeholders(),
                "name" to player.username
            )
        }
        player.message(message.removeUser().messageForRemovedPlayer(), *party.placeholders(), "name" to player.username)
    }

    @Subscribe
    fun onChangeLeader(event: PartyChangeLeaderEvent) {
        val oldLeader = event.oldLeader
        val newLeader = event.newLeader
        val party = event.party

        oldLeader.message(
            message.changeLeader().messageForOldLeader(),
            *party.placeholders(),
            "name" to oldLeader.username
        )
        newLeader.message(
            message.changeLeader().messageForNewLeader(),
            *party.placeholders(),
            "name" to oldLeader.username
        )
        party.players.mapNotNull { it.toPlayer() }.filter { it != oldLeader && it != newLeader }.forEach {
            it.message(
                message.changeLeader().messageForMembers(),
                *party.placeholders(),
                "name" to oldLeader.username
            )
        }
    }

    @Subscribe
    fun onSendInvitation(event: PartySendInvitationEvent) {
        val party = event.party
        val invited = event.player

        party.leader.toPlayer().message(
            message.sendInvitation().messageForSender(),
            *party.placeholders(),
            "name" to invited.username
        )
        invited.message(message.sendInvitation().messageForInvited(), *party.placeholders(), "name" to invited.username)
    }

    @Subscribe
    fun onResponseInvitation(event: PartyResponseInvitationEvent) {
        val config = message.responseInvitation()

        val party = event.party
        val sender = party.leader
        val invited = event.player

        when (event.status) {
            InvitationStatus.ACCEPT -> {
                invited.message(config.accept().messageForInvited(), *party.placeholders(), "name" to invited.username)
            }

            InvitationStatus.DECLINE -> {
                sender.toPlayer().message(config.decline().messageForSender(), *party.placeholders(), "name" to invited.username)
                invited.message(config.decline().messageForInvited(), *party.placeholders(), "name" to invited.username)
            }

            InvitationStatus.IGNORE -> {
                sender.toPlayer().message(config.ignore().messageForSender(), *party.placeholders(), "name" to invited.username)
                invited.message(config.ignore().messageForInvited(), *party.placeholders(), "name" to invited.username)
            }

            InvitationStatus.REMOVE_LEADER -> {
                sender.toPlayer().message(
                    config.removeLeader().messageForSender(),
                    *party.placeholders(),
                    "name" to invited.username
                )
                invited.message(config.removeLeader().messageForInvited(), *party.placeholders(), "name" to invited.username)
            }
        }
    }
}