#!/usr/bin/env python3
"""Dupe-Alarm: meldet neue 6b6t-Blogbeiträge über Dupes in einem Discord-Kanal.

Liest den RSS-Feed des 6b6t-Blogs, sucht in Titel und Text nach Dupe-Wörtern und schickt
für jeden neuen Treffer eine Nachricht an den Discord-Webhook aus DISCORD_WEBHOOK_URL.
Welche Beiträge schon bekannt sind, steht in der Statusdatei (Standard: dupe-alarm-state.json).
Beim allerersten Lauf werden alle vorhandenen Beiträge nur gemerkt, nicht gemeldet.

Benutzung:
  DISCORD_WEBHOOK_URL=... python3 tools/dupe_alarm.py
  python3 tools/dupe_alarm.py --dry-run     # nichts senden, nichts speichern, nur anzeigen
  python3 tools/dupe_alarm.py --test        # nur eine Testnachricht an Discord schicken
"""

import argparse
import json
import os
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET

FEED_URL = "https://blog.6b6t.org/rss/"
USER_AGENT = "cheecky-dupe-alarm (+https://github.com/genben-sb3/cheecky-client)"
# dupe, dupes, duping, duped, duper, dupers, duplication, duplicate ...
DUPE_PATTERN = re.compile(r"\bdup(e|es|ed|er|ers|ing|licat\w*)\b", re.IGNORECASE)
CONTENT_TAG = "{http://purl.org/rss/1.0/modules/content/}encoded"


def fetch(url):
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.read()


def parse_feed(xml_bytes):
    """Liste von Beiträgen (id, Titel, Link, Datum, Text) in Feed-Reihenfolge (neueste zuerst)."""
    posts = []
    for item in ET.fromstring(xml_bytes).iter("item"):
        def text(tag):
            return (item.findtext(tag) or "").strip()

        link = text("link")
        posts.append({
            "id": text("guid") or link,
            "title": text("title"),
            "link": link,
            "date": text("pubDate"),
            "body": text("description") + " " + text(CONTENT_TAG),
        })
    return posts


def is_dupe_post(post):
    return bool(DUPE_PATTERN.search(post["title"]) or DUPE_PATTERN.search(post["body"]))


def send_to_discord(webhook_url, post):
    payload = {
        "content": f"**Dupe-Alarm:** Neuer 6b6t-Blogbeitrag über Dupes\n**{post['title']}**\n{post['link']}",
        # Keine Pings, auch falls der Titel @everyone o. ä. enthält
        "allowed_mentions": {"parse": []},
    }
    request = urllib.request.Request(
        webhook_url,
        data=json.dumps(payload).encode(),
        headers={"Content-Type": "application/json", "User-Agent": USER_AGENT},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=30):
        pass


def load_state(path):
    try:
        with open(path) as f:
            return set(json.load(f)["seen"])
    except FileNotFoundError:
        return None


def save_state(path, seen):
    with open(path, "w") as f:
        json.dump({"seen": sorted(seen)}, f, indent=1)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--state", default="dupe-alarm-state.json", help="Statusdatei mit bekannten Beiträgen")
    parser.add_argument("--dry-run", action="store_true", help="nichts senden, nichts speichern")
    parser.add_argument("--test", action="store_true", help="nur eine Testnachricht an Discord schicken")
    args = parser.parse_args()

    if args.test:
        webhook_url = os.environ.get("DISCORD_WEBHOOK_URL")
        if not webhook_url:
            print("DISCORD_WEBHOOK_URL fehlt.", file=sys.stderr)
            return 1
        send_to_discord(webhook_url, {"title": "Test: Der Dupe-Alarm funktioniert.", "link": "https://blog.6b6t.org/"})
        print("Testnachricht geschickt.")
        return 0

    try:
        posts = parse_feed(fetch(FEED_URL))
    except (OSError, ET.ParseError) as e:
        # Blog kurz nicht erreichbar: nächster Lauf versucht es wieder. Kein Fehler, sonst
        # verschickt GitHub alle 15 Minuten eine Fehler-Mail.
        print(f"Feed nicht lesbar, überspringe diesen Lauf: {e}")
        return 0
    seen = load_state(args.state)
    first_run = seen is None
    if first_run:
        seen = set()

    new_dupe_posts = [p for p in posts if p["id"] not in seen and is_dupe_post(p)]
    if args.dry_run:
        print(f"{len(posts)} Beiträge im Feed, Dupe-Treffer (unabhängig vom Status):")
        for post in posts:
            if is_dupe_post(post):
                print(f"  {post['date']}  {post['title']}  {post['link']}")
        return 0

    if first_run:
        print(f"Erster Lauf: {len(posts)} vorhandene Beiträge gemerkt, nichts gemeldet.")
    else:
        webhook_url = os.environ.get("DISCORD_WEBHOOK_URL")
        if new_dupe_posts and not webhook_url:
            print("DISCORD_WEBHOOK_URL fehlt, kann nicht melden.", file=sys.stderr)
            return 1
        # Älteste zuerst melden, damit die Reihenfolge im Kanal stimmt. Nach jeder Meldung
        # speichern, damit ein späterer Fehler nichts doppelt meldet.
        for post in reversed(new_dupe_posts):
            send_to_discord(webhook_url, post)
            seen.add(post["id"])
            save_state(args.state, seen)
            print(f"Gemeldet: {post['title']}")
        if not new_dupe_posts:
            print("Keine neuen Dupe-Beiträge.")

    seen.update(p["id"] for p in posts)
    save_state(args.state, seen)
    return 0


if __name__ == "__main__":
    sys.exit(main())
