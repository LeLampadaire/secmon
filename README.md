## SecMon
#Projet développé en Java pour le cours de réseau de deuxième année à l'HELMO Campus Guillemins.

Le but du projet est d'avoir deux probes (https et snmp) qui envoyait en multicast leur présence afin que le Worker(un thread créé par le DaemonMonitor) lui envoie les serveurs à écouter. Le DaemonMonitor créé une mémoire partagée, une ConcurrentLinkedQueue qui va être partagé au Worker et au ClientThread (en plus de la liste des comptes). Le client qui se connecte au DaemonMonitor, peut encoder des commandes qui vont être analysées par le ClientThread afin d'y répondre et d'avoir un résultat. 

#Les participants
- Nathan Lemoine
- Florent Lequien
- Rausin Julien

#Améliorations
- Créer une classe qui stocke les regex et vérifie les commandes envoyées par toutes les classes qui utilisent les regex.
- Créer une classe mère pour les deux probes.
