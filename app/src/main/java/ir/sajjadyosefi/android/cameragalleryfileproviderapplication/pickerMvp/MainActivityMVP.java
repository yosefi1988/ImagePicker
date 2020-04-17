package ir.sajjadyosefi.android.cameragalleryfileproviderapplication.pickerMvp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ir.sajjadyosefi.android.cameragalleryfileproviderapplication.BuildConfig;
import ir.sajjadyosefi.android.cameragalleryfileproviderapplication.R;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;


public class MainActivityMVP extends AppCompatActivity implements ImageContract.View {

    static final int REQUEST_TAKE_PHOTO = 101;
    static final int REQUEST_GALLERY_PHOTO = 102;
    static String[] permissions = new String[] {
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.user_profile_photo)
    ImageButton userProfilePhoto;
    @BindView(R.id.user_profile_name)
    TextView userProfileName;
    @BindView(R.id.user_profile_short_bio)
    TextView userProfileShortBio;
    private ImagePresenter mPresenter;
    Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mvp);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        mPresenter = new ImagePresenter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startCamera(File file) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if (file != null) {
                photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void chooseGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                mPresenter.showPreview(photoURI);
            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                Uri selectedImage = data.getData();
                String mPhotoPath = getRealPathFromUri(selectedImage);
                mPresenter.showPreview(mPhotoPath);
            }
        }
    }

    @Override
    public boolean checkPermission() {
        for (String mPermission : permissions) {
            int result = ActivityCompat.checkSelfPermission(this, mPermission);
            if (result == PackageManager.PERMISSION_DENIED) return false;
        }
        return true;
    }

    @Override
    public void showPermissionDialog() {
        Dexter.withActivity(this).withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(error -> showErrorDialog())
                .onSameThread()
                .check();
    }

    @Override
    public File getFilePath() {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_need_permission));
        builder.setMessage(getString(R.string.message_grant_permission));
        builder.setPositiveButton(getString(R.string.label_setting), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    public void showNoSpaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_message_no_more_space));
        builder.setMessage(getString(R.string.error_message_insufficient_space));
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public int availableDisk() {
        File mFilePath = getFilePath();
        long freeSpace = mFilePath.getFreeSpace();
        return Math.round(freeSpace / 1048576);
    }

    @Override
    public File newFile() {
        Calendar cal = Calendar.getInstance();
        long timeInMillis = cal.getTimeInMillis();
        String mFileName = String.valueOf(timeInMillis) + ".jpeg";
        File mFilePath = getFilePath();
        try {
            File newFile = new File(mFilePath.getAbsolutePath(), mFileName);
            newFile.createNewFile();
            return newFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void showErrorDialog() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_message), Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void displayImagePreview(String mFilePath) {
        Glide.with(MainActivityMVP.this)
                .load(mFilePath)
                .apply(new RequestOptions().centerCrop().circleCrop().placeholder(R.drawable.ic_launcher_background))
                .into(userProfilePhoto);
    }

    @Override
    public void displayImagePreview(Uri mFileUri) {
        Glide.with(MainActivityMVP.this)
                .load(mFileUri)
                .apply(new RequestOptions().centerCrop().circleCrop().placeholder(R.drawable.ic_launcher_background))
                .into(userProfilePhoto);
    }

    /**
     * Get real file path from URI
     */
    @Override
    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void selectImage() {
        final CharSequence[] items = {
                getString(R.string.take_photo), getString(R.string.choose_gallery),
                getString(R.string.cancel)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityMVP.this);
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Take Photo")) {
                mPresenter.cameraClick();
            } else if (items[item].equals("Choose from Gallery")) {
                mPresenter.ChooseGalleryClick();
            } else if (items[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @OnClick({ R.id.user_profile_photo, R.id.user_profile_name, R.id.user_profile_short_bio })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.user_profile_photo:
                selectImage();
                break;
            case R.id.user_profile_name:
                Intent browserIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_androidwave)));
                startActivity(browserIntent);
                break;
            case R.id.user_profile_short_bio:
                Intent mIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_androidwave_android)));
                startActivity(mIntent);
                break;
        }
    }
}