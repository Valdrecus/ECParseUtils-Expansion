<p align="center">
  <img src="server-icon.png" alt="Logo" width="128">
</p>

<h1>🧩 ParseUtils-Expansion</h1>

<p>
  An advanced <a href="https://github.com/PlaceholderAPI/PlaceholderAPI">PlaceholderAPI</a> expansion designed to retrieve and format
  <strong>detailed player-specific data</strong>, including <strong>playtime, ranks, experience, location, statistics</strong>, and more — even for <strong>offline players</strong>.
</p>
<p>
  This fork is tailored for <strong>Minecraft 1.17+</strong>, fully compatible with <strong>modern Paper-based servers</strong> and built to work seamlessly alongside popular plugins such as <strong>LuckPerms</strong>, <strong>Vault</strong>, <strong>EssentialsX</strong>, and external APIs for advanced features.
</p>

<hr>

<h2>✨ Features</h2>
<ul>
  <li>✅ Fully PlaceholderAPI-compatible</li>
  <li>🌐 Supports UUID, GeoIP, rank info, join dates, and more</li>
  <li>🧍 Works with online and offline players</li>
  <li>⚙️ External integrations: LuckPerms, Vault, EssentialsX</li>
  <li>🧠 Smart parsing system (e.g., thousands separator, formatting options)</li>
  <li>📦 Supports dynamic formatting with locale and timezone</li>
</ul>

<hr>

<h2>🔧 Usage</h2>

<h3>🕒 Playtime</h3>
<pre>
%ecparseutils_seconds_played_[Notch]% → 123456
%ecparseutils_minutes_played_[Notch]% → 2057
%ecparseutils_hours_played_[Notch]% → 34
%ecparseutils_days_played_[Notch]% → 1
</pre>

<p><strong>Formatted:</strong></p>
<pre>%ecparseutils_formatted:hours_played_[Notch]% → 1,234</pre>

<hr>

<h3>🧍 Player Information</h3>
<pre>
%ecparseutils_uuid_[Notch]% → 069a79f4-44e9-4726-a5be-fca90e38aaf5
%ecparseutils_realname_[nOtCh]% → Notch
%ecparseutils_first_joined_[Notch]% → May 22, 2025 at 2:04 AM
</pre>

<p><strong>With timezone and locale:</strong></p>
<pre>
%ecparseutils_first_joined_[Notch]_America/New_York_en_US% → May 21, 2025 10:04 PM
%ecparseutils_first_joined_[Notch]_Europe/Madrid_es_ES% → 22 may. 2025 4:04 AM
%ecparseutils_first_joined_[Notch]_Asia/Tokyo_ja_JP% → 5月22日 2025 11:04 午前
</pre>

<p><strong>⚠️ Invalid parameters fallback to UTC / English.</strong></p>
<p>
  <a href="https://en.wikipedia.org/wiki/List_of_tz_database_time_zones">🌐 TZ List</a> |
  <a href="https://www.localeplanet.com/icu/">🗣️ Locale List</a>
</p>

<pre>
%ecparseutils_first_joined_[Notch]% → May 22, 2025 at 2:04 AM
</pre>

<hr>

<h3>📅 Last Seen</h3>
<pre>
%ecparseutils_lastseen_seconds_[Notch]% → 540
%ecparseutils_lastseen_minutes_[Notch]% → 9
%ecparseutils_lastseen_hours_[Notch]% → 0
%ecparseutils_lastseen_days_[Notch]% → 0
</pre>

<p><strong>Formatted:</strong></p>
<pre>%ECparseutils_lastseen_formatted:minutes_[Notch]% → 1,234</pre>

<hr>

<h3>📊 Player Stats & Metadata</h3>
<pre>
%ecparseutils_exp_[Notch]% → 527
%ecparseutils_fish_caught_[Notch]% → 43
%ecparseutils_time_since_death_[Notch]% → 00:12:56
%ecparseutils_totems_used_[Notch]% → 2
%ecparseutils_health_[Notch]% → 18.0
</pre>

<hr>

<h3>💸 Economy & Rank</h3>
<pre>
%ecparseutils_balance_[Notch]% → 1,200.75
%ecparseutils_rank_[Notch]% → Knight
%ecparseutils_rank_expire_[Notch]% → Knight (expires in 2d 4h)
</pre>

<hr>

<h3>🌍 Network & Location</h3>
<pre>
%ecparseutils_proxy_[Notch]% → false
%ecparseutils_geoip_[Notch]% → 🇵🇪 Peru - Lima (IP: xxx.xxx.xxx.xxx)
</pre>

<hr>

<h3>💤 EssentialsX</h3>
<pre>
%ecparseutils_afk_time_[Notch]% → 00:03:21
</pre>

<hr>

<h3>🧠 Advanced Placeholder Parsing</h3>
<pre>
%parseutils_parseother:[Notch]_placeholder%
%parseutils_parseother:[Notch]_formatter_text_uppercase_{player_name}% → NOTCH
</pre>

<hr>

<h2>⚙️ Behavior</h2>
<ul>
  <li>If the player is <strong>online</strong>, the <code>lastseen_</code> placeholders return <code>"online"</code>.</li>
  <li>If the player <strong>has never joined</strong>, all placeholders return <code>"PLAYER_NOT_FOUND"</code>.</li>
  <li>Player names are <strong>case-insensitive</strong>, but must be wrapped in brackets: <code>[Player]</code>.</li>
</ul>

<hr>

<h2>📦 Dependencies</h2>
<ul>
  <li><a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a></li>
  <li><a href="https://www.spigotmc.org/resources/vault.34315/">Vault</a> (for rank and balance)</li>
  <li><a href="https://luckperms.net/">LuckPerms</a> (for rank_expire)</li>
  <li><a href="https://essentialsx.net/">EssentialsX</a> (for afk_time and balance)</li>
  <li>External IP Geolocation API</li>
</ul>
