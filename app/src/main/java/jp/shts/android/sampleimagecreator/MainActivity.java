package jp.shts.android.sampleimagecreator;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.thebluealliance.spectrum.SpectrumDialog;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.shts.android.sampleimagecreator.creator.CustomImageCreator;
import jp.shts.android.sampleimagecreator.creator.DateTimeImageCreator;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static jp.shts.android.sampleimagecreator.Store.KEY_DIRNAME;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

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

    @Bind(R.id.dirname_text)
    TextView editDirname;

    ZonedDateTime target;

    private int size = 10;

    private Store store;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private FullscreenProgressDialog progressDialog;
    private CustomImageCreator creator;

    private SharedPreferences.OnSharedPreferenceChangeListener l
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (KEY_DIRNAME.equals(key)) {
                editDirname.setText(store.dirname());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        progressDialog = new FullscreenProgressDialog(this);
        store = new Store(this);
        creator = new CustomImageCreator(this) {

            @Override
            protected int getBackgroundColor() {
                if (store.checkedBackgroundColor()) {
                    return store.backgroundColor();
                }
                return super.getBackgroundColor();
            }

            @Override
            protected int getPaintColor() {
                if (store.checkedTextColor()) {
                    return store.textColor();
                }
                return super.getPaintColor();
            }

            @Override
            protected LocalDateTime getExifLocalDateTime() {
                return target.toLocalDateTime();
            }

            @Override
            protected int getBulkCreateSize() {
                return size;
            }
        };

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

        // 枚数設定
        String[] arr = getResources().getStringArray(R.array.bulk_size);
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arr);
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

        editDirname.setText(store.dirname());

        store.pref().registerOnSharedPreferenceChangeListener(l);
    }

    private String createDateDescriptionText() {
        return formatString(target) + "の画像を" + size + "枚作成します";
    }

    private String formatString(ZonedDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.getDefault());
        return localDateTime.format(formatter);
    }

    @OnClick(R.id.edit_created_sample_images_date)
    void onClickChangeImageDateButton() {
        // Show date picker
        DatePickerDialog dialog = new DatePickerDialog(this, R.style.Theme_SampleImage_Dialog_Alert,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Log.d(MainActivity.class.getSimpleName(), "onDateSet: year(" + year + ") monthOfYear(" + monthOfYear + ") dayOfMonth(" + dayOfMonth + ")");
                        target = target.withYear(year).withMonth(monthOfYear + 1).withDayOfMonth(dayOfMonth);
                        dateTextView.setText(createDateDescriptionText());
                    }
                },
                target.getYear(), target.getMonthValue() - 1, target.getDayOfMonth());
        dialog.show();
    }

    @OnClick(R.id.edit_created_sample_images_time)
    void onClickChangeImageTimeButton() {
        // Show time picker
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                target = target.withHour(hourOfDay).withMinute(minute);
                dateTextView.setText(createDateDescriptionText());
            }
        }, target.getHour(), target.getMinute(), true);
        timePickerDialog.show();
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

    @Override
    protected void onDestroy() {
        store.pref().unregisterOnSharedPreferenceChangeListener(l);
        progressDialog.dismiss();
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void createImage() {
        progressDialog.show();

        subscriptions.add(creator.bulkCreate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<String, Uri>>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "作成が完了しました", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "エラーが発生しました", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Pair<String, Uri> stringUriPair) {
                    }
                }));
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

    @OnClick(R.id.edit_created_sample_images_background_color)
    void onClickChangeTextColorButton() {
        showColorPickerDialog(findViewById(R.id.background_color_preview));
    }

    @OnClick(R.id.edit_created_sample_images_text_color)
    void onClickChangeBackgroundColorButton() {
        showColorPickerDialog(findViewById(R.id.text_color_preview));
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
                            view.setBackgroundColor(color);
                            if (view.getId() == R.id.background_color_preview) {
                                store.backgroundColor(color);
                            } else {
                                store.textColor(color);
                            }
                        } else {
                            // Canceled
                        }
                    }
                })
                .build()
                .show(MainActivity.this.getSupportFragmentManager(), "showColorPickerDialog");
    }

    @OnClick(R.id.edit_dirname)
    void onClickEditDirname() {
        ViewGroup vg = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.edit_dirname, null);
        final EditText et = (EditText) vg.findViewById(R.id.editor);
        et.setText(store.dirname());
        new AlertDialog.Builder(this)
                .setTitle("保存先のディレクトリ名を入力してください")
                .setView(vg)
                .setPositiveButton("設定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        store.dirname(et.getText().toString());
                    }
                })
                .setNegativeButton("キャンセル", null)
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        createRandomAtOneDay();
        return true;
    }

    // 15枚
    private void createRandomAtOneDay() {
        DateTimeImageCreator d = new DateTimeImageCreator(this);
        d.setData(Arrays.asList(
                randomTime(), randomTime(), randomTime(), randomTime(),
                randomTime(), randomTime(), randomTime(), randomTime(),
                randomTime(), randomTime(), randomTime(), randomTime(),
                randomTime(), randomTime(), randomTime(), randomTime()));
        progressDialog.show();
        subscriptions.add(d.bulkCreate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<String, Uri>>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "作成が完了しました", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "エラーが発生しました", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Pair<String, Uri> stringUriPair) {
                    }
                }));
    }

    private ZonedDateTime randomTime() {
        Random r = new Random();
        int hours = r.nextInt(24);
        int minutes = r.nextInt(60);
        return target.withHour(hours).withMinute(minutes);
    }
}
