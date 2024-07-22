package ru.snapix.snapicooperation.commands

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import ru.snapix.library.NetworkPlayer
import ru.snapix.library.libs.commands.BaseCommand
import ru.snapix.library.libs.commands.annotation.CatchUnknown
import ru.snapix.library.libs.commands.annotation.CommandAlias
import ru.snapix.library.libs.commands.annotation.Default
import ru.snapix.library.libs.commands.annotation.Subcommand
import ru.snapix.library.message
import ru.snapix.library.toPlayer
import ru.snapix.snapicooperation.PanelStorage
import ru.snapix.snapicooperation.settings.Settings
import ru.snapix.snapicooperation.user

@CommandAlias("%friend_command_main")
class FriendCommand : BaseCommand() {
    private val config get() = Settings.message.friend().commands()

    @Default
    fun default(player: Player) {
        PanelStorage.friendMenu(player)
    }

    @CatchUnknown
    @Subcommand("%friend_command_help")
    fun help(sender: CommandSource) {
        sender.message(config.help())
    }

    @Subcommand("%friend_command_add")
    fun add(player: Player, args: Array<String>) {
        val user = player.user

        if (args.isEmpty()) {
            player.message(config.add().use())
            return
        }
        val invited = args[0]

        if (invited.equals(player.username, ignoreCase = true)) {
            player.message(config.add().errorYourself())
            return
        }

        val ntInvited = NetworkPlayer(invited)
        if (!ntInvited.isExist()) {
            player.message(config.add().notFound(), "name" to invited)
            return
        }

        val playerInvited = ntInvited.toPlayer()
        if (playerInvited == null) {
            player.message(config.add().offline(), "name" to ntInvited.name())
            return
        }

        val userInvited = playerInvited.user

        if (user.friends.contains(playerInvited.username)) {
            player.message(config.add().alreadyInYouFriend(), "name" to playerInvited.username)
            return
        }

        if (user.invitations.contains(playerInvited.username)) {
            player.message(config.add().alreadyInvite(), "name" to playerInvited.username)
            return
        }

        if (userInvited.invitations.contains(player.username)) {
            player.message(config.add().alreadyInviteByOther(), "name" to playerInvited.username)
            return
        }

        if (user.friends.size + user.invitations.size >= user.maxSize) {
            player.message(config.add().fullFriend(), "name" to playerInvited.username, "max_size" to user.maxSize)
            return
        }

        if (userInvited.friends.size + userInvited.invitations.size >= userInvited.maxSize) {
            player.message(config.add().fullFriendByOther(), "name" to playerInvited.username, "max_size" to userInvited.maxSize)
            return
        }

        user.createInvitation(playerInvited)
    }

    @Subcommand("%friend_command_accept")
    fun accept(player: Player, args: Array<String>) {
        val user = player.user

        if (args.isEmpty()) {
            player.message(config.accept().use())
            return
        }
        val inviter = args[0]

        if (inviter.equals(player.username, ignoreCase = true)) {
            player.message(config.accept().errorYourself())
            return
        }

        val ntInviter = NetworkPlayer(inviter)
        if (!ntInviter.isExist()) {
            player.message(config.accept().notFound(), "name" to inviter)
            return
        }

        val playerInviter = ntInviter.toPlayer()
        if (playerInviter == null) {
            player.message(config.accept().offline(), "name" to ntInviter.name())
            return
        }

        val userInviter = playerInviter.user

        if (user.friends.contains(playerInviter.username)) {
            player.message(config.accept().alreadyInFriend(), "name" to playerInviter.username)
            return
        }

        userInviter.accept(player)
    }

    @Subcommand("%friend_command_deny")
    fun deny(player: Player, args: Array<String>) {
        if (args.isEmpty()) {
            player.message(config.deny().use())
            return
        }
        val inviter = args[0]

        if (inviter.equals(player.username, ignoreCase = true)) {
            player.message(config.deny().errorYourself())
            return
        }

        val ntInviter = NetworkPlayer(inviter)
        if (!ntInviter.isExist()) {
            player.message(config.deny().notFound(), "name" to inviter)
            return
        }

        val playerInviter = ntInviter.toPlayer()
        if (playerInviter == null) {
            player.message(config.deny().offline(), "name" to ntInviter.name())
            return
        }

        val userInviter = playerInviter.user

        if (userInviter.friends.contains(player.username)) {
            player.message(config.deny().alreadyInFriend(), "name" to playerInviter.username)
            return
        }

        userInviter.decline(player)
    }

    @Subcommand("%friend_command_remove")
    fun remove(player: Player, args: Array<String>) {
        val user = player.user

        if (args.isEmpty()) {
            player.message(config.remove().use())
            return
        }
        val removed = args[0]

        if (removed.equals(player.username, ignoreCase = true)) {
            player.message(config.remove().errorYourself())
            return
        }

        val ntRemoved = NetworkPlayer(removed)
        if (!ntRemoved.isExist()) {
            player.message(config.remove().notFound(), "name" to removed)
            return
        }

        val name = ntRemoved.name()

        val userRemoved = name.user

        if (user.friends.contains(name)) {
            player.message(config.remove().notInFriend(), "name" to name)
            return
        }

        user.removeFriend(userRemoved)
    }
}