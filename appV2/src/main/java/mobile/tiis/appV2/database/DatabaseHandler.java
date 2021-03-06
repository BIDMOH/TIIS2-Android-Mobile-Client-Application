/*******************************************************************************
 * <!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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

package mobile.tiis.appv2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mobile.tiis.appv2.R;
import mobile.tiis.appv2.base.BackboneActivity;
import mobile.tiis.appv2.base.BackboneApplication;
import mobile.tiis.appv2.database.SQLHandler.Tables;
import mobile.tiis.appv2.entity.AdjustmentReasons;
import mobile.tiis.appv2.entity.AdministerVaccinesModel;
import mobile.tiis.appv2.entity.AefiListItem;
import mobile.tiis.appv2.entity.Birthplace;
import mobile.tiis.appv2.entity.ChartDataModel;
import mobile.tiis.appv2.entity.Child;
import mobile.tiis.appv2.entity.Dose;
import mobile.tiis.appv2.entity.HealthFacility;
import mobile.tiis.appv2.entity.HealthFacilityBalance;
import mobile.tiis.appv2.entity.ImmunizationCardItem;
import mobile.tiis.appv2.entity.ModelImmunizedChild;
import mobile.tiis.appv2.entity.NewChartDataTable;
import mobile.tiis.appv2.entity.NonVaccinationReason;
import mobile.tiis.appv2.entity.Place;
import mobile.tiis.appv2.entity.ScheduledVaccination;
import mobile.tiis.appv2.entity.Status;
import mobile.tiis.appv2.entity.Stock;
import mobile.tiis.appv2.entity.User;
import mobile.tiis.appv2.entity.VaccinationAppointment;
import mobile.tiis.appv2.entity.VaccinationEvent;
import mobile.tiis.appv2.entity.VaccinationQueueObject;
import mobile.tiis.appv2.fragments.FragmentVaccineNameQuantity;
import mobile.tiis.appv2.postman.PostmanModel;
import mobile.tiis.appv2.entity.StockStatusEntity;
import mobile.tiis.appv2.DatabaseModals.SessionsModel;


/**
 * Created by Melisa on 02/02/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHandler";

    public static final String DATABASE_NAME = "giis_mobile.db";
    private static final int DATABASE_VERSION = 6;
    public static boolean dbPreinstalled = false;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }

    public static void getDBFile(Context ctx) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + ctx.getPackageName() + "/databases/giis_mobile.db";
                String backupDBPath = Environment.getExternalStorageDirectory() + "/giis_mobile.sqlite";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.d(" ", "Database Transfered!");
                }
            }
        } catch (Exception e) {
            Log.e("", e.toString());
        }
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        db.execSQL("PRAGMA automatic_index = false;");
        return db;
    }

    public static boolean checkIfThereIsDatabaseFile(Context ctx){
        String currentDBPath = "/data/data/" + ctx.getPackageName() + "/databases/giis_mobile.db";
        File currentDB = new File(currentDBPath);
        return currentDB.exists();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (!dbPreinstalled) {
            db.execSQL(SQLHandler.SQLHealthFacilityTable);
            db.execSQL(SQLHandler.SQLChildUpdatesQueueTable);
            db.execSQL(SQLHandler.SQLPlaceTable);
            db.execSQL(SQLHandler.SQLBirthplaceTable);
            db.execSQL(SQLHandler.SQLAdjustmentTable);
            db.execSQL(SQLHandler.SQLConfigTable);
            db.execSQL(SQLHandler.SQLUserTable);
            db.execSQL(SQLHandler.SQLChildTable);

            db.execSQL("CREATE UNIQUE INDEX index_childId ON " + Tables.CHILD + "("+SQLHandler.ChildColumns.ID+")");

            db.execSQL(SQLHandler.SQLStatusTable);
            db.execSQL(SQLHandler.SQLCommunityTable);
            db.execSQL(SQLHandler.SQLChildWeightTable);
            db.execSQL(SQLHandler.SQLNonvaccinationReasonTable);
            db.execSQL(SQLHandler.SQLAgeDefinitionsTable);
            db.execSQL(SQLHandler.SQLItemTable);
            db.execSQL(SQLHandler.SQLScheduledVaccinationTable);
            db.execSQL(SQLHandler.SQLDoseTable);
            db.execSQL(SQLHandler.SQLVaccinationAppointmentTable);
            db.execSQL("CREATE UNIQUE INDEX index_vaccination_appointment ON " + Tables.VACCINATION_APPOINTMENT + "("+ SQLHandler.VaccinationAppointmentColumns.CHILD_ID +","+ SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE +")");



            db.execSQL(SQLHandler.SQLVaccinationEventTable);
            db.execSQL("CREATE INDEX childIdIndex ON " + Tables.VACCINATION_EVENT + "(CHILD_ID);");
            db.execSQL("CREATE UNIQUE INDEX index_vaccination_event ON " + Tables.VACCINATION_EVENT + "("+ SQLHandler.VaccinationEventColumns.CHILD_ID +","+ SQLHandler.VaccinationEventColumns.DOSE_ID+")");


            db.execSQL(SQLHandler.SQLWeightTable);
            db.execSQL(SQLHandler.SQLVaccinationQueueTable);
            db.execSQL(SQLHandler.SQLResponseTypeTable);
            //set data to the lookup table
            db.execSQL(SQLHandler.SQLFillResponseTypeTable);
            db.execSQL(SQLHandler.SQLPostmanTable);
            db.execSQL(SQLHandler.SQLUIValuesTable);
            db.execSQL(SQLHandler.SQLFillUitValuesTable);

            db.execSQL(SQLHandler.SQLMonthlyPlanView);

            db.execSQL(SQLHandler.SQLChildSupplements);
            db.execSQL(SQLHandler.SQLItemLot);
            db.execSQL(SQLHandler.SQLHealthFacilityBalance);
            db.execSQL(SQLHandler.SQLActiveLotNumber);

            db.execSQL(SQLHandler.SQLDeseaseSurveillanceData);
            db.execSQL(SQLHandler.SQLRefrigeratorColums);
            db.execSQL(SQLHandler.SQLStockStatus);
            db.execSQL(SQLHandler.SQLImmunizationSession);
            db.execSQL(SQLHandler.SQLVaccinationsBcgOpvTt);
            db.execSQL(SQLHandler.SQLMajorImunizationActivities);
            db.execSQL(SQLHandler.SQLSyringesAndSafetyBoxes);
            db.execSQL(SQLHandler.SQLHealthFacilityVitaminA);
            db.execSQL(SQLHandler.SQLDistributedStock);
            db.execSQL(SQLHandler.SQLLoginSessions);

        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        // TODO remove this when we want to use Upgrade database and also test how the upgrade
        // TODO function affects the case where the device will hav a DB preinstalled before even installing the appv2
        if (!dbPreinstalled) {
            // KILL PREVIOUS TABLES IF UPGRADED
            db.execSQL("DROP TABLE IF EXISTS " + Tables.HEALTH_FACILITY);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CHILD_UPDATES_QUEUE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.PLACE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ADJUSTMENT_REASONS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.USER);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CHILD);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.STATUS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.COMMUNITY);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CHILD_WEIGHT);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.NONVACCINATION_REASON);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.AGE_DEFINITIONS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEM);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.SCHEDULED_VACCINATION);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.DOSE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.VACCINATION_APPOINTMENT);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.VACCINATION_EVENT);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.VACCINATION_QUEUE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.POSTMAN);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.RESPONSE_TYPE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CHILD_SUPPLEMENTS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEM_LOT);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.HEALTH_FACILITY_BALANCE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.BIRTHPLACE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CONFIG);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ACTIVE_LOT_NUMBERS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.DESEASES_SURVEILLANCE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.REFRIGERATOR_TEMPERATURE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.STOCK_STATUS_REPORT);

            db.execSQL("DROP TABLE IF EXISTS " + Tables.IMMUNIZATION_SESSION);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.VACCINATIONS_BCG_OPV_TT);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.MAJOR_IMMUNIZATION_ACTIVITIES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.SYRINGES_AND_SAFETY_BOXES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.HF_VITAMIN_A);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.STOCK_DISTRIBUTIONS);

            db.execSQL("DROP VIEW IF EXISTS " + SQLHandler.Views.MONTHLY_PLAN);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.HF_LOGIN_SESSIONS);

            // CREATE NEW INSTANCE OF SCHEMA
            onCreate(db);
        }
    }

    public void insert(ContentValues cv, String object) {

    }

    public Cursor dosequery(String childId) {
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = null;
        //datetime('now','+10 days');
        cursor =
                database.rawQuery(
                        "SELECT FULLNAME " +
                                "FROM vaccination_event INNER JOIN  dose " +
                                "ON vaccination_event.DOSE_ID = dose.ID " +
                                "WHERE CHILD_ID=? " +
                                "AND APPOINTMENT_ID = vaccination_event.APPOINTMENT_ID " +
                                "AND vaccination_event.SCHEDULED_DATE <= datetime('now','+10 days') " +
                                "AND vaccination_event.IS_ACTIVE=true AND vaccination_event.VACCINATION_STATUS=false " +
                                "AND (vaccination_event.NONVACCINATION_REASON_ID=0  OR vaccination_event.NONVACCINATION_REASON_ID in (Select ID from nonvaccination_reason where KEEP_CHILD_DUE = 'true'))"
                        , new String[]{childId});
        return cursor;
    }

    public String getCurrentMonthName(BackboneApplication application){

        String monthName = "";

        java.util.Date now = new java.util.Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int month = calendar.get(Calendar.MONTH);

        if (month == 9 || month == 10 || month == 11){
            monthName = getMonthNameFromNumber((month+1)+"", application);
        }else {
            monthName = getMonthNameFromNumber(("0"+(month+1)), application);
        }

        return monthName;
    }

    /**
     * adds child weight after from weight save
     *
     * @param cv
     * @return
     */
    public long addChildWeight(ContentValues cv) {
        long result;
        SQLiteDatabase sd = getWritableDatabase();
        sd.beginTransaction();
        try{
            result = sd.insert(Tables.CHILD_WEIGHT, null, cv);
            sd.setTransactionSuccessful();
        }catch(Exception e){
            sd.endTransaction();
            throw e;
        }
        //end the transaction on no error
        sd.endTransaction();
        return result;
    }

    public long addNonVaccinationReason(ContentValues cv) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isNonVaccinationReasonPresent(cv.getAsString(SQLHandler.NonVaccinationReasonColumns.ID))) {
                result = sd.insert(Tables.NONVACCINATION_REASON, null, cv);
            } else {
                result = sd.update(Tables.NONVACCINATION_REASON, cv, SQLHandler.NonVaccinationReasonColumns.ID + "=" + cv.getAsString(SQLHandler.NonVaccinationReasonColumns.ID), null);
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public boolean isNonVaccinationReasonPresent(String id) {
        String selectQuery = "SELECT * FROM " + Tables.NONVACCINATION_REASON +
                " WHERE "+SQLHandler.NonVaccinationReasonColumns.ID+" = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public long addPlace(ContentValues cv) {

        SQLiteDatabase sd = getWritableDatabase();
        long result = sd.insert(Tables.PLACE, null, cv);
        return result;
    }

    public long addStockStatusReport(ContentValues cv){
        long result;

        SQLiteDatabase sd = getWritableDatabase();
        sd.beginTransaction();
        try{
            result = sd.insert(Tables.STOCK_STATUS_REPORT, null, cv);
            sd.setTransactionSuccessful();
        }catch(Exception e){
            result=-1;
            sd.endTransaction();
            throw e;
        }
        sd.endTransaction();
        return result;

    }

    public long updateStockStatusReport(ContentValues cv, String reportedMonth, String antigen){
        SQLiteDatabase sd = getWritableDatabase();
        long result ;
        sd.beginTransaction();
        try{
            result = sd.update(Tables.STOCK_STATUS_REPORT, cv, SQLHandler.StockStatusColumns.REPORTED_MONTH + "= ? AND "+
                            SQLHandler.StockStatusColumns.ITEM_NAME+" = ?",
                    new String[]{
                            reportedMonth,
                            antigen
                    });
            sd.setTransactionSuccessful();

            //do not any more database operations between
            //setTransactionSuccessful and endTransaction
        }catch(Exception e){
            //end the transaction on error too when doing exception handling
            result=-1;
            sd.endTransaction();
            throw e;
        }
        //end the transaction on no error
        sd.endTransaction();


        return result;
    }

    public long addStock(ContentValues cv) {

        long result;
        SQLiteDatabase sd = getWritableDatabase();
        sd.beginTransaction();
        try{
            result = sd.insert(Tables.HEALTH_FACILITY_BALANCE, null, cv);
            sd.setTransactionSuccessful();
        }catch(Exception e){
            result=-1;
            sd.endTransaction();
            throw e;
        }
        sd.endTransaction();
        return result;
    }

    public long updateStock(ContentValues cv, String id) {

        SQLiteDatabase sd = getWritableDatabase();
        long result ;
        sd.beginTransaction();
        try{
            result = sd.update(Tables.HEALTH_FACILITY_BALANCE, cv, SQLHandler.HealthFacilityBalanceColumns.LOT_ID + "=?",
                    new String[]{id});
            sd.setTransactionSuccessful();
            //do not any more database operations between
            //setTransactionSuccessful and endTransaction
        }catch(Exception e){
            //end the transaction on error too when doing exception handling
            result=-1;
            sd.endTransaction();
            throw e;
        }
        //end the transaction on no error
        sd.endTransaction();


        return result;
    }

    public boolean isStockInDB(String lot_id, String gtin) {
        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.HEALTH_FACILITY_BALANCE + " WHERE "
                +SQLHandler.HealthFacilityBalanceColumns.LOT_ID +" = '" + lot_id + "' AND "
                +SQLHandler.HealthFacilityBalanceColumns.GTIN +" = '" + gtin + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    public boolean isStockStatusInDB(String selectedMonth, String antigen){
        String selectQuery = "SELECT  * FROM " + Tables.STOCK_STATUS_REPORT + " WHERE "
                +SQLHandler.StockStatusColumns.REPORTED_MONTH+" = '" +selectedMonth+ "' AND "+SQLHandler.StockStatusColumns.ITEM_NAME+" = '"+antigen+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();

        Log.d("monthstartandenddate", "Query is : "+selectQuery);
        Log.d("monthstartandenddate", "Result is : "+found);

        cursor.close();
        return found;
    }


    public long addChild(ContentValues cv) {
        SQLiteDatabase db = getWritableDatabase();

        long result;

        db.beginTransaction();
        try{
            result = db.insert(Tables.CHILD, null, cv);
            db.setTransactionSuccessful();
        }catch(Exception e){
            result=-1;
            db.endTransaction();
            throw e;
        }
        db.endTransaction();
        return result;
    }
    public long addBulkChild(ContentValues cv) {
        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert(Tables.CHILD, null, cv);
        return result;
    }

    public long addStockAdjustment(ContentValues cv) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isStockAdjustmentPresent(cv.getAsString(SQLHandler.AdjustmentColumns.ID))) {
                result = sd.insert(Tables.ADJUSTMENT_REASONS, null, cv);
            } else {
                result = sd.update(Tables.ADJUSTMENT_REASONS, cv, SQLHandler.AdjustmentColumns.ID + "=" + cv.getAsString(SQLHandler.AdjustmentColumns.ID), null);
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public boolean isStockAdjustmentPresent(String id) {
        String selectQuery = "SELECT * FROM " + Tables.ADJUSTMENT_REASONS +
                " WHERE "+SQLHandler.AdjustmentColumns.ID+" = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }


    public long addStatus(ContentValues cv) {

        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert(Tables.STATUS, null, cv);
        return result;
    }


    public long addUpdateStatus(ContentValues cv, String statusId) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT or UPDATE
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isIdPresentInStatus(statusId)) {
                result = sd.insert(Tables.STATUS, null, cv);
            } else {
                result = sd.update(Tables.STATUS, cv, SQLHandler.StatusColumns.ID + "=?", new String[]{statusId});
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addUser(ContentValues cv) {

        SQLiteDatabase sd = getWritableDatabase();
        long result =-1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.USER, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }

    }

    public long addWeightList(ContentValues cv) {
        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert(Tables.WEIGHT, null, cv);
        return result;
    }

    public long addUpdateWeightList(ContentValues cv, String weightId) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT or UPDATE
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isIdPresentInWeight(weightId)) {
                result = sd.insert(Tables.WEIGHT, null, cv);
            } else {
                result = sd.update(Tables.WEIGHT, cv, SQLHandler.WeightColumns.ID + "=?", new String[]{weightId});
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addItemLot(ContentValues cv) {
        SQLiteDatabase sd = getWritableDatabase();
        long result =-1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.ITEM_LOT, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }
    public long addUpdateItemLot(ContentValues cv, String itemLotId) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT or UPDATE
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isIdPresentInItemLot(itemLotId)) {
                result = sd.insert(Tables.ITEM_LOT, null, cv);
            } else {
                result = sd.update(Tables.ITEM_LOT, cv, SQLHandler.ItemLotColumns.ID + "=?", new String[]{itemLotId});
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addDose(ContentValues cv) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isDosePresent(cv.getAsString(SQLHandler.DoseColumns.ID))) {
                result = sd.insert(Tables.DOSE, null, cv);
            } else {
                result = sd.update(Tables.DOSE, cv, SQLHandler.DoseColumns.ID + "=" + cv.getAsString(SQLHandler.DoseColumns.ID), null);
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public boolean isDosePresent(String id) {
        String selectQuery = "SELECT * FROM " + Tables.DOSE +
                " WHERE "+SQLHandler.DoseColumns.ID+" = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public long addVaccinationEvent(ContentValues cv) {
        SQLiteDatabase sd = getWritableDatabase();
        long result =-1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.VACCINATION_EVENT, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addAgeDefinitions(ContentValues cv) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isAgeDefinitionsPresent(cv.getAsString(SQLHandler.AgeDefinitionsColumns.ID))) {
                result = sd.insert(Tables.AGE_DEFINITIONS, null, cv);
            } else {
                result = sd.update(Tables.AGE_DEFINITIONS, cv, SQLHandler.AgeDefinitionsColumns.ID + "=" + cv.getAsString(SQLHandler.AgeDefinitionsColumns.ID), null);
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }


    public String getDistrictNames(String healthFacilityId) {
        SQLiteDatabase sd = getWritableDatabase();
        String result = "";

        sd.beginTransaction();
        try {
            Cursor c = sd.rawQuery("SELECT * FROM "+Tables.PLACE+" WHERE "+GIISContract.PlaceColumns.HEALTH_FACILITY_ID+" = '"+healthFacilityId+"'",null);
            c.moveToFirst();
            result = c.getString(c.getColumnIndex(GIISContract.HealthFacilityColumns.NAME));
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public boolean isAgeDefinitionsPresent(String id) {
        String selectQuery = "SELECT * FROM " + Tables.AGE_DEFINITIONS +
                " WHERE "+SQLHandler.AgeDefinitionsColumns.ID+" = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public long addScheduledVaccination(ContentValues cv) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isScheduledVaccinationPresent(cv.getAsString(SQLHandler.ScheduledVaccinationColumns.ID))) {
                result = sd.insert(Tables.SCHEDULED_VACCINATION, null, cv);
            } else {
                result = sd.update(Tables.SCHEDULED_VACCINATION, cv, SQLHandler.ScheduledVaccinationColumns.ID + "=" + cv.getAsString(SQLHandler.ScheduledVaccinationColumns.ID), null);
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public boolean isScheduledVaccinationPresent(String id) {
        String selectQuery = "SELECT * FROM " + Tables.SCHEDULED_VACCINATION +
                " WHERE "+SQLHandler.ScheduledVaccinationColumns.ID+" = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }


    public long addItem(ContentValues cv) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isItemPresent(cv.getAsString(SQLHandler.ItemColumns.ID))) {
                result = sd.insert(Tables.ITEM, null, cv);
            } else {
                result = sd.update(Tables.ITEM, cv, SQLHandler.ItemColumns.ID + "=" + cv.getAsString(SQLHandler.ItemColumns.ID), null);
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public boolean isItemPresent(String id) {
        String selectQuery = "SELECT * FROM " + Tables.ITEM +
                " WHERE "+SQLHandler.ItemColumns.ID+" = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public long addHealthFacility(ContentValues cv) {
        SQLiteDatabase sd = getWritableDatabase();
        long result =-1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.HEALTH_FACILITY, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }

    }

    public long addUpdateRefrigeratorTemperature(ContentValues cv, String selectedMonth){
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isColdChainPresentInDb(selectedMonth)) {
                Log.d("COLD_CHAIN_TAG", "Not present, adding new");
                result = sd.insert(Tables.REFRIGERATOR_TEMPERATURE, null, cv);
            } else {
                Log.d("COLD_CHAIN_TAG", "Yes present Update");
                result = sd.update(Tables.REFRIGERATOR_TEMPERATURE, cv, SQLHandler.RefrigeratorColums.REPORTED_MONTH + "=?", new String[]{selectedMonth});
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addUpdateDeseasesSurveillance(ContentValues cv, String selectedMonth){
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isDeseaseSurveillanceEntryInDB(selectedMonth)) {
                Log.d("COLD_CHAIN_TAG", "DESEASE: Not present, adding new");
                result = sd.insert(Tables.DESEASES_SURVEILLANCE, null, cv);
            } else {
                Log.d("COLD_CHAIN_TAG", "DESEASE: Yes present Update");
                result = sd.update(Tables.DESEASES_SURVEILLANCE, cv, SQLHandler.RefrigeratorColums.REPORTED_MONTH + "=?", new String[]{selectedMonth});
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    /**
     *
     * @param cv
     * @param selectedMonth (Month Name + Year)
     * @param doseID
     * @return
     */
    public long addUpdateVaccinationsBcgOpvTt(ContentValues cv, String selectedMonth, String doseID){
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isaVaccinationsBcgOpvTtInDb(selectedMonth, doseID)) {
                Log.d("COLD_CHAIN_TAG", "BCG_OPV_TT: Not present, adding new");
                result = sd.insert(Tables.VACCINATIONS_BCG_OPV_TT, null, cv);
            } else {
                Log.d("COLD_CHAIN_TAG", "BCG_OPV_TT: Yes present Update");
                result = sd.update(Tables.VACCINATIONS_BCG_OPV_TT, cv, SQLHandler.VaccinationsBcgOpvTtColumns.REPORTING_MONTH + "=? AND "+
                        SQLHandler.VaccinationsBcgOpvTtColumns.DOSE_ID+" =?",
                        new String[]{
                                selectedMonth,
                                doseID
                        });
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addUpdateInjectionEquipment(ContentValues cv, String selectedMonth, String itemName){
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isInjectionItemInDb(selectedMonth, itemName)) {
                Log.d("COLD_CHAIN_TAG", "Syringes: Not present, adding new");
                result = sd.insert(Tables.SYRINGES_AND_SAFETY_BOXES, null, cv);
            } else {
                Log.d("COLD_CHAIN_TAG", "Syringes: Yes present Update");
                result = sd.update(Tables.SYRINGES_AND_SAFETY_BOXES, cv, SQLHandler.SyringesAndSafetyBoxesColumns.REPORTING_MONTH + "=? AND "+
                                SQLHandler.SyringesAndSafetyBoxesColumns.ITEM_NAME+" =?",
                        new String[]{
                                selectedMonth,
                                itemName
                        });
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addUpdateVitaminAStock(ContentValues cv, String selectedMonth, String vitaminName){
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isVitaminAInDb(selectedMonth, vitaminName)) {
                Log.d("VITAMIN_A", "Vitamin A : Not present, adding new");
                result = sd.insert(Tables.HF_VITAMIN_A, null, cv);
            } else {
                Log.d("VITAMIN_A", "Vitamin A : Yes present Update");
                result = sd.update(Tables.HF_VITAMIN_A, cv, SQLHandler.HfVitaminAColumns.REPORTING_MONTH + "=? AND "+
                                SQLHandler.HfVitaminAColumns.VITAMIN_NAME+" =?",
                        new String[]{
                                selectedMonth,
                                vitaminName
                        });
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addUpdateImmunizationSessions(ContentValues cv, String selectedMonth){
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isImmunizationSessionInDb(selectedMonth)) {
                Log.d("COLD_CHAIN_TAG", "BCG_OPV_TT: Not present, adding new");
                result = sd.insert(Tables.IMMUNIZATION_SESSION, null, cv);
            } else {
                Log.d("COLD_CHAIN_TAG", "BCG_OPV_TT: Yes present Update");
                result = sd.update(Tables.IMMUNIZATION_SESSION, cv, SQLHandler.VaccinationsBcgOpvTtColumns.REPORTING_MONTH + "=?",
                        new String[]{
                                selectedMonth
                        });
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addImmunizationSessions(ContentValues cv){
        SQLiteDatabase sd = getWritableDatabase();
        long result =-1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.IMMUNIZATION_SESSION, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addOtherMajorImmunizationActivities(ContentValues cv){
        SQLiteDatabase sd = getWritableDatabase();
        long result =-1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.MAJOR_IMMUNIZATION_ACTIVITIES, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long addUpdateHealthFacility(ContentValues cv, String healthFacId) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT or UPDATE
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isIdPresentInHealthFac(healthFacId)) {
                result = sd.insert(Tables.HEALTH_FACILITY, null, cv);
            } else {
                result = sd.update(Tables.HEALTH_FACILITY, cv, SQLHandler.HealthFacilityColumns.ID + "=?", new String[]{healthFacId});
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }
    public List<Place> getAllPlaces() {
        //Container
        List<Place> placeList = new ArrayList<Place>();

        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.PLACE +" GROUP BY NAME ORDER BY NAME";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Place place = new Place();
                place.setId(cursor.getString(4));//(Integer.parseInt(cursor.getString(4)));
                place.setName(cursor.getString(5));
                place.setCode(cursor.getString(7));
                placeList.add(place);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return placeList;
    }

    public List<HealthFacility> getAllHealthFacility() {

        //Container
        List<HealthFacility> healthFacilityList = new ArrayList<HealthFacility>();

        //Query on HealthFacility Table
        String selectQuery = "SELECT * FROM " + Tables.HEALTH_FACILITY + " WHERE "+ SQLHandler.HealthFacilityColumns.TYPE_ID+" = '3'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                HealthFacility healthFacility = new HealthFacility();
                healthFacility.setId(cursor.getString(4));
                healthFacility.setName(cursor.getString(5));
                healthFacility.setCode(cursor.getString(6));
                healthFacility.setTypeId(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityColumns.TYPE_ID)));
                healthFacility.setParentId(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityColumns.PARENT_ID)));
                healthFacilityList.add(healthFacility);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return healthFacilityList;

    }

    public List<HealthFacility> getAllDistrictCoucils() {

        //Container
        List<HealthFacility> healthFacilityList = new ArrayList<HealthFacility>();

        //Query on HealthFacility Table
        String selectQuery = "SELECT * FROM " + Tables.HEALTH_FACILITY + " WHERE "+ SQLHandler.HealthFacilityColumns.TYPE_ID+" = '2'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                HealthFacility healthFacility = new HealthFacility();
                healthFacility.setId(cursor.getString(4));
                healthFacility.setName(cursor.getString(5));
                healthFacility.setCode(cursor.getString(6));
                healthFacility.setTypeId(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityColumns.TYPE_ID)));
                healthFacilityList.add(healthFacility);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return healthFacilityList;

    }

    public List<Status> getStatus() {

        //Container
        List<Status> statusList = new ArrayList<Status>();

        //Query on Status Table
        String selectQuery = "SELECT  * FROM " + Tables.STATUS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Status status = new Status();
                status.setId(cursor.getString(4));
                status.setName(cursor.getString(7));
                statusList.add(status);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return statusList;

    }

    public List<User> getAllUsers() {
        //Container
        List<User> userList = new ArrayList<User>();

        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.USER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getString(4));
                user.setUsername(cursor.getString(5));
                user.setFirstname(cursor.getString(7));
                user.setLastname(cursor.getString(8));
                user.setPassword(cursor.getString(6));
                //user.setLastLogin(Date.valueOf(cursor.getString(4)));
                //user.setUserRoleId(cursor.getString(4));
                user.setHealthFacilityId(cursor.getString(16));
                userList.add(user);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return userList;
    }

    public List<Child> getAllChildren() {
        //Container
        List<Child> childList = new ArrayList<Child>();

        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.CHILD;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Child child = new Child();
                child.setId(cursor.getString(4));//(Integer.parseInt(cursor.getString(4)));
                child.setBarcodeID(cursor.getString(6));
                child.setFirstname1(cursor.getString(8));
                child.setLastname1(cursor.getString(9));
                // Adding child to list
                childList.add(child);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return childList;
    }

    public List<NonVaccinationReason> getAllNonvaccinationReasons() {
        //Container
        List<NonVaccinationReason> nvrList = new ArrayList<NonVaccinationReason>();

        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.NONVACCINATION_REASON;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                NonVaccinationReason nvr = new NonVaccinationReason();
                nvr.setId(cursor.getString(5));//(Integer.parseInt(cursor.getString(4)));
                nvr.setName(cursor.getString(6));
                nvr.setKeepChildDue(cursor.getString(4));
                // Adding child to list
                nvrList.add(nvr);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return nvrList;
    }

    /**
     * this method checks if a child is in the DB
     *
     * @param barcodeId
     * @return
     */
    public boolean isChildInDB(String barcodeId) {
        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.CHILD + " WHERE BARCODE_ID = '" + barcodeId + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    /**
     * @param barcodeId
     * @param dateNow   Date in format yyyy-MM-dd
     * @return
     * @Arinela this method checks if a child is in the DB
     */
    public boolean isChildInChildWeightToday(String barcodeId, String dateNow) {
        String selectQuery = "SELECT  * FROM " + Tables.CHILD_WEIGHT + " WHERE CHILD_BARCODE = '" + barcodeId + "' and DATE=datetime('" + dateNow + "')";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    /**
     * This method is used by the postman synchroniser that checks every x minutes if there ate any posts in queue to be sent
     *
     * @return null if no posts or a List<PostmanModel> if there are posts to be sent
     */
    public List<PostmanModel> getAllPosts() {
        List<PostmanModel> postList = null;
        String selectQuery = "SELECT  * FROM " + Tables.POSTMAN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            postList = new ArrayList<>();
            do {
                PostmanModel post = new PostmanModel();
                post.setPostId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
                post.setUrl(cursor.getString(cursor.getColumnIndex(SQLHandler.PostmanColumns.URL)));
                post.setResponseType(cursor.getInt(cursor.getColumnIndex(SQLHandler.PostmanColumns.RESPONSE_TYPE_ID)));
                post.setTempId(cursor.getString(cursor.getColumnIndex(SQLHandler.PostmanColumns.TEMPORARY_ID)));
                postList.add(post);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return postList;
    }

    /**
     * used to delete a post from postman table after result was SC_OK
     *
     * @param postId
     * @return
     */
    public boolean deletePostFromPostman(int postId) {
        SQLiteDatabase db = getWritableDatabase();
        long result = -1;
        db.beginTransaction();
        try {
            result = db.delete(Tables.POSTMAN, BaseColumns._ID + " = " + postId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            db.endTransaction();
            return result > 0;
        }
    }

    /**
     * This method adds a post to be sent later to the server
     *
     * @param url            the url to be posted later
     * @param responseTypeId the response type id
     * @return
     */
    public long addPost(String url, int responseTypeId) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT
        SQLiteDatabase sd = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLHandler.PostmanColumns.URL, url);
        cv.put(SQLHandler.PostmanColumns.RESPONSE_TYPE_ID, responseTypeId);
        cv.putNull(SQLHandler.PostmanColumns.TEMPORARY_ID);
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.POSTMAN, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }


    /**
     * This method adds a post to be sent later to the server
     *
     * @param url            the url to be posted later
     * @param responseTypeId the response type id
     * @return
     */
    public long updatePost(String url, int responseTypeId,int postmanId) {
        SQLiteDatabase sd = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLHandler.PostmanColumns.URL, url);
        cv.put(SQLHandler.PostmanColumns.RESPONSE_TYPE_ID, responseTypeId);
        cv.putNull(SQLHandler.PostmanColumns.TEMPORARY_ID);
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.POSTMAN, cv,BaseColumns._ID+" = "+postmanId,null);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }


    /**
     * This method adds a child id to the childupdates queue to be used to pull child updates incase of network failure
     *
     * @param childId            the url to be posted later
     * @param responseTypeId the response type id
     * @return
     */
    public long addChildToChildUpdatesQueue(String childId, int responseTypeId) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT
        SQLiteDatabase sd = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLHandler.ChildUpdatesQueueColumns.CHILD_ID, childId);
        cv.put(SQLHandler.ChildUpdatesQueueColumns.RESPONSE_TYPE_ID, responseTypeId);
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.CHILD_UPDATES_QUEUE, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    /**
     * This method select all child id from the childupdates queue to be used to pull child updates uponreceiving a push tickle message
     *
     * @return List<ChildIds>
     */
    public List<String> getChildIdsFromChildUpdatesQueue() {
        String selectQuery = "SELECT CHILD_ID FROM " + Tables.CHILD_UPDATES_QUEUE;
        List<String> childIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int size = cursor.getCount();

        for(int i=0;i<size;i++) {
            cursor.moveToPosition(i);
            childIds.add(cursor.getString(0));

        }
        cursor.close();
        return childIds;
    }


    public boolean removeChildFromChildUpdateQueue(String childId) {
        SQLiteDatabase sd = getWritableDatabase();
        String[] whereArgs = new String[]{String.valueOf(childId)};
        int result = sd.delete(Tables.CHILD_UPDATES_QUEUE, SQLHandler.ChildUpdatesQueueColumns.CHILD_ID
                + "= ? ", whereArgs);

        return (result > 0);
    }

    /**
     * This method adds a post to be sent later to the server
     *
     * @param url            the url to be posted later
     * @param responseTypeId the response type id
     * @return
     */
    public long addPost(String url, int responseTypeId, String tempId) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT
        SQLiteDatabase sd = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLHandler.PostmanColumns.URL, url);
        cv.put(SQLHandler.PostmanColumns.RESPONSE_TYPE_ID, responseTypeId);
        cv.put(SQLHandler.PostmanColumns.TEMPORARY_ID, tempId);
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.POSTMAN, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    /**
     * method used to get child id by passing the barcode of the child
     *
     * @param barcodeId
     * @return the childId if there is one or null if not
     */
    public String getChildIdByBarcode(String barcodeId) {
        //Query on Child Table
        String selectQuery = "SELECT ID FROM " + Tables.CHILD + " WHERE BARCODE_ID = '" + barcodeId + "'";
        String childId = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            childId = cursor.getString(0);
            cursor.close();
            return childId;
        }
        cursor.close();
        return null;
    }

    /**
     * method used to get weight by passing the barcode of the child
     * this method is used in Admin Vacc
     *
     * @param barcodeId
     * @return long
     */
    public float  getWeightForToday(String barcodeId) {
        //Query on Child Table
        String selectQuery = "SELECT WEIGHT FROM " + Tables.CHILD_WEIGHT + " WHERE CHILD_BARCODE = '" + barcodeId
                + "' AND datetime(DATE,'unixepoch') < datetime('now','+1 days')  " +
                " AND datetime(DATE,'unixepoch') > datetime('now', '-1 days') ";
        float  weight =-1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            weight = cursor.getFloat(0);
            cursor.close();
        }
        cursor.close();
        return weight;
    }

    /**
     * Merr diferencen ne dite nga birthdate_old ne birthdate_new dmth ne bejme
     * Days = birthdate_new – birthdate_old
     * Dhe days mund te kete vlere positive ose negative
     * <p/>
     * Merr listen e te gjithe vaccination_appointment ku Child_id = id qe ke
     * Per secilin ndrysho “scheduled_date” = “scheduled_date” + days
     *
     * @param childId
     * @param numberOfDaysChange
     * @return
     */
    public boolean updateVaccinationAppointementForBirthDtChangeChild(String childId, long numberOfDaysChange) {
        String selectQuery = "SELECT datetime(substr(" + SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE + " ,7,10), 'unixepoch') , " +
                SQLHandler.VaccinationAppointmentColumns.ID + " FROM " +
                Tables.VACCINATION_APPOINTMENT + " WHERE " + SQLHandler.VaccinationAppointmentColumns.CHILD_ID + "='" + childId + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String dateStr = cursor.getString(0);
                long date = 0;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr).getTime();
                } catch (ParseException e) {
                    return false;
                }
                java.util.Date birthDate = new Date(date + numberOfDaysChange);
                String newAppointementDate = BackboneActivity.stringToDateParser(birthDate);
                ContentValues contentValues = new ContentValues();
                contentValues.put(SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE, newAppointementDate);
                db.beginTransaction();
                try {
                    db.update(Tables.VACCINATION_APPOINTMENT, contentValues, SQLHandler.VaccinationAppointmentColumns.ID + "=?",
                            new String[]{
                                    cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationAppointmentColumns.ID))
                            });
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    cursor.close();
                    return false;
                } finally {
                    db.endTransaction();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return true;
    }

    public boolean updateVaccinationEventForBirthDtChangeChild(String childId, long numberOfDaysChange) {
        String selectQuery = "SELECT datetime(substr(" + SQLHandler.VaccinationEventColumns.SCHEDULED_DATE + " ,7,10), 'unixepoch') , " +
                SQLHandler.VaccinationEventColumns.ID + " FROM " +
                Tables.VACCINATION_EVENT + " WHERE " + SQLHandler.VaccinationEventColumns.CHILD_ID + "='" + childId + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String dateStr = cursor.getString(0);
                long date = 0;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr).getTime();
                } catch (ParseException e) {
                    return false;
                }
                java.util.Date birthDate = new Date(date + numberOfDaysChange);
                String newEventDate = BackboneActivity.stringToDateParser(birthDate);
                ContentValues contentValues = new ContentValues();
                contentValues.put(SQLHandler.VaccinationEventColumns.SCHEDULED_DATE, newEventDate);
                db.beginTransaction();
                try {
                    db.update(Tables.VACCINATION_EVENT, contentValues, SQLHandler.VaccinationEventColumns.ID + "=?",
                            new String[]{
                                    cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.ID))
                            });
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    return false;
                } finally {
                    db.endTransaction();
                }

            } while (cursor.moveToNext());
        }
        return true;
    }

    /**
     * Kur bejme ndryshim tek statusi i child:
     * Nqse status_new <> 1 (Active)
     * Merr listen e te gjithe vaccination_appointment ku Child_id = id qe ke
     * Per secilin ndrysho “is_active” = false;
     *
     * @param childId
     * @return
     */
    public boolean updateVaccinationAppointementDisactive(String childId) {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase db = this.getReadableDatabase();

        contentValues.put(SQLHandler.VaccinationAppointmentColumns.IS_ACTIVE, "false");
        db.beginTransaction();
        try {
            db.update(Tables.VACCINATION_APPOINTMENT, contentValues, SQLHandler.VaccinationAppointmentColumns.CHILD_ID + "=?",
                    new String[]{
                            childId
                    });
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * Kur bejme ndryshim tek health facility i child:
     * Nqse health_facility_new <> health_facility_old
     * Merr listen e te gjithe vaccination_appointment ku child_id = id qe ke
     * Per secilin ndrysho “Scheduled_facility_id” = health_facility_new
     *
     * @param childId
     * @param healthCenterId
     * @return
     */
    public boolean updateVaccinationAppointementNewFacility(String childId, String healthCenterId) {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase db = this.getReadableDatabase();
        contentValues.put(SQLHandler.VaccinationAppointmentColumns.SCHEDULED_FACILITY_ID, healthCenterId);
        db.beginTransaction();
        try {
            db.update(Tables.VACCINATION_APPOINTMENT, contentValues, SQLHandler.VaccinationAppointmentColumns.CHILD_ID + "=?",
                    new String[]{
                            childId
                    });
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * Kur bejme ndryshim tek health facility i child:
     * Nqse health_facility_new <> health_facility_old
     * Merr listen e te gjithe vaccination_appointment ku child_id = id qe ke
     * Per secilin ndrysho “Scheduled_facility_id” = health_facility_new
     *
     * @param childId
     * @param healthCenterId
     * @return
     */
    public boolean updateVaccinationEventNewFacility(String childId, String healthCenterId) {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase db = this.getReadableDatabase();
        contentValues.put(SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID, healthCenterId);
        db.beginTransaction();
        try {
            db.update(Tables.VACCINATION_EVENT, contentValues, SQLHandler.VaccinationEventColumns.CHILD_ID + "=? and" +
                            SQLHandler.VaccinationEventColumns.VACCINATION_STATUS + " = 'false' and " +
                            SQLHandler.VaccinationEventColumns.NONVACCINATION_REASON_ID + " = 0",
                    new String[]{
                            childId
                    });
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * method used to get child health facility id by passing the ID of the child
     *
     * @param childId
     * @return the Health Facility ID if there is one or null if not
     */
    public String getChildHFIDByChildId(String childId) {
        //Query on Child Table
        String selectQuery = "SELECT " + SQLHandler.ChildColumns.HEALTH_FACILITY_ID + " FROM " + Tables.CHILD + " WHERE ID =?";
        String hfid = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{childId});
        if (cursor.moveToFirst()) {
            hfid = cursor.getString(0);
            cursor.close();
            return hfid;
        }
        cursor.close();
        return null;
    }

    /**
     * method used to get logged in user health facility id by passing the ID of the User
     *
     * @param userId
     * @return the Health Facility ID if there is one or null if not
     */
    public String getUserHFIDByUserId(String userId) {
        //Query on Child Table
        String selectQuery = "SELECT " + SQLHandler.UserColumns.HEALTH_FACILITY_ID + " FROM " + Tables.USER + " WHERE ID =?";
        String hfid = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userId});
        if (cursor.moveToFirst()) {
            hfid = cursor.getString(0);
            cursor.close();
            return hfid;
        }
        cursor.close();
        return null;
    }

    /**
     * This method decides if child is to be added to VaccinationQueue
     *
     * @param childId
     * @return true if child is to be added or false otherwise
     */
    public boolean isChildToBeAddedInVaccinationQueue(String childId) {
        String selectQuery = "SELECT Count (*) FROM VACCINATION_EVENT" +
                " WHERE CHILD_ID = '" + childId + "' AND datetime(substr(SCHEDULED_DATE,7,10), 'unixepoch') <= datetime('now','+10 days')" +
                " AND VACCINATION_STATUS = 'false' AND" +
                " (NONVACCINATION_REASON_ID=0  OR NONVACCINATION_REASON_ID in (Select ID from nonvaccination_reason where KEEP_CHILD_DUE = 'true'))  AND IS_ACTIVE = 'true'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getInt(0) > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    /**
     * This method looks if child is in VaccinationQueue
     *
     * @param childId
     * @return true if child is found or false otherwise
     */
    public boolean isChildInVaccinationQueue(String childId) {
        String selectQuery = "SELECT * FROM " + Tables.VACCINATION_QUEUE +
                " WHERE CHILD_ID = '" + childId + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    /**
     * Method adds child to vaccination Queue
     *
     * @param cv values to be added
     * @return returns a number greater than -1 if transaction was successful or -1 if not
     */
    public long addChildToVaccinationQueue(ContentValues cv) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isChildInVaccinationQueue(cv.getAsString(SQLHandler.VaccinationQueueColumns.CHILD_ID))) {
                result = sd.insert(Tables.VACCINATION_QUEUE, null, cv);
            } else {
                result = sd.update(Tables.VACCINATION_QUEUE, cv, SQLHandler.VaccinationQueueColumns.CHILD_ID + "=" + cv.getAsString(SQLHandler.VaccinationQueueColumns.CHILD_ID), null);
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public boolean deleteVaccinationQueueEntriesOfOtherDays(String dateNow) {
        SQLiteDatabase db = getWritableDatabase();
        long result = -1;
        db.beginTransaction();
        try {
            result = db.delete(Tables.VACCINATION_QUEUE, SQLHandler.VaccinationQueueColumns.DATE + " NOT like '%" + dateNow + "%'", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            db.endTransaction();
            return result > 0;
        }
    }

    public String getAllChilcIdInVaccinationQueue() {
        //Query on  VACCINATION_QUEUE
        String selectQuery = "SELECT " + SQLHandler.VaccinationQueueColumns.CHILD_ID + " FROM " + Tables.VACCINATION_QUEUE
                + " ORDER BY datetime(substr(DATE,1,10))";
        String childIds = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean gotFirst = false;
        if (cursor.moveToFirst()) {
            do {
                if (gotFirst) {
                    childIds += ",'" + cursor.getString(0) + "'";
                } else {
                    gotFirst = true;
                    childIds += "'" + cursor.getString(0) + "'";
                }
            } while (cursor.moveToNext());
            cursor.close();
            return childIds;
        }
        cursor.close();
        return null;
    }

    /**
     * get list of vaccines with quantities associated for Vaccination Queue
     *
     * @return a list or null if no data
     */
    public ArrayList<FragmentVaccineNameQuantity.VacineNameQuantity> getQuantityOfVaccinesNeededVaccinationQueue(String schedule) {
        ArrayList<FragmentVaccineNameQuantity.VacineNameQuantity> list = new ArrayList<>();
        String _schedule = "";
        _schedule = schedule;
        String selectQuery = "";
        if (_schedule.isEmpty() || _schedule.equals("") || _schedule == null){
            selectQuery = "Select SCHEDULED_VACCINATION.NAME as VACCINE, COUNT(SCHEDULED_VACCINATION_ID) as QUANTITY"
                    + " FROM monthly_plan inner join SCHEDULED_VACCINATION on monthly_plan.SCHEDULED_VACCINATION_ID = "
                    + " SCHEDULED_VACCINATION.ID WHERE monthly_plan.CHILD_ID in (" + getAllChilcIdInVaccinationQueue() + ") AND "
                    + " datetime(substr(SCHEDULED_DATE,7,10), 'unixepoch') <= datetime('now','+10 days')"
                    + " GROUP BY SCHEDULED_VACCINATION_ID, VACCINE  ORDER BY SCHEDULED_VACCINATION_ID";
        }else{
            selectQuery = "Select SCHEDULED_VACCINATION.NAME as VACCINE, COUNT(SCHEDULED_VACCINATION_ID) as QUANTITY"
                    + " FROM monthly_plan inner join SCHEDULED_VACCINATION on monthly_plan.SCHEDULED_VACCINATION_ID = "
                    + " SCHEDULED_VACCINATION.ID WHERE monthly_plan.CHILD_ID in (" + getAllChilcIdInVaccinationQueue() + ") AND "
                    + " monthly_plan.SCHEDULE = '"+ _schedule +"' AND "
                    + " datetime(substr(SCHEDULED_DATE,7,10), 'unixepoch') <= datetime('now','+10 days')"
                    + " GROUP BY SCHEDULED_VACCINATION_ID, VACCINE  ORDER BY SCHEDULED_VACCINATION_ID";
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                FragmentVaccineNameQuantity.VacineNameQuantity item = new FragmentVaccineNameQuantity.VacineNameQuantity();
                item.setName(cursor.getString(0));
                item.setQuantity(cursor.getInt(1));
                list.add(item);
            } while (cursor.moveToNext());
            cursor.close();
            return list;
        }
        cursor.close();
        return null;
    }

    /**
     * get list of vaccines with quantities associated for Vaccination Queue
     *
     * @return a list or null if no data
     */
    public ArrayList<FragmentVaccineNameQuantity.VacineNameQuantity> getQuantityOfVaccinesNeededMonthlyPlan(String hfid, String schedule, String fromDate, String toDate) {
        ArrayList<FragmentVaccineNameQuantity.VacineNameQuantity> list = new ArrayList<>();
        String _schedule = "";
        _schedule   = schedule;
        String selectQuery = "";

        long t = Calendar.getInstance().getTimeInMillis()/1000;
        long t1 = (t + (30 * 24 * 60 * 60));
        long t2 = (t - (30 * 24 * 60 * 60));
        String to_date =  t1+ "";
        String from_date = t2+ "";

        try {
            if (!fromDate.equals("") && !toDate.equals("")) {
                from_date = fromDate;
                Log.d("day13", "from picker : "+from_date);
                to_date = toDate;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (_schedule.isEmpty() || _schedule.equals("") || _schedule == null){
            selectQuery = "Select SCHEDULED_VACCINATION.NAME as VACCINE, COUNT(SCHEDULED_VACCINATION_ID) as QUANTITY"
                    + " FROM monthly_plan inner join SCHEDULED_VACCINATION on monthly_plan.SCHEDULED_VACCINATION_ID = "
                    + " SCHEDULED_VACCINATION.ID WHERE HEALTH_FACILITY_ID = " + hfid + " AND "
                    + " (substr(SCHEDULED_DATE,7,10)) > ('" +from_date+ "') "
                    + " AND (substr(SCHEDULED_DATE,7,10)) <= ('" +to_date+ "') "
                    + " GROUP BY SCHEDULED_VACCINATION_ID, VACCINE  ORDER BY SCHEDULED_VACCINATION_ID";
        }else{
            selectQuery = "Select SCHEDULED_VACCINATION.NAME as VACCINE, COUNT(SCHEDULED_VACCINATION_ID) as QUANTITY"
                    + " FROM monthly_plan inner join SCHEDULED_VACCINATION on monthly_plan.SCHEDULED_VACCINATION_ID = "
                    + " SCHEDULED_VACCINATION.ID WHERE HEALTH_FACILITY_ID = " + hfid + " AND "
                    + " monthly_plan.SCHEDULE = '"+ _schedule +"' AND "
                    + " (substr(SCHEDULED_DATE,7,10)) > ('" +from_date+ "') "
                    + " AND (substr(SCHEDULED_DATE,7,10)) <= ('" +to_date+ "') "
                    + " GROUP BY SCHEDULED_VACCINATION_ID, VACCINE  ORDER BY SCHEDULED_VACCINATION_ID";
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                FragmentVaccineNameQuantity.VacineNameQuantity item = new FragmentVaccineNameQuantity.VacineNameQuantity();
                item.setName(cursor.getString(0));
                item.setQuantity(cursor.getInt(1));
                list.add(item);
            } while (cursor.moveToNext());
            cursor.close();
            return list;
        }
        cursor.close();
        return null;
    }

    public long updateChild(ContentValues cv, String id) {
        Log.d("CSN","Database Handler updating child");
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.CHILD, cv, SQLHandler.ChildColumns.ID + "=?",
                    new String[]{
                            id
                    });
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long updateChildWithBarcode(ContentValues cv, String barcodeId) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.CHILD, cv, SQLHandler.ChildColumns.BARCODE_ID + "=?",
                    new String[]{
                            barcodeId
                    });
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public void updateAdministerVaccineSchedule(ContentValues cv, String vacc_event_id) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.update(Tables.VACCINATION_EVENT, cv, "ID=?", new String[]{vacc_event_id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

//    public long updateAdministerVaccineSchedule(ContentValues cv, String vacc_event_id) {
//        SQLiteDatabase sd = getWritableDatabase();
//        long result = -1;
//        sd.beginTransaction();
//        try {
//            result = sd.update(Tables.VACCINATION_EVENT, cv, SQLHandler.VaccinationEventColumns.ID + "=?",
//                    new String[]{
//                            vacc_event_id
//                    });
//            sd.setTransactionSuccessful();
//        } catch (Exception e) {
//            //Error in between database transaction
//            result = -1;
//        } finally {
//            sd.endTransaction();
//            return result;
//        }
//    }

    public boolean isVaccinationEventInDb(String childId, String doseId) {
        String selectQuery = "SELECT MODIFIED_ON,MODIFIED_BY FROM " + Tables.VACCINATION_EVENT +
                " WHERE CHILD_ID = '" + childId + "' AND "
                +" DOSE_ID = '"+doseId+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public boolean isVaccinationAppointmentInDb(String childId, String scheduledDate) {
        String selectQuery = "SELECT * FROM " + Tables.VACCINATION_APPOINTMENT +
                " WHERE CHILD_ID = '" + childId + "' AND "
                +" SCHEDULED_DATE = '"+scheduledDate+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public long updateVaccinationEvent(ContentValues cv, String id) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.VACCINATION_EVENT, cv, SQLHandler.VaccinationEventColumns.ID + "=?",
                    new String[]{
                            id
                    });
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long updateVaccinationEvent(ContentValues cv, String childId , String doseId) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.VACCINATION_EVENT, cv, SQLHandler.VaccinationEventColumns.CHILD_ID + "=? AND "+ SQLHandler.VaccinationEventColumns.DOSE_ID +"=? ",
                    new String[]{
                            childId,doseId
                    });
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }


    public long updateVaccinationAppointment(ContentValues cv, String id) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.VACCINATION_APPOINTMENT, cv, SQLHandler.VaccinationAppointmentColumns.ID + "=?",
                    new String[]{
                            id
                    });
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public long updateVaccinationAppointmentByScheduledDate(ContentValues cv, String date) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.VACCINATION_APPOINTMENT, cv, SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE + "=?",
                    new String[]{
                            date
                    });
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }


    public long updateVaccinationAppointmentByScheduledDateAndChildId(ContentValues cv,String childId ,String date) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.VACCINATION_APPOINTMENT, cv, SQLHandler.VaccinationAppointmentColumns.CHILD_ID + "=? AND "+SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE+"=?",
                    new String[]{
                            childId,date
                    });
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public List<VaccinationQueueObject> getAllVaccinationQueueObjects() {
        //Container
        List<VaccinationQueueObject> vaccinationQueueObjectList = new ArrayList<VaccinationQueueObject>();


        return vaccinationQueueObjectList;
    }

    public void updateWeight(ContentValues cv, String barcode) {
        SQLiteDatabase sd = getWritableDatabase();
        sd.beginTransaction();
        try {
            sd.update(Tables.CHILD_WEIGHT, cv, "CHILD_BARCODE=?", new String[]{barcode});
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            throw e;
        } finally {
            sd.endTransaction();
        }

    }

    public void updateStockBalance(ContentValues cv, String lot_id) {
        SQLiteDatabase sd = getWritableDatabase();
        sd.beginTransaction();
        try {
            sd.update(Tables.HEALTH_FACILITY_BALANCE, cv, "lot_id=?", new String[]{lot_id});
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            throw e;
        } finally {
            sd.endTransaction();
        }

    }

    public void updateAdministerVaccineDoneStatus(ContentValues cv, String status, String dose) {
        SQLiteDatabase sd = getWritableDatabase();
        Boolean result = false;

        do {
            try {
                Thread.sleep(1000);
                sd.beginTransaction();
                try {
                    sd.update(Tables.VACCINATION_EVENT, cv, "APPOINTMENT_ID=? AND DOSE_ID=?", new String[]{status, dose});
                    sd.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    throw e;
                } finally {
                    sd.endTransaction();
                    result = true;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }while (!result);

    }

    public boolean removeChild(String childId) {
        SQLiteDatabase sd = getWritableDatabase();
        String[] whereArgs = new String[]{String.valueOf(childId)};

        int result = sd.delete(Tables.CHILD, BaseColumns._ID
                + "= ? ", whereArgs);

        return (result > 0);
    }

    public boolean removeChildWeight(int weightId) {
        SQLiteDatabase sd = getWritableDatabase();
        String[] whereArgs = new String[]{String.valueOf(weightId)};

        int result = sd.delete(Tables.CHILD_WEIGHT, BaseColumns._ID
                + "= ? ", whereArgs);

        return (result > 0);
    }

//    public long addVaccinationEvent(ContentValues cv) {
//
//        // RETRIEVE WRITEABLE DATABASE AND INSERT
//        SQLiteDatabase sd = getWritableDatabase();
//        long result = sd.insert(Tables.VACCINATION_EVENT, null, cv);
//        return result;
//    }

    public long addVaccinationAppointment(ContentValues cv) {
        long result=-1;

        SQLiteDatabase sd = getWritableDatabase();
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.VACCINATION_APPOINTMENT, null, cv);
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result=-1;
            throw e;
        } finally {
            sd.endTransaction();
            return result;
        }

    }

    public boolean removeVaccinationAppointment(int vaccinationAppointmentId) {
        SQLiteDatabase sd = getWritableDatabase();
        String[] whereArgs = new String[]{String.valueOf(vaccinationAppointmentId)};

        int result = sd.delete(Tables.VACCINATION_APPOINTMENT, BaseColumns._ID
                + "= ? ", whereArgs);

        return (result > 0);
    }

    //@Arinela query search

    public boolean removeVaccinationEvent(int vaccinationEventId) {
        SQLiteDatabase sd = getWritableDatabase();
        String[] whereArgs = new String[]{String.valueOf(vaccinationEventId)};

        int result = sd.delete(Tables.VACCINATION_EVENT, BaseColumns._ID
                + "= ? ", whereArgs);

        return (result > 0);
    }

    /**
     * @param firtsname
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
     */
    public ArrayList<Child> searchChild(String barcode, String firtsname, String firstname2,String motherFistname, String DobFrom, String DobTo, String tempId, String surname, String motherSurname,
                                        String placeID, String healthID, String villageId, String statusId, String beginIndex) {
        boolean thereIsData = false;

        Log.d("PANDA", barcode);

        String selectQuery = "";
        if(!healthID.equals("")){
            thereIsData=true;
            selectQuery+="HEALTH_FACILITY_ID like '%" + healthID +"% AND ";
        }

        if(!firtsname.equals("")){
            thereIsData=true;
            selectQuery+="FIRSTNAME1 like '%" + firtsname +  "%' AND ";
        }

        if(!firstname2.equals("")){
            thereIsData=true;
            selectQuery+="FIRSTNAME2 like '%" + firstname2 +  "%' AND ";
        }

        if(!motherFistname.equals("")){
            thereIsData=true;
            selectQuery+="MOTHER_FIRSTNAME like '%" + motherFistname +  "%' AND ";
        }

        if(!tempId.equals("")){
            thereIsData=true;
            selectQuery+="TEMP_ID like '%" + tempId +  "%' AND ";
        }
        if(!surname.equals("")){
            thereIsData=true;
            selectQuery+="LASTNAME1 like '%" + surname +  "%' AND ";
        }

        if(!motherSurname.equals("")){
            thereIsData=true;
            selectQuery+="MOTHER_LASTNAME like '%" + motherSurname +  "%' AND ";
        }

        if(!placeID.equals("")){
            thereIsData=true;
            selectQuery+="BIRTHPLACE_ID like '%" + placeID +  "%' AND ";
        }

        if(!villageId.equals("")){
            thereIsData=true;
            selectQuery+="DOMICILE_ID like '%" + villageId +  "%' AND ";
        }

        if(!statusId.equals("")){
            thereIsData=true;
            selectQuery+="STATUS_ID like '%" + statusId +  "%' AND ";
        }

        if(!barcode.equals("")){
            thereIsData=true;
            selectQuery+="BARCODE_ID like '%" + barcode +  "%' AND ";
        }

        if(!DobFrom.equals("") || !DobTo.equals("")){
            thereIsData=true;
            selectQuery+= ((!DobFrom.equals("")) ? " datetime(substr(BIRTHDATE,7,10), 'unixepoch') >= datetime( '" + DobFrom + "','unixepoch')" : "") +
                    ((!DobTo.equals("")) ? " datetime(substr(BIRTHDATE,7,10), 'unixepoch') <= datetime( '" + DobTo + "','unixepoch')" : "");
        }else if(thereIsData){
            int startIndex = selectQuery.length()-5;
            int endIndex = selectQuery.length()-1;
            String replacement = "";
            String toBeReplaced = selectQuery.substring(startIndex, endIndex);


            StringBuilder str = new StringBuilder(selectQuery);
            str.replace(startIndex, endIndex, "");
            selectQuery = str.toString();
        }


        String sql;
        if(thereIsData){
            sql = "SELECT  * FROM " + Tables.CHILD + " WHERE "+selectQuery+
            " ORDER BY BIRTHDATE DESC "+
                    " LIMIT "+beginIndex+", 10 ; ";
        }else {
            sql = "SELECT  * FROM " + Tables.CHILD + ""+selectQuery+
                    " ORDER BY BIRTHDATE DESC "+
                    " LIMIT "+beginIndex+", 10 ; ";
        }

        Log.e("Query ", sql);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        ArrayList<Child> children = null;
        if (cursor.moveToFirst()) {
            children = new ArrayList<>();

            do {
                Child child = new Child();
                child.setMotherFirstname(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_FIRSTNAME)));
                child.setMotherLastname(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_LASTNAME)));
                child.setBirthdate(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BIRTHDATE)).substring(6, 19));
                child.setFirstname1(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.FIRSTNAME1)));
                child.setFirstname2(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.FIRSTNAME2)));
                child.setLastname1(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.LASTNAME1)));
                child.setGender(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.GENDER)).equals("true") ? "Male" : "Female");
                child.setMotherLastname(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_LASTNAME)));
                child.setDomicileId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.DOMICILE_ID)));
                child.setDomicile(getVillageName(child.getDomicileId()));
                child.setHealthcenterId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.HEALTH_FACILITY_ID)));
                child.setHealthcenter(getHealthCenterName(child.getHealthcenterId()));
                child.setBarcodeID(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BARCODE_ID)));
                child.setId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.ID)));
                child.setChildRegistryYear(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.CHILD_REGISTRY_YEAR)));
                child.setMotherHivStatus(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_VVU_STS)));
                child.setMotherTT2Status(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_TT2_STS)));
                child.setChildCumulativeSn(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.CUMULATIVE_SERIAL_NUMBER)));
                children.add(child);
            } while (cursor.moveToNext());

            cursor.close();
            return children;
        }
        cursor.close();
        return children;
    }


    /**
     * @param birthdate
     * @param gender
     * @return
     * @Arinela This method check if in our db have another child with this properties,and special part is date that i use this method to convert only yyyy-MM-dd
     */

    public ArrayList<Child> searchIfChildIsRegisteredFromMaternityApp(String mothersfirstName, String mothersLastName, long birthdate, String gender) {
        String selectQuery = "SELECT * FROM child" +
                " WHERE " +
                SQLHandler.ChildColumns.MOTHER_FIRSTNAME+" = '"+mothersfirstName+"' AND " +
                SQLHandler.ChildColumns.MOTHER_LASTNAME+" = '"+mothersLastName+"' AND " +
                "strftime('%Y-%m-%d',substr(BIRTHDATE ,7,10), 'unixepoch') = strftime('%Y-%m-%d','" + (birthdate/1000) + "','unixepoch')" +
                " AND "+ SQLHandler.ChildColumns.GENDER+" = '" + gender + "' ";

        Log.d(TAG,"searchMaternity = "+selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<Child> children = null;
        if (cursor.moveToFirst()) {
            children = new ArrayList<>();
            do {
                Child child = new Child();
                child.setMotherFirstname(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_FIRSTNAME)));
                child.setMotherLastname(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_LASTNAME)));
                child.setBirthdate(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BIRTHDATE)).substring(6, 19));
                child.setFirstname1(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.FIRSTNAME1)));
                child.setFirstname2(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.FIRSTNAME2)));
                child.setLastname1(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.LASTNAME1)));
                child.setGender(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.GENDER)).equals("true") ? "Male" : "Female");
                child.setMotherLastname(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_LASTNAME)));
                child.setDomicileId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.DOMICILE_ID)));
                child.setDomicile(getVillageName(child.getDomicileId()));
                child.setHealthcenterId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.HEALTH_FACILITY_ID)));
                child.setHealthcenter(getHealthCenterName(child.getHealthcenterId()));
                child.setBarcodeID(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BARCODE_ID)));
                child.setId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.ID)));
                child.setChildRegistryYear(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.CHILD_REGISTRY_YEAR)));
                child.setMotherHivStatus(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_VVU_STS)));
                child.setMotherTT2Status(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.MOTHER_TT2_STS)));
                child.setChildCumulativeSn(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.CUMULATIVE_SERIAL_NUMBER)));
                children.add(child);
                Log.d(TAG,"child was registered from maternity app");
            } while (cursor.moveToNext());

            cursor.close();
            return children;
        }
        cursor.close();
        return children;
    }


    /**
     * @param villageID
     * @return
     * @Arinela
     */

    public String getVillageName(String villageID) {
        //Query on Child Table
        String selectQuery = "SELECT NAME From " + Tables.PLACE + " WHERE ID =?";
        String hfid = villageID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { villageID});
        if (cursor.moveToFirst()) {
            hfid = cursor.getString(0);
        }
        cursor.close();
        return hfid;
    }

    /**
     * @param healthID
     * @return
     * @Arinela
     */

    public String getHealthCenterName(String healthID) {
        //Query on Child Table
        String selectQuery = "SELECT NAME From " + Tables.HEALTH_FACILITY + " WHERE ID ='" + healthID + "'";
        String hfid = healthID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            hfid = cursor.getString(0);
        }
        cursor.close();
        return hfid;
    }

    /**
     * @param cv values to be added
     * @return returns a number greater than -1 if transaction was successful or -1 if not
     * @Arinela Method adds child to child table
     */
    public long registerChild(ContentValues cv) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT
        cv.put(GIISContract.ChildColumns.UPDATED, 1);
        //for now insert random key
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.CHILD, null, cv);

            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }


    public boolean isBarcodeInChildTable(String barcode) {
        String selectQuery = "SELECT * FROM child" +
                " WHERE BARCODE_ID = '" + barcode + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public boolean isChildIDInChildTable(String ID) {
        String selectQuery = "SELECT ID FROM child" +
                " WHERE ID = '" + ID + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public boolean isChildBarcodeIDInChildTable(String ID) {
        String selectQuery = "SELECT BARCODE_ID FROM child" +
                " WHERE BARCODE_ID = '" + ID + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public boolean isChildSupplementedVitAToday(String childId) {
        try {
            String query = "SELECT * FROM " + Tables.CHILD_SUPPLEMENTS +
                    " WHERE " + SQLHandler.ChildSupplementsColumns.CHILD_ID + " = '" +
                    childId + "' AND " + SQLHandler.ChildSupplementsColumns.DATE + " = date('now', 'start of day') AND " +
                    SQLHandler.ChildSupplementsColumns.VitA + " = 'true'";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                String vitA = cursor.getString(cursor.getColumnIndex(SQLHandler.ChildSupplementsColumns.VitA));
                if (vitA != null && vitA.equalsIgnoreCase("true"))
                    return true;
                else
                    return false;
            } else {
                cursor.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isChildSupplementedMebendezolrToday(String childId) {
        String query = "SELECT * FROM " + Tables.CHILD_SUPPLEMENTS +
            " WHERE " + SQLHandler.ChildSupplementsColumns.CHILD_ID + " = '" +
            childId + "' AND " + SQLHandler.ChildSupplementsColumns.DATE + " = date('now', 'start of day') AND " +
            SQLHandler.ChildSupplementsColumns.MEBENDEZOLR + " = 'true'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        try {


            if (cursor.moveToFirst()) {
                String mebende = cursor.getString(cursor.getColumnIndex(SQLHandler.ChildSupplementsColumns.MEBENDEZOLR));
                if (mebende != null && mebende.equalsIgnoreCase("true")) {

                    cursor.close();
                    return true;
                }
                else{


                    cursor.close();
                    return false;}
            } else {
                cursor.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return false;
    }

    /**
     * Funciont that inserts in the ChildSupplements if the child did have the today supplements
     *
     * @param childId
     * @param vitA
     * @param mebendezolr
     * @param modificationUserId
     * @return - the id of the new inserted element
     */
    public long inserTodaySupplements(String childId, boolean vitA, boolean mebendezolr, String modificationUserId) {

        long result=-1;

        SQLiteDatabase sd = getWritableDatabase();
        sd.beginTransaction();
        try {
            sd.delete(Tables.CHILD_SUPPLEMENTS,
                    SQLHandler.ChildSupplementsColumns.CHILD_ID + " = '" +
                            childId + "' AND " + SQLHandler.ChildSupplementsColumns.DATE + " = date('now', 'start of day')", null);

            ContentValues contentValues = new ContentValues();
            if (vitA)
                contentValues.put(SQLHandler.ChildSupplementsColumns.VitA, "true");
            if (mebendezolr)
                contentValues.put(SQLHandler.ChildSupplementsColumns.MEBENDEZOLR, "true");

            if (contentValues.size() > 0) {
                String uuid =UUID.randomUUID().toString();
                uuid = uuid.replace('\'','a');
                uuid = uuid.replace('\"','a');
                contentValues.put(SQLHandler.ChildSupplementsColumns.ID,uuid );
                contentValues.put(SQLHandler.ChildSupplementsColumns.CHILD_ID, childId);
                contentValues.put(SQLHandler.ChildSupplementsColumns.MODIFIED_BY, modificationUserId);
                result = sd.insert(Tables.CHILD_SUPPLEMENTS, null, contentValues);
            } else {
                result= -1;
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result=-1;
            throw e;
        } finally {
            sd.endTransaction();
            return result;
        }


    }

    /**
     * Changes the id of the specified row with the new id that we got from the server.
     *
     * @param rowId
     * @param newChildSupplementsId
     * @return
     */
    public boolean updateChildSupplementsNewid(long rowId, long newChildSupplementsId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLHandler.ChildSupplementsColumns.ID, newChildSupplementsId);
        SQLiteDatabase db = this.getReadableDatabase();



        boolean result=false;

        SQLiteDatabase sd = getWritableDatabase();
        sd.beginTransaction();
        try {

            int rowsAffected = db.update(Tables.CHILD_SUPPLEMENTS, contentValues, BaseColumns._ID + "=?",
                    new String[]{
                            rowId + ""
                    });
            if (rowsAffected > 0) {
                result =  true;
            } else {
                result =  false;
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result=false;
            throw e;
        } finally {
            sd.endTransaction();
            return result;
        }


    }


    public ArrayList<ImmunizationCardItem> getImmunizationCard(String childId) {
        String selectQuery = "SELECT d." + SQLHandler.DoseColumns.FULLNAME +
                ", i." + SQLHandler.ItemLotColumns.LOT_NUMBER +
                ", h." + SQLHandler.HealthFacilityColumns.NAME +
                ", v." + SQLHandler.VaccinationEventColumns.VACCINATION_DATE +
                ", v." + SQLHandler.VaccinationEventColumns.VACCINATION_STATUS +
                ", v." + SQLHandler.VaccinationEventColumns.APPOINTMENT_ID +
                ", n." + SQLHandler.NonVaccinationReasonColumns.NAME + " as NONVACCINREASON " +
                " FROM " +
                Tables.VACCINATION_EVENT + " v" +
                " INNER JOIN " + Tables.DOSE + " d on v." + SQLHandler.VaccinationEventColumns.DOSE_ID + " = d." + SQLHandler.DoseColumns.ID +
                " INNER JOIN " + Tables.HEALTH_FACILITY + " h on v." + SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID + " = h." + SQLHandler.HealthFacilityColumns.ID +
                " LEFT JOIN " + Tables.ITEM_LOT + " i on v." + SQLHandler.VaccinationEventColumns.VACCINE_LOT_ID + " = i." + SQLHandler.ItemLotColumns.ID +
                " LEFT JOIN " + Tables.NONVACCINATION_REASON + " n on v." + SQLHandler.VaccinationEventColumns.NONVACCINATION_REASON_ID + " = n." + SQLHandler.NonVaccinationReasonColumns.ID +
                " WHERE v." + SQLHandler.VaccinationEventColumns.CHILD_ID + " = '" + childId + "'" +
                " ORDER BY v." + SQLHandler.VaccinationEventColumns.VACCINATION_DATE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<ImmunizationCardItem> imuImmunizationCardItems = new ArrayList<>();
        ;
        if (cursor.moveToFirst()) {

            do {
                ImmunizationCardItem immunizationCardItem = new ImmunizationCardItem();
                immunizationCardItem.setVacineDose(cursor.getString(cursor.getColumnIndex(SQLHandler.DoseColumns.FULLNAME)));
                immunizationCardItem.setVaccineLot(cursor.getString(cursor.getColumnIndex(SQLHandler.ItemLotColumns.LOT_NUMBER)));
                immunizationCardItem.setHealthCenterName(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityColumns.NAME)));
                immunizationCardItem.setAppointementId(cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID)));
                String dateStr = cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.VACCINATION_DATE));
                if (dateStr != null && !dateStr.isEmpty())
                    immunizationCardItem.setVaccinationDate(BackboneActivity.dateParser(dateStr));
                immunizationCardItem.setVaccinationStringFormatted(dateStr);
                String status = cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.VACCINATION_STATUS));
                if (status.equalsIgnoreCase("true")) {
                    immunizationCardItem.setDone(true);
                } else {
                    immunizationCardItem.setDone(false);
                }
                immunizationCardItem.setNonVaccinaitonReason(cursor.getString(cursor.getColumnIndex("NONVACCINREASON")));
                imuImmunizationCardItems.add(immunizationCardItem);
            } while (cursor.moveToNext());

            cursor.close();
            return imuImmunizationCardItems;
        }
        cursor.close();
        return imuImmunizationCardItems;
    }

    /**
     * Kjo eshte nje metode e cila na kthen te dhenat per last vaccination appointement per aefi
     * qe te plotesojme tek {@link}
     *
     * @return
     */
    public ArrayList<AefiListItem> getAefiLastVaccinationAppointement(String childId) {
        String selectQuery = "SELECT v." + SQLHandler.VaccinationEventColumns.APPOINTMENT_ID + " , " +
                "v." + SQLHandler.VaccinationEventColumns.VACCINATION_DATE + " , " +
                "h." + SQLHandler.HealthFacilityColumns.NAME + " , " +
                "v." + SQLHandler.VaccinationEventColumns.VACCINATION_STATUS + " , " +
                "vap." + SQLHandler.VaccinationAppointmentColumns.AEFI + " , " +
                "vap." + SQLHandler.VaccinationAppointmentColumns.AEFI_DATE + " , " +
                "vap." + SQLHandler.VaccinationAppointmentColumns.NOTES + " " +
                "FROM " + Tables.VACCINATION_EVENT + " v inner join " +
                Tables.HEALTH_FACILITY + " h on v." + SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID + " = h." +
                SQLHandler.HealthFacilityColumns.ID + " inner join " + Tables.VACCINATION_APPOINTMENT + " vap on v." +
                SQLHandler.VaccinationEventColumns.APPOINTMENT_ID + " = vap." + SQLHandler.VaccinationAppointmentColumns.ID +
                " where " +
                "v." + SQLHandler.VaccinationEventColumns.CHILD_ID + " = '" + childId + "' AND " +
                "v." + SQLHandler.VaccinationEventColumns.VACCINATION_STATUS + " = 'true' AND " +
                "(vap." + SQLHandler.VaccinationAppointmentColumns.AEFI + " = 'false' or " +
                "vap." + SQLHandler.VaccinationAppointmentColumns.AEFI + " is null)" +
                " GROUP BY v." + SQLHandler.VaccinationEventColumns.APPOINTMENT_ID +
                ", v." + SQLHandler.VaccinationEventColumns.VACCINATION_DATE +
                ", h." + SQLHandler.HealthFacilityColumns.NAME +
                ", v." + SQLHandler.VaccinationEventColumns.VACCINATION_STATUS +
                ", vap." + SQLHandler.VaccinationAppointmentColumns.AEFI +
                ", vap." + SQLHandler.VaccinationAppointmentColumns.AEFI_DATE +
                ", vap." + SQLHandler.VaccinationAppointmentColumns.NOTES +
                " ORDER BY " + SQLHandler.VaccinationEventColumns.VACCINATION_DATE + " DESC LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            ArrayList<AefiListItem> aefiListItems = new ArrayList<AefiListItem>();
            do {
                AefiListItem aefiListItem = new AefiListItem();
                aefiListItem.setAppointementId(cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID)));
                aefiListItem.setVaccines(getAefiVaccinessInString(childId,
                        cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID))));
                aefiListItem.setHealthFacilityName(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityColumns.NAME)));
                if (cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.VACCINATION_STATUS)).equalsIgnoreCase("true")) {
                    aefiListItem.setDone(true);
                } else {
                    aefiListItem.setDone(false);
                }
                String aefiStr = cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationAppointmentColumns.AEFI));
                if (aefiStr != null && aefiStr.equalsIgnoreCase("true")) {
                    aefiListItem.setAefi(true);
                } else {
                    aefiListItem.setAefi(false);
                }
                String dateStr = cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.VACCINATION_DATE));
                if (dateStr != null && !dateStr.isEmpty())
                    aefiListItem.setVaccinationDate(BackboneActivity.dateParser(dateStr));
                String dateStrAefi = cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationAppointmentColumns.AEFI_DATE));
                if (dateStrAefi != null && !dateStrAefi.isEmpty())
                    aefiListItem.setAefiDate(BackboneActivity.dateParser(dateStrAefi));
                aefiListItem.setNotes(cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationAppointmentColumns.NOTES)));
                aefiListItems.add(aefiListItem);
            } while (cursor.moveToNext());
            return aefiListItems;
        } else
            return null;
    }


    /**
     * Kjo eshte nje metode e cila na kthen te dhenat per vaccination appointement per aefi
     * qe te plotesojme tek {@link}
     *
     * @return
     */
    public ArrayList<AefiListItem> getAefiVaccinationAppointement(String childId) {
        String selectQuery = "SELECT v." + SQLHandler.VaccinationEventColumns.APPOINTMENT_ID + " , " +
                "v." + SQLHandler.VaccinationEventColumns.VACCINATION_DATE + " , " +
                "h." + SQLHandler.HealthFacilityColumns.NAME + " , " +
                "v." + SQLHandler.VaccinationEventColumns.VACCINATION_STATUS + " , " +
                "vap." + SQLHandler.VaccinationAppointmentColumns.AEFI + " , " +
                "vap." + SQLHandler.VaccinationAppointmentColumns.AEFI_DATE + " , " +
                "vap." + SQLHandler.VaccinationAppointmentColumns.NOTES + " " +
                "FROM " + Tables.VACCINATION_EVENT + " v inner join " +
                Tables.HEALTH_FACILITY + " h on v." + SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID + " = h." +
                SQLHandler.HealthFacilityColumns.ID + " inner join " + Tables.VACCINATION_APPOINTMENT + " vap on v." +
                SQLHandler.VaccinationEventColumns.APPOINTMENT_ID + " = vap." + SQLHandler.VaccinationAppointmentColumns.ID +
                " where " +
                "v." + SQLHandler.VaccinationEventColumns.CHILD_ID + " = '" + childId + "' AND " +
                "(v." + SQLHandler.VaccinationEventColumns.VACCINATION_STATUS + " = 'true' OR " +
                "v." + SQLHandler.VaccinationEventColumns.NONVACCINATION_REASON_ID + " <> 0)" +
                " GROUP BY v." + SQLHandler.VaccinationEventColumns.APPOINTMENT_ID +
                ", v." + SQLHandler.VaccinationEventColumns.VACCINATION_DATE +
                ", h." + SQLHandler.HealthFacilityColumns.NAME +
                ", v." + SQLHandler.VaccinationEventColumns.VACCINATION_STATUS +
                ", vap." + SQLHandler.VaccinationAppointmentColumns.AEFI +
                ", vap." + SQLHandler.VaccinationAppointmentColumns.AEFI_DATE +
                ", vap." + SQLHandler.VaccinationAppointmentColumns.NOTES +
                " ORDER BY " + SQLHandler.VaccinationEventColumns.VACCINATION_DATE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            ArrayList<AefiListItem> aefiListItems = new ArrayList<AefiListItem>();
            do {
                Log.d("eafi","appointment_id="+cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID))+" AND childId = "+childId);
                AefiListItem aefiListItem = new AefiListItem();
                aefiListItem.setAppointementId(cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID)));

                String vaccineName = getAefiVaccinessInString(childId, cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID)));
                Log.d("eafi","vaccines names = "+vaccineName);

                aefiListItem.setVaccines(vaccineName);
                aefiListItem.setHealthFacilityName(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityColumns.NAME)));
                if (cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.VACCINATION_STATUS)).equalsIgnoreCase("true")) {
                    aefiListItem.setDone(true);
                } else {
                    aefiListItem.setDone(false);
                }
                String aefiStr = cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationAppointmentColumns.AEFI));
                if (aefiStr != null && aefiStr.equalsIgnoreCase("true")) {
                    aefiListItem.setAefi(true);
                } else {
                    aefiListItem.setAefi(false);
                }
                String dateStr = cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationEventColumns.VACCINATION_DATE));
                if (dateStr != null && !dateStr.isEmpty())
                    aefiListItem.setVaccinationDate(BackboneActivity.dateParser(dateStr));
                String dateStrAefi = cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationAppointmentColumns.AEFI_DATE));
                if (dateStrAefi != null && !dateStrAefi.isEmpty())
                    aefiListItem.setAefiDate(BackboneActivity.dateParser(dateStrAefi));
                aefiListItem.setNotes(cursor.getString(cursor.getColumnIndex(SQLHandler.VaccinationAppointmentColumns.NOTES)));
                aefiListItems.add(aefiListItem);
            } while (cursor.moveToNext());
            return aefiListItems;
        } else
            return null;
    }


    private String getAefiVaccinessInString(String childId, String appointementId) {
        String selectQuery = "SELECT d." + SQLHandler.DoseColumns.FULLNAME +
                " FROM " + Tables.VACCINATION_EVENT + " ve inner join " +
                Tables.DOSE + " d on ve." + SQLHandler.VaccinationEventColumns.DOSE_ID + " = d." +
                SQLHandler.DoseColumns.ID + " inner join " + Tables.VACCINATION_APPOINTMENT + " va on ve." +
                SQLHandler.VaccinationEventColumns.APPOINTMENT_ID + " = va." + SQLHandler.VaccinationAppointmentColumns.ID +
                " where " +
                "ve." + SQLHandler.VaccinationEventColumns.CHILD_ID + " = '" + childId + "' AND " +
                "ve." + SQLHandler.VaccinationEventColumns.APPOINTMENT_ID + " = '" + appointementId + "' AND " +
                "ve." + SQLHandler.VaccinationEventColumns.VACCINATION_STATUS + " = 'true'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        StringBuilder sb = new StringBuilder("");
        if (cursor.moveToFirst()) {
            do {
                sb.append(cursor.getString(cursor.getColumnIndex(SQLHandler.DoseColumns.FULLNAME)));
            } while (cursor.moveToNext());{
                cursor.close();
            return sb.toString();
            }
        } else{
           cursor.close();
            return null;
    }}


    /**
     * funciton that updates a vaccination apointement for the given id of the vaccination appointement
     *
     * @param cv
     * @param id
     * @return
     */
    public long updateVaccinationAppointementById(ContentValues cv, String id) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.VACCINATION_APPOINTMENT, cv, SQLHandler.VaccinationAppointmentColumns.ID + "=?",
                    new String[]{
                            id
                    });
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

// ******** Fillojne metodat per regjistrimin e child,ju lutem mos i ndryshoni  *******

    /**
     * @param cv,recordTempId
     * @return
     * @Arinela This method update record of register child with child id
     */
    public boolean updateChildTableWithChildID(ContentValues cv, String childTempId) {
        SQLiteDatabase sd = getWritableDatabase();
       boolean result = false;
        sd.beginTransaction();
        try {
            result = sd.update(Tables.CHILD, cv, GIISContract.ChildTable.ID + " = ?", new String[]{childTempId}) > 0;
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = false;
        } finally {
            sd.endTransaction();
            return result;
        }
    }


    //@Arinela query getDosesByBirthDate

    /**
     * @param birthDate
     * @return
     */
    public ArrayList<Dose> getDosesByDates(long birthDate) {
        //Query on Child Table
        String selectQuery = "SELECT  d.*  from dose as d join scheduled_vaccination as sv " +
                " on d.SCHEDULED_VACCINATION_ID = sv.ID  " +
                " where  sv.ENTRY_DATE <= datetime('" + birthDate + "', 'unixepoch')  and sv.EXIT_DATE  >= datetime('" + birthDate + "', 'unixepoch') " +
                " or sv.EXIT_DATE = null or datetime(substr(sv.EXIT_DATE,7,10), 'unixepoch') < datetime('1970-01-01')" +
                " order by d.ID";


        Log.e("Query ", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<Dose> doses = null;
        if (cursor.moveToFirst()) {
            doses = new ArrayList<>();

            do {
                Dose dose = new Dose();
                dose.setId(cursor.getString(cursor.getColumnIndex(GIISContract.DoseColumns.ID)));
                dose.setScheduledVaccinationId(cursor.getString(cursor.getColumnIndex(GIISContract.DoseColumns.SCHEDULED_VACCINATION_ID)));
                dose.setDoseNumber(cursor.getString(cursor.getColumnIndex(GIISContract.DoseColumns.DOSE_NUMBER)));
                dose.setFullname(cursor.getString(cursor.getColumnIndex(GIISContract.DoseColumns.FULLNAME)));
                dose.setAgeDefinitionId(cursor.getString(cursor.getColumnIndex(GIISContract.DoseColumns.AGE_DEFINITON_ID)));
                dose.setFromAgeDefId(cursor.getString(cursor.getColumnIndex(GIISContract.DoseColumns.FROM_AGE_DEFINITON_ID)));
                dose.setToAgeDefId(cursor.getString(cursor.getColumnIndex(GIISContract.DoseColumns.TO_AGE_DEFINITON_ID)));
                doses.add(dose);
            } while (cursor.moveToNext());

            cursor.close();
            return doses;
        }
        cursor.close();
        return doses;
    }


    /**
     * @param childId
     * @return the childId but i need only this field,birthdate,healthcenterid
     * @Arinela method used to get partial child data by passing the child id of the child
     */
    public Child getChildIdByChildID(String childId) {
        //Query on Child Table
        String selectQuery = "SELECT " + SQLHandler.ChildColumns.HEALTH_FACILITY_ID + ", substr(" + SQLHandler.ChildColumns.BIRTHDATE + ",7,10) as " +
                SQLHandler.ChildColumns.BIRTHDATE + " FROM " + Tables.CHILD + " WHERE ID = '" + childId + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            Child child = new Child();
            child.setBirthdate(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BIRTHDATE)));
            child.setHealthcenterId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.HEALTH_FACILITY_ID)));
            cursor.close();

            return child;
        }
        cursor.close();
        return null;
    }


    /**
     * @param barcode
     * @return the child
     * @Arinela method used to get partial child data by passing the child id of the child
     */
    public Child getChildByBarcode(String barcode) {
        //Query on Child Table
        String selectQuery = "SELECT * FROM " + Tables.CHILD + " WHERE "+ SQLHandler.ChildColumns.BARCODE_ID+"= '" + barcode + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            Child child = new Child();
            child.setBirthdate(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BIRTHDATE)));
            child.setHealthcenterId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.HEALTH_FACILITY_ID)));
            child.setDomicileId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.DOMICILE_ID)));
            cursor.close();

            return child;
        }
        cursor.close();
        return null;
    }

    /**
     * @param id
     * @return the child
     * @Arinela method used to get partial child data by passing the child id of the child
     */
    public Child getChildById(String id) {
        //Query on Child Table
        String selectQuery = "SELECT * FROM " + Tables.CHILD + " WHERE "+ SQLHandler.ChildColumns.ID+"= '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            Child child = new Child();
            child.setBirthdate(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BIRTHDATE)));
            child.setHealthcenterId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.HEALTH_FACILITY_ID)));
            child.setDomicileId(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.DOMICILE_ID)));
            child.setBarcodeID(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BARCODE_ID)));
            child.setBirthdate(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.BIRTHDATE)));
            child.setGender(cursor.getString(cursor.getColumnIndex(SQLHandler.ChildColumns.GENDER)));
            cursor.close();

            return child;
        }
        cursor.close();
        return null;
    }

    public int getDayesInAgeDefinitions(String ageDefinitionsID) {
        //Query on Child Table
        String selectQuery = "SELECT " + SQLHandler.AgeDefinitionsColumns.DAYS + " FROM " + Tables.AGE_DEFINITIONS + " WHERE ID = '" + ageDefinitionsID + "'";
        int days = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            days = Integer.parseInt(cursor.getString(0));
            cursor.close();
            return days;
        }
        cursor.close();
        return days;

    }

    public int InsertVaccinationsForChild(String childId, String userId) {
        try {
            Child child = getChildIdByChildID(childId);

            List<Dose> vaccineDoseList = getDosesByDates(Long.parseLong(child.getBirthdate()));

            for (Dose dose : vaccineDoseList) {

                String uuid = UUID.randomUUID().toString();
                uuid = uuid.replace('\'','a');
                uuid = uuid.replace('\"','a');
                VaccinationAppointment o = new VaccinationAppointment();

                o.setChildId(childId);
                o.setScheduledFacilityId(child.getHealthcenterId());
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(Long.parseLong(child.getBirthdate()) * 1000));
                c.add(Calendar.DATE, getDayesInAgeDefinitions(dose.getAgeDefinitionId()));
                String dayPlusAgeDefDays = c.getTime().getTime() + "";
                o.setScheduledDate("/Date(" + dayPlusAgeDefDays + "-0500)/");
                o.setNotes("");
                o.setModifiedOn("/Date(" + Calendar.getInstance().getTime().getTime() + "-0500)/");
                o.setModifiedBy(userId);


                ContentValues values = new ContentValues();
                values.put(SQLHandler.VaccinationAppointmentColumns.ID, uuid);
                values.put(SQLHandler.VaccinationAppointmentColumns.CHILD_ID, childId);
                values.put(SQLHandler.VaccinationAppointmentColumns.SCHEDULED_FACILITY_ID, o.getScheduledFacilityId());
                values.put(SQLHandler.VaccinationAppointmentColumns.SCHEDULED_DATE, o.getScheduledDate());
                values.put(SQLHandler.VaccinationAppointmentColumns.NOTES, o.getNotes());
                values.put(SQLHandler.VaccinationAppointmentColumns.MODIFIED_BY, userId);
                values.put(SQLHandler.VaccinationAppointmentColumns.MODIFIED_ON, o.getModifiedOn());
                values.put(GIISContract.VaccinationAppointmentColumns.IS_ACTIVE, "true");
                values.put("updated", 1);
                values.put("owners_username", "");
                values.put("modfied_at", o.getModifiedOn());


                List<VaccinationAppointment> list = getVaccinationAppointmentForList(childId, "/Date(" + dayPlusAgeDefDays + "-0500)/");

                int count = list.size();

                if (count == 0) {

                    long lastApp = insertVaccinationApp(values);

                    if (lastApp > 0) {
                        {

                            updateVaccinationAppointementId(uuid, lastApp + "");

                            VaccinationEvent ve = new VaccinationEvent();

                            ve.setAppointmentId(uuid);
                            ve.setChildId(childId);
                            ve.setDoseId(dose.getId());
                            ve.setHealthFacilityId(child.getHealthcenterId());
                            ve.setScheduledDate(o.getScheduledDate());
                            ve.setVaccinationDate(o.getScheduledDate());
                            ve.setNotes("");
                            ve.setVaccinationStatus("false");
                            int dosenum = Integer.parseInt(dose.getDoseNumber()) - 1;

                            ContentValues valuesEvent = new ContentValues();
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.ID, uuid);
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID, uuid);
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.CHILD_ID, childId);
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.DOSE_ID, ve.getDoseId());
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID, ve.getHealthFacilityId());
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.SCHEDULED_DATE, ve.getScheduledDate());
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.VACCINATION_DATE, ve.getVaccinationDate());
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.VACCINATION_STATUS, ve.getVaccinationStatus());
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.NONVACCINATION_REASON_ID, 0);
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.VACCINE_LOT_ID, 0);
                            valuesEvent.put("updated", 1);
                            valuesEvent.put("owners_username", "");
                            valuesEvent.put("modfied_at", o.getModifiedOn());


                            if (dosenum > 0 && otherDose(childId, dose.getScheduledVaccinationId(), dosenum)) {
                                ve.setIsActive("false");
                            } else {
                                ve.setIsActive("true");
                            }

                            valuesEvent.put(SQLHandler.VaccinationEventColumns.IS_ACTIVE, ve.getIsActive());

                            ve.setModifiedOn("/Date(" + Calendar.getInstance().getTime().getTime() + "-0500)/");
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.MODIFIED_ON, ve.getModifiedOn());

                            ve.setModifiedBy(userId);
                            valuesEvent.put(SQLHandler.VaccinationEventColumns.MODIFIED_BY, ve.getModifiedBy());

                            long i = insertVaccinationEvent(valuesEvent);
                            if (i < 0) {
                                deleteChildFromVaccApp(childId);
                                removeChildFromChildTable(childId);
                                return 0;
                            }
                        }
                    } else {
                        removeChildFromChildTable(childId);
                        return 0;
                    }
                } else {
                    VaccinationAppointment va = list.get(0);
                    VaccinationEvent ve = new VaccinationEvent();

                    ve.setAppointmentId(va.getId());
                    ve.setChildId(childId);
                    ve.setDoseId(dose.getId());
                    ve.setHealthFacilityId(child.getHealthcenterId());
                    ve.setScheduledDate(o.getScheduledDate());
                    ve.setVaccinationDate(o.getScheduledDate());
                    ve.setNotes("");
                    ve.setVaccinationStatus("false");
                    int dosenum = Integer.parseInt(dose.getDoseNumber()) - 1;

                    ContentValues valuesEvent = new ContentValues();
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.APPOINTMENT_ID, va.getId());
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.CHILD_ID, childId);
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.DOSE_ID, ve.getDoseId());
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID, ve.getHealthFacilityId());
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.SCHEDULED_DATE, ve.getScheduledDate());
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.VACCINATION_DATE, ve.getVaccinationDate());
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.VACCINATION_STATUS, ve.getVaccinationStatus());

                    valuesEvent.put(SQLHandler.VaccinationEventColumns.NONVACCINATION_REASON_ID, 0);
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.ID, uuid);
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.VACCINE_LOT_ID, 0);
                    valuesEvent.put("updated", 1);
                    valuesEvent.put("owners_username", "");
                    valuesEvent.put("modfied_at", o.getModifiedOn());

                    if (dosenum > 0 && otherDose(childId, dose.getScheduledVaccinationId(), dosenum)) {
                        ve.setIsActive("false");
                    } else {
                        ve.setIsActive("true");
                    }

                    valuesEvent.put(SQLHandler.VaccinationEventColumns.IS_ACTIVE, ve.getIsActive());

                    ve.setModifiedOn("/Date(" + Calendar.getInstance().getTime().getTime() + "-0500)/");
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.MODIFIED_ON, ve.getModifiedOn());

                    ve.setModifiedBy(userId);
                    valuesEvent.put(SQLHandler.VaccinationEventColumns.MODIFIED_BY, ve.getModifiedBy());

                    long i = insertVaccinationEvent(valuesEvent);
                    if (i < 0) {
                        deleteChildFromVaccApp(childId);
                        removeChildFromChildTable(childId);
                        return 0;
                    }
                }
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();

        }

        return 0;

    }


    public boolean updateVaccinationAppointementId(String uuid, String _id) {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase db = this.getReadableDatabase();
        contentValues.put(SQLHandler.VaccinationAppointmentColumns.ID, uuid);
        db.beginTransaction();
        try {
            db.update(Tables.VACCINATION_APPOINTMENT, contentValues, BaseColumns._ID + "=?", new String[]{_id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public boolean updateVaccinationAppointementChildId(String uuid, String childId) {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase db = this.getReadableDatabase();
        contentValues.put(SQLHandler.VaccinationAppointmentColumns.CHILD_ID, childId);
        db.beginTransaction();
        try {
            db.update(Tables.VACCINATION_APPOINTMENT, contentValues, SQLHandler.VaccinationAppointmentColumns.CHILD_ID + "=?", new String[]{uuid});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public boolean updateVaccinationAppointementOutreach(ContentValues cv, String childId, String appointment_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        try {
            db.update(Tables.VACCINATION_APPOINTMENT, cv, "ID=? AND CHILD_ID=?", new String[]{appointment_id, childId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public boolean updateVaccinationEventChildId(String uuid, String childId) {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase db = this.getReadableDatabase();
        contentValues.put(SQLHandler.VaccinationEventColumns.CHILD_ID, childId);
        db.beginTransaction();
        try {
            db.update(Tables.VACCINATION_EVENT, contentValues, SQLHandler.VaccinationEventColumns.CHILD_ID + "=?", new String[]{uuid});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            return false;
        } finally {
            db.endTransaction();
            return true;
        }
    }

    public ArrayList<VaccinationAppointment> getVaccinationAppointmentForList(String childId, String scheduledDate) {
        ArrayList<VaccinationAppointment> list = new ArrayList<>();
        //Query on Child Table
        String selectQuery = "SELECT * FROM " + Tables.VACCINATION_APPOINTMENT + " WHERE " + GIISContract.VaccinationAppointmentColumns.CHILD_ID + " = '" + childId +
                "' AND strftime('%Y-%m-%d',substr(SCHEDULED_DATE ,7,10), 'unixepoch') = strftime('%Y-%m-%d',substr('" + scheduledDate + "',7,10),'unixepoch')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                VaccinationAppointment vac = new VaccinationAppointment();
                vac.setId(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.ID)));
                vac.setChildId(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.CHILD_ID)));
                vac.setScheduledFacilityId(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.SCHEDULED_FACILITY_ID)));
                vac.setScheduledDate(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.SCHEDULED_DATE)));
                vac.setIsActive(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.IS_ACTIVE)));
                vac.setNotes(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.NOTES)));
                vac.setModifiedOn(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.MODIFIED_ON)));
                vac.setModifiedBy(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.MODIFIED_BY)));

                list.add(vac);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    public VaccinationAppointment getVaccinationAppointmentById(String id) {
        VaccinationAppointment vac = new VaccinationAppointment();
        String selectQuery = "SELECT * FROM " + Tables.VACCINATION_APPOINTMENT + " WHERE " + GIISContract.VaccinationAppointmentColumns.ID + " = '"+id+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            vac.setId(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.ID)));
            vac.setChildId(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.CHILD_ID)));
            vac.setScheduledFacilityId(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.SCHEDULED_FACILITY_ID)));
            vac.setScheduledDate(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.SCHEDULED_DATE)));
            vac.setIsActive(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.IS_ACTIVE)));
            vac.setNotes(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.NOTES)));
            vac.setModifiedOn(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.MODIFIED_ON)));
            vac.setModifiedBy(cursor.getString(cursor.getColumnIndex(GIISContract.VaccinationAppointmentColumns.MODIFIED_BY)));
        }
        cursor.close();
        return vac;
    }

    /**
     * @param childId,vacID,doseNo
     * @return true/false with Vacc Event
     * @Arinela method used to fill beans Vaccination Event,get All Vaccinations Event
     */


    private boolean otherDose(String childId, String vacID, int doseNo) {
        String selectQuery = "Select Count(*) from " + Tables.VACCINATION_EVENT + " as ve  join  " + Tables.DOSE +
                " as d  on" +
                " ve.DOSE_ID  = d.ID where d.SCHEDULED_VACCINATION_ID = '" + vacID + "' AND  DOSE_NUMBER = '" + doseNo + "' AND  CHILD_ID = '" + childId + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean result = false;
        cursor.moveToFirst();
        result = (cursor.getInt(0) > 0) ? true : false;
        cursor.close();
        return result;
    }


    /**
     * @param cv
     * @return long
     * @Arinela method used to insert vacc appointment
     */
    public long insertVaccinationApp(ContentValues cv) {

        SQLiteDatabase sd = getWritableDatabase();
        long result = -2;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.VACCINATION_APPOINTMENT, null, cv);

            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -2;
        } finally {
            sd.endTransaction();
            return result;

        }
    }

    /**
     * @param cv
     * @return long
     * @Arinela method used to insert vacc appointment
     */
    public long insertVaccinationEvent(ContentValues cv) {

        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            result = sd.insert(Tables.VACCINATION_EVENT, null, cv);

            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;

        }
    }

    /**
     * used to delete a child from table vacc appointment
     *
     * @param childId
     * @return
     */
    public boolean deleteChildFromVaccApp(String childId) {
        SQLiteDatabase db = getWritableDatabase();
        long result = -1;
        db.beginTransaction();
        try {
            result = db.delete(Tables.VACCINATION_APPOINTMENT, GIISContract.VaccinationAppointmentColumns.CHILD_ID + " = '" + childId + "'", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            db.endTransaction();
            return result > 0;
        }
    }

    /**
     * used to delete a child from table child
     *
     * @param childId
     * @return
     */
    public boolean removeChildFromChildTable(String childId) {
        SQLiteDatabase sd = getWritableDatabase();
        String[] whereArgs = new String[]{childId};
        int result = -1;
        sd.beginTransaction();
        try {
            result = sd.delete(Tables.CHILD, GIISContract.ChildColumns.ID
                    + "= ? ", whereArgs);
            sd.setTransactionSuccessful();
            deleteChildFromVaccApp(childId);
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result > 0;
        }


    }

    // ******** End method for regjister *******

    //**** Begin check and insert update Birthplace and Domicile id that are found in children and not in places table

    /**
     * This method adds a place in the table "place" when the place was not found in the original places table and it was in "child" table
     *
     * @param cv      ContentValues
     * @param placeId the id of the place we got from the server
     * @return
     */
    public long addPlacesThatWereNotInDB(ContentValues cv, String placeId) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT or UPDATE
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isIdPresentInPlaces(placeId)) {
                result = sd.insert(Tables.PLACE, null, cv);
            } else {
                result = sd.update(Tables.PLACE, cv, SQLHandler.PlaceColumns.ID + "=?", new String[]{placeId});
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            Log.e("addPlaceThatWereNotInDB", "result : " + result);
            return result;
        }
    }


    private boolean isIdPresentInPlaces(String id) {
        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.PLACE + " WHERE ID = '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    private boolean isIdPresentInStatus(String id) {
        String selectQuery = "SELECT  * FROM " + Tables.STATUS + " WHERE ID = '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }
    private boolean isIdPresentInWeight(String id) {
        String selectQuery = "SELECT  * FROM " + Tables.WEIGHT + " WHERE ID = '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }
    private boolean isIdPresentInItemLot(String id) {
        String selectQuery = "SELECT  * FROM " + Tables.ITEM_LOT + " WHERE ID = '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }
    private boolean isIdPresentInHealthFac(String id) {
        String selectQuery = "SELECT  * FROM " + Tables.HEALTH_FACILITY + " WHERE ID = '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }



    //MONTHLY REPORT METHODS BEGIN

    private boolean isColdChainPresentInDb(String selectedMonth){
        Log.d("COLD_CHAIN_TAG", "checking cold chain");
        String selectQuery = "SELECT * FROM "+ Tables.REFRIGERATOR_TEMPERATURE + " WHERE "+ SQLHandler.RefrigeratorColums.REPORTED_MONTH+"='"+selectedMonth+"'";
        Log.d("COLD_CHAIN_TAG", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("COLD_CHAIN_TAG", "cursor size is : "+cursor.getCount());
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;

    }

    private boolean isDeseaseSurveillanceEntryInDB(String selectedMonth){
        Log.d("COLD_CHAIN_TAG", "checking Surveillance");
        String selectQuery = "SELECT * FROM "+ Tables.DESEASES_SURVEILLANCE + " WHERE "+ SQLHandler.RefrigeratorColums.REPORTED_MONTH+"='"+selectedMonth+"'";
        Log.d("COLD_CHAIN_TAG", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("COLD_CHAIN_TAG", "cursor size is : "+cursor.getCount());
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    private boolean isaVaccinationsBcgOpvTtInDb(String selectedMonth, String doseId){
        Log.d("COLD_CHAIN_TAG", "checking BCG_OPV_TT");
        String selectQuery = "SELECT * FROM "+ Tables.VACCINATIONS_BCG_OPV_TT + " WHERE "+ SQLHandler.VaccinationsBcgOpvTtColumns.REPORTING_MONTH+"='"+selectedMonth+"' AND "+ SQLHandler.VaccinationsBcgOpvTtColumns.DOSE_ID+" = '"+doseId+"'";
        Log.d("COLD_CHAIN_TAG", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("COLD_CHAIN_TAG", "cursor size is : "+cursor.getCount());
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    private boolean isInjectionItemInDb(String selectedMonth, String itemName){
        Log.d("COLD_CHAIN_TAG", "checking BCG_OPV_TT");
        String selectQuery = "SELECT * FROM "+ Tables.SYRINGES_AND_SAFETY_BOXES + " WHERE "+ SQLHandler.SyringesAndSafetyBoxesColumns.REPORTING_MONTH+"='"+selectedMonth+"' AND "+ SQLHandler.SyringesAndSafetyBoxesColumns.ITEM_NAME+" = '"+itemName+"'";
        Log.d("COLD_CHAIN_TAG", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("COLD_CHAIN_TAG", "cursor size is : "+cursor.getCount());
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    private boolean isVitaminAInDb(String selectedMonth, String vitaminName){
        Log.d("VITAMIN_A", "checking Vitamin A");
        String selectQuery = "SELECT * FROM "+ Tables.HF_VITAMIN_A + " WHERE "+ SQLHandler.HfVitaminAColumns.REPORTING_MONTH+"='"+selectedMonth+"' AND "+ SQLHandler.HfVitaminAColumns.VITAMIN_NAME+" = '"+vitaminName+"'";
        Log.d("VITAMIN_A", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("VITAMIN_A", "cursor size is : "+cursor.getCount());
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    private boolean isImmunizationSessionInDb(String selectedMonth){
        Log.d("IMMUNIZATION_SESSION", "checking Imunization Session");
        String selectQuery = "SELECT * FROM "+ Tables.IMMUNIZATION_SESSION + " WHERE "+ SQLHandler.ImmunizationSessionColumns.REPORTING_MONTH+"='"+selectedMonth+"'";
        Log.d("IMMUNIZATION_SESSION", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("IMMUNIZATION_SESSION", "cursor size is : "+cursor.getCount());
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    //MONTHLY REPORT METHODS END

    public String getDomicilesFoundInChildAndNotInPlace() {
        //Query on Child Table
        String selectQuery = " SELECT DISTINCT(DOMICILE_ID) FROM CHILD WHERE DOMICILE_ID  NOT IN (SELECT ID FROM place) AND DOMICILE_ID <> 0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String placesIds = null;
        if (cursor.moveToFirst()) {
            placesIds = "";

            do {
                if (!cursor.isFirst()) placesIds += ",";
                placesIds += cursor.getString(0);
            } while (cursor.moveToNext());

            cursor.close();
            return placesIds;
        }
        cursor.close();
        return placesIds;
    }


    //**** END check and insert update Birthplace and Domicile id that are found in children and not in places table

    //**** Begin check and insert update HealthFacility id that are found in vacc event  and not in HealthFacility table

    public long addHFIDThatWereNotInDB(ContentValues cv, String id) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT or UPDATE
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isIdPresentInHealthFacility(id)) {
                result = sd.insert(Tables.HEALTH_FACILITY, null, cv);
            } else {
                result = sd.update(Tables.HEALTH_FACILITY, cv, SQLHandler.HealthFacilityColumns.ID + "=?", new String[]{id});
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            Log.e("addPlaceThatWereNotInDB", "result : " + result);
            return result;
        }
    }


    private boolean isIdPresentInHealthFacility(String id) {
        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.HEALTH_FACILITY + " WHERE ID = '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }
    public String getHFIDFoundInVaccEvAndNotInHealthFac() {
        //Query on Child Table
        String selectQuery =
                " SELECT DISTINCT(HEALTH_FACILITY_ID) FROM vaccination_event WHERE HEALTH_FACILITY_ID  NOT IN (SELECT ID FROM health_facility) AND HEALTH_FACILITY_ID <> 0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String placesIds = null;
        if (cursor.moveToFirst()) {
            placesIds = "";

            do {
                if (!cursor.isFirst()) placesIds += ",";
                placesIds += cursor.getString(0);
            } while (cursor.moveToNext());

            cursor.close();
            return placesIds;
        }
        cursor.close();
        return placesIds;
    }

    /**
     * this mehod takes a HealthFacilityID and returns a string 1,123,12312,... wwith the ids of children that are not of
     * the health facility of the logged in user
     * @param hfid the hfid of the logged in user
     * @return
     */
    public String getChildrenFromOtherHFIDThanLoggedUser(String hfid) {
        //Query on Child Table
        String selectQuery =
                " SELECT BARCODE_ID FROM child WHERE HEALTH_FACILITY_ID  != '"+hfid+"' AND HEALTH_FACILITY_ID <> 0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String placesIds = null;
        if (cursor.moveToFirst()) {
            placesIds = "";

            do {
                if (!cursor.isFirst()) placesIds += ",";
                placesIds += cursor.getString(0);
            } while (cursor.moveToNext());

            cursor.close();
            return placesIds;
        }
        cursor.close();
        return placesIds;
    }
    //**** END check and insert update HealthFacility id that are found in vacc event  and not in HealthFacility table


    //**** BEGIN getting Birthplaces
    public long addBirthplaces(ContentValues cv, String id) {
        // RETRIEVE WRITEABLE DATABASE AND INSERT or UPDATE
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        try {
            if (!isIdPresentInBirthplace(id)) {
                result = sd.insert(Tables.BIRTHPLACE, null, cv);
            } else {
                result = sd.update(Tables.BIRTHPLACE, cv, SQLHandler.PlaceColumns.ID + "=?", new String[]{id});
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            Log.e("addBirthplace", "result : " + result);
            return result;
        }
    }

    private boolean isIdPresentInBirthplace(String id) {
        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.BIRTHPLACE + " WHERE ID = '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    public boolean isChildRegistrationNoPresentInDb(String year,String childRegistrationSn,String barcodeId) {
        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.CHILD + " WHERE "+
                SQLHandler.ChildColumns.CHILD_REGISTRY_YEAR+" = '"+ year+"' AND "+
                SQLHandler.ChildColumns.CUMULATIVE_SERIAL_NUMBER+" = '"+ childRegistrationSn+"' AND "+
                SQLHandler.ChildColumns.BARCODE_ID+" <> '"+barcodeId+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    public List<Birthplace> getAllBirthplaces() {
        //Container
        List<Birthplace> placeList = new ArrayList<>();

        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.BIRTHPLACE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Birthplace bplace = new Birthplace();
                bplace.setId(cursor.getString(cursor.getColumnIndex(SQLHandler.PlaceColumns.ID)));
                bplace.setName(cursor.getString(cursor.getColumnIndex(SQLHandler.PlaceColumns.NAME)));
                placeList.add(bplace);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return placeList;
    }
    //**** END getting birthplaces



    public List<ModelImmunizedChild> getImmunizedChildren(String dateVar,BackboneApplication app) {
        //Container
        List<ModelImmunizedChild> childrenList = new ArrayList<ModelImmunizedChild>();

        //Query on Child Table
        String selectQuery = "Select APPOINTMENT_ID, CHILD.FIRSTNAME1 , CHILD.LASTNAME1 , " +
                "( SELECT GROUP_CONCAT(d.FULLNAME) " +
                " FROM vaccination_event as ve inner join dose as d " +
                "ON ve.dose_id = d.ID " +
                " WHERE v.APPOINTMENT_ID = ve.APPOINTMENT_ID AND ve.VACCINATION_STATUS = 'true'  " +
                " AND strftime('%Y-%m-%d',substr(ve.VACCINATION_DATE ,7,10), 'unixepoch') = strftime('%Y-%m-%d','"+dateVar+"')) as VACCINES, " +
                "( SELECT va.OUTREACH FROM vaccination_event vee inner join vaccination_appointment as va ON vee.APPOINTMENT_ID = va.ID "+
                " WHERE v.APPOINTMENT_ID = vee.APPOINTMENT_ID AND vee.VACCINATION_STATUS = 'true'  " +
                " AND strftime('%Y-%m-%d',substr(vee.VACCINATION_DATE ,7,10), 'unixepoch') = strftime('%Y-%m-%d','"+dateVar+"')) as OUTREACH " +
                " FROM VACCINATION_EVENT v  join CHILD " +
                " on v.CHILD_ID= CHILD.ID  " +
                " WHERE strftime('%Y-%m-%d',substr(VACCINATION_DATE ,7,10), 'unixepoch') = strftime('%Y-%m-%d','"+dateVar+"')" +
                " AND VACCINATION_STATUS = 'true' " +
                " GROUP BY APPOINTMENT_ID, CHILD.FIRSTNAME1 || ' ' || CHILD.LASTNAME1";
// " AND v."+SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID+" = '"+appv2.getLOGGED_IN_USER_HF_ID()+"'"+
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ModelImmunizedChild children = new ModelImmunizedChild();
                children.setAppId(cursor.getString(0));
                children.setName(cursor.getString(1));
                children.setLastname(cursor.getString(2));
                children.setVaccine(cursor.getString(3));
                children.setOutreach(cursor.getString(4));
                childrenList.add(children);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return childrenList;
    }

    /**
     * method used to get the list of the situation in the HeaalthFacilityBalance table
     * @return
     */
    public List<Stock> getAllHealthFacilityBalance() {
        //Container
        List<Stock> stockList = new ArrayList<>();

        //Query on Child Table
        String selectQuery = "SELECT lot_id, gtin , lot_number , item , sum(balance) as balance , expire_date , ReorderQty, GtinIsActive, LotIsActive FROM "+ Tables.HEALTH_FACILITY_BALANCE +" where datetime(substr(expire_date,7,10), 'unixepoch') >= datetime('now') group by item" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Stock stockItem = new Stock();
                stockItem.setBalance(cursor.getInt(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.BALANCE)));
                stockItem.setExpireDate(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.EXPIRE_DATE)));
                stockItem.setGtin(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.GTIN)));
                stockItem.setItem(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.ITEM)));
                stockItem.setLotId(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.LOT_ID)));
                stockItem.setLotNumber(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.LOT_NUMBER)));
                stockItem.setReorderQty(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.REORDER_QTY)));
                stockItem.setLotIsActive(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.LOT_ISACTIVE)));
                stockItem.setGtinIsActive(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.GTIN_ISACTIVE)));
                stockList.add(stockItem);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return stockList;
    }

    /**
     * method used to get the list of the situation in the HeaalthFacilityBalance table
     * @return
     */
    public List<ScheduledVaccination> getAllScheduledVaccination() {
        //Container
        List<ScheduledVaccination> stockList = new ArrayList<>();

        //Query on Child Table
        String selectQuery = "SELECT * FROM "+ Tables.SCHEDULED_VACCINATION ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                ScheduledVaccination item = new ScheduledVaccination();
                item.setId(cursor.getString(cursor.getColumnIndex(SQLHandler.ScheduledVaccinationColumns.ID)));
                item.setName(cursor.getString(cursor.getColumnIndex(SQLHandler.ScheduledVaccinationColumns.NAME)));
                stockList.add(item);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return stockList;
    }

    /**
     * method used to get coverage as percentage
     * @return
     */
    public int getCoveragePercentage(String loggedUserHFID , String scheduledVaccinationId) {
        //Container
        List<ScheduledVaccination> stockList = new ArrayList<>();

        String selectQueryVaccinationDateCount = "select count(*) from vaccination_event " +
                "where health_facility_id = '"+loggedUserHFID+"' and vaccination_status = 'true' " +
                "and strftime('%m-%Y', substr(vaccination_date,7,10), 'unixepoch') = strftime('%m-%Y','now') " +
                "and dose_id in (select id from dose where scheduled_vaccination_id = '"+scheduledVaccinationId+"') ";

        String selectQueryScheduledDateCount = "select count(*) from vaccination_event " +
                "where health_facility_id = '"+loggedUserHFID+"' " +
                "and strftime('%m-%Y', substr(scheduled_date,7,10), 'unixepoch') = strftime('%m-%Y','now') " +
                "and dose_id in (select id from dose where scheduled_vaccination_id = '"+scheduledVaccinationId+"') ";

        SQLiteDatabase db = this.getWritableDatabase();
        int countDoneVacines = 0;
        int countScheduledVaccines = 1;

        double coveragePercent = 0d;

        Cursor cursor = db.rawQuery(selectQueryVaccinationDateCount, null);
        if (cursor.moveToFirst()) {
            do {
                countDoneVacines = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        cursor.close();

        cursor = db.rawQuery(selectQueryScheduledDateCount, null);
        if (cursor.moveToFirst()) {
            do {
                countScheduledVaccines = cursor.getInt(0);
                // this is done just to exclude the 0/0 case
                if(countScheduledVaccines == 0)countScheduledVaccines = 1;
            } while (cursor.moveToNext());
        }

        cursor.close();

        coveragePercent = ((double)countDoneVacines/(double)countScheduledVaccines)*100;
        return (int)coveragePercent;
    }

    /**
     * method used to get the list of the situation in the HeaalthFacilityBalance table
     * @return
     */
    public List<ChartDataModel> getMonthlyPerformance(BackboneApplication app) {
        //Container
        List<ChartDataModel> items = new ArrayList<>();

        //Query on Child Table
        String selectQuery = "SELECT strftime('%m',substr("+SQLHandler.VaccinationEventColumns.VACCINATION_DATE+",7,10), 'unixepoch') as month, count(*) as  no_vaccinations" +
                " FROM "+Tables.VACCINATION_EVENT +
                " WHERE "+SQLHandler.VaccinationEventColumns.VACCINATION_STATUS+" = 'true'" +
                " AND "+SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID+" = '"+app.getLOGGED_IN_USER_HF_ID()+"'"+
                " AND strftime('%Y',substr("+SQLHandler.VaccinationEventColumns.VACCINATION_DATE+",7,10), 'unixepoch') =  strftime('%Y','now') "+
                " group by strftime('%m',substr("+SQLHandler.VaccinationEventColumns.VACCINATION_DATE+",7,10), 'unixepoch')";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ChartDataModel item = new ChartDataModel();
                item.setValue(cursor.getInt(cursor.getColumnIndex("no_vaccinations")));
                item.setLabel(getMonthNameFromNumber(cursor.getString(cursor.getColumnIndex("month")), app));
                items.add(item);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return items;
    }

    /**
     * method intended for internal usage to get January from 01 , etc.
     * @param monthNrAsString rapresentation of month ex: "01"
     * @param app needed to get access to appv2 resources (in this case strings.xml)
     * @return
     */
    public String getMonthNameFromNumber(String monthNrAsString, BackboneApplication app){
        String monthName = "";
        if(monthNrAsString.equals("01")){
            monthName = app.getString(R.string.january);
        }else if(monthNrAsString.equals("02")){
            monthName = app.getString(R.string.february);
        }else if(monthNrAsString.equals("03")){
            monthName = app.getString(R.string.march);
        }else if(monthNrAsString.equals("04")){
            monthName = app.getString(R.string.april);
        }else if(monthNrAsString.equals("05")){
            monthName = app.getString(R.string.may);
        }else if(monthNrAsString.equals("06")){
            monthName = app.getString(R.string.june);
        }else if(monthNrAsString.equals("07")){
            monthName = app.getString(R.string.july);
        }else if(monthNrAsString.equals("08")){
            monthName = app.getString(R.string.august);
        }else if(monthNrAsString.equals("09")){
            monthName = app.getString(R.string.september);
        }else if(monthNrAsString.equals("10")){
            monthName = app.getString(R.string.october);
        }else if(monthNrAsString.equals("11")){
            monthName = app.getString(R.string.november);
        }else if(monthNrAsString.equals("12")){
            monthName = app.getString(R.string.december);
        }

        if(monthNrAsString.equals("1")){
            monthName = app.getString(R.string.january);
        }else if(monthNrAsString.equals("2")){
            monthName = app.getString(R.string.february);
        }else if(monthNrAsString.equals("3")){
            monthName = app.getString(R.string.march);
        }else if(monthNrAsString.equals("4")){
            monthName = app.getString(R.string.april);
        }else if(monthNrAsString.equals("5")){
            monthName = app.getString(R.string.may);
        }else if(monthNrAsString.equals("6")){
            monthName = app.getString(R.string.june);
        }else if(monthNrAsString.equals("7")){
            monthName = app.getString(R.string.july);
        }else if(monthNrAsString.equals("8")){
            monthName = app.getString(R.string.august);
        }else if(monthNrAsString.equals("9")){
            monthName = app.getString(R.string.september);
        }

        return monthName;
    }

    public List<NewChartDataTable> getImmunizationsNew(String dateVar, BackboneApplication app){
        //Container
        List<NewChartDataTable> childrenList = new ArrayList<NewChartDataTable>();

        //Query on Child Table
        String selectQuery = "SELECT d.FULLNAME, d.DOSE_NUMBER, count(ve.DOSE_ID) as count, sv.CODE" +
                " FROM vaccination_event as ve join dose as d" +
                " ON d.ID = ve.DOSE_ID" +
                " JOIN scheduled_vaccination as sv" +
                " ON d.SCHEDULED_VACCINATION_ID = sv.ID" +
                " WHERE strftime('%Y-%m-%d',substr(ve.vaccination_date,7,10), 'unixepoch') = '"+dateVar+"'"+
                " AND ve.vaccination_status = 'true' " +
                " AND ve."+SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID+" = '"+app.getLOGGED_IN_USER_HF_ID()+"'"+
                " GROUP BY d.FULLNAME";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        Log.d("immunizations","immunization count = "+cursor.getCount());
        Log.d("immunizations","datevar = "+dateVar);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                NewChartDataTable children = new NewChartDataTable();
                children.setName(cursor.getString(0));
                Log.d("ARSENALTAG", "DOSE NUMBER IS : " + cursor.getString(1));
                children.setDosenumber(cursor.getString(1));
                children.setValue(cursor.getInt(2));
                children.setLabel(cursor.getString(3));
                childrenList.add(children);
                Log.d("ARSENALTAG", "Found one moving to next, name is : "+cursor.getString(0)+" CODE is : "+cursor.getString(3)+" Dose Number is : "+cursor.getInt(1));
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return childrenList;
    }

    public List<ChartDataModel> getImmunizations(String dateVar,BackboneApplication app) {
        //Container
        List<ChartDataModel> childrenList = new ArrayList<ChartDataModel>();

        //Query on Child Table
        String selectQuery = "SELECT sv.code, count(sv.ID) as count" +
                " FROM vaccination_event as ve join dose as d" +
                " ON d.ID = ve.DOSE_ID" +
                " JOIN scheduled_vaccination as sv" +
                " ON d.SCHEDULED_VACCINATION_ID = sv.ID" +
                " WHERE strftime('%Y-%m-%d',substr(ve.vaccination_date,7,10), 'unixepoch') = '"+dateVar+"'"+
                " AND ve.vaccination_status = 'true' " +
                " AND ve."+SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID+" = '"+app.getLOGGED_IN_USER_HF_ID()+"'"+
                " GROUP BY sv.CODE";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ChartDataModel children = new ChartDataModel();
                children.setLabel(cursor.getString(0));
                children.setValue(cursor.getInt(1));
                childrenList.add(children);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return childrenList;
    }

    public boolean isMonthYearPresentInStockStatusTable(String monthYear, String itemName){
        Log.d("STOCK_STATUS", monthYear+" "+itemName+" Checking to see if present");
        String query = "SELECT * FROM "+Tables.STOCK_STATUS_REPORT+
                " WHERE "+ SQLHandler.StockStatusColumns.REPORTED_MONTH+"='"+monthYear+
                "' AND "+SQLHandler.StockStatusColumns.ITEM_NAME+" = '"+itemName+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.getCount()>0){
            Log.d("STOCK_STATUS", monthYear+" "+itemName+" Present in the database");
            return true;
        }
        else{
            Log.d("STOCK_STATUS", monthYear+" "+itemName+ " Not present in the database!!");
            return false;
        }
    }

    public long addToStockStatusTable(String monthYear, String itemName, int adjustment, boolean add){

        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        //The CV passed here contains only single variables to be updated
        try {
            if (isMonthYearPresentInStockStatusTable(monthYear, itemName)){
                Log.d("STOCK_STATUS", monthYear+" "+itemName);
                ContentValues contentValues = new ContentValues();
                contentValues.put(GIISContract.SyncColumns.UPDATED, 1);
                if (add){
                    String currentValue = "0";
                    String query = "SELECT "+ SQLHandler.StockStatusColumns.DOSES_RECEIVED+" FROM "+Tables.STOCK_STATUS_REPORT
                            +" WHERE "+ SQLHandler.StockStatusColumns.REPORTED_MONTH+" = "+monthYear+ " AND "
                            + SQLHandler.StockStatusColumns.ITEM_NAME+ " = "+itemName;
                    SQLiteDatabase db = this.getWritableDatabase();
                    Cursor cursor = db.rawQuery(query, null);
                    if (cursor.moveToFirst()){
                        currentValue = cursor.getString(0);
                    }
                    int currentValueInteger = Integer.parseInt(currentValue);
                    int adjustedValue   = currentValueInteger+adjustment;
                    contentValues.put(SQLHandler.StockStatusColumns.DOSES_RECEIVED, adjustedValue+"");
                }else {
                    String currentValue = "0";
                    String query = "SELECT "+ SQLHandler.StockStatusColumns.DISCARDED_UNOPENED+" FROM "+Tables.STOCK_STATUS_REPORT
                            +" WHERE "+ SQLHandler.StockStatusColumns.REPORTED_MONTH+" = "+monthYear+ " AND "
                            + SQLHandler.StockStatusColumns.ITEM_NAME+ " = "+itemName;
                    SQLiteDatabase db = this.getWritableDatabase();
                    Cursor cursor = db.rawQuery(query, null);
                    if (cursor.moveToFirst()){
                        currentValue = cursor.getString(0);
                    }
                    int currentValueInteger = Integer.parseInt(currentValue);
                    int adjustedValue   = currentValueInteger+adjustment;
                    contentValues.put(SQLHandler.StockStatusColumns.DISCARDED_UNOPENED, adjustedValue+"");
                }
                result = sd.update(Tables.STOCK_STATUS_REPORT, contentValues,
                        GIISContract.StockStatusColumns.REPORTED_MONTH + " = ? AND "+ GIISContract.StockStatusColumns.ITEM_NAME + " = ?",
                        new String[]{
                                monthYear, itemName
                        });
            }else {
                Log.d("STOCK_STATUS", monthYear+" "+itemName+" Not present Inserting now!!");
                ContentValues contentValues = new ContentValues();
                contentValues.put(GIISContract.SyncColumns.UPDATED, 1);
                contentValues.put(SQLHandler.StockStatusColumns.REPORTED_MONTH, monthYear);
                contentValues.put(SQLHandler.StockStatusColumns.ITEM_NAME, itemName);
                if (add){
                    contentValues.put(SQLHandler.StockStatusColumns.DOSES_RECEIVED, adjustment+"");
                }else{
                    contentValues.put(SQLHandler.StockStatusColumns.DISCARDED_UNOPENED, adjustment+"");
                }
                result = sd.insert(Tables.STOCK_STATUS_REPORT, null, contentValues);
            }
            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public List<StockStatusEntity> generateStock(String dateFrom, String dateTo, BackboneApplication app){
        List<StockStatusEntity> stockStatusEntities = new ArrayList<>();

        String selectQuery = "";
        selectQuery = selectQuery+"SELECT sv.code, count(sv.ID) ";
        selectQuery = selectQuery+"FROM "+ Tables.VACCINATION_EVENT+" as ve join dose as d ON d.ID = ve.DOSE_ID ";
        selectQuery = selectQuery+"JOIN "+ Tables.SCHEDULED_VACCINATION+" as sv ON d.SCHEDULED_VACCINATION_ID = sv.ID ";
        selectQuery = selectQuery+"WHERE strftime('%Y-%m-%d',substr(ve.vaccination_date,7,10), 'unixepoch') >= "+dateFrom+" AND strftime('%Y-%m-%d',substr(ve.vaccination_date,7,10), 'unixepoch') <= "+ dateTo+" ";
        selectQuery = selectQuery+"AND ve.vaccination_status = 'true' ";
        selectQuery = selectQuery+"AND ve."+SQLHandler.VaccinationEventColumns.HEALTH_FACILITY_ID+" = '"+app.getLOGGED_IN_USER_HF_ID()+ " ";
        selectQuery = selectQuery+"GROUP BY sv.CODE ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                StockStatusEntity children = new StockStatusEntity();
                children.setAntigen(cursor.getString(0));
                children.setChildrenImmunized(cursor.getString(1));
                stockStatusEntities.add(children);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();

        return stockStatusEntities;
    }


    //Methods for AdministerVaccines

    public AdministerVaccinesModel getPartOneAdminVaccModel(boolean starter_set,String appointment_id,String dose_id){

        //final RowObjects rowObjects = new RowObjects();
        AdministerVaccinesModel adminVacc = new AdministerVaccinesModel();
        if (!starter_set) {
            adminVacc.setStarter_row(true);
        }

        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = null;

        cursor =
                database.rawQuery("SELECT vaccination_event.ID AS VID, VACCINATION_STATUS, SCHEDULED_DATE AS SCHDT, SCHEDULED_VACCINATION_ID, DOSE_ID, DOSE_NUMBER AS DN, dose.FULLNAME as FULLNAME FROM vaccination_event INNER JOIN dose " +
                        "ON vaccination_event.DOSE_ID = dose.ID WHERE APPOINTMENT_ID=? AND dose.ID=?", new String[]{appointment_id, dose_id});


        if (cursor.moveToFirst()) {
            do {
//                vaccination_status = cursor.getString(cursor.getColumnIndex("VACCINATION_STATUS"));
                adminVacc.setDose_id(cursor.getString(cursor.getColumnIndex("DOSE_ID")));
                adminVacc.setVacc_ev_id(cursor.getString(cursor.getColumnIndex("VID")));
                adminVacc.setScheduled_Date_field(cursor.getString(cursor.getColumnIndex("SCHDT")));
                adminVacc.setDose_Number_field(cursor.getString(cursor.getColumnIndex("DN")));
                adminVacc.setScheduled_Vaccination_Id(cursor.getString(cursor.getColumnIndex("SCHEDULED_VACCINATION_ID")));
                adminVacc.setDoseName(cursor.getString(cursor.getColumnIndex("FULLNAME")));

                try {
                    adminVacc.setDose_Number_Parsed(Integer.parseInt(adminVacc.getDose_Number_field()) + 1);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return adminVacc;


    }


    public void  getPartTwoAdminVacc(AdministerVaccinesModel adminvacc,long daysDiff,int DateDiffDialog){

        String scheduled_vaccination_id, item_id;
        Map<String, String> vac_lot_map = new HashMap<String, String>();
        List<String> lot_name = new ArrayList<String>();
        List<String> lot_balance = new ArrayList<>();

        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = null;

        cursor =
                database.rawQuery("SELECT FROM_AGE_DEFINITON_ID AS FAID, TO_AGE_DEFINITON_ID AS TAID, SCHEDULED_VACCINATION_ID FROM dose WHERE ID=?", new String[]{adminvacc.getDose_id()});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String faid = cursor.getString(cursor.getColumnIndex("FAID"));
            String taid = cursor.getString(cursor.getColumnIndex("TAID"));

            scheduled_vaccination_id = cursor.getString(cursor.getColumnIndex("SCHEDULED_VACCINATION_ID"));
            Log.d("Scheduled vacc id", scheduled_vaccination_id);


            Calendar calendar=Calendar.getInstance();
            GregorianCalendar gregorianCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));


            Cursor cursor2 =   database.rawQuery("SELECT ITEM_ID FROM scheduled_vaccination WHERE ID=?", new String[]{scheduled_vaccination_id});
            if (cursor2.getCount() > 0) {
                cursor2.moveToFirst();
                item_id = cursor2.getString(cursor2.getColumnIndex("ITEM_ID"));

                cursor =   database.rawQuery("SELECT '-1' AS id, '-----' AS lot_number,'30' AS balance, datetime('now') as expire_date UNION " +
                        " SELECT '-2' AS id, 'No Lot' AS lot_number, '30' AS balance, datetime('now') as expire_date UNION " +
                        " SELECT item_lot.id, item_lot.lot_number, health_facility_balance.balance, datetime(substr(item_lot.expire_date,7,10), 'unixepoch') " +
                        " FROM item_lot  join health_facility_balance ON item_lot.ID = health_facility_balance.lot_id " +
                        " INNER JOIN active_lot_numbers ON health_facility_balance.lot_id = active_lot_numbers.lot_id "+
                        " WHERE active_lot_numbers.date= "+gregorianCalendar.getTimeInMillis()+" AND  item_lot.item_id = ? AND health_facility_balance.LotIsActive = 'true' AND CAST(health_facility_balance.balance as REAL) > "+0+"" +
                        " AND datetime(substr(item_lot.expire_date,7,10), 'unixepoch') >= datetime('now') ORDER BY expire_date", new String[]{item_id});
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            Log.d("", "Adding to map" + cursor.getString(cursor.getColumnIndex("lot_number")));
                            vac_lot_map.put(cursor.getString(cursor.getColumnIndex("lot_number")), cursor.getString(cursor.getColumnIndex("id")));
                            lot_name.add(cursor.getString(cursor.getColumnIndex("lot_number")));
                            lot_balance.add(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.BALANCE)));
                        } while (cursor.moveToNext());
                        adminvacc.setVaccine_lot_map(vac_lot_map);
                        adminvacc.setVaccine_lot_list(lot_name);
                        adminvacc.setBalance(lot_balance);
                    }
                }
            }

            try {

                if (faid != null && Integer.parseInt(faid) > 0) {
                    String days;
                    cursor = database.rawQuery("SELECT DAYS FROM age_definitions WHERE ID=?", new String[]{faid});
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        days = cursor.getString(cursor.getColumnIndex("DAYS"));
                        Log.d("faid", "condition true ? false" + daysDiff + " < " + Long.parseLong(days));
                        if (daysDiff < Long.parseLong(days)) {

                            if (DateDiffDialog == 0) {
                                DateDiffDialog = 1;
                            }
                        }
                    }
                } else if (taid != null && Integer.parseInt(taid) > 0) {
                    String days;
                    cursor =  database.rawQuery("SELECT DAYS FROM age_definitions WHERE ID=?", new String[]{taid});
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        days = cursor.getString(cursor.getColumnIndex("DAYS"));
                        if (daysDiff > Long.parseLong(days)) {
                            if (DateDiffDialog == 0) {
                                DateDiffDialog = 2;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }



    public ArrayList<String> getDosesForAppointmentID(String appointmentID) {
        //Container
        ArrayList<String> dosesList = new ArrayList<String>();

        //Query on Child Table
        String selectQuery = "Select dose_id from vaccination_event where appointment_id = '"+appointmentID +
                "' and vaccination_status = 'false' and is_active = 'true' and (nonvaccination_reason_id = 0" +
                " or nonvaccination_reason_id in (select id from nonvaccination_reason where keep_child_due = 'true'))";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                dosesList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return dosesList;
    }




    public HashMap<String, String> getPreviousDateAndWeight(String barcode){
        HashMap<String, String> date_and_weight = null;
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = null;

        cursor =database.rawQuery("SELECT DATE , WEIGHT  FROM child_weight WHERE CHILD_BARCODE=?", new String[]{barcode});

            if (cursor.moveToFirst()) {

                date_and_weight = new HashMap<String, String>();
                date_and_weight.put("Date", cursor.getString(cursor.getColumnIndex("DATE")));
                date_and_weight.put("Weight",cursor.getString(cursor.getColumnIndex("WEIGHT")));

                cursor.close();
                return date_and_weight;
            }
        cursor.close();
        return date_and_weight;
    }

    public long updateHealthFacilityBalance(HealthFacilityBalance item) {
        SQLiteDatabase sd = getWritableDatabase();
        long result = -1;
        sd.beginTransaction();
        ContentValues cv = new ContentValues();
        cv.put(SQLHandler.HealthFacilityBalanceColumns.BALANCE, item.getBalance());
        try {
            result = sd.update(Tables.HEALTH_FACILITY_BALANCE, cv,
                    GIISContract.HealthFacilityBalanceTable.LOT_ID + " = ? And  "
                    + GIISContract.HealthFacilityBalanceTable.GTIN + " =?",
                    new String[]{item.getLot_id(),item.getGtin()});

            sd.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            sd.endTransaction();
            return result;
        }
    }

    public List<String> getNameFromAdjustmentReasons() {
        //Container
        List<String> list = new ArrayList<>();

        //Query on Child Table
        String selectQuery = "SELECT  NAME FROM " + Tables.ADJUSTMENT_REASONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            list.add("-------");
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return list;
    }

    public List<AdjustmentReasons> getAdjustmentReasons() {
        //Container
        List<AdjustmentReasons> listAdjustmentReasons = new ArrayList<>();

        //Query on Child Table
        String selectQuery = "SELECT  * FROM " + Tables.ADJUSTMENT_REASONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AdjustmentReasons item = new AdjustmentReasons();
                item.setId(cursor.getString(cursor.getColumnIndex(SQLHandler.AdjustmentColumns.ID)));
                item.setName(cursor.getString(cursor.getColumnIndex(SQLHandler.AdjustmentColumns.NAME)));
                item.setIsActive(cursor.getString(cursor.getColumnIndex(SQLHandler.AdjustmentColumns.IS_ACTIVE)));
                item.setPozitive(cursor.getString(cursor.getColumnIndex(SQLHandler.AdjustmentColumns.POSITIVE)));
                listAdjustmentReasons.add(item);
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return listAdjustmentReasons;
    }


    //method used to check if there are any unsynchronized child details in postman table
    public boolean checkIfChildIdIsInPostman(String childId){
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT * FROM " + Tables.POSTMAN +
                    " WHERE " + SQLHandler.PostmanColumns.URL + " LIKE '%" + getChildById(childId).getBarcodeID() + "%'", null);
            if (c.getCount() > 0) {
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        }catch (Exception e){
            return false;
        }

    }

    //method used to check if there are any unsynchronized child details in postman table
    public boolean checkIfChildBarcodeIsInPostman(String barcode){
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT * FROM " + Tables.POSTMAN +
                    " WHERE " + SQLHandler.PostmanColumns.URL + " LIKE '%" + getChildByBarcode(barcode).getBarcodeID() + "%'", null);
            if (c.getCount() > 0) {
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        }catch (Exception e){
            return false;
        }

    }

    public void saveActiveLotNumber(String lot_id,String lot_number,String item,long date){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(GIISContract.ActiveLotNumbersColumns.LOT_ID,lot_id);
        contentValues.put(GIISContract.ActiveLotNumbersColumns.LOT_NUMBER,lot_number);
        contentValues.put(GIISContract.ActiveLotNumbersColumns.ITEM,item);
        contentValues.put(GIISContract.ActiveLotNumbersColumns.DATE,date);

        db.beginTransaction();
        try {
            db.insert(Tables.ACTIVE_LOT_NUMBERS, null,contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }


    /**
     * method used to get the list of the vaccinations in the HealthFacilityBalance table whose balance is non zero
     * @return
     */
    public List<Stock> getAvailableHealthFacilityBalance() {
        //Container
        List<Stock> stockList = new ArrayList<>();

        //Query on Child Table
        String selectQuery = "SELECT lot_id, gtin , lot_number , item , sum(balance) as balance , expire_date , ReorderQty, GtinIsActive, LotIsActive FROM "+ Tables.HEALTH_FACILITY_BALANCE +" where datetime(substr(expire_date,7,10), 'unixepoch') >= datetime('now') group by item" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getInt(cursor.getColumnIndex("balance"))>0) {
                    Stock stockItem = new Stock();
                    stockItem.setBalance(cursor.getInt(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.BALANCE)));
                    stockItem.setExpireDate(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.EXPIRE_DATE)));
                    stockItem.setGtin(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.GTIN)));
                    stockItem.setItem(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.ITEM)));
                    stockItem.setLotId(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.LOT_ID)));
                    stockItem.setLotNumber(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.LOT_NUMBER)));
                    stockItem.setReorderQty(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.REORDER_QTY)));
                    stockItem.setLotIsActive(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.LOT_ISACTIVE)));
                    stockItem.setGtinIsActive(cursor.getString(cursor.getColumnIndex(SQLHandler.HealthFacilityBalanceColumns.GTIN_ISACTIVE)));
                    stockList.add(stockItem);
                }
            } while (cursor.moveToNext());
        }

        // return container
        cursor.close();
        return stockList;
    }


    public boolean isStockDistributionInDb(int StockDistributionId) {
        String selectQuery = "SELECT "+ SQLHandler.StockDistributionsValuesColumns.TO_HEALTH_FACILITY_ID+" FROM stock_distributions" +
                " WHERE "+ SQLHandler.StockDistributionsValuesColumns.STOCK_DISTRIBUTION_ID+" = " + StockDistributionId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public long storeHealthFacilitySession(String healthFacilityId, String userId, long time) {

        SQLiteDatabase db = getWritableDatabase();
        long result = -1;
        db.beginTransaction();
        ContentValues insertValues = new ContentValues();
        insertValues.put(GIISContract.HfLoginSessions.USER_ID, userId);
        insertValues.put(GIISContract.HfLoginSessions.HEALTH_FACILITY_ID, healthFacilityId);
        insertValues.put(GIISContract.HfLoginSessions.LOGING_TIME, time);
        insertValues.put(GIISContract.HfLoginSessions.SESSION_LENGTH, 0);
        insertValues.put(GIISContract.HfLoginSessions.STATUS, -1);

        try {
            result = db.insert(Tables.HF_LOGIN_SESSIONS, null, insertValues);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            db.endTransaction();
            Log.d(TAG,"Login session stored with result = "+ result+" for userid = "+userId);
            return result;
        }
    }



    /**
     * status codes.
     *   0 = currently inprogress session.
     *  -1 = completed session but has not yet been synchronized with the server
     *   1 = completed and synchronised sessions.
     * @return
     */
    public long updateHealthFacilityStatus(long session_id,int status) {

        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM "+Tables.HF_LOGIN_SESSIONS+" WHERE "+BaseColumns._ID+" = "+session_id,null);
        c.moveToFirst();
        long result = -1;
        db.beginTransaction();
        ContentValues insertValues = new ContentValues();
        insertValues.put(BaseColumns._ID, c.getInt(0));
        insertValues.put(GIISContract.HfLoginSessions.STATUS, status);
        Log.d("destroy","updating session status = "+ status);
        Log.d("destroy","updating id  = "+  c.getInt(0));

        try {
            result = db.update(Tables.HF_LOGIN_SESSIONS, insertValues, BaseColumns._ID + " = "+ c.getInt(0),null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            db.endTransaction();
            return result;
        }
    }

    public long deleteHealthFacilityStatus(long session_id) {
        SQLiteDatabase db = getWritableDatabase();
        long result = -1;
        db.beginTransaction();
        try {
            result = db.delete(Tables.HF_LOGIN_SESSIONS, BaseColumns._ID + " = " + session_id, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Error in between database transaction
            result = -1;
        } finally {
            db.endTransaction();
            return result;
        }
    }

    public List<SessionsModel> getHealthFacilitySessions() {

        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM "+Tables.HF_LOGIN_SESSIONS+" WHERE "+GIISContract.HfLoginSessions.STATUS+" = -1 ",null);
        int count = c.getCount();

        Log.d(TAG,"SESSIONS COUNT = "+c.getCount());

        List<SessionsModel> modelList = new ArrayList<>();
        for (int i=0;i<count;i++){
            c.moveToPosition(i);
            SessionsModel model = new SessionsModel();
            model.setModel(c,model);
            Log.d(TAG,"session user id = "+c.getInt(c.getColumnIndex(GIISContract.HfLoginSessions.USER_ID)));
            modelList.add(model);
        }

        Log.d(TAG,"SESSIONS Models COUNT = "+modelList.size());

        return modelList;

    }
}