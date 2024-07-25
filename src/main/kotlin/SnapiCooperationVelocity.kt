package ru.snapix.snapicooperation

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import ru.snapix.library.bukkit.SnapiLibraryBukkit
import ru.snapix.library.network.ServerType
import ru.snapix.library.network.player.OfflineNetworkPlayer
import ru.snapix.library.network.player.OnlineNetworkPlayer
import ru.snapix.library.velocity.VelocityPlugin
import ru.snapix.snapicooperation.caches.Parties
import ru.snapix.snapicooperation.commands.Commands
import ru.snapix.snapicooperation.database.FriendDatabase
import ru.snapix.snapicooperation.listeners.ChatListener
import ru.snapix.snapicooperation.listeners.ConnectionListener
import ru.snapix.snapicooperation.listeners.PartyListener
import java.nio.file.Path

@Plugin(
    id = "snapicooperation",
    name = "SnapiCooperation",
    authors = ["SnapiX"],
    version = "1.0.0",
    dependencies = [
        Dependency(id = "snapilibrary")
    ]
)
class SnapiCooperationVelocity @Inject constructor(server: ProxyServer, logger: Logger, @DataDirectory dataDirectory: Path) :
    VelocityPlugin() {
    init {
        init(server, logger, dataDirectory)
    }

    @Subscribe
    fun onEnable(event: ProxyInitializeEvent) {
        server.eventManager.register(this, VelocityListener())
    }

    fun onDisable(event: ProxyShutdownEvent) {
        Parties.values().forEach { Parties.remove(it) }
    }
}

class VelocityListener {
    @Subscribe
    fun onServerConnected(event: ServerConnectedEvent) {
        val player = OnlineNetworkPlayer(event.player.username)
        val party = Parties.values().find { it.inParty(player) }

        if (party == null || !party.isLeader(player)) return

        party.players.mapNotNull { it.getProxyPlayer() }.forEach {
            it.createConnectionRequest(event.server).connect()
            println("Work! ${event.server} $it")
        }
    }
}