package com.ftxeven.parseutils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
        return "1.6";
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
        } else if (identifier.contains("_[") && identifier.contains("]")) {
            int splitIndex = identifier.indexOf("_[");
            String fullType = identifier.substring(0, splitIndex).toLowerCase(Locale.ROOT);
            type = fullType.startsWith("first_joined") ? "first_joined" : fullType;
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

                if (identifier.contains("]_") && identifier.indexOf("]_") < identifier.length() - 1) {
                    try {
                        String result = handleFirstJoinedWithTimezoneAndLocale(target, identifier);
                        if (result != null) {
                            return result;
                        }
                    } catch (Exception e) {}
                }

                Date date = new Date(firstPlayed);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.ENGLISH);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return capitalizeFirstLetter(sdf.format(date));
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

    private String handleFirstJoinedWithTimezoneAndLocale(OfflinePlayer target, String identifier) {
        long firstPlayed = target.getFirstPlayed();
        if (firstPlayed <= 0) return null;

        String paramsPart = identifier.substring(identifier.indexOf("]_") + 2);
        String[] parts = paramsPart.split("_");
        if (parts.length < 2) return null;

        int lastUnderscore = paramsPart.lastIndexOf('_');
        int prevUnderscore = paramsPart.lastIndexOf('_', lastUnderscore - 1);
        if (prevUnderscore == -1) return null;

        String timezoneStr = paramsPart.substring(0, prevUnderscore);
        String localeStr = paramsPart.substring(prevUnderscore + 1);

        String[] localeParts = localeStr.split("_");
        Locale locale = localeParts.length == 1 ?
                new Locale(localeParts[0]) : new Locale(localeParts[0], localeParts[1]);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", locale);
        TimeZone tz = TimeZone.getTimeZone(timezoneStr);
        sdf.setTimeZone(tz);

        Date date = new Date(firstPlayed);
        return capitalizeFirstLetter(sdf.format(date));
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        for (int i = 0; i < input.length(); i++) {
            if (Character.isLetter(input.charAt(i))) {
                return input.substring(0, i) +
                        Character.toUpperCase(input.charAt(i)) +
                        input.substring(i + 1);
            }
        }
        return input;
    }

    private String extractName(String input) {
        if (input.startsWith("[") && input.contains("]")) {
            return input.substring(1, input.indexOf("]"));
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
