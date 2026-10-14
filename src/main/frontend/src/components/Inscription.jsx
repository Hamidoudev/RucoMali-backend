import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { inscription } from "../api";

export default function Inscription() {
  const { t } = useTranslation();
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    pays: "MALI",
    telephone: "",
  });
  const [erreur, setErreur] = useState("");
  const navigate = useNavigate();

  const majChamp = (champ) => (e) =>
    setForm({ ...form, [champ]: e.target.value });

  const soumettre = async (e) => {
    e.preventDefault();
    setErreur("");
    try {
      const { data } = await inscription(form);
      localStorage.setItem("token", data.token);
      localStorage.setItem("username", data.username);
      navigate("/demandes");
    } catch (err) {
      setErreur(t("inscription.erreur"));
    }
  };

  return (
    <div className="carte">
      <h2>{t("inscription.titre")}</h2>
      <form onSubmit={soumettre}>
        <label>{t("inscription.nomUtilisateur")}</label>
        <input value={form.username} onChange={majChamp("username")} required />

        <label>{t("inscription.email")}</label>
        <input type="email" value={form.email} onChange={majChamp("email")} />

        <label>{t("inscription.motDePasse")}</label>
        <input
          type="password"
          value={form.password}
          onChange={majChamp("password")}
          required
          minLength={6}
        />

        <label>{t("inscription.pays")}</label>
        <select value={form.pays} onChange={majChamp("pays")}>
          <option value="MALI">{t("inscription.mali")}</option>
          <option value="RUSSIE">{t("inscription.russie")}</option>
        </select>

        <label>{t("inscription.telephone")}</label>
        <input value={form.telephone} onChange={majChamp("telephone")} />

        {erreur && <p className="erreur">{erreur}</p>}
        <button type="submit">{t("inscription.bouton")}</button>
      </form>
    </div>
  );
}
