package longfruit.grasscutters.SceneHook.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.GroupReplacementData;
import longfruit.grasscutters.SceneHook.SceneHook;

import java.util.*;

// Sample: /shook 33718

@Command(label = "SceneHook", aliases = "shook", permission = "SceneHook.hook",
        usage = "shook <scene#>",
        targetRequirement=Command.TargetRequirement.NONE)
public final class SceneHookCommand implements CommandHandler {

    @Override public void execute(Player sender, Player targetPlayer, List<String> args) {
        var sh = SceneHook.getInstance();

        if (args.size() < 1) {
            if (sender != null) {
                sendUsageMessage(sender);
            } else {
                sh.getLogger().info(getUsageString(sender));
            }
            return;
        }

        int dst;
        try {
            dst = sh.parseInt(args.get(0), "Invalid scene ID: ", sender);
        } catch (NumberFormatException e) {
            return;
        }

        sh.addSceneReplacement(dst, sender);

        var msg = "Hooked to scene ID %s".formatted(dst);
        if (sender != null) {
            CommandHandler.sendMessage(sender, msg);
        } else {
            sh.getLogger().info(msg);
        }
    }
}
