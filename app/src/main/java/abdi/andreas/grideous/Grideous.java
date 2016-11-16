package abdi.andreas.grideous;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import abdi.andreas.grideous.engine.Direction;
import abdi.andreas.grideous.game.GrideousView;
import abdi.andreas.grideous.game.Mode;

public class Grideous extends Activity {

    private static String ICICLE_KEY = "snake-view";
    private GrideousView grideousView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grideous);
        grideousView =  (GrideousView) findViewById(R.id.grideous);
        grideousView.setDependentViews((TextView)findViewById(R.id.text),
                findViewById(R.id.arrowContainer),
                findViewById(R.id.background));
        if(savedInstanceState == null) {
            grideousView.setMode(Mode.READY);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        grideousView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (grideousView.getCurrentMode() == Mode.RUNNING) {
                    // Normalize x,y between 0 and 1
                    float x = event.getX() / v.getWidth();
                    float y = event.getY() / v.getHeight();

                    Direction direction = Direction.UP;
                    boolean upperTriangle = (x > y);
                    boolean lowerTriangle = (x > 1-y);
                    if(upperTriangle && lowerTriangle) {
                        direction = Direction.RIGHT;
                    } else if(upperTriangle && !lowerTriangle) {
                        direction = Direction.UP;
                    } else if(!upperTriangle && lowerTriangle) {
                        direction = Direction.DOWN;
                    } else {
                        direction = Direction.LEFT;
                    }

                    // Direction is same as the quadrant which was clicked
                    grideousView.moveCurrentBlock(direction);
                } else if(grideousView.getCurrentMode() == Mode.PAUSED){
                    grideousView.resumeGame();
                } else {
                    grideousView.initializeGame();
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        grideousView.setMode(Mode.PAUSED);
    }

    //this is for if no touch screen.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if(grideousView.getCurrentMode() == Mode.READY || grideousView.getCurrentMode() == Mode.LOSING) {
                    grideousView.initializeGame();
                } else if (grideousView.getCurrentMode() == Mode.PAUSED) {
                    grideousView.resumeGame();
                } else {
                    grideousView.moveCurrentBlock(Direction.UP);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                grideousView.moveCurrentBlock(Direction.DOWN);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                grideousView.moveCurrentBlock(Direction.LEFT);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                grideousView.moveCurrentBlock(Direction.RIGHT);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
