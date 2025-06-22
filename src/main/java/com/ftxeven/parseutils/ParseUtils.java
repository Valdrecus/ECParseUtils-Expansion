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
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ParseUtils extends PlaceholderExpansion {

    private static final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "ECParseUtils";
    }

    @Override
    public String getAuthor() {
        return "ftxeven, Valdrecus";
    }

    @Override
    public String getVersion() {
        return "1.6";
        
    }
    @Override
    public String getDescription() {
        return "ParseUtils - Forked by Valdrecus, originally made by ftxeven. Adds useful player info placeholders.";
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
            case "ping": {
                if (target.isOnline() && target instanceof Player p) {
                    return p.getPing() + " ms";
                }
                return "Ping no disponible";
            }
            case "online_status": {
                boolean online = target.isOnline();
                return online ? "🟢 En línea" : "🔴 Desconectado";
            }
            case "exp": {
                if (target.isOnline() && target instanceof Player p) {
                    return String.valueOf(p.getTotalExperience());
                }
                return "Jugador desconectado";
            }
            case "fish_caught": {
                int fish = target.getStatistic(Statistic.FISH_CAUGHT);
                return formatted ? formatter.format(fish) : String.valueOf(fish);
            }
            case "time_since_death": {
                if (target.isOnline() && target instanceof Player p) {
                    int ticks = p.getStatistic(Statistic.TIME_SINCE_DEATH);
                    return String.valueOf(ticks / 20);
                }
                return "Desconectado";
            }
            case "rank": {
                if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return "Vault no encontrado";
            
                try {
                    RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp =
                        Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
                    if (rsp == null) return "Sin datos";
                    net.milkbowl.vault.permission.Permission perms = rsp.getProvider();
            
                    String[] groups = perms.getPlayerGroups(null, target);
                    if (groups.length == 0) return "Sin rango";
            
                    String mainGroup = groups[0]; // el principal
                    // Aquí podrías agregar soporte para tiempo restante con LuckPerms API
                    return mainGroup;
                } catch (Exception e) {
                    return "Error al obtener rango";
                }
            }
            case "rank_expire": {
                if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) return "LuckPerms no encontrado";
                try {
                    net.luckperms.api.LuckPerms luckPerms = net.luckperms.api.LuckPermsProvider.get();
                    net.luckperms.api.model.user.User lpUser = luckPerms.getUserManager().loadUser(target.getUniqueId()).join();
                    if (lpUser == null) return "Sin rango";
            
                    // Buscar el grupo con mayor peso
                    Optional<Map.Entry<String, Long>> result = lpUser.getNodes().stream()
                        .filter(n -> n.getType().isGroup())
                        .map(n -> (net.luckperms.api.node.types.InheritanceNode) n)
                        .map(node -> {
                            String group = node.getGroupName();
                            long expiresIn = node.hasExpiry() ? node.getExpiryDuration().getSeconds() : -1;
            
                            int weight = Optional.ofNullable(luckPerms.getGroupManager().getGroup(group))
                                .flatMap(g -> g.getWeight()).orElse(0);
            
                            return Map.entry(group + (expiresIn != -1 ? ":" + expiresIn : ""), (long) weight);
                        })
                        .max(Comparator.comparingLong(Map.Entry::getValue));
            
                    if (result.isEmpty()) return "Sin grupo";
            
                    String[] parts = result.get().getKey().split(":");
                    String groupName = parts[0];
                    long remaining = parts.length > 1 ? Long.parseLong(parts[1]) : -1;
            
                    if (remaining == -1) return groupName + " (Permanente)";
                    return groupName + " (" + formatDuration(remaining) + " restantes)";
            
                } catch (Exception e) {
                    return "Error al obtener rango";
                }
            }
            case "afk_time": {
                if (!Bukkit.getPluginManager().isPluginEnabled("Essentials")) return "Essentials no encontrado";
                try {
                    com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials)
                            Bukkit.getPluginManager().getPlugin("Essentials");
            
                    com.earth2me.essentials.User user = essentials.getUser(target.getUniqueId());
                    if (user == null || !user.isAfk()) return "0s";
            
                    long afkTime = (System.currentTimeMillis() - user.getAfkSince()) / 1000;
                    return afkTime + "s";
                } catch (Exception e) {
                    return "Error AFK";
                }
            }
            case "balance": {
                if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return "Vault no encontrado";
            
                try {
                    RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp =
                        Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                    if (rsp == null) return "Sin economía";
            
                    net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                    double balance = econ.getBalance(target);
                    return formatted ? formatter.format(balance) : String.format(Locale.US, "%.2f", balance);
                } catch (Exception e) {
                    return "Error al obtener saldo";
                }
            }
            case "totems_used": {
                if (target.isOnline() && target instanceof Player p) {
                    int totems = p.getStatistic(Statistic.USE_ITEM, Material.TOTEM_OF_UNDYING);
                    return String.valueOf(totems);
                }
                return "0";
            }
            case "proxy": {
                if (!target.isOnline() || !(target instanceof Player p)) return "Desconectado";
                String ip = Objects.requireNonNull(p.getAddress()).getAddress().getHostAddress();
            
                try {
                    URL url = new URL("http://ip-api.com/json/" + ip + "?fields=proxy,mobile,hosting,status");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(3000);
                    con.setReadTimeout(3000);
                    con.setRequestMethod("GET");
            
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json = in.lines().reduce("", (acc, line) -> acc + line);
                    in.close();
            
                    if (!json.contains("\"status\":\"success\"")) return "No detectado";
            
                    boolean isProxy = json.contains("\"proxy\":true") || json.contains("\"hosting\":true") || json.contains("\"mobile\":true");
            
                    return isProxy ? "🛡️ Usa VPN o Proxy" : "✅ IP limpia";
                } catch (Exception e) {
                    return "Error al verificar";
                }
            }
            case "geoip": {
                if (!target.isOnline() || !(target instanceof Player p)) return "Jugador desconectado";
            
                String ip = Objects.requireNonNull(p.getAddress()).getAddress().getHostAddress();
                try {
                    URL url = new URL("http://ip-api.com/json/" + ip + "?fields=status,country,city,countryCode,query");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(3000);
                    con.setReadTimeout(3000);
                    con.setRequestMethod("GET");
            
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
            
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
            
                    // Parse JSON
                    String json = response.toString();
                    if (!json.contains("\"status\":\"success\"")) return "Localización desconocida";
            
                    String country = getValue(json, "country");
                    String city = getValue(json, "city");
                    String flag = getFlag(getValue(json, "countryCode"));
            
                    return flag + " " + country + " - " + city + " (IP: " + ip + ")";
                } catch (Exception e) {
                    return "Error al obtener IP";
                }
            }
            case "health": {
                if (target.isOnline() && target instanceof Player p) {
                    double health = p.getHealth();
                    return String.format(Locale.US, "%.1f ♥", health);
                }
                return "Jugador desconectado";    
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
    private String getValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int index = json.indexOf(search);
        if (index == -1) return "";
        int start = index + search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
    private String formatDuration(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
    
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0 || sb.length() == 0) sb.append(minutes).append("m");
        return sb.toString().trim();
    }
    private String getFlag(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) return "";
        int first = Character.codePointAt(countryCode.toUpperCase(), 0) - 0x41 + 0x1F1E6;
        int second = Character.codePointAt(countryCode.toUpperCase(), 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
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
