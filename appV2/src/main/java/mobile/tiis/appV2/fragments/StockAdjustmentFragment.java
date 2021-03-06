package mobile.tiis.appv2.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.ganfra.materialspinner.MaterialSpinner;
import mobile.tiis.appv2.R;
import mobile.tiis.appv2.adapters.SingleTextViewAdapter;
import mobile.tiis.appv2.adapters.StockAdjustmentListAdapter;
import mobile.tiis.appv2.base.BackboneActivity;
import mobile.tiis.appv2.base.BackboneApplication;
import mobile.tiis.appv2.database.DatabaseHandler;
import mobile.tiis.appv2.entity.AdjustmentReasons;
import mobile.tiis.appv2.entity.HealthFacilityBalance;

/**
 *  Created by issymac on 09/02/16.
 */
public class StockAdjustmentFragment extends Fragment{

    private static final String ARG_POSITION = "position";

    StockAdjustmentListAdapter adapter;

    private BackboneApplication application;

    private DatabaseHandler database;

    public static List<HealthFacilityBalance> rowCollectorList;

    private List<AdjustmentReasons> listAdjustmentReasons;

    private TableLayout stockHostTable;

    public static StockAdjustmentFragment newInstance() {
        StockAdjustmentFragment f = new StockAdjustmentFragment();
        Bundle b = new Bundle();
//        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_stock_adjustment, null);

        View saveFooterView             = inflater.inflate(R.layout.stock_adjustment_footer, null);
        Button saveButton               = (Button) root.findViewById(R.id.save_btn);
        stockHostTable                  = (TableLayout) root.findViewById(R.id.stock_table_container);

        application = (BackboneApplication) this.getActivity().getApplication();
        database = application.getDatabaseInstance();

        listAdjustmentReasons = database.getAdjustmentReasons();
        rowCollectorList = getHealthFacilityBalanceRows();
        addViewsToTable();

        //stockHostTable.addView(saveFooterView);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveButtonClicked();
            }
        });

        return root;
    }

    public void addViewsToTable(){
        stockHostTable.removeAllViews();
        for (final HealthFacilityBalance healthFacilityBalance : rowCollectorList){

            if(expDateValue(BackboneActivity.dateParser(healthFacilityBalance.getExpire_date()))){

            }else {

                View rowView = View.inflate(StockAdjustmentFragment.this.getActivity(), R.layout.stock_adjustment_list_item, null);

                TextView vaccineName = (TextView) rowView.findViewById(R.id.item_name);
                vaccineName.setTypeface(BackboneActivity.Rosario_Regular);
                vaccineName.setText(healthFacilityBalance.getItem_name());

                TextView vaccineLotNumber = (TextView) rowView.findViewById(R.id.lot_number);
                vaccineLotNumber.setTypeface(BackboneActivity.Rosario_Regular);
                vaccineLotNumber.setText(healthFacilityBalance.getLot_number());

                TextView vacccineBalance = (TextView) rowView.findViewById(R.id.balance);
                vacccineBalance.setTypeface(BackboneActivity.Rosario_Regular);
                vacccineBalance.setText(healthFacilityBalance.getBalance()+"");

                final MaterialEditText stockAdjustmentQuantity = (MaterialEditText) rowView.findViewById(R.id.met_quantity);

                MaterialSpinner stockAdjustmentReason = (MaterialSpinner)rowView.findViewById(R.id.reason_spinner);
                SingleTextViewAdapter spAdjustmentReasons = new SingleTextViewAdapter(this.getActivity(), R.layout.single_text_spinner_item_drop_down, database.getNameFromAdjustmentReasons());
                stockAdjustmentReason.setAdapter(spAdjustmentReasons);

                stockAdjustmentQuantity.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                item.setTempBalance(etQuantity.getText().toString());
                        healthFacilityBalance.setTempBalance(stockAdjustmentQuantity.getText().toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                stockAdjustmentReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
//                item.setSelectedAdjustmentReasonPosition(i);
                        healthFacilityBalance.setSelectedAdjustmentReasonPosition(i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                stockHostTable.addView(rowView);

            }

        }
    }

    public boolean expDateValue(Date expiry) {
        Date now = new Date();
        long diff = getDaysDifference(now, expiry);
        if (diff<0){
            return true;
        }
        else{
            return false;
        }
    }

    public static final long  getDaysDifference(Date d1, Date d2){
        long diff = d2.getTime() - d1.getTime();
        long difference = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return difference;
    }

    private void saveButtonClicked(){

        boolean canContinue = true;
        boolean balanceNegative = false;

        // check if there are rows that have only qty or reason selected
        for (HealthFacilityBalance item : rowCollectorList) {
            if ((item.getSelectedAdjustmentReasonPosition() == 0 && !item.getTempBalance().equals("")) || (item.getSelectedAdjustmentReasonPosition() != 0 && item.getTempBalance().equals(""))) {
                canContinue = false;
                break;
            }
        }
        if (canContinue) {
            //check if qty is to be subtracted from balance and if that would bring a negative balance
            for (HealthFacilityBalance item : rowCollectorList) {
                if (!item.getTempBalance().equals("")
                        && listAdjustmentReasons.get(item.getSelectedAdjustmentReasonPosition()-1).getPozitive().equals("false")) {
                    if((item.getBalance()-Integer.parseInt(item.getTempBalance())) < 0){
                        balanceNegative = true;
                        break;
                    }
                }
            }

            if (!balanceNegative) {
                boolean success = true;
                for (HealthFacilityBalance item : rowCollectorList) {
                    if (!item.getTempBalance().equals("")) {
                        //database.addToStockStatusTable();
                        //needed monthyear, itemname and Contentvalue

                        Date now = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);
                        int month = calendar.get(Calendar.MONTH);
                        Log.d("STOCK_STATUS", "month is : "+month);
                        int year = calendar.get(Calendar.YEAR);

                        String monthName = "";

                        if (month == 9 || month == 10 || month == 11){
                            monthName = database.getMonthNameFromNumber((month+1)+"", application);
                        }else {
                            monthName = database.getMonthNameFromNumber(("0"+(month+1)), application);
                        }

                        Log.d("STOCK_STATUS", "Month obtained is : "+monthName);

                        /**
                         * Inspect If the dose adjustment has been additional or substractional and respond accordingly
                         * ADDITIONAL (true) : Call method to add the adjusted value to the RECEIVED STOCK in the stock status table
                         * SUBSTRACTION (false) : Call method to add the adjusted value to DISCARDED UNOPENED column of the stock status table
                         */
                        if (listAdjustmentReasons.get(item.getSelectedAdjustmentReasonPosition() - 1).getPozitive().equals("true")){
                            //add
                            item.setBalance(item.getBalance()+Integer.parseInt(item.getTempBalance()));
                            database.addToStockStatusTable(monthName+" "+year, item.getItem_name(), Integer.parseInt(item.getTempBalance()), true);
                            //TODO: Get the actual reasons that contribute to REVEIVED_STOCKS only, receipt, from another facility!

                        }else {
                            //Substract
                            item.setBalance(item.getBalance()-Integer.parseInt(item.getTempBalance()));
                            database.addToStockStatusTable(monthName+" "+year, item.getItem_name(), Integer.parseInt(item.getTempBalance()), false);
                            //TODO: Get the actual reasons that contribute to DISCARDED_UNOPENED only, vvm status changed, and Expired!
                        }
//                        item.setBalance(
//                                (listAdjustmentReasons.get(item.getSelectedAdjustmentReasonPosition() - 1).getPozitive().equals("true"))
//                                        ?item.getBalance()+Integer.parseInt(item.getTempBalance())
//                                        :item.getBalance()-Integer.parseInt(item.getTempBalance()));
                        if (database.updateHealthFacilityBalance(item) != -1) {
                            startThread(item, listAdjustmentReasons.get(item.getSelectedAdjustmentReasonPosition() - 1).getId());
                        } else {
                            Toast.makeText(StockAdjustmentFragment.this.getActivity(), "Not saved", Toast.LENGTH_LONG).show();
                            success = false;
                        }
                    }
                }
                if (success) {

                    showDialogWhenSavedSucc(getResources().getString(R.string.saved_successfully));

                    rowCollectorList = getHealthFacilityBalanceRows();
                    adapter = new StockAdjustmentListAdapter(StockAdjustmentFragment.this.getActivity(), rowCollectorList, database.getNameFromAdjustmentReasons());
                }
            } else {
                showDialogWhenSavedSucc(getResources().getString(R.string.balance_negative));
            }
        } else {
            showDialogWhenSavedSucc(getResources().getString(R.string.choose_both_qty_reason));
        }

        addViewsToTable();

    }

    public void showDialogWhenSavedSucc(String text) {

        Toast.makeText(StockAdjustmentFragment.this.getActivity(), text, Toast.LENGTH_LONG).show();

    }

    private ArrayList<HealthFacilityBalance> getHealthFacilityBalanceRows(){
        ArrayList<HealthFacilityBalance> list = new ArrayList<>();
        Cursor cursor = null;
        cursor = database.getReadableDatabase().rawQuery("SELECT * FROM health_facility_balance where GtinIsActive='true' AND LotIsActive='true'  AND datetime(substr(expire_date,7,10), 'unixepoch') >= datetime('now')  ", null);
        if(cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    HealthFacilityBalance row = new HealthFacilityBalance();
                    row.setBalance(cursor.getInt(cursor.getColumnIndex("balance")));
                    Log.d("balance", row.getBalance() + "");
                    row.setExpire_date(cursor.getString(cursor.getColumnIndex("expire_date")));
                    Log.d("expdate", row.getExpire_date());
                    row.setGtin(cursor.getString(cursor.getColumnIndex("gtin")));
                    Log.d("gtin", row.getGtin());

                    row.setLot_number(cursor.getString(cursor.getColumnIndex("lot_number")));
                    Log.d("lot_number", row.getLot_number());
                    row.setLot_id(cursor.getString(cursor.getColumnIndex("lot_id")));
                    Log.d("lot_id", row.getLot_id());
                    row.setItem_name(cursor.getString(cursor.getColumnIndex("item")));
                    Log.d("item", row.getItem_name());
//
//                    row.setItem_name(cursor.getString(cursor.getColumnIndex("reorder_qty")));
//                    Log.d("reorder_qty", row.getReorder_qty());
                    list.add(row);
                } while (cursor.moveToNext());
            }
        }

        return list;
    }

    private void startThread(HealthFacilityBalance healthFacilityBalance, String reasonId){
        new Thread(){
            HealthFacilityBalance healthFacilityBalance;
            String reasonId;
            public Thread setData(HealthFacilityBalance healthFacilityBalance, String reasonId) {
                this.healthFacilityBalance = healthFacilityBalance;
                this.reasonId  = reasonId;
                return this;
            }

            @Override
            public void run() {
                super.run();
                BackboneApplication backbone = (BackboneApplication) StockAdjustmentFragment.this.getActivity().getApplication();

                try {
                    backbone.saveStockAdjustmentReasons(URLEncoder.encode(healthFacilityBalance.getGtin(), "utf-8")
                            , URLEncoder.encode(healthFacilityBalance.getLot_number(), "utf-8")
                            , URLEncoder.encode(healthFacilityBalance.getTempBalance(), "utf-8")
                            , URLEncoder.encode(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()), "utf-8")
                            , URLEncoder.encode(reasonId, "utf-8")
                            , URLEncoder.encode(backbone.getLOGGED_IN_USER_ID(), "utf-8"));
                }catch (Exception e){e.printStackTrace();}

            }
        }.setData(healthFacilityBalance, reasonId).start();
    }

}
