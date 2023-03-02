package dk.nydt.advancedguis.commands;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dk.nydt.advancedguis.AdvancedGuis;
import dk.nydt.sscore.api.builders.SkullBuilder;
import dk.nydt.sscore.api.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dk.nydt.advancedguis.AdvancedGuis.config;
import static dk.nydt.advancedguis.AdvancedGuis.configYML;

public class AG implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        if(args.length == 0) {
            sendColoredString(AdvancedGuis.configYML.getStringList("Messages.help"), p);
            return true;
        } else if (args[0].equalsIgnoreCase("open")) {
            if (args.length == 2) {
                try {
                    String permission = AdvancedGuis.configYML.getString("GUIs." + args[1] + ".permission");
                    if (!(permission.equalsIgnoreCase("null"))) {
                        if (p.hasPermission(permission)) {
                            openInventory(args[1], p);
                            return true;
                        } else {
                            sendColoredString(AdvancedGuis.configYML.getStringList("Messages.no-permission"), p);
                            return false;
                        }
                    } else {
                        openInventory(args[1], p);
                        return true;
                    }

                } catch (Exception ignored) {
                    sendColoredString(AdvancedGuis.configYML.getStringList("Messages.non-existent"), p);
                    return false;
                }

            } else {
                sendColoredString(AdvancedGuis.configYML.getStringList("Messages.non-existent"), p);
                return false;
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (p.hasPermission(AdvancedGuis.configYML.getString("Permissions.reload"))) {
                config.reloadConfig();
                configYML = config.getConfig();
                sendColoredString(AdvancedGuis.configYML.getStringList("Messages.reload"), p);
                return true;
            } else {
                sendColoredString(AdvancedGuis.configYML.getStringList("Messages.no-permission"), p);
                return false;
            }
        }
        return false;
    }

    private void openInventory(String arg, Player p) {
        String name = AdvancedGuis.configYML.getString("GUIs." + arg + ".name");
        name = name.replace("%gui%", arg);
        int rows = AdvancedGuis.configYML.getInt("GUIs." + arg + ".rows");

        Gui inv = Gui.gui()
                .title(Component.text(ColorUtils.getColored(name)))
                .rows(rows)
                .create();

        List<Map<?, ?>> items = configYML.getMapList("GUIs." + arg + ".slots");
        for (Map<?, ?> item : items) {
            Integer slot = (Integer) item.get("slot");

            ItemStack itemStack;
            String itemName;

            itemName = (String) item.get("name");

            int amount = 1;
            if (item.containsKey("amount")) {
                amount = (int) item.get("amount");
            }

            if (item.containsValue("CUSTOM_SKULL")) {
                String url = item.get("value").toString();
                itemStack = SkullBuilder.itemFromUrl(url);
                itemStack.setAmount(amount);
            } else {
                Material itemType = Material.getMaterial((String) item.get("item"));
                short itemData;
                if (item.containsKey("data")) {
                    itemData = ((Integer) item.get("data")).shortValue();
                } else {
                    itemData = 0;
                }

                itemStack = new ItemStack(itemType, amount, itemData);
            }

            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(ColorUtils.getColored(itemName));
            if (item.containsKey("lore")) {
                List<?> loreList = (List<?>) item.get("lore");
                List<String> lore = new ArrayList<>();
                for (Object obj : loreList) {
                    lore.add(ColorUtils.getColored(obj.toString()));
                }
                meta.setLore(lore);
            }
            itemStack.setItemMeta(meta);

            GuiItem guiItem = ItemBuilder.from(itemStack).asGuiItem(event -> {
                event.setCancelled(true);
                if (item.containsKey("message")) {
                    List<?> messages = (List<?>) item.get("message");
                    for (Object m : messages) {
                        m = m.toString().replace("%player%", p.getName());
                        p.sendMessage(ColorUtils.getColored(m.toString()));
                    }
                }
                if (item.containsKey("p-command")) {
                    List<?> commands = (List<?>) item.get("p-command");
                    for (Object c : commands) {
                        c = c.toString().replace("%player%", p.getName());
                        p.performCommand(c.toString());
                    }
                }
                if (item.containsKey("c-command")) {
                    List<?> commands = (List<?>) item.get("c-command");
                    for (Object c : commands) {
                        c = c.toString().replace("%player%", p.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.toString());
                    }
                }
            });

            inv.setItem(slot, guiItem);
        }
        inv.open(p);
    }

    private void sendColoredString(List<String> list, Player p) {
        for (String m : list) {
            p.sendMessage(ColorUtils.getColored(m));
        }
    }

}
