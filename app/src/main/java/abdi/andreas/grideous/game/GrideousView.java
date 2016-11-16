package abdi.andreas.grideous.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

import abdi.andreas.grideous.R;
import abdi.andreas.grideous.engine.Direction;
import abdi.andreas.grideous.engine.RefreshHandler;
import abdi.andreas.grideous.engine.TileGameView;

public class GrideousView extends TileGameView {

    private static final String TAG = "GrideousView";

    //Drawable configurations
    private static final int RED_BLOCK = 1;
    private static final int GREEN_BLOCK = 2;
    private static final int BLUE_BLOCK = 3;

    //graphics redraw handling.
    private RefreshHandler refreshHandler = new RefreshHandler(this);

    //Views
    private TextView statusView;
    private View arrowsView;
    private View backgroundView;


    //game data
    private Mode currentMode = Mode.READY;
    private Direction currentDirection = Direction.UP;
    private Direction nextDirection = Direction.UP;

    private long score = 0;
    private long moveDelay = 600;

    private long lastMove;

    private boolean[][] rubble;
    private Block currentBlock = null;
    /**
     * game logic pseudo code.
     * a fall direction is set.
     * blocks fall in the fall direction. (1 - 3 blocks).
     * if the next block that generates cannot move
     * If there is a row/ column full of the blocks, that wave is cleared.
     * If it still cannot move, then check if any part of it is out of borders, and block it out.
    **/

     private static final Random RNG = new Random();

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

    public void moveCurrentBlock(Direction direction) {
        switch (direction) {
            case UP:
                if (currentDirection != Direction.DOWN) {
                    nextDirection = Direction.UP;
                }
                break;
            case RIGHT:
                if (currentDirection != Direction.LEFT) {
                    nextDirection = Direction.RIGHT;
                }
                break;
            case DOWN:
                if (currentDirection != Direction.UP) {
                    nextDirection = Direction.DOWN;
                }
                break;
            case LEFT:
                if (currentDirection != Direction.RIGHT) {
                    nextDirection = Direction.LEFT;
                }
                break;
        }
    }

    public void update(){
        if(currentMode == Mode.RUNNING) {
            long currentTime = System.currentTimeMillis();
            if(currentTime - lastMove > moveDelay) {
                performUpdate();
                lastMove = currentTime;
            }
            refreshHandler.sleep(moveDelay);
        }
    }

    private void performUpdate() {
        clearGrid();
        runGameTick();
        drawComponents();
    }

    public static Block generateBlock( Point maxPosition) {
        Block block = new Block();

        int blockType = 1 + RNG.nextInt(2);
        //generate the generic 3 block vertical
        Point center = new Point(
                (int) Math.floor(maxPosition.x/2),
                (int) Math.floor(maxPosition.y/2));
        for(int x = -1; x < 2; x++) {
            for(int y = -1; y < 2; y++) {
                boolean generateBlock = RNG.nextBoolean();
                if(generateBlock) {
                    Point part = new Point(
                            center.x + x,
                            center.y + y
                    );
                    block.parts.add(part);
                }
            }
        }
        return block;
    }

    private void runGameTick() {
        if(currentBlock == null) {
            currentBlock = generateCurrentBlock();
        }

        currentDirection = nextDirection;
        //TODO: consider making parts of the block fall in a direction if there is nothing under.
        doRubbleClearing();
        Block nextPosition = generateNextCurrentBlockPosition();
        if(blockCollidesWithWall(nextPosition)||blockCollidesWithRubble(nextPosition)) {
            setCurrentBlockToRubble();
            Block newCurrentBlock = generateCurrentBlock();
            if(newCurrentBlock == null) {
                setModeAndUpdate(Mode.LOSING);
            } else {
                this.currentBlock = newCurrentBlock;
            }
        } else {
            currentBlock = nextPosition;
        }
    }

    private Block generateCurrentBlock() {
        Block newCurrentBlock = generateBlock(new Point(xTileCount, yTileCount));
        if(blockCollidesWithRubble(newCurrentBlock)) {
            return null;
        }
        return newCurrentBlock;

    }

    private void setCurrentBlockToRubble() {
        for(Point part : currentBlock.parts) {
            rubble[part.x][part.y] = true;
        }
    }

    private void doRubbleClearing() {
        doRowRubbleClearing();
        doColumnRubbleClearing();
    }

    private void doRowRubbleClearing() {
        for(int y = 0; y < yTileCount; y++) {
            boolean rowFilled = true;
            for(int x = 0; x < xTileCount; x++) {
                if(!rubble[x][y]) {
                    rowFilled = false;
                    break;
                }
            }
            if(rowFilled) {
                for(int x = 0; x < xTileCount; x++) {
                    rubble[x][y] = false;
                }
                handleScoreUp();
            }
        }
    }

    private void doColumnRubbleClearing() {
        for(int x = 0; x < yTileCount; x++) {
            boolean columnFilled = true;
            for(int y = 0; y < xTileCount; y++) {
                columnFilled = false;
                break;
            }
            if(columnFilled) {
                for(int y = 0; y < yTileCount; y++) {
                    rubble[x][y] = false;
                }
                handleScoreUp();
            }
        }
    }

    private void drawComponents() {
        drawWalls();
        drawRubble();
        drawCurrentBlock();
    }

    private void drawWalls(){
        for(int x = 0; x < xTileCount; x++) {
            setGridTile(GREEN_BLOCK, x, 0);
            setGridTile(GREEN_BLOCK, x, yTileCount - 1);
        }
        for(int y = 0; y < yTileCount; y++){
            setGridTile(GREEN_BLOCK, 0, y);
            setGridTile(GREEN_BLOCK, xTileCount -1, y);
        }
    }

    private void drawCurrentBlock() {
        for(Point parts : currentBlock.parts) {
            setGridTile(RED_BLOCK, parts.x, parts.y);
        }
    }

    private void drawRubble() {
        for(int x = 0; x < xTileCount; x++) {
            for(int y = 0; y < yTileCount; y++) {
                if(rubble[x][y]) {
                    setGridTile(BLUE_BLOCK, x,y);
                }
            }
        }
    }

    private Point generateNextBlockPartPosition(Point point) {
        switch(currentDirection) {
            case UP:
                return new Point(point.x, point.y - 1);
            case DOWN:
                return new Point(point.x, point.y + 1);
            case LEFT:
                return new Point(point.x - 1, point.y);
            case RIGHT:
                return new Point(point.x + 1, point.y);
            default:
                return point;
        }
    }

    private Block generateNextCurrentBlockPosition() {
        Block block = new Block();
        for(Point part : currentBlock.parts) {
            Point newPart = generateNextBlockPartPosition(part);
            block.parts.add(newPart);
        }

        return block;
    }

    private boolean blockCollidesWithWall(Block block) {
        for(Point part : block.parts) {
            if(pointCollidesWithWall(part)) {
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

    private boolean blockCollidesWithRubble(Block block) {
        for(Point part : block.parts) {
            if(rubble[part.x][part.y]) {
                return true;
            }
        }
        return false;
    }

    private void handleScoreUp() {
        score++;
        moveDelay *= 0.9;
    }

    private void initializeGameData() {
        rubble = new boolean[xTileCount][yTileCount];
        moveDelay = 600;
        score = 0;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.rubble = new boolean[xTileCount][yTileCount];
    }
}
