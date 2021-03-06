package bot.horo.command

import discord4j.rest.util.Color
import kotlinx.coroutines.reactive.awaitSingle

fun CommandsBuilder.help() {
    command("help") {
        dispatch {
            val content = localization.translate("cant-see-message", event.member.get())
            val title = localization.translate("command-list", event.member.get())

            event.message.channel.awaitSingle()
                .createMessage {
                    it.setContent(content)
                        .setEmbed { embed ->
                            embed.setTitle(title)
                                .setDescription(commands.joinToString(separator = "\n") { command -> ".horo${command.name}" })
                                .setColor(Color.PINK)
                        }
                }
                .awaitSingle()
        }
    }
}
