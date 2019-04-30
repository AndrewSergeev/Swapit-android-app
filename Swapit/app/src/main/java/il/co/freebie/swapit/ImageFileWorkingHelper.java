package il.co.freebie.swapit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by one 1 on 08-Feb-19.
 */

public final class ImageFileWorkingHelper {
    private ImageFileWorkingHelper(){}

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

    public static void fillCircledIvWithPicasso(Uri imageUri, ImageView iv) {
        if(imageUri!= null && !imageUri.toString().isEmpty()){
            Picasso.get().load(imageUri).transform(new CircleTransform()).into(iv);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

    }

    public static void fillCircleIv(Bitmap bitmap, ImageView iv){
        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader (bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);
        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);
        iv.setImageBitmap(circleBitmap);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    public static void fillImageView(FragmentActivity activity, Uri imageUri, ImageView selectedAdPhotoIv) {
        try {
            InputStream imageStream = activity.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap =  BitmapFactory.decodeStream(imageStream);
            selectedAdPhotoIv.setImageBitmap(bitmap);
            imageStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean validateFileSize(FragmentActivity activity, Uri uri, int maxSize){
        boolean valid = false;
        try {
            InputStream imageStream = activity.getContentResolver().openInputStream(uri);
            int size = imageStream.available();
            valid = (size <= maxSize);
            imageStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

       return valid;
    }

    public static void takePictureForFragment(Fragment fragment, File file, int requestCode) {
        Uri fileUri = FileProvider.getUriForFile(fragment.getActivity(), BuildConfig.APPLICATION_ID + ".provider", file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void picPictureForFragment(Fragment fragment, int requestCode){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        fragment.startActivityForResult(photoPickerIntent, requestCode);
    }
}
