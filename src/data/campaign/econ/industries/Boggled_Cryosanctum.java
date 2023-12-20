package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.Cryosanctum;
import data.campaign.econ.boggledTools;

public class Boggled_Cryosanctum extends Cryosanctum
{
    @Override
    public void apply()
    {
        super.apply();

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            int size = this.market.getSize();
            this.demand(boggledTools.BoggledCommodities.domainArtifacts, size);
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.cryosanctumPlayerBuildEnabled))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.cryosanctumPlayerBuildEnabled))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

