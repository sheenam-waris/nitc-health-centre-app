package com.example.healthcentre.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthcentre.LoginActivity;
import com.example.healthcentre.R;
import com.example.healthcentre.UserSession;
import com.example.healthcentre.appointmenttabactivity.AdminViewDoctorsAdapter;
import com.example.healthcentre.models.Appointment;
import com.example.healthcentre.models.Role;
import com.example.healthcentre.models.StaticDataProvider;
import com.example.healthcentre.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class AdminViewDoctorsActivity extends AppCompatActivity {

    private ImageView addNewDoctor;
    private RecyclerView doctorsRCV;

    private ArrayList<User> doctorUserArraylist;
    private AdminViewDoctorsAdapter adminViewDoctorsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_doctors);
        confirmLogin();
        addNewDoctor = findViewById(R.id.add_new_doctor);
        doctorsRCV = findViewById(R.id.recyclerview_admin_view_doctor);
        doctorsRCV.setLayoutManager(new LinearLayoutManager(this));

        doctorUserArraylist = new ArrayList<>();
        adminViewDoctorsAdapter = new AdminViewDoctorsAdapter(doctorUserArraylist);
        doctorsRCV.setAdapter(adminViewDoctorsAdapter);

        runFetchDoctorUserBackground();

        addNewDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),AdminAddNewUserActivity.class);
                intent.putExtra("role",2);
                startActivity(intent);
            }
        });
    }

    private void fetchAllDoctors() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("role", 2);
        final String requestBody = object.toString();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticDataProvider.getApiBaseUrl() + "/user/get-by-role", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    String status = responseJson.getString("status");
                    switch(status){
                        case "SUCCESS":
                            //changed api
                            JSONArray doctors = responseJson.getJSONArray("users");
                            for(int i=0;i<doctors.length();i++){
                                JSONObject obj = doctors.getJSONObject(i);
                                int user_id = obj.getInt("user_id");
                                if(doctorUserArraylist.stream().filter(a -> {
                                    if(a.getUserId() == user_id)return true;
                                    else return false;
                                }).count()>0){
                                    continue;
                                }
                                String name = obj.getString("name");
                                String email =  obj.getString("email");
                                String dob = obj.getString("dob");
                                String gender = obj.getString("gender");
                                String phone = obj.getString("phone");
                               // Log.d(getClass().getCanonicalName(),reason+" :: "+visited);
                                User a = new User(user_id,name,email,dob,gender,phone, Role.DOCTOR);
                                doctorUserArraylist.add(a);
                                Log.d(getClass().getCanonicalName(), "Appointment reason : " + doctorUserArraylist.size());
                            }
                            adminViewDoctorsAdapter.setUserData(doctorUserArraylist);
                            //Log.d(getClass().getCanonicalName(),filteredAppointments.size()+"");
                            adminViewDoctorsAdapter.notifyDataSetChanged();
                            break;
                        case "Failed":
                            String message = responseJson.getString("message");
                            Log.e("DoctorViewDoctorActivity", message);
                            break;
                        default:
                            Log.e("Doctor View Doctor: ", "LOL");
                            break;

                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("LoginActivity", "VolleyError: " + error.getMessage());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };
        requestQueue.add(stringRequest);
    }

    private void runFetchDoctorUserBackground(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fetchAllDoctors();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void confirmLogin(){
        if(UserSession.getInstance().isLoggedIn()){
            return;
        }else{
            //Intent for login
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}