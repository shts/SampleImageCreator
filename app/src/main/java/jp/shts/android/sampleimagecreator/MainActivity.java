package jp.shts.android.sampleimagecreator;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.thebluealliance.spectrum.SpectrumDialog;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.shts.android.imagecreator.Creator;
import jp.shts.android.imagecreator.SimpleImageCreator;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.created_sample_images_date)
    TextView dateTextView;

    @Bind(R.id.created_sample_images_count_spinner)
    Spinner spinner;

    @Bind(R.id.fix_created_sample_images_background_color)
    Switch backgroundColorSwitch;

    @Bind(R.id.fix_background_color_container)
    View backgroundColorSettingsContainer;

    @Bind(R.id.fix_text_color_container)
    View textColorSettingsContainer;

    @Bind(R.id.fix_created_sample_images_text_color)
    Switch textColorSwitch;

    @Bind(R.id.text_color_preview)
    View colorPreviewText;
    @Bind(R.id.background_color_preview)
    View colorPreviewBackground;

    ZonedDateTime target;

    private int size = 10;

    private Store store;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        store = new Store(this);

        target = LocalDateTime.now().atZone(ZoneId.systemDefault());
        dateTextView.setText(createDateDescriptionText());

        // 背景色設定
        boolean isCheckedBackgroundSwitch = store.checkedBackgroundColor();
        backgroundColorSwitch.setChecked(isCheckedBackgroundSwitch);
        backgroundColorSettingsContainer.setVisibility(isCheckedBackgroundSwitch ? View.VISIBLE : View.GONE);
        backgroundColorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                backgroundColorSettingsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                store.checkedBackgroundColor(isChecked);
            }
        });

        // 文字色設定
        boolean isCheckedTextSwitch = store.checkedTextColor();
        textColorSwitch.setChecked(isCheckedTextSwitch);
        textColorSettingsContainer.setVisibility(isCheckedTextSwitch ? View.VISIBLE : View.GONE);
        textColorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                textColorSettingsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                store.checkedTextColor(isChecked);
            }
        });

        int colorBackground = store.backgroundColor();
        if (colorBackground != -1) {
            colorPreviewBackground.setBackgroundColor(colorBackground);
        } else {
            colorPreviewBackground.setBackgroundResource(R.color.colorPrimary);
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.add("1");
        adapter.add("5");
        adapter.add("10");
        adapter.add("15");
        adapter.add("20");
        adapter.add("25");
        adapter.add("30");
        adapter.add("35");
        adapter.add("40");
        adapter.add("45");
        adapter.add("50");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                String s = (String) adapter.getItem(spinner.getSelectedItemPosition());
                size = Integer.parseInt(s);
                dateTextView.setText(createDateDescriptionText());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String createDateDescriptionText() {
        return formatString(target) + "の画像を" + size + "枚作成します";
    }

    private String formatString(ZonedDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.getDefault());
        return localDateTime.format(formatter);
    }

    @OnClick(R.id.edit_created_sample_images_date)
    void onClickChangeImageDateButton() {
        showDatePicker();
    }

    private void showDatePicker() {
        final ZonedDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
        final DatePickerDialog dialog = new DatePickerDialog(this, R.style.Theme_SampleImage_Dialog_Alert,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        target = LocalDateTime.of(year, (monthOfYear + 1), dayOfMonth, 0, 0, 0, 0).atZone(ZoneId.systemDefault());
                        dateTextView.setText(createDateDescriptionText());
                    }
                },
                localDateTime.getYear(), localDateTime.getMonthValue() - 1, localDateTime.getDayOfMonth());
        dialog.show();
    }

    @OnClick(R.id.create_image)
    void onClickCreateImageButton() {
        new AlertDialog.Builder(this)
                .setMessage(createDateDescriptionText())
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivityPermissionsDispatcher.createImageWithCheck(MainActivity.this);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void createImage() {
        new SimpleImageCreator(this, new Creator.OnCreateListener() {
            @Override
            public void onStartCreateImage() {
            }

            @Override
            public void onFinishCreateImage(String path, Uri uri) {
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "画像作成が完了しました", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }) {
            @Override
            protected LocalDateTime getExifLocalDateTime() {
                return LocalDateTime.of(
                        target.getYear(),
                        target.getMonth(),
                        target.getDayOfMonth(),
                        target.getHour(),
                        target.getMinute());
            }

            @Override
            protected int getBulkCreateSize() {
                return size;
            }
        }.bulkCreate();
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onPermissionDeniedStorage() {
        Toast.makeText(this, R.string.need_write_external_permission, Toast.LENGTH_LONG).show();
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onShowRationaleStorage(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.need_write_external_permission)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onNeverAskAgainStorage() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.need_write_external_permission)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void showColorPickerDialog(final View view) {
        new SpectrumDialog.Builder(this)
                .setTitle("色を選んでね")
                .setColors(R.array.material_colors)
                .setSelectedColorRes(R.color.colorPrimary)
                .setDismissOnColorSelected(true)
                .setFixedColumnCount(5)
                .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                        if (positiveResult) {
                            Toast.makeText(MainActivity.this, "Color selected: #" + Integer.toHexString(color).toUpperCase(), Toast.LENGTH_SHORT).show();
                            view.setBackgroundColor(color);
                            if (view.getId() == R.id.edit_created_sample_images_background_color) {
                                store.backgroundColor(color);
                            } else {
                                store.textColor(color);
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Dialog cancelled", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).build().show(MainActivity.this.getSupportFragmentManager(), "dialog_demo_4");
    }

    @OnClick(R.id.edit_created_sample_images_background_color)
    void onClickChangeTextColorButton() {
        showColorPickerDialog(findViewById(R.id.edit_created_sample_images_background_color));
    }

    @OnClick(R.id.edit_created_sample_images_text_color)
    void onClickChangeBackgroundColorButton() {
        showColorPickerDialog(findViewById(R.id.edit_created_sample_images_text_color));
    }
}
