package dev.jsinco.brewery.garden.commands.subcomands;

import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.commands.AddonSubCommand;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.constants.GenericPlantType;
import dev.jsinco.brewery.garden.constants.PlantType;
import dev.jsinco.brewery.garden.constants.PlantTypeSeeds;
import dev.jsinco.brewery.garden.utility.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiveCommand implements AddonSubCommand {

    private final Map<String, GenericPlantType> items = new HashMap<>();

    {
        for (var plant : PlantType.values()) {
            items.put(plant.name().toLowerCase(), plant);
        }
        for (var seed : PlantTypeSeeds.values()) {
            items.put(seed.name().toLowerCase(), seed);
        }
    }

    @Override
    public boolean execute(BreweryGarden addon, BreweryGardenConfig config, CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        int amount = 1;
        if (args.length >= 2) {
            amount = Integer.parseInt(args[1]);
        }

        Player player = null;
        if (args.length >= 3) {
            player = Bukkit.getPlayerExact(args[2]);
        } else if (sender instanceof Player p) {
            player = p;
        }

        if (player == null) {
            return false;
        }

        GenericPlantType plantType = items.get(args[0].toLowerCase());
        ItemStack item = plantType.getItemStack(amount);
        if (item != null) {
            player.getInventory().addItem(item);
            MessageUtil.sendMessage(sender, "Gave &6x" + amount + " " + plantType.name().toLowerCase() + " &rto " + player.getName());
        } else {
            MessageUtil.sendMessage(sender, "Unknown item.");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(BreweryGarden addon, CommandSender sender, String label, String[] args) {
        if (args.length == 1) {
            return items.keySet().stream().toList();
        } else if (args.length == 2) {
            return List.of("1", "16", "24", "32", "48", "64");
        }
        return null;
    }

    @Override
    public String permission() {
        return "garden.command.give";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String usage(String label) {
        return "&e/" + label + "garden give <item!> <amount?> <player?>";
    }
}
