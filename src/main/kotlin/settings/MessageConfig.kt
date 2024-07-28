package ru.snapix.snapicooperation.settings

import ru.snapix.library.libs.dazzleconf.annote.ConfDefault.DefaultString
import ru.snapix.library.libs.dazzleconf.annote.ConfDefault.DefaultStrings
import ru.snapix.library.libs.dazzleconf.annote.ConfKey
import ru.snapix.library.libs.dazzleconf.annote.SubSection

interface MessageConfig {
    @SubSection
    fun party(): Party
    interface Party {
        @SubSection
        @ConfKey("disband")
        fun disband(): Disband
        interface Disband {
            @ConfKey("leader-disband-for-leader")
            @DefaultString("Вы удалили группу")
            fun leaderDisbandForLeader(): String

            @ConfKey("leader-disband-for-member")
            @DefaultString("Группа %party_leader% была удалена")
            fun leaderDisbandForMembers(): String
        }

        @SubSection
        @ConfKey("create-user")
        fun createUser(): CreateUser
        interface CreateUser {
            @ConfKey("message-for-members")
            @DefaultString("Игрока %name% был добавлен в группу")
            fun messageForMembers(): String

            @ConfKey("message-for-added-player")
            @DefaultString("Вы вошли в группу %party_leader%")
            fun messageForAddedPlayer(): String
        }

        @SubSection
        @ConfKey("remove-user")
        fun removeUser(): RemoveUser
        interface RemoveUser {
            @ConfKey("message-for-members")
            @DefaultString("Игрока %name% был удален из группу")
            fun messageForMembers(): String

            @ConfKey("message-for-removed-player")
            @DefaultString("Вас удалили из группы %party_leader%")
            fun messageForRemovedPlayer(): String
        }

        @SubSection
        @ConfKey("leave-user")
        fun leave(): LeaveUser
        interface LeaveUser {
            @ConfKey("message-for-members")
            @DefaultString("Игрока %name% вышел из группы")
            fun messageForMembers(): String

            @ConfKey("message-for-removed-player")
            @DefaultString("Вы вышли из группы %party_leader%")
            fun messageForRemovedPlayer(): String
        }

        @SubSection
        @ConfKey("change-leader")
        fun changeLeader(): ChangeLeader
        interface ChangeLeader {
            @ConfKey("message-for-old-leader")
            @DefaultString("Вы отдали лидерство игроку %party_leader%. Ваше имя %name%")
            fun messageForOldLeader(): String

            @ConfKey("message-for-new-leader")
            @DefaultString("Вы получили лидерство от игрока %name%")
            fun messageForNewLeader(): String

            @ConfKey("message-for-members")
            @DefaultString("Лидер %name% передал права %party_leader%")
            fun messageForMembers(): String
        }

        @SubSection
        @ConfKey("send-invitation")
        fun sendInvitation(): SendInvitation
        interface SendInvitation {
            @ConfKey("message-for-sender")
            @DefaultString("Вы отправили приглашение игроку %name%")
            fun messageForSender(): String

            @ConfKey("message-for-invited")
            @DefaultString("Вы получили приглашение от игрока %party_leader%. Сделать кнопки через minimessage!!!")
            fun messageForInvited(): String
        }

        @SubSection
        @ConfKey("response-invitation")
        fun responseInvitation(): ResponseInvitation
        interface ResponseInvitation {
            @SubSection
            fun accept(): Accept
            interface Accept {
                @ConfKey("message-for-invited")
                @DefaultString("Вы приняли приглашение от игрока %party_leader%")
                fun messageForInvited(): String
            }

            @SubSection
            fun decline(): Decline
            interface Decline {
                @ConfKey("message-for-sender")
                @DefaultString("%name% отклонил ваше приглашение в группу %party_leader%")
                fun messageForSender(): String

                @ConfKey("message-for-invited")
                @DefaultString("Вы отклонили приглашение от игрока %party_leader%")
                fun messageForInvited(): String
            }

            @SubSection
            fun ignore(): Ignore
            interface Ignore {
                @ConfKey("message-for-sender")
                @DefaultString("%name% проигнорировал ваше приглашение в группу %party_leader%")
                fun messageForSender(): String

                @ConfKey("message-for-invited")
                @DefaultString("Вы проигнорировали приглашение от игрока %party_leader%")
                fun messageForInvited(): String
            }

            @SubSection
            @ConfKey("remove-leader")
            fun removeLeader(): RemoveLeader
            interface RemoveLeader {
                @ConfKey("message-for-sender")
                @DefaultString("Вы убрали приглашение игроку %name%")
                fun messageForSender(): String

                @ConfKey("message-for-invited")
                @DefaultString("%party_leader% удалил ваше приглашение")
                fun messageForInvited(): String
            }
        }

        @SubSection
        fun commands(): Commands
        interface Commands {
            @DefaultStrings("G", "Help")
            fun help(): List<String>

            @SubSection
            fun disband(): Disband
            interface Disband {
                @ConfKey("not-in-party")
                @DefaultString("Группа не найдена")
                fun notInParty(): String

                @ConfKey("only-leave")
                @DefaultString("Вы не можете удалить группу, так как вы не владелец группы %party_leader%. Вы можете только выйти из группы.")
                fun onlyLeave(): String
            }

            @SubSection
            fun invite(): Invite
            interface Invite {
                @ConfKey("not-leader")
                @DefaultString("Вы не лидер. Лидер - %party_leader%")
                fun notLeader(): String

                @DefaultString("Используйте: /party invite name")
                fun use(): String

                @ConfKey("error-yourself")
                @DefaultString("Вы не можете себя приглашать")
                fun errorYourself(): String

                @ConfKey("not-found")
                @DefaultString("Игрок %name% не найден")
                fun notFound(): String

                @DefaultString("Игрок %name% оффлайн")
                fun offline(): String

                @ConfKey("already-in-you-party")
                @DefaultString("Игрок %name% уже в вашей пати")
                fun alreadyInYouParty(): String

                @ConfKey("already-in-party-leader")
                @DefaultString("Игрок %name% лидер другой группы")
                fun alreadyInPartyLeader(): String

                @ConfKey("already-in-party")
                @DefaultString("Игрок %name% уже в другой группе в качестве участника %party_leader%")
                fun alreadyInParty(): String

                @ConfKey("already-invite")
                @DefaultString("Вы уже пригласили %name% в группу %party_leader%")
                fun alreadyInvite(): String

                @ConfKey("full-party")
                @DefaultString("Группа %party_leader% полная")
                fun fullParty(): String
            }

            @SubSection
            fun accept(): Accept
            interface Accept {
                @ConfKey("already-in-party")
                @DefaultString("Вы уже в группе %party_leader%")
                fun alreadyInParty(): String

                @DefaultString("Используйте: /party accept name")
                fun use(): String

                @ConfKey("error-yourself")
                @DefaultString("Вы не можете принять себя")
                fun errorYourself(): String

                @ConfKey("not-found")
                @DefaultString("Игрок %name% не найден")
                fun notFound(): String

                @DefaultString("Игрок %name% оффлайн")
                fun offline(): String

                @ConfKey("not-found-party")
                @DefaultString("Не найдена группа у игрока %name%")
                fun notFoundParty(): String

                @ConfKey("not-leader")
                @DefaultString("Игрок %name% не лидер. А лидер - %party_leader%")
                fun notLeader(): String

                @ConfKey("not-invite")
                @DefaultString("Игрок %party_leader% не приглашал вас!")
                fun notInvite(): String
            }

            @SubSection
            fun deny(): Deny
            interface Deny {
                @ConfKey("already-in-party")
                @DefaultString("Вы уже в группе %party_leader%")
                fun alreadyInParty(): String

                @DefaultString("Используйте: /party deny name")
                fun use(): String

                @ConfKey("error-yourself")
                @DefaultString("Вы не можете отклонить себя")
                fun errorYourself(): String

                @ConfKey("not-found")
                @DefaultString("Игрок %name% не найден")
                fun notFound(): String

                @DefaultString("Игрок %name% оффлайн")
                fun offline(): String

                @ConfKey("not-found-party")
                @DefaultString("Не найдена группа у игрока %name%")
                fun notFoundParty(): String

                @ConfKey("not-leader")
                @DefaultString("Игрок %name% не лидер. А лидер - %party_leader%")
                fun notLeader(): String

                @ConfKey("not-invite")
                @DefaultString("Игрок %party_leader% не приглашал вас!")
                fun notInvite(): String
            }

            @SubSection
            fun remove(): Remove
            interface Remove {
                @ConfKey("not-party")
                @DefaultString("Не найдена группа")
                fun notParty(): String

                @ConfKey("not-leader")
                @DefaultString("Вы не лидер. А лидер - %party_leader%")
                fun notLeader(): String

                @DefaultString("Используйте: /party remove name")
                fun use(): String

                @ConfKey("error-yourself")
                @DefaultString("Вы не можете удалить себя")
                fun errorYourself(): String

                @ConfKey("not-found")
                @DefaultString("Игрок %name% не найден")
                fun notFound(): String

                @DefaultString("Игрок %name% оффлайн")
                fun offline(): String

                @ConfKey("not-in-party")
                @DefaultString("Игрок %name% не в вашей пати")
                fun notInParty(): String
            }

            @SubSection
            fun chat(): Chat
            interface Chat {
                @ConfKey("not-party")
                @DefaultString("Не найдена группа")
                fun notParty(): String

                @DefaultString("Используйте: /party chat message")
                fun use(): String
            }

            @SubSection
            fun leave(): Leave
            interface Leave {
                @ConfKey("not-party")
                @DefaultString("Не найдена группа")
                fun notParty(): String

                @ConfKey("leader")
                @DefaultString("Вы лидер. Вы не можете выйти")
                fun leader(): String
            }

            @SubSection
            fun lead(): Lead
            interface Lead {
                @ConfKey("not-party")
                @DefaultString("Не найдена группа")
                fun notParty(): String

                @ConfKey("not-leader")
                @DefaultString("Вы не лидер. А лидер - %party_leader%")
                fun notLeader(): String

                @DefaultString("Используйте: /party lead name")
                fun use(): String

                @ConfKey("error-yourself")
                @DefaultString("Вы не можете сделать лидером себя")
                fun errorYourself(): String

                @ConfKey("not-found")
                @DefaultString("Игрок %name% не найден")
                fun notFound(): String

                @DefaultString("Игрок %name% оффлайн")
                fun offline(): String

                @ConfKey("not-in-party")
                @DefaultString("Игрок %name% не в вашей пати")
                fun notInParty(): String
            }
        }
    }

    @SubSection
    fun friend(): Friend
    interface Friend {
        @SubSection
        fun commands(): FriendCommands
        interface FriendCommands {
            @DefaultStrings("G", "Help")
            fun help(): List<String>

            @SubSection
            fun add(): AddFriend
            interface AddFriend {
                @DefaultString("Используйте: /friend add name")
                fun use(): String

                @ConfKey("error-yourself")
                @DefaultString("Вы не можете себя приглашать")
                fun errorYourself(): String

                @ConfKey("not-found")
                @DefaultString("Игрок %name% не найден")
                fun notFound(): String

                @DefaultString("Игрок %name% оффлайн")
                fun offline(): String

                @ConfKey("already-in-you-friend")
                @DefaultString("Игрок %name% уже в ваших друзьях")
                fun alreadyInYouFriend(): String

                @ConfKey("already-invite")
                @DefaultString("Вы уже пригласили %name% в друзья")
                fun alreadyInvite(): String

                @ConfKey("already-invite-by-other")
                @DefaultString("%name% уже вас пригласил в друзья. Ответьте на его приглашение.")
                fun alreadyInviteByOther(): String

                @ConfKey("limit-invite-friend")
                @DefaultString("Вы не можете пригласить игрока, так как вы пригласили больше 4 игроков. Нужно подождать ответ на ваши другие приглашения")
                fun limitInviteFriend(): String

                @ConfKey("full-friend")
                @DefaultString("У вас максимальное количество друзей %max_size%")
                fun fullFriend(): String

                @ConfKey("full-friend-by-other")
                @DefaultString("У игрока %name% уже максимальное количество друзей %max_size%")
                fun fullFriendByOther(): String
            }

            @SubSection
            fun accept(): AcceptFriend
            interface AcceptFriend {
                @DefaultString("Используйте: /friend accept name")
                fun use(): String

                @ConfKey("error-yourself")
                @DefaultString("Вы не можете принять себя")
                fun errorYourself(): String

                @ConfKey("not-found")
                @DefaultString("Игрок %name% не найден")
                fun notFound(): String

                @DefaultString("Игрок %name% оффлайн")
                fun offline(): String

                @ConfKey("already-in-friend")
                @DefaultString("Вы уже в друзьях с %name%")
                fun alreadyInFriend(): String

                @ConfKey("not-invite")
                @DefaultString("Игрок %name% не приглашал вас!")
                fun notInvite(): String
            }

            @SubSection
            fun deny(): DenyFriend
            interface DenyFriend {
                @DefaultString("Используйте: /friend decline name")
                fun use(): String

                @ConfKey("error-yourself")
                @DefaultString("Вы не можете отклонить себя")
                fun errorYourself(): String

                @ConfKey("not-found")
                @DefaultString("Игрок %name% не найден")
                fun notFound(): String

                @DefaultString("Игрок %name% оффлайн")
                fun offline(): String

                @ConfKey("already-in-friend")
                @DefaultString("Вы уже в друзьях с %name%")
                fun alreadyInFriend(): String

                @ConfKey("not-invite")
                @DefaultString("Игрок %name% не приглашал вас!")
                fun notInvite(): String
            }

            @SubSection
            fun remove(): RemoveFriend
            interface RemoveFriend {
                @DefaultString("Используйте: /party remove name")
                fun use(): String

                @ConfKey("error-yourself")
                @DefaultString("Вы не можете удалить себя")
                fun errorYourself(): String

                @ConfKey("not-found")
                @DefaultString("Игрок %name% не найден")
                fun notFound(): String

                @ConfKey("not-in-friend")
                @DefaultString("Игрок %name% не в ваших друзьях")
                fun notInFriend(): String
            }
        }

        @SubSection
        @ConfKey("send-invitation")
        fun sendInvitation(): SendInvitation
        interface SendInvitation {
            @ConfKey("message-for-sender")
            @DefaultString("Вы отправили приглашение игроку %receiver%")
            fun messageForSender(): String

            @ConfKey("message-for-invited")
            @DefaultString("Вы получили приглашение от игрока %sender%. Сделать кнопки через minimessage!!!")
            fun messageForInvited(): String
        }

        @SubSection
        @ConfKey("response-invitation")
        fun responseInvitation(): ResponseInvitation
        interface ResponseInvitation {
            @SubSection
            fun accept(): Accept
            interface Accept {
                @ConfKey("message-for-invited")
                @DefaultString("Вы приняли приглашение от игрока %sender%")
                fun messageForInvited(): String
            }

            @SubSection
            fun decline(): Decline
            interface Decline {
                @ConfKey("message-for-sender")
                @DefaultString("%receiver% отклонил ваше приглашение в группу %sender%")
                fun messageForSender(): String

                @ConfKey("message-for-invited")
                @DefaultString("Вы отклонили приглашение от игрока %sender%")
                fun messageForInvited(): String
            }

            @SubSection
            fun ignore(): Ignore
            interface Ignore {
                @ConfKey("message-for-sender")
                @DefaultString("%receiver% проигнорировал ваше приглашение в группу %sender%")
                fun messageForSender(): String

                @ConfKey("message-for-invited")
                @DefaultString("Вы проигнорировали приглашение от игрока %sender%")
                fun messageForInvited(): String
            }

            @SubSection
            @ConfKey("remove-by-user")
            fun removeByUser(): RemoveLeader
            interface RemoveLeader {
                @ConfKey("message-for-sender")
                @DefaultString("Вы убрали приглашение игроку %receiver%")
                fun messageForSender(): String

                @ConfKey("message-for-invited")
                @DefaultString("%sender% удалил ваше приглашение")
                fun messageForInvited(): String
            }
        }

        @SubSection
        @ConfKey("remove-friend")
        fun removeFriend(): RemoveUser
        interface RemoveUser {
            @ConfKey("message-for-sender")
            @DefaultString("Вы удалили %receiver%")
            fun messageForSender(): String
        }

        @SubSection
        @ConfKey("create-user")
        fun createUser(): CreateUser
        interface CreateUser {
            @ConfKey("message-for-sender")
            @DefaultString("Вы теперь дружите с %receiver%")
            fun messageForSender(): String
        }
    }
}