import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { connexion } from "../api";

export default function Connexion({ onConnecte }) {
  const { t } = useTranslation();
  const [form, setForm] = useState({ username: "", password: "" });
  const [erreur, setErreur] = useState("");
  const navigate = useNavigate();

  const soumettre = async (e) => {
    e.preventDefault();
    setErreur("");
    try {
      const { data } = await connexion(form);
      localStorage.setItem("token", data.token);
      localStorage.setItem("username", data.username);
      onConnecte(data.username);
      navigate("/demandes");
    } catch (err) {
      setErreur(t("connexion.erreur"));
    }
  };

  return (
    <div className="carte">
      <h2>{t("connexion.titre")}</h2>
      <form onSubmit={soumettre}>
        <label>{t("connexion.nomUtilisateur")}</label>
        <input
          value={form.username}
          onChange={(e) => setForm({ ...form, username: e.target.value })}
          required
        />

        <label>{t("connexion.motDePasse")}</label>
        <input
          type="password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
        />

        {erreur && <p className="erreur">{erreur}</p>}
        <button type="submit">{t("connexion.bouton")}</button>
      </form>
    </div>
  );
}
