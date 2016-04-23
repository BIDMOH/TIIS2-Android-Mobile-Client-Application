package mobile.tiis.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import mobile.tiis.app.R;
import mobile.tiis.app.adapters.DrawerListItemsAdapter;
import mobile.tiis.app.base.BackboneActivity;
import mobile.tiis.app.base.BackboneApplication;
import mobile.tiis.app.database.DatabaseHandler;
import mobile.tiis.app.fragments.FragmentStackManager;
import mobile.tiis.app.fragments.VaccinationQueueFragment;
import mobile.tiis.app.helpers.Utils;
import mobile.tiis.app.postman.CheckForChangesSynchronisationService;
import mobile.tiis.app.postman.RoutineAlarmReceiver;

/**
 *  Created by issymac on 10/12/15.
 */
public class HomeActivityRevised extends BackboneActivity {

    IntentFilter filter = new IntentFilter();
    private static final String TAG = "HomeActivityRevised";
    private DrawerLayout drawerLayout;
    private NavigationView nv;
    public  TextView toolbarTitle;
    private boolean isDrawerLocked;

    private boolean canILeave = false;

    private DrawerListItemsAdapter adapter;

    private String[] drawerItems;
    private ActionBarDrawerToggle drawerToggle;

    FragmentStackManager fm;

    public final String[] mFragments = {
            "mobile.tiis.app.fragments.HomeFragment",
            "mobile.tiis.app.fragments.HomeFragment",
            "mobile.tiis.app.fragments.RegisterChildFragment",
            "mobile.tiis.app.fragments.SearchChildFragment",
            "mobile.tiis.app.fragments.VaccinationQueueFragment",
            "mobile.tiis.app.fragments.ReportsFragment",
            "mobile.tiis.app.fragments.MonthlyPlanFragment",
            "mobile.tiis.app.fragments.StockFragment",
            "mobile.tiis.app.fragments.RegisterChildFragment"
    };

    public final String HOME_FRAGMENT = "mobile.tiis.app.fragments.HomeFragment";
    public final String REGISTER_CHILD_FRAGMENT = "mobile.tiis.app.fragments.RegisterChildFragment";
    public final String SEARCH_CHILD_FRAGMENT = "mobile.tiis.app.fragments.SearchChildFragment";
    public final String VACCINATION_QUEUE_FRAGMENT = "mobile.tiis.app.fragments.VaccinationQueueFragment";
    public final String REPORTS_FRAGMENT = "mobile.tiis.app.fragments.ReportsFragment";
    public final String MONTHLY_PLAN_FRAGMENT = "mobile.tiis.app.fragments.MonthlyPlanFragment";
    public final String STOCK_FRAGMENT = "mobile.tiis.app.fragments.StockFragment";

    public final String LOGOUT = "logout";

    public String currentFragment = "";

    public Toolbar toolbar;

    private boolean sync_needed;
    private FrameLayout frameLayout;
    private SharedPreferences sync_preferences;
    private SharedPreferences login_preferences;
    public static final String LOGINPREFERENCE = "loginPrefs" ;

    public AlertDialog.Builder alertDialogBuilder;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getStringExtra(CheckForChangesSynchronisationService.SynchronisationService_MESSAGE);
            Log.d("day5","broadcast service received an intent");

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            try {
                 ((VaccinationQueueFragment) fragment).updateList();
            }catch (Exception e){
                e.printStackTrace();
            }

            try {
                // TODO: Auto Refresh search list from here (uncoment)
                // ((SearchChildFragment) fragment).updateList();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(Bundle starter) {
        super.onCreate(starter);
        Log.e(TAG, TAG);

        setContentView(R.layout.homeactivity_redesigned);
        setupTypeface(HomeActivityRevised.this);
        initializeViews();
        Log.d(TAG, "starting my service");

        final BackboneApplication app = (BackboneApplication) getApplication();
        if (app.getLOGGED_IN_FIRSTNAME() != null && app.getLOGGED_IN_LASTNAME() != null && app.getUsername() != null){
            TextView welcomeText = (TextView) nv.getHeaderView(0).findViewById(R.id.welcome_username);
            welcomeText.setText(app.getLOGGED_IN_FIRSTNAME() + " " + app.getLOGGED_IN_LASTNAME() + " " + "(" + app.getUsername() + ")");
        }else{
            forceLogout();
        }

        //Create dialogue to prompt syncronization
        LayoutInflater li = LayoutInflater.from(HomeActivityRevised.this);
        View promptsView = li.inflate(R.layout.custom_alert_dialogue, null);
        alertDialogBuilder = new AlertDialog.Builder(HomeActivityRevised.this);
        alertDialogBuilder.setView(promptsView);
        TextView message = (TextView) promptsView.findViewById(R.id.dialogMessage);
        message.setText("App Synchronising.... Please Wait!");
        alertDialogBuilder.setCancelable(false);

        login_preferences = getSharedPreferences(LOGINPREFERENCE, Context.MODE_PRIVATE);

        fm = new FragmentStackManager(this);

        String lastFragment = app.LAST_FRAGMENT;
        String lastFragmentTitle = app.LAST_FRAGMENT_TITLE;
        if (starter == null) {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.content_frame, Fragment.instantiate(HomeActivityRevised.this, lastFragment));
            tx.addToBackStack(lastFragment);
            toolbarTitle.setText(lastFragmentTitle);
            tx.commit();
        } else {
//            Log.d("Saving", "Saved back to activity");
//            //Maintain the current fragment
//            String currentFragment = starter.getString("currentFragment");
//            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
//            tx.replace(R.id.content_frame, Fragment.instantiate(HomeActivityRevised.this, currentFragment));
//            tx.addToBackStack(currentFragment);
//            tx.commit();
        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

        }

        sync_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sync_needed = true;
        boolean secondSyncNeeded = false, firstLoginOfDaySyncNeeded = false;
        sync_needed = sync_preferences.getBoolean("synchronization_needed", true);
        secondSyncNeeded = sync_preferences.getBoolean("secondSyncNeeded", false);
        firstLoginOfDaySyncNeeded = sync_preferences.getBoolean("firstLoginOfDaySyncNeeded", false);
        if (app.getLOGGED_IN_FIRSTNAME() == null && app.getLOGGED_IN_LASTNAME() == null && app.getUsername() == null) {
            Log.d("CHECHINGLOGOUT", "No user creds found in the application upon starting the activiry");
            performLogout();
        }else{
            //Do main if necessary
            if (Utils.isOnline(this) && sync_needed && app.getMainSyncronizationNeededStatus()) {
                new mainSynchronisation().execute(0);
            } else if (Utils.isOnline(this) && firstLoginOfDaySyncNeeded) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    new firstLoginOfDaySynchronisation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
                else
                    new firstLoginOfDaySynchronisation().execute(0);
            } else if (Utils.isOnline(this) && secondSyncNeeded) {
                Log.e("RUBIN", "RUBIN secondSyncNeeded");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    new updateSynchronisation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
                else
                    new updateSynchronisation().execute(0);

                //Starting the repeating synchronisation procedure that happens every 10 minutes
                // and pulls changes done to children or children added

            }
            RoutineAlarmReceiver.setAlarmCheckForChangesInChild(this);
            RoutineAlarmReceiver.setPostmanAlarm(HomeActivityRevised.this);
            nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
//                    Snackbar.make(frameLayout, item.getTitle() + " pressed", Snackbar.LENGTH_LONG).show();
                    item.setChecked(true);
                    drawerLayout.closeDrawers();
                    switch (item.getItemId()) {
                        case R.id.drawer_home:
                            if (app.saveNeeded) {
                                app.LAST_FRAGMENT = HOME_FRAGMENT;
                                alertUserLeavingScreen(HOME_FRAGMENT, getString(R.string.home));
                            } else {
                                app.LAST_FRAGMENT = HOME_FRAGMENT;
                                app.LAST_FRAGMENT_TITLE = getString(R.string.home);
                                changeFragment(HOME_FRAGMENT, getString(R.string.home));
                            }
                            break;
                        case R.id.reg_child:
                            if (app.saveNeeded) {
                                app.LAST_FRAGMENT = REGISTER_CHILD_FRAGMENT;
                                alertUserLeavingScreen(REGISTER_CHILD_FRAGMENT, getString(R.string.home_register_child));
                            } else {
                                app.LAST_FRAGMENT = REGISTER_CHILD_FRAGMENT;
                                app.LAST_FRAGMENT_TITLE = getString(R.string.home_register_child);
                                changeFragment(REGISTER_CHILD_FRAGMENT, getString(R.string.home_register_child));
                            }
                            break;
                        case R.id.search_child:
                            if (app.saveNeeded) {
                                app.LAST_FRAGMENT = SEARCH_CHILD_FRAGMENT;
                                alertUserLeavingScreen(SEARCH_CHILD_FRAGMENT, getString(R.string.home_search_child));
                            } else {
                                app.LAST_FRAGMENT = SEARCH_CHILD_FRAGMENT;
                                app.LAST_FRAGMENT_TITLE = getString(R.string.home_search_child);
                                changeFragment(SEARCH_CHILD_FRAGMENT, getString(R.string.home_search_child));
                            }
                            break;
                        case R.id.vac_queue:
                            if (app.saveNeeded) {
                                app.LAST_FRAGMENT = VACCINATION_QUEUE_FRAGMENT;
                                alertUserLeavingScreen(VACCINATION_QUEUE_FRAGMENT, getString(R.string.home_vac_queue));
                            } else {
                                app.LAST_FRAGMENT = VACCINATION_QUEUE_FRAGMENT;
                                app.LAST_FRAGMENT_TITLE = getString(R.string.home_vac_queue);
                                changeFragment(VACCINATION_QUEUE_FRAGMENT, getString(R.string.home_vac_queue));
                            }
                            break;
                        case R.id.reports:
                            if (app.saveNeeded) {
                                app.LAST_FRAGMENT = HOME_FRAGMENT;
                                alertUserLeavingScreen(REPORTS_FRAGMENT, getString(R.string.home_reports));
                            } else {
                                app.LAST_FRAGMENT = HOME_FRAGMENT;
                                app.LAST_FRAGMENT_TITLE = getString(R.string.home);
                                changeFragmentToReportActivity(getString(R.string.home_reports));
                                //changeFragment(REPORTS_FRAGMENT, getString(R.string.home_reports));
                            }
                            break;
                        case R.id.month_plan:
                            if (app.saveNeeded) {
                                app.LAST_FRAGMENT = MONTHLY_PLAN_FRAGMENT;
                                alertUserLeavingScreen(MONTHLY_PLAN_FRAGMENT, getString(R.string.home_monthly_plan));
                            } else {
                                app.LAST_FRAGMENT = MONTHLY_PLAN_FRAGMENT;
                                app.LAST_FRAGMENT_TITLE = getString(R.string.home_monthly_plan);
                                changeFragment(MONTHLY_PLAN_FRAGMENT, getString(R.string.home_monthly_plan));
                            }
                            break;
                        case R.id.stock:
                            if (app.saveNeeded) {
                                app.LAST_FRAGMENT = HOME_FRAGMENT;
                                alertUserLeavingScreen(STOCK_FRAGMENT, getString(R.string.home_stock));
                            } else {
                                app.LAST_FRAGMENT = HOME_FRAGMENT;
                                app.LAST_FRAGMENT_TITLE = getString(R.string.home);
                                changeFragmentToActivity(getString(R.string.home_stock));
                            }
                            break;
                        default:
                            if (app.saveNeeded) {
                                alertUserLeavingScreen(LOGOUT, getString(R.string.home));
                            } else {
                                performLogout();
                            }
                    }
                    return true;
                }
            });

            // Set the adapter for the list view
            drawerItems = getResources().getStringArray(R.array.drawerOptions);

            adapter = new DrawerListItemsAdapter(HomeActivityRevised.this, drawerItems);

            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                    R.drawable.ic_drawer, // nav menu toggle icon
                    R.string.app_name
            ) {
                /**
                 * Called when a drawer has settled in a completely closed state.
                 */
                public void onDrawerClosed(View view) {
                    drawerToggle.syncState();
                    getSupportActionBar().setTitle("TImR");
//                ((FragmentInterface)fragment).showMenuActions();
                    invalidateOptionsMenu();
                }

                /**
                 * Called when a drawer has settled in a completely open state.
                 */
                public void onDrawerOpened(View drawerView) {
                    drawerToggle.syncState();
                    getSupportActionBar().setTitle("Select Option");
//                ((FragmentInterface)fragment).hideMenuActions();
                    invalidateOptionsMenu();
                }
            };

            if (!isDrawerLocked) {
                drawerLayout.setDrawerListener(drawerToggle);
            }

        }

        try {
            String filePath = Environment.getExternalStorageDirectory() + "/giis_logcat.txt";
            Runtime.getRuntime().exec(new String[]{"logcat", "-v", filePath, "*:V", "*:S"});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void performLogout(){

        Log.d("CHECHINGLOGOUT", "Logout is here reason unknown....... ");

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.custom_alert_dialogue, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);

        TextView message = (TextView) promptsView.findViewById(R.id.dialogMessage);
        message.setText("Are you sure you want to Logout");

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                Intent intent = new Intent(HomeActivityRevised.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                login_preferences.edit().putBoolean("isLoggedIn", false).apply();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    private void forceLogout(){
        Intent intent = new Intent(HomeActivityRevised.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        try {
            login_preferences.edit().putBoolean("isLoggedIn", false).apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initializeViews(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        nv = (NavigationView) findViewById(R.id.navigation_view);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        BackboneApplication app = (BackboneApplication) getApplication();
        Log.d("Saving", "Saved" + app.getMainSyncronizationNeededStatus());
        savedInstanceState.putBoolean("mainSync", app.getMainSyncronizationNeededStatus());
        savedInstanceState.putString("currentFragment", currentFragment);

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("Saving", "Saved back to activity");
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
//        boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
//        double myDouble = savedInstanceState.getDouble("myDouble");
//        int myInt = savedInstanceState.getInt("MyInt");

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(status_receiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(status_receiver);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        MenuItem item = null;
        if(item != null) {
//            item.setVisible(!drawerOpen);
        }
        item = null;
        if(item != null) {
//            item.setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private class mainSynchronisation extends AsyncTask<Integer, Integer, Boolean> {

        AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!DatabaseHandler.dbPreinstalled)
                Toast.makeText(getApplicationContext(), "Database synchronization started.", Toast.LENGTH_LONG).show();
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            if (!DatabaseHandler.dbPreinstalled) {
                BackboneApplication application = (BackboneApplication) getApplication();

                application.parsePlace();
                application.parseBirthplace();
                application.parseStatus();
                application.parseWeight();
                application.parseNonVaccinationReason();
                application.parseAgeDefinitions();
                application.parseItem();
                application.parseScheduledVaccination();
                application.parseDose();
                application.parseHealthFacility();
                application.parseItemLots();
                application.parseStock();
                application.parseStockAdjustmentReasons();


                String hfidFoundInVaccEvOnlyAndNotInHealthFac = application.getDatabaseInstance().getHFIDFoundInVaccEvAndNotInHealthFac();
                if (hfidFoundInVaccEvOnlyAndNotInHealthFac != null) {
                    application.parseHealthFacilityThatAreInVaccEventButNotInHealthFac(hfidFoundInVaccEvOnlyAndNotInHealthFac);
                }

                try {
//                    application.intervalGetChildrenByHealthFacilitySinceLastLogin(); // old service
                    application.parseChildCollector(); // old service
//                    application.parseChildCollector2(); // new service
                }catch (Exception e){
                    e.printStackTrace();
                }


                String placesFoundInChildOnlyAndNotInPlace = application.getDatabaseInstance().getDomicilesFoundInChildAndNotInPlace();
                if(placesFoundInChildOnlyAndNotInPlace != null){
                    application.parsePlacesThatAreInChildAndNotInPlaces(placesFoundInChildOnlyAndNotInPlace);
                }

            }
            BackboneApplication app = (BackboneApplication) getApplication();

            SharedPreferences.Editor editor = sync_preferences.edit();
            editor.putBoolean("synchronization_needed", false);
            editor.putBoolean("secondSyncNeeded", false);
            editor.commit();
            app.setMainSyncronizationNeededStatus(false);
            alertDialog.dismiss();
            //Starting the repeating synchronisation procedure that happens every week
            // and pulls changes done to some main tables
            RoutineAlarmReceiver.setAlarmWeeklyUpdateBaseTables(HomeActivityRevised.this.getApplicationContext());
            return true;
        }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Log.e("SYNC FINISHED", "Database synchronization finished.");


        //Starting the repeating synchronisation procedure that happens every 10 minutes
        // and pulls changes done to children or children added
        RoutineAlarmReceiver.setAlarmCheckForChangesInChild(HomeActivityRevised.this);

        if (!DatabaseHandler.dbPreinstalled)
            Toast.makeText(getApplicationContext(), "mainSynchronisation finished.", Toast.LENGTH_LONG).show(

        );
    }
}


    private class updateSynchronisation extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast toast = Toast.makeText(getApplicationContext(), "Database synchronization started.", Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            BackboneApplication application = (BackboneApplication) getApplication();
//            application.continuousModificationParser();
            application.intervalGetChildrenByHealthFacilitySinceLastLogin();

            if(application.getLOGGED_IN_USER_ID()!=null && !application.getLOGGED_IN_USER_ID().equals("")){
                application.getGetChildByIdList();
            }

            Log.e("getting places","getting places that are not found in the table but are in the childrens records");
            String placesFoundInChildOnlyAndNotInPlace = application.getDatabaseInstance().getDomicilesFoundInChildAndNotInPlace();
            if(placesFoundInChildOnlyAndNotInPlace != null){
                application.parsePlacesThatAreInChildAndNotInPlaces(placesFoundInChildOnlyAndNotInPlace);
            }

            String hfidFoundInVaccEvOnlyAndNotInHealthFac = application.getDatabaseInstance().getHFIDFoundInVaccEvAndNotInHealthFac();
            if(hfidFoundInVaccEvOnlyAndNotInHealthFac != null){
                application.parseHealthFacilityThatAreInVaccEventButNotInHealthFac(hfidFoundInVaccEvOnlyAndNotInHealthFac);
            }

            application.parseStock();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            Log.e("SYNC FINISHED", "Database synchronization finished.");
            Toast toast = Toast.makeText(getApplicationContext(), "updateSynchronisation finished.", Toast.LENGTH_LONG);
            toast.show();

        }
    }

    BroadcastReceiver status_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BackboneApplication app = (BackboneApplication) getApplication();
            ImageView wifi_logo = (ImageView)findViewById(R.id.home_wifi_icon);
            if(Utils.isOnline(context)){
//                wifi_logo.setBackgroundColor(0xff00ff00);
                wifi_logo.setImageResource(R.drawable.network_on);
                app.setOnlineStatus(true);
            }
            else{
//                wifi_logo.setBackgroundColor(0xffff0000);
                wifi_logo.setImageResource(R.drawable.network_off);
                app.setOnlineStatus(false);
            }
        }
    };

    private class firstLoginOfDaySynchronisation extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast toast = Toast.makeText(getApplicationContext(), "First synchronization of the day started.", Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            BackboneApplication application = (BackboneApplication) getApplication();
            application.firstLoginOfTheDay();
            if(application.getLOGGED_IN_USER_ID()!=null && !application.getLOGGED_IN_USER_ID().equals("")){
                application.getGetChildByIdList();
            }

            Log.e("getting places","getting places that are not found in the table but are in the childrens records");
            String placesFoundInChildOnlyAndNotInPlace = application.getDatabaseInstance().getDomicilesFoundInChildAndNotInPlace();
            if(placesFoundInChildOnlyAndNotInPlace != null){
                application.parsePlacesThatAreInChildAndNotInPlaces(placesFoundInChildOnlyAndNotInPlace);
            }

            String hfidFoundInVaccEvOnlyAndNotInHealthFac = application.getDatabaseInstance().getHFIDFoundInVaccEvAndNotInHealthFac();
            if(hfidFoundInVaccEvOnlyAndNotInHealthFac != null){
                application.parseHealthFacilityThatAreInVaccEventButNotInHealthFac(hfidFoundInVaccEvOnlyAndNotInHealthFac);
            }

            application.parseStock();

            SharedPreferences.Editor editor = sync_preferences.edit();
            editor.putBoolean("firstLoginOfDaySyncNeeded", false);
            editor.apply();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            Log.e("SYNC FINISHED", "Database synchronization finished. FirstSyncOfDay");
            Toast toast = Toast.makeText(getApplicationContext(), "firstLoginOfDaySynchronisation finished.", Toast.LENGTH_LONG);
            toast.show();

        }
    }

    public void alertUserLeavingScreen(final String fragment, final String title){

        BackboneApplication app = (BackboneApplication) getApplication();

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.custom_alert_dialogue, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        TextView message = (TextView) promptsView.findViewById(R.id.dialogMessage);
        message.setText(app.PROMPT_MESSAGE);

        if (fragment == LOGOUT){
            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // get user input and set it to result
                                    // edit text
                                    performLogout();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
        }else if(fragment.equals(STOCK_FRAGMENT)){
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // get user input and set it to result
                                    // edit text
                                    setPromptNeeded(false);
                                    changeFragmentToActivity(title);
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
        }else if(fragment.equals(REPORTS_FRAGMENT)){
            alertDialogBuilder
        .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                setPromptNeeded(false);
                                changeFragmentToReportActivity(title);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
    }else {
            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // get user input and set it to result
                                    // edit text
                                    setPromptNeeded(false);
                                    changeFragment(fragment, title);
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
        }
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void changeFragmentToActivity(String title){
        Intent intent = new Intent(this, StockActivityRevised.class);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    public void changeFragmentToReportActivity(String title){
        Intent intent = new Intent(this, ReportsActivityRevised.class);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    public void changeFragment(String fragment, String title){
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.content_frame, Fragment.instantiate(HomeActivityRevised.this, fragment));
        tx.addToBackStack(fragment);
        toolbarTitle.setText(title);
        //app.setCurrentFragment("HOME");
        currentFragment = fragment;
        tx.commit();
    }

    public void setPromptNeeded(boolean b){
        BackboneApplication app = (BackboneApplication) getApplication();
        app.saveNeeded = b;
    }

    @Override
    public void onBackPressed(){
        BackboneApplication app = (BackboneApplication) getApplication();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        if (fm.getBackStackEntryCount() > 0) {
            popFragmentPrompt(app.saveNeeded);
        } else {
            Log.i("BAKSTAK", "nothing on backstack, calling super");
            super.onBackPressed();
        }
    }

    public void popFragmentPrompt(Boolean doPrompt){
        final FragmentManager fm = getSupportFragmentManager();
        if (doPrompt){
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.custom_alert_dialogue, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(promptsView);

            TextView message = (TextView) promptsView.findViewById(R.id.dialogMessage);
            message.setText("Are you sure you want to Leave");

            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    fm.popBackStack();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }else {
            Log.i("BAKSTAK", "popping backstack");
            fm.popBackStack();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(CheckForChangesSynchronisationService.SynchronisationService_RESULT)
        );
    }


    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

}