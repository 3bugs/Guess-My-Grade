package com.promlert.guessmygrade.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Promlert on 12/28/2015.
 */
public class StudentsDAO {

    private static final String TAG = StudentsDAO.class.getSimpleName();

    private static final String DATABASE_NAME = "students.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "students";
    public static final String COL_ID = "_id";
    public static final String COL_STUDENT_ID = "student_id";
    public static final String COL_NAME = "name";
    public static final String COL_GRADE = "grade";

    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_STUDENT_ID + " TEXT, "
            + COL_NAME + " TEXT, "
            + COL_GRADE + " TEXT"
            + ")";

    private Context mContext;
    private static DatabaseHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    public StudentsDAO(Context context) {
        context = context.getApplicationContext();
        mContext = context;

        if (mDbHelper == null) {
            mDbHelper = new DatabaseHelper(context);
        }
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public Student selectStudentById(String studentId) {
        Cursor cursor = mDatabase.query(
                TABLE_NAME,
                null,
                COL_STUDENT_ID + "=?",
                new String[]{studentId},
                null,
                null,
                null
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            final String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
            final String grade = cursor.getString(cursor.getColumnIndex(COL_GRADE));

            Student student = new Student(studentId, name, grade);
            return student;
        }
        return null;
    }

    public ArrayList<Student> selectAllStudents() {
        ArrayList<Student> studentList = new ArrayList<>();

        Cursor cursor = mDatabase.query(TABLE_NAME, null, COL_GRADE + "!=?", new String[]{"W"}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String studentId = cursor.getString(cursor.getColumnIndex(COL_STUDENT_ID));
                String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                String grade = cursor.getString(cursor.getColumnIndex(COL_GRADE));

                Student student = new Student(studentId, name, grade);
                studentList.add(student);
            }
            cursor.close();
        }
        return studentList;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String FILENAME = "data.csv";
        private Context mContext;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE);
            insertStudentsData(db);
        }

        private void insertStudentsData(SQLiteDatabase db) {
            AssetManager am = mContext.getAssets();
            String line;
            try {
                InputStream is = am.open(FILENAME);
                InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);

                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    ContentValues cv = new ContentValues();
                    cv.put(COL_STUDENT_ID, parts[0]);
                    cv.put(COL_NAME, parts[1]);
                    cv.put(COL_GRADE, parts[2]);
                    long insertResult = db.insert(TABLE_NAME, null, cv);

                    if (insertResult == -1) {
                        Log.e(TAG, "Error insert student data: "
                                + parts[0] + ", " + parts[1] + ", " + parts[2]);
                    } else {
                        Log.i(TAG, "Insert student data successfully: "
                                + parts[0] + ", " + parts[1] + ", " + parts[2]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error opening data file: " + FILENAME);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            return;
        }
    }

    public static class Student {
        public final String studentId;
        public final String name;
        public final String grade;

        public Student(String studentId, String name, String grade) {
            this.studentId = studentId;
            this.name = name;
            this.grade = grade;
        }
    }
}
