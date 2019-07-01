# CodinGame-Bot

**NOTICE: I forgot to build the NPE fix and publish it. Since I don’t have access to my computer, the previous version is currently running. Only usernames that can be matched exactly will work**

Discord HackWeek: bot for [CodinGame](https://www.codingame.com/)

CodinGame does not have an official API, nor is their unofficial API documented, so a LOT of time was spent reverse-engineering it.

**The developers are OK with this as long as requests are not spammed**

One command: profile

The prefix is a mention

Download jarfile and view run instructions via the releases page.

You can test the bot [here](https://discord.gg/Xa94RwU)

Known bugs:
- user lookup by handle is broken
- achievement priority is broken for users who have gained a large number of Legend ("PLATINUM") achievements
- the bio can probably go over the char limit for embeds
- misspelled "matching" as "mathcing"
- literally everything

Dependencies:
- JDA 3: https://github.com/DV8FromTheWorld/JDA
- OkHttp 3: https://github.com/square/okhttp
- org.json. Not sure this is open source, but it comes with JDA.
- JCH: https://github.com/BenjaminUrquhart/JCH

![aaa](https://chat.is-going-to-rickroll.me/i/MSGRtVrwVcfZVQ.png)
