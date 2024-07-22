package ru.snapix.snapicooperation

import com.velocitypowered.api.proxy.Player
import dev.simplix.protocolize.api.ClickType
import ru.snapix.library.menu.Item
import ru.snapix.library.menu.dsl.Material
import ru.snapix.library.menu.dsl.generatorPanel
import ru.snapix.library.menu.dsl.panel
import ru.snapix.library.menu.nextPage
import ru.snapix.library.menu.prevPage
import ru.snapix.library.toDate
import ru.snapix.snapicooperation.api.CooperationApi
import ru.snapix.snapicooperation.api.Party
import ru.snapix.snapicooperation.api.User
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds

object PanelStorage {
    fun partyMenu(player: Player) {
        if (Party[player] == null) {
            nullPartyMenu(player)
        } else {
            defaultPartyMenu(player)
        }
    }

    fun nullPartyMenu(player: Player) {
        panel(player) {
            title = "Создать группу"
            layout {
                - "         "
                - "         "
                - "    G    "
                - "         "
                - "         "
                - "  O   I  "
            }
            items {
                'G' {
                    name = "&aСоздать группу"
                    head = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBiNTVmNzQ2ODFjNjgyODNhMWMxY2U1MWYxYzgzYjUyZTI5NzFjOTFlZTM0ZWZjYjU5OGRmMzk5MGE3ZTcifX19"
                    lore {
                        - "&fСоздайте новую группу для"
                        - "&fсовместной игры и общения!"
                        - ""
                        - "&fЧтобы вступать в другие группы,"
                        - "&fвам нужно будет удалить эту"
                        - ""
                        - "&aНажмите, чтобы создать группу"
                    }
                    actions {
                        Party.create(player)
                        defaultPartyMenu(player)
                    }
                }
                'O' {
                    name = "&aОнлайн"
                    material = Material.PAPER
                    lore {
                        - "&fВы можете пригласить любого"
                        - "&fигрока, для игры вместе!"
                        - ""
                        - "&fОнлайн на сервере: &aОНЛАЙН"
                        - "&fОнлайн друзей: &aОНЛАЙН"
                    }
                }
                'I' {
                    name = "&aО группах"
                    material = Material.REDSTONE
                    lore {
                        - "&fВы можете "
                        - "&fигрока, для игры вместе!"
                        - ""
                        - "&fОнлайн на сервере: &aОНЛАЙН"
                        - "&fОнлайн друзей: &aОНЛАЙН"
                    }
                }
            }
        }
    }

    fun defaultPartyMenu(player: Player) {
        generatorPanel<Player?>(player) {
            title = "Группа"
            update = 1.seconds

            generatorSource {
                val list: MutableList<Player?> = mutableListOf(player.party.leader.toPlayer(), *player.party.players.mapNotNull { it.toPlayer() }.toTypedArray(), *player.party.invitations.mapNotNull { it.toPlayer() }.toTypedArray())
                val need = player.party.maxSize - list.size
                if (need != 0) {
                    repeat(need) { list.add(null) }
                }
                list
            }
            generatorOutput = {
                if (it != null) {
                    if (player.party.isLeader(it)) {
                        Item(
                            name = "&a${it.username}",
                            head = it.username,
                            lore = listOf(
                                "&fЛидер группы, который",
                                "&fможет распускать группу",
                                "&fи приглашать участников",
                                "",
                                "&fСейчас находится: &a${it.currentServer.getOrNull()?.server?.serverInfo?.name ?: "Где-то в переходе..."}",
                                "&fВремя создания: &a${player.party.date?.toDate("dd/MM/yyyy HH:mm") ?: "–"}",
                                "",
                                "&aНажмите, чтобы открыть профиль"
                            ),
                            clickAction = {
                                TODO("Make open profile")
                            }
                        )
                    } else if (player.party.inParty(it)) {
                        Item(
                            name = "&a${it.username} (todo: make is friend)",
                            head = it.username,
                            lore = listOf(
                                "",
                                "&fСейчас находится: &a${it.currentServer.getOrNull()?.server?.serverInfo?.name ?: "Где-то в переходе..."}",
                                "",
                                "&a${if (player.party.isLeader(player)) "&aНажмите ЛКМ, чтобы открыть профиль\n&aНажмите СКМ, чтобы назначить лидером группы\n&aНажмите ПКМ, чтобы исключить участника" else "&aНажмите, чтобы открыть профиль"}"
                            ),
                            clickAction = {
                                if (player.party.isLeader(player)) {
                                    if (type == ClickType.LEFT_CLICK || type == ClickType.SHIFT_LEFT_CLICK) {
                                        TODO("Make open profile")
                                    }
                                    if (type == ClickType.CREATIVE_MIDDLE_CLICK) {
                                        player.party.changeLeader(it)
                                    }
                                    if (type == ClickType.RIGHT_CLICK || type == ClickType.SHIFT_RIGHT_CLICK) {
                                        player.party.removePlayer(it)
                                    }
                                } else {
                                    TODO("Make open profile")
                                }
                            }
                        )
                    } else {
                        Item(
                            name = "&a${it.username} Приглашен",
                            head = it.username,
                            clickAction = {
                                player.party.removeInvitation(player)
                            }
                        )
                    }
                } else {
                    Item(
                        name = "&aНет игрока",
                        material = Material.RED_STAINED_GLASS_PANE,
                        clickAction = {
                            playerListPartyMenu(player)
                        }
                    )
                }
            }

            layout {
                -"FFFFFFFFF"
                -"FFFF FFFF"
                -"FF F F FF"
                -"FFFFFFFFF"
                -"FFFFDFFFF"
            }

            items {
                'F' {
                    material = Material.AIR
                }
            }

            replacements {
                -("leader" to { player.party.leader })
                -("party_size" to { player.party.size })
                -("party_max_size" to { player.party.maxSize })
            }
        }
    }

    fun playerListPartyMenu(player: Player) {
        generatorPanel<Player>(player) {
            title = "Группа"
            update = 1.seconds

            // TODO: Make sort by friend
            generatorSource { CooperationApi.playerWithoutParty().toList() }
            generatorOutput = {
                Item(
                    name = "&a${it.username}",
                    head = it.username,
                )
            }

            layout {
                -"FFFFFFFFF"
                -"F       F"
                -"F       F"
                -"F       F"
                -"FFFFRFFFF"
            }

            items {
                'F' {
                    material = Material.AIR
                }
                'R' {
                    material = Material.ARROW
                    actions {
                        defaultPartyMenu(player)
                    }
                }
            }
        }
    }

    fun friendMenu(player: Player) {
        generatorPanel<User>(player) {
            title = "Друзья"

            layout {
                - "#########"
                - "#       #"
                - "#       #"
                - "#       #"
                - "P# ### #N"
            }

            generatorSource { player.user.friends.map { it.user } }
            generatorOutput = {
                Item(
                    name = "&a${it.name}",
                    head = it.name,
                )
            }
            comparator = compareBy<User> { it.name.toPlayer() != null }.thenBy { it.name }

            items {
                '#' {
                    material = Material.AIR
                }
                'P' {
                    name = "Предыдущая"
                    material = Material.ARROW
                    condition { prevPage() }
                    actions {
                        prevPage()
                    }
                }
                'N' {
                    name = "Следующая"
                    material = Material.ARROW
                    condition { nextPage() }
                    actions {
                        nextPage()
                    }
                }
            }
        }
    }

    val Player.party: Party
        get() {
            val party = Party[this]

            if (party == null) {
                nullPartyMenu(this)
                return null!!
            }

            return party
        }
}