package com.promlert.guessmygrade;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.promlert.guessmygrade.db.StudentsDAO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuessActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = GuessActivity.class.getSimpleName();
    private static final int DELAY_IN_SECOND = 2;
    protected static final String KEY_EXTRA_STUDENT_ID = "extra_student_id";
    protected static final String KEY_EXTRA_GRADE = "extra_grade";

    private StudentsDAO mStudentsDAO;
    private StudentsDAO.Student mCurrentStudent;

    private final List<String> mOrderedGradeList = Arrays.asList(
            new String[]{"f", "d", "d+", "c", "c+", "b", "b+", "a"}
    );
    private final ArrayList<TextView> mGradeTextViewList = new ArrayList<>();
    private final ArrayList<Integer> mColorList = new ArrayList<>();

    private TextView mSelectedGradeTextView;
    private View mProgressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess);
        setupViews();

        Intent i = getIntent();
        String studentId = i.getStringExtra(KEY_EXTRA_STUDENT_ID);

        mStudentsDAO = new StudentsDAO(this);
        mCurrentStudent = mStudentsDAO.selectStudentById(studentId);

        TextView studentInfoTextView = (TextView) findViewById(R.id.student_info_text_view);
        studentInfoTextView.setText(mCurrentStudent.studentId + "\n" + mCurrentStudent.name);
    }

    private void setupViews() {
        mProgressLayout = findViewById(R.id.progress_layout);
        mProgressLayout.setVisibility(View.GONE);

        mGradeTextViewList.add((TextView) findViewById(R.id.grade_a_text_view));
        mGradeTextViewList.add((TextView) findViewById(R.id.grade_b_plus_text_view));
        mGradeTextViewList.add((TextView) findViewById(R.id.grade_b_text_view));
        mGradeTextViewList.add((TextView) findViewById(R.id.grade_c_plus_text_view));
        mGradeTextViewList.add((TextView) findViewById(R.id.grade_c_text_view));
        mGradeTextViewList.add((TextView) findViewById(R.id.grade_d_plus_text_view));
        mGradeTextViewList.add((TextView) findViewById(R.id.grade_d_text_view));
        mGradeTextViewList.add((TextView) findViewById(R.id.grade_f_text_view));

        for (TextView tv : mGradeTextViewList) {
            tv.setOnClickListener(this);
            // Keep background color as a tag.
            tv.setTag(((ColorDrawable) tv.getBackground()).getColor());
        }
    }

    @Override
    public void onClick(View v) {
        final TextView clickedTextView = (TextView) v;

        if (!isSelected(clickedTextView)) {
            deselectAll();
            select(clickedTextView);
        }

        mProgressLayout.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressLayout.setVisibility(View.GONE);

                        String guessGrade = clickedTextView.getText().toString().toLowerCase().trim();
                        String actualGrade = mCurrentStudent.grade.toLowerCase().trim();

                        int guessGradeIndex = mOrderedGradeList.indexOf(guessGrade);
                        int actualGradeIndex = mOrderedGradeList.indexOf(actualGrade);

                        showResult(guessGrade.toUpperCase(), guessGradeIndex - actualGradeIndex);
                    }
                });
            }
        }, DELAY_IN_SECOND * 1000);
    }

    private void showResult(String gradeToShow, int result) {
        View dialogLayout = View.inflate(this, R.layout.result, null);
        TextView resultTextView = (TextView) dialogLayout.findViewById(R.id.result_text_view);

        if (result == 0) {
            Intent i = new Intent(GuessActivity.this, ShowGradeActivity.class);
            i.putExtra(KEY_EXTRA_STUDENT_ID, mCurrentStudent.studentId);
            i.putExtra(KEY_EXTRA_GRADE, gradeToShow);
            startActivity(i);
            return;
        } else if (result > 0) {
            resultTextView.setText("มากไป");
        } else if (result < 0) {
            resultTextView.setText("น้อยไป");
        }

        new AlertDialog.Builder(this)
                .setTitle("เกรด " + gradeToShow)
                .setView(dialogLayout)
                .show();
    }

    private void deselectAll() {
        for (TextView tv : mGradeTextViewList) {
            deselect(tv);
        }
    }

    private void select(TextView selectedTextView) {
        //Toast.makeText(this, selectedTextView.getText().toString(), Toast.LENGTH_SHORT).show();
        mSelectedGradeTextView = selectedTextView;

        final GradientDrawable drawable = new GradientDrawable();
        drawable.setStroke(10, Color.DKGRAY);
        drawable.setColor(((Integer) selectedTextView.getTag()).intValue());
        selectedTextView.setBackgroundDrawable(drawable);
    }

    private void deselect(TextView selectedTextView) {
        mSelectedGradeTextView = null;

        final GradientDrawable drawable = new GradientDrawable();
        drawable.setStroke(0, Color.TRANSPARENT);
        drawable.setColor(((Integer) selectedTextView.getTag()).intValue());
        selectedTextView.setBackgroundDrawable(drawable);
    }

    private boolean isSelected(TextView gradeTextView) {
        return mSelectedGradeTextView == gradeTextView;
    }
}
