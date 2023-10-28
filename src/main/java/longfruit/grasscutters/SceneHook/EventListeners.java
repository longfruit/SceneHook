package longfruit.grasscutters.SceneHook;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Position;
import emu.grasscutter.server.event.EventHandler;
import emu.grasscutter.server.event.HandlerPriority;
import emu.grasscutter.server.event.game.SceneBlockLoadedEvent;
import emu.grasscutter.server.event.game.SceneMetaLoadEvent;
import emu.grasscutter.scripts.data.*;

import java.util.*;

public final class EventListeners {
    public static void onSceneBlockLoad(SceneBlockLoadedEvent event) {
        var sh = SceneHook.getInstance();

        var block = event.block;
        var sceneId = block.sceneId;

        if (sh.hasSceneHook(sceneId)) {
            var groupsToAdd = new ArrayList<SceneGroup>();

            // Set dynamic replacement attributes
            var blockReplacement = sh.getBlockReplacement(sceneId);
            block.groups.values().forEach(g -> {
                for (var replacement : blockReplacement.replacements) {
                    if (g.id == replacement.replacedSceneGroup) {
                        g.dynamic_load = true;
                        g.is_replaceable = new SceneReplaceable();
                        g.is_replaceable.value = true;
                        g.is_replaceable.version = 0;
                        g.is_replaceable.new_bin_only = true;
                        sh.getLogger().info("onSceneBlockLoad: Modified scene {} group {}", sceneId, g.id);

                        // Add replacement group for block to load.
                        // TODO Maybe copy from g?
                        var newGroup = new SceneGroup();
                        newGroup.block_id = block.id;
                        newGroup.id = replacement.newSceneGroup;
                        newGroup.refresh_id = 1;
                        newGroup.pos = new Position(g.pos);
                        newGroup.dynamic_load = true;
                        newGroup.is_replaceable = new SceneReplaceable();
                        newGroup.is_replaceable.value = true;
                        newGroup.is_replaceable.version = 0;
                        newGroup.is_replaceable.new_bin_only = true;
                        newGroup.overrideScriptPath = "%s/Scene/%s/scene%s_group%s.lua"
                                .formatted(sh.getDataFolder().getAbsolutePath(),
                                        sceneId, sceneId, newGroup.id);
                        groupsToAdd.add(newGroup);
                        sh.getLogger().info("onSceneBlockLoad: Added new replacement group {}", newGroup.id);
                    }
                }
            });

            groupsToAdd.forEach(g -> block.groups.put(g.id, g));
            sh.getLogger().info("onSceneBlockLoad: hooked sceneId {}", sceneId);
        }
    }

    public static void onSceneMetaLoad(SceneMetaLoadEvent event) {
        var sh = SceneHook.getInstance();

        var scene = event.scene;
        var sceneId = scene.getId();

        if (sh.hasSceneHook(sceneId)) {
            event.hasOverride = true;
            scene.runWhenFinished(() -> {
                for (var replacement : sh.getBlockReplacement(sceneId).replacements) {
                    sh.getLogger().info("onSceneMetaLoad: Loading scene {} dynamic group {}", sceneId, replacement.newSceneGroup);
                    scene.loadDynamicGroup(replacement.newSceneGroup);
                }
            });
            sh.getLogger().info("onSceneMetaLoad: Overriding scene {}", sceneId);
        }
    }
}
