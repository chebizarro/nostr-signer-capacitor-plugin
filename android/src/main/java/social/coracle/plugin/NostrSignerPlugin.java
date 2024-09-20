package social.coracle.plugin;

import android.util.Log;

public class NostrSignerPlugin {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
