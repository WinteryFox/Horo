package bot.horo.command

import bot.horo.Database
import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import org.jetbrains.annotations.PropertyKey
import java.util.*

internal val commands = mutableListOf<Command>()
fun MutableList<Command>.traverse(input: String): Command? {
    var sequence = input.splitToSequence(" ")
    var command = this.find { it.name == sequence.first() || it.aliases.contains(sequence.first()) } ?: return null
    sequence = sequence.drop(1)

    for (seq in sequence) {
        command = command.children.find { it.name == seq || it.aliases.contains(seq) } ?: return null
    }

    return command
}

class Command(
    val name: String,
    val aliases: Set<String>,
    val dispatch: suspend CommandContext.() -> Unit,
    val parameters: MutableList<String> = mutableListOf(),
    val botPermissions: PermissionSet = PermissionSet.of(Permission.SEND_MESSAGES),
    val userPermissions: PermissionSet = PermissionSet.of(Permission.SEND_MESSAGES),
    val children: MutableList<Command> = mutableListOf()
) {
    var parent: Command? = null

    init {
        children.forEach {
            it.parent = this
        }
    }
}

class CommandBuilder(private val name: String) {
    private val aliases = mutableSetOf<String>()
    private lateinit var dispatch: suspend CommandContext.() -> Unit
    private val parameters = mutableListOf<String>()
    private val children = mutableListOf<Command>()
    private val botPermissions = PermissionSet.none()
    private val userPermissions = PermissionSet.none()

    fun alias(alias: String) {
        this.aliases.add(alias)
    }

    fun dispatch(dispatch: suspend CommandContext.() -> Unit) {
        this.dispatch = dispatch
    }

    fun parameter(name: String): Parameter {
        this.parameters.add(name)
        return Parameter(name)
    }

    fun botPermissions(vararg permission: Permission) {
        this.botPermissions.addAll(permission)
    }

    fun userPermission(vararg permission: Permission) {
        this.userPermissions.addAll(permission)
    }

    fun subcommand(name: String, dsl: CommandBuilder.() -> Unit) = children.add(CommandBuilder(name).apply(dsl).build())

    fun build() = Command(name, aliases, dispatch, parameters, botPermissions, userPermissions, children)
}

object CommandsBuilder {
    fun command(name: String, dsl: CommandBuilder.() -> Unit) {
        commands.add(CommandBuilder(name).apply(dsl).build())
    }
}

data class Parameter(val name: String)

data class CommandContext(
    val event: MessageCreateEvent,
    val parameters: Map<String, String>,
    val database: Database
) {
    fun Parameter.get(): String {
        return parameters.getValue(name)
    }

    suspend fun translate(@PropertyKey(resourceBundle = "localization") key: String, member: Member): String =
        ResourceBundle.getBundle(
            "localization",
            Locale(
                database.query(
                    """
SELECT CASE
           WHEN (SELECT locale FROM guilds WHERE snowflake = $1) IS NULL THEN
                   (SELECT locale FROM users WHERE snowflake = $2)
           ELSE
                   (SELECT locale FROM guilds WHERE snowflake = $1)
           END
                    """,
                    mapOf(Pair("$1", member.guildId.asLong()), Pair("$2", member.id.asLong()))
                ) { row, _ ->
                    row["locale"] as String
                }.firstOrNull() ?: "en_GB"
            )
        ).getString(key)
}

fun registerCommands(builder: CommandsBuilder.() -> Unit) {
    CommandsBuilder.apply(builder)
}