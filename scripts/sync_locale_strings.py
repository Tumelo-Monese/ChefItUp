#!/usr/bin/env python3
"""Append missing EN string keys into AF/ZU/ST."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
EN_PATH = ROOT / "values" / "strings.xml"

TRANSLATIONS: dict[str, dict[str, str]] = {
    "splash_brand": {"af": "CHEF IT UP", "zu": "CHEF IT UP", "st": "CHEF IT UP"},
    "splash_tagline": {"af": "Resepsoeker", "zu": "Isitholi seResiphi", "st": "Se batli sa Diresepe"},
    "cd_app_logo": {"af": "Chef it Up-logo", "zu": "Ilogo ye-Chef it Up", "st": "Logo ea Chef it Up"},
    "home_empty_feed": {
        "af": "Nog geen resepte nie. Trek om te verfris of soek.",
        "zu": "Azikho iziresiphi okwamanje. Donsela ukuvuselela noma useshe.",
        "st": "Ha ho diresepe hajoale. Hula ho nchafatsa kapa batla.",
    },
    "empty_generic_title": {
        "af": "Nog niks hier nie",
        "zu": "Akukho lutho lapha okwamanje",
        "st": "Ha ho letho mona hajoale",
    },
    "empty_generic_body": {
        "af": "Kom binnekort weer.",
        "zu": "Buyela maduze.",
        "st": "Khutla haufinyane.",
    },
    "error_state_title": {
        "af": "Iets het skeefgeloop",
        "zu": "Kukhona okungahambanga kahle",
        "st": "Ho na le se fositseng",
    },
    "action_retry": {"af": "Probeer weer", "zu": "Zama futhi", "st": "Leka hape"},
    "cooking_timer_done_title": {
        "af": "Afteller klaar",
        "zu": "Isikhathi siphelile",
        "st": "Nako e felile",
    },
    "cooking_timer_done_body": {
        "af": "Jou kookafteller is klaar.",
        "zu": "Isikhathi sakho sokupheka siphelile.",
        "st": "Nako ea hau ea ho pheha e felile.",
    },
    "meal_reminder_title": {
        "af": "Tyd om te kook",
        "zu": "Isikhathi sokupheka",
        "st": "Nako ea ho pheha",
    },
    "meal_reminder_body": {
        "af": "Kyk jou maaltydplan vir vandag.",
        "zu": "Bheka uhlelo lwakho lokudla lwanamuhla.",
        "st": "Sheba moralo oa hau oa lijo oa kajeno.",
    },
    "recipe_recommendation_title": {
        "af": "Nuwe resepidee",
        "zu": "Umbono omusha weresiphi",
        "st": "Khopolo e ncha ea resepe",
    },
    "recipe_recommendation_body": {
        "af": "Ontdek iets heerliks om volgende te kook.",
        "zu": "Thola okumnandi ongakupheka next.",
        "st": "Fumana ntho e monate eo u ka e phehang.",
    },
    "notification_channel_reminders": {
        "af": "Maaltydherinneringe",
        "zu": "Izikhumbuzo zokudla",
        "st": "Likhopotso tsa lijo",
    },
}


def parse_strings(path: Path) -> dict[str, tuple[str, str]]:
    text = path.read_text(encoding="utf-8")
    pattern = re.compile(
        r'<string\b([^>]*)\bname="([^"]+)"([^>]*)>(.*?)</string>',
        re.DOTALL,
    )
    out: dict[str, tuple[str, str]] = {}
    for m in pattern.finditer(text):
        pre, name, post, body = m.group(1), m.group(2), m.group(3), m.group(4)
        attrs = (pre + post).strip()
        out[name] = (attrs, body)
    return out


def main() -> None:
    en = parse_strings(EN_PATH)
    for loc in ("af", "zu", "st"):
        path = ROOT / f"values-{loc}" / "strings.xml"
        existing = parse_strings(path)
        missing = [k for k in en if k not in existing]
        if not missing:
            print(f"{loc}: complete ({len(existing)} keys)")
            continue
        text = path.read_text(encoding="utf-8")
        block_lines = ["", f"    <!-- Synced missing keys ({len(missing)}) -->"]
        for key in missing:
            attrs, body = en[key]
            content = TRANSLATIONS.get(key, {}).get(loc, body)
            if "translatable=" in attrs:
                # Keep non-translatable markers from EN
                extra = ""
                if 'translatable="false"' in attrs:
                    extra = ' translatable="false"'
                block_lines.append(f'    <string name="{key}"{extra}>{content}</string>')
            else:
                block_lines.append(f'    <string name="{key}">{content}</string>')
        insertion = "\n".join(block_lines) + "\n"
        path.write_text(text.replace("</resources>", insertion + "</resources>"), encoding="utf-8")
        print(f"{loc}: added {len(missing)} keys")


if __name__ == "__main__":
    main()
