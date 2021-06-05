package com.chatapp.example.flamingoapp.phase2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;

import com.chatapp.example.flamingoapp.adapters.ChatAdapter;
import com.chatapp.example.flamingoapp.models.MessageModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.phone.DoctorAppointment.R;
import com.phone.DoctorAppointment.databinding.ActivityChatDetailBinding;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    ProgressDialog dialog;
    String senderId, receiverId , senderRoom , receiverRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();
        dialog=new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        auth=FirebaseAuth.getInstance();
        senderId = auth.getUid();     // taking user id from firebase first  // using final to make variable global
        // sender and receiver id
        receiverId=getIntent().getStringExtra("userId");  // taking
        String userName=getIntent().getStringExtra("userName");
        String profilePic=getIntent().getStringExtra("profilePic");

        binding.userName.setText(userName);   // uploading to chatDetails
        Picasso.get().load(profilePic).placeholder(R.drawable.user2).into(binding.profileimage);



        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ChatDetailActivity.this,ChatListActivity.class);
                startActivity(intent);
            }
        });

        final ArrayList<MessageModel> messageModels=new ArrayList<>();    // data coming from MessageModel

        final ChatAdapter chatAdapter= new ChatAdapter(messageModels,this, receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);  // linear layout in recycler view
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        senderRoom=senderId+receiverId;
        receiverRoom=receiverId+senderId;


        //for showing online offline
        database.getReference().child("presence").child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String status =snapshot.getValue(String.class);
                    if(!status.isEmpty())
                    {
                        binding.userStatus.setText(status);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // working on showing message in recyclerView
        database.getReference().child("chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();                       // showing one msg only once
                        for(DataSnapshot snapshot1: snapshot.getChildren())
                        {
                            MessageModel model=snapshot1.getValue(MessageModel.class);
                            model.setMessageId(snapshot1.getKey());   //get message id to delete
                            //
                            messageModels.add(model);                  // adding msgs from firebase

                        }
                        chatAdapter.notifyDataSetChanged();            // updating recyclerView continues
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        final Handler handler= new Handler();
        binding.etMessage.addTextChangedListener(new TextWatcher() {   // to show typing status
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                database.getReference().child("presence").child(senderId).setValue("Typing...");   // if typing then show typing else show online
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStopTyping,1000);       // 1000  =  1 second
            }

            Runnable userStopTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderId).setValue("Online");
                }
            };
        });

        binding.sendMessage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String message2=binding.etMessage.getText().toString();
                if(binding.etMessage.getText().toString().isEmpty())
                {
                    binding.etMessage.setError("Enter the message");
                    return;
                }

                String  message= null;
                try {
                    message = encrypt(message2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Date date =new Date();
                final MessageModel model=new MessageModel(senderId, message,date.getTime());   // taking sender msg and id
                model.setTime(new Date().getTime());
                binding.etMessage.setText("");                                 // empty editText after send msg


                database.getReference().child("chats").child(senderRoom)   // sender node work
                        .push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // receiver node work  ,,, message will send on both sides , sender and receiver and editText also get empty
                        database.getReference().child("chats")
                                .child(receiverRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });
                    }
                });
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);

            }
        });

        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                 // camera

                //    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //  startActivityForResult(intent,30);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId =FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        String currentId =FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {                     // getting image from gallery
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==25 || requestCode==30)
        {
            if(data!=null)
            {
                if(data.getData()!=null)
                {
                    Uri selectedImage =data.getData();


                    Calendar calendar= Calendar.getInstance();
                    final StorageReference reference = storage.getReference().child("pictures").child(calendar.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful())
                            {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {   // taking back image url
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath =uri.toString();
                                        ;
                                        String message= binding.etMessage.getText().toString();
                                        Date date =new Date();
                                        final MessageModel model=new MessageModel(senderId, message,date.getTime());   // taking sender msg and id
                                        model.setTime(new Date().getTime());
                                        model.setMessage("Photoz");                                  // to check image is sent
                                        model.setImageUrl(filePath);
                                        binding.etMessage.setText("");                                      // empty editText after send msg

                                        database.getReference().child("chats").child(senderRoom)               // sender node work
                                                .push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // receiver node work  ,,, message will send on both sides , sender and receiver and editText also get empty
                                                database.getReference().child("chats")
                                                        .child(receiverRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });
                                            }
                                        });


                                    }
                                });/////////

                            }
                        }
                    });
                }
            }

        }
    }

    public String encrypt(String data) throws Exception
    {
        String AES="AES";
        String password="terminator";
        SecretKeySpec key=generateKey(password);
        Cipher c=Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE,key);
        byte [] encVal = c.doFinal(data.getBytes());
        String encryptedValue= Base64.encodeToString(encVal,Base64.DEFAULT);
        return  encryptedValue;

    }

    private SecretKeySpec generateKey(String password) throws  Exception
    {
        final MessageDigest digest=MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0,bytes.length );
        byte []key=digest.digest();
        SecretKeySpec secretKeySpec=new SecretKeySpec(key, "AES");
        return  secretKeySpec;
    }
}