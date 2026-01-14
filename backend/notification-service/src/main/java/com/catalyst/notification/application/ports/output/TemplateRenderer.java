package com.catalyst.notification.application.ports.output;

import com.catalyst.notification.domain.valueobject.NotificationType;

import java.util.Map;

/**
 * Port for rendering email templates.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface TemplateRenderer {
    
    /**
     * Renders an email template to HTML.
     * 
     * @param type the notification type
     * @param templateData the template variables
     * @return the rendered HTML content
     * @throws com.catalyst.notification.domain.exception.TemplateNotFoundException if template not found
     */
    String render(NotificationType type, Map<String, Object> templateData);
}

