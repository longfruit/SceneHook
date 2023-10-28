package longfruit.grasscutters.SceneHook;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.GroupReplacementData;
import emu.grasscutter.plugin.Plugin;
import emu.grasscutter.server.event.EventHandler;
import emu.grasscutter.server.event.HandlerPriority;
import emu.grasscutter.server.event.game.SceneBlockLoadedEvent;
import emu.grasscutter.server.event.game.SceneMetaLoadEvent;
import emu.grasscutter.utils.JsonUtils;

import longfruit.grasscutters.SceneHook.commands.*;
import longfruit.grasscutters.SceneHook.objects.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public final class SceneHook extends Plugin {
    /* Turn the plugin into a singleton. */
    private static SceneHook instance;

    private List<Integer> sceneHooks;
    private Map<Integer, BlockReplacement> blockReplacements;

    public void addSceneHook(int dst) {
        sceneHooks.add(dst);
    }

    public boolean hasSceneHook(int dst) {
        return sceneHooks.contains(dst);
    }

    public int getSceneHook(int dst) {
        return sceneHooks.get(dst);
    }

    public BlockReplacement getBlockReplacement(int sceneId) {
        return blockReplacements.get(sceneId);
    }

    public void addSceneReplacement(int sceneId, Player sender) {
        File config = new File(this.getDataFolder(), "Scene/%s/replacement.json".formatted(sceneId));
        if (!config.exists()) {
            sendErrorMessage(sender, "Replacement for scene %s not found in plugin data folder.".formatted(sceneId));
        }
        try {
            var blockReplacement = JsonUtils.loadToClass(new FileReader(config), BlockReplacement.class);
            blockReplacement.replacements.forEach(r -> {
                addGroupReplacement(r.newSceneGroup, Stream.of(r.replacedSceneGroup).toList());
            });
            blockReplacements.put(sceneId, blockReplacement);
        } catch (IOException e) {
            e.printStackTrace();
            sendErrorMessage(sender, "Failed to load replacement for scene %s.".formatted(sceneId));
        }
        addSceneHook(sceneId);
        this.getLogger().info("addSceneReplacement: sceneId {}", sceneId);
    }

    public void addGroupReplacement(int dst, List<Integer> srcs) {
        GameData.getGroupReplacements()
                .computeIfAbsent(dst, k -> new GroupReplacementData(k, new ArrayList<Integer>()))
                .replace_groups.addAll(srcs);
        this.getLogger().info("addGroupReplacement: group {} replaces {}", dst, srcs);
    }

    public int parseInt(String s, String errorPrefix, Player sender) throws NumberFormatException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            sendErrorMessage(sender, "%s%s".formatted(errorPrefix, s));
            throw e;
        }
    }

    public void sendErrorMessage(Player sender, String msg) {
        if (sender != null) {
            CommandHandler.sendMessage(sender, msg);
        } else {
            this.getLogger().error(msg);
        }
    }

    /**
     * Gets the plugin instance.
     * @return A plugin singleton.
     */
    public static SceneHook getInstance() {
        return instance;
    }

    /**
     * This method is called immediately after the plugin is first loaded into system memory.
     */
    @Override public void onLoad() {
        // Set the plugin instance.
        instance = this;

        sceneHooks = new ArrayList<>();
        blockReplacements = new HashMap<>();

        // Log a plugin status message.
        this.getLogger().info("SceneHook plugin has been loaded.");
    }

    /**
     * This method is called before the servers are started, or when the plugin enables.
     */
    @Override public void onEnable() {
        // Register event listeners.
        new EventHandler<>(SceneBlockLoadedEvent.class)
                .priority(HandlerPriority.NORMAL)
                .listener(EventListeners::onSceneBlockLoad)
                .register(this);
        new EventHandler<>(SceneMetaLoadEvent.class)
                .priority(HandlerPriority.NORMAL)
                .listener(EventListeners::onSceneMetaLoad)
                .register(this);

        // Register commands.
        this.getHandle().registerCommand(new SceneGroupReplaceCommand());
        this.getHandle().registerCommand(new SceneHookCommand());

        // Enable requested scene hooks on plugin enable.
        File config = new File(this.getDataFolder(), "onEnable.json");
        if (config.exists()) {
            try {
                var onEnable = JsonUtils.loadToClass(new FileReader(config), OnEnableHook.class);
                onEnable.hookScenesOnPluginEnable.forEach(id -> {
                    addSceneReplacement(id, null);
                    this.getLogger().info("Hooked scene {}.", id);
                });
            } catch (IOException e) {
                e.printStackTrace();
                this.getLogger().error("Could not read onEnable.json.");
            }
        }

        // Log a plugin status message.
        this.getLogger().info("SceneHook plugin has been enabled.");
    }

    /**
     * This method is called when the plugin is disabled.
     */
    @Override public void onDisable() {
        // Log a plugin status message.
        this.getLogger().info("SceneHook plugin has been disabled.");
    }
}
