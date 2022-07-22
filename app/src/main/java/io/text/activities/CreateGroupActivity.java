package io.text.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import io.text.BuildConfig;
import io.text.R;
import io.text.models.Group;
import io.text.models.User;
import io.text.utils.SecurityUtil;

public class CreateGroupActivity extends AppCompatActivity {

    private static final String TAG = "CreateGroupActivity";
    private ImageView idQrcode;
    private EditText idGroupName;
    private Button idBtnGenerateQR;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mGroups;
    private DatabaseReference userGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        idQrcode = findViewById(R.id.idQrcode);
        idGroupName = findViewById(R.id.idGroupName);
        idBtnGenerateQR = findViewById(R.id.idBtnGenerateQR);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mGroups = FirebaseDatabase.getInstance(BuildConfig.DB_URL).getReference().child("groups");
        userGroups = FirebaseDatabase.getInstance(BuildConfig.DB_URL).getReference().child("users").child(mFirebaseUser.getUid()).child("groups");
        idGroupName.requestFocus();
        idGroupName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                generateQR();
            }
            return false;
        });

        idBtnGenerateQR.setOnClickListener(v -> generateQR());
    }

    void generateQR() {
        if (TextUtils.isEmpty(idGroupName.getText().toString())) {
            Toast.makeText(CreateGroupActivity.this, "Enter group name to generate QR code", Toast.LENGTH_SHORT).show();
        } else {
            try {
                QRCodeWriter writer = new QRCodeWriter();
                SecurityUtil su = new SecurityUtil();
                String public_key = su.getPublicKey();
                String newGroupKey = mGroups.push().getKey();
                Group newGroup = new Group(newGroupKey, idGroupName.getText().toString());
                mGroups.child(newGroupKey).setValue(newGroup);
                User user = new User(mFirebaseUser.getUid(), mFirebaseUser.getDisplayName(), public_key);
                mGroups.child(newGroupKey).child("members").child(mFirebaseUser.getUid()).setValue(user);
                userGroups.child(newGroupKey).setValue(idGroupName.getText().toString());
                BitMatrix bitMatrix = writer.encode(newGroupKey, BarcodeFormat.QR_CODE, 400, 400);
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
                Toast.makeText(getBaseContext(), "Group created", Toast.LENGTH_SHORT).show();
            } catch (WriterException e) {
                Log.e(TAG, e.toString());
            }
        }
    }


}
