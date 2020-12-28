package com.barbera.barberaconsumerapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingActivityAdapter extends BaseAdapter {
    private List<BookingModel> bookingAdapterList;
    private String UserName;
    private String UserPhone;
    private ProgressDialog progressDialog;

    public BookingActivityAdapter(List<BookingModel> bookingAdapterList) {
        this.bookingAdapterList = bookingAdapterList;
    }

    @Override
    public int getCount() {
        return bookingAdapterList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view;
        if (convertView == null)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_booking_fragement, null);
        else
            view = (View) convertView;

        TextView serviceSummary=(TextView)view.findViewById(R.id.booking_service_title);
        TextView totalAmount=(TextView)view.findViewById(R.id.booking_service_total);
        TextView dateTime=(TextView)view.findViewById(R.id.booking_date_time);
        TextView address=(TextView)view.findViewById(R.id.booking_address);
        final Button cancelBooking=(Button)view.findViewById(R.id.cancel_button);

        serviceSummary.setText(bookingAdapterList.get(position).getSummary());
        totalAmount.setText("Total Amount Rs "+bookingAdapterList.get(position).getAmount());
        dateTime.setText(bookingAdapterList.get(position).getDate()+"\n"+bookingAdapterList.get(position).getTime());
        address.setText(bookingAdapterList.get(position).getAddress());
        extractNameAndContact();

        cancelBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                builder.setMessage("Really!!You Want to Cancel..");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog=new ProgressDialog(view.getContext());
                        progressDialog.setMessage("Hold On for a moment...");
                        progressDialog.show();
                        progressDialog.setCancelable(false);
                        addtoSheet(position,view);
                        dropBooking(position,view);
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });
        return view;
    }

    private void extractNameAndContact() {
        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        UserName=task.getResult().get("Name").toString();
                        UserPhone=task.getResult().get("Phone").toString();
                    }
                });
    }

    private void dropBooking(final int position, final View view) {
        Map<String,Object> CancelData=new HashMap<>();
        CancelData.put("status","Cancelled");
        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Bookings")
                .document(bookingAdapterList.get(position).getDocId()).delete().
                addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    BookingsActivity.bookingActivityList.remove(bookingAdapterList.get(position));
                    BookingsActivity.bookingActivityAdapter.notifyDataSetChanged();
                }
                else
                    Toast.makeText(view.getContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addtoSheet(final int position,final View view) {
        StringRequest stringRequest=new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbyn906_iser-mdKH73SAln_nrIQLv7X7ZDB5CVKfr706j9n2Ypl/exec"
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(view.getContext(),"Booking Cancelled",Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        },  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(view.getContext(),error.toString(),Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();
                //here we pass params
                parmas.put("action","cancelItem");
                parmas.put("userName",UserName);
                parmas.put("services",bookingAdapterList.get(position).getSummary());
                parmas.put("servicedate",bookingAdapterList.get(position).getDate());
                parmas.put("servicetime",bookingAdapterList.get(position).getTime());
                parmas.put("total",bookingAdapterList.get(position).getAmount());
                parmas.put("address",bookingAdapterList.get(position).getAddress());
                parmas.put("phone", UserPhone);

                return parmas;
            }
        };
        int socketTimeOut = 50000;// u can change this .. here it is 50 seconds
        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        RequestQueue queue = Volley.newRequestQueue(view.getContext());
        queue.add(stringRequest);
    }
}