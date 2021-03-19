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
Im Android Manifest wird ein Intent angemeldet. Der Intent wird in der onCreate-Methode unserer App
verarbeitet. Um Problemen vorzubeugen wird die geteilte Datei einer Typüberprüfung unterzogen. Es wird
geprüft, ob eine WhatsApp-Sprachnachrichten im .opus-Format geteilt wurde. Das .opus-Format ist eine
.ogg-Datei mit .opus-Codec. Wenn die Typüberprüfung erfolgreich war, wird die Datei an die
Weiterverarbeitung übergeben.

[!Image aus Whatsapp Teilen](https://github.com/Zadest/s2tApp/blob/main/imgs/share1.png?raw=true)
[!Image aus Whatsapp Teilen2](https://github.com/Zadest/s2tApp/blob/main/imgs/share2.png)

### 2.2 Konvertierung Opus zu MP3 und Splitting der Datei
Die an unsere App in Schritt [2.1](#21-whatsapp-sprachnachricht-exportieren) übersendete Datei hat
den Dateityp .ogg mit opus-Codec. Wit.Ai kann dieses Format nicht verarbeiten, weshalb eine Konvertierung
der Sprachnachricht von .opus nach .mp3 erfolgt. Hierfür wird die externe Bibliothek `FFMPEG` verwendet.
Die verwendete Implementierung befindet sich [hier](https://github.com/bravobit/FFmpeg-Android). Da
die Dateilänge für eine Verarbeitung mit Wit.Ai auf 20 Sekunden beschränkt ist, wird die übersendete
Datei nach der erfolgreichen Konvertierung in 20 Sekunden-Abschnitte unterteilt. Auch für diesen Schritt
kommt die `FFMPEG`-Bibliothek zum Einsatz.

### 2.3 Übertragung der Sprachnachricht an Wit.Ai
Zur Umwandlung der in [2.2](#22-konvertierung-opus-zu-mp3-und-splitting-der-datei) umgewandelten und
gesplitteten Dateien in Text kommt die Machine-Learning-basierte NLP-API [Wit.Ai](https://wit.ai/)
zum Einsatz. Die Verbindung erfolgt über HTTP-Requests mithilfe des REST-Clients [Retrofit]
(https://square.github.io/retrofit/). Damit sich die einzelnen Calls nicht überschneiden, und der
Text zur Sprachnachricht trotz der Aufteilung des Files in der richtigen Reihenfolge ankommt, wird
mit rekursivem Aufruf der entsprechenden Methode "callWit" gearbeitet.

### 2.4 Genutzte Funktionen von Wit.Ai
Konkret genutzt wird bei Wit.Ai der Endpunkt [/speech](https://wit.ai/docs/http/20200513#post__speech_link).
In dem den Entwicklern zugänglichen User-Interface von Wit.Ai lassen sich erhaltene Ergebnisse
redigieren, um so den internen Trainings-Prozess zu beeinflussen und die Umwandlung Schritt für Schritt zu optimieren.
Dazu gehört nicht nur die Transkription des Textes, sondern auch die Erkennung der Named Entities für
Daten und Namen ([wit\datetime](https://wit.ai/docs/built-in-entities/20200513/#wit_datetime) und
[wit\contact](https://wit.ai/docs/built-in-entities/20200513/#wit_contact)).

### 2.5 Rückgabe und Darstellung der Daten
Die Rückgabe der Daten durch Wit.Ai erfolgt im JSON-Format, siehe noch einmal der [\speech-Endpunkt]
(https://wit.ai/docs/http/20200513#post__speech_link).
Aus diesem Format lassen sich die benötigten Informationen über den Text und die erkannten Named Entities extrahieren.
Die Anzeige der von Wit.Ai erkannten Named Entities erfolgt über [SpannableStrings]
(https://developer.android.com/reference/android/text/SpannableString).
In der JSON-Response werden die Start- und Endindices der jeweiligen Entity
(hier: wit$contact:contact und wit$datetime:datetime) angegeben. Die Indices werden verwendet, um in
den SpannableStrings entsprechende Spans zu setzen und die Textfarbe der Spans zu ändern. Weiterhin
ist es in der App durch einen langen Klick auf den Text möglich, diesen vorm eventuellen Speichern
noch einmal zu bearbeiten.

### 2.6 Navigation
Die Navigation in der App geschieht über eine BottomNavigation und Fragments, die Teilbereiche der App
darstellen. Für jedes Fragment ist eine Java-Klasse, für die Funktionalität, und eine XML-Ressourcen-Datei,
für die Definition des Layouts, festgelegt. Die Oberfläche der App ist eine statische Oberfläche in die
beim Anklicken der Items in der Menüleiste flexibel die Layouts geladen werden. Diese werden in einer FragmentView
angezeigt, die auch das Ausführen der Funktionalitäten ermöglicht. Beim Wechseln zwischen den Ansichten, wird
der aktuelle Zustand gespeichert und bleibt beim Zurückwechseln erhalten. Beim ersten Öffnen oder Teilen
in die App wird die Ansicht mit dem passenden Namen "Start" aufgerufen. Die zweite Ansicht trägt den
Namen "Archiv", da hier die gespeicherten Texte angezeigt werden.

## 3. Extras

### 3.1 Audioplayer
Der Audioplayer bietet die Möglichkeit, die Sprachnachricht auch nach der Übersendung an Wit.Ai erneut
anzuhören. Auf diese Weise können mögliche Fehler im Transkript nachvollzogen und entsprechend verbessert werden.
Für die Implementierung des Audioplayers kommt der `MediaPlayer` von Android zum Einsatz
(s. [hier](https://developer.android.com/guide/topics/media/mediaplayer)).

### 3.2 Speichern von Texten
Ein weiteres Feature der App ist die Option zum  Speicherns von erzeugten Texten. Hier lässt sich in
einem Popup-Dialog zusätzlich angeben, von welcher Person die Sprachnachricht ursprünglich stammte.
Das Speichern wird hier mithilfe von [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences)
ausgeführt, was eine performante persistente Speicherung von Strings ermöglicht. Das Speichern der Named
Entities wird in der App mithilfe von Stringoperationen ermöglicht. In einer weiteren Ansicht der App
mit der Überschrift "Archiv" (s. [Navigation](#26-navigation)) lassen sich die gespeicherten Texte sichten,
nachlesen und bei Bedarf löschen. Eine Bearbeitung ist hier nicht mehr möglich. Sie werden in einer
ListView nach Datum der Sprachnachricht sortiert angezeigt.
