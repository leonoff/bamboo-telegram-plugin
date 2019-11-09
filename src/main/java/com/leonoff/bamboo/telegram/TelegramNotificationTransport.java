package com.leonoff.bamboo.telegram;

import com.atlassian.bamboo.author.Author;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.versions.DeploymentVersion;
import com.atlassian.bamboo.deployments.versions.service.DeploymentVersionLinkedJiraIssuesService;
import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.variable.VariableDefinition;
import com.atlassian.bamboo.variable.VariableSubstitution;
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
import java.util.stream.Collectors;

public class TelegramNotificationTransport implements NotificationTransport {
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
                                         @Nullable DeploymentResult deploymentResult) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.plan = plan;
        this.resultsSummary = resultsSummary;
        this.deploymentResult = deploymentResult;
    }

    public void sendNotification(@NotNull Notification notification) {

        final StringBuilder message = new StringBuilder();
        String imContent = notification.getIMContent();

        if (!StringUtils.isEmpty(imContent) ) {

            if ( resultsSummary != null ) {

                if (resultsSummary.isSuccessful()) {
                    message.append("\uD83D\uDE00 \uD83D\uDC4C ");
                } else {
                    message.append("\uD83D\uDE31 \uD83D\uDE45\u200D♂️ ");
                }
                message.append(imContent).append(resultsSummary.getReasonSummary()).append("\n");

                List<String> authorsNames = resultsSummary.getUniqueAuthors().stream().map(Author::getFullName).collect(Collectors.toList());
                if (!authorsNames.isEmpty()) {
                    message.append(" Responsible Users: ")
                            .append(String.join(", ", authorsNames))
                            .append("\n");
                }

                List<VariableSubstitution> variables = resultsSummary.getManuallyOverriddenVariables();
                if (!variables.isEmpty()) {
                    message.append("Variables: \n");
                    for (VariableSubstitution variable : variables) {
                        message.append(variable.getKey())
                                .append(": ")
                                .append(variable.getKey().contains("password") ? "******" : variable.getValue())
                                .append(" \n");
                    }
                }

                List<String> labels = resultsSummary.getLabelNames();
                if (!labels.isEmpty()) {
                    message.append("Labels: ")
                            .append(String.join(", ", labels))
                            .append("\n");
                }

                Set<LinkedJiraIssue> jiraIssues = resultsSummary.getRelatedJiraIssues();
                if (!jiraIssues.isEmpty()) {
                    message.append("Issues: \n");
                    for (LinkedJiraIssue issue : jiraIssues) {

                        if (issue.getJiraIssueDetails() == null) {
                            message.append(issue.getIssueKey());
                        } else {
                            message.append("<a href=\"")
                                    .append(issue.getJiraIssueDetails().getDisplayUrl())
                                    .append("\">")
                                    .append(issue.getIssueKey())
                                    .append("</a>")
                                    .append(" - ")
                                    .append(issue.getJiraIssueDetails().getSummary());
                        }
                        message.append("\n");
                    }
                }
            }

            if ( deploymentResult != null ) {
                message.append("Deployment notification. \n Reason: ");
                message.append(imContent).append(deploymentResult.getReasonSummary()).append("\n");

                message.append("Deployed version name: ");
                message.append(deploymentResult.getDeploymentVersionName()).append("\n");

                message.append("Environment deployed to: ");
                message.append(deploymentResult.getEnvironment().getName()).append("\n");

                message.append("Started at: ");
                message.append(deploymentResult.getStartedDate()).append("\n");

                if (deploymentResult.getFinishedDate() != null) {
                    message.append("Finished at: ");
                    message.append(deploymentResult.getFinishedDate()).append("\n");
                }

                if (deploymentResult.getDeploymentVersion() != null) {

                    DeploymentVersion currentDeploymentVersion = deploymentResult.getDeploymentVersion();
                    message.append("Version created by: ");
                    message.append(currentDeploymentVersion.getCreatorDisplayName()).append("\n");

                    /* Set<String> LinkedjiraIssues = currentDeploymentVersion.getJiraIssueKeysForDeploymentVersion();

                   if (!LinkedjiraIssues.isEmpty()) {
                        message.append(" Issues linked: ")
                                .append(String.join(", ", LinkedjiraIssues))
                                .append("\n");
                    }*/
                }
            }
        }

        try {
            TelegramBot bot = TelegramBotAdapter.build(botToken);
            SendMessage request = new SendMessage(chatId, message.toString())
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
