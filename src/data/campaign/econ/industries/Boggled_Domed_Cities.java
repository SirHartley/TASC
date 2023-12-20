package data.campaign.econ.industries;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import data.campaign.econ.boggledTools;

public class Boggled_Domed_Cities extends BaseIndustry implements MarketImmigrationModifier
{
    @Override
    public boolean canBeDisrupted()
    {
        if(!this.market.hasCondition(Conditions.WATER_SURFACE))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static float IMPROVE_STABILITY_BONUS = 1f;
    public static float DEFENSE_MALUS = 0.05f;

    public static float DEFENSE_BONUS = 6f;
    public static float ACCESSIBILITY_MALUS = -.10f;

    public static List<String> SUPPRESSED_CONDITIONS = new ArrayList<>();
    static
    {
        SUPPRESSED_CONDITIONS.add(Conditions.NO_ATMOSPHERE);
        SUPPRESSED_CONDITIONS.add(Conditions.THIN_ATMOSPHERE);
        SUPPRESSED_CONDITIONS.add(Conditions.DENSE_ATMOSPHERE);
        SUPPRESSED_CONDITIONS.add(Conditions.TOXIC_ATMOSPHERE);
        SUPPRESSED_CONDITIONS.add(Conditions.EXTREME_WEATHER);
        SUPPRESSED_CONDITIONS.add(Conditions.INIMICAL_BIOSPHERE);

        // need Alex to fix this re aquaculture interaction - suppressing water surface causes aquaculture to produce no food
        SUPPRESSED_CONDITIONS.add(Conditions.WATER_SURFACE);


        // Unknown Skies conditions
        // Suppression appears to actually remove all effects, not just hazard rating modifier.
        SUPPRESSED_CONDITIONS.add("US_storm");
        SUPPRESSED_CONDITIONS.add("US_virus");
        SUPPRESSED_CONDITIONS.add("US_shrooms");
        SUPPRESSED_CONDITIONS.add("US_mind");
    }

    @Override
    protected void buildingFinished()
    {
        super.buildingFinished();
    }

    @Override
    public void apply()
    {
        super.apply(true);

        // Reduces ground defense in Domed Cities mode, increases it in Seafloor Cities mode.
        if(!this.market.hasCondition(Conditions.WATER_SURFACE))
        {
            if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domedCitiesDefensePenaltyEnabled))
            {
                this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), DEFENSE_MALUS, getNameForModifier());
            }
        }
        else
        {
            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), DEFENSE_BONUS, getNameForModifier());
        }

        if(isFunctional())
        {
            for (String cid : SUPPRESSED_CONDITIONS)
            {
                if(cid.equals(Conditions.WATER_SURFACE) && this.market.hasCondition(Conditions.WATER_SURFACE))
                {
                    // Temporary hack to "suppress" water surface without actually suppressing it -
                    // actually suppressing it causes aquaculture to produce no food.
                    // Alex has to fix this in vanilla as far as I know.
                    float hazard = -0.25f;
                    this.market.getHazard().modifyFlat(this.getModId(), hazard, "Seafloor cities");
                }
                else
                {
                    market.suppressCondition(cid);
                }
            }

            // Reduces accessibility by 10% if in Seafloor Cities mode
            if(this.market.hasCondition(Conditions.WATER_SURFACE))
            {
                this.market.getAccessibilityMod().modifyFlat(this.getModId(), ACCESSIBILITY_MALUS, "Seafloor cities");
            }

            //Stability bonus
            if (this.aiCoreId == null)
            {
                this.market.getStability().unmodifyFlat(this.getModId());
            }
            else if (this.aiCoreId.equals(Commodities.GAMMA_CORE))
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)1, this.getNameForModifier());
            }
            else if (this.aiCoreId.equals(Commodities.BETA_CORE))
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)2, this.getNameForModifier());
            }
            else if (this.aiCoreId.equals(Commodities.ALPHA_CORE))
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)3, this.getNameForModifier());
            }
        }
    }

    @Override
    public void unapply()
    {
        for (String cid : SUPPRESSED_CONDITIONS)
        {
            market.unsuppressCondition(cid);
        }

        this.market.getStability().unmodifyFlat(this.getModId());
        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());

        // Seafloor cities modifiers
        this.market.getAccessibilityMod().unmodifyFlat(this.getModId());
        this.market.getHazard().unmodifyFlat(this.getModId());
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        MarketAPI market = this.market;

        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domedCitiesEnabled) || !boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
        {
            return false;
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        // Meteor impacts preclude building
        if(market.hasCondition(Conditions.METEOR_IMPACTS))
        {
            return false;
        }

        // Tectonic activity precludes building unless Harmonic Damper is built and functional.
        // There's no check to automatically remove Domed Cities if Harmonic Damper is deconstructed or disrupted.
        if(market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY) && (market.getIndustry(boggledTools.BoggledIndustries.harmonicDamperIndustryID) == null || !market.getIndustry(boggledTools.BoggledIndustries.harmonicDamperIndustryID).isFunctional()))
        {
            return false;
        }

        // Certain planet types preclude building unless they have the US condition Floating Continent (US_floating)
        if(boggledTools.getPlanetType(market.getPlanetEntity()).equals(boggledTools.gasGiantPlanetID) && !market.hasCondition("US_floating"))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domedCitiesEnabled) || !boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
        {
            return false;
        }

        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        return true;
    }

    @Override
    public String getUnavailableReason()
    {
        MarketAPI market = this.market;

        // Should never be seen because showWhenAvailable() will be false if either condition is true.
        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domedCitiesEnabled) || !boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
        {
            return "Error in getUnavailableReason() in Domed Cities. Please report this to boggled on the forums.";
        }

        // Should never be seen because showWhenAvailable() will be false if the market is a station.
        if(boggledTools.marketIsStation(this.market))
        {
            return "Error in getUnavailableReason() in Domed Cities. Please report this to boggled on the forums.";
        }

        String planetType = boggledTools.getPlanetType(market.getPlanetEntity());

        // Tectonic activity precludes building unless Harmonic Damper is built and functional.
        // There's no check to automatically remove Domed Cities if Harmonic Damper is deconstructed or disrupted.
        if(market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY) && (market.getIndustry(boggledTools.BoggledIndustries.harmonicDamperIndustryID) == null || !market.getIndustry(boggledTools.BoggledIndustries.harmonicDamperIndustryID).isFunctional()))
        {
            return market.getName() + " experiences frequent seismic events that could destroy megastructures. It would be too dangerous to construct one here.";
        }

        // Meteor impacts preclude building
        if(market.hasCondition(Conditions.METEOR_IMPACTS))
        {
            return market.getName() + " experiences frequent meteor impacts that could destroy megastructures. It would be too dangerous to construct one here.";
        }

        // Can't build on gas giants
        if(planetType.equals(boggledTools.gasGiantPlanetID) && !market.hasCondition("US_floating"))
        {
            return "There is no solid ground on " + market.getName() + " upon which to build a dome.";
        }

        return "Error in getUnavailableReason() in Domed Cities. Please report this to boggled on the forums.";
    }


    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        //Prevents AI cores from modifying upkeep
    }

    private void addAICoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode, String coreType, String highlights) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = coreType + "-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = coreType + "-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases stability by %s.", 0.0F, highlight, highlights);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases stability by %s.", opad, highlight, highlights);
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        addAICoreDescription(tooltip, mode, "Alpha", "3");
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        addAICoreDescription(tooltip, mode, "Beta", "2");
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        addAICoreDescription(tooltip, mode, "Gamma", "1");
    }

    @Override
    public String getCurrentName()
    {
        if(!this.market.hasCondition(Conditions.WATER_SURFACE))
        {
            return "Domed Cities";
        }
        else
        {
            return "Seafloor Cities";
        }
    }

    @Override
    public String getCurrentImage()
    {
        if(!this.market.hasCondition(Conditions.WATER_SURFACE))
        {
            return this.getSpec().getImageName();
        }
        else
        {
            return Global.getSettings().getSpriteName("boggled", "seafloor_cities");
        }
    }

    @Override
    protected String getDescriptionOverride()
    {
        if(!this.market.hasCondition(Conditions.WATER_SURFACE))
        {
            return null;
        }
        else
        {
            return "It's not impossible to build a city at the bottom of the sea - in fact, it's advantageous when defending against raids and bombardment. Most space marines lack experience operating in an underwater environment, and kinetic weapons are ineffective against submerged targets. However, traders are hampered by the underwater conditions, which makes the colony less accessible.";
        }
    }

    @Override
    public boolean canImprove()
    {
        return true;
    }

    @Override
    protected void applyImproveModifiers()
    {
        if (isImproved())
        {
            if(!this.market.hasCondition(Conditions.WATER_SURFACE))
            {
                market.getStability().modifyFlat("DOME_improve", IMPROVE_STABILITY_BONUS, getImprovementsDescForModifiers() + " (Domed cities)");
            }
            else
            {
                market.getStability().modifyFlat("DOME_improve", IMPROVE_STABILITY_BONUS, getImprovementsDescForModifiers() + " (Seafloor cities)");
            }
        }
        else
        {
            market.getStability().unmodifyFlat("DOME_improve");
        }
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode)
    {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            info.addPara("Stability increased by %s.", 0f, highlight, "" + (int) IMPROVE_STABILITY_BONUS);
        }
        else
        {
            info.addPara("Increases stability by %s.", 0f, highlight, "" + (int) IMPROVE_STABILITY_BONUS);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }

    @Override
    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }

    public void modifyIncoming(MarketAPI market, PopulationComposition incoming)
    {
        incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(this.getCurrentName().toLowerCase()));
    }

    protected float getImmigrationBonus()
    {
        return Math.max(0, market.getSize() - 1);
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        float opad = 10.0F;

        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED || !isFunctional())
        {
            tooltip.addPara("If operational, would counter the effects of:", opad, Misc.getHighlightColor(), "");
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
            }
        }

        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && isFunctional())
        {
            tooltip.addPara("Countering the effects of:", opad, Misc.getHighlightColor(), "");
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
            }
        }

        if(isFunctional())
        {
            tooltip.addPara("%s population growth (based on colony size)", 10f, Misc.getHighlightColor(), "+" + (int) getImmigrationBonus());
        }

        if(!this.market.hasCondition(Conditions.WATER_SURFACE))
        {
            if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domedCitiesDefensePenaltyEnabled))
            {
                tooltip.addPara("Ground defense strength: %s", opad, Misc.getNegativeHighlightColor(), new String[]{"x" + DEFENSE_MALUS});
            }
        }
        else
        {
            tooltip.addPara("Ground defense strength: %s", opad, Misc.getHighlightColor(), new String[]{"x" + DEFENSE_BONUS});
        }

        if(this.market.hasCondition(Conditions.WATER_SURFACE))
        {
            tooltip.addPara("Accessibility penalty: %s", opad, Misc.getNegativeHighlightColor(), "-10%");
        }
    }
}

