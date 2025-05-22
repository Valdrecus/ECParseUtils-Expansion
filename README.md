# ParseUtils-Expansion
PlaceholderAPI expansion for parsing and retrieving data of other players.

# Usage
### 🕒 Playtime
`%parseutils_seconds_played[Notch]%`
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