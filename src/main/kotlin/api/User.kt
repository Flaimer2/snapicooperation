package ru.snapix.snapicooperation.api

import com.velocitypowered.api.proxy.Player
import kotlinx.serialization.Serializable
import net.luckperms.api.LuckPermsProvider
import ru.snapix.library.callEvent
import ru.snapix.library.message
import ru.snapix.library.runLaterTask
import ru.snapix.snapicooperation.api.events.friend.*
import ru.snapix.snapicooperation.caches.Friends
import ru.snapix.snapicooperation.database.FriendDatabase
import ru.snapix.snapicooperation.plugin
import ru.snapix.snapicooperation.settings.Settings
import kotlin.time.Duration.Companion.seconds

@Serializable
class User(val name: String, val friends: MutableList<String>, val invitations: MutableSet<String>) {
    val maxSize: Int get() {
        val api = LuckPermsProvider.get()
        val user = api.userManager.getUser(name) ?: return Settings.config.defaultMaxSize()
        return user.cachedData.permissionData.permissionMap
            .filter { it.key.startsWith("friend.maxsize") && it.value }.keys
            .mapNotNull { it.removePrefix("friend.maxsize").toIntOrNull() }.maxOrNull() ?: Settings.config.defaultMaxSize()
    }

    constructor(name: String) : this(name, mutableListOf(), mutableSetOf())
    companion object {
        @JvmStatic
        fun create(player: Player): User {
            val user = get(player) ?: User(player.username)

            FriendDatabase.update(user)
            Friends.update(user)

            return user
        }

        @JvmStatic
        operator fun get(name: String): User? {
            return Friends[name]
        }

        @JvmStatic
        operator fun get(player: Player): User? {
            return get(player.username)
        }
    }

    fun addFriend(friend: String): User {
        val user = get(friend) ?: User(friend)
        invitations.remove(friend)

        friends.add(friend)
        user.friends.add(name)

        Friends.update(this)
        Friends.update(user)

        FriendDatabase.update(this)
        FriendDatabase.update(user)

        plugin.callEvent(FriendAddEvent(this, user))

        return user
    }

    fun removeFriend(friend: User) {
        friends.remove(friend.name)
        friend.friends.remove(name)

        Friends.update(this)
        Friends.update(friend)

        FriendDatabase.update(this)
        FriendDatabase.update(this)

        plugin.callEvent(FriendRemoveEvent(this, friend))
    }

    fun createInvitation(player: Player) {
        if (invitations.add(player.username)) {
            Friends.update(this)
            plugin.callEvent(FriendSendInvitationEvent(player, this))
            plugin.server.runLaterTask(Settings.config.invitationDelay().seconds) {
                if (!invitations.contains(player.username)) return@runLaterTask
                invitations.remove(player.username)
                Friends.update(this)
                plugin.callEvent(FriendResponseInvitationEvent(player, this, InvitationStatus.IGNORE))
            }
        }
    }

    fun removeInvitation(player: Player) {
        invitations.remove(player.username)
        Friends.update(this)
        plugin.callEvent(FriendResponseInvitationEvent(player, this, InvitationStatus.REMOVE))
    }

    fun accept(player: Player) {
        if (!invitations.contains(player.username)) {
            player.message(Settings.message.friend().commands().accept().notInvite(), "name" to name)
            return
        }
        addFriend(player.username)
        plugin.callEvent(FriendResponseInvitationEvent(player, this, InvitationStatus.ACCEPT))
    }

    fun decline(player: Player) {
        if (!invitations.contains(player.username)) {
            player.message(Settings.message.friend().commands().deny().notInvite(), "name" to name)
            return
        }
        invitations.remove(player.username)
        Friends.update(this)
        plugin.callEvent(FriendResponseInvitationEvent(player, this, InvitationStatus.DECLINE))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is User) return false
        return other.name == name
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + friends.hashCode()
        result = 31 * result + invitations.hashCode()
        return result
    }
}