package com.gms.app.ewhabarcode;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gms.app.ewhabarcode.dialog.CashDialog;
import com.gms.app.ewhabarcode.dialog.DummyDialog;
import com.gms.app.ewhabarcode.dialog.ManualDialog;
import com.gms.app.ewhabarcode.dialog.NoGasDialog;
import com.gms.app.ewhabarcode.dialog.StockReportDialog;
import com.gms.app.ewhabarcode.dialog.TankDialog;
import com.gms.app.ewhabarcode.domain.BottleVO;
import com.gms.app.ewhabarcode.domain.CustomerSimpleVO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements BottomSheetDialog.BottomSheetListener {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 3;
    public BluetoothAdapter mBluetoothAdapter = null;
    Set<BluetoothDevice> mDevices;
    int mPairedDeviceCount;
    BluetoothDevice mRemoteDevice;
    BluetoothSocket mSocket;
    InputStream mInputStream;
    OutputStream mOutputStream;
    Thread mWorkerThread;
    int readBufferPositon;      //?????? ??? ?????? ?????? ?????? ??????
    byte[] readBuffer;      //?????? ??????
    byte mDelimiter = 10;

    private TextView tv_result;
    private static TextView tv_bottleCount;
    private int readBufferPosition; // ?????? ??? ?????? ?????? ??????

    private Button btn_logout, btn_setting,
            btn_rental, btn_back, btn_sales, btn_ln2,
            btn_noGas, btn_mass, btn_money, btn_rentB,
            btn_dummy, btn_tank, btn_order, btn_report,
            btn_scan, btn_manual, btn_history, btn_etc;

    private TextView main_label;
    private int REQUEST_TEST = 1;
    private static ArrayList<MainData> arrayList;
    private static MainAdapter mainAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private static final String shared = "file";
    private String userId = "";
    private String previousBottles = "";
    private String host = "";
    private String version = "";
    private static final boolean closeB = true;

    //qr code scanner object
    private IntentIntegrator qrScan;
    int tempInd = 0;
    static int iCount = 0;
    private LinearLayout titleLayout;

    String[] items;
    static boolean connectBlue = false; // bluetooth ?????? ??????
    static boolean isScan = false;  //qrScan ??????
    static boolean isMainActivity = true;

    static SharedPreferences sharedPreferences = null;

    private static String appVersion = "";
    private static String appVersionCheckDate = "";
    private long versionCode = 0;
    private static boolean isGetC = false;

    @Override
    protected void onPause() {
        super.onPause();

        String arrStr = "";
        if (arrayList != null && arrayList.size() > 0) {
            for (int i = 0; i < arrayList.size(); i++) {
                MainData mainD = arrayList.get(i);
                arrStr += mainD.getTv_bottleBarCd() + "@";
            }
            //SharedPreferences sharedPreferences = getSharedPreferences(shared, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("notSaveArray", arrStr);
            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMainActivity = true;
//        Log.d("MainActivity", "onResume");
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                //WIFI??? ?????????
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                //LTE(???????????????)??? ?????????
            }
        } else {
            // ??????????????????
            AlertDialog.Builder builder1
                    = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_DARK);
            builder1.setTitle("??????????????????")
                    .setMessage("???????????? ???????????? ??????????????? ")
                    .setPositiveButton("??????", null);
            AlertDialog ad = builder1.create();

            ad.show();
        }
        if (!isScan) {
            //SharedPreferences sharedPreferences = getSharedPreferences(shared, 0);
            String arrStr = sharedPreferences.getString("notSaveArray", "");

            if (arrStr != null && arrStr.length() > 1) {
                clearArrayList();
                String[] aCode = arrStr.split("@");
                Button btn_info = findViewById(R.id.btn_buyback);

                for (int i = 0; i < aCode.length; i++) {

                    // ????????? ?????? ?????? ????????????
                    Gson gson = new Gson();
                    String sharedValue = sharedPreferences.getString(aCode[i], "");

                    if (sharedValue != null && sharedValue.length() > 10) {
                        BottleVO bottle = new BottleVO();

                        bottle = (BottleVO) gson.fromJson(sharedValue, bottle.getClass());

                        if (bottle != null && bottle.getBottleBarCd() != null) {
                            MainData mainData = new MainData(bottle.getBottleId(), bottle.getBottleBarCd(), bottle.getProductNm(), bottle.getMenuType() + "???", btn_info);
                            arrayList.add(mainData);
                        }
                    }
                }
                mainAdapter.notifyDataSetChanged();
                tv_bottleCount.setText("????????? ????????? :  " + arrayList.size());
            }
        }
        if (checkCustomerData(sharedPreferences) && !isGetC)
            new HttpAsyncTask().execute(host + getString(R.string.api_customerList));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        host = getString(R.string.host_name);
//        Log.d("MainActivity", "onCreate");
        isMainActivity = true;
        // ?????? ?????? ????????????
        PackageInfo packageInfo = null;

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            if (packageInfo != null) {
                version = packageInfo.versionName;
                /*
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    versionCode = packageInfo.getLongVersionCode(); // avoid huge version numbers and you will be ok
                } else {
                    versionCode = packageInfo.versionCode;
                }
                */
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Package Version", "NameNotFoundException");
        } catch (Exception ex) {
            version = "1.0.0";
        }

        // user_id ???????????? 0601 ??????
        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");

        //SharedPreferences ????????? ?????? ?????? ??????
        sharedPreferences = getSharedPreferences(shared, 0);
        userId = sharedPreferences.getString("id", "");

        if (userId == null || userId.length() <= 0) userId = uid;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        //String value = id.getText().toString();
        editor.putString("id", uid);
        editor.commit();

        // ????????? ?????? ??????
        if (userId == null || userId.length() <= 0) {
            Toast.makeText(MainActivity.this, "????????? ????????? ????????????.????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
            editor = sharedPreferences.edit();

            editor.clear();
            editor.commit();

            Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent1);
        }

        // ????????? ????????????
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                //WIFI??? ?????????
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                //LTE(???????????????)??? ?????????
            }
        } else {
            // ??????????????????
            AlertDialog.Builder builder1
                    = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_DARK);
            builder1.setTitle("??????????????????")
                    .setMessage("???????????? ???????????? ??????????????? ")
                    .setPositiveButton("??????", null);
            AlertDialog ad = builder1.create();

            ad.show();
        }
        Log.d("MainActivity", "Build.VERSION.SDK_INT =" + Build.VERSION.SDK_INT);
        Log.d("MainActivity", "Build.VERSION_CODES.S =" + Build.VERSION_CODES.S);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("MainActivity", "this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) =" + this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN));
            Log.d("MainActivity", "PackageManager.PERMISSION_GRANTED =" + PackageManager.PERMISSION_GRANTED);
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("??????????????? ?????? ???????????? ???????????????");
                builder.setMessage("????????????????????? ??????????????? ?????? ??? ??? ????????? ?????? ?????? ????????? ????????? ??????????????????.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                    }
                });

                builder.show();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("??????????????? ?????? ???????????? ???????????????");
                builder.setMessage("????????????????????? ??????????????? ?????? ??? ??? ????????? ?????? ?????? ????????? ????????? ??????????????????.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3);
                    }
                });
                builder.show();
            }
        }
        // ????????? ??????
        /*
        SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy.MM.dd", Locale.KOREA );
        Date currentTime = new Date ( );
        String dTime = formatter.format ( currentTime );
*/
        //if(!dTime.equals(appVersionCheckDate)) {
        /* 20210212
            String appVersionUrl = host + getString(R.string.api_appVersion);

            // AsyncTask??? ?????? HttpURLConnection ??????.
            AppNetworkTask appNetworkTask = new AppNetworkTask(appVersionUrl, null);
            appNetworkTask.execute();
        //s}
        */
        // ??????????????? ????????????
        String value = sharedPreferences.getString("clist", "");

        if (value == null || value.length() <= 10 || checkCustomerData(sharedPreferences)) {
            isGetC = true;
            new HttpAsyncTask().execute(host + getString(R.string.api_customerList));
        }
        tv_bottleCount = (TextView) findViewById(R.id.tv_bottleCount);   // ????????? ?????? ????????????
        tv_bottleCount.setText("????????? ????????? : " + iCount);

        btn_logout = (Button) findViewById(R.id.btn_logout);     //????????????
        btn_setting = (Button) findViewById(R.id.btn_setting);     //???????????? ??????

        btn_rental = (Button) findViewById(R.id.btn_rental);         // ??????
        btn_back = (Button) findViewById(R.id.btn_back);       // ??????
        btn_sales = (Button) findViewById(R.id.btn_sales);       // ??????
        btn_ln2 = (Button) findViewById(R.id.btn_ln2);       // LN2

        btn_noGas = (Button) findViewById(R.id.btn_noGas);       // ????????????
        btn_mass = (Button) findViewById(R.id.btn_mass);     //??????
        btn_money = (Button) findViewById(R.id.btn_money);       // ??????/?????????
        btn_rentB = (Button) findViewById(R.id.btn_rentB);       // ????????????

        btn_dummy = (Button) findViewById(R.id.btn_dummy);       // ???????????????
        btn_tank = (Button) findViewById(R.id.btn_tank);     //Tank
        btn_order = (Button) findViewById(R.id.btn_buyback);         //??????
        btn_report = (Button) findViewById(R.id.btn_report);       // ????????????

        btn_scan = (Button) findViewById(R.id.btn_scan);         // ????????????
        btn_manual = (Button) findViewById(R.id.btn_manual);       // ????????????
        btn_history = (Button) findViewById(R.id.btn_history);       // ????????????
        btn_etc = (Button) findViewById(R.id.btn_etc);        // ??????

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);
        linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setReverseLayout(true);   // topbottom
//        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        arrayList = new ArrayList<>();
        mainAdapter = new MainAdapter(arrayList);
        recyclerView.setAdapter(mainAdapter);

        titleLayout = (LinearLayout) findViewById(R.id.title_layout);

        //intializing scan object
        qrScan = new IntentIntegrator(this);
        //IntentIntegrator integrator = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        //qrScan.setCaptureActivity(MainActivity.class);
        //qrScan.initiateScan();

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                qrScan.setPrompt("?????????...");
                qrScan.setBeepEnabled(false);//????????? ????????? ??????
                qrScan.setOrientationLocked(true); //20210212(?????????????????????)
                qrScan.initiateScan();
            }
        });


        main_label = (TextView) findViewById(R.id.main_label);

        btn_rental.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrayList.size() <= 0) {
                    Toast.makeText(MainActivity.this, "????????? ???????????????", Toast.LENGTH_SHORT).show();
                } else {
                    CustomDialog customDialog = new CustomDialog(MainActivity.this, "??????");

                    String tempStr = "";
                    for (int i = 0; i < arrayList.size(); i++) {
                        tempStr += arrayList.get(i).getTv_bottleBarCd() + ",";
                    }

                    // ????????? ?????????????????? ????????????.
                    customDialog.callFunction(tempStr, userId);
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrayList.size() <= 0) {
                    Toast.makeText(MainActivity.this, "????????? ???????????????", Toast.LENGTH_SHORT).show();
                } else {
                    CustomDialog customDialog = new CustomDialog(MainActivity.this, btn_back.getText().toString());

                    String tempStr = "";
                    for (int i = 0; i < arrayList.size(); i++) {
                        tempStr += arrayList.get(i).getTv_bottleBarCd() + ",";
                    }

                    // ????????? ?????????????????? ????????????.
                    customDialog.callFunction(tempStr, userId);
                }
            }
        });

        btn_sales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (arrayList.size() <= 0) {
                    Toast.makeText(MainActivity.this, "????????? ???????????????", Toast.LENGTH_SHORT).show();
                } else {
                    CustomDialog customDialog = new CustomDialog(MainActivity.this, "??????");

                    String tempStr = "";
                    for (int i = 0; i < arrayList.size(); i++) {
                        tempStr += arrayList.get(i).getTv_bottleBarCd() + ",";
                    }

                    // ????????? ?????????????????? ????????????.
                    customDialog.callFunction(tempStr, userId);
                }
            }
        });

        btn_ln2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoGasDialog noGasDialog = new NoGasDialog(MainActivity.this, btn_ln2.getText().toString());

                // ????????? ?????????????????? ????????????.
                noGasDialog.callFunction(userId);
            }
        });

        //????????????
        btn_noGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NoGasDialog noGasDialog = new NoGasDialog(MainActivity.this, btn_noGas.getText().toString());

                // ????????? ?????????????????? ????????????.
                noGasDialog.callFunction(userId);
            }
        });

        btn_mass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MassActivity.class);
                intent.putExtra("uid", userId);
                isMainActivity = false;
                startActivity(intent);
            }
        });

        btn_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ?????? ??? ?????????
                CashDialog cash = new CashDialog(MainActivity.this);
                cash.callFunction(userId);
            }
        });

        btn_rentB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StockReportDialog customDialog = new StockReportDialog(MainActivity.this, btn_rentB.getText().toString());

                // ????????? ?????????????????? ????????????.
                customDialog.callFunction();
            }
        });

        btn_dummy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DummyDialog dummyDialog = new DummyDialog(MainActivity.this, btn_dummy.getText().toString());

                // ????????? ?????????????????? ????????????.
                dummyDialog.callFunction(arrayList, mainAdapter);

            }
        });

        btn_tank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TankDialog tankDialog = new TankDialog(MainActivity.this);

                // ????????? ?????????????????? ????????????.
                tankDialog.callFunction(userId);
            }
        });

        btn_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, OrderListActivity.class);
                intent.putExtra("uid", userId);
                isMainActivity = false;
                startActivity(intent);
            }
        });

//        btn_as.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if(arrayList.size() <= 0){
//                    Toast.makeText(MainActivity.this, "????????? ???????????????", Toast.LENGTH_LONG).show();
//                }else {
//
//                    String tempStr = "";
//                    for (int i = 0; i < arrayList.size(); i++) {
//                        tempStr += arrayList.get(i).getTv_bottleBarCd() + ",";
//                    }
//
//                    // ?????? ??? ?????????
//                    AsSheetDialog asSheet = new AsSheetDialog(MainActivity.this,tempStr);
//                    asSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
//
//                }
//            }
//        });
        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                // user_id ???????????? 0601 ??????
                intent.putExtra("uid", userId);
                startActivity(intent);
            }
        });

        //????????????
        btn_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ManualDialog manualDialog = new ManualDialog(MainActivity.this);

                // ????????? ?????????????????? ????????????.
                manualDialog.callFunction(arrayList, mainAdapter);
            }
        });


        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                previousBottles = sharedPreferences.getString("previousBottles", "");
                //Log.d("previousBottles", previousBottles);

                if (previousBottles != null && previousBottles.length() > 0) {
                    String[] aCode = previousBottles.split(",");
                    Button btn_info = findViewById(R.id.btn_buyback);

                    for (int i = 0; i < aCode.length; i++) {
                        //Log.d("previousBottles", "aCode " + aCode[i]);

                        // ????????? ?????? ?????? ????????????
                        Gson gson = new Gson();
                        String sharedValue = sharedPreferences.getString(aCode[i], "");

                        BottleVO bottle = new BottleVO();
                        bottle = (BottleVO) gson.fromJson(sharedValue, bottle.getClass());
                        boolean updateFlag = true;
                        for (int j = 0; j < arrayList.size(); j++) {
                            if (arrayList.get(j).getTv_bottleBarCd().equals(bottle.getBottleBarCd()))
                                updateFlag = false;
                        }
                        if (updateFlag) {
                            MainData mainData = new MainData(bottle.getBottleId(), bottle.getBottleBarCd(), bottle.getProductNm(), bottle.getMenuType() + "???", btn_info);
                            arrayList.add(mainData);
                        }
                    }
                    mainAdapter.notifyDataSetChanged();
                    tv_bottleCount.setText("????????? ????????? :  " + arrayList.size());
                } else {
                    Toast.makeText(MainActivity.this, "?????? ?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_etc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (arrayList.size() <= 0) {
                    Toast.makeText(MainActivity.this, "????????? ???????????????", Toast.LENGTH_LONG).show();
                } else {

                    String tempStr = "";
                    for (int i = 0; i < arrayList.size(); i++) {
                        tempStr += arrayList.get(i).getTv_bottleBarCd() + ",";
                    }

                    // ?????? ??? ?????????
                    BottomSheetDialog bottomSheet = new BottomSheetDialog(MainActivity.this, tempStr);
                    bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");

                }
            }
        });
        /*
        btn_deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(arrayList.size() > 0 ){

                    AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                    ad.setMessage("???????????? ?????????????????????????");

                    ad.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(MainActivity.this, "???????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                            MainActivity.clearArrayList();
                            dialog.dismiss();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("notSaveArray");
                            editor.commit();
                        }
                    });

                    ad.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }else{
                    Toast.makeText(MainActivity.this, "????????? ??????????????? ????????????.", Toast.LENGTH_SHORT).show();
                }
            }
        });
*/
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SharedPreferences ????????? ?????? ?????? ??????
                //SharedPreferences sharedPreferences = getSharedPreferences(shared,0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //String value = id.getText().toString();
                editor.clear();
                editor.commit();

                try {
                    if (mSocket != null)
                        mSocket.close();
                } catch (IOException e) {

                }
                Toast.makeText(MainActivity.this, "???????????? ???????????????,", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDevice();
            }
        });

        //??????????????? ????????? ???????????? ??????
        if (!connectBlue)
            CheckBluetooth();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("?????????", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("?????? ??????");
                    builder.setMessage("?????? ?????? ??? ????????? ????????? ???????????? ??????????????? ??????????????? ?????? ??? ???????????? ????????????.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                break;
            }
            case 2: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("?????????", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("?????? ??????");
                    builder.setMessage("???????????? ??????????????? ???????????? ???????????????.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                break;
            }
            case 3: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("?????????", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("?????? ??????");
                    builder.setMessage("???????????? ?????? ????????? ???????????? ???????????????.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                break;
            }
        }
        return;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //Log.d("onWindowFocusChangee==========","btn_setting.width = " + btn_setting.getWidth() + "\nmain_label.width = " + main_label.getWidth());
        ViewGroup.LayoutParams params = main_label.getLayoutParams();
        params.width = titleLayout.getWidth() - btn_setting.getWidth() * 4;

        main_label.setLayoutParams(params);
    }


    @Override
    public void onBackPressed() {
        //Toast.makeText(this,"?????? ??? ????????? ???????????????", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);

        ad.setMessage("???(V" + version + ")??? ?????????????????????????");

        ad.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                moveTaskToBack(MainActivity.closeB);
                finish();
                Process.killProcess(Process.myPid());
                dialog.dismiss();
            }
        });

        ad.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        ad.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "--");
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //qrcode ??? ?????????
            if (result.getContents() == null) {
                String arrStr = "";
                if (arrayList != null && arrayList.size() > 0) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        MainData mainD = arrayList.get(i);
                        arrStr += mainD.getTv_bottleBarCd() + "@";
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("notSaveArray", arrStr);
                    editor.commit();
                }
                Toast.makeText(MainActivity.this, "??????!", Toast.LENGTH_SHORT).show();
                isScan = false;
            } else {
                //qrcode ????????? ?????????
                //Toast.makeText(MainActivity.this, "????????????!"+result.getContents(), Toast.LENGTH_SHORT).show();
                try {
                    boolean isBeen = false;
                    for (int i = 0; i < arrayList.size(); i++) {
                        if (arrayList.get(i).getTv_bottleBarCd().equals(result.getContents()))
                            isBeen = true;
                    }

                    if (!isBeen) {
                        String url = host + getString(R.string.api_bottleDetail) + "?bottleBarCd=" + result.getContents();//AA315923";

                        // AsyncTask??? ?????? HttpURLConnection ??????.
                        NetworkTask networkTask = new NetworkTask(url, null);
                        networkTask.execute();
                        //data??? json?????? ??????
                        JSONObject obj = new JSONObject(result.getContents());
                        //Toast.makeText(MainActivity.this, obj.getString("name"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                qrScan.initiateScan();  //???????????? ??????
                isScan = true;
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onButtonClicked(String text) {
        //super(text);
        //mTextView.setText(text);
    }


    // ?????? Bluetooth ?????? ??????
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void CheckBluetooth() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // ????????? ???????????? ???????????? ?????? ??????
            Toast.makeText(MainActivity.this, "Bluetooth??? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
        } else {
            // ????????? ???????????? ???????????? ??????
            if (!mBluetoothAdapter.isEnabled()) {
                // ??????????????? ??????????????? ????????? ????????? ??????
                // ??????????????? ?????? ????????? ????????? ?????? ????????? ?????? ??????
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setTitle("?????? ??????");
//                    builder.setMessage("???????????? ?????? ????????? ???????????? ???????????????.");
//                    builder.setPositiveButton(android.R.string.ok, null);
//                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                        }
//
//                    });
//                    builder.show();
//                    return;
//                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // ??????????????? ???????????? ?????? ????????? ??????
                // ???????????? ?????? ????????? ???????????? ????????? ????????? ??????.

                selectDevice();
            }
        }
    }

    private void selectDevice() {
        //?????????????????? ?????? ?????? ??????

        // Use this check to determine whether Bluetooth classic is supported on the device.
// Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "???????????? ????????????", Toast.LENGTH_SHORT).show();
            finish();
        }
// Use this check to determine whether BLE is supported on the device. Then
// you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "???????????? ????????????", Toast.LENGTH_SHORT).show();
            finish();
        }

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//           // return;
//        }
        mDevices = mBluetoothAdapter.getBondedDevices();
        //?????????????????? ?????? ??????
        mPairedDeviceCount = mDevices.size();
        //Alertdialog ??????(activity?????? context??????)
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        //AlertDialog ?????? ??????
        builder.setTitle("????????? ??????????????????");

        // ????????? ??? ???????????? ????????? ?????? ?????? ??????
        final List<String> listItems = new ArrayList<String>();
        for (BluetoothDevice device : mDevices) {
            listItems.add(device.getName());
        }
        if (listItems.size() == 0) {
            //no bonded device => searching
            //Log.d("Bluetooth", "No bonded device");
        } else {
            // ?????? ?????? ??????
            listItems.add("Cancel");

            final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                //??? ???????????? click??? ?????? listener??? ??????
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Dialog dialog_ = (Dialog) dialog;
                    // ????????? ????????? ???????????? ?????? '??????'??? ?????? ??????
                    if (which == listItems.size() - 1) {
                        Toast.makeText(dialog_.getContext(), "????????? ??????????????????", Toast.LENGTH_SHORT).show();

                    } else {
                        //????????? ?????? ??????????????? ????????? ?????? ?????? ????????? ??????
                        connectToSelectedDevice(items[which].toString());
                        connectBlue = true;
                        //Toast.makeText(MainActivity.this, "??????????????? connectBlue."+connectBlue, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setCancelable(false);    // ?????? ?????? ?????? ?????? ??????
            AlertDialog alert = builder.create();
            if (mSocket != null && mSocket.isConnected()) {
                Toast.makeText(MainActivity.this, "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(MainActivity.this, "??????????????? ???????????? ???????????????", Toast.LENGTH_SHORT).show();
            }
            alert.show();   //alert ??????
        }
    }

    private void connectToSelectedDevice(final String selectedDeviceName) {
        //???????????? ????????? ???????????? ????????? ????????? ????????? ????????? ?????? ????????? ????????? ?????? GUI??? ????????? ?????????
        //????????? ?????? ????????? thread??? ???????????? thread??? ?????? ????????? ?????? ?????? ???????????? ????????????.

        //handler??? thread?????? ????????? ???????????? ?????? ?????? ????????? ???????????????.
        final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                //Log.i("***************** MainActivity  connectToSelectedDevice msg",msg.toString());
                if (msg.what == 1) // ?????? ??????
                {
                    try {

                        //????????? ???????????? ???????????? outstream??? inputstream??? ?????????. ??????????????? ??????
                        //???????????? ?????? ?????? ????????? ??????.
                        //mOutputStream = mSocket.getOutputStream();
                        InputStream tempInput = mSocket.getInputStream();
                        mInputStream = tempInput;

                        // ????????? ?????? ??????
                        beginListenForData();
                        //receiveData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {    //?????? ??????
                    //Toast.makeText(MainActivity.this ,"????????? ??????????????????", Toast.LENGTH_SHORT).show();
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //??????????????? ????????? thread ??????
        Thread thread = new Thread(new Runnable() {
            public void run() {
                //????????? ????????? ????????? ?????? bluetooth device??? object
                mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
                //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                Log.d("MainActivity", "PackageActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT)  =" + ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) );
                Log.d("MainActivity", "PackageManager.PERMISSION_GRANTED =" + PackageManager.PERMISSION_GRANTED);
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    //;
//                }
                UUID uuid = (mRemoteDevice.getUuids())[0].getUuid();

                try {
                    // ?????? ??????
                    mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);

                    // RFCOMM ????????? ?????? ??????, socket??? connect????????? ????????? ?????????. ????????? ui??? ????????? ?????? ?????? ????????????
                    // Thread??? ?????? ????????? ???????????? ??????.
                    mSocket.connect();
                    mHandler.sendEmptyMessage(1);
                    connectBlue = true;
                } catch (Exception e) {
                    // ???????????? ?????? ??? ?????? ??????
                    mHandler.sendEmptyMessage(-1);
                    //mSocket.close();
                    Log.e("Excception e", e.getMessage());
                }
            }
        });

        //?????? thread??? ????????????
        thread.start();
    }

    //????????? ???????????? ?????? ?????? ????????? ?????? ???????????? ??????????????? bluetoothdevice ????????? ???????????? ??????
    //bluetoothdevice????????? ????????? ??????????????? ????????? ????????? ????????? ???????????? ??????.

    BluetoothDevice getDeviceFromBondedList(String name) {
        BluetoothDevice selectedDevice = null;
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return null;
//        }
        mDevices = mBluetoothAdapter.getBondedDevices();
        //pair ???????????? ?????? ????????? ?????? ?????? ??????, ????????? ?????? device ??????
        for (BluetoothDevice device : mDevices) {
            if (name.equals(device.getName())) {
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }

    //???????????? ????????? ?????? Listener
    protected void beginListenForData() {
        final Handler handler = new Handler();
        readBuffer = new byte[1024];  //  ?????? ??????
        readBufferPositon = 0;        //   ?????? ??? ?????? ?????? ?????? ??????
        //Log.i("***************** MainActivity  beginListenForData msg","start");
        // ????????? ?????? ?????????
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!Thread.currentThread().isInterrupted()) {

                    try {
                        int bytesAvailable = 0;
                        if(mInputStream!=null)
                            bytesAvailable = mInputStream.available();

                        if (bytesAvailable > 0) { //???????????? ????????? ??????
                            connectBlue = true;
                            byte[] packetBytes = new byte[bytesAvailable];
                            //mInputStream.read(packetBytes);
                            int bytesInt = mInputStream.read(packetBytes);
                            String readMessage = new String(packetBytes, 0, bytesInt);

                            if(readMessage.indexOf("\u0001") > 0){
                                readMessage = readMessage.substring(readMessage.indexOf("\u0001")+1,readMessage.length());
                            }
                            if(readMessage.indexOf("\u0003") > 0){
                                readMessage = readMessage.substring(0,readMessage.indexOf("\u0003"));
                            }

                            if(readMessage !=null && readMessage.length() > 0) {

                                //String url = host + "api/bottleDetail.do?bottleBarCd=" + readMessage.substring(5, readMessage.length());//AA315923";
                                String url = host + getString(R.string.api_bottleDetail)+"?bottleBarCd=" + readMessage;//AA315923";
                                // AsyncTask??? ?????? HttpURLConnection ??????.
                                NetworkTask networkTask = new NetworkTask(url, null);
                                networkTask.execute();
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //????????? ?????? thread ??????
        mWorkerThread.start();
    }

    static public  void  clearArrayList(){
        arrayList.clear();
        mainAdapter.notifyDataSetChanged();
        tv_bottleCount.setText("????????? ????????? :  0");
        iCount = 0;
    }

    static  ArrayList<MainData>  getArrayList(){
        return arrayList;
    }

    static public void  setTextBottleCount(){

        tv_bottleCount.setText("????????? ????????? : "+arrayList.size());
    }

    public void SendResetSignal(){
        String msg = "bs00000";
        try {
            mOutputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkCustomerData(SharedPreferences sharedPreferences){
        Date today = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String toDate = df.format(today);

        String getDate = sharedPreferences.getString("cdate", "");
        //Log.d(TAG, "checkCustomerData: " + getDate+ "==toDate: "+toDate);
        if(getDate != null && getDate.equals(toDate)){
            return false;
        }else
            return true;
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // ?????? ????????? ????????? ??????.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // ?????? URL??? ?????? ???????????? ????????????.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()??? ?????? ????????? ?????? onPostExecute()??? ??????????????? ??????????????? s??? ????????????.
            String bottleBarCd ="";
            String bottleId ="";
            String productNm ="";
            String bottleChargeDt = null;
            Button btn_info = findViewById(R.id.btn_buyback);
            try {
                Gson gson = new Gson();
                BottleVO bottle = new BottleVO();
                bottle = (BottleVO) gson.fromJson(s, bottle.getClass());
                if(bottle != null && bottle.getBottleBarCd() !=null && bottle.getBottleBarCd().length() > 5){
                    if(!isMainActivity) {

                        MassActivity.addMassData(bottle);
                    }else{

                        bottleId = bottle.getBottleId();
                        bottleBarCd = bottle.getBottleBarCd();
                        productNm = bottle.getProductNm();
                        bottleChargeDt = bottle.getMenuType() + "???";

                        SharedPreferences sharedPreferences = getSharedPreferences(shared, 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString(bottleBarCd, s);
                        editor.commit();

                        boolean updateFlag = true;
                        for (int i = 0; i < arrayList.size(); i++) {
                            if (arrayList.get(i).getTv_bottleBarCd().equals(bottleBarCd))
                                updateFlag = false;
                        }

                        if (updateFlag) {
                            MainData mainData = new MainData(bottleId, bottleBarCd, productNm, bottleChargeDt, btn_info);

                            arrayList.add(mainData);
                            mainAdapter.notifyDataSetChanged();
                            iCount++;
                            tv_bottleCount.setText("????????? ????????? : " + arrayList.size());
                            Toast.makeText(MainActivity.this, bottleBarCd+"??? ??????????????????.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this, "???????????? ???????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                }


            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "?????? ????????? ???????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, List<CustomerSimpleVO>> {
        private final String TAG = HttpAsyncTask.class.getSimpleName();
        // int REQUEST_CODE =
        // OkHttp ???????????????
        OkHttpClient client = new OkHttpClient();

        @Override
        protected List<CustomerSimpleVO> doInBackground(String... params) {
            List<CustomerSimpleVO> customerList = new ArrayList<>();
            String strUrl = params[0];
            try {
                // ??????
                Request request = new Request.Builder()
                        .url(strUrl)
                        .build();
                // ??????
                Response response = client.newCall(request).execute();

                Gson gson = new Gson();

                // import java.lang.reflect.Type
                Type listType = new TypeToken<ArrayList<CustomerSimpleVO>>() {
                }.getType();
                customerList = gson.fromJson(response.body().string(), listType);

                //Log.d(TAG, "onCreate: " + customerList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return customerList;
        }

        @Override
        protected void onPostExecute(List<CustomerSimpleVO> customerList) {
            super.onPostExecute(customerList);

            StringBuffer sb = new StringBuffer();
            items = new String[customerList.size()];
            for (int i = 0; i < customerList.size(); i++) {
                items[i] = customerList.get(i).getCustomerNm().toString();
                sb.append(customerList.get(i).getCustomerNm().toString());
                sb.append("#");
            }
            //SharedPreferences sharedPreferences = getSharedPreferences(shared,0);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            Date today = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            String toDate = df.format(today);

            editor.putString("clist", sb.toString());
            editor.putString("cdate", toDate);
            editor.commit();
            isGetC = false;
        }
    }

    public class AppNetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public AppNetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // ?????? ????????? ????????? ??????.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // ?????? URL??? ?????? ???????????? ????????????.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if(s != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy.MM.dd", Locale.KOREA );
                    Date currentTime = new Date ( );
                    appVersionCheckDate = formatter.format ( currentTime );

                    appVersion = s;
                    long iAppVersion = Long.parseLong(appVersion);
                    versionCode = Long.parseLong(getString(R.string.VersionCode));
                    if (iAppVersion > versionCode) {

                        // Linkify the message
                        //final SpannableString spans = new SpannableString(Html.fromHtml("<a href=\"http://www.google.com\">Check this link out</a>")); // msg should have url to enable clicking
                        //Linkify.addLinks(spans, Linkify.ALL);

                        AlertDialog.Builder builder2
                                = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_DARK);
                        builder2 .setTitle("??????????????????")
                                .setMessage("????????? ????????? ?????? ????????????. \n?????? ???????????? ????????????")
                                .setPositiveButton("??????",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                moveTaskToBack(MainActivity.closeB);
                                                finish();
                                                Process.killProcess(Process.myPid());
                                                dialog.dismiss();
                                            }
                                        });
                        AlertDialog ad = builder2.create();

                        ad.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
