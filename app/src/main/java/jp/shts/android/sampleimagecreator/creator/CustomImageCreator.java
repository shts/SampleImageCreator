package jp.shts.android.sampleimagecreator.creator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import jp.shts.android.sampleimagecreator.Store;

public class CustomImageCreator extends SimpleImageCreator {

    private static final String TAG = CustomImageCreator.class.getSimpleName();

    private Store store;

    public CustomImageCreator(@NonNull Context context) {
        super(context);
        store = new Store(context);
    }

    @Override
    protected String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append(getExifLocalDateTime().getYear()).append("/");
        sb.append(getExifLocalDateTime().getMonthValue()).append("/");
        sb.append(getExifLocalDateTime().getDayOfMonth());
        return sb.toString();
    }

    @Override
    protected void decorate(Canvas canvas) {
        canvas.drawColor(getBackgroundColor());

        Paint p = new Paint();
        p.setColor(getPaintColor());

        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(getTextSize());
        canvas.drawText(getText(), getTextSize(), (IMAGE_HEIGHT / 2) + getTextSize() / 4, p);
        canvas.save();
    }

    @Override
    protected float getTextSize() {
        return 300f;
    }

    @Override
    protected String dirname() {
        return store.dirname();
    }

}
