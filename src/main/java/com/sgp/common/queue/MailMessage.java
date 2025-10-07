package com.sgp.common.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailMessage {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> model;
}