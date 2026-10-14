import React, { useState } from "react";
import { BrowserRouter, Routes, Route, Link, Navigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import Connexion from "./components/Connexion";
import Inscription from "./components/Inscription";
import CreerDemande from "./components/CreerDemande";
import ListeDemandes from "./components/ListeDemandes";
import SuiviMatches from "./components/SuiviMatches";
import SelecteurLangue from "./components/SelecteurLangue";

function App() {
  const { t } = useTranslation();
  const [utilisateur, setUtilisateur] = useState(
    localStorage.getItem("username") || null
  );

  const deconnexion = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    setUtilisateur(null);
  };

  return (
    <BrowserRouter>
      <div className="conteneur">
        <header className="entete">
          <h1>{t("app.titre")}</h1>
          <nav style={{ display: "flex", alignItems: "center" }}>
            {utilisateur ? (
              <>
                <Link to="/nouvelle-demande">{t("nav.nouvelleDemande")}</Link>
                <Link to="/demandes">{t("nav.mesDemandes")}</Link>
                <Link to="/matches">{t("nav.mesMatches")}</Link>
                <a href="#" onClick={deconnexion}>{t("nav.deconnexion")}</a>
              </>
            ) : (
              <>
                <Link to="/connexion">{t("nav.connexion")}</Link>
                <Link to="/inscription">{t("nav.inscription")}</Link>
              </>
            )}
            <SelecteurLangue />
          </nav>
        </header>

        <Routes>
          <Route
            path="/connexion"
            element={<Connexion onConnecte={setUtilisateur} />}
          />
          <Route path="/inscription" element={<Inscription />} />
          <Route
            path="/nouvelle-demande"
            element={utilisateur ? <CreerDemande /> : <Navigate to="/connexion" />}
          />
          <Route
            path="/demandes"
            element={utilisateur ? <ListeDemandes /> : <Navigate to="/connexion" />}
          />
          <Route
            path="/matches"
            element={utilisateur ? <SuiviMatches /> : <Navigate to="/connexion" />}
          />
          <Route
            path="/"
            element={<Navigate to={utilisateur ? "/demandes" : "/connexion"} />}
          />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
