package com.malirussia.transfer.dto;

import com.malirussia.transfer.model.Match;

import java.time.LocalDateTime;

public class MatchDtos {

    public static class MatchResponse {
        public Long id;
        public DemandeDtos.DemandeResponse demandeMaliVersRussie;
        public DemandeDtos.DemandeResponse demandeRussieVersMali;
        public LocalDateTime dateMatch;
        public boolean confirmeParMali;
        public boolean confirmeParRussie;
        public boolean estComplet;

        public static MatchResponse depuis(Match m) {
            MatchResponse r = new MatchResponse();
            r.id = m.getId();
            r.demandeMaliVersRussie = DemandeDtos.DemandeResponse.depuis(m.getDemandeMaliVersRussie());
            r.demandeRussieVersMali = DemandeDtos.DemandeResponse.depuis(m.getDemandeRussieVersMali());
            r.dateMatch = m.getDateMatch();
            r.confirmeParMali = m.isConfirmeParMali();
            r.confirmeParRussie = m.isConfirmeParRussie();
            r.estComplet = m.getEstComplet();
            return r;
        }
    }
}
