package com.promlert.guessmygrade;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.promlert.guessmygrade.db.StudentsDAO;

import java.util.ArrayList;

public class StudentListActivity extends AppCompatActivity {

    private static final String TAG = StudentListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        final ListView studentsDataListView = (ListView) findViewById(R.id.students_data_list_view);
        ArrayList studentList = new StudentsDAO(this).selectAllStudents();

        StudentListAdapter adapter = new StudentListAdapter(
                this,
                R.layout.student_item_layout,
                studentList
        );
        studentsDataListView.setAdapter(adapter);
    }

    private static class StudentListAdapter extends ArrayAdapter<StudentsDAO.Student> {

        private static final String TAG = StudentListAdapter.class.getSimpleName();

        private Context mContext;
        private int mLayoutResId;
        private ArrayList<StudentsDAO.Student> mStudentList;

        public StudentListAdapter(Context context, int layoutResId, ArrayList<StudentsDAO.Student> studentList) {
            super(context, layoutResId, studentList);
            mContext = context;
            mLayoutResId = layoutResId;
            mStudentList = studentList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemLayout = convertView;
            ViewHolder holder;

            if (itemLayout == null) {
                itemLayout = View.inflate(mContext, mLayoutResId, null);

                TextView studentIdTextView = (TextView) itemLayout.findViewById(R.id.student_id_text_view);
                TextView nameTextView = (TextView) itemLayout.findViewById(R.id.name_text_view);
                ImageView gradeImageView = (ImageView) itemLayout.findViewById(R.id.grade_image_view);

                holder = new ViewHolder(studentIdTextView, nameTextView, gradeImageView);
                itemLayout.setTag(holder);
            }

            holder = (ViewHolder) itemLayout.getTag();
            holder.studentIdTextView.setText(mStudentList.get(position).studentId);
            holder.nameTextView.setText(mStudentList.get(position).name + " [ " + mStudentList.get(position).grade + " ]");
            holder.gradeImageView.setImageDrawable(
                    ShowGradeActivity.loadGradeImageAssetAsDrawable(
                            getContext(),
                            mStudentList.get(position).grade
                    )
            );
            if ("a".equalsIgnoreCase(mStudentList.get(position).grade)) {
                itemLayout.setBackgroundColor(Color.YELLOW);
            } else {
                itemLayout.setBackgroundColor(Color.TRANSPARENT);
            }

            return itemLayout;
        }

        private static class ViewHolder {
            public final TextView studentIdTextView;
            public final TextView nameTextView;
            public final ImageView gradeImageView;

            public ViewHolder(TextView studentIdTextView, TextView nameTextView, ImageView gradeImageView) {
                this.studentIdTextView = studentIdTextView;
                this.nameTextView = nameTextView;
                this.gradeImageView = gradeImageView;
            }
        }
    }
}
