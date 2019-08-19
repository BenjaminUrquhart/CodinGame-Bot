# CodinGame-Bot

Discord HackWeek: bot for [CodinGame](https://www.codingame.com/)

CodinGame does not have an official API, nor is their unofficial API documented, so a LOT of time was spent reverse-engineering it.

**The developers are OK with this as long as requests are not spammed**

Commands:
profile,
puzzle

The prefix is a mention

Download jarfile and view run instructions via the releases page.

You can test the bot [here](https://discord.gg/Xa94RwU)

Known bugs:
- user lookup by handle is broken
- achievement priority is broken for users who have gained a large number of Legend ("PLATINUM") achievements
- the bio can probably go over the char limit for embeds
- This:
```
.statement_wrapping_div, .statement {font-size: 14px;font-weight: 400;}
.statement_wrapping_div {margin-bottom: 10px;}
.statement_wrapping_div ul {padding-left: 40px;}
.statement {background-color:rgba(255, 255, 255, 0.85);padding: 20px;padding-top:0px;}
code, span.const {display:inline-block;font-family:Inconsolata,consolas,monospace;padding-left:5px;padding-right:5px;padding-top:1px;padding-bottom:1px;background-color:#18a1ea;white-space: nowrap;margin-right:2px;color: white;}
span.var {display: inline-block;padding-left:5px;padding-right:5px;padding-top:1px;padding-bottom:2px;background-color:#f2dd00;white-space:nowrap;margin-left:1px;margin-right:2px;color:black;}
.stat_mid_title {font-weight:700;margin-bottom:5px;}
.protocol_title{color:#ffd200;}
.cross-statementBlock{position:relative;}
```

Dependencies:
- JDA 3: https://github.com/DV8FromTheWorld/JDA
- OkHttp 3: https://github.com/square/okhttp
- Apache Commons Text: http://commons.apache.org/proper/commons-text/download_text.cgi
- org.json. Not sure this is open source, but it comes with JDA.
- JCH: https://github.com/BenjaminUrquhart/JCH
