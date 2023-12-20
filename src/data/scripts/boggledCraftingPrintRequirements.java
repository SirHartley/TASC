package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledCraftingPrintRequirements extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledCraftingPrintRequirements() {}

    public boggledCraftingPrintRequirements(SectorEntityToken entity) {
        this.init(entity);
    }

    protected void init(SectorEntityToken entity)
    {
        this.entity = entity;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if(dialog == null) return false;

        this.entity = dialog.getInteractionTarget();
        TextPanelAPI text = dialog.getTextPanel();

        if(this.entity.getMarket() == null)
        {
            return true;
        }
        else
        {
            Color good = Misc.getPositiveHighlightColor();
            Color bad = Misc.getNegativeHighlightColor();

            MarketAPI market = this.entity.getMarket();

            // Print crafting requirements
            String itemToCraftString = null;
            String craftingProjectId = null;
            if(ruleId.contains("CorruptedNanoforge"))
            {
                itemToCraftString = "a corrupted nanoforge";
                craftingProjectId = boggledTools.craftCorruptedNanoforgeProjectID;
            }
            else if(ruleId.contains("PristineNanoforge"))
            {
                itemToCraftString = "a pristine nanoforge";
                craftingProjectId = boggledTools.craftPristineNanoforgeProjectID;
            }
            else if(ruleId.contains("SynchrotronCore"))
            {
                itemToCraftString = "a synchrotron core";
                craftingProjectId = boggledTools.craftSynchrotronProjectID;
            }
            else if(ruleId.contains("HypershuntTap"))
            {
                itemToCraftString = "hypershunt tap";
                craftingProjectId = boggledTools.craftHypershuntTapProjectID;
            }
            else if(ruleId.contains("CryoarithmeticEngine"))
            {
                itemToCraftString = "a cryoarithmetic engine";
                craftingProjectId = boggledTools.craftCryoarithmeticEngineProjectID;
            }
            else if(ruleId.contains("PlanetKillerDevice"))
            {
                itemToCraftString = "a planet-killer device";
                craftingProjectId = boggledTools.craftPlanetKillerDeviceProjectID;
            }
            else if(ruleId.contains("FusionLamp"))
            {
                itemToCraftString = "a fusion lamp";
                craftingProjectId = boggledTools.craftFusionLampProjectID;
            }
            else if(ruleId.contains("FullereneSpool"))
            {
                itemToCraftString = "a fullerene spool";
                craftingProjectId = boggledTools.craftFullereneSpoolProjectID;
            }
            else if(ruleId.contains("PlasmaDynamo"))
            {
                itemToCraftString = "a plasma dynamo";
                craftingProjectId = boggledTools.craftPlasmaDynamoProjectID;
            }
            else if(ruleId.contains("AutonomousMantleBore"))
            {
                itemToCraftString = "an autonomous mantle bore";
                craftingProjectId = boggledTools.craftAutonomousMantleBoreProjectID;
            }
            else if(ruleId.contains("SoilNanites"))
            {
                itemToCraftString = "soil nanites";
                craftingProjectId = boggledTools.craftSoilNanitesProjectID;
            }
            else if(ruleId.contains("CatalyticCore"))
            {
                itemToCraftString = "a catalytic core";
                craftingProjectId = boggledTools.craftCatalyticCoreProjectID;
            }
            else if(ruleId.contains("CombatDroneReplicator"))
            {
                itemToCraftString = "a combat drone replicator";
                craftingProjectId = boggledTools.craftCombatDroneReplicatorProjectID;
            }
            else if(ruleId.contains("BiofactoryEmbryo"))
            {
                itemToCraftString = "a biofactory embryo";
                craftingProjectId = boggledTools.craftBiofactoryEmbryoProjectID;
            }
            else if(ruleId.contains("DealmakerHolosuite"))
            {
                itemToCraftString = "a dealmaker holosuite";
                craftingProjectId = boggledTools.craftDealmakerHolosuiteProjectID;
            }

            text.addPara("Requirements to craft " +  itemToCraftString + ":");
            boggledTools.TerraformingProject craftingProject = boggledTools.getCraftingProject(craftingProjectId);
            if (craftingProject != null) {
                for (boggledTools.TerraformingRequirements craftingRequirements : craftingProject.getProjectRequirements()) {
                    if (craftingRequirements.checkRequirement(market)) {
                        text.addPara("      - %s", good, craftingRequirements.getTooltip());
                    } else {
                        text.addPara("      - %s", bad, craftingRequirements.getTooltip());
                    }
                }
            }
        }

        return true;
    }
}