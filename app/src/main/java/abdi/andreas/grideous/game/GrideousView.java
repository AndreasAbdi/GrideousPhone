package abdi.andreas.grideous.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import abdi.andreas.grideous.R;
import abdi.andreas.grideous.engine.Direction;
import abdi.andreas.grideous.engine.RefreshHandler;
import abdi.andreas.grideous.engine.TileGameView;

public class GrideousView extends TileGameView {

    private static final String TAG = "GrideousView";



    private Mode currentMode = Mode.READY;
    private Direction currentDirection = Direction.UP;
    private Direction nextDirection = Direction.UP;

    //Drawable configurations
    private static final int RED_BLOCK = 1;
    private static final int GREEN_BLOCK = 2;
    private static final int BLUE_BLOCK = 3;

    private long score = 0;
    private long moveDelay = 600;

    private long lastMove;

    //graphics redraw handling.
    private RefreshHandler refreshHandler = new RefreshHandler(this);

    //game data
    private ArrayList<Point> snakeParts = new ArrayList<>();
    private ArrayList<Point> appleList = new ArrayList<>();
    private static final Random RNG = new Random();

    //Views
    private TextView statusView;
    private View arrowsView;
    private View backgroundView;


    private void setTileTypes(Resources resources) {
        resetTileTypes(4);
        //TODO: if update > 21 change to (resource, null) get drawable calls.
        setTileType(RED_BLOCK, resources.getDrawable(R.drawable.redblock));
        setTileType(BLUE_BLOCK, resources.getDrawable(R.drawable.greenblock));
        setTileType(GREEN_BLOCK, resources.getDrawable(R.drawable.blueblock));
    }

    private void constructorDelegate(Context context) {
        setFocusable(true);
        Resources resources = this.getContext().getResources();
        setTileTypes(resources);
    }

    public GrideousView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        constructorDelegate(context);
    }

    public GrideousView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    private void initializeGameData() {
        snakeParts.clear();
        appleList.clear();

        snakeParts.add(new Point(7,7));

        addRandomApple();
        moveDelay = 600;
        score = 0;

    }

    public void resumeGame() {
        setModeAndUpdate(Mode.RUNNING);
        update();
    }

    public void initializeGame() {
        initializeGameData();
        resumeGame();
    }

    public void setDependentViews(TextView textView, View arrowsView, View backgroundView) {
        this.statusView = textView;
        this.arrowsView = arrowsView;
        this.backgroundView = backgroundView;
    }

    public void setModeAndUpdate(Mode mode) {
        Mode oldMode = this.currentMode;
        this.currentMode = mode;

        if(mode == Mode.RUNNING && oldMode != Mode.RUNNING) {
            statusView.setVisibility(View.INVISIBLE);
            update();
            arrowsView.setVisibility(View.VISIBLE);
            backgroundView.setVisibility(View.VISIBLE);
            return;
        }

        Resources resources = getContext().getResources();
        CharSequence string = "";

        switch(mode) {
            case RUNNING:
                return;
            case PAUSED:
                string = resources.getText(R.string.mode_paused);
                break;
            case READY:
                string = resources.getText(R.string.mode_ready);
                break;
            case LOSING:
                string = resources.getString(R.string.mode_losing, score);
                break;
        }

        arrowsView.setVisibility(View.GONE);
        backgroundView.setVisibility(View.GONE);

        statusView.setText(string);
        statusView.setVisibility(View.VISIBLE);
    }

    public Mode getCurrentMode() {
        return this.currentMode;
    }

    public void moveSnake(Direction direction) {
        switch(direction){
            case UP:
                if(currentDirection != Direction.DOWN) {
                    nextDirection = Direction.UP;
                }
                break;
            case RIGHT:
                if(currentDirection != Direction.LEFT) {
                    nextDirection = Direction.RIGHT;
                }
                break;
            case DOWN:
                if(currentDirection != Direction.UP) {
                    nextDirection = Direction.DOWN;
                }
                break;
            case LEFT:
                if(currentDirection != Direction.RIGHT) {
                    nextDirection = Direction.LEFT;
                }
                break;
        }
    }

    private boolean pointCollidesWithSnake(Point point) {
        for(int index = 0; index < snakeParts.size(); index++) {
            if(snakeParts.get(index).equals(point)) {
                return true;
            }
        }
        return false;
    }

    private boolean pointCollidesWithWall(Point point) {
        boolean pointCollidesWithWall = false;
        pointCollidesWithWall = pointCollidesWithWall || point.x < 1;
        pointCollidesWithWall = pointCollidesWithWall || point.y < 1;
        pointCollidesWithWall = pointCollidesWithWall || point.x > xTileCount - 2;
        pointCollidesWithWall = pointCollidesWithWall || point.y > yTileCount - 2;
        return pointCollidesWithWall;
    }

    private Point generateRandomPointOnGrid() {
        int x = 1 + RNG.nextInt(xTileCount - 2);
        int y = 1 + RNG.nextInt(yTileCount - 2);
        return new Point(x,y);
    }

    private void addRandomApple() {
        boolean currentPositionInvalid = true;
        Point point = null;
        while(currentPositionInvalid) {
            point = generateRandomPointOnGrid();
            currentPositionInvalid = pointCollidesWithSnake(point);
        }
        appleList.add(point);
    }

    public void update(){
        if(currentMode == Mode.RUNNING) {
            long currentTime = System.currentTimeMillis();
            if(currentTime - lastMove > moveDelay) {
                clearGrid();
                updateWalls();
                updateSnake();
                updateApples();
                lastMove = currentTime;
            }
            refreshHandler.sleep(moveDelay);
        }
    }

    private void updateWalls(){
        for(int x = 0; x < xTileCount; x++) {
            setGridTile(GREEN_BLOCK, x, 0);
            setGridTile(GREEN_BLOCK, x, yTileCount - 1);
        }
        for(int y = 0; y < yTileCount; y++){
            setGridTile(GREEN_BLOCK, 0, y);
            setGridTile(GREEN_BLOCK, xTileCount -1, y);
        }
    }

    private void updateApples(){
        for (Point point : appleList) {
            setGridTile(BLUE_BLOCK, point.x, point.y);
        }
    }

    private Point generateNewHead() {
        Point currentHead = snakeParts.get(0);
        switch(currentDirection) {
            case UP:
                return new Point(currentHead.x, currentHead.y - 1);
            case DOWN:
                return new Point(currentHead.x, currentHead.y + 1);
            case LEFT:
                return new Point(currentHead.x - 1, currentHead.y);
            case RIGHT:
                return new Point(currentHead.x + 1, currentHead.y);
            default:
                return currentHead;
        }
    }

    private Point pointCollidesWithApple(Point point) {
        for(Point apple : appleList) {
            if(point.equals(apple)) {
                return apple;
            }
        }
        return null;
    }

    private void setSnakeTiles() {
        for(Point snakePart : snakeParts) {
            setGridTile(RED_BLOCK, snakePart.x, snakePart.y);
        }
    }

    private void handleScoreUp() {
        addRandomApple();
        score++;
        moveDelay *= 0.9;
    }

    private void updateSnake(){
        boolean growSnake = false;
        currentDirection = nextDirection;
        Point newHead = generateNewHead();
        if(pointCollidesWithSnake(newHead) || pointCollidesWithWall(newHead)) {
            setModeAndUpdate(Mode.LOSING);
        }
        //look for apples.
        Point appleCollisionPoint = pointCollidesWithApple(newHead);
        if(appleCollisionPoint != null) {
            appleList.remove(appleCollisionPoint);
            handleScoreUp();
            growSnake = true;
        }

        snakeParts.add(0, newHead);
        if(!growSnake) {
            snakeParts.remove(snakeParts.size() - 1);
        }
        setSnakeTiles();

    }

}
