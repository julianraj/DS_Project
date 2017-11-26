package app.rm.replica;

import java.util.HashMap;
import java.util.Map;

public abstract class Replica<S> {
    public static final String campuses[] = new String[]{"KKL", "DVL", "WST"};

    public boolean hasError;

    protected Map<String, S> serverMap;

    public Replica(boolean hasError) {
        this.hasError = hasError;
        serverMap = new HashMap<>();
    }

    public void start() {
        start(false);
    }

    protected abstract void start(boolean requestData);

    public abstract void stop();

    public abstract void restart();

    protected abstract void requestData();

    protected abstract void mapJsonToData(String json);

    public abstract boolean ping(String campus);
}
