package ru.snapix.snapicooperation.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.snapix.library.SnapiLibrary
import ru.snapix.library.libs.commands.BaseCommand
import ru.snapix.library.libs.commands.annotation.CatchUnknown
import ru.snapix.library.libs.commands.annotation.CommandAlias
import ru.snapix.library.libs.commands.annotation.Default
import ru.snapix.library.libs.commands.annotation.Subcommand
import ru.snapix.library.network.player.OfflineNetworkPlayer
import ru.snapix.library.utils.message
import ru.snapix.library.utils.translateAlternateColorCodes
import ru.snapix.snapicooperation.PanelStorage
import ru.snapix.snapicooperation.api.User
import ru.snapix.snapicooperation.settings.Settings

@CommandAlias("%friend_command_main")
class FriendCommand : BaseCommand() {
    private val config get() = Settings.message.friend().commands()

    @Default
    fun default(player: Player) {
        PanelStorage.friendMenu(player)
    }

    @CatchUnknown
    @Subcommand("%friend_command_help")
    fun help(sender: CommandSender) {
        sender.sendMessage(config.help().map { translateAlternateColorCodes(it) }.toTypedArray())
    }

    @Subcommand("%friend_command_add")
    fun add(player: Player, args: Array<String>) {
        val user = User[player]

        if (args.isEmpty()) {
            player.message(config.add().use())
            return
        }
        val invited = args[0]

        if (invited.equals(player.name, ignoreCase = true)) {
            player.message(config.add().errorYourself())
            return
        }

        val ntInvited = SnapiLibrary.getPlayer(invited)
        if (!ntInvited.hasPlayedBefore()) {
            player.message(config.add().notFound(), "name" to invited)
            return
        }

        if (!ntInvited.isOnline()) {
            player.message(config.add().offline(), "name" to ntInvited.getName())
            return
        }

        val userInvited = User[ntInvited]

        if (user.friends.contains(ntInvited.getName())) {
            player.message(config.add().alreadyInYouFriend(), "name" to ntInvited.getName())
            return
        }

        if (user.invitations.contains(ntInvited.getName())) {
            player.message(config.add().alreadyInvite(), "name" to ntInvited.getName())
            return
        }

        if (userInvited.invitations.contains(player.name)) {
            player.message(config.add().alreadyInviteByOther(), "name" to ntInvited.getName())
            return
        }

        if (user.friends.size + user.invitations.size >= user.maxSize) {
            player.message(
                config.add().fullFriend(),
                "name" to ntInvited.getName(),
                "size" to user.friends.size + user.invitations.size,
                "max_size" to user.maxSize
            )
            return
        }

        if (userInvited.friends.size + userInvited.invitations.size >= userInvited.maxSize) {
            player.message(
                config.add().fullFriendByOther(),
                "name" to ntInvited.getName(),
                "size" to user.friends.size + user.invitations.size,
                "max_size" to user.maxSize
            )
            return
        }

        user.createInvitation(userInvited.name)
    }

    @Subcommand("%friend_command_accept")
    fun accept(player: Player, args: Array<String>) {
        val user = User[player]

        if (args.isEmpty()) {
            player.message(config.accept().use())
            return
        }
        val inviter = args[0]

        if (inviter.equals(player.name, ignoreCase = true)) {
            player.message(config.accept().errorYourself())
            return
        }

        val ntInviter = SnapiLibrary.getPlayer(inviter)
        if (!ntInviter.hasPlayedBefore()) {
            player.message(config.accept().notFound(), "name" to inviter)
            return
        }

        if (!ntInviter.isOnline()) {
            player.message(config.accept().offline(), "name" to ntInviter.getName())
            return
        }

        val userInviter = User[ntInviter]

        if (user.friends.contains(userInviter.name)) {
            player.message(config.accept().alreadyInFriend(), "name" to userInviter.name)
            return
        }

        userInviter.accept(player)
    }

    @Subcommand("%friend_command_decline")
    fun deny(player: Player, args: Array<String>) {
        if (args.isEmpty()) {
            player.message(config.deny().use())
            return
        }
        val inviter = args[0]

        if (inviter.equals(player.name, ignoreCase = true)) {
            player.message(config.deny().errorYourself())
            return
        }

        val ntInviter = SnapiLibrary.getPlayer(inviter)
        if (!ntInviter.hasPlayedBefore()) {
            player.message(config.deny().notFound(), "name" to inviter)
            return
        }

        if (!ntInviter.isOnline()) {
            player.message(config.deny().offline(), "name" to ntInviter.getName())
            return
        }

        val userInviter = User[ntInviter]

        if (userInviter.friends.contains(player.name)) {
            player.message(config.deny().alreadyInFriend(), "name" to ntInviter.getName())
            return
        }

        userInviter.decline(player)
    }

    @Subcommand("%friend_command_remove")
    fun remove(player: Player, args: Array<String>) {
        val user = User[player]

        if (args.isEmpty()) {
            player.message(config.remove().use())
            return
        }
        val removed = args[0]

        if (removed.equals(player.name, ignoreCase = true)) {
            player.message(config.remove().errorYourself())
            return
        }

        val ntRemoved = OfflineNetworkPlayer(removed)
        if (!ntRemoved.hasPlayedBefore()) {
            player.message(config.remove().notFound(), "name" to removed)
            return
        }

        val name = ntRemoved.getName()
        val userRemoved = User[name]

        if (!user.friends.contains(name)) {
            player.message(config.remove().notInFriend(), "name" to name)
            return
        }

        user.removeFriend(userRemoved)
    }
}