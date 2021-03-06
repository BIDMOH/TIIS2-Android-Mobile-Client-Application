package mobile.tiis.appv2.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import mobile.tiis.appv2.R;
import mobile.tiis.appv2.base.BackboneApplication;
import mobile.tiis.appv2.database.DatabaseHandler;

/**
 * Created by issymac on 30/03/16.
 */
public class HealthFacilityVisitsAndVaccinationSummaryFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    private int position;

    private TextView region, district, healthFacility, reportingPeriod, title;

    BackboneApplication app;

    private ProgressBar progressBar;

    private DatabaseHandler mydb;
    private View rowview,chartList,chart_view;
    private  MaterialEditText metDOBFrom,metDOBTo;

    final DatePickerDialog fromDatePicker = new DatePickerDialog();
    final DatePickerDialog toDatePicker = new DatePickerDialog();

    private String toDateString="",fromDateString="";
    private EditText editTextUsedToRequestFocus;

    public static HealthFacilityVisitsAndVaccinationSummaryFragment newInstance(int position) {
        HealthFacilityVisitsAndVaccinationSummaryFragment f = new HealthFacilityVisitsAndVaccinationSummaryFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        toDatePicker.setMaxDate(Calendar.getInstance());
        app = (BackboneApplication) this.getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rowview = inflater.inflate(R.layout.fragment_health_facility_list_and_vaccination_summary, null);

        TextView districtValue = (TextView)rowview.findViewById(R.id.district_value);
        districtValue.setText(app.getHealthFacilityDistrictName(app.getLOGGED_IN_USER_HF_ID()));


        prepareUIElements(rowview);
        mydb = app.getDatabaseInstance();
        healthFacility.setText(mydb.getHealthCenterName(app.getLOGGED_IN_USER_HF_ID()));
//        reportingPeriod.setText(mydb.getUserHFIDByUserId(appv2.getLOGGED_IN_USER_ID()));

        return rowview;
    }

    public void prepareUIElements(View v){
        chart_view      = v.findViewById(R.id.chart_view);
        editTextUsedToRequestFocus          = (EditText) v.findViewById(R.id.edit_text_used_to_request_focus);
        editTextUsedToRequestFocus.requestFocus();
        region          = (TextView) v.findViewById(R.id.region_value);
        district        = (TextView) v.findViewById(R.id.district_title);
        healthFacility  = (TextView) v.findViewById(R.id.hf_value);
        reportingPeriod = (TextView) v.findViewById(R.id.period_title);
        title           = (TextView) v.findViewById(R.id.the_title);
        progressBar     = (ProgressBar) v.findViewById(R.id.progres_bar);
        chartList     = v.findViewById(R.id.chartList);

        metDOBFrom              = (MaterialEditText) v.findViewById(R.id.met_dob_from);
        metDOBTo                = (MaterialEditText) v.findViewById(R.id.met_dob_value);


        metDOBTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDatePicker.show(((Activity) getActivity()).getFragmentManager(), "DatePickerDialogue");
                toDatePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        metDOBTo.setText((dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "-" + ((monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : monthOfYear + 1) + "-"
                                + year);

                        Calendar toCalendar = Calendar.getInstance();
                        toCalendar.set(year, monthOfYear, dayOfMonth);
                        fromDatePicker.setMaxDate(toCalendar);
                        toDateString = (toCalendar.getTimeInMillis()/1000)+"";
                        editTextUsedToRequestFocus.requestFocus();

                        if(!fromDateString.equals("")){
                            chart_view.setVisibility(View.VISIBLE);
                            new FilterList().execute(app.getLOGGED_IN_USER_HF_ID(),fromDateString,toDateString);
                        }else{
                            final Snackbar snackbar=Snackbar.make(rowview,"Please select a start date to view the chart",Snackbar.LENGTH_LONG);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    }
                });
            }
        });


        metDOBFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDatePicker.show(((Activity) getActivity()).getFragmentManager(), "DatePickerDialogue");
                fromDatePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        metDOBFrom.setText((dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "-" + ((monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : monthOfYear + 1) + "-"
                                + year);

                        Calendar fromCalendar = Calendar.getInstance();
                        fromCalendar.set(year, monthOfYear, dayOfMonth);
                        toDatePicker.setMinDate(fromCalendar);
                        fromDateString = ((fromCalendar.getTimeInMillis() - 24*60*60*1000) / 1000) + "";
                        editTextUsedToRequestFocus.requestFocus();

                        if (!toDateString.equals("")) {
                            chart_view.setVisibility(View.VISIBLE);
                            new FilterList().execute(app.getLOGGED_IN_USER_HF_ID(), fromDateString, toDateString);
                        } else {
                            final Snackbar snackbar = Snackbar.make(rowview, "Please select an end date to view the report", Snackbar.LENGTH_LONG);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    }
                });
            }
        });

    }

    public class FilterList extends AsyncTask<String, Void, ViewRows> {



        @Override
        protected void onPreExecute() {

            progressBar.setVisibility(View.VISIBLE);
            chartList.setVisibility(View.INVISIBLE);
        }

        @Override
        protected ViewRows doInBackground(String... params) {
            String healthFacilityId = params[0];
            String fromDate ="";
            String toDate ="";

            try{
                fromDate = params[1];
                toDate = params[2];
            }catch (Exception e){
                e.printStackTrace();
            }

            String SQLCountTotalVisits,SQLCountTotalOutReach,SQLCountTotalFixed,SQLCountTotalWithin,SQLCountTotalOutside,SQLCountVaccinedOutReach,SQLCountVaccinedFixed,SQLCountVaccinedWithin,
                    SQLCountVaccinedOutside,SQLCountTotalNewVisits,SQLCountNewVisitsOutreach,SQLCountNewVisitsFixed,SQLCountNewVisitsWithin,SQLCountNewVisitsOutside,SQLCountUnderImmunizedTotal,SQLCountUnderImmunizedOutreach,
                    SQLCountUnderImmunizedFixed,SQLCountUnderImmunizedWithin,SQLCountUnderImmunizedOutside,SQLCountFullyImmunized,SQLCountFullyImmunizedOutreach,SQLCountFullyImmunizedFixed,SQLCountFullyImmunizedWithin,SQLCountFullyImmunizedOutside;
            SQLCountTotalVisits = "SELECT  COUNT (DISTINCT(child.ID)) AS IDS  FROM  vaccination_event, vaccination_appointment,child " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.CHILD_ID=vaccination_appointment.CHILD_ID " +
                    "AND vaccination_event.CHILD_ID=child.ID " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.VACCINATION_STATUS = 'true' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";

            Log.e("SQLCountTotalVisits", SQLCountTotalVisits);

            SQLCountTotalOutReach = "SELECT ID, "+
                    "(SELECT COUNT (DISTINCT(ve.CHILD_ID)) FROM vaccination_appointment as va inner join "+
                    "vaccination_event as ve on va.ID = ve.APPOINTMENT_ID "+
                    "WHERE v.ID = va.ID " +
                    "AND ve.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch') ) AS IDS "+
                    "FROM VACCINATION_APPOINTMENT v WHERE v.OUTREACH = 'true' ";

            String SQLCountTotalOutReach2 = "SELECT  COUNT (DISTINCT(child.ID)) AS IDS  FROM child "+
                    "LEFT JOIN vaccination_event ve ON ve.CHILD_ID = child.ID LEFT JOIN vaccination_appointment va ON ve.APPOINTMENT_ID=va.ID " +
                    "WHERE ve.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND va.OUTREACH = 'true' " +
                    "AND ve.VACCINATION_STATUS = 'true' " +
                    "AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";


            SQLCountTotalFixed = "SELECT ID, "+
                    "(SELECT COUNT (DISTINCT(ve.CHILD_ID)) FROM vaccination_appointment as va inner join "+
                    "vaccination_event as ve on va.ID = ve.APPOINTMENT_ID "+
                    "WHERE v.ID = va.ID " +
                    "AND ve.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch') ) AS IDS "+
                    "FROM VACCINATION_APPOINTMENT v WHERE v.OUTREACH = 'false' ";

//
//            SQLCountTotalWithin = "SELECT  COUNT (DISTINCT(child.ID)) AS IDS  FROM  vaccination_event, vaccination_appointment,child " +
//                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
//                    "AND vaccination_event.CHILD_ID=child.ID " +
//                    "AND child.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
//                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
//                    "AND vaccination_event.VACCINATION_STATUS = 'true' " +
//                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
//                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";

            SQLCountTotalWithin = "SELECT COUNT (DISTINCT(c.ID)) AS IDS FROM vaccination_appointment as va " +
                    "INNER JOIN " +
                    "   vaccination_event as ve on va.ID = ve.APPOINTMENT_ID " +
                    "   INNER JOIN " +
                    "   child as c on c.ID = ve.CHILD_ID " +
                    "   WHERE ve.APPOINTMENT_ID = va.ID " +
                    "       AND ve.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "       AND c.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "       AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch')" +
                    "       AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";



//
//            SQLCountTotalOutside = "SELECT  COUNT (DISTINCT(child.ID)) AS IDS  FROM  vaccination_event, vaccination_appointment,child " +
//                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
//                    "AND vaccination_event.CHILD_ID=child.ID " +
//                    "AND vaccination_event.VACCINATION_STATUS = 'true' " +
//                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
//                    "AND child.HEALTH_FACILITY_ID <> '"+healthFacilityId+"' " +
//                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
//                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";

            SQLCountTotalOutside = "SELECT COUNT (DISTINCT(c.ID)) AS IDS FROM vaccination_appointment as va " +
                    "INNER JOIN " +
                    "   vaccination_event as ve on va.ID = ve.APPOINTMENT_ID " +
                    "   INNER JOIN " +
                    "   child as c on c.ID = ve.CHILD_ID " +
                    "   WHERE ve.APPOINTMENT_ID = va.ID " +
                    "       AND ve.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "       AND c.HEALTH_FACILITY_ID <> '"+healthFacilityId+"' " +
                    "       AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch')" +
                    "       AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";



            SQLCountVaccinedOutReach = "SELECT COUNT (DISTINCT(c.ID)) AS IDS FROM vaccination_appointment as va " +
                    "INNER JOIN " +
                    "   vaccination_event as ve on va.ID = ve.APPOINTMENT_ID " +
                    "INNER JOIN " +
                    "   child as c on c.ID = ve.CHILD_ID " +
                    "WHERE ve.APPOINTMENT_ID = va.ID " +
                    "   AND ve.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "   AND ve.VACCINATION_STATUS = 'true'" +
                    "   AND va.OUTREACH = 'true'" +
                    "   AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch')" +
                    "   AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";


            SQLCountVaccinedFixed = "SELECT COUNT (DISTINCT(c.ID)) AS IDS FROM vaccination_appointment as va " +
                    "INNER JOIN " +
                    "   vaccination_event as ve on va.ID = ve.APPOINTMENT_ID " +
                    "INNER JOIN " +
                    "   child as c on c.ID = ve.CHILD_ID " +
                    "WHERE ve.APPOINTMENT_ID = va.ID " +
                    "   AND ve.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "   AND ve.VACCINATION_STATUS = 'true'" +
                    "   AND va.OUTREACH = 'false'" +
                    "   AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch')" +
                    "   AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";


            SQLCountVaccinedWithin =  "SELECT COUNT (DISTINCT(c.ID)) AS IDS FROM vaccination_appointment as va " +
                    "INNER JOIN " +
                    "   vaccination_event as ve on va.ID = ve.APPOINTMENT_ID " +
                    "INNER JOIN " +
                    "   child as c on c.ID = ve.CHILD_ID " +
                    "WHERE ve.APPOINTMENT_ID = va.ID " +
                    "   AND ve.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "   AND c.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "   AND ve.VACCINATION_STATUS = 'true'" +
                    "   AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch')" +
                    "   AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";

            SQLCountVaccinedOutside = "SELECT COUNT (DISTINCT(c.ID)) AS IDS FROM vaccination_appointment as va " +
                    "INNER JOIN " +
                    "   vaccination_event as ve on va.ID = ve.APPOINTMENT_ID " +
                    "INNER JOIN " +
                    "   child as c on c.ID = ve.CHILD_ID " +
                    "WHERE ve.APPOINTMENT_ID = va.ID " +
                    "   AND ve.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "   AND c.HEALTH_FACILITY_ID <> '"+healthFacilityId+"' " +
                    "   AND ve.VACCINATION_STATUS = 'true'" +
                    "   AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch')" +
                    "   AND datetime(substr(ve.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";



            SQLCountTotalNewVisits="SELECT  COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID  " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.VACCINATION_STATUS = 'true' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND  vaccination_event.CHILD_ID NOT IN (" +
                    "SELECT  vaccination_event.CHILD_ID FROM vaccination_event,vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID  " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.CHILD_ID=vaccination_appointment.CHILD_ID " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<datetime('"+fromDate+"','unixepoch')  GROUP BY vaccination_appointment.ID,vaccination_event.CHILD_ID)";


            SQLCountNewVisitsOutreach="SELECT COUNT(CHILD_ID) AS IDS FROM  (" +
                    "SELECT  * FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID  " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.VACCINATION_STATUS = 'true' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND  vaccination_event.CHILD_ID NOT IN (" +
                    "SELECT  vaccination_event.CHILD_ID FROM vaccination_event,vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID  " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.CHILD_ID=vaccination_appointment.CHILD_ID  " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<datetime('"+fromDate+"','unixepoch')  GROUP BY vaccination_appointment.ID,vaccination_event.CHILD_ID)" +
                    "GROUP BY vaccination_event.CHILD_ID " +
                    "ORDER BY vaccination_event.VACCINATION_DATE ASC " +
                    ") WHERE OUTREACH='true'";


            SQLCountNewVisitsFixed="SELECT COUNT(CHILD_ID) AS IDS FROM  (" +
                    "SELECT  * FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID  " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.VACCINATION_STATUS = 'true' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND  vaccination_event.CHILD_ID NOT IN (" +
                    "SELECT  vaccination_event.CHILD_ID FROM vaccination_event,vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID  " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.CHILD_ID=vaccination_appointment.CHILD_ID  " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<datetime('"+fromDate+"','unixepoch')  GROUP BY vaccination_appointment.ID,vaccination_event.CHILD_ID)" +
                    "GROUP BY vaccination_event.CHILD_ID " +
                    "ORDER BY vaccination_event.VACCINATION_DATE ASC " +
                    ") WHERE OUTREACH='false'";


            SQLCountNewVisitsWithin = "SELECT COUNT(CHILD_ID) AS IDS FROM  (" +
                    "SELECT  vaccination_event.CHILD_ID AS CHILD_ID,child.HEALTH_FACILITY_ID AS HEALTH_FACILITY_ID  FROM  vaccination_event, vaccination_appointment,child " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID   " +
                    "AND vaccination_event.CHILD_ID=child.ID " +
                    "AND vaccination_event.VACCINATION_STATUS = 'true' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch')  " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND  vaccination_event.CHILD_ID NOT IN ( " +
                    "SELECT  vaccination_event.CHILD_ID FROM vaccination_event,vaccination_appointment WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID   " +
                    "AND vaccination_event.CHILD_ID=vaccination_appointment.CHILD_ID  " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<datetime('"+fromDate+"','unixepoch')  GROUP BY vaccination_appointment.ID,vaccination_event.CHILD_ID)" +
                    "GROUP BY vaccination_event.CHILD_ID " +
                    "ORDER BY vaccination_event.VACCINATION_DATE ASC " +
                    ") WHERE HEALTH_FACILITY_ID = '"+healthFacilityId+"'";


            SQLCountNewVisitsOutside = "SELECT COUNT(CHILD_ID) AS IDS FROM  (" +
                    "SELECT  vaccination_event.CHILD_ID AS CHILD_ID,child.HEALTH_FACILITY_ID AS HEALTH_FACILITY_ID  FROM  vaccination_event, vaccination_appointment,child " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID   " +
                    "AND vaccination_event.CHILD_ID=child.ID " +
                    "AND vaccination_event.VACCINATION_STATUS = 'true' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch')  " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND  vaccination_event.CHILD_ID NOT IN ( " +
                    "SELECT  vaccination_event.CHILD_ID FROM vaccination_event,vaccination_appointment WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID   " +
                    "AND vaccination_event.CHILD_ID=vaccination_appointment.CHILD_ID  " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<datetime('"+fromDate+"','unixepoch')  GROUP BY vaccination_appointment.ID,vaccination_event.CHILD_ID)" +
                    "GROUP BY vaccination_event.CHILD_ID " +
                    "ORDER BY vaccination_event.VACCINATION_DATE ASC " +
                    ") WHERE HEALTH_FACILITY_ID <> '"+healthFacilityId+"'";


            SQLCountUnderImmunizedTotal="SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.VACCINATION_STATUS = 'false' " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";


            SQLCountUnderImmunizedOutreach="SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.VACCINATION_STATUS = 'false'  " +
                    "AND vaccination_appointment.OUTREACH = 'true' " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";


            SQLCountUnderImmunizedFixed=   "SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.VACCINATION_STATUS = 'false'  " +
                    "AND vaccination_appointment.OUTREACH = 'false' " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";


            SQLCountUnderImmunizedWithin="SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment,child " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.VACCINATION_STATUS = 'false'  " +
                    "AND vaccination_event.CHILD_ID=child.ID " +
                    "AND child.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";


            SQLCountUnderImmunizedOutside="SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment,child " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.VACCINATION_STATUS = 'false'  " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.CHILD_ID=child.ID " +
                    "AND child.HEALTH_FACILITY_ID <> '"+healthFacilityId+"' " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') " +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')";


            SQLCountFullyImmunized="SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') "+
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND vaccination_event.APPOINTMENT_ID NOT IN (" +
                    "SELECT DISTINCT(vaccination_event.APPOINTMENT_ID) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID "+
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND vaccination_event.VACCINATION_STATUS = 'false')";

            SQLCountFullyImmunizedOutreach="SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND vaccination_appointment.OUTREACH='true'" +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') "+
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND vaccination_event.APPOINTMENT_ID NOT IN (" +
                    "SELECT DISTINCT(vaccination_event.APPOINTMENT_ID) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND vaccination_event.VACCINATION_STATUS = 'false')";

            SQLCountFullyImmunizedFixed="SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_appointment.OUTREACH='false'" +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') "+
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND vaccination_event.APPOINTMENT_ID NOT IN (" +
                    "SELECT DISTINCT(vaccination_event.APPOINTMENT_ID) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND vaccination_event.VACCINATION_STATUS = 'false')";

            SQLCountFullyImmunizedWithin="SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment,child " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.CHILD_ID=child.ID " +
                    "AND child.HEALTH_FACILITY_ID = '"+healthFacilityId+"' " +
                    "AND vaccination_event.HEALTH_FACILITY_ID == '"+healthFacilityId+"'" +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') "+
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND vaccination_event.APPOINTMENT_ID NOT IN (" +
                    "SELECT DISTINCT(vaccination_event.APPOINTMENT_ID) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.HEALTH_FACILITY_ID == '"+healthFacilityId+"'" +
                    "AND vaccination_event.VACCINATION_STATUS = 'false')";

            SQLCountFullyImmunizedOutside="SELECT COUNT (DISTINCT(vaccination_event.CHILD_ID)) AS IDS FROM  vaccination_event, vaccination_appointment,child " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.CHILD_ID=child.ID " +
                    "AND child.HEALTH_FACILITY_ID <> '"+healthFacilityId+"' " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')>=datetime('"+fromDate+"','unixepoch') "+
                    "AND datetime(substr(vaccination_event.VACCINATION_DATE,7,10), 'unixepoch')<=datetime('"+toDate+"','unixepoch')" +
                    "AND vaccination_event.APPOINTMENT_ID NOT IN (" +
                    "SELECT DISTINCT(vaccination_event.APPOINTMENT_ID) AS IDS FROM  vaccination_event, vaccination_appointment " +
                    "WHERE vaccination_event.APPOINTMENT_ID=vaccination_appointment.ID " +
                    "AND vaccination_event.HEALTH_FACILITY_ID = '"+healthFacilityId+"'" +
                    "AND vaccination_event.VACCINATION_STATUS = 'false')";

            ViewRows viewRows = new ViewRows();
            SQLiteDatabase db = mydb.getReadableDatabase();

            Cursor cursor1 = db.rawQuery(SQLCountTotalOutReach, null);
            if(cursor1.moveToFirst()) {
                viewRows.setTotalOutreach(cursor1.getInt(cursor1.getColumnIndex("IDS")));
            }

            Cursor cursor2 = db.rawQuery(SQLCountTotalVisits,null);
            if(cursor2.moveToFirst())
                viewRows.setTotal(cursor2.getInt(cursor2.getColumnIndex("IDS")));


            Cursor cursor3 = db.rawQuery(SQLCountTotalFixed,null);
            if(cursor3.moveToFirst())
                viewRows.setTotalFixed(cursor3.getInt(cursor3.getColumnIndex("IDS")));


            Cursor cursor4 = db.rawQuery(SQLCountTotalWithin,null);
            if(cursor4.moveToFirst())
                viewRows.setTotalWithin(cursor4.getInt(cursor4.getColumnIndex("IDS")));

            Cursor cursor5 = db.rawQuery(SQLCountTotalOutside,null);
            if(cursor5.moveToFirst())
                viewRows.setTotalOutside(cursor5.getInt(cursor5.getColumnIndex("IDS")));


            Cursor cursor6 = db.rawQuery(SQLCountVaccinedOutReach,null);
            if(cursor6.moveToFirst())
                viewRows.setVaccineOutreach(cursor6.getInt(cursor6.getColumnIndex("IDS")));


            Cursor cursor7 = db.rawQuery(SQLCountVaccinedFixed,null);
            if(cursor7.moveToFirst())
                viewRows.setVaccineFixed(cursor7.getInt(cursor7.getColumnIndex("IDS")));


            Cursor cursor9 = db.rawQuery(SQLCountVaccinedWithin,null);
            if(cursor9.moveToFirst())
                viewRows.setVaccineWithin(cursor9.getInt(cursor9.getColumnIndex("IDS")));

            Cursor cursor10 = db.rawQuery(SQLCountVaccinedOutside,null);
            cursor10.moveToFirst();
            viewRows.setVaccineOutside(cursor10.getInt(cursor10.getColumnIndex("IDS")));


            Cursor cursor11 = db.rawQuery(SQLCountTotalNewVisits,null);
            if(cursor11.moveToFirst())
                viewRows.setNewVisitsTotal(cursor11.getInt(cursor11.getColumnIndex("IDS")));

            Cursor cursor12 = db.rawQuery(SQLCountNewVisitsOutreach,null);
            if(cursor12.moveToFirst())
                viewRows.setNewVisitsOutreach(cursor12.getInt(cursor12.getColumnIndex("IDS")));


            Cursor cursor13 = db.rawQuery(SQLCountNewVisitsFixed,null);
            if(cursor13.moveToFirst())
                viewRows.setNewVisitsFixed(cursor13.getInt(cursor13.getColumnIndex("IDS")));

            Cursor cursor14 = db.rawQuery(SQLCountNewVisitsWithin,null);
            if(cursor14.moveToFirst())
                viewRows.setNewVisitsWithin(cursor14.getInt(cursor14.getColumnIndex("IDS")));



            Cursor cursor15 = db.rawQuery(SQLCountNewVisitsOutside,null);
            if(cursor15.moveToFirst())
                viewRows.setNewVisitsOutside(cursor15.getInt(cursor15.getColumnIndex("IDS")));



            Cursor cursor16 = db.rawQuery(SQLCountUnderImmunizedTotal,null);
            if(cursor16.moveToFirst())
                viewRows.setUnderImmunizedTotal(cursor16.getInt(cursor16.getColumnIndex("IDS")));



            Cursor cursor17 = db.rawQuery(SQLCountUnderImmunizedOutreach,null);
            if(cursor17.moveToFirst())
                viewRows.setUnderImmunizedOutreach(cursor17.getInt(cursor17.getColumnIndex("IDS")));

            Cursor cursor18 = db.rawQuery(SQLCountUnderImmunizedFixed,null);
            if(cursor18.moveToFirst())
                viewRows.setUnderImmunizedFixed(cursor18.getInt(cursor18.getColumnIndex("IDS")));


            Cursor cursor19 = db.rawQuery(SQLCountUnderImmunizedWithin,null);
            if(cursor19.moveToFirst())
                viewRows.setUnderImmunizedWithin(cursor19.getInt(cursor19.getColumnIndex("IDS")));

            Cursor cursor20 = db.rawQuery(SQLCountUnderImmunizedOutside,null);
            if(cursor20.moveToFirst())
                viewRows.setUnderImmunizedOutside(cursor20.getInt(cursor20.getColumnIndex("IDS")));



            Cursor cursor21 = db.rawQuery(SQLCountFullyImmunized,null);
            if(cursor21.moveToFirst())
                viewRows.setFullyImmunizedTotal(cursor21.getInt(cursor21.getColumnIndex("IDS")));


            Cursor cursor22 = db.rawQuery(SQLCountFullyImmunizedOutreach,null);
            if(cursor22.moveToFirst())
                viewRows.setFullyImmunizedOutreach(cursor22.getInt(cursor22.getColumnIndex("IDS")));

            Cursor cursor23 = db.rawQuery(SQLCountFullyImmunizedFixed,null);
            if(cursor23.moveToFirst())
                viewRows.setFullyImmunizedFixed(cursor23.getInt(cursor23.getColumnIndex("IDS")));


            Cursor cursor24 = db.rawQuery(SQLCountFullyImmunizedWithin,null);
            if(cursor24.moveToFirst())
                viewRows.setFullyImmunizedWithin(cursor24.getInt(cursor24.getColumnIndex("IDS")));

            Cursor cursor25 = db.rawQuery(SQLCountFullyImmunizedOutside,null);
            if(cursor25.moveToFirst())
                viewRows.setFullyImmunizedOutside(cursor25.getInt(cursor25.getColumnIndex("IDS")));

            return viewRows;
        }

        @Override
        protected void onPostExecute(ViewRows result) {

            ((TextView)rowview.findViewById(R.id.total_visits_fixed)).setText(result.getTotalFixed()+"");
            ((TextView)rowview.findViewById(R.id.total_visitsoutreach)).setText(result.getTotalOutreach()+"");
            ((TextView)rowview.findViewById(R.id.total_visits_vaccination_strategy)).setText(result.getTotalOutreach()+result.getTotalFixed()+"");
            ((TextView)rowview.findViewById(R.id.total_visits_within)).setText(result.getTotalWithin()+"");
            ((TextView)rowview.findViewById(R.id.total_visits_outside)).setText(result.getTotalOutside()+"");
            ((TextView)rowview.findViewById(R.id.total_visits_total_catchment_area)).setText((result.getTotalWithin()+result.getTotalOutside())+"");

            ((TextView)rowview.findViewById(R.id.vaccinated_fixed)).setText(result.getVaccineFixed()+"");
            ((TextView)rowview.findViewById(R.id.vaccinated_outreach)).setText(result.getVaccineOutreach()+"");
            ((TextView)rowview.findViewById(R.id.vaccinated_vaccination_strategy)).setText(result.getVaccineOutreach()+result.getVaccineFixed()+"");
            ((TextView)rowview.findViewById(R.id.vaccinated_within)).setText(result.getVaccineWithin()+"");
            ((TextView)rowview.findViewById(R.id.vaccinated_outside)).setText(result.getVaccineOutside()+"");
            ((TextView)rowview.findViewById(R.id.vaccinated_catchment_area)).setText((result.getVaccineWithin()+result.getVaccineOutside())+"");

            ((TextView)rowview.findViewById(R.id.new_visits_fixed)).setText(result.getNewVisitsFixed()+"");
            ((TextView)rowview.findViewById(R.id.new_visits_outreach)).setText(result.getNewVisitsOutreach()+"");
            ((TextView)rowview.findViewById(R.id.new_visits_vaccination_strategy)).setText((result.getNewVisitsOutreach()+result.getNewVisitsFixed())+"");
            ((TextView)rowview.findViewById(R.id.new_visits_within)).setText(result.getNewVisitsWithin()+"");
            ((TextView)rowview.findViewById(R.id.new_visits_outside)).setText(result.getNewVisitsOutside()+"");
            ((TextView)rowview.findViewById(R.id.new_visits_catchment_area)).setText((result.getNewVisitsWithin()+result.getNewVisitsOutside())+"");


            ((TextView)rowview.findViewById(R.id.under_immunized_fixed)).setText(result.getUnderImmunizedFixed()+"");
            ((TextView)rowview.findViewById(R.id.under_immunized_outreach)).setText(result.getUnderImmunizedOutreach()+"");
            ((TextView)rowview.findViewById(R.id.under_immunized_vaccination_strategy)).setText(result.getUnderImmunizedTotal()+"");
            ((TextView)rowview.findViewById(R.id.under_immunized_within)).setText(result.getUnderImmunizedWithin()+"");
            ((TextView)rowview.findViewById(R.id.under_immunized_outside)).setText(result.getUnderImmunizedOutside()+"");
            ((TextView)rowview.findViewById(R.id.under_immunized_catchment_area)).setText((result.getUnderImmunizedWithin()+result.getUnderImmunizedOutside())+"");


            ((TextView)rowview.findViewById(R.id.fully_immunized_fixed)).setText((result.getFullyImmunizedFixed())+"");
            ((TextView)rowview.findViewById(R.id.fully_immunized_outreach)).setText(result.getFullyImmunizedOutreach()+"");
            ((TextView)rowview.findViewById(R.id.fully_immunized_vaccination_strategy)).setText((result.getFullyImmunizedTotal())+"");
            ((TextView)rowview.findViewById(R.id.fully_immunized_within)).setText(result.getFullyImmunizedWithin()+"");
            ((TextView)rowview.findViewById(R.id.fully_immunized_outside)).setText(result.getFullyImmunizedOutside()+"");
            ((TextView)rowview.findViewById(R.id.fully_immunized_catchment_area)).setText((result.getFullyImmunizedWithin()+result.getFullyImmunizedOutside())+"");

            progressBar.setVisibility(View.GONE);
            chartList.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }

    class ViewRows {

        public ViewRows(){
        }

        int totalFixed,totalOutreach,total,totalWithin,totalOutside;
        int vaccineFixed,vaccineOutreach,vaccineTotal,vaccineWithin,vaccineOutside;
        int NewVisitsFixed,NewVisitsOutreach,NewVisitsTotal,NewVisitsWithin,NewVisitsOutside;
        int underImmunizedFixed,underImmunizedOutreach,underImmunizedTotal,underImmunizedWithin,underImmunizedOutside;
        int fullyImmunizedFixed,fullyImmunizedOutreach,fullyImmunizedTotal,fullyImmunizedWithin,fullyImmunizedOutside;

        public int getTotalFixed() {
            return totalFixed;
        }

        public void setTotalFixed(int totalFixed) {
            this.totalFixed = totalFixed;
        }

        public int getTotalOutreach() {
            return totalOutreach;
        }

        public void setTotalOutreach(int totalOutreach) {
            this.totalOutreach = totalOutreach;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getTotalWithin() {
            return totalWithin;
        }

        public void setTotalWithin(int totalWithin) {
            this.totalWithin = totalWithin;
        }

        public int getTotalOutside() {
            return totalOutside;
        }

        public void setTotalOutside(int totalOutside) {
            this.totalOutside = totalOutside;
        }

        public int getVaccineFixed() {
            return vaccineFixed;
        }

        public void setVaccineFixed(int vaccineFixed) {
            this.vaccineFixed = vaccineFixed;
        }

        public int getVaccineOutreach() {
            return vaccineOutreach;
        }

        public void setVaccineOutreach(int vaccineOutreach) {
            this.vaccineOutreach = vaccineOutreach;
        }

        public int getVaccineTotal() {
            return vaccineTotal;
        }

        public void setVaccineTotal(int vaccineTotal) {
            this.vaccineTotal = vaccineTotal;
        }

        public int getVaccineWithin() {
            return vaccineWithin;
        }

        public void setVaccineWithin(int vaccineWithin) {
            this.vaccineWithin = vaccineWithin;
        }

        public int getVaccineOutside() {
            return vaccineOutside;
        }

        public void setVaccineOutside(int vaccineOutside) {
            this.vaccineOutside = vaccineOutside;
        }

        public int getNewVisitsFixed() {
            return NewVisitsFixed;
        }

        public void setNewVisitsFixed(int newVisitsFixed) {
            NewVisitsFixed = newVisitsFixed;
        }

        public int getNewVisitsOutreach() {
            return NewVisitsOutreach;
        }

        public void setNewVisitsOutreach(int newVisitsOutreach) {
            NewVisitsOutreach = newVisitsOutreach;
        }

        public int getNewVisitsTotal() {
            return NewVisitsTotal;
        }

        public void setNewVisitsTotal(int newVisitsTotal) {
            NewVisitsTotal = newVisitsTotal;
        }

        public int getNewVisitsWithin() {
            return NewVisitsWithin;
        }

        public void setNewVisitsWithin(int newVisitsWithin) {
            NewVisitsWithin = newVisitsWithin;
        }

        public int getNewVisitsOutside() {
            return NewVisitsOutside;
        }

        public void setNewVisitsOutside(int newVisitsOutside) {
            NewVisitsOutside = newVisitsOutside;
        }


        public int getUnderImmunizedFixed() {
            return underImmunizedFixed;
        }

        public void setUnderImmunizedFixed(int underImmunizedFixed) {
            this.underImmunizedFixed = underImmunizedFixed;
        }

        public int getUnderImmunizedOutreach() {
            return underImmunizedOutreach;
        }

        public void setUnderImmunizedOutreach(int underImmunizedOutreach) {
            this.underImmunizedOutreach = underImmunizedOutreach;
        }

        public int getUnderImmunizedTotal() {
            return underImmunizedTotal;
        }

        public void setUnderImmunizedTotal(int underImmunizedTotal) {
            this.underImmunizedTotal = underImmunizedTotal;
        }

        public int getUnderImmunizedWithin() {
            return underImmunizedWithin;
        }

        public void setUnderImmunizedWithin(int underImmunizedWithin) {
            this.underImmunizedWithin = underImmunizedWithin;
        }

        public int getUnderImmunizedOutside() {
            return underImmunizedOutside;
        }

        public void setUnderImmunizedOutside(int underImmunizedOutside) {
            this.underImmunizedOutside = underImmunizedOutside;
        }

        public int getFullyImmunizedFixed() {
            return fullyImmunizedFixed;
        }

        public void setFullyImmunizedFixed(int fullyImmunizedFixed) {
            this.fullyImmunizedFixed = fullyImmunizedFixed;
        }

        public int getFullyImmunizedOutreach() {
            return fullyImmunizedOutreach;
        }

        public void setFullyImmunizedOutreach(int fullyImmunizedOutreach) {
            this.fullyImmunizedOutreach = fullyImmunizedOutreach;
        }

        public int getFullyImmunizedTotal() {
            return fullyImmunizedTotal;
        }

        public void setFullyImmunizedTotal(int fullyImmunizedTotal) {
            this.fullyImmunizedTotal = fullyImmunizedTotal;
        }

        public int getFullyImmunizedWithin() {
            return fullyImmunizedWithin;
        }

        public void setFullyImmunizedWithin(int fullyImmunizedWithin) {
            this.fullyImmunizedWithin = fullyImmunizedWithin;
        }

        public int getFullyImmunizedOutside() {
            return fullyImmunizedOutside;
        }

        public void setFullyImmunizedOutside(int fullyImmunizedOutside) {
            this.fullyImmunizedOutside = fullyImmunizedOutside;
        }
    }

}
