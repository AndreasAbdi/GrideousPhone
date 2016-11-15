package abdi.andreas.grideous.engine;

import android.os.Handler;
import android.os.Message;

public class RefreshHandler extends Handler {
    private TileGameView refreshTarget;

    @Override
    public void handleMessage(Message msg) {
        refreshTarget.update();
        refreshTarget.invalidate();
    }

    public void sleep(long delayMilliseconds) {
        this.removeMessages(0);
        sendMessageDelayed(obtainMessage(0),delayMilliseconds);
    }

    public RefreshHandler(TileGameView view) {
        this.refreshTarget = view;
    }
}
