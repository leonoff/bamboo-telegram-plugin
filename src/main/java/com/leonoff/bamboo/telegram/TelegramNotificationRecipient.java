package com.leonoff.bamboo.telegram;

import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.versions.DeploymentVersion;
import com.atlassian.bamboo.notification.NotificationRecipient;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.notification.recipients.AbstractNotificationRecipient;
import com.atlassian.bamboo.deployments.notification.DeploymentResultAwareNotificationRecipient;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plugin.descriptor.NotificationRecipientModuleDescriptor;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class TelegramNotificationRecipient extends AbstractNotificationRecipient implements DeploymentResultAwareNotificationRecipient,
        NotificationRecipient.RequiresPlan,
        NotificationRecipient.RequiresResultSummary {

    private static String BOT_TOKEN = "botToken";
    private static String CHAT_ID = "chatId";

    private String botToken = null;
    private Long chatId;

    private TemplateRenderer templateRenderer;

    private ImmutablePlan plan;
    private ResultsSummary resultsSummary;
    private DeploymentResult deploymentResult;


    @Override
    public void populate(@NotNull Map<String, String[]> params) {
        if (params.containsKey(BOT_TOKEN)) {
            int i = params.get(BOT_TOKEN).length - 1;
            this.botToken = params.get(BOT_TOKEN)[i];
        }
        if (params.containsKey(CHAT_ID)) {
            int i = params.get(CHAT_ID).length - 1;
            this.chatId = Long.valueOf(params.get(CHAT_ID)[i]);
        }
    }

    @Override
    public void init(@Nullable String configurationData) {

        if (StringUtils.isNotBlank(configurationData)) {
            String delimiter = "\\|";

            String[] configValues = configurationData.split(delimiter);

            if (configValues.length > 0) {
                botToken = configValues[0];
            }
            if (configValues.length > 1) {
                chatId = Long.valueOf(configValues[1]);
            }
        }
    }

    @NotNull
    @Override
    public String getRecipientConfig() {
        // We can do this because webhook URLs don't have | in them, but it's pretty dodge. Better to JSONify or something?
        String delimiter = "|";

        StringBuilder recipientConfig = new StringBuilder();
        if (StringUtils.isNotBlank(botToken)) {
            recipientConfig.append(botToken);
        }
        if (chatId != null) {
            recipientConfig.append(delimiter);
            recipientConfig.append(chatId);
        }
        return recipientConfig.toString();
    }

    @NotNull
    @Override
    public String getEditHtml() {
        String editTemplateLocation = ((NotificationRecipientModuleDescriptor) getModuleDescriptor()).getEditTemplate();
        return templateRenderer.render(editTemplateLocation, populateContext());
    }

    @NotNull
    @Override
    public String getViewHtml() {
        String editTemplateLocation = ((NotificationRecipientModuleDescriptor) getModuleDescriptor()).getViewTemplate();
        return templateRenderer.render(editTemplateLocation, populateContext());
    }

    private Map<String, Object> populateContext() {
        Map<String, Object> context = Maps.newHashMap();

        if (botToken != null) {
            context.put(BOT_TOKEN, botToken);
        }
        if (chatId != null) {
            context.put(CHAT_ID, chatId);
        }

        return context;
    }

    @NotNull
    public List<NotificationTransport> getTransports() {
        List<NotificationTransport> list = Lists.newArrayList();
        list.add(new TelegramNotificationTransport(botToken, chatId, plan, resultsSummary, deploymentResult));
        return list;
    }

    public void setPlan(@Nullable final Plan plan) {
        this.plan = plan;
    }

    public void setPlan(@Nullable final ImmutablePlan plan) {
        this.plan = plan;
    }

    public void setDeploymentResult(@Nullable final DeploymentResult deploymentResult) {
        this.deploymentResult = deploymentResult;
    }

    public void setResultsSummary(@Nullable final ResultsSummary resultsSummary) {
        this.resultsSummary = resultsSummary;
    }

    //-----------------------------------Dependencies
    public void setTemplateRenderer(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }
}
