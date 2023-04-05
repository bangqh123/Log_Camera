package com.example.logbook_camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    int currentIndex = 0;
    final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    ImageCapture imageCapture;
    Button Photo, Next, Back;
    PreviewView Cam;
    ImageView Image;
    TextView Mess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findAllElements();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            int REQUEST_CODE_PERMISSIONS = 101;
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        setImage();
        setPhoto();
        setNext();
        setBack();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void startCamera()  {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                preview.setSurfaceProvider(Cam.getSurfaceProvider());
                Camera camera = cameraProvider.bindToLifecycle((this), cameraSelector, preview, imageCapture);
            } catch (InterruptedException | ExecutionException e) {
                Toast.makeText(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void setBack() {
        Back.setOnClickListener(v -> {
            currentIndex --;
            setImage();
            displayMessage(getImage().get(currentIndex));
            setAnimationRight();
        });
    }

    private void setNext() {
        Next.setOnClickListener(v -> {
            currentIndex ++;
            setImage();
            displayMessage(getImage().get(currentIndex));
            setAnimationLeft();
        });
    }

    private void setPhoto() {
        Photo.setOnClickListener(v -> {
            long timestamp = System.currentTimeMillis();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

            ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues).build();

            imageCapture.takePicture(outputFileOptions,getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        List<String> imageAbsolutePaths = getImage();
                        Glide.with(MainActivity.this).load(imageAbsolutePaths.get(0)).centerCrop().into(Image);
                        displayMessage(outputFileResults.getSavedUri().getPath());
                        Toast.makeText(MainActivity.this, "Photo has been saved successfully. "
                                        +imageAbsolutePaths.size()+"@"+ outputFileResults.getSavedUri().getPath(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            );
        });
    }

    private void setImage() {
        List<String> imagePaths = getImage();
        final int count = getImage().size();
         if (currentIndex >= count){
            currentIndex = 0;
        } else if (currentIndex < 0) {
            currentIndex = count -1;
        } if (count > 0) {
            Glide.with(this).load(imagePaths.get(currentIndex)).centerCrop().into(Image);
        }
    }

    private List<String> getImage() {
        final List<String> paths = new ArrayList();
        final Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String[] projection = { MediaStore.MediaColumns.DATA,MediaStore.Images.Media.BUCKET_DISPLAY_NAME };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";
        final Cursor cursor = this.getContentResolver().query(uri, projection, null,null, orderBy);
        final int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            final String absolutePathOfImage = cursor.getString(column_index_data);
            paths.add(absolutePathOfImage);
        }
        cursor.close();
        return paths;
    }

    private void displayMessage(String message) {
        Mess.setText(message);
    }
//
    private void setAnimationLeft() {
        Animation left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Image.setAnimation(left);
    }

    private void setAnimationRight() {
        Animation right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        Image.setAnimation(right);
    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void findAllElements() {
        Photo = findViewById(R.id.btnPhoto);
        Next = findViewById(R.id.btnNext);
        Back = findViewById(R.id.btnBack);
        Mess = findViewById(R.id.ilMess);
        Image = findViewById(R.id.ivImage);
        Cam = findViewById(R.id.pvCam);
    }
}