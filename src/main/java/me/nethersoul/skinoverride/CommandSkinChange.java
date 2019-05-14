package me.nethersoul.skinoverride;

import me.nethersoul.skinoverride.utils.FileUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.List;

public class CommandSkinChange implements IClientCommand {

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return "skin";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /skin [url]";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String name = sender.getName();

        if (args.length < 1) {
            sender.sendMessage(new TextComponentString("[SkinOverride] " + this.getUsage(sender)));
            return;
        }

        String skin_url = args[0];

        try {
            new URL(skin_url);
        } catch (Exception ex) {
            sender.sendMessage(new TextComponentString("[SkinOverride] \247cPlease input a valid URL!"));
            return;
        }

        SkinOverride.addSkin(name, skin_url);
        ConfigManager.sync(SkinOverride.MODID, Config.Type.INSTANCE);

        sender.sendMessage(new TextComponentString("[SkinOverride] \247bSkin uploaded correctly. Please wait until it gets updated ingame (max 5 seconds)."));
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) { return true; }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
