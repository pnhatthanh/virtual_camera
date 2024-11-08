package com.pbl.virtualcam;

import static android.Manifest.permission.CAMERA;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE=100;
    private TextureView textureView;
    private boolean isFrontCamera;
    private CameraCaptureSession myCameraCaptureSession;
    private String cameraID;
    private CameraManager cameraManager;
    private CameraDevice myCameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private Integer sensorOrientation = 0;
    private boolean isPlay=true;
    private SettingStorage setting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });
        setting=new SettingStorage(this);
        if (ActivityCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            initializeCamera();
        }
        Button switchCameraButton=findViewById(R.id.switch_camera);
        switchCameraButton.setOnClickListener(e-> switchCamera());

        Button stopButton=findViewById(R.id.stop);
        stopButton.setOnClickListener(e->{
            isPlay=!isPlay;
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            if (isPlay) {
                stopButton.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_play));
                toast.setText("Đang phát video");
            } else {
                stopButton.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_stop));
                toast.setText("Đã dừng phát video");
            }
            toast.show();
        });
        Button settingButton=findViewById(R.id.setting_btn);
        settingButton.setOnClickListener(e->{
            Intent intent=new Intent(CameraActivity.this,SettingActivity.class);
            startActivity(intent);
        });

        new Thread(()->{
            try{
                new SocketManager(8888);
            }catch(Exception e) {
                Toast.makeText(this, "Kết nối thất bại!", Toast.LENGTH_SHORT).show();
            }
        }).start();
    }

    private void initializeCamera() {
        textureView = findViewById(R.id.view);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        textureView.setSurfaceTextureListener(textureListener);
        try {
            if(setting.GetValue(ValueSetting.Orientation,"Phong cảnh").equals("Phong cảnh")){
                cameraID = cameraManager.getCameraIdList()[0];
                isFrontCamera=false;
            }else{
                cameraID = cameraManager.getCameraIdList()[1];
                isFrontCamera=true;
            }
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
            sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void switchCamera() {
        if (myCameraDevice != null) {
            myCameraDevice.close();
        }
        try {
            isFrontCamera=!isFrontCamera;
            if(isFrontCamera){
                cameraID =cameraManager.getCameraIdList()[1];
                setting.SetValue(ValueSetting.Orientation,"Chân dung");
            }else{
                cameraID =cameraManager.getCameraIdList()[0];
                setting.SetValue(ValueSetting.Orientation,"Phong cảnh");
            }
            CameraCharacteristics characteristics= cameraManager.getCameraCharacteristics(cameraID);
            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startCamera();
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
            startCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {}
    };

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            myCameraDevice = cameraDevice;
            StartCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            myCameraDevice.close();
            myCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            myCameraDevice.close();
            myCameraDevice = null;
        }
    };

    private void startCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraID, stateCallback, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void StartCameraPreview() {
        String[] size=setting.GetValue(ValueSetting.Size,"640*480").split("\\*");
        int height=Integer.parseInt(size[0]);
        int width=Integer.parseInt(size[1]);
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(width, height);
        Surface surface = new Surface(surfaceTexture);

        imageReader=ImageReader.newInstance(width, height, ImageFormat.JPEG, 2);
        Surface imageReaderSurface=imageReader.getSurface();

        imageReader.setOnImageAvailableListener(reader ->{
            Image image = reader.acquireLatestImage();
            if (image != null){
                ByteBuffer buffer= image.getPlanes()[0].getBuffer();
                byte[] bytes= new byte[buffer.remaining()];
                buffer.get(bytes);
                if(isPlay){
                    SocketManager.timeStamp = new Date().getTime();
                    SocketManager.bytes = compressAndProcessImage(bytes);
                }
                image.close();
            }
        }, null);

        try {
            captureRequestBuilder= myCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.addTarget(imageReaderSurface);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                OutputConfiguration outputConfiguration = new OutputConfiguration(surface);
                OutputConfiguration imageOutput = new OutputConfiguration(imageReaderSurface);

                SessionConfiguration sessionConfiguration = new SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        Arrays.asList(outputConfiguration, imageOutput),
                        getMainExecutor(),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                myCameraCaptureSession = cameraCaptureSession;
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                                try {
                                    myCameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                                } catch (CameraAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                myCameraCaptureSession = null;
                            }
                        });
                myCameraDevice.createCaptureSession(sessionConfiguration);
            } else {
                myCameraDevice.createCaptureSession(Arrays.asList(surface, imageReaderSurface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        myCameraCaptureSession = cameraCaptureSession;
                        try {
                            myCameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        myCameraCaptureSession = null;
                    }
                }, null);
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera();
            }
        }
    }

    private byte[] compressAndProcessImage(byte[] imageBytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        switch (setting.GetValue(ValueSetting.Quality,"Trung bình")){
            case "Thấp":
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                break;
            case "Trung bình":
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
                break;
            case "Cao":
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                break;
        }
        return byteArrayOutputStream.toByteArray();
    }
//    private Bitmap rotateBitmap(Bitmap bitmap, Integer orientation) {
//        Matrix matrix = new Matrix();
//        if(isFrontCamera) {
//            matrix.preScale(1.0f, -1.0f);
//        }
//        matrix.postRotate(orientation);
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//    }

    private void closeCamera() {
        if (myCameraCaptureSession != null) {
            myCameraCaptureSession.close();
            myCameraCaptureSession = null;
        }
        if (myCameraDevice != null) {
            myCameraDevice.close();
            myCameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }
}