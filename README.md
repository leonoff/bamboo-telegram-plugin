Bamboo Telegram Plugin
=========

A simple notification plugin that allows using a Telegram messenger as Notification Recipient.

Installation
---------
* Setup [Atlassian SDK](https://developer.atlassian.com/docs/getting-started/set-up-the-atlassian-plugin-sdk-and-build-a-project)

* Build plugin
```
$ git clone myrepo
$ cd mydir
$ atlas-package
```

* [Install plugin](https://confluence.atlassian.com/display/UPM/Installing+add-ons) into Bamboo
* Setup Notifications for Build
  * Event: *All Builds Completed*
  * Recipient type: *Telegram*
  * Bot API Token: *key obtained from Telegram @BotFather*
  * Chat identifier: *identifier of the target chat (group or user)*


Additional Guides
----------------
* [Create Telegram Bot](https://core.telegram.org/bots#3-how-do-i-create-a-bot)
* Setup Telegram Bot
  * Talk with @BotFather and enable `/setjoingroups`
  * Create a group chat and add Bot to the group
  * Send a initial command to chat. e.g. [/start@YourNameBot](https://core.telegram.org/bots#commands)
  * You could get chat ID from [Telegram API](https://core.telegram.org/bots/api/) e.g.
```
$ curl -s "https://api.telegram.org/botBOT_TOKEN/getUpdates" | json_pp
{
   [
      {
         "message" : {
            "entities" : [
               {
                  "type" : "bot_command",
                  "offset" : 0,
                  "length" : 6
               }
            ],
            "from" : {
               "first_name" : "Mikhail",
               "last_name" : "Leonov",
               "id" : 1000101000
            },
            "date" : 1476823322,
            "chat" : {
               "type" : "private",
               "first_name" : "Mikhail",
               "id" : 163021324,
               "last_name" : "Leonov"
            },
            "message_id" : 10,
            "text" : "/start"
         },
         "update_id" : 57242258
      }
   ]
}

```