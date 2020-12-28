package com.barbera.barberaconsumerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import static com.barbera.barberaconsumerapp.MenParlourActivity.progressBarOnServiceList;

public class WomenParlourActivity extends AppCompatActivity {
    private FirebaseFirestore fstore;
    private FirebaseAuth firebaseAuth;
    private GridView listView;
    public List<Service> womenserviceList=new ArrayList<Service>();
    private String Category;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_men_parlour);

        Intent intent=getIntent();
        Category=intent.getStringExtra("Category");
        fstore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        listView=(GridView) findViewById(R.id.RootView);
        TextView title=(TextView)findViewById(R.id.salon_title);
        progressBarOnServiceList=(ProgressBar)findViewById(R.id.progressBarOnMenSalon);
        ImageView cart=(ImageView)findViewById(R.id.cartOnParlour);

        FirebaseMessaging.getInstance().subscribeToTopic("women")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Successful";
                        if (!task.isSuccessful()) {
                            msg = "Failed";
                        }
                    }
                });

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FirebaseAuth.getInstance().getCurrentUser()==null){
                    Toast.makeText(getApplicationContext(),"You Must Log In to continue",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                }
                else {
                    startActivity(new Intent(getApplicationContext(), CartActivity.class));
                }
            }
        });

        title.setText(Category);
        final ServiceAdapter adapter = new ServiceAdapter(womenserviceList);


        if(womenserviceList.size()==0) {
            progressBarOnServiceList.setVisibility(View.VISIBLE);
            fstore.collection("WomenCategory").document(Category).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                for(int i=1;i<=(long)task.getResult().get("Services");i++){
                                    fstore.collection("Women\'s Salon").document(task.getResult().get("Service_"+i).toString()).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                    if(documentSnapshot.exists()){
                                                        womenserviceList.add(new Service(documentSnapshot.get("icon").toString(), documentSnapshot.get("Service_title").toString(),
                                                                documentSnapshot.get("price").toString(), documentSnapshot.getId(), documentSnapshot.get("type").toString(),
                                                                documentSnapshot.get("cut_price").toString()));
                                                        listView.setAdapter(adapter);
                                                        progressBarOnServiceList.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                }
                                if (firebaseAuth.getCurrentUser()!=null&&dbQueries.cartList.size() == 0)
                                    dbQueries.loadCartList();
                            }
                        }
                    });
        }
        else{

            listView.setAdapter(adapter);
        }

    }
}