package com.ftxeven.parseutils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ParseUtils extends PlaceholderExpansion {

    private static final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "parseutils";
    }

    @Override
    public String getAuthor() {
        return "ftxeven";
    }

    @Override
    public String getVersion() {
        return "1.5";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier == null || !identifier.contains("[")) return null;

        if (identifier.startsWith("parseother:[")) {
            int bracketEnd = identifier.indexOf("]_{");
            String innerPlaceholder;

            if (bracketEnd != -1 && identifier.endsWith("}")) {
                String targetName = identifier.substring("parseother:[".length(), bracketEnd);
                innerPlaceholder = identifier.substring(bracketEnd + 3, identifier.length() - 1);

                if (targetName.isEmpty() || innerPlaceholder.isEmpty()) return "PLAYER_NOT_FOUND";

                OfflinePlayer otherTarget = findPlayerOptimized(targetName);
                if (otherTarget == null || (!otherTarget.hasPlayedBefore() && !otherTarget.isOnline())) {
                    return "PLAYER_NOT_FOUND";
                }

                while (innerPlaceholder.contains("{") && innerPlaceholder.contains("}")) {
                    int open = innerPlaceholder.lastIndexOf('{');
                    int close = innerPlaceholder.indexOf('}', open);
                    if (open == -1 || close == -1) break;

                    String inner = innerPlaceholder.substring(open + 1, close);
                    String parsedInner = PlaceholderAPI.setPlaceholders(otherTarget, "%" + inner + "%");
                    innerPlaceholder = innerPlaceholder.substring(0, open) + parsedInner + innerPlaceholder.substring(close + 1);
                }

                String finalParsed = PlaceholderAPI.setPlaceholders(otherTarget, "%" + innerPlaceholder + "%");
                return (finalParsed == null || finalParsed.isEmpty()) ? "" : finalParsed;

            } else {
                int splitIndex = identifier.indexOf("]_");
                if (splitIndex == -1) return "INVALID_FORMAT";

                String targetName = identifier.substring("parseother:[".length(), splitIndex);
                innerPlaceholder = identifier.substring(splitIndex + 2);

                if (targetName.isEmpty() || innerPlaceholder.isEmpty()) return "PLAYER_NOT_FOUND";

                OfflinePlayer otherTarget = findPlayerOptimized(targetName);
                if (otherTarget == null || (!otherTarget.hasPlayedBefore() && !otherTarget.isOnline())) {
                    return "PLAYER_NOT_FOUND";
                }

                String finalParsed = PlaceholderAPI.setPlaceholders(otherTarget, "%" + innerPlaceholder + "%");
                return (finalParsed == null || finalParsed.isEmpty()) ? "" : finalParsed;
            }
        }

        boolean formatted = false;
        String type;
        String playerName;

        if (identifier.startsWith("lastseen_formatted:")) {
            int splitIndex = identifier.indexOf("_[");
            if (splitIndex == -1) return null;
            formatted = true;
            type = "lastseen_" + identifier.substring("lastseen_formatted:".length(), splitIndex).toLowerCase(Locale.ROOT);
            playerName = extractName(identifier.substring(splitIndex + 1));
        } else if (identifier.startsWith("formatted:")) {
            int splitIndex = identifier.indexOf("_[");
            if (splitIndex == -1) return null;
            formatted = true;
            type = identifier.substring("formatted:".length(), splitIndex).toLowerCase(Locale.ROOT);
            playerName = extractName(identifier.substring(splitIndex + 1));
        } else if (identifier.contains("_[") && identifier.endsWith("]")) {
            int splitIndex = identifier.indexOf("_[");
            type = identifier.substring(0, splitIndex).toLowerCase(Locale.ROOT);
            playerName = extractName(identifier.substring(splitIndex + 1));
        } else {
            return null;
        }

        if (playerName == null || playerName.isEmpty()) return "PLAYER_NOT_FOUND";

        OfflinePlayer target = findPlayerOptimized(playerName);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            return "PLAYER_NOT_FOUND";
        }

        switch (type) {
            case "seconds_played":
            case "minutes_played":
            case "hours_played":
            case "days_played": {
                int ticks = target.getStatistic(Statistic.PLAY_ONE_MINUTE);
                int seconds = ticks / 20;
                long value = switch (type) {
                    case "seconds_played" -> seconds;
                    case "minutes_played" -> seconds / 60;
                    case "hours_played" -> seconds / 3600;
                    case "days_played" -> seconds / 86400;
                    default -> 0;
                };
                return formatted ? formatter.format(value) : String.valueOf(value);
            }
            case "uuid": {
                return target.getUniqueId().toString();
            }
            case "realname": {
                return target.getName();
            }
            case "first_joined": {
                long firstPlayed = target.getFirstPlayed();
                if (firstPlayed <= 0) return "PLAYER_NOT_FOUND";

                Date date = new Date(firstPlayed);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.ENGLISH);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.format(date);
            }
            case "lastseen_seconds":
            case "lastseen_minutes":
            case "lastseen_hours":
            case "lastseen_days": {
                if (target.isOnline()) return "online";

                long lastSeen = target.getLastPlayed();
                long now = System.currentTimeMillis();
                if (lastSeen <= 0 || lastSeen > now) return "PLAYER_NOT_FOUND";

                long diffMillis = now - lastSeen;
                long result = switch (type) {
                    case "lastseen_seconds" -> diffMillis / 1000;
                    case "lastseen_minutes" -> diffMillis / 60000;
                    case "lastseen_hours" -> diffMillis / (1000 * 60 * 60);
                    case "lastseen_days" -> diffMillis / (1000 * 60 * 60 * 24);
                    default -> 0;
                };
                return formatted ? formatter.format(result) : String.valueOf(result);
            }
            default:
                return null;
        }
    }

    private String extractName(String input) {
        if (input.startsWith("[") && input.endsWith("]")) {
            return input.substring(1, input.length() - 1);
        }
        return null;
    }

    private OfflinePlayer findPlayerOptimized(String name) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)) return p;
        }

        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            if (p.getName() != null && p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }

        if (Bukkit.getOnlineMode()) {
            return null;
        }

        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        OfflinePlayer fallback = Bukkit.getOfflinePlayer(uuid);
        return fallback.hasPlayedBefore() ? fallback : null;
    }
}