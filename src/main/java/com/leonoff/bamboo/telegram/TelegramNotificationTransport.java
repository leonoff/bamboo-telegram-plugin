package com.leonoff.bamboo.telegram;

import com.atlassian.bamboo.author.Author;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TelegramNotificationTransport implements NotificationTransport
{
    private static final Logger log = Logger.getLogger(TelegramNotificationTransport.class);

    private final String botToken;

    private final Long chatId;

    @Nullable
    private final ImmutablePlan plan;
    @Nullable
    private final ResultsSummary resultsSummary;
    @Nullable
    private final DeploymentResult deploymentResult;

    public TelegramNotificationTransport(String botToken,
                                      Long chatId,
                                      @Nullable ImmutablePlan plan,
                                      @Nullable ResultsSummary resultsSummary,
                                      @Nullable DeploymentResult deploymentResult)
    {
        this.botToken = botToken;
        this.chatId = chatId;
        this.plan = plan;
        this.resultsSummary = resultsSummary;
        this.deploymentResult = deploymentResult;
    }

    public void sendNotification(@NotNull Notification notification)
    {

        String message = notification.getIMContent();

        if (!StringUtils.isEmpty(message)) {
            if (resultsSummary != null) {
                if (resultsSummary.isSuccessful()) {
                    message = "\uD83D\uDE00 \uD83D\uDC4C" + message + resultsSummary.getReasonSummary();
                } else {
                    message = "\uD83D\uDE31 \uD83D\uDE45\u200D♂️" + message + resultsSummary.getReasonSummary();
                }

                Set<Author> authors = resultsSummary.getUniqueAuthors();
                if (!authors.isEmpty()) {
                    message += " Responsible Users: ";

                    ArrayList<String> usernames = new ArrayList<String>();

                    for (Author author: authors)
                    {
                        usernames.add(author.getFullName());
                    }

                    message += String.join(", ", usernames);
                }
            }

            try {
                TelegramBot bot = TelegramBotAdapter.build(botToken);
                SendMessage request = new SendMessage(chatId, message)
                        .parseMode(ParseMode.HTML);
                BaseResponse response = bot.execute(request);
                if (!response.isOk()) {
                    log.error("Error using telegram API. error code: " + response.errorCode() + " message: " + response.description());
                } else {
                    log.info("Success Telegram API message response: " + response.description() + " toString: " + response.toString());
                }
            } catch (RuntimeException e) {
                log.error("Error using telegram API: " + e.getMessage(), e);
            }
        }
    }
}
