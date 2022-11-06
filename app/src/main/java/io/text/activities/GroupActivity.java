package io.text.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.text.BuildConfig;
import io.text.R;
import io.text.models.Message;

public class GroupActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static String uid;
    private Button mSendButton;
    private Button mClearButton;
    private LinearLayout contextContainer;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText mMessageEditText;
    private TextView mNoMessages;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mGroupReference;
    private DatabaseReference userGroups;
    private DatabaseReference mMessageReference;
    private FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> mFirebaseAdapter;
    private String mUsername;
    private String name;
    private Message message;
    AlertDialog.Builder dialogbuilder;

    TextView contextNameView;
    TextView contextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        uid = bundle.getString("UID");
        name = bundle.getString("NAME");
        setTitle(name);
        setContentView(R.layout.activity_group);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mUsername = mFirebaseUser.getDisplayName();
        mMessageRecyclerView = findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSendButton = findViewById(R.id.sendButton);
        mClearButton = findViewById(R.id.clearButton);
        contextNameView = findViewById(R.id.contextNameView);
        contextView = findViewById(R.id.contextView);
        mNoMessages = findViewById(R.id.noMessages);
        mMessageEditText = findViewById(R.id.messageEditText);
        contextContainer = findViewById(R.id.contextContainer);
        contextNameView = findViewById(R.id.contextNameView);
        contextView = findViewById(R.id.contextView);
        userGroups = FirebaseDatabase.getInstance(BuildConfig.DB_URL).getReference().child("users").child(mFirebaseUser.getUid()).child("groups");
        mGroupReference = FirebaseDatabase.getInstance(BuildConfig.DB_URL).getReference().child("groups").child(uid);
        mGroupReference.keepSynced(true);
        mMessageReference = mGroupReference.child("messages");
        mMessageReference.keepSynced(true);

        mMessageReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                mNoMessages.setVisibility((TextView.INVISIBLE));
                Log.e(TAG, dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mNoMessages.setVisibility((TextView.VISIBLE));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mGroupReference.child("messages").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getValue() == null) {
                    mNoMessages.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e(TAG, "Error getting data", task.getException());
            }
        });

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mSendButton.setEnabled(charSequence.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(mMessageReference, Message.class)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.group_message_adapter_item, viewGroup, false);
                return new messageViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final RecyclerView.ViewHolder viewHolder,
                                            int position,
                                            Message message) {
                ((messageViewHolder) viewHolder).bind(message);
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int MessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (MessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mSendButton.setOnClickListener(view -> {
            mNoMessages.setVisibility(TextView.GONE);
            String contextViewString = contextView.getText().toString();
            String contextNameViewString = contextNameView.getText().toString();
            message = new
                    Message(mFirebaseUser.getUid(),
                    mUsername,
                    mMessageEditText.getText().toString(),
                    contextNameViewString.equals("") ? null: contextNameViewString,
                    contextViewString.equals("") ? null: contextViewString);
            contextNameView.setText("");
            contextView.setText("");
            mGroupReference.child("messages").push().setValue(message);
            mMessageEditText.setText("");
            if(contextContainer.getVisibility() == LinearLayout.VISIBLE)
                contextContainer.setVisibility(LinearLayout.GONE);
        });

        mClearButton.setOnClickListener(view -> {
            contextContainer.setVisibility(LinearLayout.GONE);
        });

        dialogbuilder = new AlertDialog.Builder(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.exit_group) {
            dialogbuilder.setMessage("Are you sure you wish to exit this group?");
            dialogbuilder.setCancelable(true);

            dialogbuilder.setPositiveButton(
                    "Yes",
                    (dialog, id) -> {
                        mGroupReference.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().getValue() != null) {
                                    int count = Integer.parseInt(String.valueOf(task.getResult().child("memberCount").getValue()));
                                    if (count == 1) {
                                        mGroupReference.removeValue();
                                    } else {
                                        mGroupReference.child("memberCount").setValue(count - 1);
                                        mGroupReference.child("members").child(mFirebaseUser.getUid()).removeValue();
                                    }
                                    userGroups.child(uid).removeValue();
                                    finish();
                                }
                            } else {
                                Log.e(TAG, "Error getting data", task.getException());
                            }
                        });
                        dialog.cancel();
                    });

            dialogbuilder.setNegativeButton(
                    "No",
                    (dialog, id) -> dialog.cancel());
            AlertDialog alert = dialogbuilder.create();
            alert.show();
            return true;
        } else if (item.getItemId() == R.id.action_code) {
            Intent intent = new Intent(GroupActivity.this, GroupCodeActivity.class);
            intent.putExtra("NAME", name);
            intent.putExtra("UID", uid);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class messageViewHolder extends RecyclerView.ViewHolder{
        LinearLayout messageContainer;
        TextView nameTextView;
        TextView timeTextView;
        TextView messageTextView;
        TextView messageContextName;
        TextView messageContextText;
        LinearLayout messageContextContainer;
        View divider;
        View divider1;

        messageViewHolder(View v) {
            super(v);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            nameTextView = itemView.findViewById(R.id.name);
            timeTextView = itemView.findViewById(R.id.time);
            messageTextView = itemView.findViewById(R.id.text);
            messageContextName = itemView.findViewById(R.id.messageContextName);
            messageContextText = itemView.findViewById(R.id.messageContextText);
            messageContextContainer = itemView.findViewById(R.id.messageContextContainer);
            divider = itemView.findViewById(R.id.divider);
            divider1 = itemView.findViewById(R.id.divider1);
        }

        void bind(final Message message) {
            if (message.getText() != null) {
                if(message.getContextText() != null) {
                    messageContextContainer.setVisibility(LinearLayout.VISIBLE);
                    messageContextName.setVisibility(TextView.VISIBLE);
                    messageContextText.setVisibility(TextView.VISIBLE);
                    divider.setVisibility(View.VISIBLE);
                    divider1.setVisibility(View.VISIBLE);
                    messageContextName.setText(message.getContextName());
                    messageContextText.setText(message.getContextText());
                } else {
                    messageContextContainer.setVisibility(LinearLayout.GONE);
                    messageContextName.setVisibility(TextView.GONE);
                    messageContextText.setVisibility(TextView.GONE);
                    divider.setVisibility(View.GONE);
                    divider1.setVisibility(View.GONE);
                }
                nameTextView.setText(message.getName());
                timeTextView.setText(DateFormat.format("dd MMM yyyy HH:mm", message.getTime()));
                messageTextView.setText(message.getText());
                messageTextView.setVisibility(TextView.VISIBLE);
                messageContainer.setOnLongClickListener(v -> {
                    contextContainer.setVisibility(LinearLayout.VISIBLE);
                    contextNameView.setText(message.getName());
                    contextView.setText(message.getText());
                    return false;
                });
                Linkify.addLinks(messageTextView, Linkify.ALL);
            }
        }
    }
}
