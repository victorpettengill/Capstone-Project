package br.com.victorpettengill.hawk_eyedcitizen.utils;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

import br.com.victorpettengill.hawk_eyedcitizen.application.HawkEyedCitizen;

/**
 * Created by appimagetech on 12/01/18.
 */

public class Utils {

    public static byte[] bitmapToBytes(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        return data;
    }

    public static File saveResizedImage(Bitmap imagem){

        File resizedFile = new File(HawkEyedCitizen.getAppContext().getFilesDir().getPath(), new Date().getTime()+".jpg");

        OutputStream fOut=null;
        try {
            fOut = new BufferedOutputStream(new FileOutputStream(resizedFile));
            imagem.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, fOut);
            fOut.flush();
            fOut.close();

        } catch (Exception e) {
            Log.e("Erro", "Error ao salvar imagem redimensionada!",e);
        }

        return resizedFile;
    }

}
