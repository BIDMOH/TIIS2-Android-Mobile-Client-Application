package mobile.tiis.appv2.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;


import mobile.tiis.appv2.fragments.ChildRegisterReportFragment;
import mobile.tiis.appv2.fragments.DefaultersReportFragment;
import mobile.tiis.appv2.fragments.HealthFacilityImmunizationCoverageScheduledReportFragment;
import mobile.tiis.appv2.fragments.HealthFacilityImmunizationCoverageTargetReportFragment;
import mobile.tiis.appv2.fragments.HealthFacilityVisitsAndVaccinationSummaryFragment;
import mobile.tiis.appv2.fragments.DropoutReportFragment;
import mobile.tiis.appv2.fragments.ImmunizationChartFragment;
import mobile.tiis.appv2.fragments.ImmunizedChildrenFragment;
import mobile.tiis.appv2.fragments.StockTabFragment;
import mobile.tiis.appv2.fragments.TabFragment;


import mobile.tiis.appv2.fragments.ChildRegisterReportFragment;
import mobile.tiis.appv2.fragments.DefaultersReportFragment;
import mobile.tiis.appv2.fragments.HealthFacilityImmunizationCoverageScheduledReportFragment;
import mobile.tiis.appv2.fragments.HealthFacilityImmunizationCoverageTargetReportFragment;
import mobile.tiis.appv2.fragments.HealthFacilityVisitsAndVaccinationSummaryFragment;
import mobile.tiis.appv2.fragments.DropoutReportFragment;
import mobile.tiis.appv2.fragments.ImmunizationChartFragment;
import mobile.tiis.appv2.fragments.ImmunizedChildrenFragment;
import mobile.tiis.appv2.fragments.StockStatusFragment;
import mobile.tiis.appv2.fragments.StockTabFragment;
import mobile.tiis.appv2.fragments.TabFragment;

/**
 *  Created by issymac on 14/12/15.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final String[] TITLES = {"Child Register Report (MTUHA)","Immunization Coverage Report (Scheduled)", "Immunization Coverage Report (Target)", "Defaulters List", "Dropout Report", "Immunized Children", "Vaccination Summary", "Stock Report"};


    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("ChartsLog", position+"");
        if (position == 0){
            return ChildRegisterReportFragment.newInstance(position);
        }

        if (position == 1){
            return HealthFacilityImmunizationCoverageScheduledReportFragment.newInstance(position);
        }
        else if (position == 2){
            return HealthFacilityImmunizationCoverageTargetReportFragment.newInstance(position);
        }
//        else if (position == 2){
//            return HealthFacilityImmunizationCoverageChartFragment.newInstance(position);
//        }
        else if (position == 3){
            return DefaultersReportFragment.newInstance(position);
        }
        else if (position == 4){
            return DropoutReportFragment.newInstance(position);
        }
        else if (position == 5){
            return ImmunizedChildrenFragment.newInstance();
        }
        else if (position == 6){
            return HealthFacilityVisitsAndVaccinationSummaryFragment.newInstance(position);
        }
        else if (position == 7){
            return StockStatusFragment.newInstance();
        }
        else{
            return TabFragment.newInstance(position);
        }
    }

}