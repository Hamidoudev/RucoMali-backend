import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { creerDemande } from "../api";

export default function CreerDemande() {
  const { t } = useTranslation();
  const [form, setForm] = useState({
    sens: "MALI_RU",
    montant_devise_source: "",
    devise_source: "XOF",
    devise_cible: "RUB",
  });
  const [erreur, setErreur] = useState("");
  const [succes, setSucces] = useState(false);
  const navigate = useNavigate();

  const majChamp = (champ) => (e) =>
    setForm({ ...form, [champ]: e.target.value });

  const soumettre = async (e) => {
    e.preventDefault();
    setErreur("");
    try {
      await creerDemande(form);
      setSucces(true);
      setTimeout(() => navigate("/demandes"), 1200);
    } catch (err) {
      setErreur(t("creerDemande.erreur"));
    }
  };

  return (
    <div className="carte">
      <h2>{t("creerDemande.titre")}</h2>
      <p>{t("creerDemande.description")}</p>
      <form onSubmit={soumettre}>
        <label>{t("creerDemande.sens")}</label>
        <select value={form.sens} onChange={majChamp("sens")}>
          <option value="MALI_RU">{t("creerDemande.maliVersRussie")}</option>
          <option value="RU_MALI">{t("creerDemande.russieVersMali")}</option>
        </select>

        <label>{t("creerDemande.montant")}</label>
        <input
          type="number"
          step="0.01"
          value={form.montant_devise_source}
          onChange={majChamp("montant_devise_source")}
          required
        />

        <label>{t("creerDemande.deviseSource")}</label>
        <input value={form.devise_source} onChange={majChamp("devise_source")} required />

        <label>{t("creerDemande.deviseCible")}</label>
        <input value={form.devise_cible} onChange={majChamp("devise_cible")} required />

        {erreur && <p className="erreur">{erreur}</p>}
        {succes && <p style={{ color: "#1a7a3c" }}>{t("creerDemande.succes")}</p>}
        <button type="submit">{t("creerDemande.bouton")}</button>
      </form>
    </div>
  );
}
