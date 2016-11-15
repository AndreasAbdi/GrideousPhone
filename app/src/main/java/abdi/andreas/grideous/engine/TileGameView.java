package abdi.andreas.grideous.engine;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import abdi.andreas.grideous.R;

/**
 * Tile View class to handle displaying a grid based game.
 */
public abstract class TileGameView extends View {

    protected static int tileSize;
    protected static int xTileCount;
    protected static int yTileCount;

    private static int xOffset;
    private static int yOffset;
    private final Paint paint = new Paint();

    /**
     * grid to which the game is to be drawn.
     */
    private int[][] tileGrid;

    /**
     * bitmap of the different tiles to draw.
     */
    private Bitmap[] tileTypes;


    /**
     *  Constructors.
     */


    private void constructorDelegateGetTileSize(Context context, AttributeSet attributeSet) {
        TypedArray styledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.TileGameView);
        tileSize = styledAttributes.getDimensionPixelSize(R.styleable.TileGameView_tileSize, 10);

        styledAttributes.recycle();
    }

    public TileGameView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        constructorDelegateGetTileSize(context, attributeSet);
    }

    public TileGameView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        constructorDelegateGetTileSize(context, attributeSet);
    }

    public TileGameView(Context context) {
        super(context);
    }


    /**
     * Member functions.
     */

    public void clearGrid() {
        for(int x = 0; x < xTileCount; x++) {
            for(int y = 0; y <yTileCount; y++){
                setGridTile(0,x,y);
            }
        }
    }
    public void setGridTile(int tileType, int x, int y) {
        tileGrid[x][y] = tileType;
    }

    public void setTileType(int key, Drawable tile) {
        Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tile.setBounds(0,0,tileSize,tileSize);
        tile.draw(canvas);

        tileTypes[key] = bitmap;
    };

    public void resetTileTypes(int tileCount){
        tileTypes = new Bitmap[tileCount];
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int x = 0; x < xTileCount; x++) {
            for(int y = 0; y < yTileCount; y++) {
                int currentTileTypeKey =  tileGrid[x][y];
                if(currentTileTypeKey > 0) {
                    canvas.drawBitmap(tileTypes[currentTileTypeKey],
                            xOffset + x*tileSize,
                            yOffset + y*tileSize,
                            paint);
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        xTileCount = (int) Math.floor(w/tileSize);
        yTileCount = (int) Math.floor(h/tileSize);

        xOffset = (w - tileSize*xTileCount)/2;
        yOffset = (h - tileSize*yTileCount)/2;

        tileGrid = new int[xTileCount][yTileCount];
        clearGrid();

    }

    public abstract void update();

}
