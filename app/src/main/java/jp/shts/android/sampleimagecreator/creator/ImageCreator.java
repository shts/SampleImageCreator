package jp.shts.android.sampleimagecreator.creator;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class ImageCreator {

    private static final int IMAGE_WIDTH = 2000/*px*/;
    private static final int IMAGE_HEIGHT = 1500/*px*/;
    private static final int DEFAULT_BULK_CREATE_SIZE = 10;

    private Context context;
    private Colors colors;

    public interface OnCreateListener {
        void onStartCreateImage();

        void onFinishCreateImage(String path, Uri uri);
    }

    public ImageCreator(@NonNull Context context) {
        this.context = context;
        colors = new Colors(context);
    }

    public ImageCreator(@NonNull Context context, OnCreateListener listener) {
        this.context = context;
        this.listener = listener;
        colors = new Colors(context);
    }

    public void create() {
        if (listener != null) listener.onStartCreateImage();

        Bitmap bitmap = createBaseBitmap();
        Canvas canvas = new Canvas(bitmap);
        decorate(canvas);

        final File f = createOutputFile();
        saveBitmap(bitmap, f);
        addDateTimeAsExif(f);

        MediaScannerConnection.scanFile(
                context, new String[]{f.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        if (listener != null) listener.onFinishCreateImage(path, uri);
                    }
                });
    }

    private OnCreateListener listener;

    public void setOnCreateListener(OnCreateListener listener) {
        this.listener = listener;
    }

    public void bulkCreate() {
        for (int i = 0; i < getBulkCreateSize(); i++) {
            create();
        }
    }

    public void bulkCreate(int createSize) {
        for (int i = 0; i < createSize; i++) {
            create();
        }
    }

    protected int getBulkCreateSize() {
        return DEFAULT_BULK_CREATE_SIZE;
    }

    protected LocalDateTime getExifLocalDateTime() {
        return LocalDateTime.now();
    }

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

    protected Bitmap createBaseBitmap() {
        return Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
    }

    protected File createOutputFile() {
        return new File(getExternalStorageDir(),
                String.valueOf(System.currentTimeMillis()) + ".jpg");
    }

    @Nullable
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

    private boolean saveBitmap(@NonNull Bitmap bitmap, @NonNull File file) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)) {
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // nop
                }
            }
        }
        return false;
    }

    private void addDateTimeAsExif(File file) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
            String formattedDateTime = getExifLocalDateTime().format(formatter);
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, formattedDateTime);
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
