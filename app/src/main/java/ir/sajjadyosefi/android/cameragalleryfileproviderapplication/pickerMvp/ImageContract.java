package ir.sajjadyosefi.android.cameragalleryfileproviderapplication.pickerMvp;


import android.net.Uri;

import java.io.File;

/**
 * Created on : Jan 06, 2019
 * Author     : AndroidWave
 * Website    : https://androidwave.com/
 */
public class ImageContract {
    public interface View {

        boolean checkPermission();

        void showPermissionDialog();

        File getFilePath();

        void openSettings();

        void startCamera(File file);

        void chooseGallery();

        void showNoSpaceDialog();

        int availableDisk();

        File newFile();

        void showErrorDialog();

        void displayImagePreview(String mFilePath);

        void displayImagePreview(Uri mFileUri);

        String getRealPathFromUri(Uri contentUri);
    }

    interface Presenter {

        void cameraClick();

        void ChooseGalleryClick();

        void saveImage(Uri uri);

        void permissionDenied();

        void showPreview(String mFilePath);

        void showPreview(Uri mFileUri);
    }
}