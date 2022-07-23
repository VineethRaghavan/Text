package io.text.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import io.text.R;

public class GroupCodeActivity extends AppCompatActivity {

    private static final String TAG = "CreateGroupActivity";
    private ImageView idQrcode;
    private String uid;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_code);
        Bundle bundle = getIntent().getExtras();
        uid = bundle.getString("UID");
        name = bundle.getString("NAME");
        setTitle("QR code for " + name);
        idQrcode = findViewById(R.id.idQrcode);
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(uid, BarcodeFormat.QR_CODE, 400, 400);
            int w = bitMatrix.getWidth();
            int h = bitMatrix.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    pixels[y * w + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            final float roundPx = (float) bitmap.getWidth() * 0.025f;
            roundedBitmapDrawable.setCornerRadius(roundPx);
            idQrcode.setImageDrawable(roundedBitmapDrawable);
        } catch (WriterException e) {
            Log.e(TAG, e.toString());
        }
    }
}
