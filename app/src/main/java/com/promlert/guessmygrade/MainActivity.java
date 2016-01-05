package com.promlert.guessmygrade;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.promlert.guessmygrade.db.StudentsDAO;

public class MainActivity extends AppCompatActivity {

    private StudentsDAO mStudentsDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStudentsDAO = new StudentsDAO(this);

        EditText studentIdEditText = (EditText) findViewById(R.id.student_id_edit_text);
        studentIdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String inputStudentId = s.toString().trim();
                if (inputStudentId.length() == 8) {
                    submitInput(inputStudentId);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        studentIdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    final String inputStudentId = v.getText().toString();
                    submitInput(inputStudentId);
                }
                return true;
            }
        });
    }

    private void submitInput(final String inputStudentId) {
        StudentsDAO.Student student = mStudentsDAO.selectStudentById(inputStudentId);
        if (student != null) {
            if (student.grade.equalsIgnoreCase("w")) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("รหัส " + inputStudentId)
                        .setMessage("ชื่อ-นามสกุล: " + student.name +
                                "\nได้ถอนรายวิชานี้ไปแล้ว (เกรด: W)")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("รหัส " + inputStudentId)
                        .setMessage("ชื่อ-นามสกุล: " + student.name +
                                "\n\nต้องการดูเกรดของรหัสนี้ใช่หรือไม่")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this, GuessActivity.class);
                                i.putExtra(GuessActivity.KEY_EXTRA_STUDENT_ID, inputStudentId);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("ไม่พบข้อมูลรหัสนักศึกษา " + inputStudentId)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
