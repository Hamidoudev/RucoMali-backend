import React from "react";
import { useTranslation } from "react-i18next";

const LANGUES = [
  { code: "fr", label: "FR" },
  { code: "ru", label: "RU" },
  { code: "en", label: "EN" },
];

export default function SelecteurLangue() {
  const { i18n } = useTranslation();

  return (
    <select
      value={i18n.resolvedLanguage || i18n.language}
      onChange={(e) => i18n.changeLanguage(e.target.value)}
      style={{ width: "auto", padding: "6px 8px", marginLeft: "16px" }}
      aria-label="Langue / Language / Язык"
    >
      {LANGUES.map((l) => (
        <option key={l.code} value={l.code}>
          {l.label}
        </option>
      ))}
    </select>
  );
}
