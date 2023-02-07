package com.example.tfg_joseangel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tfg_joseangel.clases.Componente;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class nuevo_producto extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private EditText edt_nombreProd;
    private EditText edt_numSerie;
    private EditText edt_precio;
    private EditText edt_stock;
    private EditText edt_marca;
    private Componente componentes = new Componente();

    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_producto);

        edt_numSerie = (EditText) findViewById(R.id.edt_numSerie);
        edt_nombreProd = (EditText) findViewById(R.id.edt_nombreProd);
        edt_precio = (EditText) findViewById(R.id.edt_precio);
        edt_stock = (EditText) findViewById(R.id.edt_stock);
        edt_marca = (EditText) findViewById(R.id.edt_marca);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser usuario = firebaseAuth.getCurrentUser();
                if (usuario != null) {

                }
            }
        };
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    public void nuevoProducto(View view){
        String nombre = String.valueOf(edt_nombreProd.getText());
        String idComp = String.valueOf(edt_numSerie.getText());
        String precio = String.valueOf(edt_precio.getText());
        String cantidad = String.valueOf(edt_stock.getText());
        String idMar = String.valueOf(edt_marca.getText());

        if (idComp.isEmpty() || nombre.isEmpty() || precio.isEmpty() || cantidad.isEmpty() || idMar.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
        }else{
            firebaseAuth.signInWithEmailAndPassword(idComp,idMar).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {        //Revisar, puede que el metodo no funcione
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()){
                        Log.i("Nuevo componente","Excepción: " + task.getException().toString());
                        try {
                            throw task.getException();
                        }catch (FirebaseAuthInvalidUserException e){
                            nuevoProductoFirebase(idComp,idMar);
                        }catch (FirebaseAuthInvalidCredentialsException e){
                            Toast.makeText(nuevo_producto.this, "Componente ya registrado", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void nuevoProductoFirebase(String idComp, String idMar){
        firebaseAuth.createUserWithEmailAndPassword(idComp,idMar).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(nuevo_producto.this, "Error al registrar el componente", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(nuevo_producto.this, "Componente registrado con éxito", Toast.LENGTH_SHORT).show();
                    addToFirestore();
                }
            }
        });
    }

    private void addToFirestore(){
        String nombre = String.valueOf(edt_nombreProd.getText());
        String idComp = String.valueOf(edt_numSerie.getText());
        String precio = String.valueOf(edt_precio.getText());
        String cantidad = String.valueOf(edt_stock.getText());
        String idMar = String.valueOf(edt_marca.getText());

        Map<String, Object> componente = new HashMap<>();
        componente.put("Nombre", nombre);
        componente.put("Ref", idComp);
        componente.put("Precio", precio);
        componente.put("Stock", cantidad);
        componente.put("Marca", idMar);

       componentes.setNombre(nombre);
       componentes.setIdComp(idComp);
       componentes.setIdMar(idMar);

        db.collection("Componentes").document(firebaseAuth.getCurrentUser().getUid()).set(componente).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                createDocument();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Firestore", "Error al añadir el componente en firestore", e);
            }
        });
    }

    private void createDocument() {         //buscar
        Map<String, Object> data = new HashMap<>();
        db.collection("businessdata")
                .document(firebaseAuth.getCurrentUser().getEmail())
                .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("Firestore", "Usuario nuevo en firestore");
                        firebaseAuth.signOut();
                        Intent intent = new Intent(nuevo_producto.this, HomeActivity.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        DocumentReference documentReference = db.collection("businessdata").document(firebaseAuth.getCurrentUser().getEmail());     //Revisar linea para cambiar el "current user"
        documentReference.collection("editinfocomponentes")
                .document("componentes").set(componentes).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}