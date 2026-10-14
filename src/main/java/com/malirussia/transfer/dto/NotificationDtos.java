package com.malirussia.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationDtos {

    public static class NotificationResponse {
        public Long id;
        public String type;
        public String reference;
        public String nom;
        public BigDecimal montant;
        public String devise;
        public int decimales;
        public BigDecimal montant2;
        public String devise2;
        public int decimales2;
        public boolean lue;
        public LocalDateTime date;
    }

    public static class NotificationsResponse {
        public long nonLues;
        public List<NotificationResponse> notifications;
    }
}
