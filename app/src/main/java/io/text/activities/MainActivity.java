package io.text.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import io.text.BuildConfig;
import io.text.R;
import io.text.models.User;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );
    ListView groupListView;
    TextView noGroupsText;
    ProgressBar pd;
    AlertDialog.Builder dialogbuilder;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference userGroups;
    private DatabaseReference mGroups;
    private TreeMap<String, String> loadedGroups;
    private ArrayAdapter<String> groupArrayAdapter;
    private ArrayList<String> groupList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialogbuilder = new AlertDialog.Builder(this);
        groupListView = findViewById(R.id.groupsList);
        noGroupsText = findViewById(R.id.noGroupsText);
        pd = findViewById(R.id.progressBar);
        ConnectivityManager cm =
                (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null;
        if (!isConnected) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            List<AuthUI.IdpConfig> providers = Collections.singletonList(
                    new AuthUI.IdpConfig.PhoneBuilder().build());

            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTheme(R.style.LoginTheme)
                    .build();
            signInLauncher.launch(signInIntent);

        } else {
            mGroups = FirebaseDatabase.getInstance(BuildConfig.DB_URL).getReference().child("groups");
            userGroups = FirebaseDatabase.getInstance(BuildConfig.DB_URL).getReference().child("users").child(mFirebaseUser.getUid()).child("groups");
            loadedGroups = new TreeMap<>();
            groupList = new ArrayList<>();
            groupArrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.group_adapter_item, R.id.group_name, groupList);
            groupListView.setAdapter(groupArrayAdapter);
            ChildEventListener groupEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    pd.setIndeterminate(false);
                    pd.setProgress(100);
                    loadedGroups.put(dataSnapshot.getValue().toString(), dataSnapshot.getKey());
                    noGroupsText.setVisibility(View.GONE);
                    groupList.clear();
                    groupList.addAll(loadedGroups.keySet());
                    groupArrayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    loadedGroups.remove(dataSnapshot.getValue().toString());
                    groupList.clear();
                    groupList.addAll(loadedGroups.keySet());
                    groupArrayAdapter.notifyDataSetChanged();
                    if (loadedGroups.isEmpty()) {
                        pd.setVisibility(View.INVISIBLE);
                        noGroupsText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "groups:onCancelled", databaseError.toException());
                }
            };
            userGroups.addChildEventListener(groupEventListener);
            userGroups.get().addOnCompleteListener(task2 -> {
                if (task2.isSuccessful()) {
                    if (task2.getResult().getValue() == null) {
                        pd.setVisibility(View.INVISIBLE);
                        noGroupsText.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Error getting data", task2.getException());
                }
            });
            groupListView.setOnItemClickListener((parent, view, position, id) -> {
                String name = (String) parent.getItemAtPosition(position);
                String uid = loadedGroups.get(name);
                Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                intent.putExtra("NAME", name);
                intent.putExtra("UID", uid);
                startActivity(intent);
            });
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            startActivity(new Intent(getBaseContext(), SignInActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out_menu) {
            dialogbuilder.setMessage("Are you sure you wish to sign out?");
            dialogbuilder.setCancelable(true);

            dialogbuilder.setPositiveButton(
                    "Yes",
                    (dialog, id) -> {
                        mFirebaseAuth.signOut();
                        dialog.cancel();
                        finish();
                    });

            dialogbuilder.setNegativeButton(
                    "No",
                    (dialog, id) -> dialog.cancel());
            AlertDialog alert = dialogbuilder.create();
            alert.show();
            return true;
        } else if (item.getItemId() == R.id.action_scan) {
            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setPrompt("");
            intentIntegrator.setBeepEnabled(false);
            intentIntegrator.setOrientationLocked(true);
            intentIntegrator.initiateScan();
            return true;
        } else if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(getBaseContext(), CreateGroupActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String content = intentResult.getContents();
            if (content == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                mGroups.child(content).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getValue() != null) {
                        userGroups.child(content).get().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                if (task2.getResult().getValue() != null)
                                    Toast.makeText(getBaseContext(), "You are already a member of scanned group", Toast.LENGTH_SHORT).show();
                                else {
                                    String name = String.valueOf(task.getResult().child("name").getValue());
                                    int count = Integer.parseInt(String.valueOf(task.getResult().child("memberCount").getValue()));
                                    userGroups.child(content).setValue(name);
                                    mGroups.child(content).child("memberCount").setValue(count + 1);
                                    User user = new User(mFirebaseUser.getUid(), mFirebaseUser.getDisplayName());
                                    mGroups.child(content).child("members").child(mFirebaseUser.getUid()).setValue(user);
                                    Toast.makeText(getBaseContext(), intentResult.getContents(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("firebase", "Error getting data", task2.getException());
                            }
                        });

                    } else {
                        Log.e(TAG, "Group does not exist", task.getException());
                        Toast.makeText(getBaseContext(), "Group does not exist", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}