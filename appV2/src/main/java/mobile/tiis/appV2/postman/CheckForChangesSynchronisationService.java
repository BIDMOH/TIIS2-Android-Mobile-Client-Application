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

package mobile.tiis.appv2.postman;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import mobile.tiis.appv2.base.BackboneApplication;

/**
 * Created by utente1 on 4/8/2015.
 */
public class CheckForChangesSynchronisationService  extends IntentService {
    private LocalBroadcastManager broadcaster;
    static final public String SynchronisationService_RESULT = "mobile,giis.appv2.CheckForChangesSynchronisationService.REQUEST_PROCESSED";
    static final public String SynchronisationService_MESSAGE = "mobile,giis.appv2.CheckForChangesSynchronisationService..MSG";


    public CheckForChangesSynchronisationService() {
        super(CheckForChangesSynchronisationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
        BackboneApplication app = (BackboneApplication) getApplication();
        synchronized (app) {
            app.parseConfiguration();
            if(app.getLOGGED_IN_USER_ID()!=null && !app.getLOGGED_IN_USER_ID().equals("0")) {
//                appv2.continuousModificationParser();
                app.getVaccinationQueueByDateAndUser();
            }

            String placesFoundInChildOnlyAndNotInPlace = app.getDatabaseInstance().getDomicilesFoundInChildAndNotInPlace();
            if(placesFoundInChildOnlyAndNotInPlace != null){
                app.parsePlacesThatAreInChildAndNotInPlaces(placesFoundInChildOnlyAndNotInPlace);
            }

            String hfidFoundInVaccEvOnlyAndNotInHealthFac = app.getDatabaseInstance().getHFIDFoundInVaccEvAndNotInHealthFac();
            if(hfidFoundInVaccEvOnlyAndNotInHealthFac != null){
                app.parseHealthFacilityThatAreInVaccEventButNotInHealthFac(hfidFoundInVaccEvOnlyAndNotInHealthFac);
            }
            app.parseStock();
            app.parseStockDistributions();

        }

        this.stopSelf();
    }

    public void sendResult(String message) {
        Intent intent = new Intent(SynchronisationService_RESULT);
        if(message != null)
            intent.putExtra(SynchronisationService_MESSAGE, message);
        broadcaster.sendBroadcast(intent);
    }

}