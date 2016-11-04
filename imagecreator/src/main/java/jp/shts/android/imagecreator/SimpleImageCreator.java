package jp.shts.android.imagecreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import org.threeten.bp.LocalDateTime;

import java.io.File;
import java.util.Random;

public class SimpleImageCreator extends Creator {

    private static final int IMAGE_WIDTH = 2000/*px*/;
    private static final int IMAGE_HEIGHT = 1500/*px*/;
    private static final int DEFAULT_BULK_CREATE_SIZE = 10;

    private final Colors colors;

    public SimpleImageCreator(@NonNull Context context) {
        super(context);
        colors = new Colors(context);
    }

    public SimpleImageCreator(@NonNull Context context, OnCreateListener listener) {
        super(context, listener);
        colors = new Colors(context);
    }

    @Override
    protected Bitmap createBaseBitmap() {
        return Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void decorate(Canvas canvas) {
        canvas.drawColor(getBackgroundColor());

        Paint p = new Paint();
        p.setColor(getPaintColor());

        float textSize = 500;
        p.setTextSize(textSize);
        canvas.drawText(randomText(), (IMAGE_WIDTH / 2) - textSize, (IMAGE_HEIGHT / 2), p);
        canvas.save();
    }

    @ColorInt
    protected int getBackgroundColor() {
        return colors.random();
    }

    @ColorInt
    protected int getPaintColor() {
        return colors.random();
    }

    @Override
    protected File createOutputFile() {
        return new File(getExternalStorageDir(),
                String.valueOf(System.currentTimeMillis()) + ".jpg");
    }

    private File getExternalStorageDir() {
        final File dir = Environment.getExternalStorageDirectory();
        if (dir == null) {
            return null;
        }
        final File randomDir = new File(dir, "random");
        // ディレクトリじゃない同名ファイルがあれば削除
        if (randomDir.exists() && !randomDir.isDirectory()) {
            if (!randomDir.delete()) {
                return null;
            }
        }
        if (!randomDir.exists()) {
            if (!randomDir.mkdir()) {
                return null;
            }
        }
        return randomDir;
    }

    @Override
    protected LocalDateTime getExifLocalDateTime() {
        return LocalDateTime.now();
    }

    @Override
    protected int getBulkCreateSize() {
        return DEFAULT_BULK_CREATE_SIZE;
    }

    private String[] ALPHABET = {
            "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "N", "M",
            "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"
    };

    private String randomText() {
        Random r = new Random();
        String c = ALPHABET[r.nextInt(ALPHABET.length - 1)];
        int i = r.nextInt(99);
        if (i > 10) {
            return c + String.valueOf(i);
        } else {
            return c + "0" + String.valueOf(i);
        }
    }

}
