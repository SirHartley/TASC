package data.campaign.econ.industries;

import java.awt.*;
import java.lang.String;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import data.campaign.econ.boggledTools;

public class Boggled_Planet_Cracker extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    private int daysWithoutShortage = 0;
    private int lastDayChecked = 0;
    public static int requiredDaysToCrack = 200;

    private SectorEntityToken getOrbitFocus()
    {
        return this.market.getPrimaryEntity().getOrbitFocus();
    }

    private MarketAPI getFocusMarket()
    {
        return this.market.getPrimaryEntity().getOrbitFocus().getMarket();
    }

    private boolean marketSuitableForCracker()
    {
        // Only buildable on stations
        if(!boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        // Station needs to obit a non-gas giant planet
        SectorEntityToken orbitFocus = getOrbitFocus();
        if(orbitFocus == null)
        {
            return false;
        }

        if(orbitFocus.getMarket() == null || orbitFocus.getMarket().getPlanetEntity() == null || boggledTools.getPlanetType(orbitFocus.getMarket().getPlanetEntity()).equals(boggledTools.gasGiantPlanetID))
        {
            return false;
        }

        // Can't already have tectonic activity
        MarketAPI focusMarket = getFocusMarket();
        if(focusMarket.hasCondition(Conditions.TECTONIC_ACTIVITY) || focusMarket.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY))
        {
            return false;
        }

        // Can't already have maxed out resources
        if(focusMarket.hasCondition(Conditions.ORE_ULTRARICH) && focusMarket.hasCondition(Conditions.RARE_ORE_ULTRARICH))
        {
            return false;
        }

        return true;
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        if(this.marketSuitableForCracker() && this.isFunctional())
        {
            CampaignClockAPI clock = Global.getSector().getClock();

            if(clock.getDay() != lastDayChecked)
            {
                daysWithoutShortage++;
                lastDayChecked = clock.getDay();

                if(daysWithoutShortage >= requiredDaysToCrack)
                {
                    if (this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Planet cracking on " + getFocusMarket().getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Completed");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                    }

                    boggledTools.incrementOreForPlanetCracking(getFocusMarket());

                    boggledTools.addCondition(getFocusMarket(), Conditions.TECTONIC_ACTIVITY);

                    boggledTools.surveyAll(getFocusMarket());
                    boggledTools.refreshSupplyAndDemand(getFocusMarket());
                    boggledTools.refreshAquacultureAndFarming(getFocusMarket());
                }
            }
        }
    }

    public void apply()
    {
        super.apply(true);
    }

    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.planetCrackerEnabled))
        {
            if(this.marketSuitableForCracker())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.planetCrackerEnabled) && boggledTools.marketIsStation(this.market))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public String getUnavailableReason()
    {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.planetCrackerEnabled))
        {
            // Station needs to obit a non-gas giant planet
            SectorEntityToken orbitFocus = getOrbitFocus();
            if (orbitFocus == null)
            {
                return this.market.getName() + " is not in orbit around a planet.";
            }

            if (orbitFocus.getMarket() == null || orbitFocus.getMarket().getPlanetEntity() == null || boggledTools.getPlanetType(orbitFocus.getMarket().getPlanetEntity()).equals(boggledTools.gasGiantPlanetID))
            {
                return "Gas giants cannot be cracked.";
            }

            // Can't already have tectonic activity
            MarketAPI focusMarket = getFocusMarket();
            if (focusMarket.hasCondition(Conditions.TECTONIC_ACTIVITY) || focusMarket.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY))
            {
                return getFocusMarket().getName() + " already has tectonic activity - making it worse won't increase ore availability.";
            }

            // Can't already have maxed out resources
            if (focusMarket.hasCondition(Conditions.ORE_ULTRARICH) && focusMarket.hasCondition(Conditions.RARE_ORE_ULTRARICH))
            {
                return getFocusMarket().getName() + " already has easily accessible ore deposits. Cracking the planet would serve no purpose.";
            }

        }
        return "Error in getUnavailableReason() in Planet Cracker. Please tell Boggled about this on the forums.";
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        daysWithoutShortage = 0;
        lastDayChecked = 0;

        super.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();

        //Inserts cracking status after description
        if(this.marketSuitableForCracker() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            //200 days; divide daysWithoutShortage by 2 to get the percent
            int percentComplete = daysWithoutShortage / 2;

            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            tooltip.addPara("Planet cracking is approximately %s complete on " + getFocusMarket().getName() + ".", opad, highlight, new String[]{percentComplete + "%"});
        }

        // Tell the player they can remove it
        if(!this.marketSuitableForCracker() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Further planet cracking operations would serve no purpose on " + getFocusMarket().getName() + ". The Planet Cracker can now be deconstructed without any risk of regression.", opad);
        }

        if(this.isDisrupted() && this.marketSuitableForCracker() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            Color bad = Misc.getNegativeHighlightColor();
            tooltip.addPara("Progress is stalled while the planet cracker is disrupted.", bad, opad);
        }
    }

    @Override
    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }

    @Override
    public boolean canImprove() { return false; }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}

