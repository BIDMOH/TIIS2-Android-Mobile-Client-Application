package mobile.tiis.appv2.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import mobile.tiis.appv2.R;
import mobile.tiis.appv2.base.BackboneApplication;
import mobile.tiis.appv2.database.DatabaseHandler;
import mobile.tiis.appv2.database.SQLHandler;
import mobile.tiis.appv2.helpers.Utils;

/**
 * Created by issymac on 14/03/16.
 */
public class WeightOflineFragment extends Fragment {

    String barcode;

    private TextView text;

    Button saveButton;

    MaterialEditText weight_input_dec, weight_input_comma, dateValue;

    private boolean isWeightSetForChild = false;
    private BackboneApplication backbone;



    public static WeightOflineFragment newInstance(String barcode) {
        WeightOflineFragment f = new WeightOflineFragment();
        Bundle b = new Bundle();
        b.putString("barcode", barcode);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        barcode = getArguments().getString("barcode");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup rootview = (ViewGroup) inflater.inflate(R.layout.weight_offline_fragment, null);
        setupviews(rootview);

        backbone = (BackboneApplication) getActivity().getApplication();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveButtonClickedEventHandler();
            }
        });

        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd-MMM-yyyy");
        dateValue.setText(ft.format(dNow));

        return rootview;
    }

    public void saveButtonClickedEventHandler(){
        if (Utils.isStringBlank((weight_input_dec.getText().toString())) || weight_input_dec.getText().toString().substring(0, 1).equals("0")) {
            final AlertDialog.Builder ad = new AlertDialog.Builder(this.getActivity());
            ad.setTitle(getString(R.string.warning));
            ad.setMessage(getString(R.string.weight_not_correct));
            ad.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            // this will solve your error
            AlertDialog alert = ad.create();
            alert.show();
            alert.getWindow().getAttributes();

            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            textView.setTextSize(30);
        } else {

            if (!isWeightSetForChild) {
                isWeightSetForChild = true;
                updateWeight((weight_input_dec.getText().toString()) + "." + (weight_input_comma.getText().toString().trim().equals("") ? "00" : weight_input_comma.getText().toString()));

                String dateToday = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                String dateTodayTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").format(Calendar.getInstance().getTime());
                new Thread() {
                    String threadDateToday
                            ,
                            threadDateModON
                            ,
                            threadbarcode
                            ,
                            threadWeight
                            , threadModBy;

                    public Thread setData(String threadbarcode, String threadDateToday, String threadDateModON, String threadWeight, String threadModBy) {
                        try {
                            this.threadDateToday = URLEncoder.encode(threadDateToday, "utf-8");
                            this.threadDateModON = URLEncoder.encode(threadDateModON, "utf-8");
                            this.threadbarcode = threadbarcode;
                            this.threadModBy = threadModBy;
                            this.threadWeight = threadWeight;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        return this;
                    }

                    @Override
                    public void run() {


                        synchronized (this) {
                            backbone.saveWeight(threadbarcode,
                                    threadDateToday, threadDateModON, threadWeight,
                                    threadModBy);
                            //Register Audit
                            backbone.registerAudit(BackboneApplication.CHILD_AUDIT, threadbarcode, threadDateModON,
                                    backbone.getLOGGED_IN_USER_ID(), 6);

                        }

                    }
                }.setData(barcode
                        , dateToday
                        , dateTodayTimestamp
                        , weight_input_dec.getText().toString() + "." + (weight_input_comma.getText().toString().trim().equals("") ? "00" : weight_input_comma.getText().toString())
                        , backbone.getLOGGED_IN_USER_ID()).start();

            }

        }
    }

    public void setupviews(View v){
        saveButton          = (Button) v.findViewById(R.id.weight_save_btn);
        weight_input_dec    = (MaterialEditText) v.findViewById(R.id.met_weight_value);
        weight_input_comma  = (MaterialEditText) v.findViewById(R.id.met_weight_decimal_value);
        dateValue           = (MaterialEditText) v.findViewById(R.id.met_date_value);
    }

    public void updateWeight(String weight) {
        DatabaseHandler mydb = backbone.getDatabaseInstance();
        ContentValues child = new ContentValues();
        child.put(SQLHandler.SyncColumns.UPDATED, 1);
        child.put(SQLHandler.ChildWeightColumns.WEIGHT, weight);
        child.put(SQLHandler.ChildWeightColumns.DATE, Calendar.getInstance().getTimeInMillis()/1000);
        child.put(SQLHandler.ChildWeightColumns.CHILD_BARCODE, barcode);
        Cursor cursor = null;

        cursor = mydb.getReadableDatabase().rawQuery("SELECT * FROM child_weight WHERE CHILD_BARCODE=? ", new String[]{String.valueOf(barcode)});
        if (cursor.getCount() > 0) {
            mydb.updateWeight(child, barcode);
            final AlertDialog.Builder ad = new AlertDialog.Builder(this.getActivity());
            ad.setMessage(getString(R.string.weight_updated));
            ad.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // this will solve your error
            AlertDialog alert = ad.create();
            alert.show();
            alert.getWindow().getAttributes();

            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            textView.setTextSize(30);
        } else {
            mydb.addChildWeight(child);
            final AlertDialog.Builder ad = new AlertDialog.Builder(this.getActivity());
            ad.setMessage(getString(R.string.weight_registered));
            ad.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // this will solve your error
            AlertDialog alert = ad.create();
            alert.show();
            alert.getWindow().getAttributes();

            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            textView.setTextSize(30);
        }

    }

}
