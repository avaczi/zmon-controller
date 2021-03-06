package org.zalando.zmon.domain;

import java.util.List;

public class Alert {

    private AlertDefinitionAuth alertDefinition;
    private List<LastCheckResult> entities;
    private String message;
    private boolean notificationsAck;
    private Long entitiesCount;

    public AlertDefinition getAlertDefinition() {
        return alertDefinition;
    }

    public void setAlertDefinition(final AlertDefinitionAuth alertDefinition) {
        this.alertDefinition = alertDefinition;
    }

    public List<LastCheckResult> getEntities() {
        return entities;
    }

    public void setEntities(final List<LastCheckResult> entities) {
        this.entities = entities;
    }

    public void setEntitiesCount(final Long number){
        this.entitiesCount = number;
    }

    public Long getEntitiesCount(){
        return this.entitiesCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public boolean isNotificationsAck() {
        return notificationsAck;
    }

    public void setNotificationsAck(boolean notificationsAck) {
        this.notificationsAck = notificationsAck;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AlertDetails [alertDefinition=");
        builder.append(alertDefinition);
        builder.append(", entities=");
        builder.append(entities);
        builder.append(", entitiesCount=");
        builder.append(entitiesCount);
        builder.append(", message=");
        builder.append(message);
        builder.append("]");
        return builder.toString();
    }
}
