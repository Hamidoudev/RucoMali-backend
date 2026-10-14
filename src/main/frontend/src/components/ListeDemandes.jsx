import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { listerMesDemandes } from "../api";

const CLE_STATUT = {
  EN_ATTENTE: { cle: "statuts.enAttente", classe: "badge-attente" },
  MATCHEE: { cle: "statuts.matchee", classe: "badge-matchee" },
  COMPLETE: { cle: "statuts.complete", classe: "badge-complete" },
  ANNULEE: { cle: "statuts.annulee", classe: "badge-attente" },
};

export default function ListeDemandes() {
  const { t, i18n } = useTranslation();
  const [demandes, setDemandes] = useState([]);
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    listerMesDemandes()
      .then(({ data }) => setDemandes(data))
      .finally(() => setChargement(false));
  }, []);

  if (chargement) return <p>Chargement…</p>;

  if (demandes.length === 0) {
    return <div className="carte"><p>{t("listeDemandes.vide")}</p></div>;
  }

  return (
    <div>
      <h2>{t("listeDemandes.titre")}</h2>
      {demandes.map((d) => {
        const statut = CLE_STATUT[d.statut] || CLE_STATUT.EN_ATTENTE;
        return (
          <div className="carte" key={d.id}>
            <p>
              <strong>
                {d.sens === "MALI_RU" ? t("creerDemande.maliVersRussie") : t("creerDemande.russieVersMali")}
              </strong>{" "}
              <span className={`badge ${statut.classe}`}>{t(statut.cle)}</span>
            </p>
            <p>
              {d.montant_devise_source} {d.devise_source}
              {d.montant_devise_cible_estime &&
                ` ≈ ${d.montant_devise_cible_estime} ${d.devise_cible}`}
            </p>
            <p style={{ fontSize: "0.8rem", color: "#666" }}>
              {t("listeDemandes.creeLe", {
                date: new Date(d.date_creation).toLocaleString(i18n.language),
              })}
            </p>
          </div>
        );
      })}
    </div>
  );
}
