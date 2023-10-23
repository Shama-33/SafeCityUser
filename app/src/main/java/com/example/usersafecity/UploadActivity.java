package com.example.usersafecity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;



import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.usersafecity.ml.Model;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    Button btnloc;
    TextView result, confidence;
    ImageView imageView;
    Button picture,select;
    int imageSize = 224;
    String Classify="",Confidence="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        btnloc=findViewById(R.id.btnloc);
       /* btnloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),LocationActivity.class);
                startActivity(intent);
            }
        });*/

        btnloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the image, classification result, and confidence
               // Bitmap image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                //String classificationResult = result.getText().toString();
                //String confidenceInfo = confidence.getText().toString();

                // Check if any of the data is empty
                if ( Classify.isEmpty() || Confidence.isEmpty()) {
                    // Display a toast message indicating that data is missing
                    Toast.makeText(UploadActivity.this, "Data is missing. Please classify an image first.", Toast.LENGTH_SHORT).show();
                } else {
                    // Create an Intent to navigate to the next activity
                    Intent intent = new Intent(UploadActivity.this, LocationActivity.class);

                    // Pass the image, classification result, and confidence to the next activity
                    //intent.putExtra("IMAGE", image);
                    Uri imageUri = getImageUriFromImageView(imageView);
                    intent.putExtra("IMAGE_URI", imageUri.toString());
                    intent.putExtra("CLASSIFY", Classify);
                    intent.putExtra("CONFIDENCE", Confidence);

                    // Start the next activity
                    startActivity(intent);
                }
            }
        });



        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);
        select=findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickImageIntent, 2);

            }
        });

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch camera if we have permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, 1);
                    } else {
                        //Request camera permission if we don't have it.
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                    }
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.content_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.SignOutMenuId)
        {

            FirebaseAuth.getInstance().signOut();
            finish();
            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId()==R.id.ProfileMenuId)
        {

        }
        else if (item.getItemId()==R.id.HistoryMenuId)
        {
            Intent i=new Intent(getApplicationContext(),HistoryActivity.class);
            startActivity(i);

        }
        else if (item.getItemId()==R.id.HomeMenuId)
        {

        }
        return super.onOptionsItemSelected(item);
    }







    public  void classifyImage(Bitmap image)
    {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer  = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int [] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel=0;
            for(int i=0; i<imageSize;i++){
                for(int j=0;j<imageSize;j++){
                    int val= intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16)& 0xFF)* (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8)& 0xFF)* (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF)* (1.f / 255.f));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;

            for(int i=0 ;i<confidences.length ;i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes={"Damaged_Road","Trash","Flood","Homeless_people"};
            //result.setText(classes[maxPos]);
            Classify=classes[maxPos];
            String s="";
            for(int i=0;i<classes.length;i++){
                s+= String.format("%s: %.1f%%\n",classes[i],confidences[i]*100);
            }
            //confidence.setText(s);
            Confidence=s;

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image =(Bitmap) data.getExtras().get("data");
            int dimension=Math.min(image.getWidth(),image.getHeight());
            image=ThumbnailUtils.extractThumbnail(image,dimension,dimension);
            imageView.setImageBitmap(image);
            image=Bitmap.createScaledBitmap(image,imageSize,imageSize,false);
            classifyImage(image);


        }
        else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectedImage = data.getData();
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                classifyImage(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }







    private Uri getImageUriFromImageView(ImageView imageView) {
        Uri imageUri = null;
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            imageUri = getImageUriFromBitmap(bitmap);
        }
        return imageUri;
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        // You can save the bitmap to a temporary file and return the URI
        // Here's an example of how to do it:
        File tempFile = new File(getExternalCacheDir(), "temp_image.jpg");
        try {
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(tempFile);
    }

}
