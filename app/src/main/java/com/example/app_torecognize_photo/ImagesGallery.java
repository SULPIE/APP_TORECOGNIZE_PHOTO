package com.example.app_torecognize_photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.app_torecognize_photo.ml.MobilenetV110224Quant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class ImagesGallery
{
    @SuppressLint("Recycle")
    public static ArrayList<String> ListOfImages(Context context, String query, boolean IsGalleryOpeningFirstTime) throws IOException {
        Uri uri;
        Cursor cursor;
        Bitmap bitmapImage;
        int column_index_data,column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage;
        String[] linesEng = new String[1001];
        String[] linesRU = new String[1001];

        linesEng = ReadTextFromFile("NeuroLabels.txt", context);
        linesRU = ReadTextFromFile("NeuroLabelsRU.txt", context);

        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] protection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String orderBy  = MediaStore.Video.Media.DATE_TAKEN;
        cursor = context.getContentResolver().query(uri, protection, null, null, orderBy + " DESC");
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        String[] wordsByQuery = query.split(" ");
        String[] wordClassEng;
        String[] wordClassRU;

        while(cursor.moveToNext())
        {
            if(IsGalleryOpeningFirstTime || query == "")
            {
                absolutePathOfImage = cursor.getString(column_index_data);
                listOfAllImages.add(absolutePathOfImage);
                continue;
            }

            MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(context);
            bitmapImage = BitmapFactory.decodeFile(cursor.getString(column_index_data));
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);

            bitmapImage = Bitmap.createScaledBitmap(bitmapImage, 224, 224, true);
            inputFeature0.loadBuffer(TensorImage.fromBitmap(bitmapImage).getBuffer());

            MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            wordClassEng = linesEng[(getMaxClassFromModel(outputFeature0.getFloatArray()))].split(" ");
            wordClassRU = linesRU[(getMaxClassFromModel(outputFeature0.getFloatArray()))].split(" ");


            for(String wordq : wordsByQuery)
            {
                for(int idx = 0; idx < wordClassEng.length; idx++)
                {
                    if(wordq.toLowerCase().contains(wordClassEng[idx].toLowerCase()))
                    {
                        absolutePathOfImage = cursor.getString(column_index_data);
                        listOfAllImages.add(absolutePathOfImage);
                        break;
                    }
                }

                for(int idx = 0; idx < wordClassRU.length; idx++)
                {
                    if(wordq.toLowerCase().contains(wordClassRU[idx].toLowerCase()))
                    {
                        absolutePathOfImage = cursor.getString(column_index_data);
                        listOfAllImages.add(absolutePathOfImage);
                        break;
                    }
                }
            }

            model.close();
        }

        return listOfAllImages;
    }
    private static int getMaxClassFromModel(float[] arr)
    {
        int max = 0;
        for(int i = 0; i < arr.length; i++)
        {
            if(arr[i] > arr[max])
            {
                max = i;
            }
        }
        return max;
    }

    private static String[] ReadTextFromFile(String FileName, Context context)
    {
        String[] lines = new String[1001];
        int cnt = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open(FileName), "UTF-8"))) {
            String lineStr = null;
            int currentLine = 0;

            while ((lineStr = bufferedReader.readLine()) != null) {
                lines[cnt] = lineStr;
                cnt++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }
}
