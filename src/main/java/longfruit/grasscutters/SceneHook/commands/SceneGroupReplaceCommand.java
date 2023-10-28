package longfruit.grasscutters.SceneHook.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.GroupReplacementData;
import longfruit.grasscutters.SceneHook.SceneHook;

import java.util.*;

// Sample: /sgrep 233718106 233718006

@Command(label = "SceneGroupReplace", aliases = "sgrep", permission = "SceneHook.replace",
        usage = "sgrep <replacement scene group#> <replaced group#> [additional replaced group#...]",
        targetRequirement=Command.TargetRequirement.ONLINE)
public final class SceneGroupReplaceCommand implements CommandHandler {

    @Override public void execute(Player sender, Player targetPlayer, List<String> args) {
        var sh = SceneHook.getInstance();

        if (args.size() < 2) {
            if (sender != null) {
                sendUsageMessage(sender);
            } else {
                sh.getLogger().info(getUsageString(sender));
            }
            return;
        }

        int dst;
        var srcs = new ArrayList<Integer>();
        try {
            dst = sh.parseInt(args.get(0), "Invalid replacement scene group ID: ", sender);
            for (int i = 1; i < args.size(); i++) {
                srcs.add(sh.parseInt(args.get(i), "Invalid to-be-replaced scene group ID: ", sender));
            }
        } catch (NumberFormatException e) {
            return;
        }

        sh.addGroupReplacement(dst, srcs);
        targetPlayer.getScene().loadDynamicGroup(dst);

        var msg = "Loaded dynamic group ID %s".formatted(dst);
        if (sender != null) {
            CommandHandler.sendMessage(sender, msg);
        } else {
            sh.getLogger().info(msg);
        }
    }
}
