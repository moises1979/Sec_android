package com.example.sec_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RegisterActivity extends AppCompatActivity {
    static String clave = "moisesDurante979";
    private static SecretKeySpec secret;
    private EditText editTextEmailR, editTextPasswordR, editTextRepPasswordR;
    private Button buttonRegister, buttonReturn;

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        editTextEmailR = (EditText) findViewById(R.id.editTextEmailRegister);
        editTextPasswordR = (EditText) findViewById(R.id.editTextPassword);
        editTextRepPasswordR = (EditText) findViewById(R.id.editTextRepPasswordRegister);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        buttonReturn = (Button) findViewById(R.id.buttonReturn);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextEmailR.getText().toString().isEmpty() && editTextPasswordR.getText().toString().isEmpty() && editTextRepPasswordR.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Digite Correo y Contraseña",
                            Toast.LENGTH_SHORT).show();
                } else if (!editTextPasswordR.getText().toString().equals(editTextRepPasswordR.getText().toString())) {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas NO son iguales, por favor veirifique",
                            Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        byte[] claveE = encryptMsg(editTextPasswordR.getText().toString(), generateKey());
                        String claveD = decryptMsg(claveE, generateKey());
                        mAuth.createUserWithEmailAndPassword(editTextEmailR.getText().toString(), claveD)
                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            User user = new User();
                                            user.setUid(uid);
                                            user.setCorreo(editTextEmailR.getText().toString());
                                            databaseReference.child("Users").child(user.getUid()).setValue(user);
                                            DBhelper dbHelper = new DBhelper(RegisterActivity.this);
                                            dbHelper.insertData(editTextEmailR.getText().toString(), uid, RegisterActivity.this);
                                            Toast.makeText(RegisterActivity.this, "Usuario creado con exito", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Error al Registrar el Usuario", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public static SecretKey generateKey() throws NoSuchAlgorithmException, InvalidKeyException {
        return secret = new SecretKeySpec(clave.getBytes(), "AES");
    }

    public static byte[] encryptMsg(String message, SecretKey secret) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return cipherText;
    }

    public static String decryptMsg(byte[] cipherText, SecretKey secret) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        return decryptString;

    }
}
