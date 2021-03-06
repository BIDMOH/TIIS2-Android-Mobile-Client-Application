package mobile.tiis.appv2.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobile.tiis.appv2.CustomViews.NestedListView;
import mobile.tiis.appv2.R;
import mobile.tiis.appv2.base.BackboneActivity;
import mobile.tiis.appv2.base.BackboneApplication;
import mobile.tiis.appv2.database.DatabaseHandler;
import mobile.tiis.appv2.database.GIISContract;
import mobile.tiis.appv2.database.SQLHandler;
import mobile.tiis.appv2.entity.AdministerVaccinesModel;
import mobile.tiis.appv2.entity.Child;
import mobile.tiis.appv2.entity.Stock;
import mobile.tiis.appv2.mObjects.RowCollector;
import mobile.tiis.appv2.util.ViewAppointmentRow;

import static mobile.tiis.appv2.ChildDetailsActivity.childId;
import static mobile.tiis.appv2.base.BackboneApplication.TABLET_REGISTRATION_MODE_PREFERENCE_NAME;

/**
 *  Created by issymac on 27/01/16.
 */
public class ChildVaccinatePagerFragment extends Fragment {
    private static final String TAG = ChildVaccinatePagerFragment.class.getSimpleName();

    private BackboneApplication app;

    private DatabaseHandler dbh;

    private Child currentChild;

    private ArrayList<ViewAppointmentRow> var;

    private static boolean barcodeIsEmpty = false;

    private static final String CHILD_OBJECT = "child";

    private ArrayList<RowCollector> rowCollectorContainer;

    private String hf_id, child_id, birthplacestr, villagestr, hfstr, statusstr, gender_val, birthdate_val;

    final String VACC_ID_LOG = "Vaccination Id";

    final String VACC_NAME_LOG = "Vaccination Name";

    final String VACC__ITEM_ID_LOG = "Vaccination Item Id";


    private RelativeLayout noBarcode,registrationModeEnabled;

    private RelativeLayout frameLayout;

    public static final int getMonthsDifference(Date date1, Date date2) {
        int m1 = date1.getYear() * 12 + date1.getMonth();
        int m2 = date2.getYear() * 12 + date2.getMonth();
        return m2 - m1;
    }

    public static final long getDaysDifference(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);

        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.HOUR,0);
        c1.set(Calendar.MINUTE,0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);

        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.HOUR,0);
        c2.set(Calendar.MINUTE,0);
        c2.set(Calendar.SECOND,0);
        c2.set(Calendar.MILLISECOND,0);

        long diff = c2.getTimeInMillis() - c1.getTimeInMillis();
        long difference = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return Math.abs(difference);
    }

    public static Date dateParser(String date_str) {
        Date date = null;
        Pattern pattern = Pattern.compile("\\((.*?)-");
        Pattern pattern_plus = Pattern.compile("\\((.*?)\\+");
        Matcher matcher = pattern.matcher(date_str);
        Matcher matcher_plus = pattern_plus.matcher(date_str);
        if (matcher.find()) {
            date = new Date(Long.parseLong(matcher.group(1)));
        } else if (matcher_plus.find()) {
            date = new Date(Long.parseLong(matcher_plus.group(1)));
        } else {
            date = new Date();
        }
        return date;
    }

    public static ChildVaccinatePagerFragment newInstance(Child currentChild, String appointmentId) {
        ChildVaccinatePagerFragment f = new ChildVaccinatePagerFragment();
        Bundle b                    = new Bundle();
        b                           .putSerializable(CHILD_OBJECT, currentChild);
        b                           .putString("appointmentId", appointmentId);
        f                           .setArguments(b);
        return f;
    }

    private NestedListView vaccineDosesListView;

    private FrameLayout vaccinateFrame;

    private ArrayList<AdministerVaccinesModel> arrayListAdminVacc;

    private FragmentStackManager fm;

    private String appointmentID = "";
    private List<Stock> listStock;
    private TextView lotNumberErrorMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentChild     = (Child) getArguments().getSerializable(CHILD_OBJECT);

        if (getArguments().getString("appointmentId") != null){
            appointmentID   = getArguments().getString("appointmentId");
        }


        app = (BackboneApplication) ChildVaccinatePagerFragment.this.getActivity().getApplication();
        dbh = app.getDatabaseInstance();

        if (currentChild.getBarcodeID().isEmpty() || currentChild.getBarcodeID() == null || currentChild.getBarcodeID().equals("")){
            barcodeIsEmpty = true;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup v;
        v = (ViewGroup) inflater.inflate(R.layout.child_faccinate_pager_fragment, null);
        setUpView(v);

        validateAccessPrevilageForTheChild();

        if (!appointmentID.isEmpty()){

            ChildAppointmentsListFragment appointmentsListFragment = new ChildAppointmentsListFragment();
            Bundle mBundle=new Bundle();
            mBundle.putString("child_id", childId);
            mBundle.putString("barcode", currentChild.getBarcodeID());
            mBundle.putString("birthdate", currentChild.getBirthdate());
            appointmentsListFragment.setArguments(mBundle);

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.vacc_fragment_frame, appointmentsListFragment);
            ft.addToBackStack("fragmentVaccineList");
            ft.commit();

            AdministerVaccineFragment administerVaccineFragment = new AdministerVaccineFragment();
            Bundle bundle = new Bundle();
            bundle.putString("appointment_id", appointmentID);
            bundle.putString("birthdate", currentChild.getBirthdate());

            bundle.putString("barcode", currentChild.getBarcodeID());
            Log.d("appointmentID", "Appointment Id is : " + appointmentID);
            administerVaccineFragment.setArguments(bundle);
            app.setCurrentFragment(app.VACCINATE_CHILD_FRAGMENT);

//            fm.addFragment(administerVaccineFragment, R.id.vacc_fragment_frame, true, FragmentTransaction.TRANSIT_FRAGMENT_FADE, false);

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.vacc_fragment_frame, administerVaccineFragment);
            fragmentTransaction.addToBackStack("AdministerVaccineFragment");
            fragmentTransaction.commit();
        }else{
            ChildAppointmentsListFragment appointmentsListFragment = new ChildAppointmentsListFragment();
            Bundle bundle=new Bundle();
            bundle.putString("child_id", childId);
            bundle.putString("barcode", currentChild.getBarcodeID());
            bundle.putString("birthdate", currentChild.getBirthdate());


            SimpleDateFormat ft1 = new SimpleDateFormat("dd-MMM-yyyy");
            Log.d("optisation", "Birth date is : "+ft1.format(BackboneActivity.dateParser(currentChild.getBirthdate())));
            appointmentsListFragment.setArguments(bundle);
//
//        //add the Fragment to display a list of current child's appointments
//        fm = new FragmentStackManager(this.getActivity());
//        appv2.setCurrentFragment(appv2.APPOINTMENT_LIST_FRAGMENT);
//        fm.addFragment(appointmentsListFragment, R.id.vacc_fragment_frame, true, FragmentTransaction.TRANSIT_FRAGMENT_FADE, false);

            app.setCurrentFragment(app.APPOINTMENT_LIST_FRAGMENT);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.vacc_fragment_frame, appointmentsListFragment);
            ft.addToBackStack("fragmentVaccineList");
            ft.commit();
        }

        Date todayD = new Date();
        SimpleDateFormat ftD = new SimpleDateFormat("dd-MMM-yyyy");

        return v;
    }

    private void validateAccessPrevilageForTheChild(){
        Log.d(TAG,"validateAccessPrevilagesForTheChild called");
        listStock = dbh.getAvailableHealthFacilityBalance();
        if (currentChild.getBarcodeID().isEmpty() || currentChild.getBarcodeID().equals("")){
            Toast.makeText(ChildVaccinatePagerFragment.this.getActivity(), getString(R.string.empty_barcode), Toast.LENGTH_SHORT).show();
            noBarcode.setVisibility(View.VISIBLE);
            registrationModeEnabled.setVisibility(View.GONE);
            frameLayout.setVisibility(View.GONE);
        }else if(!(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getBoolean(TABLET_REGISTRATION_MODE_PREFERENCE_NAME, false))){

            if(!checkIfLotNumbersWereSetForAllVaccinesDuringTheDaysLoginOfTheDay()){
                noBarcode.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
                registrationModeEnabled.setVisibility(View.VISIBLE);
                lotNumberErrorMessage.setText("Lot Numbers for some vaccines have not been selected. \nInorder to vaccinate a child, Go to Lot Number Settings on the main menu and select the vaccination Lot Numbers for vaccines that will be used today");

            }else if(!checkIfThereAnyActivatedLotNumbersThatHaveRunOutOfStock()){
                noBarcode.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
                registrationModeEnabled.setVisibility(View.VISIBLE);
                lotNumberErrorMessage.setText("Lot Numbers that were previously selected for today's use for some vaccines have run out. \n Please go to Lot Number Settings on the main menu and select new vaccination Lot Numbers to continue vaccinating children");
            }

        }else{
            noBarcode.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            registrationModeEnabled.setVisibility(View.GONE);
        }
    }

    public void setUpView(View v){
        vaccineDosesListView        = (NestedListView) v.findViewById(R.id.lv_dose_list);
        vaccinateFrame              = (FrameLayout) v.findViewById(R.id.vacc_fragment_frame);
        noBarcode    = (RelativeLayout) v.findViewById(R.id.child_no_barcode_layout);
        registrationModeEnabled    = (RelativeLayout) v.findViewById(R.id.registration_mode_enabled_layout);
        lotNumberErrorMessage    = (TextView) v.findViewById(R.id.lot_number_error_message);
        noBarcode.setVisibility(View.GONE);
        frameLayout  = (RelativeLayout) v.findViewById(R.id.frame_layout);
    }

    public Child getChildFromCursror(Cursor cursor) {
        Child parsedChild = new Child();
        parsedChild.setId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.ID)));
        parsedChild.setBarcodeID(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BARCODE_ID)));
        parsedChild.setTempId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.TEMP_ID)));
        parsedChild.setFirstname1(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.FIRSTNAME1)));
        parsedChild.setFirstname2(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.FIRSTNAME2)));
        parsedChild.setLastname1(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.LASTNAME1)));
        parsedChild.setBirthdate(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BIRTHDATE)));
        parsedChild.setMotherFirstname(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_FIRSTNAME)));
        parsedChild.setMotherLastname(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_LASTNAME)));
        parsedChild.setPhone(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.PHONE)));
        parsedChild.setNotes(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.NOTES)));
        parsedChild.setBirthplaceId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BIRTHPLACE_ID)));
        parsedChild.setGender(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.GENDER)));
        Cursor cursor1 = dbh.getReadableDatabase().rawQuery("SELECT * FROM birthplace WHERE ID=?", new String[]{parsedChild.getBirthplaceId()});
        if (cursor1.getCount() > 0) {
            cursor1.moveToFirst();
            birthplacestr = cursor1.getString(cursor1.getColumnIndex(SQLHandler.PlaceColumns.NAME));
        }
        parsedChild.setBirthplace(birthplacestr);

        parsedChild.setDomicileId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.DOMICILE_ID)));
        Cursor cursor2 = dbh.getReadableDatabase().rawQuery("SELECT * FROM place WHERE ID=?", new String[]{parsedChild.getDomicileId()});
        if (cursor2.getCount() > 0) {
            cursor2.moveToFirst();
            villagestr = cursor2.getString(cursor2.getColumnIndex(SQLHandler.PlaceColumns.NAME));
        }

        parsedChild.setDomicile(villagestr);
        parsedChild.setHealthcenterId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.HEALTH_FACILITY_ID)));
        try {
            Cursor cursor3 = dbh.getReadableDatabase().rawQuery("SELECT * FROM health_facility WHERE ID=?", new String[]{parsedChild.getHealthcenterId()});
            if (cursor3.getCount() > 0) {
                cursor3.moveToFirst();
                hfstr = cursor3.getString(cursor3.getColumnIndex(SQLHandler.HealthFacilityColumns.NAME));
            }
        }catch (Exception e){
            hfstr = "";
        }
        parsedChild.setHealthcenter(hfstr);

        parsedChild.setStatusId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.STATUS_ID)));
        Cursor cursor4 = dbh.getReadableDatabase().rawQuery("SELECT * FROM status WHERE ID=?", new String[]{parsedChild.getStatusId()});
        if (cursor4.getCount() > 0) {
            cursor4.moveToFirst();
            statusstr = cursor4.getString(cursor4.getColumnIndex(SQLHandler.StatusColumns.NAME));
        }
        parsedChild.setStatus(statusstr);
        return parsedChild;

    }

    private boolean checkIfLotNumbersWereSetForAllVaccinesDuringTheDaysLoginOfTheDay(){
        Calendar calendar=Calendar.getInstance();
        GregorianCalendar gregorianCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        for (Stock stock : listStock){
            Cursor c = dbh.getReadableDatabase().rawQuery("SELECT lot_id FROM "+ SQLHandler.Tables.ACTIVE_LOT_NUMBERS+" WHERE "+ GIISContract.ActiveLotNumbersColumns.ITEM+" = '"+stock.getItem()+"' AND "+
                    GIISContract.ActiveLotNumbersColumns.DATE+" = "+gregorianCalendar.getTimeInMillis(),null);
            if(c.getCount()==0){
                return false;
            }
        }
        return true;
    }

    private boolean checkIfThereAnyActivatedLotNumbersThatHaveRunOutOfStock(){
        Calendar calendar=Calendar.getInstance();
        GregorianCalendar gregorianCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        for (Stock stock : listStock){
            Cursor c = dbh.getReadableDatabase().rawQuery("SELECT active_lot_numbers.lot_id FROM "+ SQLHandler.Tables.ACTIVE_LOT_NUMBERS+" INNER JOIN health_facility_balance ON health_facility_balance.lot_id = active_lot_numbers.lot_id WHERE active_lot_numbers.item = '"+stock.getItem()+"' AND "+
                    GIISContract.ActiveLotNumbersColumns.DATE+" = "+gregorianCalendar.getTimeInMillis()+" AND CAST(health_facility_balance.balance as REAL) > "+0+"",null);
            if(c.getCount()==0){
                return false;
            }
        }
        return true;
    }

    public void updateChild() {
        currentChild = dbh.getChildById(childId);
        validateAccessPrevilageForTheChild();
    }

}
