package com.vathsav.canopy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.vathsav.canopy.database.DatabaseHandler;
import com.vathsav.canopy.database.Order;
import com.vathsav.canopy.menu.cart.CartItem;
import com.vathsav.canopy.support.JSONParser;
import com.vathsav.canopy.support.SessionManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PaymentActivity extends Activity {

    // Session Tags
    public static final String KEY_NAME = "name";
    public static final String KEY_REGISTER_NUMBER = "registerNumber";
    public static final String KEY_HOSTEL = "hostel";
    public static final String KEY_ROOM_NUMBER = "roomNumber";
    private static final String TAG_DETAILS = "details";
    private static final String TAG_BALANCE = "wallet_balance";

    // Data insertion
    // JSON node names
    private static final String TAG_SUCCESS = "success";
    int orderTotal = 0;

    // Fetch wallet balance
    String url_fetch_wallet_balance = "http://www.vathsav.com/canopy/php/fetch_wallet_balance.php";
    String walletBalance = "";
    TextView txtWalletBalance, txtOrderTotal, txtRemainingBalance;
    String session_registerNumber;
    SessionManager session;
    HashMap<String, String> userDetails;

    // Server config variables
    JSONParser jsonParser = new JSONParser();
    String url_insert_order = "http://www.vathsav.com/canopy/php/insert_food_order.php";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //todo alllow flags
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_payment);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.payment_pay_now).setVisibility(View.INVISIBLE);
        findViewById(R.id.payment_roomDetailsLayout).setVisibility(View.INVISIBLE);

        // Get session credentials
        session = new SessionManager(getApplication());
        userDetails = session.getUserDetails();
        final String registerNumber = userDetails.get(KEY_REGISTER_NUMBER);
        final String name = userDetails.get(KEY_NAME);
        final String delivery = userDetails.get(KEY_HOSTEL) + " " + userDetails.get(KEY_ROOM_NUMBER);

        session_registerNumber = registerNumber;

        txtWalletBalance = (TextView) findViewById(R.id.payment_txtBalance);
        txtRemainingBalance = (TextView) findViewById(R.id.payment_txtRemainingBalance);
        txtOrderTotal = (TextView) findViewById(R.id.payment_orderTotal);

        // Balance

        if (isNetworkAvailable()) {
            // todo remove this
            new FetchWalletDetails(session_registerNumber).execute();
        } else {
            Toast.makeText(getApplicationContext(), "Unable to establish connection to the internet", Toast.LENGTH_SHORT).show();
        }

        // Order Total
        // Fetch order from local DB
        DatabaseHandler db = new DatabaseHandler(this);
        List<Order> orderList = db.getAllItems();

        String orderDetails = "";

        for (Order order : orderList) {
            String log = "Item: " + order.getName() + ", Price: " + order.getPrice() + ", Quantity: " + order.getQuantity();
            Log.d("DB: ", log);

            if (orderDetails.isEmpty()) {
                orderDetails = log;
            } else {
                orderDetails = orderDetails + "\n" + log;
            }
            if (Integer.parseInt(order.getQuantity()) != 0) {
                int price = Integer.parseInt(order.getPrice()) / Integer.parseInt(order.getQuantity());
                orderTotal = orderTotal + price;
            }
        }

        findViewById(R.id.payment_radio_button_room_delivery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkIfHostler()) {
                    findViewById(R.id.payment_roomDetailsLayout).setVisibility(View.VISIBLE);
                    showPayButton();
                } else {
                    Toast.makeText(getApplicationContext(), "This feature is available only for hostlers", Toast.LENGTH_LONG).show();
                    findViewById(R.id.payment_roomDetailsLayout).setVisibility(View.INVISIBLE);
                }
            }
        });

        findViewById(R.id.payment_radio_button_counter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.payment_roomDetailsLayout).setVisibility(View.INVISIBLE);
                showPayButton();
            }
        });

        final String details = orderDetails;

        // If make payment button is clicked

        findViewById(R.id.payment_pay_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(PaymentActivity.this)
                        .setTitle("Payment Confirmation")
                        .setMessage("Are you sure?")
                        .setPositiveButton(R.string.payment_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                TextView remainingBalance = (TextView) findViewById(R.id.payment_txtRemainingBalance);
                                int balance = Integer.parseInt(remainingBalance.getText().toString().substring(2));
                                if (isNetworkAvailable()) {
                                    if (balance > -1) {
                                        InsertOrder insertOrder = new InsertOrder(registerNumber, name, details, delivery);
                                        insertOrder.execute();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Insufficient balance. Cannot place order.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Unable to connect to the internet. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.payment_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });
    }

    private void showPayButton() {
        findViewById(R.id.payment_pay_now).setVisibility(View.VISIBLE);
        TranslateAnimation animation = new TranslateAnimation(
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 300, Animation.ABSOLUTE, 0
        );
        animation.setDuration(500);
        findViewById(R.id.payment_pay_now).setAnimation(animation);
    }

    private boolean checkIfHostler() {
        if (userDetails.get(KEY_HOSTEL) != "Day Scholar") {
            TextView txtHostel = (TextView) findViewById(R.id.payment_hostel);
            TextView txtRoomNumber = (TextView) findViewById(R.id.payment_room_number);
            txtHostel.setText(userDetails.get(KEY_HOSTEL));
            txtRoomNumber.setText(userDetails.get(KEY_ROOM_NUMBER));
            return true;
        } else {
            return false;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // todo make this page efficient. Not satifisfied

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class InsertOrder extends AsyncTask<String, String, Boolean> {

        private final String register_number;
        private final String name;
        private final String orderDetails;
        private final String delivery;

        InsertOrder(String _registerNumber, String _name, String _orderDetails, String _delivery) {
            register_number = _registerNumber;
            name = _name;
            orderDetails = _orderDetails;
            delivery = _delivery;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PaymentActivity.this);
            progressDialog.setMessage("Processing your order..");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... args) {
            int success;

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("register_number", register_number));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("details", orderDetails));
            params.add(new BasicNameValuePair("delivery", delivery));
            JSONObject json = jsonParser.makeHttpRequest(url_insert_order, "POST", params);

            try {
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(final Boolean success) {
            if (success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
                        databaseHandler.deleteAll();
                        Intent showHomeActivity = new Intent("com.vathsav.canopy.HOME");
                        startActivity(showHomeActivity);
                        finish();
                        Toast.makeText(getApplicationContext(), "Your order has been placed!", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Your order couldn't be placed. An error occurred.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            progressDialog.dismiss();
        }
    }

    public class FetchWalletDetails extends AsyncTask<String, String, Boolean> {

        private final String register_number;

        FetchWalletDetails(String registerNumber) {
            register_number = registerNumber;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PaymentActivity.this);
            progressDialog.setMessage("Contacting server..");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... args) {
            int success;

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("register_number", register_number));
            JSONObject json = jsonParser.makeHttpRequest(url_fetch_wallet_balance, "GET", params);
            try {
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    JSONArray result = json.getJSONArray(TAG_DETAILS);
                    JSONObject details = result.getJSONObject(0);
                    walletBalance = details.getString(TAG_BALANCE);
                    Log.d("Async Task: ", walletBalance);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(final Boolean success) {
            txtWalletBalance.setText("₹ " + walletBalance);
            txtOrderTotal.setText("₹ " + orderTotal);
            txtRemainingBalance.setText("₹ " + String.valueOf(
                            Integer.parseInt(txtWalletBalance.getText().toString().substring(2)) -
                                    Integer.parseInt(txtOrderTotal.getText().toString().substring(2))
                    )
            );
            progressDialog.dismiss();
        }
    }
}
