package com.gsb.logistics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Auth auth = new Auth();
    private Rules rules = new Rules();

    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }

    public Rules getRules() { return rules; }
    public void setRules(Rules rules) { this.rules = rules; }

    public static class Auth {
        private String username = "admin";
        private String password = "admin123";
        private String token = "logistics-token-2026";

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class Rules {
        private int overdueDays = 7;
        private int abnormalStreak = 3;
        private double lowStockRatio = 0.2;
        private int repairReviewHours = 24;

        public int getOverdueDays() { return overdueDays; }
        public void setOverdueDays(int overdueDays) { this.overdueDays = overdueDays; }
        public int getAbnormalStreak() { return abnormalStreak; }
        public void setAbnormalStreak(int abnormalStreak) { this.abnormalStreak = abnormalStreak; }
        public double getLowStockRatio() { return lowStockRatio; }
        public void setLowStockRatio(double lowStockRatio) { this.lowStockRatio = lowStockRatio; }
        public int getRepairReviewHours() { return repairReviewHours; }
        public void setRepairReviewHours(int repairReviewHours) { this.repairReviewHours = repairReviewHours; }
    }
}
