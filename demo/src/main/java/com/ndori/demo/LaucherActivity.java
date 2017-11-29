package com.ndori.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ndori.demo.ItemsDemo.ItemsActivity;

public class LaucherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laucher);
    }

    public void onLoginDemo(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void onNetworkRequest(View view) {
        startActivity(new Intent(this, NetworkRequestActivity.class));
    }

    public void onItemsRequests(View view) {
        startActivity(new Intent(this, ItemsActivity.class));
    }
}
