package jp.shts.android.imagecreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public abstract class Creator {

    public interface OnCreateListener {
        void onStartCreateImage();

        void onFinishCreateImage(String path, Uri uri);
    }

    private OnCreateListener listener;
    private Context context;

    public Creator(@NonNull Context context) {
        this.context = context;
    }

    public Creator(@NonNull Context context, OnCreateListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void create() {
        if (listener != null) listener.onStartCreateImage();

        Bitmap bitmap = createBaseBitmap();
        Canvas canvas = (bitmap == null) ? new Canvas() : new Canvas(bitmap);
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

    public void bulkCreate() {
        for (int i = 0; i < getBulkCreateSize(); i++) {
            create();
        }
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

    protected abstract int getBulkCreateSize();

    protected abstract Bitmap createBaseBitmap();

    protected abstract void decorate(Canvas canvas);

    protected abstract File createOutputFile();

    protected abstract LocalDateTime getExifLocalDateTime();
}
