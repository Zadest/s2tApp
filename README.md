# S2TApp

Speech-to-text App für Android Phones

## Inhalt
- [1. Grundidee](#1-grundidee)
- [2. Vorgehensweise](#2-vorgehensweise)
- [3. Extras](#3-extras)
- [4. Probleme](#4-probleme)
- [5. Lösungen](#5-lösungen)


## 1. Grundidee
Immer häufiger verwenden wir bei der Nutzung von Instant Messaging Diensten wie bspw. Whatsapp die mitgelieferten Sprachnachrichtsfunktionen. Mit diesen können eingesprochene Nachrichten aufgenommen und versendet werden. Diese Sprachnachrichten bleiben im Chat-Verlauf bestehen und sind zu jedem späteren Zeitpunkt anzuhören.
Mit dieser Möglichkeit, eine Nachricht durch eigene Aussprache mehr Information zukommen zu lassen als einem geschrieben Text, ist allerdings auch das Abrufen der Nachricht eingeschränkt. Im Alltag gibt es immer Situationen, in denen wir die Zeit haben eine Nachricht zu lesen, aber nicht eine Sound-Datei anzuhören. Auch das erneute Abrufen von Informationen innerhalb einer Sprachnachricht ist nicht durch ein einfaches Überfliegen der Nachricht getan.

Das Ziel dieser App ist es daher, empfangene Sprachnachrichten in Text umzuwandeln, um eine bessere (Wieder-)verwendbarkeit und Abrufbarkeit der Informationen zu ermöglichen.

## 2. Vorgehensweise

### 2.1 Whatsapp-Sprachnachricht exportieren
- Intent im Android Manifest "anmelden"
- Intent in der onCreate Methode unserer Anwendung verarbeiten
- Typüberprüfung
- Zur Weiterverarbeitung übergeben

### 2.2 Konvertierung Opus zu MP3 und Splitting der Datei
Die an unsere App in Schritt 2.1 übersendete Datei hat den Dateityp .ogg mit opus-Codec.
Wit.Ai kann dieses Format nicht verarbeiten, weshalb eine Konvertierung der Sprachnachricht von .opus nach .mp3 erfolgt.
Hierfür wird die externe Bibliothek `FFMPEG` verwendet. Die verwendete Implentierung sehen Sie [hier](https://github.com/bravobit/FFmpeg-Android).
Da die Dateilänge für eine Verarbeitung mit Wit.Ai auf 20 Sekunden beschränkt ist, wird die übersendete Datei nach der erfolgreichen Konvertierung in 20 Sekunden-Chunks unterteilt.
Auch für diesen Schritt kommt die `FFMPEG`-Implentierung zum Einsatz.

### 2.3 Sprachnachricht an Wit.Ai übertragen
- HTTP-Kommunikation mithilfe von Retrofit
- Rekursives Schicken der einzelnen MP3-Dateien

### 2.4 Wit.Ai - Voodoo
- Nutzung des /speech-Endpunkts: https://wit.ai/docs/http/20200513#post__speech_link

### 2.5 Rückgabe und Darstellung der Daten
- Response im JSON-Format
- Benötigter Text befindet sich unter "text"
- Layout

### 2.6 Navigation
- Fragments als Teilbereiche der App
- Navigation als festes Bestandteil wechselt zwischen den Fragmenten hin und her

## 3. Extras

### 3.1 Audioplayer

### 3.2 Speichern von Texten
- SharedPreferences
- Popup-Dialog

