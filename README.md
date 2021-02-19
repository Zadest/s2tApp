# S2TApp

Speech-to-text App für Android Phones

## Inhalt
- [1. Grundidee](#1-grundidee)
- [2. Vorgehensweise](#2-vorgehensweise)
- [3. Extras](#3-extras)


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
Hierfür wird die externe Bibliothek `FFMPEG` verwendet. Die verwendete Implementierung befindet sich [hier](https://github.com/bravobit/FFmpeg-Android).
Da die Dateilänge für eine Verarbeitung mit Wit.Ai auf 20 Sekunden beschränkt ist, wird die übersendete Datei nach der erfolgreichen Konvertierung in 20 Sekunden-Chunks unterteilt.
Auch für diesen Schritt kommt die `FFMPEG`-Implementierung zum Einsatz.

### 2.3 Übertragung der Sprachnachricht an Wit.Ai
Zur Umwandlung der in 2.2 umgewandelten und gesplitteten Dateien in Text kommt die Machine-Learning-basierte NLP-API [Wit.Ai](https://wit.ai/) zum Einsatz.
Die Verbindung erfolgt über HTTP-Requests mithilfe des REST-Clients [Retrofit](https://square.github.io/retrofit/).
Damit sich die einzelnen Calls nicht überschneiden, und der Text zur Sprachnachricht trotz der Aufteilung des Files in der richtigen Reihenfolge ankommt, wird mit rekursivem Aufruf der entsprechenden Methode "call" gearbeitet.

### 2.4 Genutzte Funktionen von Wit.Ai
Konkret genutzt wird bei Wit.Ai der Endpunkt [/speech](https://wit.ai/docs/http/20200513#post__speech_link).
In dem den Entwicklern zugänglichen User-Interface von Wit.Ai lassen sich erhaltene Ergebnisse redigieren, um so den internen Trainings-Prozess zu beeinflussen und die Umwandlung Schritt für Schritt zu optimieren.
Dazu gehört nicht nur die Transkription des Textes, sondern auch die Erkennung der Named Entities für Daten und Namen ([wit\datetime](https://wit.ai/docs/built-in-entities/20200513/#wit_datetime) und [wit\contact](https://wit.ai/docs/built-in-entities/20200513/#wit_contact)).

### 2.5 Rückgabe und Darstellung der Daten
- Response im JSON-Format
- Benötigter Text befindet sich unter "text"
- Layout

### 2.6 Navigation
- Fragments als Teilbereiche der App
- Navigation als festes Bestandteil wechselt zwischen den Fragmenten hin und her

## 3. Extras

### 3.1 Audioplayer
Der Audioplayer bietet die Möglichkeit, die Sprachnachricht auch nach der Übersendung an Wit.Ai erneut anzuhören.
Auf diese Weise können mögliche Fehler im Transkript nachvollzogen und entsprechend verbessert werden.
Für die Implementierung des Audioplayers kommt der `MediaPlayer` von Android zum Einsatz (s. [hier](https://developer.android.com/guide/topics/media/mediaplayer)).

### 3.2 Speichern von Texten
- SharedPreferences
- Popup-Dialog

