package com.example.app_torecognize_photo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{
    DialogLoading dialogLoading = new DialogLoading(this);
    int CHECK_REQUEST_PERMISSION = 101;
    RecyclerView recyclerView;
    GalleryAdapter galleryAdapter;
    List<String> images;
    EditText editTextQue;

    private String selectedImagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        dialogLoading.showDialog();
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerview_gallery_images);
        editTextQue = findViewById(R.id.editTextQuery);

        Button button = (Button) findViewById(R.id.buttonPerform);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dialogLoading.showDialog();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        try
                        {
                            LoadImages(false);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 1000);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialogLoading.closeDialog();
                    }
                }, 3000);
            }
        });

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CHECK_REQUEST_PERMISSION);
        }
        else
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        LoadImages(true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 200);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogLoading.closeDialog();
            }
        }, 2000);
    }



    @Override
    protected void onRestart() {
        super.onRestart();

        // first clear the recycler view so items are not populated twice
        for (int i = 0; i < galleryAdapter.getSize(); i++) {
            //galleryAdapter.delete(i);
        }
        recyclerView.setAdapter(galleryAdapter);
    }

    private void LoadImages(boolean isOpenFirstTime) throws IOException {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        images = ImagesGallery.ListOfImages(this, editTextQue.getText().toString(), isOpenFirstTime);
        galleryAdapter = new GalleryAdapter(this, images, new GalleryAdapter.PhotoListener() {
            @Override
            public void onPhotoClick(String path)
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(path), "image/*");
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(galleryAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == CHECK_REQUEST_PERMISSION)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Read external storage permission granted", Toast.LENGTH_SHORT).show();
                try
                {
                    LoadImages(true);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                Toast.makeText(this, "Read external storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}