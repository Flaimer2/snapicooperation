package ru.snapix.snapicooperation.api

import kotlinx.serialization.Serializable
import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player
import ru.snapix.library.bukkit.utils.callEvent
import ru.snapix.library.network.player.NetworkPlayer
import ru.snapix.library.utils.message
import ru.snapix.snapicooperation.api.events.friend.*
import ru.snapix.snapicooperation.caches.Friends
import ru.snapix.snapicooperation.database.FriendDatabase
import ru.snapix.snapicooperation.plugin
import ru.snapix.snapicooperation.settings.Settings
import kotlin.time.Duration.Companion.seconds

@Serializable
class User(val name: String, val friends: MutableList<String>, val invitations: MutableSet<String>) {
    val maxSize: Int
        get() {
            val api = LuckPermsProvider.get()
            val user = api.userManager.getUser(name) ?: return Settings.config.defaultMaxSize()
            return user.cachedData.permissionData.permissionMap
                .filter { it.key.startsWith("friend.maxsize") && it.value }.keys
                .mapNotNull { it.removePrefix("friend.maxsize").toIntOrNull() }.maxOrNull()
                ?: Settings.config.defaultMaxSize()
        }

    constructor(name: String) : this(name, mutableListOf(), mutableSetOf())

    companion object {
        @JvmStatic
        fun create(name: String): User {
            val user = Friends[name] ?: User(name)

            FriendDatabase.update(user)
            Friends.update(user)

            return user
        }

        @JvmStatic
        operator fun get(name: String): User {
            return Friends[name] ?: create(name)
        }

        @JvmStatic
        operator fun get(player: Player): User {
            return get(player.name)
        }

        @JvmStatic
        operator fun get(networkPlayer: NetworkPlayer): User {
            return get(networkPlayer.getName())
        }
    }

    fun addFriend(friend: String): User {
        val user = get(friend)
        invitations.remove(friend)

        friends.add(friend)
        user.friends.add(name)

        Friends.update(this)
        Friends.update(user)

        FriendDatabase.update(this)
        FriendDatabase.update(user)

        callEvent(FriendAddEvent(this, user))

        return user
    }

    fun removeFriend(friend: User) {
        friends.remove(friend.name)
        FriendDatabase.update(this)
        Friends.update(this)

        friend.friends.remove(name)
        FriendDatabase.update(friend)
        Friends.update(friend)

        callEvent(FriendRemoveEvent(this, friend))
    }

    fun createInvitation(friend: String) {
        if (invitations.add(friend)) {
            Friends.update(this)
            callEvent(FriendSendInvitationEvent(this, friend))
            plugin.server.scheduler.runTaskLater(plugin, {
                val user = User[name]
                if (user.invitations.contains(friend)) {
                    user.invitations.remove(friend)
                    Friends.update(user)
                    callEvent(FriendResponseInvitationEvent(friend, user, InvitationStatus.IGNORE))
                }
            }, Settings.config.invitationDelay().seconds.inWholeSeconds * 20)
        }
    }

    fun removeInvitation(friend: String) {
        invitations.remove(friend)
        Friends.update(this)
        callEvent(FriendResponseInvitationEvent(friend, this, InvitationStatus.REMOVE))
    }

    fun accept(player: Player) {
        if (!invitations.contains(player.name)) {
            player.message(Settings.message.friend().commands().accept().notInvite(), "name" to name)
            return
        }
        addFriend(player.name)
        callEvent(FriendResponseInvitationEvent(player.name, this, InvitationStatus.ACCEPT))
    }

    fun decline(player: Player) {
        if (!invitations.contains(player.name)) {
            player.message(Settings.message.friend().commands().deny().notInvite(), "name" to name)
            return
        }
        invitations.remove(player.name)
        Friends.update(this)
        callEvent(FriendResponseInvitationEvent(player.name, this, InvitationStatus.DECLINE))
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