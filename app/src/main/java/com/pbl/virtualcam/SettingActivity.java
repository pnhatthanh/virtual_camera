package com.pbl.virtualcam;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingActivity extends AppCompatActivity {
    private final String[] orientations = {"Phong cảnh", "Chân dung"};
    private final String[] sizes={"640*480","960*540","1280*720", "1920*1080"};
    private final String[] listQuality={"Auto","Thấp","Trung bình", "Cao"};
    private int selectedOrientationIndex = 0;
    private int selectedSize=0;
    private int selectedQuality=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout videoSize = findViewById(R.id.tv_video_size);
        videoSize.setOnClickListener(e-> showSizeDialog());

        LinearLayout tvVideoOrientation= findViewById(R.id.tv_video_orientation);
        tvVideoOrientation.setOnClickListener(e-> showOrientationDialog());



    }
    private void showOrientationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Định hướng video");
        builder.setSingleChoiceItems(orientations, selectedOrientationIndex, (dialog, which) -> {
            selectedOrientationIndex = which;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            TextView tvSelectedOrientation = findViewById(R.id.tv_selected_orientation);
            tvSelectedOrientation.setText(orientations[selectedOrientationIndex]);
            dialog.dismiss();
            Toast.makeText(this, "Đã chọn: " + orientations[selectedOrientationIndex], Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showSizeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kích cỡ video");
        builder.setSingleChoiceItems(sizes, selectedSize, (dialog, which) -> {
            selectedSize = which;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            TextView tvSelectedOrientation = findViewById(R.id.tv_selected_size);
            tvSelectedOrientation.setText(sizes[selectedSize]);
            dialog.dismiss();
            Toast.makeText(this, "Đã chọn: " + sizes[selectedSize], Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showQuality(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chất lượng video");
        builder.setSingleChoiceItems(listQuality, selectedQuality, (dialog, which) -> {
            selectedQuality = which;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            TextView tvSelectedOrientation = findViewById(R.id.tv_selected_size);
            tvSelectedOrientation.setText(sizes[selectedSize]);
            dialog.dismiss();
            Toast.makeText(this, "Đã chọn: " + sizes[selectedSize], Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}