package jp.shts.android.sampleimagecreator.creator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import java.util.List;

public class DateTimeImageCreator extends CustomImageCreator {

    private List<ZonedDateTime> zonedDateTimeList;

    private int counter = 0;

    public DateTimeImageCreator(@NonNull Context context) {
        super(context);
    }

    public void setData(List<ZonedDateTime> zonedDateTimeList) {
        this.zonedDateTimeList = zonedDateTimeList;
    }

    @Override
    protected void decorate(Canvas canvas) {
        final int backgroundColor = getBackgroundColor();
        canvas.drawColor(backgroundColor);

        final Paint pDate = new Paint();
        final int paintColor = notSameColor(backgroundColor);
        pDate.setColor(paintColor);
        pDate.setTypeface(Typeface.DEFAULT_BOLD);
        pDate.setTextSize(getTextSize());
        canvas.drawText(getText(), getTextSize(), (IMAGE_HEIGHT / 2) + getTextSize() / 4, pDate);

        final Paint pTime = new Paint();
        pTime.setColor(paintColor);
        pTime.setTypeface(Typeface.DEFAULT_BOLD);
        pTime.setTextSize(getTextSize());
        float datePointY = (IMAGE_HEIGHT / 2) + getTextSize() / 4;
        canvas.drawText(getTimeText(), getTextSize(), datePointY + getTextSize(), pTime);

        canvas.save();

        counter++;
    }

    private int notSameColor(int backgroundColor) {
        int color = getPaintColor();
        while (backgroundColor == color) {
            color = getPaintColor();
        }
        return color;
    }

    @Override
    protected int getBulkCreateSize() {
        return zonedDateTimeList.size();
    }

    @Override
    protected LocalDateTime getExifLocalDateTime() {
        return zonedDateTimeList.get(counter).toLocalDateTime();
    }

    private String getTimeText() {
        LocalDateTime l = getExifLocalDateTime();
        String hour = l.getHour() < 10 ? "0" + l.getHour() : String.valueOf(l.getHour());
        String minutes = l.getMinute() < 10 ? "0" + l.getMinute() : String.valueOf(l.getMinute());
        return hour + ":" + minutes;
    }
}
