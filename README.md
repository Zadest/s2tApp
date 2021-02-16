# S2TApp
[Vorgehensweise] (./#2-vorgehensweise)
Speech-to-text App for Android Phones

## 1. Grundidee
Immer häufiger verwenden wir bei der Nutzung von Instant Messaging Diensten wie bspw. Whatsapp die mitgelieferten Sprachnachrichtsfunktionen. Mit diesen können eingesprochene Nachrichten aufgenommen und versendet werden. Diese Sprachnachrichten bleiben im Chat-Verlauf bestehen und sind zu jedem späteren Zeitpunkt anzuhören.
Mit dieser Möglichkeit, eine Nachricht durch eigene Aussprache mehr Information zukommen zu lassen als einem geschrieben Text, ist allerdings auch das Abrufen der Nachricht eingeschränkt. Im Alltag gibt es immer Situationen, in denen wir die Zeit haben eine Nachricht zu lesen, aber nicht eine Sound-Datei anzuhören. Auch das erneute Abrufen von Informationen innerhalb einer Sprachnachricht ist nicht durch ein einfaches Überfliegen der Nachricht getan.

Das Ziel dieser App ist es daher, empfangene Sprachnachrichten in Text umzuwandeln, um eine bessere (Wieder-)verwendbarkeit und Abrufbarkeit der Informationen zu ermöglichen.

## 2. Vorgehensweise

### 2.1 Whatsapp-Sprachnachricht exportieren

### 2.2 Sprachnachricht an Wit.Ai übertragen
- HTTP-Kommunikation mithilfe von Retrofit
- Rekursives Schicken der einzelnen MP3-Dateien

### 2.3 Wit.Ai - Voodoo
- Nutzung des /speech-Endpunkts: https://wit.ai/docs/http/20200513#post__speech_link

### 2.4 Rückgabe und Darstellung der Daten
- Response im JSON-Format
- Benötigter Text befindet sich unter "text"
- Layout

## 3. Extras

### 3.1 Audioplayer

### 3.2 Speichern von Texten
- SharedPreferences
- Popup-Dialog

## 4. Probleme

### 4.1 Entschlüsseln des Opus-Codecs

### 4.2 Navigation

- Fragments als Teilbereiche der App
- Navigation als festes Bestandteil wechselt zwischen den Fragmenten hin und her

## 5. Lösungen

### 5.1 FFmpeg Library
