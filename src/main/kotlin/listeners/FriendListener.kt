package ru.snapix.snapicooperation.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import ru.snapix.library.network.player.OfflineNetworkPlayer
import ru.snapix.library.network.player.OnlineNetworkPlayer
import ru.snapix.snapicooperation.api.events.friend.*
import ru.snapix.snapicooperation.settings.Settings

class FriendListener : Listener {
    val message get() = Settings.message.friend()

    @EventHandler
    fun onAddFriend(event: FriendAddEvent) {
        val sender = OfflineNetworkPlayer(event.user.name)
        val receiver = OfflineNetworkPlayer(event.friend.name)

        val placeholder = arrayOf("sender" to sender.getName(), "receiver" to receiver.getName())

        sender.sendMessage(message.createUser().messageForSender(), *placeholder)
    }

    @EventHandler
    fun onRemoveFriend(event: FriendRemoveEvent) {
        val sender = OfflineNetworkPlayer(event.user.name)
        val receiver = OfflineNetworkPlayer(event.friend.name)

        val placeholder = arrayOf("sender" to sender.getName(), "receiver" to receiver.getName())

        sender.sendMessage(message.removeFriend().messageForSender(), *placeholder)
    }

    @EventHandler
    fun onResponseInvitationFriend(event: FriendResponseInvitationEvent) {
        val sender = OfflineNetworkPlayer(event.sender.name)
        val receiver = OfflineNetworkPlayer(event.invited)

        val placeholder = arrayOf("sender" to sender.getName(), "receiver" to receiver.getName())

        when (event.status) {
            InvitationStatus.ACCEPT -> receiver.sendMessage(message.responseInvitation().accept().messageForInvited(), *placeholder)
            InvitationStatus.DECLINE -> {
                sender.sendMessage(message.responseInvitation().decline().messageForSender(), *placeholder)
                receiver.sendMessage(message.responseInvitation().decline().messageForInvited(), *placeholder)
            }
            InvitationStatus.IGNORE -> {
                sender.sendMessage(message.responseInvitation().ignore().messageForSender(), *placeholder)
                receiver.sendMessage(message.responseInvitation().ignore().messageForInvited(), *placeholder)
            }
            InvitationStatus.REMOVE -> {
                sender.sendMessage(message.responseInvitation().removeByUser().messageForSender(), *placeholder)
                receiver.sendMessage(message.responseInvitation().removeByUser().messageForInvited(), *placeholder)
            }
        }
    }

    @EventHandler
    fun onSendInvitationFriend(event: FriendSendInvitationEvent) {
        val sender = OfflineNetworkPlayer(event.sender.name)
        val receiver = OfflineNetworkPlayer(event.receiver)

        sender.sendMessage(message.sendInvitation().messageForSender(), "receiver" to receiver.getName())
        receiver.sendMessage(message.sendInvitation().messageForInvited(), "sender" to sender.getName())
    }
}