# RésoSoin — fiche de recette avec un cabinet pilote

Cette fiche sert à tester le besoin d'interopérabilité **sans recueillir de donnée patient**.

## Règle de séance

Utiliser uniquement :
- noms de logiciels et fonctions ;
- parcours génériques ;
- exemples inventés ;
- captures expurgées si le cabinet souhaite montrer une interface.

Ne jamais copier dans cette fiche un nom de patient, un rendez-vous réel, un motif médical, un document clinique, un identifiant patient ou des identifiants de connexion.

## 1. Environnement du cabinet

- Profession(s) :
- Nombre de professionnel·les :
- Secrétariat : interne / externe / aucun
- Logiciel métier principal :
- Agenda principal :
- Plateforme de prise de rendez-vous :
- MSSanté : oui / non / ne sait pas
- Autres outils indispensables :

## 2. Parcours à observer

Pour chaque parcours, noter **où une même information est ressaisie**.

### Nouveau rendez-vous
1. Où la demande arrive-t-elle ?
2. Où le créneau est-il vérifié ?
3. Où le rendez-vous est-il créé ?
4. Une deuxième saisie est-elle nécessaire ?
5. Qu'est-ce qui est recopié ?

### Modification / annulation
1. Quel outil reçoit la modification ?
2. Quels autres outils doivent être corrigés ?
3. Que se passe-t-il si une mise à jour échoue ?

### Arrivée au cabinet
1. Quelles informations sont nécessaires avant la consultation ?
2. Sont-elles déjà présentes dans le logiciel métier ?
3. Une donnée issue de l'agenda est-elle ressaisie ?

### Après le rendez-vous
1. RésoSoin aurait-il encore une action utile ?
2. Cette action est-elle administrative ou clinique ?
3. Peut-elle rester dans le logiciel métier ?

## 3. Double saisie

| Information | Source | Destination | Fréquence | Temps perdu estimé | Risque si désynchronisé |
|---|---|---|---|---|---|
| Exemple fictif : horaire | agenda A | logiciel B | quotidien | à mesurer | rendez-vous incohérent |

Ne pas noter de contenu patient réel.

## 4. Capacités déjà disponibles

Pour chaque outil :
- export CSV ?
- export PDF ?
- iCalendar / CalDAV ?
- import documenté ?
- API officielle ?
- connecteur partenaire ?
- documentation fournie au cabinet ?
- interlocuteur éditeur/intégrateur ?

Conserver les documents contractuels ou techniques hors de ce dépôt s'ils ne sont pas publics.

## 5. Test des prototypes RésoSoin

Tester les prototypes cabinet et interopérabilité du dossier `resosoin/prototypes/`.

Questions :
- La frontière RésoSoin / logiciel métier est-elle comprise ?
- Quelle action paraît inutile ?
- Quelle action manque ?
- L'aperçu avant import est-il suffisant ?
- Que faudrait-il absolument voir avant de confirmer une écriture ?
- Une simple exportation résoudrait-elle le problème, sans connecteur temps réel ?
- Quel échec serait le plus gênant : retard, doublon, absence de mise à jour, mauvaise donnée ?

## 6. Priorisation du premier pont

Un connecteur ne devient prioritaire que si :
1. le problème est fréquent ;
2. il entraîne une vraie double saisie ou un risque concret ;
3. un export/import simple ne suffit pas ;
4. l'éditeur fournit une interface autorisée ;
5. le périmètre de données est minimal et clair ;
6. le comportement en panne est acceptable.

Ne pas choisir un connecteur uniquement parce qu'un éditeur est très répandu.

## 7. Sortie attendue

Après la séance, produire une synthèse **sans donnée patient** :
- outil(s) concernés ;
- problème précis ;
- flux minimal souhaité ;
- fréquence ;
- solution la plus simple : aucun pont / export / import / synchronisation ;
- documentation officielle disponible ;
- qualification administrative ou potentiellement santé à confirmer ;
- prochain interlocuteur nécessaire.

Cette synthèse sert ensuite à sélectionner le cas d'usage CI-SIS/FHIR/CDA ou propriétaire à étudier.