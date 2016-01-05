package com.promlert.guessmygrade;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ShowGradeActivity extends AppCompatActivity {

    private static final String TAG = ShowGradeActivity.class.getSimpleName();
    private static final String REQUIRED_FACEBOOK_PERMISSION = "publish_actions";

    private enum PendingAction {
        NONE, POST_PICTURE
    }
    private PendingAction mPendingAction = PendingAction.NONE;

    private CallbackManager mCallbackManager;
    private ProfileTracker mProfileTracker;

    private String mStudentId, mGrade;

    private ProfilePictureView mFacebookProfilePicture;
    private View mLoggedInLayout, mNotLoggedInLayout;
    private ImageView mGradeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFacebookIntegration(); // must be called before setContentView()
        setContentView(R.layout.activity_show_grade);

        Intent i = getIntent();
        mStudentId = i.getStringExtra(GuessActivity.KEY_EXTRA_STUDENT_ID);
        mGrade = i.getStringExtra(GuessActivity.KEY_EXTRA_GRADE);

        setupViews();
    }

    private void setupFacebookIntegration() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handlePendingAction();
                updateUI();
            }

            @Override
            public void onCancel() {
                updateUI();
            }

            @Override
            public void onError(FacebookException error) {
                updateUI();
            }
        });

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
            }
        };
    }

    private void setupViews() {
        mLoggedInLayout = findViewById(R.id.logged_in_layout);
        mNotLoggedInLayout = findViewById(R.id.not_logged_in_layout);
        mFacebookProfilePicture = (ProfilePictureView) findViewById(R.id.facebook_profile_picture);

        String title = "===== ใช่แล้ว =====\n\nรหัส " + mStudentId + " ได้เกรด " + mGrade;
        TextView titleTextView = (TextView) findViewById(R.id.title_text_view);
        titleTextView.setText(title);

        mGradeImageView = (ImageView) findViewById(R.id.grade_image_view);
        Drawable drawable = loadGradeImageAssetAsDrawable(this, mGrade);
        mGradeImageView.setImageDrawable(drawable);

        Button shareFacebookButton = (Button) findViewById(R.id.share_facebook_button);
        shareFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPublish(PendingAction.POST_PICTURE);
            }
        });
    }

    @Nullable
    public static Drawable loadGradeImageAssetAsDrawable(Context context, String grade) {
        Drawable drawable = null;
        String imageFilename = "grade_" + grade.toLowerCase().replace("+", "_plus").concat(".jpg");
        try {
            InputStream is = context.getAssets().open(imageFilename);
            drawable = Drawable.createFromStream(is, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return drawable;
    }

    private void updateUI() {
        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;
        Profile profile = Profile.getCurrentProfile();

        if (loggedIn && (profile != null)) {
            mFacebookProfilePicture.setProfileId(profile.getId());
            mLoggedInLayout.setVisibility(View.VISIBLE);
            mNotLoggedInLayout.setVisibility(View.GONE);
        } else {
            mFacebookProfilePicture.setProfileId(null);
            mLoggedInLayout.setVisibility(View.GONE);
            mNotLoggedInLayout.setVisibility(View.VISIBLE);
        }
    }

    private void performPublish(PendingAction action) {
        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token != null) {
            mPendingAction = action;
            handlePendingAction();
        }
    }

    private void handlePendingAction() {
        PendingAction oldAction = mPendingAction;
        mPendingAction = PendingAction.NONE;

        switch (oldAction) {
            case NONE:
                break;
            case POST_PICTURE:
                postGradePicture();
                break;
        }
    }

    private void postGradePicture() {
        Profile profile = Profile.getCurrentProfile();

        if (profile != null && hasPublishPermission()) {
            Bitmap gradeBitmap = drawableToBitmap(mGradeImageView.getDrawable());
            SharePhoto share =  new SharePhoto.Builder()
                    .setBitmap(gradeBitmap)
                    .build();

            ArrayList<SharePhoto> shareList = new ArrayList<>();
            shareList.add(share);

            SharePhotoContent content = new SharePhotoContent.Builder()
                    .setPhotos(shareList)
                    .build();
            ShareApi.share(content, shareCallback);
        } else {
            mPendingAction = PendingAction.POST_PICTURE;
            LoginManager.getInstance().logInWithPublishPermissions(
                    this,
                    Arrays.asList(REQUIRED_FACEBOOK_PERMISSION)
            );
        }
    }

    private boolean hasPublishPermission() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return token != null && token.getPermissions().contains(REQUIRED_FACEBOOK_PERMISSION);
    }

    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result result) {
            if (result.getPostId() != null) {
                Toast.makeText(
                        ShowGradeActivity.this,
                        "แชร์ผลเกรดไปยัง Facebook แล้ว",
                        Toast.LENGTH_LONG
                ).show();
            }
        }

        @Override
        public void onError(FacebookException error) {
            new AlertDialog.Builder(ShowGradeActivity.this)
                    .setTitle("Error")
                    .setMessage(error.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }

        @Override
        public void onCancel() { }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfileTracker.stopTracking();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(
                    1,
                    1,
                    Bitmap.Config.ARGB_8888
            ); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
