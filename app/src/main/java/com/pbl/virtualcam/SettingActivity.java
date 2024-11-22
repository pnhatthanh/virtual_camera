package com.pbl.virtualcam;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.util.Arrays;

public class SettingActivity extends AppCompatActivity {
    TextView tvOrientation;
    TextView tvQuality;
    TextView tvSize ;
    TextView tvEquipments;
    Button comeback;
    private final String[] orientations = {"Phong cảnh", "Chân dung"};
    private final String[] sizes={"640*480","800*600", "960*720"};
    private final String[] listQuality={"Thấp","Trung bình", "Cao"};
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
        selectedQuality= Arrays.asList(listQuality).indexOf(setting.GetValue(ValueSetting.Quality,"Trung bình"));
        selectedSize= Arrays.asList(sizes).indexOf(setting.GetValue(ValueSetting.Size,"640*480"));
        selectedOrientation= Arrays.asList(orientations).indexOf(setting.GetValue(ValueSetting.Orientation,"Chân dung"));
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

        comeback=findViewById(R.id.comeback);
        comeback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
        String[] devices = SocketManager.socketSet.keySet().toArray(new String[0]);
        if(devices.length==0){
            tvEquipments.setText(SocketManager.socketSet.size()+" thiết bị");
            return;
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Các thiết bị kết nối");
        builder.setItems(devices,(dialog,which)->{
            AlertDialog.Builder confirmBuilder=new AlertDialog.Builder(this);
            confirmBuilder.setTitle("Ngắt kết nối");
            confirmBuilder.setMessage("Bạn có muốn ngắt kết nối với thiết bị " + devices[which] + " không?")
                    .setPositiveButton("Có", (dialog1, which1) -> {
                        SocketManager.socketSet.get(devices[which]).StopThread();
                        showDevice();
                    })
                    .setNegativeButton("Không", (dialog1, which1) -> {
                        dialog1.dismiss();
                        AlertDialog parentDialog=builder.create();
                        parentDialog.show();
                    });
            confirmBuilder.create().show();
        });
        builder.setPositiveButton("OK",(dia,w)->{
            dia.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d->{
            tvEquipments.setText(SocketManager.socketSet.size()+" thiết bị");
        });
        dialog.show();
    }
}