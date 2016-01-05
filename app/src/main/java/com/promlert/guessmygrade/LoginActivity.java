package com.promlert.guessmygrade;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.promlert.guessmygrade.db.StudentsDAO;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int STUDENT_ID_MAX_LENGTH = 8;
    private static final String SURPRISE_ID = "12344321";

    private TextView mNumberDisplayTextView;
    private StudentsDAO mStudentsDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mStudentsDAO = new StudentsDAO(this);

        mNumberDisplayTextView = (TextView) findViewById(R.id.number_display_text_view);
        clearDisplay();

        final ArrayList<Button> digitButtonList = new ArrayList<>();
        digitButtonList.add((Button) findViewById(R.id.button1));
        digitButtonList.add((Button) findViewById(R.id.button2));
        digitButtonList.add((Button) findViewById(R.id.button3));
        digitButtonList.add((Button) findViewById(R.id.button4));
        digitButtonList.add((Button) findViewById(R.id.button5));
        digitButtonList.add((Button) findViewById(R.id.button6));
        digitButtonList.add((Button) findViewById(R.id.button7));
        digitButtonList.add((Button) findViewById(R.id.button8));
        digitButtonList.add((Button) findViewById(R.id.button9));
        digitButtonList.add((Button) findViewById(R.id.button0));

        for (Button b : digitButtonList) {
            b.setOnClickListener(this);
        }

        ((Button) findViewById(R.id.clear_button))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearDisplay();
                    }
                });
    }

    private void clearDisplay() {
        mNumberDisplayTextView.setText("");
    }

    @Override
    public void onClick(View v) {
        if (mNumberDisplayTextView.getText().toString().length() < STUDENT_ID_MAX_LENGTH) {
            String digit = ((Button) v).getText().toString();
            mNumberDisplayTextView.append(digit);

            if (mNumberDisplayTextView.getText().toString().length() == STUDENT_ID_MAX_LENGTH) {
                submitInput();
            }
        }
    }

    private void submitInput() {
        final String inputStudentId = mNumberDisplayTextView.getText().toString();

        if (SURPRISE_ID.equals(inputStudentId)) {
            Intent i = new Intent(this, StudentListActivity.class);
            startActivity(i);
            Toast.makeText(this, "Surprise Mode!", Toast.LENGTH_LONG).show();
            clearDisplay();
            return;
        }

        StudentsDAO.Student student = mStudentsDAO.selectStudentById(inputStudentId);
        if (student != null) {
            if (student.grade.equalsIgnoreCase("w")) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("รหัส " + inputStudentId)
                        .setMessage("ชื่อ-นามสกุล: " + student.name +
                                "\nได้ถอนรายวิชานี้ไปแล้ว (เกรด: W)")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearDisplay();
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("รหัส " + inputStudentId)
                        .setMessage("ชื่อ-นามสกุล: " + student.name +
                                "\n\nต้องการดูเกรดของรหัสนี้ใช่หรือไม่")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearDisplay();
                                Intent i = new Intent(LoginActivity.this, GuessActivity.class);
                                i.putExtra(GuessActivity.KEY_EXTRA_STUDENT_ID, inputStudentId);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearDisplay();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        } else {
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("ไม่พบข้อมูลรหัสนักศึกษา " + inputStudentId)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearDisplay();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }
}
