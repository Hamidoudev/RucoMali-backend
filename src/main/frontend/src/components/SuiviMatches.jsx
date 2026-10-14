import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { listerMesMatches, confirmerMatch } from "../api";

export default function SuiviMatches() {
  const { t } = useTranslation();
  const [matches, setMatches] = useState([]);
  const [chargement, setChargement] = useState(true);

  const rafraichir = () => {
    listerMesMatches()
      .then(({ data }) => setMatches(data))
      .finally(() => setChargement(false));
  };

  useEffect(rafraichir, []);

  const confirmer = async (id) => {
    await confirmerMatch(id);
    rafraichir();
  };

  if (chargement) return <p>Chargement…</p>;

  if (matches.length === 0) {
    return <div className="carte"><p>{t("suiviMatches.vide")}</p></div>;
  }

  return (
    <div>
      <h2>{t("suiviMatches.titre")}</h2>
      {matches.map((m) => (
        <div className="carte" key={m.id}>
          <p>
            {m.demande_mali_vers_russie.auteur_username} ({t("inscription.mali")}) ⇄{" "}
            {m.demande_russie_vers_mali.auteur_username} ({t("inscription.russie")})
          </p>
          <p>
            {m.est_complet ? (
              <span className="badge badge-complete">{t("suiviMatches.termine")}</span>
            ) : (
              <span className="badge badge-matchee">{t("suiviMatches.enCours")}</span>
            )}
          </p>
          {!m.est_complet && (
            <button onClick={() => confirmer(m.id)}>
              {t("suiviMatches.confirmer")}
            </button>
          )}
        </div>
      ))}
    </div>
  );
}
