package engineLoop;

import interfaces.Updatable;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;

public class EngineLoop extends AnimationTimer {
    private long lastUpdate = 0;
    private final List<Updatable> updatableList = new ArrayList<>();

    public EngineLoop() {
    }

    public void addUpdatable(Updatable updatable) {
        updatableList.add(updatable);
    }

    @Override
    public void handle(long now) {
        if (lastUpdate > 0) {
            double dt = (now - lastUpdate) / 1_000_000_000.0;
            for (Updatable u : updatableList) {
                u.update(dt);
            }
        }
        lastUpdate = now;
    }


}
