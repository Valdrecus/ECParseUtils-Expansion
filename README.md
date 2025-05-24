# ParseUtils-Expansion
PlaceholderAPI expansion for parsing and retrieving data of other players.

# Usage
### 🕒 Playtime
`%parseutils_seconds_played_[Notch]%`
→ 123456
Total playtime in seconds.

`%parseutils_minutes_played_[Notch]%`
→ 2057
Total playtime in minutes.

`%parseutils_hours_played_[Notch]%`
→ 34
Total playtime in hours.

`%parseutils_days_played_[Notch]%`
→ 1
Total playtime in days.

Example using `formatted:` > `%parseutils_formatted:hours_played_[Notch]%`
→ 1,234
Playtime hours formatted with thousands separator formatting.

### 🧍 Player Information
`%parseutils_uuid_[Notch]%`
→ 069a79f4-44e9-4726-a5be-fca90e38aaf5
Player's UUID.

`%parseutils_realname_[nOtCh]%`
→ Notch
Player’s real name (case-sensitive).

`%parseutils_first_joined_[Notch]%`  
→ May 22, 2025 at 2:04 AM  
First join date in UTC time.  

Using parameters:  
`%parseutils_first_joined_[Notch]_<timezone>_<locale>%`  
→ Returns first join date in specified timezone and language format.  

Examples:  
`%parseutils_first_joined_[Notch]_America/New_York_en_US%`  
→ May 21, 2025 10:04 PM (EDT, English format)  

`%parseutils_first_joined_[Notch]_Europe/Madrid_es_ES%`  
→ 22 may. 2025 4:04 AM (CEST, Spanish format)  

`%parseutils_first_joined_[Notch]_Asia/Tokyo_ja_JP%`  
→ 5月22日 2025 11:04 午前 (JST, Japanese format)  

:warning: **Invalid parameters default to UTC/English format.**  

Timezone (Use TZ Identifier): https://en.wikipedia.org/wiki/List_of_tz_database_time_zones  
Locale (Use ID): https://www.localeplanet.com/icu/ (Valid format: idiom_REGION)  

### 📅 Last seen
`%parseutils_lastseen_seconds_[Notch]%`  
→ 540  
Seconds since last online.

`%parseutils_lastseen_minutes_[Notch]%`  
→ 9  
Minutes since last online.

`%parseutils_lastseen_hours_[Notch]%`  
→ 0  
Hours since last online.

`%parseutils_lastseen_days_[Notch]%`  
→ 0  
Days since last online.

Example using `formatted:` > `%parseutils_lastseen_formatted:minutes_[Notch]%`  
→ 1,234  
Minutes since last online with thousands separator formatting.

### Parsing other placeholders
`%parseutils_parseother:[Notch]_placeholder%`  
Example using a placeholder inside another placeholder > `%parseutils_parseother:[Notch]_formatter_text_uppercase_{player_name}%`  
→ NOTCH

# Behavior

- If the player is online, the `lastseen_`* placeholders return "online".
- If the player has never joined the server, any placeholder returns "`PLAYER_NOT_FOUND`".
- All player names are case-insensitive.
- The player name must be enclosed in brackets: [player].
