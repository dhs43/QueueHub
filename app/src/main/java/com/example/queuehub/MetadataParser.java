package com.example.queuehub;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.security.AccessController.getContext;

public class MetadataParser {

    public String getSongTitle(Uri uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(MainActivity.context, uri);
        String songTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        return songTitle;
    }

    public String getSongArtist(Uri uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(MainActivity.context, uri);
        String songArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        return songArtist;
    }

    public byte[] getSongBtyeArray(Uri uri) {
        //gets byte array
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(MainActivity.context, uri);
        byte[] art = retriever.getEmbeddedPicture();

        return art;
        //puts byte array into file
        //File file;
        //file = new File("android.resource://raw/btyes.java");
        //FileOutputStream out = new FileOutputStream(file);
        //out.write(art);
        //out.close();
        //return file;
    }

    //========================================================================================
    //  For when we pull 'art' from server how to assign to imageview
    //---------------------------------------------------------------------------------------=
    //        if( art != null ){
    //            imgAlbum.setImageBitmap( BitmapFactory.decodeByteArray(art, 0, art.length));
    //        }
    //        else{
    //            imgAlbum.setImageResource(R.drawable.no_image);
    //        }
    //========================================================================================
}