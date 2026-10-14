package com.malirussia.transfer.dto;
import com.malirussia.transfer.dto.MatchDtos;
import com.malirussia.transfer.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDtos {

    public static class TransactionResponse {
        public Long id;
        public MatchDtos.MatchResponse match;
        public BigDecimal tauxApplique;
        public LocalDateTime dateCreation;

        public static TransactionResponse depuis(Transaction t) {
            TransactionResponse r = new TransactionResponse();
            r.id = t.getId();
            r.match = MatchDtos.MatchResponse.depuis(t.getMatch());
            r.tauxApplique = t.getTauxApplique();
            r.dateCreation = t.getDateCreation();
            return r;
        }
    }
}