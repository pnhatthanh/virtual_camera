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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity {
    TextView tvOrientation;
    TextView tvQuality;
    TextView tvSize ;
    TextView tvEquipments;
    private final String[] orientations = {"Phong cảnh", "Chân dung"};
    private final String[] sizes={"640*480","960*540", "1920*1080"};
    private final String[] listQuality={"Auto","Thấp","Trung bình", "Cao"};
    private int selectedOrientation;
    private int selectedSize;
    private int selectedQuality;
    SettingStorage setting;

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
        setting=new SettingStorage(this);
        selectedQuality= Arrays.asList(listQuality).indexOf(setting.GetValue(ValueSetting.Quality,"Auto"));
        selectedSize= Arrays.asList(sizes).indexOf(setting.GetValue(ValueSetting.Size,"640*480"));
        selectedOrientation= Arrays.asList(orientations).indexOf(setting.GetValue(ValueSetting.Orientation,"Phong cảnh"));

        tvOrientation = findViewById(R.id.tv_selected_orientation);
        tvQuality= findViewById(R.id.tv_selected_quality);

        tvSize = findViewById(R.id.tv_selected_size);
        tvOrientation.setText(orientations[selectedOrientation]);

        tvSize.setText(sizes[selectedSize]);
        tvQuality.setText(listQuality[selectedQuality]);

        tvEquipments=findViewById(R.id.tv_equipments);
        tvEquipments.setText(SocketManager.socketSet.size()+" thiết bị");

        LinearLayout videoSize = findViewById(R.id.tv_video_size);
        videoSize.setOnClickListener(e-> showSizeDialog());

        LinearLayout tvVideoOrientation= findViewById(R.id.tv_video_orientation);
        tvVideoOrientation.setOnClickListener(e-> showOrientationDialog());

        LinearLayout videoQuality=findViewById(R.id.video_quality);
        videoQuality.setOnClickListener(e->showQuality());

        LinearLayout devices=findViewById(R.id.devices);
        devices.setOnClickListener(e->showDevice());
    }
    private void showOrientationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Định hướng video");
        builder.setSingleChoiceItems(orientations, selectedOrientation, (dialog, which) -> {
            selectedOrientation = which;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            tvOrientation.setText(orientations[selectedOrientation]);
            setting.SetValue(ValueSetting.Orientation,orientations[selectedOrientation]);
            dialog.dismiss();
            Toast.makeText(this, "Đã chọn: " + orientations[selectedOrientation], Toast.LENGTH_SHORT).show();
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
            tvSize.setText(sizes[selectedSize]);
            setting.SetValue(ValueSetting.Size,sizes[selectedSize]);
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
            tvQuality.setText(listQuality[selectedQuality]);
            setting.SetValue(ValueSetting.Quality,listQuality[selectedQuality]);
            dialog.dismiss();
            Toast.makeText(this, "Đã chọn: " + listQuality[selectedQuality], Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showDevice(){
        List<String> devices=new ArrayList<>();
        for(SocketHandler client:SocketManager.socketSet){
            devices.add(client.getClientAddress());
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Các thiết bị kết nối");
        builder.setItems(devices.toArray(new String[0]),(dialog,which)->{
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}