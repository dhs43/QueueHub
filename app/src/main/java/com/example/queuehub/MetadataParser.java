package com.example.queuehub;

import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MetadataParser {

    public static String getSongTitle(String uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        String songTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        return songTitle;
    }

    public static String getSongArtist(String uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        String songArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        return songArtist;
    }

    public static byte[] getSongBtyeArray(String uri) throws IOException {
        //gets byte array
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
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