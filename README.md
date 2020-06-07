![CI/CD](https://github.com/sogis/ilivalidator-custom-functions/workflows/CI/CD/badge.svg)

# ilivalidator-custom-functions
Custom functions for ilivalidator

## Beschreibung
Implementierungen von INTERLIS-Funktionen, die _ilivalidator_ funktional erweitern. 

## Benutzerdokumentation
Siehe auch: [claeis/ilivalidator](https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst)

Will man die zusätzlichen INTERLIS-Funktionen verwenden können, müssen diese _ilivalidator_ bekannt gemacht werden. Dazu braucht es:

1. Die Java-Klassen, welche die INTERLIS-Funktion implementieren.
2. Ein Modell, wo die INTERLIS-Funktionen deklariert werden.
3. Ein Modell, wo die INTERLIS-Funktionen in Constraints angwendet werden.
4. Eine Konfigurationsdatei, welche die Modelle aus 2 und 3 und dem Originalmodell "verkabelt".

Punkte 2. und 3. (und daraus folgend 4.) sind optional, falls alles im Originalmodell abgehandelt werden kann. In unserem Fall die die INTERLIS-Funktionsdeklarationen jedoch separiert im Modell `SO_FunctionsExt`.

Nachfolgender Aufruf prüft, ob die Links auf Dokumente in der Transferdatei tatsächlich auf eine HTTP-Ressource zeigen:

```
java -jar ilivalidator.jar --plugins plugins/ --config validateData.toml ch.so.sk.gesetze.xtf
```

Im Ordner `plugins` ist die Jar-Datei mit den Java-Klassen. Die Datei [`validateData.toml`](./src/test/data/validateData.toml) verkabelt die notwendigen Modelle miteinander.

### GRETL-Jobs
Für GRETL muss zum jetzigen Zeitpunkt ein anderer Approach (aka Workaround) gewählt werden. Es scheint, als funktioniert das Laden der Klassen in _iox-ili_ nicht (`NoClassDefFoundError`...) wenn es via Gradle gemacht wird. Aus diesem Grund werden bereits zur compile time die Zusatzfunktionen in _ilivalidator_ registriert und auf das Laden der Klassen wird gänzlich verzichtet, d.h. sie sind bereits im Klassenpfad (und werden als normale Abhängigkeiten im GRETL-Projekt definiert.). Das heisst auch, dass die `pluginFolder`-Option obsolet ist und nur die `configFile`-Option notwendig ist. Der Nachteil dieser Lösung ist, dass die Zusatzfunktionsnamen hardcodiert in der ilivalidator-Task-Implementierung sind (könnte wahrscheinlich noch geändert werden, wenn man die Jar-Datei mit den Zusatzfunktionen analog _iox-ili_ durchsucht). Da aber bei einer Änderung der Zusatzfunktionen sowieso die Version der Abhängigkeit in GRETL angepasst werden muss, ist das momentan noch vertretbar.

Beispiel:

task validateFile(type: IliValidator) {
    description = "Validiert die Transferdatei mit zusätzlichen Checks."
    dataFiles = ["ch.so.sk.gesetze.xtf"]
    logFile = "/tmp/gretl-share/ilivalidator.log"
    allObjectsAccessible = false
    configFile = "validateData.toml"
}

Die Jar-Datei mit den Java-Klassen ist ins GRETL-Runtime-Image gebrannt und muss dementsprechend neu gebuildet werden, falls sich die etwas ändert. Der Pluginsordner ist nicht änderbar: `/home/gradle/plugins/`. Siehe [Dockerfile](https://github.com/sogis/gretl/blob/master/runtimeImage/gretl/Dockerfile).

## Developing
Jeder Commit stösst die Github-Action Pipeline an. Ist der Build und das Testen erfolgreich, wird die Jar-Datei auf [Bintray](https://bintray.com/beta/#/sogis/ilivalidator-custom-functions?tab=packages) deployed.

Das "Funktionskopf-Modell" `SO_FunctionsExt` muss - falls nötig - [separat deployed](https://github.com/sogis/sogis-interlis-repository) werden. Achtung: Funktionen müssen abwärtskompatibel sein, damit bereits deployte Funktionen immer noch funktionieren.

## TODO
- Caching für/bei SET CONSTRAINTS verstehen und umsetzen für Cycle-Detector.

