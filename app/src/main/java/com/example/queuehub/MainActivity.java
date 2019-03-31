package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);

        requestPermissions(
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setType("audio/*");
                startActivityForResult(intent, 7);
                //Log.v("the",getAllAudios(getApplicationContext()).toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch(requestCode){

            case 7:

                if(resultCode==RESULT_OK){

                    Uri PathHolder = data.getData();

                    //File file = getAllAudios(getApplicationContext(), PathHolder);
                    Toast.makeText(MainActivity.this, PathHolder.toString() , Toast.LENGTH_LONG).show();

                }
                break;

        }
    }

    public static File getAllAudios(Context c, Uri uri) {
        File file = null;
        String[] projection = { MediaStore.Audio.AudioColumns.DATA ,MediaStore.Audio.Media.DISPLAY_NAME};
        Cursor cursor = c.getContentResolver().query(uri, projection, null, null, null);
        try {
            cursor.moveToFirst();
            file = new File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

//    public static List<File> getAllAudios(Context c) {
//        List<File> files = new ArrayList<>();
//        String[] projection = { MediaStore.Audio.AudioColumns.DATA ,MediaStore.Audio.Media.DISPLAY_NAME};
//        Cursor cursor = c.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
//        try {
//            cursor.moveToFirst();
//            do{
//                files.add((new File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)))));
//            }while(cursor.moveToNext());
//
//            cursor.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return files;
//    }
}
