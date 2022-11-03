package com.example.doan.fragments;

import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.doan.IpAddressWifi;
import com.example.doan.MyNotificationPublisher;
import com.example.doan.R;
import com.example.doan.Session;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.SimpleFormatter;

public class CreateTaskFragment extends Fragment {
    public ImageButton imageButtonDate, imageButtonTime;
    public Button btnCreateTask;
    public TextView textViewDate , textviewTime;
    public EditText editTextName ,editTextDes;
    String timeTonotify;
    IpAddressWifi ipAddressWifi ;
    String url;
    Session session;
    String nameLogin ;
    long time = new Date().getTime();
    String tmpStr = String.valueOf(time);
    String last4Str = tmpStr.substring(tmpStr.length()-5);
    int id = Integer.valueOf(last4Str);
    public CreateTaskFragment() {
        // Required empty public constructor
    }
    private  void AnhXa(View view){
        // Image Button
        imageButtonDate = (ImageButton) view.findViewById(R.id.imageButtonDate);
        imageButtonTime = (ImageButton) view.findViewById(R.id.imageButtonTime);
        //Button
        btnCreateTask = (Button) view.findViewById(R.id.buttonCreateTask);
        // Text View
        textViewDate = (TextView) view.findViewById(R.id.textViewContentDate);
        textviewTime = (TextView) view.findViewById(R.id.textviewTime);
        //Edit Text
        editTextName = (EditText) view.findViewById(R.id.editTextName);
        editTextDes = (EditText) view.findViewById(R.id.edittextDes);
        //
        session = new Session(getActivity());
        ipAddressWifi = new IpAddressWifi();
        url = "http://"+ ipAddressWifi.getIp()+ipAddressWifi.getPortLocalHost()+"/"+ipAddressWifi.getFileNameDB()+"/insertTask.php";
        Log.d("url",url);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View  view=  inflater.inflate(R.layout.fragment_create_task, container, false);
        AnhXa(view);
        Map<String,String> mapSess = session.getUserDetails();
        nameLogin = mapSess.get("user_name").toString().trim();
        imageButtonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });
        imageButtonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTime();
            }
        });
        btnCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editTextName.getText().toString().trim();
                String date = textViewDate.getText().toString().trim();
                String time =textviewTime.getText().toString().trim();
                processinsert(title,date,time);
                ListFragment fragment = new ListFragment();
                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.constraint,fragment,"fragment");
                fm.commit();
            }
        });
        return view;
    }

    public void selectTime() {
        //this method performs the time picker task
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                timeTonotify = i + ":" + i1;
                //temp variable to store the time to set alarm
                textviewTime.setText(FormatTime(i, i1));
                //sets the button text as selected time
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }
    public String FormatTime(int hour, int minute) {
        //this method converts the time into 12hr format and assigns am or pm
        String time;
        time = "";
        String formattedMinute;
        if (minute / 10 == 0) {
            formattedMinute = "0" + minute;
        } else {
            formattedMinute = "" + minute;
        }
        if (hour == 0) {
            time = "12" + ":" + formattedMinute + " AM";
        } else if (hour < 12) {
            time = hour + ":" + formattedMinute + " AM";
        } else if (hour == 12) {
            time = "12" + ":" + formattedMinute + " PM";
        } else {
            int temp = hour - 12;
            time = temp + ":" + formattedMinute + " PM";
        }
        return time;
    }
    public void selectDate() {
        //this method performs the date picker task
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                textViewDate.setText(day + "-" + (month + 1) + "-" + year);
                //sets the selected date as test for button
            }
        }, year, month, day);
        datePickerDialog.show();
    }
    private void insertTask(String url){
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.trim().equals("success")){
                    Log.d("Success",response.toString());
                } else {
                    Log.d("Error success",response.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error Insert",error.toString().trim());
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("id",String.valueOf(id).trim());
                map.put("title",editTextName.getText().toString().trim());
                map.put("content",editTextDes.getText().toString().trim());
                map.put("time",textviewTime.getText().toString().trim());
                map.put("date",textViewDate.getText().toString().trim());
                map.put("user_id",nameLogin);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }
    private void processinsert(String title, String date, String time) {
        //inserts the title,date,time into sql lite database
        setAlarm(title, date, time);
        //add mysql
        insertTask(url);
        //calls the set alarm method to set alarm
        Toast.makeText(getActivity(), "Thêm thành công", Toast.LENGTH_SHORT).show();
    }

    private void setAlarm(String text, String date, String time) {
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        //chỉ định đối tượng quản lý báo động để đặt báo thức
        Intent intent = new Intent(getActivity(), MyNotificationPublisher.class);
        intent.putExtra("event", text);
        //gửi dữ liệu đến lớp báo động để tạo kênh và thông báo
        intent.putExtra("time", date);
        intent.putExtra("date", time);
        intent.putExtra("content", editTextDes.getText().toString().trim());
        intent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), id, intent, PendingIntent.FLAG_ONE_SHOT);
        String dateandtime = date + " " + timeTonotify;
        DateFormat formatter = new SimpleDateFormat("d-M-yyyy hh:mm");
        try {
            Date date1 = formatter.parse(dateandtime);
            am.set(AlarmManager.RTC_WAKEUP, date1.getTime(), pendingIntent);
            Toast.makeText(getActivity(), "Alarm", Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ListFragment lfm = new ListFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.constraint,lfm);
        transaction.commit();
        //điều hướng từ việc thêm hoạt động nhắc nhở thành hoạt động
    }
}