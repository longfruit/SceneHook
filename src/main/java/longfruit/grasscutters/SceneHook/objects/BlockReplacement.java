package longfruit.grasscutters.SceneHook;

import com.google.gson.Gson;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

public final class BlockReplacement {
    public final class Replacement {
        public int replacedSceneGroup;
        public int newSceneGroup;
    }

    List<Replacement> replacements;
}
