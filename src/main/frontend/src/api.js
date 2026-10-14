import axios from "axios";
import i18n from "./i18n";

// Spring Boot écoute par défaut sur le port 8080 (vs 8000 pour Django).
const API_BASE_URL = "http://localhost:8080/api";

const client = axios.create({ baseURL: API_BASE_URL });

// Ajoute le token JWT (préfixe "Bearer", attendu par JwtAuthFilter côté
// Spring Boot) et la langue courante pour les messages d'erreur traduits.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  config.headers["Accept-Language"] = i18n.language || "fr";
  return config;
});

export const inscription = (data) => client.post("/inscription", data);
export const connexion = (data) => client.post("/connexion", data);

export const creerDemande = (data) => client.post("/demandes", data);
export const listerMesDemandes = () => client.get("/demandes");
export const listerDemandesEnAttente = () => client.get("/demandes/en-attente");

export const listerMesMatches = () => client.get("/matches");
export const confirmerMatch = (id) => client.post(`/matches/${id}/confirmer`);

export const listerTransactions = () => client.get("/transactions");

export default client;
