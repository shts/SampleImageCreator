package jp.shts.android.sampleimagecreator.creator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;

abstract class Creator {

    private Context context;
    private int bulkCounter = 0;

    Creator(@NonNull Context context) {
        this.context = context;
    }

    public Observable<Pair<String, Uri>> bulkCreate() {
        return Observable.create(new Observable.OnSubscribe<Pair<String, Uri>>() {
            @Override
            public void call(final Subscriber<? super Pair<String, Uri>> subscriber) {
                try {
                    final int bulkSize = getBulkCreateSize();
                    bulkCounter = 0;
                    for (int i = 0; i < bulkSize; i++) {
                        Bitmap bitmap = createBaseBitmap();
                        Canvas canvas = (bitmap == null) ? new Canvas() : new Canvas(bitmap);
                        decorate(canvas);

                        final File f = createOutputFile();
                        saveBitmap(bitmap, f);
                        addDateTimeAsExif(f);

                        MediaScannerConnection.scanFile(
                                context,
                                new String[]{f.getAbsolutePath()},
                                null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        subscriber.onNext(new Pair<>(path, uri));
                                        bulkCounter++;
                                        if (bulkSize <= bulkCounter) {
                                            subscriber.onCompleted();
                                        }
                                    }
                                });
                    }
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
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
