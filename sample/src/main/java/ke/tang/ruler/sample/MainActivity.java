package ke.tang.ruler.sample;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import ke.tang.ruler.ClickableMarker;
import ke.tang.ruler.DrawableMarker;
import ke.tang.ruler.Marker;
import ke.tang.ruler.MoneyRulerValueFormatter;
import ke.tang.ruler.OnRulerValueChangeListener;
import ke.tang.ruler.RulerView;

/**
 * Created by tangke on 2018/6/14.
 */

public class MainActivity extends AppCompatActivity {
    private final static int[] MARKER_ASSETS = new int[]{R.drawable.ic_backpack,
            R.drawable.ic_backpack_2,
            R.drawable.ic_bill,
            R.drawable.ic_bookmark,
            R.drawable.ic_bookshelf,
            R.drawable.ic_briefcase,
            R.drawable.ic_bus,
            R.drawable.ic_calc,
            R.drawable.ic_candy,
            R.drawable.ic_car,
            R.drawable.ic_chalkboard,
            R.drawable.ic_clock,
            R.drawable.ic_cloud_check,
            R.drawable.ic_cloud_down,
            R.drawable.ic_cloud_error,
            R.drawable.ic_cloud_refresh,
            R.drawable.ic_cloud_up,
            R.drawable.ic_donut,
            R.drawable.ic_drop,
            R.drawable.ic_eye,
            R.drawable.ic_flag,
            R.drawable.ic_glasses,
            R.drawable.ic_glove,
            R.drawable.ic_hamburger,
            R.drawable.ic_hand,
            R.drawable.ic_hotdog,
            R.drawable.ic_knife,
            R.drawable.ic_label,
            R.drawable.ic_map,
            R.drawable.ic_map2,
            R.drawable.ic_marker,
            R.drawable.ic_mcfly,
            R.drawable.ic_medicine,
            R.drawable.ic_mountain,
            R.drawable.ic_muffin,
            R.drawable.ic_open_letter,
            R.drawable.ic_packman,
            R.drawable.ic_paper_plane,
            R.drawable.ic_photo_2,
            R.drawable.ic_piggy,
            R.drawable.ic_pin,
            R.drawable.ic_pizza,
            R.drawable.ic_r2d2,
            R.drawable.ic_rocket,
            R.drawable.ic_sale,
            R.drawable.ic_skull,
            R.drawable.ic_speakers,
            R.drawable.ic_store,
            R.drawable.ic_tactic,
            R.drawable.ic_toaster,
            R.drawable.ic_train,
            R.drawable.ic_watch,
            R.drawable.ic_www};

    private DefaultState mState;

    private TextView mResult;
    private RulerView mRuler;
    private SeekBar mStepWidth;
    private Spinner mScaleColor;
    private Spinner mRulerColor;
    private SeekBar mSectionScaleCount;
    private RadioGroup mIndicators;
    private SeekBar mScaleMinHeight;
    private SeekBar mScaleMaxHeight;
    private SeekBar mScaleSize;
    private SeekBar mRulerSize;
    private SeekBar mMaxValue;
    private SeekBar mMinValue;
    private EditText mValue;
    private Button mSubmitValue;
    private SeekBar mTextSize;
    private Spinner mTextColor;
    private Spinner mFormatText;
    private Button mAddCustomMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mResult = findViewById(R.id.result);
        mRuler = findViewById(R.id.ruler);
        mRuler.setOnRulerValueChangeListener(new OnRulerValueChangeListener() {
            @Override
            public void onRulerValueChanged(int value, String displayValue) {
                mResult.setText(displayValue);
            }
        });
        mState = new DefaultState(mRuler);
        mStepWidth = findViewById(R.id.stepWidth);
        mStepWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRuler.setStepWidth((int) (mState.getStepWidth() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()) / 100 * progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mScaleColor = findViewById(R.id.scaleColor);
        mScaleColor.setAdapter(ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_dropdown_item));
        mScaleColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mRuler.setScaleColor(mState.getScaleColor());
                        break;
                    case 1:
                        mRuler.setScaleColor(Color.RED);
                        break;
                    case 2:
                        mRuler.setScaleColor(Color.BLUE);
                        break;
                    case 3:
                        mRuler.setScaleColor(Color.YELLOW);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRulerColor = findViewById(R.id.rulerColor);
        mRulerColor.setAdapter(ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_dropdown_item));
        mRulerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mRuler.setRulerColor(mState.getRulerColor());
                        break;
                    case 1:
                        mRuler.setRulerColor(Color.RED);
                        break;
                    case 2:
                        mRuler.setRulerColor(Color.BLUE);
                        break;
                    case 3:
                        mRuler.setRulerColor(Color.YELLOW);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSectionScaleCount = findViewById(R.id.sectionScaleCount);
        mSectionScaleCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRuler.setSectionScaleCount((int) (mState.getSectionScaleCount() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()) / 100 * progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mIndicators = findViewById(R.id.indicators);
        mIndicators.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.defaultIndicator:
                        mRuler.setIndicator(mState.mIndicator);
                        break;
                    case R.id.dashedLineIndicator:
                        mRuler.setIndicator(R.drawable.dashed_indicator);
                        break;
                    case R.id.arrowIndicator:
                        mRuler.setIndicator(R.drawable.arrow_indicator_tint);
                        break;
                }
            }
        });

        mScaleMinHeight = findViewById(R.id.scaleMinHeight);
        mScaleMinHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRuler.setScaleMinHeight((int) (mState.getScaleMinHeight() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()) / 100 * progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mScaleMaxHeight = findViewById(R.id.scaleMaxHeight);
        mScaleMaxHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRuler.setScaleMaxHeight((int) (mState.getScaleMaxHeight() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()) / 100 * progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mScaleSize = findViewById(R.id.scaleSize);
        mScaleSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRuler.setScaleSize((int) (mState.getScaleSize() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()) / 100 * progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mRulerSize = findViewById(R.id.rulerSize);
        mRulerSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRuler.setRulerSize((int) (mState.getScaleSize() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()) / 100 * progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mMaxValue = findViewById(R.id.maxValue);
        mMaxValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final int maxValue = mState.getMaxValue() - mState.getMaxValue() / 100 * progress;
                if (maxValue >= mRuler.getMinValue()) {
                    mRuler.setMaxValue(maxValue);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.notification_max_value_exceed, mRuler.getMaxValue()), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mMinValue = findViewById(R.id.minValue);
        mMinValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final int minValue = mState.getMinValue() + mState.getMaxValue() / 100 * progress;
                if (minValue <= mRuler.getMaxValue()) {
                    mRuler.setMinValue(minValue);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.notification_min_value_exceed, mRuler.getMaxValue()), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mValue = findViewById(R.id.value);
        mSubmitValue = findViewById(R.id.submitValue);
        mSubmitValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int value = Integer.parseInt(mValue.getText().toString());
                    mRuler.setValue(value);
                } catch (Exception e) {

                }
            }
        });

        mTextSize = findViewById(R.id.textSize);
        mTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRuler.setTextSize(mState.getTextSize() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()) / 100 * progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTextColor = findViewById(R.id.textColor);
        mTextColor.setAdapter(ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_dropdown_item));
        mTextColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mRuler.setTextColor(mState.getTextColor());
                        break;
                    case 1:
                        mRuler.setTextColor(Color.RED);
                        break;
                    case 2:
                        mRuler.setTextColor(Color.BLUE);
                        break;
                    case 3:
                        mRuler.setTextColor(Color.YELLOW);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mFormatText = findViewById(R.id.formatText);
        mFormatText.setAdapter(ArrayAdapter.createFromResource(this, R.array.formatters, android.R.layout.simple_spinner_dropdown_item));
        mFormatText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mRuler.setRulerValueFormatter(null);
                        break;
                    case 1:
                        mRuler.setRulerValueFormatter(new MoneyRulerValueFormatter());
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mAddCustomMarker = findViewById(R.id.customMarker);
        mAddCustomMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawableMarker marker = new DrawableMarker(MainActivity.this, MARKER_ASSETS[(int) (Math.random() * MARKER_ASSETS.length)], mRuler.getValue());
                marker.setOnMarkerClickListener(new ClickableMarker.OnMarkerClickListener() {
                    @Override
                    public void onMarkerClick(Marker m) {
                        Toast.makeText(MainActivity.this, R.string.notification_marker_clicked, Toast.LENGTH_SHORT).show();
                    }
                });
                mRuler.addMarker(marker);
            }
        });
    }

    private class DefaultState {
        private int mStepWidth;
        private ColorStateList mScaleColor;
        private ColorStateList mRulerColor;
        private int mSectionScaleCount;
        private Drawable mIndicator;
        private int mScaleMinHeight;
        private int mScaleMaxHeight;
        private int mScaleSize;
        private int mMaxValue;
        private int mMinValue;
        private int mValue;
        private float mTextSize;
        private ColorStateList mTextColor;

        public DefaultState(RulerView ruler) {
            mStepWidth = ruler.getStepWidth();
            mScaleColor = ruler.getScaleColor();
            mRulerColor = ruler.getRulerColor();
            mSectionScaleCount = ruler.getSectionScaleCount();
            mIndicator = ruler.getIndicator();
            mScaleMinHeight = ruler.getScaleMinHeight();
            mScaleMaxHeight = ruler.getScaleMaxHeight();
            mScaleSize = ruler.getScaleSize();
            mMaxValue = ruler.getMaxValue();
            mMinValue = ruler.getMinValue();
            mValue = ruler.getValue();
            mTextSize = ruler.getTextSize();
            mTextColor = ruler.getTextColor();
        }

        public int getStepWidth() {
            return mStepWidth;
        }

        public ColorStateList getScaleColor() {
            return mScaleColor;
        }

        public ColorStateList getRulerColor() {
            return mRulerColor;
        }

        public int getSectionScaleCount() {
            return mSectionScaleCount;
        }

        public Drawable getIndicator() {
            return mIndicator;
        }

        public int getScaleMinHeight() {
            return mScaleMinHeight;
        }

        public int getScaleMaxHeight() {
            return mScaleMaxHeight;
        }

        public int getScaleSize() {
            return mScaleSize;
        }

        public int getMaxValue() {
            return mMaxValue;
        }

        public int getMinValue() {
            return mMinValue;
        }

        public int getValue() {
            return mValue;
        }

        public float getTextSize() {
            return mTextSize;
        }

        public ColorStateList getTextColor() {
            return mTextColor;
        }
    }
}
