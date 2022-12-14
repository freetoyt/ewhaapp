package com.gms.app.ewhabarcode.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gms.app.ewhabarcode.R;
import com.gms.app.ewhabarcode.adapter.OrderProductAdapter;
import com.gms.app.ewhabarcode.adapter.OrderProductData;
import com.gms.app.ewhabarcode.domain.ProductPriceSimpleVO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrderProductDialog {

    private Context context;

    List<String> spinnerArray;

    OrderProductData orderProductData;
    private static ArrayList<OrderProductData> arrayList;
    private static OrderProductAdapter oderProductAdapter;

    SharedPreferences sharedPreferences ;
    private String shared = "file";
    boolean isUpdate = true;

    private Spinner sp_product,sp_productCapa;
    List<ProductPriceSimpleVO> productList = new ArrayList<>();

    String productType = "";
    String productCapa, orderProductEtc;
    Integer productId, productPriceSeq;
    String buttonType = "";
    String userId = "";
    String host ="";
    String value ="" ;

    String strAction = "";
    String strProductType = "";

    String strProductCount = "" ;
    String bottleChangeYn = "N";
    String bottleSaleYn = "N";
    String retrievedYn = "N";
    String asYn = "N";
    String incomeYn = "N";
    String outYn = "N";

    public OrderProductDialog(Context context, String bType) {
        this.context = context;
        this.buttonType = bType;

        sharedPreferences = context.getSharedPreferences(shared, 0);
        host = context.getString(R.string.host_name);

        new HttpAsyncTask2().execute(host + context.getString(R.string.api_allProduct));
    }

    // ????????? ??????????????? ????????? ????????????.
    public void callFunction(String id , ArrayList<OrderProductData> arrayList1,  OrderProductAdapter oderProductAdapter1){

        userId = id;
        arrayList = arrayList1;
        oderProductAdapter = oderProductAdapter1;

        // ????????? ?????????????????? ?????????????????? Dialog???????????? ????????????.
        final Dialog dlg = new Dialog(context);

        // ??????????????? ??????????????? ?????????.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // ????????? ?????????????????? ??????????????? ????????????.
        dlg.setContentView(R.layout.orderproduct_dialog);

        // ????????? ?????????????????? ????????????.
        dlg.show();

        // ????????? ?????????????????? ??? ???????????? ????????????.
        final TextView title = (TextView) dlg.findViewById(R.id.title);
        final Button okButton = (Button) dlg.findViewById(R.id.okButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);
        final EditText productCount = (EditText) dlg.findViewById(R.id.productCount);
        final CheckBox ck_option1 = (CheckBox) dlg.findViewById(R.id.ck_option1) ;
        final CheckBox ck_option2 = (CheckBox) dlg.findViewById(R.id.ck_option2) ;
        final CheckBox ck_option3 = (CheckBox) dlg.findViewById(R.id.ck_option3) ;
        final CheckBox ck_option4 = (CheckBox) dlg.findViewById(R.id.ck_option4) ;
        final CheckBox ck_option5 = (CheckBox) dlg.findViewById(R.id.ck_option5) ;
        final CheckBox ck_option6 = (CheckBox) dlg.findViewById(R.id.ck_option6) ;
        final EditText et_orderProductEtc  = (EditText) dlg.findViewById(R.id.et_orderProductEtc) ;
        title.setText(buttonType);

        sp_product = (Spinner)dlg.findViewById(R.id.sp_product);
        sp_productCapa = (Spinner)dlg.findViewById(R.id.sp_productCapa);

            //productSeq.setVisibility(View.INVISIBLE);
        productCount.setText("1");

        sp_product.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                productType = parent.getItemAtPosition(position).toString();

                List<String> spinner1Array =  new ArrayList<String>();
                for (int i = 0; i < productList.size(); i++) {
                    if (productType.equals(productList.get(i).getProductNm())) {
                        productId = productList.get(i).getProductId();

                        spinner1Array.add(productList.get(i).getProductCapa());
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinner1Array);

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        sp_productCapa.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sp_productCapa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                productCapa = parent.getItemAtPosition(position).toString();
                for (int i = 0; i < productList.size(); i++) {
                    if (productType.equals(productList.get(i).getProductNm()) && productCapa.equals(productList.get(i).getProductCapa())) {
                        productPriceSeq = productList.get(i).getProductPriceSeq();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(productCount.getText().toString().equals("") || productCount.getText().toString() == null ){
                    Toast.makeText(context, "????????? ???????????????", Toast.LENGTH_SHORT).show();
                }else {
                    boolean isGo = false;

                    strProductCount = productCount.getText().toString();
                    isGo = true;

                    if(isGo) {
                        strAction = buttonType;
                        strProductType = productType;
                        orderProductEtc = et_orderProductEtc.getText().toString();
                        if (ck_option1.isChecked()) {
                            bottleChangeYn = "Y";
                        }
                        if (ck_option2.isChecked()) {
                            bottleSaleYn = "Y";
                        }
                        if (ck_option3.isChecked()) {
                            retrievedYn = "Y";
                        }
                        if (ck_option4.isChecked()) {
                            asYn = "Y";
                        }
                        if (ck_option5.isChecked()) {
                            incomeYn = "Y";
                        }
                        if (ck_option6.isChecked()) {
                            outYn = "Y";
                        }

                        AlertDialog.Builder ad = new AlertDialog.Builder(context);
                        ad.setMessage(String.format("\"%s %s??? %s\"????????????????", strProductType,productCount.getText().toString(), strAction));

                        ad.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                boolean isAdded = false;
                                for(int i =0 ; i < arrayList.size() ; i++){
                                    OrderProductData orderProductDataTemp = arrayList.get(i);
                                    if(orderProductDataTemp.getTv_productId().equals(productId)
                                            && orderProductDataTemp.getTv_productPriceSeq().equals(productPriceSeq)
                                            && orderProductDataTemp.getTv_bottleChangeYn().equals(bottleChangeYn)
                                            && orderProductDataTemp.getTv_bottleSaleYn().equals(bottleSaleYn)
                                            && orderProductDataTemp.getTv_retrievedYn().equals(retrievedYn)
                                            && orderProductDataTemp.getTv_asYn().equals(asYn)
                                            && orderProductDataTemp.getTv_incomeYn().equals(incomeYn)
                                            && orderProductDataTemp.getTv_outYn().equals(outYn)
                                    ){
                                        isAdded = true;
                                    }
                                }
                                if(isAdded){
                                    Toast.makeText(context, "?????? ????????? ???????????????..", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(context, String.format("\"%s %s??? %s\"???????????????.", strProductType, productCapa, strAction), Toast.LENGTH_SHORT).show();
                                    orderProductData = new OrderProductData(productType, productId, productCapa, productPriceSeq, strProductCount, orderProductEtc, "??????(" + bottleChangeYn + ")", bottleChangeYn, "??????(" + bottleSaleYn + ")", bottleSaleYn, "??????(" + retrievedYn + ")", retrievedYn, "AS(" + asYn + ")", asYn, "??????(" + incomeYn+")", incomeYn,"??????(" + outYn+")", outYn,0,"N",  Integer.parseInt(strProductCount));
                                    //tv_result.setText(bottleBarCd+" "+s);
                                    arrayList.add(orderProductData);

                                    oderProductAdapter.notifyDataSetChanged();
                                    // ????????? ?????????????????? ????????????.
                                    bottleChangeYn = "N";
                                    bottleSaleYn = "N";
                                    retrievedYn = "N";
                                    asYn = "N";
                                }
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
                        // ????????? ???????????? ??????
                        AlertDialog.Builder builder1
                                = new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_DARK);
                        builder1 .setTitle("??????????????????")
                                .setMessage("????????? ???????????? ???????????? ")
                                .setPositiveButton("??????", null);
                        AlertDialog ad = builder1.create();

                        ad.show();
                    }
                }


            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "?????? ????????????.", Toast.LENGTH_SHORT).show();

                // ????????? ?????????????????? ????????????.
                dlg.dismiss();
            }
        });
    }

    // ?????? ????????????
    private class HttpAsyncTask2 extends AsyncTask<String, Void, List<ProductPriceSimpleVO>> {
        private final String TAG = HttpAsyncTask2.class.getSimpleName();
        // int REQUEST_CODE =
        // OkHttp ???????????????
        OkHttpClient client = new OkHttpClient();

        @Override
        protected List<ProductPriceSimpleVO> doInBackground(String... params) {

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
                Type listType = new TypeToken<ArrayList<ProductPriceSimpleVO>>() {
                }.getType();
                productList = gson.fromJson(response.body().string(), listType);

                //Log.d(TAG, "onCreate: " + productList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return productList;
        }

        @Override
        protected void onPostExecute(List<ProductPriceSimpleVO> productList) {
            super.onPostExecute(productList);

            Log.d("HttpAsyncTask2", productList.toString());
            spinnerArray =  new ArrayList<String>();

            for (int i = 0; i < productList.size(); i++) {

                if(i==0) spinnerArray.add(productList.get(i).getProductNm().toString());
                if(i > 0 && !productList.get(i).getProductNm().equals(productList.get(i-1).getProductNm()))
                    spinnerArray.add(productList.get(i).getProductNm().toString());
           }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //Spinner sItems = (Spinner) findViewById(R.id.spinner1);
            sp_product.setAdapter(adapter);
        }
    }

}
