package jp.shts.android.sampleimagecreator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * 通信時用プログレス
 */
public class FullscreenProgressDialog extends Dialog {

    private int progressMessageResId;

    public FullscreenProgressDialog(Context context) {
        super(context, R.style.Theme_SampleImage_Dialog);
    }

    public FullscreenProgressDialog(Context context, int progressMessageResId) {
        this(context);
        this.progressMessageResId = progressMessageResId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.fullscreen_progress_dialog);
        setCancelable(false);
        if (progressMessageResId > 0) {
            TextView messageView = (TextView) findViewById(R.id.progress_message);
            messageView.setText(progressMessageResId);
            messageView.setVisibility(View.VISIBLE);
        }
        super.onCreate(savedInstanceState);
    }
}
