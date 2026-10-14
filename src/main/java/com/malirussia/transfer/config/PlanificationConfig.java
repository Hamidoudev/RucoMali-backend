package com.malirussia.transfer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Active les tâches planifiées (@Scheduled), utilisées pour rafraîchir les taux de change. */
@Configuration
@EnableScheduling
public class PlanificationConfig {
}
