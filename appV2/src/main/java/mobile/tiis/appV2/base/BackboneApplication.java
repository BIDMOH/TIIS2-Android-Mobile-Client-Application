/*******************************************************************************
 * <--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *   ~ Copyright (C)AIRIS Solutions 2015 TIIS App - Tanzania Immunization Information System App
 *   ~
 *   ~    Licensed under the Apache License, Version 2.0 (the "License");
 *   ~    you may not use this file except in compliance with the License.
 *   ~    You may obtain a copy of the License at
 *   ~
 *   ~        http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~    Unless required by applicable law or agreed to in writing, software
 *   ~    distributed under the License is distributed on an "AS IS" BASIS,
 *   ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   ~    See the License for the specific language governing permissions and
 *   ~    limitations under the License.
 *   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
 ******************************************************************************/

package mobile.tiis.appv2.base;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.CheckBox;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import mobile.tiis.appv2.database.DatabaseHandler;
import mobile.tiis.appv2.database.GIISContract;
import mobile.tiis.appv2.database.SQLHandler;
import mobile.tiis.appv2.entity.AdjustmentReasons;
import mobile.tiis.appv2.entity.AdministerVaccinesModel;
import mobile.tiis.appv2.entity.AgeDefinitions;
import mobile.tiis.appv2.entity.Child;
import mobile.tiis.appv2.entity.ChildCollector;
import mobile.tiis.appv2.entity.ChildCollector2;
import mobile.tiis.appv2.entity.Dose;
import mobile.tiis.appv2.entity.HealthFacility;
import mobile.tiis.appv2.entity.Item;
import mobile.tiis.appv2.entity.ItemLot;
import mobile.tiis.appv2.entity.NonVaccinationReason;
import mobile.tiis.appv2.entity.Place;
import mobile.tiis.appv2.entity.ScheduledVaccination;
import mobile.tiis.appv2.entity.Status;
import mobile.tiis.appv2.entity.Stock;
import mobile.tiis.appv2.entity.StockStatusEntity;
import mobile.tiis.appv2.entity.User;
import mobile.tiis.appv2.entity.VaccinationAppointment;
import mobile.tiis.appv2.entity.VaccinationEvent;
import mobile.tiis.appv2.entity.Weight;
import mobile.tiis.appv2.helpers.Utils;
import mobile.tiis.appv2.postman.PostmanModel;
import mobile.tiis.appv2.util.Constants;
import mobile.tiis.appv2.entity.BcgOpvTtSurveillance;
import mobile.tiis.appv2.entity.DeseasesSurveilance;
import mobile.tiis.appv2.entity.HealthFacilityColdChain;
import mobile.tiis.appv2.entity.ImmunizationSession;
import mobile.tiis.appv2.entity.SyringesAndSafetyBoxes;
import mobile.tiis.appv2.entity.VitaminAStock;
import mobile.tiis.appv2.DatabaseModals.SessionsModel;


/**
 * Created by Teodor on 2/3/2015.
 */
public class BackboneApplication extends Application {
    private static final String TAG = BackboneApplication.class.getSimpleName();

    /**
     * Testing WCF
     */
//    public static final String WCF_URL = "https://ec2-54-187-21-117.us-west-2.compute.amazonaws.com/SVC/";

    /**
     * Live WCF
     */
    public static final String WCF_URL = "https://ec2-52-11-215-89.us-west-2.compute.amazonaws.com/SVC/";

    public static final String TABLET_REGISTRATION_MODE_PREFERENCE_NAME = "RegistrationMode";
    public static final String USER_MANAGEMENT_SVC = "UserManagement.svc/";
    public static final String PLACE_MANAGEMENT_SVC = "PlaceManagement.svc/";
    public static final String HEALTH_FACILITY_SVC = "HealthFacilityManagement.svc/";
    public static final String ITEM_MANAGEMENT_SVC = "ItemManagement.svc/";
    public static final String DOSE_MANAGEMENT_SVC = "DoseManagement.svc/";
    public static final String STATUS_MANAGEMENT_SVC = "StatusManagement.svc/";
    public static final String CHILD_MANAGEMENT_SVC = "ChildManagement.svc/";
    public static final String CHILD_SUPPLEMENTS_SVC = "SupplementsManagement.svc/";
    public static final String AUDIT_MANAGEMENT_SVC = "AuditManagement.svc/";
    public static final String SCHEDULED_VACCINATION_MANAGEMENT_SVC = "ScheduledVaccinationManagement.svc/";
    public static final String AGE_DEFINITION_MANAGEMENT_SVC = "AgeDefinitionManagement.svc/";
    public static final String NON_VACCINATION_REASON_MANAGEMENT_SVC = "NonVaccinationReasonManagement.svc/";
    public static final String VACCINATION_EVENT_SVC = "VaccinationEvent.svc/";
    public static final String VACCINATION_QUEUE_MANAGEMENT_SVC = "VaccinationQueueManagement.svc/";
    public static final String STOCK_MANAGEMENT_SVC = "StockManagement.svc/";
    public static final String VACCINATION_APPOINTMENT_MANAGMENT_SVC = "VaccinationAppointmentManagement.svc/";
    public static final String USER_MANAGEMENT_SVC_GETTER = "GetUser";
    public static final String PLACE_MANAGEMENT_SVC_GETTER = "GetPlaceByHealthFacilityId?hf_id=";
    public static final String GET_PLACES_BY_LIST = "GetPlacesByList?pList=";
    public static final String PLACE_MANAGEMENT_SVC_GETTER_BY_ID = "GetPlaceById?id=";
    public static final String STOCK_MANAGEMENT_SVC_GETTER = "GetCurrentStockByLot?hfId=";
    public static final String HEALTH_FACILITY_SVC_GETTER = "GetHealthFacilityById?id=";
    public static final String HEALTH_FACILITIES_SVC_GETTER = "GetHealthFacilities";
    public static final String HEALTH_FACILITY_SVC_GETTER_BY_LIST = "GetHealthFacilityByList?hList=";
    public static final String ITEM_MANAGEMENT_SVC_GETTER = "getitemlist";
    public static final String ITEM_LOT_MANAGEMENT_SVC_GETTER = "getitemlots";
    public static final String STATUS_MANAGEMENT_SVC_GETTER = "getstatuslist";
    public static final String DOSE_MANAGEMENT_SVC_GETTER = "getdoselist";
    public static final String CHILD_MANAGEMENT_SVC_GETTER = "GetChildrenByHealthFacilityV1?healthFacilityId=";
    public static final String CHILD_UPDATE = "UpdateChildWithMothersHivStatusAndTT2VaccineStatus?"; //NOTE: URL Changed
    public static final String REGISTER_CHILD_AEFI = "RegisterChildAEFI?";
    public static final String REGISTER_CHILD_AEFI_BARCODE = "RegisterChildAEFIBarcode?";
    public static final String CHILD_SUPPLEMENTS_INSERT = "RegisterSupplementsBarcode";
    public static final String WEIGHT_MANAGEMENT_SVC_GETTER = "getweight";
    public static final String AGE_DEFINITION_MANAGEMENT_SVC_GETTER = "getagedefinitionslist";
    public static final String SCHEDULED_VACCINATION_MANAGEMENT_SVC_GETTER = "getscheduledvaccinationlist";
    public static final String NON_VACCINATION_REASON_MANAGEMENT_SVC_GETTER = "getnonvaccinationreasonlist";
    public static final String URL_BUILDER_ERROR = "URL_BUILDER_ERROR";
    public static final String GET_PLACE = "GET_PLACE";
    public static final String GET_PLACE_LIST_ID = "GET_PLACE_LIST_ID";
    public static final String GET_PLACE_BY_ID = "GET_PLACE_BY_ID";
    public static final String GET_STOCK = "GET_STOCK";
    public static final String GET_STOCK_ADJUSTMENT = "GetAdjustmentReasons";
    public static final String GET_ITEM_LOT_ID = "GET_ITEM_LOT_ID";
    public static final String GET_HEALTH_FACILITY = "GET_HEALTH_FACILITY";
    public static final String GET_HEALTH_FACILITIES = "GET_HEALTH_FACILITIES";
    public static final String GET_HEALTH_FACILITY_LIST_ID = "GET_HEALTH_FACILITY_LIST_ID";
    public static final String GET_ITEM_LIST = "GET_ITEM_LIST";
    public static final String GET_DOSE_LIST = "GET_DOSE_LIST";
    public static final String GET_WEIGHT_LIST = "GET_CHILD_WEIGHT_LIST";
    public static final String GET_CHILD = "GET_CHILD";
    public static final String GET_STATUS_LIST = "GET_STATUS_LIST";
    public static final String GET_AGE_DEFINITIONS_LIST = "GET_AGE_DEFINITIONS_LIST";
    public static final String GET_SCHEDULED_VACCINATION_LIST = "GET_SCHEDULED_VACCINATION_LIST";
    public static final String GET_NON_VACCINATION_REASON_LIST = "GET_NON_VACCINATION_REASON_LIST";

    public static final String GET_FACILITY_COLD_CHAIN = "GET_FACILITY_COLD_CHAIN";

    public static final String GET_STOCK_DISTRIBUTIONS= "GetHealthFacilityStockDistributions";

    //checkin
    public static final String SEARCH_BY_BARCODE = "SearchByBarcodeV1";
    public static final String UPDATE_VACCINATION_QUEUE = "UpdateVaccinationQueue";
    public static final String REGISTER_AUDIT = "RegisterAudit";
    public static final String GET_VACCINATION_QUEUE_BY_DATE_AND_USER = "GetVaccinationQueueByDateAndUser";
    public static final String PLACEMANAGEMENT_GETBIRTHPLACELIST = "PlaceManagement.svc/GetBirthplaceList";

    public static final String GET_HEALTH_FACILITY_CHILD_CUMULATIVE_REGISTRATION_NUMBER = "HealthFacilityManagement.svc/GetCumulativeChildId";
    public static final String UPDATE_HEALTH_FACILITY_CHILD_CUMULATIVE_REGISTRATION_NUMBER = "HealthFacilityManagement.svc/UpdateHealthFacilityCumulativeChildSn";
    //ChildID
    public static final String CHILD_ID = "childID";
    //register audit Constants
    public static final String CHILD_AUDIT = "CHILD";
    public static final int ACTION_CHECKIN = 5;
    private static final String CHECK_PREINSTALLED_DB_KEY = "check_preinstalled_db_key";

    //Fields Edited Watcher
    public static boolean saveNeeded = false;
    public static String PROMPT_MESSAGE = "Are you sure you want to Leave?";

    //Thread handler
    protected Handler handler;
    private String USERNAME = "default";
    private boolean ONLINE_STATUS = false;
    private String CURRENT_ACTIVITY = "default";
    public String APPOINTMENT_LIST_FRAGMENT = "appointmentListFragment";
    public String VACCINATE_CHILD_FRAGMENT = "vaccinateChildFragment";

    //On login
    private String LOGGED_IN_USER_ID;
    private String LOGGED_IN_USERNAME;
    private String LOGGED_IN_USER_PASS;
    private String LOGGED_IN_FIRSTNAME;
    private String LOGGED_IN_LASTNAME;
    private String LOGGED_IN_USER_HF_ID;
    private DatabaseHandler databaseInstance;
    private boolean main_sync_needed = true;
    private boolean MODIFICATION_SYNCRONIZATION_COMPLETED_STATUS = false;
    private int SYNCRONIZATION_STATUS = 0;
    private boolean administerVaccineHidden = false;
    public static final String AUDIT_MANAGEMENT_GET_CONFIGURATION = "AuditManagement.svc/GetConfiguration";

    public String LAST_FRAGMENT = "mobile.tiis.appv2.fragments.HomeFragment";
    public String LAST_FRAGMENT_TITLE = "Home";
    private String CURRENT_FRAGMENT = "HOME";

    public static String getWcfUrl() {
        return WCF_URL;
    }

    public String getUsername() {
        return USERNAME;
    }

    public void setUsername(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    public boolean getOnlineStatus() {
        return ONLINE_STATUS;
    }

    public void setOnlineStatus(Boolean ONLINE_STATUS) {
        this.ONLINE_STATUS = ONLINE_STATUS;
    }

    public String getCurrentActivity() {
        return CURRENT_ACTIVITY;
    }

    public void setCurrentActivity(String CURRENT_ACTIVITY) {
        this.CURRENT_ACTIVITY = CURRENT_ACTIVITY;
    }

    public String getCurrentFragment() {
        return CURRENT_FRAGMENT;
    }

    public void setCurrentFragment(String CURRENT_FRAGMENT) {
        this.CURRENT_FRAGMENT = CURRENT_FRAGMENT;
    }

    public DatabaseHandler getDatabaseInstance() {
        if (databaseInstance == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (!prefs.contains(CHECK_PREINSTALLED_DB_KEY)) {
                DatabaseHandler.dbPreinstalled = DatabaseHandler.checkIfThereIsDatabaseFile(this);
                prefs.edit().putBoolean(CHECK_PREINSTALLED_DB_KEY, DatabaseHandler.dbPreinstalled).commit();
            } else {
                DatabaseHandler.dbPreinstalled = prefs.getBoolean(CHECK_PREINSTALLED_DB_KEY, false);
            }
            databaseInstance = new DatabaseHandler(this);
        }
        return databaseInstance;
    }

    public boolean getMainSyncronizationNeededStatus() {
        return main_sync_needed;
    }

    public void setMainSyncronizationNeededStatus(boolean MAIN_SYNCRONIZATION_NEEDED_STATUS) {
        this.main_sync_needed = MAIN_SYNCRONIZATION_NEEDED_STATUS;
    }

    public int getSyncronizationStatus() {
        return SYNCRONIZATION_STATUS;
    }

    public void setSyncronizationStatus(int SYNCRONIZATION_STATUS) {
        /**
         * -1 Not started
         * 0 In progress
         */
        this.SYNCRONIZATION_STATUS = SYNCRONIZATION_STATUS;
    }

    public void initializeOffline(String username, String password) {
        Log.d(TAG,"initiating offline");
        if (databaseInstance != null) {
            Log.d(TAG,"databaseinstance is not null");
            List<User> allUsers = databaseInstance.getAllUsers();
            Log.d(TAG,"users count = "+allUsers.size());
            for (User thisUser : allUsers) {
                Log.d(TAG,"users in device = "+thisUser.getUsername());
                if (thisUser.getUsername().equals(username)) {
                    //Log.d("UserId is now offline", thisUser.getId());
                    LOGGED_IN_USER_ID = thisUser.getId();
                    LOGGED_IN_USERNAME = thisUser.getUsername();
                    //Log.d("Initializied offline username", thisUser.getUsername());
                    LOGGED_IN_FIRSTNAME = thisUser.getFirstname();
                    //Log.d("Initializied offline firstname", thisUser.getFirstname());
                    LOGGED_IN_LASTNAME = thisUser.getLastname();
                    //Log.d("Initializied offline lastname", thisUser.getLastname());
                    LOGGED_IN_USER_HF_ID = thisUser.getHealthFacilityId();
                    LOGGED_IN_USER_PASS = password;
                    Log.d(TAG,"user found username = "+LOGGED_IN_USERNAME);
                    return;
                }
            }
        }


    }

    public void setLoggedInUserId(String value) {
        LOGGED_IN_USER_ID = value;
    }

    public String getLOGGED_IN_USER_ID() {
        return LOGGED_IN_USER_ID;
    }

    public String getLOGGED_IN_USER_HF_ID() {
        return LOGGED_IN_USER_HF_ID;
    }

    public String getLOGGED_IN_LASTNAME() {
        return LOGGED_IN_LASTNAME;
    }

    public String getLOGGED_IN_FIRSTNAME() {
        return LOGGED_IN_FIRSTNAME;
    }

    public String getLOGGED_IN_USERNAME() {
        return LOGGED_IN_USERNAME;
    }

    public String getLOGGED_IN_USER_PASS() {
        return LOGGED_IN_USER_PASS;
    }

    public void setLOGGED_IN_USER_PASS(String LOGGED_IN_USER_PASS) {
        this.LOGGED_IN_USER_PASS = LOGGED_IN_USER_PASS;
    }

    public String getHealthFacilityDistrictName(String healthFacilityId){
        return  databaseInstance.getDistrictNames(healthFacilityId);
    }
    public boolean getAdministerVaccineHidden() {
        return administerVaccineHidden;
    }

    public void setAdministerVaccineHidden(Boolean a) {
        administerVaccineHidden = a;
    }

    public void setLoggedInUsername(String value) {
        LOGGED_IN_USERNAME = value;
    }

    public void setLoggedInFirstname(String value) {
        LOGGED_IN_FIRSTNAME = value;
    }

    public void setLoggedInLastname(String value) {
        LOGGED_IN_LASTNAME = value;
    }

    public void setLoggedInUserHealthFacilityId(String value) {
        LOGGED_IN_USER_HF_ID = value;
    }

    private static String deviceId = null;

    public static String getDeviceId(Context ctx) {
        if (deviceId == null) {
            TelephonyManager tm = (TelephonyManager) ctx.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();
        }
        return deviceId;
    }


    public void parsePlace() {
        Log.d(TAG,"parse place started");
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_PLACE);
            client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + responseString);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

                    List<Place> objects = new ArrayList<Place>();
                    try {
                        objects = mapper.readValue(responseString, new TypeReference<List<Place>>() {
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        for (Place object : objects) {
                            ContentValues values = new ContentValues();
                            //Log.d("Place ID", object.getId());
                            values.put(SQLHandler.PlaceColumns.ID, object.getId());
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.PlaceColumns.NAME, object.getName());
                            values.put(SQLHandler.PlaceColumns.HEALTH_FACILITY_ID, object.getHealthFacilityId());
                            values.put(SQLHandler.PlaceColumns.CODE, object.getCode());
                            DatabaseHandler db = getDatabaseInstance();
                            db.addPlacesThatWereNotInDB(values, object.getId());
                        }
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG,"parse place finished");

    }

    public void parseBirthplace() {
        Log.d(TAG,"parse place started");
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }
            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(PLACEMANAGEMENT_GETBIRTHPLACELIST);
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    try {
                        JSONArray jarr = new JSONArray(responseString);
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + jarr.toString());
                        for (int i = 0; i < jarr.length(); i++) {
                            JSONObject jobj = jarr.getJSONObject(i);
                            ContentValues cv = new ContentValues();
                            cv.put(SQLHandler.PlaceColumns.ID, jobj.getInt("Id") + "");
                            cv.put(SQLHandler.PlaceColumns.NAME, jobj.getString("Name"));
                            getDatabaseInstance().addBirthplaces(cv, jobj.getInt("Id") + "");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"parse birstplace finished");

    }
    public void loginRequest(){
        Log.d("coze", "inside login request");
        StringBuilder webServiceLoginURL = null;
        try {

            webServiceLoginURL = new StringBuilder(WCF_URL).append(USER_MANAGEMENT_SVC)
                    .append(USER_MANAGEMENT_SVC_GETTER)
                    .append("?username=").append(URLEncoder.encode(LOGGED_IN_USERNAME, "utf-8"))
                    .append("&password=").append(URLEncoder.encode(LOGGED_IN_USER_PASS, "utf-8"));

            client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            client.get(webServiceLoginURL.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Log.d("coze","logen in success");
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void parseConfiguration() {
        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(AUDIT_MANAGEMENT_GET_CONFIGURATION);
        Log.d("", webServiceUrl.toString());

        try {
            client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    try {
                        JSONArray jarr = new JSONArray(responseString);
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + jarr.toString());
                        for (int i = 0; i < jarr.length(); i++) {
                            JSONObject jobj = jarr.getJSONObject(i);
                            if (jobj.getString("key").equals(Constants.LimitNumberOfDaysBeforeExpire)) {
                                Constants.LimitNumberOfDaysBeforeExpireVal = jobj.getInt("value");
                                saveConfiguration(Constants.LimitNumberOfDaysBeforeExpire, Constants.LimitNumberOfDaysBeforeExpireVal);
                            } else if (jobj.getString("key").equals(Constants.EligibleForVaccination)) {
                                Constants.EligibleForVaccinationVal = jobj.getInt("value");
                                saveConfiguration(Constants.EligibleForVaccination, Constants.EligibleForVaccinationVal);
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void saveConfiguration(String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.CONFIG, Context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }


    /**
     * method takes a string that contains potentially many ID of places and than sends them to server in order to get the other info for these places
     *
     * @param idsTokenized ids in the format "1,123,231..."
     */
    public void parsePlacesThatAreInChildAndNotInPlaces(String idsTokenized) {
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_PLACE_LIST_ID);
            webServiceUrl.append(URLEncoder.encode(idsTokenized));
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<Place> objects = new ArrayList<Place>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<Place>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (Place object : objects) {
                            ContentValues values = new ContentValues();
                            //Log.d("Place ID", object.getId());
                            values.put(SQLHandler.PlaceColumns.ID, object.getId());
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.PlaceColumns.NAME, object.getName());
                            //Log.d("Place NAME", object.getName());
                            values.put(SQLHandler.PlaceColumns.CODE, object.getCode());
                            DatabaseHandler db = getDatabaseInstance();
                            db.addPlacesThatWereNotInDB(values, object.getId());
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseHealthFacilityThatAreInVaccEventButNotInHealthFac(String idsTokenized) {
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_HEALTH_FACILITY_LIST_ID);
            webServiceUrl.append(URLEncoder.encode(idsTokenized));
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<HealthFacility> objects = new ArrayList<HealthFacility>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<HealthFacility>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (HealthFacility object : objects) {
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.HealthFacilityColumns.ID, object.getId());
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.HealthFacilityColumns.CODE, object.getCode());
                            values.put(SQLHandler.HealthFacilityColumns.PARENT_ID, object.getParentId());
                            values.put(SQLHandler.HealthFacilityColumns.NAME, object.getName());
                            DatabaseHandler db = getDatabaseInstance();
                            db.addHFIDThatWereNotInDB(values, object.getId());
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseStock() {
        if (LOGGED_IN_USER_HF_ID == null || LOGGED_IN_USER_HF_ID.equals("0")) return;
        final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_STOCK);
        Log.d(TAG, "Parsing Stock Periodically : "+webServiceUrl.toString());

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                List<Stock> objects = new ArrayList<Stock>();
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    objects = mapper.readValue(response, new TypeReference<List<Stock>>() {
                    });

                } catch (JsonGenerationException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    for (Stock object : objects) {
                        ContentValues values = new ContentValues();
                        values.put(SQLHandler.HealthFacilityBalanceColumns.BALANCE, object.getBalance());
                        values.put(SQLHandler.HealthFacilityBalanceColumns.EXPIRE_DATE, object.getExpireDate());
                        values.put(SQLHandler.HealthFacilityBalanceColumns.GTIN, object.getGtin());
                        values.put(SQLHandler.HealthFacilityBalanceColumns.LOT_ID, object.getLotId());
                        values.put(SQLHandler.HealthFacilityBalanceColumns.LOT_NUMBER, object.getLotNumber());
                        values.put(SQLHandler.HealthFacilityBalanceColumns.ITEM, object.getItem());
                        values.put(SQLHandler.HealthFacilityBalanceColumns.REORDER_QTY, object.getReorderQty());
                        values.put(SQLHandler.HealthFacilityBalanceColumns.GTIN_ISACTIVE, object.getGtinIsActive());
                        values.put(SQLHandler.HealthFacilityBalanceColumns.LOT_ISACTIVE, object.getLotIsActive());
                        DatabaseHandler db = getDatabaseInstance();
                        if (!db.isStockInDB(object.getLotId(), object.getGtin())) {
                            db.addStock(values);
                        } else {
                            db.updateStock(values, object.getLotId());
                        }
                    }
                }
            }
        });

        Log.d(TAG,"parse stock finished");
    }


    public boolean parseStockStatusInformation(String fromDate, String toDate, final String reportingMonth){
        if (LOGGED_IN_USER_HF_ID == null || LOGGED_IN_USER_HF_ID.equals("0")) return false;

        /*
        ec2-54-187-21-117.us-west-2.compute.amazonaws.com/SVC/StockManagement.svc/&fromDate=2016-11-132016-11-13
         */
        final StringBuilder webServiceUrl;
        webServiceUrl = new StringBuilder(WCF_URL).append(STOCK_MANAGEMENT_SVC);
        webServiceUrl.append("GetHealthFacilityCurrentStockByDose?hfid=").append(getLOGGED_IN_USER_HF_ID())
                .append("&fromDate=").append(fromDate)
                .append("&toDate=").append(toDate);

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                List<StockStatusEntity> objects = new ArrayList<StockStatusEntity>();
                Log.d("monthstartandenddate", "Response : "+response);
                Log.d("monthstartandenddate", "month is : "+reportingMonth);
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    objects = mapper.readValue(response, new TypeReference<List<StockStatusEntity>>() {
                    });

                } catch (JsonGenerationException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    for (StockStatusEntity object : objects) {

                        /*
                        "antigen": "OPV",
                        "childrenImmunized": 227,
                        "dosesDiscardedOpened": 62,
                        "dosesDiscardedUnopened": 0,
                        "dosesReceived": 0,
                        "openingBalance": 133,
                        "stockOnHand": 40
                        */

                        ContentValues values = new ContentValues();
                        values.put(SQLHandler.SyncColumns.UPDATED, 1);
                        values.put(SQLHandler.StockStatusColumns.DISCARDED_UNOPENED, object.getDosesDiscardedUnopened());
                        values.put(SQLHandler.StockStatusColumns.DISCARDED_OPENED, object.getDosesDiscardedOpened());
                        values.put(SQLHandler.StockStatusColumns.DOSES_RECEIVED, object.getDosesReceived());
                        values.put(SQLHandler.StockStatusColumns.ITEM_NAME, object.getAntigen());
                        values.put(SQLHandler.StockStatusColumns.CLOSING_BALANCE, object.getStockOnHand());
                        values.put(SQLHandler.StockStatusColumns.OPPENING_BALANCE, object.getOpeningBalance());
                        values.put(SQLHandler.StockStatusColumns.REPORTED_MONTH, reportingMonth);
                        values.put(SQLHandler.StockStatusColumns.IMMUNIZED_CHILDREN, object.getChildrenImmunized());
                        DatabaseHandler db = getDatabaseInstance();
                        if (!db.isStockStatusInDB(reportingMonth, object.getAntigen())) {
                            db.addStockStatusReport(values);
                            Log.d("monthstartandenddate", "adding new status for month : "+reportingMonth);
                        } else {
                            db.updateStockStatusReport(values, reportingMonth, object.getAntigen());
                            Log.d("monthstartandenddate", "updating existing status for month : "+reportingMonth);
                        }
                    }
                }
            }
        });
        return true;
    }


    public boolean addChildVaccinationEventVaccinationAppointment(ChildCollector2 childCollector) {
        Log.d("coze","saving data to db");

        boolean containsData = false;
        List<Child> children = childCollector.getChildList();
        List<VaccinationEvent> vaccinationEvents = childCollector.getVeList();
        List<VaccinationAppointment> vaccinationAppointments = childCollector.getVaList();
        DatabaseHandler db = getDatabaseInstance();

        SQLiteDatabase db1 = db.getWritableDatabase();
        db1.beginTransactionNonExclusive();
        try {

            if (children != null) {
                String sql0 = "INSERT OR REPLACE INTO " + SQLHandler.Tables.CHILD + " ( "+
                        SQLHandler.SyncColumns.UPDATED+", "+
                        SQLHandler.ChildColumns.ID+","+
                        SQLHandler.ChildColumns.BARCODE_ID+","+
                        SQLHandler.ChildColumns.FIRSTNAME1+","+
                        SQLHandler.ChildColumns.FIRSTNAME2+","+
                        SQLHandler.ChildColumns.LASTNAME1+","+
                        SQLHandler.ChildColumns.BIRTHDATE+","+
                        SQLHandler.ChildColumns.GENDER+","+
                        SQLHandler.ChildColumns.TEMP_ID+","+
                        SQLHandler.ChildColumns.HEALTH_FACILITY+","+
                        SQLHandler.ChildColumns.DOMICILE+","+
                        SQLHandler.ChildColumns.DOMICILE_ID+","+
                        SQLHandler.ChildColumns.HEALTH_FACILITY_ID+","+
                        SQLHandler.ChildColumns.STATUS_ID+","+
                        SQLHandler.ChildColumns.BIRTHPLACE_ID+","+
                        SQLHandler.ChildColumns.NOTES+","+
                        SQLHandler.ChildColumns.STATUS+","+
                        SQLHandler.ChildColumns.MOTHER_FIRSTNAME+","+
                        SQLHandler.ChildColumns.MOTHER_LASTNAME+","+
                        SQLHandler.ChildColumns.PHONE+","+
                        SQLHandler.ChildColumns.CUMULATIVE_SERIAL_NUMBER+","+
                        SQLHandler.ChildColumns.CHILD_REGISTRY_YEAR+","+
                        SQLHandler.ChildColumns.MOTHER_VVU_STS+","+
                        SQLHandler.ChildColumns.MOTHER_TT2_STS+","+
                        SQLHandler.ChildColumns.MODIFIED_ON+
                        " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?)";

                SQLiteStatement stmt0 = db1.compileStatement(sql0);
                for (Child child : children) {
                    containsData = true;
                    stmt0.bindString(1, "1");
                    stmt0.bindString(2, child.getId()==null?"":child.getId());
                    stmt0.bindString(3, child.getBarcodeID()==null?"":child.getBarcodeID());
                    stmt0.bindString(4, child.getFirstname1()==null?"":child.getFirstname1());
                    stmt0.bindString(5, child.getFirstname2()==null?"":child.getFirstname2());
                    stmt0.bindString(6, child.getLastname1()==null?"":child.getLastname1());
                    stmt0.bindString(7, child.getBirthdate()==null?"":child.getBirthdate());
                    stmt0.bindString(8, child.getGender()==null?"":child.getGender());
                    stmt0.bindString(9, child.getTempId()==null?"":child.getTempId());
                    stmt0.bindString(10, child.getHealthcenter()==null?"":child.getHealthcenter());
                    stmt0.bindString(11, child.getDomicile()==null?"":child.getDomicile());
                    stmt0.bindString(12, child.getDomicileId()==null?"":child.getDomicileId());
                    stmt0.bindString(13, child.getHealthcenterId()==null?"":child.getHealthcenterId());
                    stmt0.bindString(14, child.getStatusId()==null?"":child.getStatusId());
                    stmt0.bindString(15, child.getBirthplaceId()==null?"":child.getBirthplaceId());
                    stmt0.bindString(16, child.getNotes()==null?"":child.getNotes());
                    stmt0.bindString(17, child.getDomicile()==null?"":child.getDomicile());
                    stmt0.bindString(18, child.getMotherFirstname()==null?"":child.getMotherFirstname());
                    stmt0.bindString(19, child.getMotherLastname()==null?"":child.getMotherLastname());
                    stmt0.bindString(20, child.getPhone()==null?"":child.getPhone());
                    stmt0.bindString(21, child.getChildCumulativeSn()==null?"":child.getChildCumulativeSn());
                    stmt0.bindString(22, child.getChildRegistryYear()==null?"":child.getChildRegistryYear());
                    stmt0.bindString(23, child.getMotherHivStatus()==null?"":child.getMotherHivStatus());
                    stmt0.bindString(24, child.getMotherTT2Status()==null?"":child.getMotherTT2Status());
                    stmt0.bindString(25, child.getModifiedOn()==null?"":child.getModifiedOn());
                    stmt0.execute();
                    stmt0.clearBindings();
                }
            }

            if (vaccinationEvents != null) {
                String sql = "INSERT OR REPLACE INTO " + SQLHandler.Tables.VACCINATION_EVENT + " ( "+
                        SQLHandler.SyncColumns.UPDATED+", "+
                        SQLHandler.VaccinationEventColumns.APPOINTMENT_ID+","+
                        SQLHandler.VaccinationEventColumns.CHILD_ID+","+
                        SQLHandler.VaccinationEventColumns.DOSE_ID+","+
                        SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID+","+
                        SQLHandler.VaccinationEventColumns.ID+","+
                        SQLHandler.VaccinationEventColumns.IS_ACTIVE+","+
                        SQLHandler.VaccinationEventColumns.MODIFIED_BY+","+
                        SQLHandler.VaccinationEventColumns.MODIFIED_ON+","+
                        SQLHandler.VaccinationEventColumns.NONVACCINATION_REASON_ID+","+
                        SQLHandler.VaccinationEventColumns.SCHEDULED_DATE+","+
                        SQLHandler.VaccinationEventColumns.VACCINATION_DATE+","+
                        SQLHandler.VaccinationEventColumns.VACCINATION_STATUS+","+
                        SQLHandler.VaccinationEventColumns.VACCINE_LOT_ID+
                        " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                SQLiteStatement stmt = db1.compileStatement(sql);

                for (VaccinationEvent vaccinationEvent : vaccinationEvents) {
                    containsData = true;
                    stmt.bindString(1, "1");
                    stmt.bindString(2, vaccinationEvent.getAppointmentId());
                    stmt.bindString(3, vaccinationEvent.getChildId());
                    stmt.bindString(4, vaccinationEvent.getDoseId());
                    stmt.bindString(5, vaccinationEvent.getHealthFacilityId());
                    stmt.bindString(6, vaccinationEvent.getId());
                    stmt.bindString(7, vaccinationEvent.getIsActive());
                    stmt.bindString(8, vaccinationEvent.getModifiedBy());
                    stmt.bindString(9, vaccinationEvent.getModifiedOn());
                    stmt.bindString(10, vaccinationEvent.getNonvaccinationReasonId());
                    stmt.bindString(11, vaccinationEvent.getScheduledDate());
                    stmt.bindString(12, vaccinationEvent.getVaccinationDate());
                    stmt.bindString(13, vaccinationEvent.getVaccinationStatus());
                    stmt.bindString(14, vaccinationEvent.getVaccineLotId());
                    stmt.execute();
                    stmt.clearBindings();
                }
            }

            if (vaccinationAppointments != null) {
                String sql1 = "INSERT OR REPLACE INTO " + SQLHandler.Tables.VACCINATION_APPOINTMENT + " ( "+
                        SQLHandler.SyncColumns.UPDATED+", "+
                        SQLHandler.VaccinationAppointmentColumns.CHILD_ID+","+
                        SQLHandler.VaccinationAppointmentColumns.ID+","+
                        SQLHandler.VaccinationAppointmentColumns.IS_ACTIVE+","+
                        SQLHandler.VaccinationAppointmentColumns.MODIFIED_BY+","+
                        SQLHandler.VaccinationAppointmentColumns.MODIFIED_ON+","+
                        SQLHandler.VaccinationAppointmentColumns.NOTES+","+
                        SQLHandler.VaccinationAppointmentColumns.OUTREACH+","+
                        SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE+","+
                        SQLHandler.VaccinationAppointmentColumns.SCHEDULED_FACILITY_ID+
                        " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

                SQLiteStatement stmt1 = db1.compileStatement(sql1);
                for (VaccinationAppointment vaccinationAppointment : vaccinationAppointments) {
                    containsData = true;
                    stmt1.bindString(1, "1");
                    stmt1.bindString(2, vaccinationAppointment.getChildId());
                    stmt1.bindString(3, vaccinationAppointment.getId());
                    stmt1.bindString(4, vaccinationAppointment.getIsActive());
                    stmt1.bindString(5, vaccinationAppointment.getModifiedBy());
                    stmt1.bindString(6, vaccinationAppointment.getModifiedOn());
                    stmt1.bindString(7, vaccinationAppointment.getNotes());
                    stmt1.bindString(8, vaccinationAppointment.getOutreach());
                    stmt1.bindString(9, vaccinationAppointment.getScheduledDate());
                    stmt1.bindString(10, vaccinationAppointment.getScheduledFacilityId());
                    Log.d("day20", "Out Reach for "+vaccinationAppointment.getChildId()+" is : "+vaccinationAppointment.getOutreach());
                    stmt1.execute();
                    stmt1.clearBindings();

                }
            }

            db1.setTransactionSuccessful();
            db1.endTransaction();
        } catch (Exception e) {
            try {
                db1.endTransaction();
            }catch (Exception e1){
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        Log.d("coze", "saving data to db returning = " + containsData);
        return containsData ;
    }


    /**
     * method to be used  to update or insert child information after child updates are received
     * taking into consideration of the following issues
     * if a childId is already available, update or insert the new child
     * If the childId is not available check if the child's barcode is available if so update or inserth the child
     * @param childCollector
     */
    public void addChildVaccinationEventVaccinationAppointmentUnOptimisedForSmallAmountsOfData(ChildCollector childCollector){
        Child child = childCollector.getChildEntity();
        List<VaccinationEvent> vaccinationEvents = childCollector.getVeList();
        List<VaccinationAppointment> vaccinationAppointments = childCollector.getVaList();
        ContentValues childCV = new ContentValues();
        ContentValues vaccEventCV = new ContentValues();
        ContentValues vaccAppointmentCV = new ContentValues();
        DatabaseHandler db = getDatabaseInstance();

        long tStart = System.currentTimeMillis();

        boolean childIdInDB=false;      //variable used to specify if the childId is in db
        boolean childBarcodeInDB=false; //variable used to specity if the child's barcode is in db
        String orgChildId="";           //variable used to store the original child's Id  found in db just incase the child used a temporaly id before reveiving these updates, the temp child id in all vaccination appointments and vaccination events needs to be updated



        Log.e("TIMING LOG","start insert/update child in DB");
        childCV.put(SQLHandler.SyncColumns.UPDATED, 1);
        childCV.put(SQLHandler.ChildColumns.ID, child.getId());
        childCV.put(SQLHandler.ChildColumns.BARCODE_ID, child.getBarcodeID());
        childCV.put(SQLHandler.ChildColumns.FIRSTNAME1, child.getFirstname1());
        childCV.put(SQLHandler.ChildColumns.FIRSTNAME2, child.getFirstname2());
        childCV.put(SQLHandler.ChildColumns.LASTNAME1, child.getLastname1());
        childCV.put(SQLHandler.ChildColumns.BIRTHDATE, child.getBirthdate());
        childCV.put(SQLHandler.ChildColumns.GENDER, child.getGender());
        childCV.put(SQLHandler.ChildColumns.TEMP_ID, child.getTempId());
        childCV.put(SQLHandler.ChildColumns.HEALTH_FACILITY, child.getHealthcenter());
        childCV.put(SQLHandler.ChildColumns.DOMICILE, child.getDomicile());
        childCV.put(SQLHandler.ChildColumns.DOMICILE_ID, child.getDomicileId());
        childCV.put(SQLHandler.ChildColumns.HEALTH_FACILITY_ID, child.getHealthcenterId());
        childCV.put(SQLHandler.ChildColumns.STATUS_ID, child.getStatusId());
        childCV.put(SQLHandler.ChildColumns.BIRTHPLACE_ID, child.getBirthplaceId());
        childCV.put(SQLHandler.ChildColumns.NOTES, child.getNotes());
        childCV.put(SQLHandler.ChildColumns.STATUS, child.getDomicile());
        childCV.put(SQLHandler.ChildColumns.MOTHER_FIRSTNAME, child.getMotherFirstname());
        childCV.put(SQLHandler.ChildColumns.MOTHER_LASTNAME, child.getMotherLastname());
        childCV.put(SQLHandler.ChildColumns.PHONE, child.getPhone());
        childCV.put(SQLHandler.ChildColumns.CUMULATIVE_SERIAL_NUMBER, child.getChildCumulativeSn());
        childCV.put(SQLHandler.ChildColumns.MOTHER_TT2_STS, child.getMotherTT2Status());
        childCV.put(SQLHandler.ChildColumns.MOTHER_VVU_STS, child.getMotherHivStatus());
        childCV.put(SQLHandler.ChildColumns.CHILD_REGISTRY_YEAR, child.getChildRegistryYear());


        if(!db.isChildIDInChildTable(child.getId())) {
            childIdInDB= false;
            if(!db.isChildBarcodeIDInChildTable(child.getBarcodeID())){
                childBarcodeInDB=false;
                Log.d("delay","inserting child with barcode = "+child.getBarcodeID());
                db.addChild(childCV);
            }else{
                childBarcodeInDB=true;
                Log.d("delay","updating child with barcode = "+child.getBarcodeID()+" to childId = "+ child.getId());

                //obtaining the previous tempId used by the child inorder to use it in updating child's vaccination events and vaccination appointments
                orgChildId = db.getChildIdByBarcode(child.getBarcodeID());
                db.updateChildWithBarcode(childCV,child.getBarcodeID());
            }

        }else{
            childIdInDB= true;
            Log.d("delay","updating child with barcode = "+child.getBarcodeID());
            db.updateChild(childCV,child.getId());
        }



        Log.e("TIMING LOG","elapsed time insert/update child in DB (milliseconds): " + (System.currentTimeMillis() - tStart));
        tStart = System.currentTimeMillis();

        Log.e("TIMING LOG","start insert/update Vaccination Event list in DB");
        for(VaccinationEvent vaccinationEvent : vaccinationEvents){
            vaccEventCV.put(SQLHandler.SyncColumns.UPDATED, 1);
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID, vaccinationEvent.getAppointmentId());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.CHILD_ID, vaccinationEvent.getChildId());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.DOSE_ID, vaccinationEvent.getDoseId());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID, vaccinationEvent.getHealthFacilityId());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.ID, vaccinationEvent.getId());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.IS_ACTIVE, vaccinationEvent.getIsActive());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.MODIFIED_BY, vaccinationEvent.getModifiedBy());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.MODIFIED_ON, vaccinationEvent.getModifiedOn());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.NONVACCINATION_REASON_ID, vaccinationEvent.getNonvaccinationReasonId());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.SCHEDULED_DATE, vaccinationEvent.getScheduledDate());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.VACCINATION_DATE, vaccinationEvent.getVaccinationDate());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.VACCINATION_STATUS, vaccinationEvent.getVaccinationStatus());
            vaccEventCV.put(SQLHandler.VaccinationEventColumns.VACCINE_LOT_ID, vaccinationEvent.getVaccineLotId());
            if(childIdInDB){
                if(!db.isVaccinationEventInDb(vaccinationEvent.getChildId(), vaccinationEvent.getDoseId())) {
                    Log.d("delay","inserting vaccination event with childId = "+vaccinationEvent.getChildId());
                    db.addVaccinationEvent(vaccEventCV);
                }else{
                    Log.d("delay","updating vaccination event with childId = "+vaccinationEvent.getChildId());
                    db.updateVaccinationEvent(vaccEventCV,vaccinationEvent.getId());
                }
            }else if(childBarcodeInDB){
                //child's barcode is available in the db so update the original child's temporally id to the new childId from the server
                if(!db.isVaccinationEventInDb(orgChildId, vaccinationEvent.getDoseId())) {
                    db.addVaccinationEvent(vaccEventCV);
                    Log.d("delay","inserting vaccination event with barcode-childId = "+orgChildId);
                }else{
                    Log.d("delay","updating vaccination event with dose-id and barcode-childId = "+orgChildId+" with child ID = "+vaccinationEvent.getChildId());
                    //assuming the vaccination event was originally added by the same user on this tablet with a temporarily childId
                    // only modify the vaccination event id and foreign keys ids without overiding the vaccination status
                    // since the vaccination event may have updates that are  still in postman and has not yet been synched to the server
                    ContentValues ve = new ContentValues();
                    ve.put(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID, vaccinationEvent.getAppointmentId());
                    ve.put(SQLHandler.VaccinationEventColumns.CHILD_ID, vaccinationEvent.getChildId());
                    ve.put(SQLHandler.VaccinationEventColumns.ID, vaccinationEvent.getId());
                    ve.put(SQLHandler.VaccinationEventColumns.IS_ACTIVE, vaccinationEvent.getIsActive());
                    long i = db.updateVaccinationEvent(ve,orgChildId,vaccinationEvent.getDoseId());
                    Log.d("delay","updating vaccination event results = "+i);
                }
            }else{
                Log.d("delay","inserting vaccination event with childId = "+vaccinationEvent.getChildId());
                db.addVaccinationEvent(vaccEventCV);
            }

        }

        Log.e("TIMING LOG","elapsed time insert/update Vaccination Event list in DB (milliseconds): " + (System.currentTimeMillis() - tStart));
        tStart = System.currentTimeMillis();

        Log.e("TIMING LOG","start insert/update Vaccination Appointment list in DB");

        for(VaccinationAppointment vaccinationAppointment : vaccinationAppointments){
            vaccAppointmentCV.put(SQLHandler.SyncColumns.UPDATED, 1);
            vaccAppointmentCV.put(SQLHandler.VaccinationAppointmentColumns.CHILD_ID, vaccinationAppointment.getChildId());
            vaccAppointmentCV.put(SQLHandler.VaccinationAppointmentColumns.ID, vaccinationAppointment.getId());
            vaccAppointmentCV.put(SQLHandler.VaccinationAppointmentColumns.IS_ACTIVE, vaccinationAppointment.getIsActive());
            vaccAppointmentCV.put(SQLHandler.VaccinationAppointmentColumns.MODIFIED_BY, vaccinationAppointment.getModifiedBy());
            vaccAppointmentCV.put(SQLHandler.VaccinationAppointmentColumns.MODIFIED_ON, vaccinationAppointment.getModifiedOn());
            vaccAppointmentCV.put(SQLHandler.VaccinationAppointmentColumns.NOTES, vaccinationAppointment.getNotes());
            vaccAppointmentCV.put(SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE, vaccinationAppointment.getScheduledDate());
            vaccAppointmentCV.put(SQLHandler.VaccinationAppointmentColumns.SCHEDULED_FACILITY_ID, vaccinationAppointment.getScheduledFacilityId());

            if(childIdInDB){
                if(!db.isVaccinationAppointmentInDb(vaccinationAppointment.getChildId(), vaccinationAppointment.getScheduledDate())) {
                    db.addVaccinationAppointment(vaccAppointmentCV);
                }else{
                    db.updateVaccinationAppointment(vaccAppointmentCV, vaccinationAppointment.getId());
                }
            }else if(childBarcodeInDB){
                if(!db.isVaccinationAppointmentInDb(orgChildId, vaccinationAppointment.getScheduledDate())) {
                    db.addVaccinationAppointment(vaccAppointmentCV);
                }else{
                    Log.d(TAG,"updating vaccination appointment with oldID= "+orgChildId+" with child ID = "+vaccinationAppointment.getChildId());
                    db.updateVaccinationAppointmentByScheduledDateAndChildId(vaccAppointmentCV, orgChildId,vaccinationAppointment.getScheduledDate());
                }
            }else{
                db.addVaccinationAppointment(vaccAppointmentCV);
            }

        }

        Log.e("TIMING LOG","elapsed time insert/update Vaccination Appointment list in DB (milliseconds): " + (System.currentTimeMillis() - tStart));

    }



    public void parseChildCollector() {
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_CHILD);
            Log.e("parseChildCollector", webServiceUrl.toString());
            List<ChildCollector> objects = new ArrayList<ChildCollector>();

            UsePoolThreadResponseHandler poolThreadResponseHandler = new UsePoolThreadResponseHandler();
            Log.d(TAG,"logged in username = "+LOGGED_IN_USERNAME);
            Log.d(TAG,"logged in password = "+LOGGED_IN_USER_PASS);
            client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            RequestHandle message = client.get(webServiceUrl.toString(), poolThreadResponseHandler);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private ArrayList<Child> searchedChild = new ArrayList<>();
    /**
     * @param firstname
     * @param motherFistname
     * @param DobFrom
     * @param DobTo
     * @param tempId
     * @param surname
     * @param motherSurname
     * @param placeID
     * @param healthID
     * @param villageId
     * @param statusId
     * @return
     * @Arinela this method returns an empty list if we dont get any child from service
     * , arraylist with size more than 0 if we get results
     * or null if communication was not successful
     */
    public ArrayList<Child> searchChild(String Barcode, String firstname, String firstname2, String motherFistname, String DobFrom, String DobTo, String tempId, String surname, String motherSurname,
                                        String placeID, String healthID, String villageId, String statusId) {
        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC);
        webServiceUrl.append("Search?where=");
        boolean isFirstParam = true;
        if (Barcode != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("barcode=" + URLEncoder.encode(Barcode));
        }
        if (firstname != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("firstname1=" + URLEncoder.encode(firstname));
        }
        if (firstname2 != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("firstname2=" + URLEncoder.encode(firstname2));
        }
        if (motherFistname != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("motherfirstname=" + URLEncoder.encode(motherFistname));
        }
        if (DobFrom != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            if (!DobFrom.equals("")) webServiceUrl.append("birthdatefrom=" + DobFrom);
        }
        if (DobTo != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            if (!DobTo.equals("")) webServiceUrl.append("birthdateto=" + DobTo);
        }
        if (tempId != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("tempId=" + URLEncoder.encode(tempId));
        }
        if (surname != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("lastname1=" + URLEncoder.encode(surname));
        }
        if (motherSurname != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("motherlastname=" + URLEncoder.encode(motherSurname));
        }
        if (villageId != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("domicileid=" + villageId);
        }
        if (placeID != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("birthplaceId=" + placeID);
        }
        if (healthID != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("healthfacilityid=" + healthID);
        }
        if (statusId != null) {
            if (!isFirstParam) webServiceUrl.append("!");
            else isFirstParam = false;
            webServiceUrl.append("statusid=" + statusId);
        }
        webServiceUrl.append("!");

        Log.d("searchChild", webServiceUrl.toString());

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                searchedChild=null;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String result) {
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + result);
                    ArrayList<Child> children = new ArrayList<>();
                    JSONArray jChildren = new JSONArray(result);
                    Log.d("WhyDHC", jChildren.toString());
                    for (int i = 0; i < jChildren.length(); i++) {
                        try {
                            long tStart = System.currentTimeMillis();
                            Log.e("TIMING LOG", "Parsing start search children");

                            Child c = new Child();
                            JSONObject jc = jChildren.getJSONObject(i);
                            c.setFirstname1(jc.getString("Firstname1"));
                            c.setFirstname2(jc.getString("Firstname2"));
                            c.setBarcodeID(jc.getString("BarcodeId"));
                            c.setLastname1(jc.getString("Lastname1"));
                            c.setMotherFirstname(jc.getString("MotherFirstname"));
                            c.setMotherLastname(jc.getString("MotherLastname"));
                            c.setBirthdate(jc.getString("Birthdate").substring(6, 19));
                            c.setDomicile(jc.getString("DomicileId"));
                            c.setGender(jc.getBoolean("Gender") == true ? "Male" : "Female");
                            c.setHealthcenter(jc.getString("HealthcenterId"));
                            c.setId(jc.getInt("Id") + "");
                            if (jc.getString("MothersTT2Status") != null){
                                c.setMotherTT2Status(jc.getString("MothersTT2Status"));
                            }
                            if (jc.getString("MothersHivStatus") != null){
                                c.setMotherHivStatus(jc.getString("MothersHivStatus"));
                            }
//                            if (jc.getString("ChildRegistryYear") != null){
//                                c.setChildRegistryYear(jc.getString("ChildRegistryYear"));
//                            }

                            children.add(c);
                            Log.e("TIMING LOG", "elapsed time parsing search children (milliseconds): " + (System.currentTimeMillis() - tStart));
                        } catch (Exception e) {
                        }
                    }
                    Log.e("", "");
                    searchedChild = children;
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        return searchedChild;

    }


    /**
     * START: Implementation To Send monthly Report Data to the server
     */

    /**
     * Method for saving coldchain data to the server
     * @param tempMax
     * @param tempMin
     * @param alarmHigh
     * @param alarmLow
     * @param selectedMonth
     * @param selectedYear
     * @param modOn
     */
    public void sendColdChainToServer(double tempMax, double tempMin, int alarmHigh, int alarmLow, int selectedMonth, int selectedYear, String modOn) {

        final StringBuilder webServiceUrl;
        webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
        webServiceUrl.append("StoreHealthFacilityColdChain?healthFacilityId=").append(getLOGGED_IN_USER_HF_ID())
                .append("&tempMax=").append(tempMax)
                .append("&tempMin=").append(tempMin)
                .append("&alarmHighTemp=").append(alarmHigh)
                .append("&alarmLowTemp=").append(alarmLow)
                .append("&reportingMonth=").append(selectedMonth)
                .append("&userId=").append(getLOGGED_IN_USER_ID())
                .append("&modifiedOn=").append(modOn)
                .append("&reportingYear=").append(selectedYear);

        Log.d(TAG, "cold chain URL : "+webServiceUrl);

        getDatabaseInstance().addPost(webServiceUrl.toString(), 3);
    }


    public void sendDeseaseSurveillanceToServer(int feverMCases, int feverDeaths, int apfMcases, int apfDeaths, int ttMCases, int ttDeaths, int selectedMonth, int selectedYear, String modifiedOnString) {

        final StringBuilder webServiceUrl;
        webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
        webServiceUrl.append("StoreHealthFacilityDeseaseSurvailance?healthFacilityId=").append(getLOGGED_IN_USER_HF_ID())
                .append("&feverMonthlyCases=").append(feverMCases)
                .append("&feverMonthlyDeaths=").append(feverDeaths)
                .append("&AFPMonthlyCases=").append(apfMcases)
                .append("&AFPDeaths=").append(apfDeaths)
                .append("&neonatalTTCases=").append(ttMCases)
                .append("&neonatalTTDeaths=").append(ttDeaths)
                .append("&reportingMonth=").append(selectedMonth)
                .append("&reportingYear=").append(selectedYear)
                .append("&userId=").append(Integer.parseInt(getLOGGED_IN_USER_ID()))
                .append("&modifiedOn=").append(modifiedOnString);

        Log.d(TAG, "Desease Surveillance URL : "+webServiceUrl);

        getDatabaseInstance().addPost(webServiceUrl.toString(), 3);
    }

    public void sendBcgOpvTtToServer(int doseID, int maleServiceArea, int maleCatchmentArea, int femaleServiceArea, int femaleCatchmentArea, int coverageServiceArea, int coverageCatchmentArea,
                                     int coverageCatchmentAndServiceArea, int selectedMonth, int selectedYear, String modifiedOnString) {

        final StringBuilder webServiceUrl;

        webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
        webServiceUrl.append("StoreHealthFacilityBcgOpv0AndTTVaccinations?healthFacilityId=").append(getLOGGED_IN_USER_HF_ID())
                .append("&DoseId=").append(doseID)
                .append("&MaleServiceArea=").append(maleServiceArea)
                .append("&MaleCatchmentArea=").append(maleCatchmentArea)
                .append("&FemaleServiceArea=").append(femaleServiceArea)
                .append("&FemaleCatchmentArea=").append(femaleCatchmentArea)
                .append("&CoverageServiceArea=").append(coverageServiceArea)
                .append("&CoverageCatchmentArea=").append(coverageCatchmentArea)
                .append("&CoverageCatchmentAndServiceArea=").append(coverageCatchmentAndServiceArea)
                .append("&reportingMonth=").append(selectedMonth)
                .append("&reportingYear=").append(selectedYear)
                .append("&userId=").append(Integer.parseInt(getLOGGED_IN_USER_ID()))
                .append("&modifiedOn=").append(modifiedOnString);

        getDatabaseInstance().addPost(webServiceUrl.toString(), 3);
    }

    public void sendImmunizationSessionsToServer(int outreachPlanned,int fixedConducted,int outreachCanceled,int outreachConducted, String otherMajorImmunizationActivities, int reportingMonth, int reportingYear, String modifiedOn){
        final StringBuilder webServiceUrl;

        webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
        webServiceUrl.append("StoreHealthFacilityImmunizationSessions?healthFacilityId=").append(getLOGGED_IN_USER_HF_ID())
                .append("&fixedConducted=").append(fixedConducted)
                .append("&outreachCanceled=").append(outreachCanceled)
                .append("&outreachConducted=").append(outreachConducted)
                .append("&OutreachPlanned=").append(outreachPlanned)
                .append("&OtherMajorImmunizationActivities=").append(otherMajorImmunizationActivities)
                .append("&reportingMonth=").append(reportingMonth)
                .append("&reportingYear=").append(reportingYear)
                .append("&userId=").append(Integer.parseInt(getLOGGED_IN_USER_ID()))
                .append("&modifiedOn=").append(modifiedOn);

        getDatabaseInstance().addPost(webServiceUrl.toString(), 3);
    }

    public void sendSyrinjesAndSafetyBoxesToServer(String itemName, int balance, int received, int stockinhand, int used, int wastage, int stockedoutdays, int reportingmonth, int reportingyear, String modifiedOn){
        final StringBuilder webServiceUrl;

        webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
        webServiceUrl.append("StoreHealthFacilitySyringesAndSafetyBoxesStockBalance?healthFacilityId=").append(getLOGGED_IN_USER_HF_ID())
                .append("&ItemName=").append(itemName)
                .append("&OpeningBalance=").append(balance)
                .append("&Received=").append(received)
                .append("&StockInHand=").append(stockinhand)
                .append("&Used=").append(used)
                .append("&wastage=").append(wastage)
                .append("&StockedOutDays=").append(stockedoutdays)
                .append("&reportingMonth=").append(reportingmonth)
                .append("&reportingYear=").append(reportingyear)
                .append("&userId=").append(Integer.parseInt(getLOGGED_IN_USER_ID()))
                .append("&modifiedOn=").append(modifiedOn);

        getDatabaseInstance().addPost(webServiceUrl.toString(), 3);
    }

    public void sendVitaminAStockToServer(String vitaminName, int openingBalance, int received, int stockInHand, int totalAdministered, int selectedMonth, int selectedYear, String modifiedOnString, int wastage ){
        final StringBuilder webServiceUrl;
        webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
        webServiceUrl.append("StoreHealthFacilityVitaminAStockBalance?healthFacilityId=").append(getLOGGED_IN_USER_HF_ID())
                .append("&VitaminName=").append(vitaminName)
                .append("&OpeningBalance=").append(openingBalance)
                .append("&Received=").append(received)
                .append("&StockInHand=").append(stockInHand)
                .append("&TotalAdministered=").append(totalAdministered)
                .append("&reportingMonth=").append(selectedMonth)
                .append("&reportingYear=").append(selectedYear)
                .append("&userId=").append(Integer.parseInt(getLOGGED_IN_USER_ID()))
                .append("&modifiedOn=").append(modifiedOnString)
                .append("&wastage=").append(wastage);

        Log.d("VITAMIN_A", "URL to Postman is : "+webServiceUrl.toString());
        getDatabaseInstance().addPost(webServiceUrl.toString(), 3);
    }

    public void parseColdChainMonthly(String selectedMonth, String selectedYear){
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityColdChain?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID))
                    .append("&reportingMonth=").append(URLEncoder.encode(selectedMonth))
                    .append("&reportingYear=").append(URLEncoder.encode(selectedYear));

            Log.d(TAG, "Health Facility Cold Chain..........  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("TIMR_COLD_CHAIN", "Error connection to server"+responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    Log.d("TIMR_COLD_CHAIN", "Success adding cold chain "+response);
                    List<HealthFacilityColdChain> objects = new ArrayList<HealthFacilityColdChain>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<HealthFacilityColdChain>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (HealthFacilityColdChain object : objects) {
                            String reportingMonth = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();

                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.RefrigeratorColums.ALARM_HIGH_TEMP, object.getAlarmHighTemp()+"");
                            values.put(SQLHandler.RefrigeratorColums.ALARM_LOW_TEMP, object.getAlarmLowTemp()+"");
                            values.put(SQLHandler.RefrigeratorColums.TEMP_MIN, object.getTempMin()+"");
                            values.put(SQLHandler.RefrigeratorColums.TEMP_MAX, object.getTempMax()+"");
                            values.put(SQLHandler.RefrigeratorColums.REPORTED_MONTH, reportingMonth);
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateRefrigeratorTemperature(values, reportingMonth);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     *
     * This method is used to parse healthFacility Cold Chain upon first syncronization to receive all the coldchain data for that facility
     */
    public void parseHealthFacilityColdChainAsList(){
        Log.d(TAG,"parse health facility coldchain started");
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityColdChainAsList?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID));
            //GetHealthFacilityColdChainAsList?healthFacilityId=17342

            Log.d(TAG, "Health Facility Cold Chain..........  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<HealthFacilityColdChain> objects = new ArrayList<HealthFacilityColdChain>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<HealthFacilityColdChain>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (HealthFacilityColdChain object : objects) {
                            String reportingMonth = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();

                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.RefrigeratorColums.ALARM_HIGH_TEMP, object.getAlarmHighTemp()+"");
                            values.put(SQLHandler.RefrigeratorColums.ALARM_LOW_TEMP, object.getAlarmLowTemp()+"");
                            values.put(SQLHandler.RefrigeratorColums.TEMP_MIN, object.getTempMin()+"");
                            values.put(SQLHandler.RefrigeratorColums.TEMP_MAX, object.getTempMax()+"");
                            values.put(SQLHandler.RefrigeratorColums.REPORTED_MONTH, reportingMonth);
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateRefrigeratorTemperature(values, reportingMonth);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG,"parse health facility cold chain finished");
    }

    public void parseDeseaseSurveilanceMonthly(String selectedMonth, String selectedYear){
        Log.d(TAG,"parse desease surveilance started");
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityDeseaseSurvailance?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID))
                    .append("&reportingMonth=").append(URLEncoder.encode(selectedMonth))
                    .append("&reportingYear=").append(URLEncoder.encode(selectedYear));

            Log.d(TAG, "Desease Surveillance  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("TIMR_DESEASES", "Error connection to server"+responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    Log.d("TIMR_DESEASES", "Success adding Deseases "+response);
                    List<DeseasesSurveilance> objects = new ArrayList<DeseasesSurveilance>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<DeseasesSurveilance>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (DeseasesSurveilance object : objects) {
                            String reportingMonth = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();

                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.SurveillanceColumns.FEVER_MONTHLY_CASES, object.getFeverMonthlyCases()+"");
                            values.put(SQLHandler.SurveillanceColumns.FEVER_DEATHS, object.getFeverMonthlyDeaths()+"");
                            values.put(SQLHandler.SurveillanceColumns.APF_MONTHLY_CASES, object.getAFPMonthlyCases()+"");
                            values.put(SQLHandler.SurveillanceColumns.APF_DEATHS, object.getAFPDeaths()+"");
                            values.put(SQLHandler.SurveillanceColumns.NEONATAL_TT_CASES, object.getNeonatalTTCases()+"");
                            values.put(SQLHandler.SurveillanceColumns.NEONATAL_TT_DEATHS, object.getNeonatalTTDeaths()+"");
                            values.put(SQLHandler.SurveillanceColumns.REPORTED_MONTH, reportingMonth);
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateDeseasesSurveillance(values, reportingMonth);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG,"parse desease survailance finished");
    }

    public void parseDeseaseSurveillanceAsList(){

        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityDeseaseSurvailanceAsList?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID));
            //GetHealthFacilityColdChainAsList?healthFacilityId=17342

            Log.d(TAG, "Health Facility Desease Surveillance..........  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<DeseasesSurveilance> objects = new ArrayList<DeseasesSurveilance>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<DeseasesSurveilance>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (DeseasesSurveilance object : objects) {
                            String seletedMonth = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.SurveillanceColumns.FEVER_MONTHLY_CASES, object.getFeverMonthlyCases()+"");
                            values.put(SQLHandler.SurveillanceColumns.FEVER_DEATHS, object.getFeverMonthlyDeaths()+"");
                            values.put(SQLHandler.SurveillanceColumns.APF_MONTHLY_CASES, object.getAFPMonthlyCases()+"");
                            values.put(SQLHandler.SurveillanceColumns.APF_DEATHS, object.getAFPDeaths()+"");
                            values.put(SQLHandler.SurveillanceColumns.NEONATAL_TT_CASES, object.getNeonatalTTCases()+"");
                            values.put(SQLHandler.SurveillanceColumns.NEONATAL_TT_DEATHS, object.getNeonatalTTDeaths()+"");
                            values.put(SQLHandler.SurveillanceColumns.REPORTED_MONTH, seletedMonth);

                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateDeseasesSurveillance(values, seletedMonth);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void parseBcgOpvTtMonthly(String selectedMonth, String selectedYear){
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityBcgOpv0AndTTVaccinationsAsList?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID))
                    .append("&reportingMonth=").append(URLEncoder.encode(selectedMonth))
                    .append("&reportingYear=").append(URLEncoder.encode(selectedYear));

            Log.d(TAG, "Desease Surveillance  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("BCG_OPV_TT", "Error connection to server"+responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    Log.d("BCG_OPV_TT", "Success adding Deseases "+response);
                    List<BcgOpvTtSurveillance> objects = new ArrayList<BcgOpvTtSurveillance>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<BcgOpvTtSurveillance>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (BcgOpvTtSurveillance object : objects) {
                            String reportingMonth   = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();
                            String dose_id          = object.getDoseId()+"";
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.DOSE_ID, object.getDoseId()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.FEMALE_SERVICE_AREA, object.getFemaleServiceArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.MALE_SERVICE_AREA, object.getMaleServiceArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.FEMALE_CATCHMENT_AREA, object.getFemaleCatchmentArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.MALE_CATCHMENT_AREA, object.getMaleCatchmentArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.COVERAGE_SERVICE_AREA, object.getCoverageServiceArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.COVERAGE_CATCHMENT_AREA, object.getCoverageCatchmentArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.COVERAGE_CATCHMENT_AND_SERVICE, object.getCoverageCatchmentAndServiceArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.REPORTING_MONTH, reportingMonth);
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateVaccinationsBcgOpvTt(values, reportingMonth, dose_id);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseBcgOpvTtAsList(){

        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityBcgOpv0AndTTVaccinationsAsList?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID));
            //GetHealthFacilityColdChainAsList?healthFacilityId=17342

            Log.d(TAG, "BCG_OPV0_TT URL"+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<BcgOpvTtSurveillance> objects = new ArrayList<BcgOpvTtSurveillance>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<BcgOpvTtSurveillance>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (BcgOpvTtSurveillance object : objects) {
                            String reportingMonth   = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();
                            int dose_id        = object.getDoseId();
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.DOSE_ID, object.getDoseId()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.FEMALE_SERVICE_AREA, object.getFemaleServiceArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.MALE_SERVICE_AREA, object.getMaleServiceArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.FEMALE_CATCHMENT_AREA, object.getFemaleCatchmentArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.MALE_CATCHMENT_AREA, object.getMaleCatchmentArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.COVERAGE_SERVICE_AREA, object.getCoverageServiceArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.COVERAGE_CATCHMENT_AREA, object.getCoverageCatchmentArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.COVERAGE_CATCHMENT_AND_SERVICE, object.getCoverageCatchmentAndServiceArea()+"");
                            values.put(SQLHandler.VaccinationsBcgOpvTtColumns.REPORTING_MONTH, reportingMonth);
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateVaccinationsBcgOpvTt(values, reportingMonth, dose_id+"");
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void parseSyringesAndSafetyBoxesMonthly(String selectedMonth, String selectedYear){
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);

            webServiceUrl.append("GetHealthFacilitySyringesAndSafetyBoxesStockBalanceAsList?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID))
                    .append("&reportingMonth=").append(URLEncoder.encode(selectedMonth))
                    .append("&reportingYear=").append(URLEncoder.encode(selectedYear));

            Log.d(TAG, "Syrignes and Safety Boxes URL  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("SYRINGES_BOXES", "Error connection to server"+responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    Log.d("SYRINGES_BOXES", "Success adding Deseases "+response);
                    List<SyringesAndSafetyBoxes> objects = new ArrayList<SyringesAndSafetyBoxes>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<SyringesAndSafetyBoxes>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (SyringesAndSafetyBoxes object : objects) {
                            String reportingMonth   = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();
                            String item_name        = object.getItemName();
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);

                            values.put(GIISContract.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.ITEM_NAME, object.getItemName());
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.OPENING_BALANCE, object.getOpeningBalance()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.RECEIVED, object.getReceived()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.USED, object.getUsed()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.WASTAGE, object.getWastage()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.STOCK_AT_HAND, object.getStockInHand()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.STOCKED_OUT_DAYS, object.getStockedOutDays()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.REPORTING_MONTH, reportingMonth);
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateInjectionEquipment(values, reportingMonth, item_name);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseSyringesAndSafetyBoxesAsList(){

        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            //TODO Add Apropriate URL (SYRINGES AND SAFETY BOXES)
            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilitySyringesAndSafetyBoxesStockBalanceAsList?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID));

            Log.d(TAG, "SYRINGES URL  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<SyringesAndSafetyBoxes> objects = new ArrayList<SyringesAndSafetyBoxes>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<SyringesAndSafetyBoxes>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (SyringesAndSafetyBoxes object : objects) {
                            String reportingMonth   = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();
                            String item_name        = object.getItemName();
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.ITEM_NAME, object.getItemName());
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.OPENING_BALANCE, object.getOpeningBalance()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.RECEIVED, object.getReceived()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.USED, object.getUsed()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.WASTAGE, object.getWastage()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.STOCK_AT_HAND, object.getStockInHand()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.STOCKED_OUT_DAYS, object.getStockedOutDays()+"");
                            values.put(SQLHandler.SyringesAndSafetyBoxesColumns.REPORTING_MONTH, reportingMonth);
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateInjectionEquipment(values, reportingMonth, item_name);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void parseVitamAMonthly(String selectedMonth, String selectedYear){
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityVitaminAStockBalance?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID))
                    .append("&reportingMonth=").append(URLEncoder.encode(selectedMonth))
                    .append("&reportingYear=").append(URLEncoder.encode(selectedYear));

            Log.d("VITAMIN_A", "Vitamin A URL  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("VITAMIN_A", "Error connection to server"+responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    Log.d("VITAMIN_A", "Success adding Deseases "+response);
                    List<VitaminAStock> objects = new ArrayList<VitaminAStock>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<VitaminAStock>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (VitaminAStock object : objects) {
                            String reportingMonth   = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();
                            String vitamin_name        = object.getVitaminName();
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.HfVitaminAColumns.VITAMIN_NAME, object.getVitaminName());
                            values.put(SQLHandler.HfVitaminAColumns.OPENING_BALANCE, object.getOpeningBalance()+"");
                            values.put(SQLHandler.HfVitaminAColumns.RECEIVED, object.getReceived()+"");
                            values.put(SQLHandler.HfVitaminAColumns.TOTAL_ADMINISTERED, object.getTotalAdministered()+"");
                            values.put(SQLHandler.HfVitaminAColumns.WASTAGE, object.getWastage()+"");
                            values.put(SQLHandler.HfVitaminAColumns.STOCK_ON_HAND, object.getStockInHand()+"");
                            values.put(SQLHandler.HfVitaminAColumns.REPORTING_MONTH, reportingMonth);
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateVitaminAStock(values, reportingMonth, vitamin_name);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseVitaminAStockAsList(){

        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityVitaminAStockBalanceAsList?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID));
            //GetHealthFacilityColdChainAsList?healthFacilityId=17342

            Log.d(TAG, "Health Facility Desease Surveillance..........  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<VitaminAStock> objects = new ArrayList<VitaminAStock>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<VitaminAStock>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (VitaminAStock object : objects) {
                            String reportingMonth   = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();
                            String vitamin_name        = object.getHealthFacilityId();
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.HfVitaminAColumns.VITAMIN_NAME, object.getVitaminName());
                            values.put(SQLHandler.HfVitaminAColumns.OPENING_BALANCE, object.getOpeningBalance()+"");
                            values.put(SQLHandler.HfVitaminAColumns.RECEIVED, object.getReceived()+"");
                            values.put(SQLHandler.HfVitaminAColumns.TOTAL_ADMINISTERED, object.getTotalAdministered()+"");
                            values.put(SQLHandler.HfVitaminAColumns.WASTAGE, object.getWastage()+"");
                            values.put(SQLHandler.HfVitaminAColumns.STOCK_ON_HAND, object.getStockInHand()+"");
                            values.put(SQLHandler.HfVitaminAColumns.REPORTING_MONTH, reportingMonth);
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateVitaminAStock(values, reportingMonth, vitamin_name);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void parseImmunizationSessionMonthly(String selectedMonth, String selectedYear){
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityImmunizationSessions?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID))
                    .append("&reportingMonth=").append(URLEncoder.encode(selectedMonth))
                    .append("&reportingYear=").append(URLEncoder.encode(selectedYear));

            Log.d("IMMUNIZATION_SESSION", "Sessions  URL  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("IMMUNIZATION_SESSION", "Error connection to server"+responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    Log.d("IMMUNIZATION_SESSION", "Success adding Deseases "+response);
                    List<ImmunizationSession> objects = new ArrayList<ImmunizationSession>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<ImmunizationSession>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (ImmunizationSession object : objects) {
                            String reportingMonth   = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();

                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.ImmunizationSessionColumns.OTHERACTIVITIES, object.getOtherMajorImmunizationActivities());
                            values.put(SQLHandler.ImmunizationSessionColumns.OUTREACH_PLANNED, object.getOutreachPlanned()+"");
                            values.put(SQLHandler.ImmunizationSessionColumns.REPORTING_MONTH, reportingMonth);

                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateImmunizationSessions(values, reportingMonth);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseImmunizationSessionAsList(){

        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);
            webServiceUrl.append("GetHealthFacilityImmunizationSessionsAsList?healthFacilityId=").append(URLEncoder.encode(LOGGED_IN_USER_HF_ID));
            //GetHealthFacilityColdChainAsList?healthFacilityId=17342

            Log.d(TAG, "Immunization Sessions  "+webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<ImmunizationSession> objects = new ArrayList<ImmunizationSession>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<ImmunizationSession>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (ImmunizationSession object : objects) {
                            String reportingMonth   = getDatabaseInstance().getMonthNameFromNumber(object.getReportedMonth()+"", BackboneApplication.this)+" "+object.getReportedYear();

                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.ImmunizationSessionColumns.OTHERACTIVITIES, object.getOtherMajorImmunizationActivities());
                            values.put(SQLHandler.ImmunizationSessionColumns.OUTREACH_PLANNED, object.getOutreachPlanned()+"");
                            values.put(SQLHandler.ImmunizationSessionColumns.REPORTING_MONTH, reportingMonth);

                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateImmunizationSessions(values, reportingMonth);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * END: Services To Send monthly Report Data to the server
     */


    /**
     * @param barcode
     * @param dateToday
     * @param dateTodayTimestamp
     * @param weight
     * @param modBy
     * @return
     * @Arinela
     */
    private boolean weightSaved = false;
    public boolean saveWeight(String barcode, String dateToday, String dateTodayTimestamp, String weight, String modBy) {
        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC);
        webServiceUrl.append("RegisterChildWeightBarcode?barcode=").append(URLEncoder.encode(barcode)).append("&date=").append(dateToday).append("&weight=").append(weight)
                .append("&modifiedon=").append(dateTodayTimestamp).append("&modifiedby=").append(modBy);


        getDatabaseInstance().addPost(webServiceUrl.toString(), 1);

        return weightSaved;


    }

    /**
     * this method takes barcode as input and returns 1 and inserts child if child is registered(we get data from server),
     * 2 if child is not regstered(we dont get data from server) and 3 if statusCode not 200
     *
     * @param barcode
     */
    private int parseChildCollectorSearchByBarcodeResults;
    public int parseChildCollectorSearchByBarcode(String barcode) {
        final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, SEARCH_BY_BARCODE);
        webServiceUrl.append("?barcodeId=").append(URLEncoder.encode(barcode));

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                parseChildCollectorSearchByBarcodeResults=3;
                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                ChildCollector childCollector = new ChildCollector();
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    childCollector = mapper.readValue(new JSONArray(response).getJSONObject(0).toString(), ChildCollector.class);

                    addChildVaccinationEventVaccinationAppointment(childCollector);
                    parseChildCollectorSearchByBarcodeResults = 1;
                } catch (JsonGenerationException e) {
                    e.printStackTrace();
                    parseChildCollectorSearchByBarcodeResults = 2;
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                    parseChildCollectorSearchByBarcodeResults = 2;
                } catch (IOException e) {
                    e.printStackTrace();
                    parseChildCollectorSearchByBarcodeResults = 3;
                } catch (JSONException e) {
                    e.printStackTrace();
                    parseChildCollectorSearchByBarcodeResults = 2;
                }

            }
        });
        return parseChildCollectorSearchByBarcodeResults;
    }

    /**
     * Method that sends the updated child data to the server.
     * The server responds 1 for succes and 0 for error.
     *
     * @param url
     * @return
     */
    private boolean updateChildResults = false;
    public boolean updateChild(StringBuilder url) {
        url.append("&userId=" + LOGGED_IN_USER_ID);
        Log.d("coze","updating child url = "+url);

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        RequestHandle message = client.get(url.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(responseString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        updateChildResults = false;
                    }
                    if (jsonObject != null && jsonObject.optString("id", "-1").equalsIgnoreCase("1")) {
                        updateChildResults = true;
                    } else {
                        updateChildResults = false;
                    }
                }catch (Exception e){
                    updateChildResults =false;
                }
            }
        });
        return updateChildResults;
    }


    /**
     * Method that sends the updated of an aefi appointement for teh child data to the server.
     * The server responds 1 for succes and 0 for error.
     *
     * @param url
     * @return
     */
    private boolean updateAefiAppointementResults=false;
    public boolean updateAefiAppointement(StringBuilder url) {
        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        RequestHandle message = client.get(url.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                updateAefiAppointementResults=false;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String result) {
                try {

                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + result);
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        updateAefiAppointementResults = false;
                    }
                    if (jsonObject != null && jsonObject.optString("id", "-1").equalsIgnoreCase("1")) {
                        updateAefiAppointementResults = true;
                    } else {
                        updateAefiAppointementResults = false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    updateAefiAppointementResults = false;
                }
            }
        });
        return updateAefiAppointementResults;
    }

    /**
     * Method that insert a new Child Supplement id in the server
     *
     * @param url
     * @return - the newly inserted id in the server, or a negative value in case of error
     */
    private long insertChildSupplementidChildResults = -1;
    public long insertChildSupplementidChild(String url) {
        if (url != null && !url.isEmpty()) {
            client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            client.get(url.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    throwable.printStackTrace();
                    insertChildSupplementidChildResults=-1;
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject = new JSONObject(responseString);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            insertChildSupplementidChildResults = -1;
                        }
                        if (jsonObject != null) {
                            long newId = Long.parseLong(jsonObject.optString("id", "-1"));
                            insertChildSupplementidChildResults = newId;
                        } else {
                            insertChildSupplementidChildResults = -1;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        insertChildSupplementidChildResults = -1;
                    }
                }
            });
        }
        return insertChildSupplementidChildResults;
    }

    /**
     * this method  updates vaccination Queue table in Server DB and returns true if statusCode == 200
     * or false if not
     */
    private boolean updateVaccinationQueueResult=false;
    public boolean updateVaccinationQueue(String barcode, String childHfid, String dateNow, String userId) {
        final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, UPDATE_VACCINATION_QUEUE);
        webServiceUrl.append("?barcode=").append(URLEncoder.encode(barcode)).append("&hfid=").append(childHfid)
                .append("&date=").append(dateNow).append("&userId=").append(userId);
        Log.e("day30", webServiceUrl.toString());

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                updateVaccinationQueueResult=false;
                getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                updateVaccinationQueueResult =true;
            }
        });

        return updateVaccinationQueueResult;
    }


    /**
     * this method takes the date of today and the logged in user id and returns 1 and .....(we get data from server),
     * 2 if there is no entry for the queue(we dont get data from server) and 3 if statusCode not 200
     *
     * @return int that shows result interpretation
     */
    public int getVaccinationQueueByDateAndUser() {
        final StringBuilder webServiceUrl = createWebServiceURL("", GET_VACCINATION_QUEUE_BY_DATE_AND_USER);
        webServiceUrl.append("?date=").append(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime())).append("&userId=").append(getLOGGED_IN_USER_ID());
        Log.e("getVaccQueueByDt&Usr", webServiceUrl.toString());
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(webServiceUrl.toString());
            Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + webServiceUrl.toString());
            httpGet.setHeader("Authorization", "Basic " + Base64.encodeToString((LOGGED_IN_USERNAME + ":" + LOGGED_IN_USER_PASS).getBytes(), Base64.NO_WRAP));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Utils.writeNetworkLogFileOnSD(
                        Utils.returnDeviceIdAndTimestamp(getApplicationContext())
                                + " StatusCode " + httpResponse.getStatusLine().getStatusCode()
                                + " ReasonPhrase " + httpResponse.getStatusLine().getReasonPhrase()
                                + " ProtocolVersion " + httpResponse.getStatusLine().getProtocolVersion());
                return 3;
            }
            InputStream inputStream = httpResponse.getEntity().getContent();
            String responseAsString = Utils.getStringFromInputStream(inputStream);
            Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + responseAsString);
            JSONArray jarr = new JSONArray(responseAsString);
            DatabaseHandler db = getDatabaseInstance();
            String childBarcodesNotInDB = "";
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject jobj = jarr.getJSONObject(i);
                if (db.isBarcodeInChildTable(jobj.getString("BarcodeId"))) {
                    String dateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").format(new Date(Long.parseLong(jobj.getString("Date").substring(6, 19))));

                    ContentValues cv = new ContentValues();
                    cv.put(SQLHandler.VaccinationQueueColumns.CHILD_ID, db.getChildIdByBarcode(jobj.getString("BarcodeId")));
                    cv.put(SQLHandler.VaccinationQueueColumns.DATE, dateNow);
                    db.addChildToVaccinationQueue(cv);
                } else {
                    if (childBarcodesNotInDB.length() > 0) childBarcodesNotInDB += ",";
                    childBarcodesNotInDB += jobj.getString("BarcodeId");
                }
            }

            if (childBarcodesNotInDB.length() > 0) {
                getChildByBarcodeList(childBarcodesNotInDB);
            }
            return 1;
        } catch (JsonGenerationException e) {
            e.printStackTrace();
            return 2;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return 2;
        } catch (IOException e) {
            e.printStackTrace();
            return 3;
        } catch (JSONException e) {
            e.printStackTrace();
            return 2;
        }
    }


    /**
     * this method  regiters Audit in Server DB and returns true if statusCode == 200
     * or false if not
     */
    private boolean registerAuditResult=false;
    public boolean registerAudit(String table, String barcode, String dateNow, String userId, int actionId) {
        final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, REGISTER_AUDIT);
        webServiceUrl.append("?table=").append(table).append("&recordId=").append(barcode)
                .append("&userId=").append(userId).append("&date=").append(dateNow).append("&activityId=").append(actionId);
        Log.d("", webServiceUrl.toString());
        getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
        return registerAuditResult;
    }

    private static SyncHttpClient client = new SyncHttpClient();
    final int DEFAULT_TIMEOUT = 2000000;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "application created");
        client.setMaxRetriesAndTimeout(5,1000);
        client.setURLEncodingEnabled(false);

        client.setTimeout(DEFAULT_TIMEOUT);


        client.setMaxConnections(50);


    }

    public void updateChildVaccinationEventVaccinationAppointment(ChildCollector childCollector) {
        Child child = childCollector.getChildEntity();
        List<VaccinationEvent> vaccinationEvents = childCollector.getVeList();
        List<VaccinationAppointment> vaccinationAppointments = childCollector.getVaList();
        ContentValues childCV = new ContentValues();
        DatabaseHandler db = getDatabaseInstance();




        SQLiteDatabase db1 = db.getWritableDatabase();
        db1.beginTransactionNonExclusive();
        try {
            String sql0 = "INSERT OR REPLACE INTO " + SQLHandler.Tables.CHILD + " ( "+
                    SQLHandler.SyncColumns.UPDATED+", "+
                    SQLHandler.ChildColumns.ID+","+
                    SQLHandler.ChildColumns.BARCODE_ID+","+
                    SQLHandler.ChildColumns.FIRSTNAME1+","+
                    SQLHandler.ChildColumns.FIRSTNAME2+","+
                    SQLHandler.ChildColumns.LASTNAME1+","+
                    SQLHandler.ChildColumns.BIRTHDATE+","+
                    SQLHandler.ChildColumns.GENDER+","+
                    SQLHandler.ChildColumns.TEMP_ID+","+
                    SQLHandler.ChildColumns.HEALTH_FACILITY+","+
                    SQLHandler.ChildColumns.DOMICILE+","+
                    SQLHandler.ChildColumns.DOMICILE_ID+","+
                    SQLHandler.ChildColumns.HEALTH_FACILITY_ID+","+
                    SQLHandler.ChildColumns.STATUS_ID+","+
                    SQLHandler.ChildColumns.BIRTHPLACE_ID+","+
                    SQLHandler.ChildColumns.NOTES+","+
                    SQLHandler.ChildColumns.STATUS+","+
                    SQLHandler.ChildColumns.MOTHER_FIRSTNAME+","+
                    SQLHandler.ChildColumns.MOTHER_LASTNAME+","+
                    SQLHandler.ChildColumns.CUMULATIVE_SERIAL_NUMBER+","+
                    SQLHandler.ChildColumns.CHILD_REGISTRY_YEAR+","+
                    SQLHandler.ChildColumns.MOTHER_TT2_STS+","+
                    SQLHandler.ChildColumns.MOTHER_VVU_STS+","+
                    SQLHandler.ChildColumns.PHONE+
                    " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            SQLiteStatement stmt0 = db1.compileStatement(sql0);
            stmt0.bindString(1, "1");
            stmt0.bindString(2, child.getId()==null?"":child.getId());
            stmt0.bindString(3, child.getBarcodeID()==null?"":child.getBarcodeID());
            stmt0.bindString(4, child.getFirstname1()==null?"":child.getFirstname1());
            stmt0.bindString(5, child.getFirstname2()==null?"":child.getFirstname2());
            stmt0.bindString(6, child.getLastname1()==null?"":child.getLastname1());
            stmt0.bindString(7, child.getBirthdate()==null?"":child.getBirthdate());
            stmt0.bindString(8, child.getGender()==null?"":child.getGender());
            stmt0.bindString(9, child.getTempId()==null?"":child.getTempId());
            stmt0.bindString(10, child.getHealthcenter()==null?"":child.getHealthcenter());
            stmt0.bindString(11, child.getDomicile()==null?"":child.getDomicile());
            stmt0.bindString(12, child.getDomicileId()==null?"":child.getDomicileId());
            stmt0.bindString(13, child.getHealthcenterId()==null?"":child.getHealthcenterId());
            stmt0.bindString(14, child.getStatusId()==null?"":child.getStatusId());
            stmt0.bindString(15, child.getBirthplaceId()==null?"":child.getBirthplaceId());
            stmt0.bindString(16, child.getNotes()==null?"":child.getNotes());
            stmt0.bindString(17, child.getStatus()==null?"":child.getStatus());
            stmt0.bindString(18, child.getMotherFirstname()==null?"":child.getMotherFirstname());
            stmt0.bindString(19, child.getMotherLastname()==null?"":child.getMotherLastname());
            stmt0.bindString(20, child.getChildCumulativeSn()==null?"":child.getChildCumulativeSn());
            stmt0.bindString(21, child.getChildRegistryYear()==null?"":child.getChildRegistryYear());
            stmt0.bindString(22, child.getMotherTT2Status()==null?"":child.getMotherTT2Status());
            stmt0.bindString(23, child.getMotherHivStatus()==null?"":child.getMotherHivStatus());
            stmt0.bindString(24, child.getPhone()==null?"":child.getPhone());
            stmt0.execute();
            stmt0.clearBindings();

            String sql = "INSERT OR REPLACE INTO " + SQLHandler.Tables.VACCINATION_EVENT + " ( "+
                    SQLHandler.SyncColumns.UPDATED+", "+
                    SQLHandler.VaccinationEventColumns.APPOINTMENT_ID+","+
                    SQLHandler.VaccinationEventColumns.CHILD_ID+","+
                    SQLHandler.VaccinationEventColumns.DOSE_ID+","+
                    SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID+","+
                    SQLHandler.VaccinationEventColumns.ID+","+
                    SQLHandler.VaccinationEventColumns.IS_ACTIVE+","+
                    SQLHandler.VaccinationEventColumns.MODIFIED_BY+","+
                    SQLHandler.VaccinationEventColumns.MODIFIED_ON+","+
                    SQLHandler.VaccinationEventColumns.NONVACCINATION_REASON_ID+","+
                    SQLHandler.VaccinationEventColumns.SCHEDULED_DATE+","+
                    SQLHandler.VaccinationEventColumns.VACCINATION_DATE+","+
                    SQLHandler.VaccinationEventColumns.VACCINATION_STATUS+","+
                    SQLHandler.VaccinationEventColumns.VACCINE_LOT_ID+
                    " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            SQLiteStatement stmt = db1.compileStatement(sql);

            for (VaccinationEvent vaccinationEvent : vaccinationEvents) {
                stmt.bindString(1, "1");
                stmt.bindString(2, vaccinationEvent.getAppointmentId());
                stmt.bindString(3, vaccinationEvent.getChildId());
                stmt.bindString(4, vaccinationEvent.getDoseId());
                stmt.bindString(5, vaccinationEvent.getHealthFacilityId());
                stmt.bindString(6, vaccinationEvent.getId());
                stmt.bindString(7, vaccinationEvent.getIsActive());
                stmt.bindString(8, vaccinationEvent.getModifiedBy());
                stmt.bindString(9, vaccinationEvent.getModifiedOn());
                stmt.bindString(10, vaccinationEvent.getNonvaccinationReasonId());
                stmt.bindString(11, vaccinationEvent.getScheduledDate());
                stmt.bindString(12, vaccinationEvent.getVaccinationDate());
                stmt.bindString(13, vaccinationEvent.getVaccinationStatus());
                stmt.bindString(14, vaccinationEvent.getVaccineLotId());
                stmt.execute();
                stmt.clearBindings();
            }

            String sql1 = "INSERT OR REPLACE INTO " + SQLHandler.Tables.VACCINATION_APPOINTMENT + " ( "+
                    SQLHandler.SyncColumns.UPDATED+", "+
                    SQLHandler.VaccinationAppointmentColumns.CHILD_ID+","+
                    SQLHandler.VaccinationAppointmentColumns.ID+","+
                    SQLHandler.VaccinationAppointmentColumns.IS_ACTIVE+","+
                    SQLHandler.VaccinationAppointmentColumns.MODIFIED_BY+","+
                    SQLHandler.VaccinationAppointmentColumns.MODIFIED_ON+","+
                    SQLHandler.VaccinationAppointmentColumns.NOTES+","+
                    SQLHandler.VaccinationAppointmentColumns.OUTREACH+","+
                    SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE+","+
                    SQLHandler.VaccinationAppointmentColumns.SCHEDULED_FACILITY_ID+
                    " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

            SQLiteStatement stmt1 = db1.compileStatement(sql1);

            for (VaccinationAppointment vaccinationAppointment : vaccinationAppointments) {
                stmt1.bindString(1, "1");
                stmt1.bindString(2, vaccinationAppointment.getChildId());
                stmt1.bindString(3, vaccinationAppointment.getId());
                stmt1.bindString(4, vaccinationAppointment.getIsActive());
                stmt1.bindString(5, vaccinationAppointment.getModifiedBy());
                stmt1.bindString(6, vaccinationAppointment.getModifiedOn());
                stmt1.bindString(7, vaccinationAppointment.getNotes());
                stmt1.bindString(8, vaccinationAppointment.getOutreach());
                stmt1.bindString(9, vaccinationAppointment.getScheduledDate());
                stmt1.bindString(10, vaccinationAppointment.getScheduledFacilityId());

                stmt1.execute();
                stmt1.clearBindings();

            }

            db1.setTransactionSuccessful();
            db1.endTransaction();
        } catch (Exception e) {
            db1.endTransaction();
            e.printStackTrace();
        }
    }



    /**
     * method used to add child, vaccination appointments and vaccination events into the database
     *
     * @param childCollector
     */
    public void addChildVaccinationEventVaccinationAppointment(ChildCollector childCollector) {
        Child child = childCollector.getChildEntity();
        List<VaccinationEvent> vaccinationEvents = childCollector.getVeList();
        List<VaccinationAppointment> vaccinationAppointments = childCollector.getVaList();
        ContentValues childCV = new ContentValues();
        DatabaseHandler db = getDatabaseInstance();

        SQLiteDatabase db1 = db.getWritableDatabase();
        db1.beginTransactionNonExclusive();
        try {
            String sql0 = "INSERT OR REPLACE INTO " + SQLHandler.Tables.CHILD + " ( "+
                    SQLHandler.SyncColumns.UPDATED+", "+
                    SQLHandler.ChildColumns.ID+","+
                    SQLHandler.ChildColumns.BARCODE_ID+","+
                    SQLHandler.ChildColumns.FIRSTNAME1+","+
                    SQLHandler.ChildColumns.FIRSTNAME2+","+
                    SQLHandler.ChildColumns.LASTNAME1+","+
                    SQLHandler.ChildColumns.BIRTHDATE+","+
                    SQLHandler.ChildColumns.GENDER+","+
                    SQLHandler.ChildColumns.TEMP_ID+","+
                    SQLHandler.ChildColumns.HEALTH_FACILITY+","+
                    SQLHandler.ChildColumns.DOMICILE+","+
                    SQLHandler.ChildColumns.DOMICILE_ID+","+
                    SQLHandler.ChildColumns.HEALTH_FACILITY_ID+","+
                    SQLHandler.ChildColumns.STATUS_ID+","+
                    SQLHandler.ChildColumns.BIRTHPLACE_ID+","+
                    SQLHandler.ChildColumns.NOTES+","+
                    SQLHandler.ChildColumns.STATUS+","+
                    SQLHandler.ChildColumns.MOTHER_FIRSTNAME+","+
                    SQLHandler.ChildColumns.MOTHER_LASTNAME+","+
                    SQLHandler.ChildColumns.CUMULATIVE_SERIAL_NUMBER+","+
                    SQLHandler.ChildColumns.CHILD_REGISTRY_YEAR+","+
                    SQLHandler.ChildColumns.MOTHER_TT2_STS+","+
                    SQLHandler.ChildColumns.MOTHER_VVU_STS+","+
                    SQLHandler.ChildColumns.PHONE+
                    " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            SQLiteStatement stmt0 = db1.compileStatement(sql0);
            stmt0.bindString(1, "1");
            stmt0.bindString(2, child.getId()==null?"":child.getId());
            stmt0.bindString(3, child.getBarcodeID()==null?"":child.getBarcodeID());
            stmt0.bindString(4, child.getFirstname1()==null?"":child.getFirstname1());
            stmt0.bindString(5, child.getFirstname2()==null?"":child.getFirstname2());
            stmt0.bindString(6, child.getLastname1()==null?"":child.getLastname1());
            stmt0.bindString(7, child.getBirthdate()==null?"":child.getBirthdate());
            stmt0.bindString(8, child.getGender()==null?"":child.getGender());
            stmt0.bindString(9, child.getTempId()==null?"":child.getTempId());
            stmt0.bindString(10, child.getHealthcenter()==null?"":child.getHealthcenter());
            stmt0.bindString(11, child.getDomicile()==null?"":child.getDomicile());
            stmt0.bindString(12, child.getDomicileId()==null?"":child.getDomicileId());
            stmt0.bindString(13, child.getHealthcenterId()==null?"":child.getHealthcenterId());
            stmt0.bindString(14, child.getStatusId()==null?"":child.getStatusId());
            stmt0.bindString(15, child.getBirthplaceId()==null?"":child.getBirthplaceId());
            stmt0.bindString(16, child.getNotes()==null?"":child.getNotes());
            stmt0.bindString(17, child.getStatus()==null?"":child.getStatus());
            stmt0.bindString(18, child.getMotherFirstname()==null?"":child.getMotherFirstname());
            stmt0.bindString(19, child.getMotherLastname()==null?"":child.getMotherLastname());
            stmt0.bindString(20, child.getChildCumulativeSn()==null?"":child.getChildCumulativeSn());
            stmt0.bindString(21, child.getChildRegistryYear()==null?"":child.getChildRegistryYear());
            stmt0.bindString(22, child.getMotherTT2Status()==null?"":child.getMotherTT2Status());
            stmt0.bindString(23, child.getMotherHivStatus()==null?"":child.getMotherHivStatus());
            stmt0.bindString(24, child.getPhone()==null?"":child.getPhone());
            stmt0.execute();
            stmt0.clearBindings();

            String sql = "INSERT OR REPLACE INTO " + SQLHandler.Tables.VACCINATION_EVENT + " ( "+
                    SQLHandler.SyncColumns.UPDATED+", "+
                    SQLHandler.VaccinationEventColumns.APPOINTMENT_ID+","+
                    SQLHandler.VaccinationEventColumns.CHILD_ID+","+
                    SQLHandler.VaccinationEventColumns.DOSE_ID+","+
                    SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID+","+
                    SQLHandler.VaccinationEventColumns.ID+","+
                    SQLHandler.VaccinationEventColumns.IS_ACTIVE+","+
                    SQLHandler.VaccinationEventColumns.MODIFIED_BY+","+
                    SQLHandler.VaccinationEventColumns.MODIFIED_ON+","+
                    SQLHandler.VaccinationEventColumns.NONVACCINATION_REASON_ID+","+
                    SQLHandler.VaccinationEventColumns.SCHEDULED_DATE+","+
                    SQLHandler.VaccinationEventColumns.VACCINATION_DATE+","+
                    SQLHandler.VaccinationEventColumns.VACCINATION_STATUS+","+
                    SQLHandler.VaccinationEventColumns.VACCINE_LOT_ID+
                    " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            SQLiteStatement stmt = db1.compileStatement(sql);

            for (VaccinationEvent vaccinationEvent : vaccinationEvents) {
                stmt.bindString(1, "1");
                stmt.bindString(2, vaccinationEvent.getAppointmentId());
                stmt.bindString(3, vaccinationEvent.getChildId());
                stmt.bindString(4, vaccinationEvent.getDoseId());
                stmt.bindString(5, vaccinationEvent.getHealthFacilityId());
                stmt.bindString(6, vaccinationEvent.getId());
                stmt.bindString(7, vaccinationEvent.getIsActive());
                stmt.bindString(8, vaccinationEvent.getModifiedBy());
                stmt.bindString(9, vaccinationEvent.getModifiedOn());
                stmt.bindString(10, vaccinationEvent.getNonvaccinationReasonId());
                stmt.bindString(11, vaccinationEvent.getScheduledDate());
                stmt.bindString(12, vaccinationEvent.getVaccinationDate());
                stmt.bindString(13, vaccinationEvent.getVaccinationStatus());
                stmt.bindString(14, vaccinationEvent.getVaccineLotId());
                stmt.execute();
                stmt.clearBindings();
            }

            String sql1 = "INSERT OR REPLACE INTO " + SQLHandler.Tables.VACCINATION_APPOINTMENT + " ( "+
                    SQLHandler.SyncColumns.UPDATED+", "+
                    SQLHandler.VaccinationAppointmentColumns.CHILD_ID+","+
                    SQLHandler.VaccinationAppointmentColumns.ID+","+
                    SQLHandler.VaccinationAppointmentColumns.IS_ACTIVE+","+
                    SQLHandler.VaccinationAppointmentColumns.MODIFIED_BY+","+
                    SQLHandler.VaccinationAppointmentColumns.MODIFIED_ON+","+
                    SQLHandler.VaccinationAppointmentColumns.NOTES+","+
                    SQLHandler.VaccinationAppointmentColumns.OUTREACH+","+
                    SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE+","+
                    SQLHandler.VaccinationAppointmentColumns.SCHEDULED_FACILITY_ID+
                    " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

            SQLiteStatement stmt1 = db1.compileStatement(sql1);

            for (VaccinationAppointment vaccinationAppointment : vaccinationAppointments) {
                stmt1.bindString(1, "1");
                stmt1.bindString(2, vaccinationAppointment.getChildId());
                stmt1.bindString(3, vaccinationAppointment.getId());
                stmt1.bindString(4, vaccinationAppointment.getIsActive());
                stmt1.bindString(5, vaccinationAppointment.getModifiedBy());
                stmt1.bindString(6, vaccinationAppointment.getModifiedOn());
                stmt1.bindString(7, vaccinationAppointment.getNotes());
                stmt1.bindString(8, vaccinationAppointment.getOutreach());
                stmt1.bindString(9, vaccinationAppointment.getScheduledDate());
                stmt1.bindString(10, vaccinationAppointment.getScheduledFacilityId());

                stmt1.execute();
                stmt1.clearBindings();

            }

            db1.setTransactionSuccessful();
            db1.endTransaction();
        } catch (Exception e) {
            db1.endTransaction();
            e.printStackTrace();
        }
    }






    public void parseHealthFacility() {
        Log.d(TAG,"parse health facility started");
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_HEALTH_FACILITIES);
            Log.d(TAG, webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<HealthFacility> objects = new ArrayList<HealthFacility>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<HealthFacility>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (HealthFacility object : objects) {
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.HealthFacilityColumns.ID, object.getId());
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.HealthFacilityColumns.CODE, object.getCode());
                            values.put(SQLHandler.HealthFacilityColumns.TYPE_ID, object.getTypeId());
                            values.put(SQLHandler.HealthFacilityColumns.PARENT_ID, object.getParentId());
                            values.put(SQLHandler.HealthFacilityColumns.NAME, object.getName());
                            DatabaseHandler db = getDatabaseInstance();
                            db.addHealthFacility(values);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"parse health facility finished");
    }

    public void parseStatus() {
        Log.d(TAG,"parse status started");
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_STATUS_LIST);
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {

                    List<Status> objects = new ArrayList<Status>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + responseString);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(responseString, new TypeReference<List<Status>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (Status object : objects) {
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.StatusColumns.ID, object.getId());
                            //Log.d("Status ID", object.getId());
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.StatusColumns.NAME, object.getName());
                            //Log.d("Status NAME", object.getName());
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateStatus(values, object.getId());
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }


        Log.d(TAG,"parse status finished");

    }

    public void parseItemLots() {

        Log.d(TAG,"parse item lots started");
        try {
            String uname,passw;
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                uname = user.getUsername();
                passw = user.getPassword();
            } else {
                uname = LOGGED_IN_USERNAME;
                passw = LOGGED_IN_USER_PASS;
            }

            client.setBasicAuth(uname, passw, true);
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_ITEM_LOT_ID);
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<ItemLot> objects = new ArrayList<ItemLot>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<ItemLot>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (ItemLot object : objects) {
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.ItemLotColumns.ID, object.getId());
                            values.put(SQLHandler.ItemLotColumns.EXPIRE_DATE, object.getExpireDate());
                            values.put(SQLHandler.ItemLotColumns.GTIN, object.getGtin());
                            values.put(SQLHandler.ItemLotColumns.ITEM_ID, object.getItemId());
                            values.put(SQLHandler.ItemLotColumns.LOT_NUMBER, object.getLotNumber());
                            //values.put(SQLHandler.ItemLotColumns.NOTES, object.getNotes());
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateItemLot(values, object.getId());
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }


        Log.d(TAG,"parse itemlots finished");
    }

    public void parseWeight() {
        Log.d(TAG,"parse weight started");
        try {
            String uname, passw;
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                uname = user.getUsername();
                passw = user.getPassword();
            } else {
                uname = LOGGED_IN_USERNAME;
                passw = LOGGED_IN_USER_PASS;
            }

            client.setBasicAuth(uname, passw, true);

            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_WEIGHT_LIST);
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    List<Weight> objects = new ArrayList<Weight>();

                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + responseString);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(responseString, new TypeReference<List<Weight>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (Weight object : objects) {
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.WeightColumns.ID, object.getId());
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.WeightColumns.DAY, object.getDay());
                            values.put(SQLHandler.WeightColumns.GENDER, object.getGender());
                            values.put(SQLHandler.WeightColumns.SD0, object.getSD0());
                            values.put(SQLHandler.WeightColumns.SD1, object.getSD1());
                            values.put(SQLHandler.WeightColumns.SD2, object.getSD2());
                            values.put(SQLHandler.WeightColumns.SD3, object.getSD3());
                            values.put(SQLHandler.WeightColumns.SD4, object.getSD4());
                            values.put(SQLHandler.WeightColumns.SD1NEG, object.getSD1neg());
                            values.put(SQLHandler.WeightColumns.SD2NEG, object.getSD2neg());
                            values.put(SQLHandler.WeightColumns.SD3NEG, object.getSD3neg());
                            values.put(SQLHandler.WeightColumns.SD4NEG, object.getSD4neg());
                            DatabaseHandler db = getDatabaseInstance();
                            db.addUpdateWeightList(values, object.getId());
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"parse weight finished");
    }

    public void parseNonVaccinationReason() {
        Log.d(TAG,"parse non vaccination reason started");
        try {
            String uname, passw;
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                uname = user.getUsername();
                passw = user.getPassword();
            } else {
                uname = LOGGED_IN_USERNAME;
                passw = LOGGED_IN_USER_PASS;
            }

            client.setBasicAuth(uname, passw, true);
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_NON_VACCINATION_REASON_LIST);
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<NonVaccinationReason> objects = new ArrayList<NonVaccinationReason>();

                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<NonVaccinationReason>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (NonVaccinationReason object : objects) {
                            ContentValues values = new ContentValues();
                            values.put(SQLHandler.NonVaccinationReasonColumns.ID, object.getId());
                            values.put(SQLHandler.SyncColumns.UPDATED, 1);
                            values.put(SQLHandler.NonVaccinationReasonColumns.NAME, object.getName());
                            values.put(SQLHandler.NonVaccinationReasonColumns.KEEP_CHILD_DUE, object.getKeepChildDue());
                            DatabaseHandler db = getDatabaseInstance();
                            db.addNonVaccinationReason(values);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG,"parse non vaccinaiton reason finished");
    }

    public void parseAgeDefinitions() {
        Log.d(TAG,"parse agedefinition started");
        try {
            String uname, passw;
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                uname = user.getUsername();
                passw = user.getPassword();
            } else {
                uname = LOGGED_IN_USERNAME;
                passw = LOGGED_IN_USER_PASS;
            }

            client.setBasicAuth(uname, passw, true);
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_AGE_DEFINITIONS_LIST);
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<AgeDefinitions> objects = new ArrayList<AgeDefinitions>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<AgeDefinitions>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (AgeDefinitions object : objects) {
                            ContentValues adCV = new ContentValues();
                            DatabaseHandler db = getDatabaseInstance();

                            adCV.put(SQLHandler.SyncColumns.UPDATED, 1);
                            adCV.put(SQLHandler.AgeDefinitionsColumns.DAYS, object.getDays());
                            adCV.put(SQLHandler.AgeDefinitionsColumns.ID, object.getId());
                            adCV.put(SQLHandler.AgeDefinitionsColumns.NAME, object.getName());
                            db.addAgeDefinitions(adCV);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG,"parse non vaccination reason finished");
    }

    public void parseItem() {
        Log.d(TAG,"parse item started");
        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_ITEM_LIST);
            Log.d("", webServiceUrl.toString());

            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<Item> objects = new ArrayList<Item>();

                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<Item>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (Item object : objects) {
                            ContentValues itemCV = new ContentValues();
                            DatabaseHandler db = getDatabaseInstance();

                            itemCV.put(SQLHandler.SyncColumns.UPDATED, 1);
                            itemCV.put(SQLHandler.ItemColumns.CODE, object.getCode());
                            itemCV.put(SQLHandler.ItemColumns.ENTRY_DATE, object.getEntryDate());
                            itemCV.put(SQLHandler.ItemColumns.EXIT_DATE, object.getExitDate());
                            itemCV.put(SQLHandler.ItemColumns.ID, object.getId());
                            itemCV.put(SQLHandler.ItemColumns.ITEM_CATEGORY_ID, object.getItemCategoryId());
                            itemCV.put(SQLHandler.ItemColumns.NAME, object.getName());
                            db.addItem(itemCV);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"parse item finished");
    }

    public void parseScheduledVaccination() {
        Log.d(TAG,"parse scheduled vaccination started");
        try {
            String uname, passw;
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                uname = user.getUsername();
                passw = user.getPassword();
            } else {
                uname = LOGGED_IN_USERNAME;
                passw = LOGGED_IN_USER_PASS;
            }

            client.setBasicAuth(uname, passw, true);
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_SCHEDULED_VACCINATION_LIST);
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<ScheduledVaccination> objects = new ArrayList<ScheduledVaccination>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        objects = mapper.readValue(response, new TypeReference<List<ScheduledVaccination>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (ScheduledVaccination object : objects) {
                            ContentValues scheduledVaccinationCV = new ContentValues();
                            DatabaseHandler db = getDatabaseInstance();

                            scheduledVaccinationCV.put(SQLHandler.SyncColumns.UPDATED, 1);
                            scheduledVaccinationCV.put(SQLHandler.ScheduledVaccinationColumns.CODE, object.getCode());
                            scheduledVaccinationCV.put(SQLHandler.ScheduledVaccinationColumns.ENTRY_DATE, object.getEntryDate());
                            scheduledVaccinationCV.put(SQLHandler.ScheduledVaccinationColumns.EXIT_DATE, object.getExitDate());
                            scheduledVaccinationCV.put(SQLHandler.ScheduledVaccinationColumns.ID, object.getId());
                            scheduledVaccinationCV.put(SQLHandler.ScheduledVaccinationColumns.ITEM_ID, object.getItemId());
                            scheduledVaccinationCV.put(SQLHandler.ScheduledVaccinationColumns.NAME, object.getName());
                            db.addScheduledVaccination(scheduledVaccinationCV);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"parse scheduled vaccination finished");
    }

    public void parseDose() {

        Log.d(TAG,"parse dose started");
        try {
            String uname, passw;
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                uname = user.getUsername();
                passw = user.getPassword();
            } else {
                uname = LOGGED_IN_USERNAME;
                passw = LOGGED_IN_USER_PASS;
            }

            client.setBasicAuth(uname, passw, true);
            final StringBuilder webServiceUrl = createWebServiceURL(LOGGED_IN_USER_HF_ID, GET_DOSE_LIST);
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<Dose> objects = new ArrayList<Dose>();

                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<Dose>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (Dose object : objects) {
                            ContentValues doseCV = new ContentValues();
                            DatabaseHandler db = getDatabaseInstance();

                            doseCV.put(SQLHandler.SyncColumns.UPDATED, 1);
                            doseCV.put(SQLHandler.DoseColumns.AGE_DEFINITON_ID, object.getAgeDefinitionId());
                            doseCV.put(SQLHandler.DoseColumns.FULLNAME, object.getFullname());
                            doseCV.put(SQLHandler.DoseColumns.DOSE_NUMBER, object.getDoseNumber());
                            doseCV.put(SQLHandler.DoseColumns.ID, object.getId());
                            doseCV.put(SQLHandler.DoseColumns.FROM_AGE_DEFINITON_ID, object.getFromAgeDefId());
                            doseCV.put(SQLHandler.DoseColumns.TO_AGE_DEFINITON_ID, object.getToAgeDefId());
                            doseCV.put(SQLHandler.DoseColumns.SCHEDULED_VACCINATION_ID, object.getScheduledVaccinationId());
                            db.addDose(doseCV);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG,"parse dose finished");
    }

    private int parsedChildResults;
    public int parseChildById(String id) {
        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC).append("GetChildByIdV1?childId=").append(id);

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                parsedChildResults=3;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                Log.d("parseChildCollectorbyId", webServiceUrl.toString());
                ChildCollector childCollector = new ChildCollector();
                Log.d("WhyDHC2", response);
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    childCollector = mapper.readValue(new JSONArray(response).getJSONObject(0).toString(), ChildCollector.class);

                    addChildVaccinationEventVaccinationAppointment(childCollector);
                    parsedChildResults = 1;
                } catch (JsonGenerationException e) {
                    e.printStackTrace();
                    parsedChildResults = 2;
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                    parsedChildResults = 2;
                } catch (IOException e) {
                    e.printStackTrace();
                    parsedChildResults = 3;
                } catch (JSONException e) {
                    e.printStackTrace();
                    parsedChildResults = 2;
                }
            }
        });
        return  parsedChildResults;
    }



    public int parseGCMChildById(String id) {
        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC).append("GetChildByIdV1?childId=").append(id);

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                parsedChildResults=3;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                Log.d("parseChildCollectorbyId", webServiceUrl.toString());
                ChildCollector childCollector = new ChildCollector();
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    childCollector = mapper.readValue(new JSONArray(response).getJSONObject(0).toString(), ChildCollector.class);

                    addChildVaccinationEventVaccinationAppointmentUnOptimisedForSmallAmountsOfData(childCollector);
                    parsedChildResults = 1;
                } catch (JsonGenerationException e) {
                    e.printStackTrace();
                    parsedChildResults = 2;
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                    parsedChildResults = 2;
                } catch (IOException e) {
                    e.printStackTrace();
                    parsedChildResults = 3;
                } catch (JSONException e) {
                    e.printStackTrace();
                    parsedChildResults = 2;
                }
            }
        });
        return  parsedChildResults;
    }

    public void parseGCMChildrenInQueueById() {
        final List<String> childIds = databaseInstance.getChildIdsFromChildUpdatesQueue();
        int size = childIds.size();

        try {
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                client.setBasicAuth(user.getUsername(), user.getPassword(), true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }


            Log.d(TAG, "child updates queue size  = " + size);
            for (int i = 0; i < size; i++) {
                final String childId = childIds.get(i);

                Log.d(TAG, "passing child " + childId);

                //A hacky, more improvements will be done, way to check if there are still unsynchronized data in the postman for that childId, so we should wait till the data synchronizes before updateing data from the server
                if(!getDatabaseInstance().checkIfChildIdIsInPostman(childId)){
                    final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC).append("GetChildByIdV1?childId=").append(childId);
                    client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            parsedChildResults = 3;
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String response) {
                            Log.d(TAG, "parseChildCollectorbyId = " + webServiceUrl.toString());
                            ChildCollector childCollector = new ChildCollector();
                            try {
                                Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                                ObjectMapper mapper = new ObjectMapper();
                                mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                                childCollector = mapper.readValue(new JSONArray(response).getJSONObject(0).toString(), ChildCollector.class);

                                //Check if there any unsynchronized updates in the postman table for this barcode
                                if(!getDatabaseInstance().checkIfChildBarcodeIsInPostman(childCollector.getChildEntity().getBarcodeID())){
                                    Log.d(TAG,"not postponing child with barcode "+childCollector.getChildEntity().getBarcodeID());
                                    addChildVaccinationEventVaccinationAppointmentUnOptimisedForSmallAmountsOfData(childCollector);

                                    parsedChildResults = 1;
                                    databaseInstance.removeChildFromChildUpdateQueue(childId);
                                }else{
                                    Log.d(TAG,"postponing updates for a barcode in postman until the details are synchronized to the server");
                                }
                            } catch (JsonGenerationException e) {
                                e.printStackTrace();
                                parsedChildResults = 2;
                            } catch (JsonMappingException e) {
                                e.printStackTrace();
                                parsedChildResults = 2;
                            } catch (IOException e) {
                                e.printStackTrace();
                                parsedChildResults = 3;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                parsedChildResults = 2;
                            }
                        }
                    });
                }else{
                    Log.d(TAG,"postponing updates for a childID in postman until the details are synchronized to the server");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void parseCustomHealthFacility(String hf_id) {
        final StringBuilder webServiceUrl = createWebServiceURL(hf_id, GET_HEALTH_FACILITY);
        Log.d("", webServiceUrl.toString());
        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                List<HealthFacility> objects = new ArrayList<HealthFacility>();
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    objects = mapper.readValue(response, new TypeReference<List<HealthFacility>>() {
                    });
                } catch (JsonGenerationException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    for (HealthFacility object : objects) {
                        ContentValues values = new ContentValues();
                        values.put(SQLHandler.HealthFacilityColumns.ID, object.getId());
                        values.put(SQLHandler.SyncColumns.UPDATED, 1);
                        values.put(SQLHandler.HealthFacilityColumns.CODE, object.getCode());
                        values.put(SQLHandler.HealthFacilityColumns.PARENT_ID, object.getParentId());
                        values.put(SQLHandler.HealthFacilityColumns.NAME, object.getName());
                        DatabaseHandler db = getDatabaseInstance();
                        db.addUpdateHealthFacility(values, object.getId());
                    }
                }
            }
        });

    }

    public void parsePlaceById(String placeId) {
        final StringBuilder webServiceUrl = createWebServiceURL(placeId, GET_PLACE_BY_ID);
        Log.d("", webServiceUrl.toString());
        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                Place place = null;
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    place = mapper.readValue(response, Place.class);
                } catch (JsonGenerationException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (place != null) {
                        ContentValues values = new ContentValues();
                        //Log.d("Place ID", object.getId());
                        values.put(SQLHandler.PlaceColumns.ID, place.getId());
                        values.put(SQLHandler.SyncColumns.UPDATED, 1);
                        values.put(SQLHandler.PlaceColumns.NAME, place.getName());
                        //Log.d("Place NAME", object.getName());
                        values.put(SQLHandler.PlaceColumns.CODE, place.getCode());
                        DatabaseHandler db = getDatabaseInstance();
                        db.addPlacesThatWereNotInDB(values, place.getId());
                    }
                }
            }
        });
    }


    //needs to be merged with createWebServiceLoginURL and used with usr/pass as null in case not Login, hf as null in case of Login
    public StringBuilder createWebServiceURL(String rec_id, String service) {
        StringBuilder webServiceURL;

        switch (service) {
            case GET_FACILITY_COLD_CHAIN:
                webServiceURL = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC)
                        .append("GetHealthFacilityColdChain?healthFacilityId=").append(rec_id)
                        .append("&reportingMonth=").append(10)
                        .append("&reportingYear=").append(2016);
                break;
            case GET_PLACE:
                webServiceURL = new StringBuilder(WCF_URL).append(PLACE_MANAGEMENT_SVC).append(PLACE_MANAGEMENT_SVC_GETTER).append(rec_id);
                break;
            case GET_PLACE_LIST_ID:
                webServiceURL = new StringBuilder(WCF_URL).append(PLACE_MANAGEMENT_SVC).append(GET_PLACES_BY_LIST);
                break;
            case GET_HEALTH_FACILITY_LIST_ID:
                webServiceURL = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC).append(HEALTH_FACILITY_SVC_GETTER_BY_LIST);
                break;
            case GET_PLACE_BY_ID:
                webServiceURL = new StringBuilder(WCF_URL).append(PLACE_MANAGEMENT_SVC).append(PLACE_MANAGEMENT_SVC_GETTER_BY_ID).append(rec_id);
                break;
            case GET_CHILD:
                webServiceURL = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC).append(CHILD_MANAGEMENT_SVC_GETTER).append(rec_id);
                break;
            case GET_DOSE_LIST:
                webServiceURL = new StringBuilder(WCF_URL).append(DOSE_MANAGEMENT_SVC).append(DOSE_MANAGEMENT_SVC_GETTER);
                break;
            case GET_STATUS_LIST:
                webServiceURL = new StringBuilder(WCF_URL).append(STATUS_MANAGEMENT_SVC).append(STATUS_MANAGEMENT_SVC_GETTER);
                break;
            case GET_AGE_DEFINITIONS_LIST:
                webServiceURL = new StringBuilder(WCF_URL).append(AGE_DEFINITION_MANAGEMENT_SVC).append(AGE_DEFINITION_MANAGEMENT_SVC_GETTER);
                break;
            case GET_WEIGHT_LIST:
                webServiceURL = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC).append(WEIGHT_MANAGEMENT_SVC_GETTER);
                break;
            case GET_NON_VACCINATION_REASON_LIST:
                webServiceURL = new StringBuilder(WCF_URL).append(NON_VACCINATION_REASON_MANAGEMENT_SVC).append(NON_VACCINATION_REASON_MANAGEMENT_SVC_GETTER);
                break;
            case GET_ITEM_LIST:
                webServiceURL = new StringBuilder(WCF_URL).append(ITEM_MANAGEMENT_SVC).append(ITEM_MANAGEMENT_SVC_GETTER);
                break;
            case GET_SCHEDULED_VACCINATION_LIST:
                webServiceURL = new StringBuilder(WCF_URL).append(SCHEDULED_VACCINATION_MANAGEMENT_SVC).append(SCHEDULED_VACCINATION_MANAGEMENT_SVC_GETTER);
                break;
            case SEARCH_BY_BARCODE:
                webServiceURL = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC).append(SEARCH_BY_BARCODE);
                break;
            case REGISTER_AUDIT:
                webServiceURL = new StringBuilder(WCF_URL).append(AUDIT_MANAGEMENT_SVC).append(REGISTER_AUDIT);
                break;
            case UPDATE_VACCINATION_QUEUE:
                webServiceURL = new StringBuilder(WCF_URL).append(VACCINATION_EVENT_SVC).append(UPDATE_VACCINATION_QUEUE);
                break;
            case GET_HEALTH_FACILITY:
                webServiceURL = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC).append(HEALTH_FACILITY_SVC_GETTER).append(rec_id);
                break;
            case GET_HEALTH_FACILITIES:
                webServiceURL = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC).append(HEALTH_FACILITIES_SVC_GETTER);
                break;
            case GET_VACCINATION_QUEUE_BY_DATE_AND_USER:
                webServiceURL = new StringBuilder(WCF_URL).append(VACCINATION_QUEUE_MANAGEMENT_SVC).append(GET_VACCINATION_QUEUE_BY_DATE_AND_USER);
                break;
            case GET_ITEM_LOT_ID:
                webServiceURL = new StringBuilder(WCF_URL).append(STOCK_MANAGEMENT_SVC).append(ITEM_LOT_MANAGEMENT_SVC_GETTER);
                break;
            case GET_STOCK:
                webServiceURL = new StringBuilder(WCF_URL).append(STOCK_MANAGEMENT_SVC).append(STOCK_MANAGEMENT_SVC_GETTER).append(rec_id);
                break;
            default:
                webServiceURL = new StringBuilder(URL_BUILDER_ERROR);
                break;
        }

        return webServiceURL;
    }


    /**
     * @param lastname
     * @param bDate
     * @param gender
     * @return
     * @Arinela
     */
    private boolean isChildInServer = false;

    public boolean checkChildInServer(String lastname,String motherFirstname, String motherLastname,String bDate, String gender) {
        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC);
        webServiceUrl
                .append("ChildExistsByMotherAndBirthdateAndGender?lastname1=")
                .append(URLEncoder.encode(lastname))
                .append("&motherFirstname=")
                .append(URLEncoder.encode(motherFirstname))
                .append("&motherLastname=")
                .append(URLEncoder.encode(motherLastname))
                .append("&birthdate=")
                .append(bDate).append("&gender=").append(gender);

        Log.e("service weight", webServiceUrl + "");

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                isChildInServer = false;
                throwable.printStackTrace();
                getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String result) {
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + result);
                    if (result.equals("true"))
                        isChildInServer = true;
                    else
                        isChildInServer = false;
                } catch (Exception e) {
                    getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
                    isChildInServer = false;
                }
            }
        });

        return isChildInServer;

    }

    private int updatingVaccineOnTheServerResult = -1;
    public int updateVaccinationEventOnServer(final String url) {
        Log.e("Updating vaccin", url);
        Log.d("day4", "Vaccination Update URL : " + url);
        getDatabaseInstance().addPost(url, 1);
        return updatingVaccineOnTheServerResult;
    }


    public void broadcastChildUpdates(int childId) {
        final StringBuilder webServiceUrl;
        Log.i(TAG,"broadcasting child updates for childID = "+childId);

        webServiceUrl = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC);
        webServiceUrl.append("BroadcastChildUpdates?childId=").append(childId);

        getDatabaseInstance().addPost(webServiceUrl.toString(), 3);
    }

    public void broadcastChildUpdatesWithBarcodeId(String barcodeId) {
        final StringBuilder webServiceUrl;
        Log.i(TAG,"broadcasting child updates for childTempID = "+barcodeId);

        webServiceUrl = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC);
        webServiceUrl.append("BroadcastChildUpdatesWithBarcodeId?barcodeId=").append(barcodeId);

        getDatabaseInstance().addPost(webServiceUrl.toString(), 3);
    }


    /**
     * @return child ID
     * @Coze
     */
    private int childId;
    public int registerChildWithAppoitments(String barcode, String fristname, String lastname, String bDate, String gender, String hfid, String birthPlaceId, String domId,
                                            String addr, String phone, String motherFirstname, String motherLastname, String notes, String userID, String modOn,
                                            PostmanModel postmanModel, String firstname2,final String threadTempId, final String threadbarcode, String motherVVUStatus, String motherTT2Status, String childCummulativeSn, String childRegistryYear,int catchment) {
        childId = -1;
        final StringBuilder webServiceUrl;
        if (postmanModel == null) {
            webServiceUrl = new StringBuilder(WCF_URL).append(CHILD_MANAGEMENT_SVC);
            webServiceUrl.append("RegisterChildWithAppoitmentsWithMothersHivStatusAndTT2VaccineStatusAndCatchment?barcodeid=").append(barcode).append("&firstname1=")
                    .append(URLEncoder.encode(fristname)).append("&lastname1=").append(URLEncoder.encode(lastname))
                    .append("&birthdate=").append(bDate).append("&gender=").append(gender)
                    .append("&healthFacilityId=").append(hfid).append("&birthplaceId=").append(birthPlaceId).append("&domicileId=")
                    .append(domId).append("&address=").append(URLEncoder.encode(addr))
                    .append("&phone=").append(URLEncoder.encode(phone))
                    .append("&motherFirstname=").append(URLEncoder.encode(motherFirstname))
                    .append("&motherLastname=").append(URLEncoder.encode(motherLastname))
                    .append("&mothersHivStatus=").append(URLEncoder.encode(motherVVUStatus))
                    .append("&mothersTT2Status=").append(URLEncoder.encode(motherTT2Status))
                    .append("&notes=").append(URLEncoder.encode(notes))
                    .append("&userId=").append(userID).append("&modifiedOn=").append(modOn)
                    .append("&firstname2=").append((firstname2 != null) ? firstname2 : "")
                    .append("&childCumulativeSn=").append(childCummulativeSn)
                    .append("&childRegistryYear=").append(childRegistryYear)
                    .append("&catchment=").append(catchment);

        } else {
            webServiceUrl = new StringBuilder(postmanModel.getUrl());
        }

        getDatabaseInstance().addPost(webServiceUrl.toString(), 3);
        return childId;
    }



    /**
     * this method expects the childBarcode value and one of doseId(not needed a precise one)
     * // @param childBarcode
     * // @param doseId
     *
     */
    private int updatingVaccinationAppOutreachResult=-1;

    public int updateVaccinationAppOutreach(String childBarcode, String doseId) {
        final StringBuilder webServiceUrl;
        webServiceUrl = new StringBuilder(WCF_URL).append("VaccinationAppointmentManagement.svc/UpdateVaccinationApp?outreach=true&userId=")
                .append(LOGGED_IN_USER_ID)
                .append("&barcode=").append(childBarcode)
                .append("&doseId=").append(doseId);
        getDatabaseInstance().addPost(webServiceUrl.toString(), 1);

        return updatingVaccinationAppOutreachResult;

    }

    /**
     * Parsing data from Server after the First login information parser on home activity
     */
    public void continuousModificationParser() {
        if (!USERNAME.equalsIgnoreCase("default")) {
            try {

                String username, password;
                if (LOGGED_IN_USERNAME == null) {
                    Log.d(TAG,"username null");
                    List<User> allUsers = databaseInstance.getAllUsers();
                    User user = allUsers.get(0);
                    username = user.getUsername();
                    password = user.getPassword();
                } else {
                    username = LOGGED_IN_USERNAME;
                    password = LOGGED_IN_USER_PASS;
                }


                String url = WCF_URL + "ChildManagement.svc/GetChildrenByHealthFacilityBeforeLastLoginV1?idUser=" + getLOGGED_IN_USER_ID();
                Log.d("secondLoginURL", url);
                client.setBasicAuth(username, password, true);
                client.get(url, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String response) {
                        ChildCollector2 objects2 = new ChildCollector2();
                        try {
                            Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                            ObjectMapper mapper = new ObjectMapper();
                            objects2 = mapper.readValue(response, ChildCollector2.class);

                        } catch (JsonGenerationException e) {
                            e.printStackTrace();
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            addChildVaccinationEventVaccinationAppointment(objects2);
                        }
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    /**
     * Parsing data from Server after the First login information parser on home activity
     */
    public void firstLoginOfTheDay() {
        if (!USERNAME.equalsIgnoreCase("default")) {

            String url = WCF_URL + "ChildManagement.svc/GetChildrenByHealthFacilityDayFirstLoginV1?idUser=" + getLOGGED_IN_USER_ID();
            Log.d("secondLoginURL", url);

            client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            RequestHandle message = client.get(url, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    ChildCollector2 objects2 = new ChildCollector2();

                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        objects2 = mapper.readValue(response, new TypeReference<List<ChildCollector>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        addChildVaccinationEventVaccinationAppointment(objects2);
                    }
                }
            });
        }

    }

//    /**
//     * Parsing data from Server after the First login in intervals
//     */
//    public void intervalGetChildrenByHealthFacilitySinceLastLogin() {
//
//        String url = WCF_URL + "ChildManagement.svc/GetChildrenByHealthFacilitySinceLastLogin?idUser=" + getLOGGED_IN_USER_ID();
//        Log.e("SinceLastLogin", "GetChildrenByHealthFacilitySinceLastLogin url is: " + url);
//        try {
//            client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
//            client.get(url, new TextHttpResponseHandler() {
//                @Override
//                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    throwable.printStackTrace();
//                }
//
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, String response) {
//                    Log.d(TAG, "received child since last login  = " + response);
//                    ChildCollector2 objects2 = new ChildCollector2();
//                    try {
//                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
//                        ObjectMapper mapper = new ObjectMapper();
//                        objects2 = mapper.readValue(response, new TypeReference<List<ChildCollector2>>() {
//                        });
//
//                    } catch (JsonGenerationException e) {
//                        e.printStackTrace();
//                    } catch (JsonMappingException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        addChildVaccinationEventVaccinationAppointment(objects2);
//                    }
//
//                }
//            });
//        }catch (Exception e){
//            RoutineAlarmReceiver.cancelAlarm(getApplicationContext());
//        }
//
//
//    }


    /**
     * Parsing data from Server after the First login in intervals
     */
    public void intervalGetChildrenByHealthFacilitySinceLastLogin() {

        String url = WCF_URL + "ChildManagement.svc/GetChildrenByHealthFacilitySinceLastLoginV1?idUser=" + getLOGGED_IN_USER_ID();
        Log.e("SinceLastLogin", "GetChildrenByHealthFacilitySinceLastLogin url is: " + url);

        ChildCollector2 objects2 = new ChildCollector2();

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + url.toString());
            httpGet.setHeader("Authorization", "Basic " + Base64.encodeToString((LOGGED_IN_USERNAME + ":" + LOGGED_IN_USER_PASS).getBytes(), Base64.NO_WRAP));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            InputStream inputStream = httpResponse.getEntity().getContent();
            String response = Utils.getStringFromInputStream(inputStream);
            Log.e("SinceLastLogin", "responce  is: " + response);
            Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
            ObjectMapper mapper = new ObjectMapper();
            objects2 = mapper.readValue(response, new TypeReference<List<ChildCollector>>() {
            });
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d("coze", "before saving to the database");
            if (addChildVaccinationEventVaccinationAppointment(objects2)) {
                Log.d("coze","about to re login");
                loginRequest();
                objects2 = null; // clearing references so that it can be identified as GC material more easilly;
            }
        }


    }


    /**
     * Parsing data from Server getChildByBarcodeList to get children that we dont have but are found in the vacc queue of server
     */
    public void getChildByBarcodeList(String childIds) {

        String url = WCF_URL + "Childmanagement.svc/GetChildByBarcodeListV1?childList=" + childIds;
        Log.d("getChildByBarcodeList", url);

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        client.get(url.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                List<ChildCollector> objects2 = new ArrayList<ChildCollector>();

                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                    ObjectMapper mapper = new ObjectMapper();
                    objects2 = mapper.readValue(response, new TypeReference<List<ChildCollector>>() {
                    });

                } catch (JsonGenerationException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    for (ChildCollector object : objects2) {
                        addChildVaccinationEventVaccinationAppointment(object);
                    }
                }
            }
        });


    }
//
//    /**
//     * Parsing data from Server GetChildByIdListSince to get children or update
//     * before using this method we need to check if there is a logged in user in the appv2
//     */
//    public void getGetChildByIdListSince() {
//        String childIds = getDatabaseInstance().getChildrenFromOtherHFIDThanLoggedUser(getLOGGED_IN_USER_HF_ID());
//        if (childIds == null) return;
//        String url = WCF_URL + "ChildManagement.svc/GetChildByIdListSince?childIdList=" + childIds + "&userId=" + getLOGGED_IN_USER_ID();
//        Log.d("getChildByBarcodeList", url);
//
//        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
//        RequestHandle message = client.get(url.toString(), new TextHttpResponseHandler() {
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                throwable.printStackTrace();
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                ChildCollector2 objects2 = new ChildCollector2();
//
//                try {
//                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + responseString);
//                    ObjectMapper mapper = new ObjectMapper();
//                    objects2 = mapper.readValue(responseString, new TypeReference<List<ChildCollector>>() {
//                    });
//
//                } catch (JsonGenerationException e) {
//                    e.printStackTrace();
//                } catch (JsonMappingException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    addChildVaccinationEventVaccinationAppointment(objects2);
//                }
//
//            }
//        });
//
//    }

    /**
     * Parsing data from Server GetChildByIdListSince to get children or update
     * before using this method we need to check if there is a logged in user in the appv2
     */
    public void getGetChildByIdListSince() {
        String childIds = getDatabaseInstance().getChildrenFromOtherHFIDThanLoggedUser(getLOGGED_IN_USER_HF_ID());
        if (childIds == null){
            return;
        }else {
            getChildByBarcodeList(childIds);
        }

//        String url = WCF_URL + "ChildManagement.svc/GetChildByIdListSince?childIdList=" + childIds + "&userId=" + getLOGGED_IN_USER_ID();
//        Log.d("getChildByBarcodeList", url);
//
//        ChildCollector2 objects2 = new ChildCollector2();
//
//        try {
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet(url);
//            Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + url.toString());
//            httpGet.setHeader("Authorization", "Basic " + Base64.encodeToString((LOGGED_IN_USERNAME + ":" + LOGGED_IN_USER_PASS).getBytes(), Base64.NO_WRAP));
//            HttpResponse httpResponse = httpClient.execute(httpGet);
//            InputStream inputStream = httpResponse.getEntity().getContent();
//            String response = Utils.getStringFromInputStream(inputStream);
//            Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
//            ObjectMapper mapper = new ObjectMapper();
//            objects2 = mapper.readValue(response, new TypeReference<List<ChildCollector>>() {
//            });
//
//        } catch (JsonGenerationException e) {
//            e.printStackTrace();
//        } catch (JsonMappingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            addChildVaccinationEventVaccinationAppointment(objects2);
//        }
//

    }

    /**
     * Parsing data from Server GetChildByIdList to get children or update
     * before using this method we need to check if there is a logged in user in the appv2
     */
    public void getGetChildByIdList() {
        String childIds = getDatabaseInstance().getChildrenFromOtherHFIDThanLoggedUser(getLOGGED_IN_USER_HF_ID());
        if (childIds == null) return;

        String query="";
        try {
            query = URLEncoder.encode(childIds, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = WCF_URL + "ChildManagement.svc/GetChildByIdListV1?childIdList=" + query + "&userId=" + getLOGGED_IN_USER_ID();
        Log.d("getChildByIdList", url);


        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        RequestHandle message = client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                ChildCollector2 objects2 = new ChildCollector2();

                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                    ObjectMapper mapper = new ObjectMapper();
                    objects2 = mapper.readValue(response, new TypeReference<List<ChildCollector>>() {
                    });

                } catch (JsonGenerationException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    addChildVaccinationEventVaccinationAppointment(objects2);
                }
            }
        });




    }


    //method for AdminVacc
    public void setUpdateURL(AdministerVaccinesModel a, String strNotes, String strBarcode) {
        String dateTodayTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").format(Calendar.getInstance().getTime());
        try {
            dateTodayTimestamp = URLEncoder.encode(dateTodayTimestamp, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatted = new SimpleDateFormat("yyyy-MM-dd");
        String vnotes = "";
        if (!(strNotes.equalsIgnoreCase(""))) {
            vnotes = strNotes;
        }
        final StringBuilder VaccinationEventUpdateURL = new StringBuilder(WCF_URL + "VaccinationEvent.svc/UpdateVaccinationEventBarcodeAndDoseId?")
                .append("barcode=").append(URLEncoder.encode(strBarcode))
                .append("&doseId=").append(URLEncoder.encode(a.getDose_id()))
                .append("&vaccineLotId=").append(URLEncoder.encode(a.getVaccination_lot()))
                .append("&healthFacilityId=").append(URLEncoder.encode(getLOGGED_IN_USER_HF_ID()))
                .append("&vaccinationDate=").append(URLEncoder.encode(formatted.format(a.getTime2())))
                .append("&notes=").append(URLEncoder.encode(vnotes))
                .append("&vaccinationStatus=").append(a.getStatus())
                .append("&nonvaccinationReasonId=").append(a.getNon_vac_reason())
                .append("&userId=").append(this.getLOGGED_IN_USER_ID())
                .append("&modifiedOn=").append(dateTodayTimestamp);
        //.append("&vaccinationEventId=").append(vacc_ev_id);

        a.setUpdateURL(VaccinationEventUpdateURL.toString());
    }

    public void setAppointmentUpdateURL(AdministerVaccinesModel a, String appointment_id, CheckBox cbOutreach) {
        SimpleDateFormat formatted = new SimpleDateFormat("yyyy-MM-dd");

        final StringBuilder VaccinationAppointmentUpdateURL = new StringBuilder(WCF_URL + "VaccinationAppointmentManagement.svc/UpdateVaccinationApp?")
                .append("outreach=").append(String.valueOf(cbOutreach.isChecked()))
                .append("&userId=").append(getLOGGED_IN_USER_ID())
                .append("&vaccinationAppointmentId=").append(appointment_id);

        a.setUpdateURLAppointment(VaccinationAppointmentUpdateURL.toString());

    }

    private  boolean isHealthFacilityBalanceSaved;
    public boolean saveHealthFacilityBalance(String gtin, String lotno, String qty, String date, String userId) {
        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(STOCK_MANAGEMENT_SVC);
        webServiceUrl.append("StockCount?gtin=").append(gtin).append("&lotno=").append(lotno).append("&qty=").append(qty)
                .append("&date=").append(date).append("&userId=").append(userId);

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                throwable.printStackTrace();
                getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
                isHealthFacilityBalanceSaved = false;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String result) {
                Log.e(" save health faci", webServiceUrl + "");
                try {
                    Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + result);
                    isHealthFacilityBalanceSaved = true;

                } catch (Exception e) {
                    getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
                    isHealthFacilityBalanceSaved = false;
                }
            }
        });
        return isHealthFacilityBalanceSaved;

    }

    public void parseStockAdjustmentReasons() {
        Log.d(TAG,"parse stock adjustment reasons started");
        try {
            String username, password;
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                username = user.getUsername();
                password = user.getPassword();
                client.setBasicAuth(username, password, true);
            } else {
                username = LOGGED_IN_USERNAME;
                password = LOGGED_IN_USER_PASS;
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(STOCK_MANAGEMENT_SVC).append(GET_STOCK_ADJUSTMENT);
            Log.d("", webServiceUrl.toString());
            RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    List<AdjustmentReasons> objects = new ArrayList<AdjustmentReasons>();
                    try {
                        Utils.writeNetworkLogFileOnSD(Utils.returnDeviceIdAndTimestamp(getApplicationContext()) + response);
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        objects = mapper.readValue(response, new TypeReference<List<AdjustmentReasons>>() {
                        });

                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        for (AdjustmentReasons object : objects) {
                            ContentValues adCV = new ContentValues();
                            DatabaseHandler db = getDatabaseInstance();

                            adCV.put(SQLHandler.AdjustmentColumns.NAME, object.getName());
                            adCV.put(SQLHandler.AdjustmentColumns.ID, object.getId());
                            adCV.put(SQLHandler.AdjustmentColumns.POSITIVE, object.getPozitive());
                            adCV.put(SQLHandler.AdjustmentColumns.IS_ACTIVE, object.getIsActive());
                            db.addStockAdjustment(adCV);
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG,"parse stock adjustment reasons finished");
    }


    private boolean isStockAdjustmentReasonSaved=false;
    public boolean saveStockAdjustmentReasons(String gtin, String lotno, String qty, String date, String reasonId, String userId) {
        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(STOCK_MANAGEMENT_SVC);
        webServiceUrl.append("StockAdjustment?gtin=").append(gtin).append("&lotno=").append(lotno).append("&qty=").append(qty)
                .append("&date=").append(date).append("&reasonId=").append(reasonId).
                append("&userId=").append(userId);

        Log.e(" save health faci", webServiceUrl + "");

        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
        RequestHandle message = client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
                isStockAdjustmentReasonSaved= false;
                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    isStockAdjustmentReasonSaved = true;

                } catch (Exception e) {
                    getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
                    isStockAdjustmentReasonSaved = false;
                }
            }
        });

        return isStockAdjustmentReasonSaved;

    }


    private String data = "";
    public String getHealthFacilityCumulativeChildRegistrationNumber() {
        String url = WCF_URL + GET_HEALTH_FACILITY_CHILD_CUMULATIVE_REGISTRATION_NUMBER +"?healthFacilityId="+ getLOGGED_IN_USER_HF_ID();
        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);

        Log.e(TAG,"getHealthFacitliyCumulativeChildRegistrationNumber = " + url);

        RequestHandle message = client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                throwable.printStackTrace();
                data = "Error fetching the cumulative value. Please check if you have an active internet connection";
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                data = response;
            }
        });
        return data;
    }


    private int resp;
    public int  updateHealthFacilityCumulativeChildRegistrationNumber(int no) {
        String url = WCF_URL + UPDATE_HEALTH_FACILITY_CHILD_CUMULATIVE_REGISTRATION_NUMBER +"?healthFacilityId="+ getLOGGED_IN_USER_HF_ID()+"&cumulativeChildSn="+no;
        client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);

        RequestHandle message = client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                throwable.printStackTrace();
                resp = -999;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                resp = 1;
            }
        });
        return resp;
    }


    private class UsePoolThreadResponseHandler extends AsyncHttpResponseHandler {

        public UsePoolThreadResponseHandler() {
            super();

            // We wish to use the same pool thread to run the response.
            setUsePoolThread(true);
        }

        @Override
        public void onSuccess(final int statusCode, final Header[] headers, final byte[] responseBody) {
            Log.d(TAG,"receiving data in streams");
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory jsonFactory = mapper.getJsonFactory();
            try {
                JsonParser jp = jsonFactory.createJsonParser(responseBody);
                JsonToken token;
                token = jp.nextToken();

                long tStart = System.currentTimeMillis();
                Log.e("TIMING LOG", "Parsing start ");
                while ((token = jp.nextToken()) != null) {
                    switch (token) {
                        case START_OBJECT:
                            JsonNode node = jp.readValueAsTree();
                            ChildCollector obj = mapper.treeToValue(node, ChildCollector.class);
                            addChildVaccinationEventVaccinationAppointment(obj);
                            break;
                    }
                }
                Log.e("TIMING LOG", "elapsed total time (milliseconds): " + (System.currentTimeMillis() - tStart));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(final int statusCode, final Header[] headers, final byte[] responseBody, final Throwable error) {
            // This callback is now running within the pool thread execution
            // scope and not within Android's UI thread, so if we must update
            // the UI, we'll have to dispatch a runnable to the UI thread.
            Log.d(TAG, "Error = "+statusCode);
            error.printStackTrace();

        }


    }

    private class UsePoolThreadResponseHandler2 extends AsyncHttpResponseHandler {

        public UsePoolThreadResponseHandler2() {
            super();

            // We wish to use the same pool thread to run the response.
            setUsePoolThread(true);
        }

        @Override
        public void onSuccess(final int statusCode, final Header[] headers, final byte[] responseBody) {
            Log.d(TAG,"receiving data in streams");
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory jsonFactory = mapper.getJsonFactory();
            try {

                long tStart = System.currentTimeMillis();
                Log.e("TIMING LOG", "Parsing start ");
                ChildCollector2 childCollector2 = new ChildCollector2();
                mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                childCollector2 = mapper.readValue(responseBody, ChildCollector2.class);
                addChildVaccinationEventVaccinationAppointment(childCollector2);

                Log.e("TIMING LOG", "elapsed total time (milliseconds): " + (System.currentTimeMillis() - tStart));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(final int statusCode, final Header[] headers, final byte[] responseBody, final Throwable error) {
            // This callback is now running within the pool thread execution
            // scope and not within Android's UI thread, so if we must update
            // the UI, we'll have to dispatch a runnable to the UI thread.
            Log.d(TAG, "Error = "+statusCode);
            error.printStackTrace();

        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    public void parseStockDistributions() {
        Log.d(TAG,"parsing stock distributions");
        try {
            String username, password,hfid;
            if (LOGGED_IN_USERNAME == null || LOGGED_IN_USER_PASS == null) {
                List<User> allUsers = databaseInstance.getAllUsers();
                User user = allUsers.get(0);

                username = user.getUsername();
                password = user.getPassword();
                hfid = user.getHealthFacilityId();
                client.setBasicAuth(username, password, true);
            } else {
                client.setBasicAuth(LOGGED_IN_USERNAME, LOGGED_IN_USER_PASS, true);
                hfid = LOGGED_IN_USER_HF_ID;
            }

            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC).append(GET_STOCK_DISTRIBUTIONS);
            Log.d(TAG,"parsing stock distributions url = "+webServiceUrl);
            Log.d(TAG,"parsing stock distributions Health Facility Id = "+hfid);

            RequestParams params = new RequestParams();
            params.add("healthFacilityId",hfid);


            RequestHandle message = client.get(webServiceUrl.toString(), params,new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG,"parsing stock distributions failure = "+responseString);
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    Log.d(TAG,"parsing stock distributions success = "+response);
                    JSONArray arr = null;
                    try {
                        arr = new JSONArray(response);
                        int count = arr.length();
                        for (int i=0;i<count;i++) {
                            JSONObject o = arr.getJSONObject(i);

                            try {
                                ContentValues adCV = new ContentValues();
                                DatabaseHandler db = getDatabaseInstance();

                                adCV.put(SQLHandler.StockDistributionsValuesColumns.STOCK_DISTRIBUTION_ID, o.getInt("StockDistributionId"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.FROM_HEALTH_FACILITY_ID, o.getInt("FromHealthFacilityId"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.TO_HEALTH_FACILITY_ID, o.getInt("ToHealthFacilityId"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.PROGRAM_ID, o.getInt("ProgramId"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.PRODUCT_ID, o.getInt("ProductId"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.QUANTITY, o.getString("Quantity"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.ITEM_ID, o.getInt("ItemId"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.LOT_ID, o.getInt("LotId"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.VIMS_LOT_ID, o.getInt("VimsLotId"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.VVM_STATUS, o.getString("VvmStatus"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.STATUS, o.getString("Status"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.DISTRIBUTION_TYPE, o.getString("DistributionType"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.DISTRIBUTION_DATE, o.getString("DistributionDate"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.UNIT_OF_MEASURE, o.getString("BaseUom"));
                                adCV.put(SQLHandler.StockDistributionsValuesColumns.DOSES_PER_DISPENSING_UNIT, o.getInt("DosesPerDispensingUnit"));

                                if (db.isStockDistributionInDb(o.getInt("StockDistributionId"))) {
                                    db.getWritableDatabase().update(SQLHandler.Tables.STOCK_DISTRIBUTIONS, adCV,
                                            SQLHandler.StockDistributionsValuesColumns.STOCK_DISTRIBUTION_ID + "= " + o.getInt("StockDistributionId"), null);
                                } else {
                                    db.getWritableDatabase().insert(SQLHandler.Tables.STOCK_DISTRIBUTIONS, null, adCV);
                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void updateStockDistribution(int fromHealthFacilityId, int toHealthFacilityId, int productId, int lotId, int itemId, String distributionType , String distributionDate,int quantity,String status,int StockDistributionId) {
        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);

        String userId;
        String username, password;
        if (LOGGED_IN_USER_ID == null) {
            Log.d(TAG,"userid null");
            List<User> allUsers = databaseInstance.getAllUsers();
            User user = allUsers.get(0);
            userId = user.getId();
            username = user.getUsername();
            password = user.getPassword();

        } else {
            userId = LOGGED_IN_USER_ID;
            username = LOGGED_IN_USERNAME;
            password = LOGGED_IN_USER_PASS;
        }


        webServiceUrl.append("updateHeathFacilityStockDistributions?fromHealthFacilityId=").append(fromHealthFacilityId)
                .append("&toHealthFacilityId=").append(toHealthFacilityId)
                .append("&productId=").append(productId)
                .append("&lotId=").append(lotId)
                .append("&itemId=").append(itemId)
                .append("&distributionType=").append(distributionType)
                .append("&distributionDate=").append(distributionDate)
                .append("&quantity=").append(quantity)
                .append("&status=").append(status)
                .append("&userId=").append(userId)
                .append("&StockDistributionId=").append(StockDistributionId);


        try {
            client.setBasicAuth(username,password,true);
            RequestHandle message = client.get(webServiceUrl.toString(),new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    if(statusCode!=200){
                        getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            getDatabaseInstance().addPost(webServiceUrl.toString(), 1);
        }
    }


    public List<SessionsModel> GetHealthFacilitySessionUpdateUrl(){

        Log.d(TAG,"getting facility sessions");
        List<SessionsModel> modelList = databaseInstance.getHealthFacilitySessions();

        Log.d(TAG,"facility sessions count = "+modelList.size());

        for (SessionsModel model:modelList){
            final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(model.getLOGING_TIME());

            Date date = c.getTime();

            try {
                webServiceUrl.append("StoreHealthFacilityLoginSessions?userId=").append(model.getUSER_ID())
                        .append("&healthFacilityId=").append(model.getHEALTH_FACILITY_ID())
                        .append("&loginTime=").append(URLEncoder.encode(new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").format(date), "utf-8"))
                        .append("&sessionLength=").append(model.getSESSION_LENGTH()/1000);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Log.d(TAG,"model url = "+webServiceUrl);

            model.setUrl(webServiceUrl.toString());
        }


        return modelList;
    }


    public void CheckNewStockDistributionsFromVims(){
        Log.d(TAG,"checking for new stocks from VIMS");
        String healthFacilityId,username,password;
        if (LOGGED_IN_USER_ID == null) {
            List<User> allUsers = databaseInstance.getAllUsers();
            User user = allUsers.get(0);
            healthFacilityId = user.getHealthFacilityId();
            username = user.getUsername();
            password = user.getPassword();

        } else {
            healthFacilityId = LOGGED_IN_USER_HF_ID;
            username = LOGGED_IN_USERNAME;
            password = LOGGED_IN_USER_PASS;
        }
        Log.d(TAG,"CheckNewStockDistributionsFromVims - Health Facility Id  = "+healthFacilityId);
        Log.d(TAG,"CheckNewStockDistributionsFromVims - Username  = "+username);

        final StringBuilder webServiceUrl = new StringBuilder(WCF_URL).append(HEALTH_FACILITY_SVC).append("/checkForNewStockFromVims?healthFacilityId="+healthFacilityId);

        Log.d(TAG,"CheckNewStockDistributionsFromVims - url  = "+webServiceUrl);

        client.setBasicAuth(username,password,true);
        client.get(webServiceUrl.toString(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG,"response "+responseString);
            }
        });

    }

}
