package me.faun.givepet.Commands;

import mc.obliviate.bloksqliteapi.sqlutils.*;
import me.faun.givepet.GivePet;
import me.faun.givepet.PetManager;
import me.faun.givepet.Request;
import me.faun.givepet.SQL.SQLManager;
import me.faun.givepet.Utils.StringUtils;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;

@Command("givepet")
public class GivePetCommand extends CommandBase implements Listener {
    PetManager petManager = new PetManager();
    SQLManager sqlManager = new SQLManager(GivePet.getInstance());
    GivePet plugin = GivePet.getInstance();
    SQLTable requestsTable = plugin.getSqlTable();
    Request request;

    @Default
    @Completion("#players")
    public void defaultCommand(Player sender, String arg) {
        Player receiver = Bukkit.getPlayer(arg);
        if (receiver == null) {
            sender.sendMessage("Not a player");
            return;
        }

        if (receiver == sender) {
            sender.sendMessage("cannot transfer to self");
            return;
        }

        ResultSet resultSet = requestsTable.select("sender", sender.getUniqueId().toString());

        String time = StringUtils.unixToDate(System.currentTimeMillis());
        request = new Request(sender.getUniqueId(), receiver.getUniqueId(), time);

        if (sqlManager.isPending(resultSet)) {
            sender.sendMessage("pending...");
            return;
        }

        sqlManager.createRow(requestsTable, new String[]{String.valueOf(sender.getUniqueId()), String.valueOf(receiver.getUniqueId()), time, "pending"}, "sender");
        sender.sendMessage("created row. time: " + request.getTime());

        BukkitTask runnable = new BukkitRunnable() {
            @Override
            public void run() {
                switch (request.getAccepted().toLowerCase()) {
                    case "accepted":
                        System.out.println("finished is true.");
                        sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "accepted");
                        requestsTable.delete(sender.getUniqueId().toString());

                        sender.sendMessage(receiver.getDisplayName() + "accepted the request.");
                        sender.sendMessage("Click a pet that you own to give to " + receiver.getDisplayName() + "!");
                        petManager.addPDC(sender, arg);
                        cancel();
                        break;
                    case "rejected":
                        System.out.println("sender: " + request.getSender().toString() + " receiver: " + request.getReceiver().toString() + " time: " + request.getTime());
                        sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "rejected");
                        requestsTable.delete(sender.getUniqueId().toString());
                        cancel();
                        break;
                    case "pending":
                        System.out.println("finished is not yet accepted.");
                        break;
                }

            }
        }.runTaskTimerAsynchronously(GivePet.getInstance(), 20L, 20L);

        receiver.sendMessage(sender.getDisplayName() + " wants to give you their pet.");

        Bukkit.getScheduler().runTaskLater(GivePet.getInstance(), () -> {
            if (runnable.isCancelled()) {
                return;
            }

            System.out.println("request expired.");
            runnable.cancel();
            sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "accepted");
            requestsTable.delete(sender.getUniqueId().toString());

            sender.sendMessage(receiver.getDisplayName() + " did not accept in time.");
            receiver.sendMessage("Request expired.");
        }, 20L * 15);
    }

    @SubCommand("help")
    public void helpCommand(CommandSender sender) {
        sender.sendMessage("plz halp");
    }

    @SubCommand("reject")
    public void rejectRequest(CommandSender sender) {
        request.setAccepted("rejected");
        sender.sendMessage("You rejected the request!");
    }

    @SubCommand("accept")
    public void acceptRequest(CommandSender sender) {
        request.setAccepted("accepted");
        sender.sendMessage("You accepted the request!");
    }
}
