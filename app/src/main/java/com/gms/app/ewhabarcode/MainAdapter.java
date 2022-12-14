package com.gms.app.ewhabarcode;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gms.app.ewhabarcode.dialog.BottleInfoDialog;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.CustomViewHolder> {

    private ArrayList<MainData> arrayList;
    private String shared = "file";

    public MainAdapter(ArrayList<MainData> arrayList) {
        this.arrayList = arrayList;
    }


    @Override
    public MainAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list,parent,false);

        CustomViewHolder holder = new CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MainAdapter.CustomViewHolder holder, int position) {

        holder.tv_bottleId.setText(arrayList.get(position).getTv_bottleId());
        holder.tv_productNm.setText(arrayList.get(position).getTv_productNm());
        holder.tv_bottleBarCd.setText(arrayList.get(position).getTv_bottleBarCd());
        holder.tv_chargedt.setText(arrayList.get(position).getTv_chargedt());

        if(arrayList.get(position).getTv_bottleBarCd() !=null && arrayList.get(position).getTv_bottleBarCd().toString().length() > 9 ){
            holder.tv_bottleBarCd.setTextSize(15);
        }
        if(arrayList.get(position).getTv_productNm() !=null &&  arrayList.get(position).getTv_productNm().toString().length() > 9 ){
            holder.tv_productNm.setTextSize(15);
        }
        if(arrayList.get(position).getTv_chargedt() !=null &&  arrayList.get(position).getTv_chargedt().toString().length() > 5 ){
            holder.tv_chargedt.setTextSize(15);
        }
        if(arrayList.get(position).getTv_chargedt() !=null && arrayList.get(position).getTv_chargedt().indexOf("-") > -1){
            holder.tv_bottleId.setTextColor(Color.parseColor("#E53935"));
            holder.tv_productNm.setTextColor(Color.parseColor("#E53935"));
            holder.tv_bottleBarCd.setTextColor(Color.parseColor("#E53935"));
            holder.tv_chargedt.setTextColor(Color.parseColor("#E53935"));
        }

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String curNmae = holder.tv_productNm.getText().toString();
            }
        });

        //final TextView main_label = holder.btn_info;
        holder.btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //holder.textView.setText(items.get(holder.getAdapterPosition()));

                SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(shared,0);
                String value = sharedPreferences.getString(holder.tv_bottleBarCd.getText().toString(), "");
                //Toast.makeText(view.getContext(), "value "+value, Toast.LENGTH_SHORT).show();

                //info ?????? ?????????
                BottleInfoDialog bottleInfo = new BottleInfoDialog(view.getContext());
                // ????????? ?????????????????? ????????????.
                // ????????? ?????????????????? ????????? ????????? TextView??? ??????????????? ?????? ????????????.
                bottleInfo.callFunction(value);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder ad = new AlertDialog.Builder(v.getContext());
                ad.setMessage("????????? ?????????????????????????");

                ad.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Toast.makeText(v.getContext(), "????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                        //????????? ??????
                        remove(holder.getAdapterPosition());
                        MainActivity.setTextBottleCount();
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

                return true;
            }
        });
    }


    /*
    TODO ?????? ????????? ?????? ??????

    //in RecyclerView.Adapter<MHolder>
    @Override
    public void onBindViewHolder(@NonNull MHolder holder, int position) {
        holder.button.setOnClickListener(view -> {
            holder.textView.setText(items.get(holder.getAdapterPosition()));
        });
    }
    */

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }

    public void remove(int position) {
        try{
            arrayList.remove(position);
            notifyItemRemoved(position);
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{

        protected TextView tv_bottleId;
        protected TextView tv_productNm;
        protected TextView tv_bottleBarCd;
        protected TextView tv_chargedt;
        protected Button btn_info;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.tv_bottleId = (TextView) itemView.findViewById(R.id.tv_bottleId);
            this.tv_productNm = (TextView) itemView.findViewById(R.id.tv_productNm);
            this.tv_bottleBarCd = (TextView) itemView.findViewById(R.id.tv_bottleBarCd);
            this.tv_chargedt = (TextView) itemView.findViewById(R.id.tv_chargedt);
            this.btn_info = (Button)itemView.findViewById(R.id.btn_buyback);
        }
    }
}
