package ma.emsi.aitelmahjoub.tp0_aitelmahjoub_salaheddine.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation qui dure pendant plusieurs requêtes HTTP.
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Rôle "système" que l'on attribuera plus tard à un LLM.
     * Valeur par défaut que l'utilisateur peut modifier.
     * Possible d'écrire un nouveau rôle dans la liste déroulante.
     */
    private String roleSysteme;

    /**
     * Quand le rôle est choisi par l'utilisateur dans la liste déroulante,
     * il n'est plus possible de le modifier (voir code de la page JSF), sauf si on veut un nouveau chat.
     */
    private boolean roleSystemeChangeable = true;

    /**
     * Liste de tous les rôles de l'API prédéfinis.
     */
    private List<SelectItem> listeRolesSysteme;

    /**
     * Dernière question posée par l'utilisateur.
     */
    private String question;

    /**
     * Dernière réponse de l'API OpenAI.
     */
    private String reponse;

    /**
     * La conversation depuis le début.
     */
    private StringBuilder conversation = new StringBuilder();

    /**
     * Résultats de l'analyse de texte demandée :
     */
    private int nbCaracteres = 0;             // avec espaces
    private int nbCaracteresSansEspaces = 0;  // sans espaces
    private int nbMots = 0;
    private int nbVoyelles = 0;
    private String chaineRenversee = "";
    private String reformulationCourte = "";

    /**
     * Contexte JSF. Utilisé pour qu'un message d'erreur s'affiche dans le formulaire.
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Constructeur public obligatoire pour CDI si vous ajoutez d'autres constructeurs.
     */
    public Bb() {
    }

    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    /**
     * setter indispensable pour le textarea.
     *
     * @param reponse la réponse à la question.
     */
    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    /**
     * getters pour les résultats d'analyse
     *
     */
    public int getNbCaracteres() {
        return nbCaracteres;
    }

    public int getNbCaracteresSansEspaces() {
        return nbCaracteresSansEspaces;
    }

    public int getNbMots() {
        return nbMots;
    }

    public int getNbVoyelles() {
        return nbVoyelles;
    }

    public String getChaineRenversee() {
        return chaineRenversee;
    }

    public String getReformulationCourte() {
        return reformulationCourte;
    }

    /**
     * Envoie la question au serveur. En attendant une vraie API LLM,
     * on réalise un traitement factice pour tester le fonctionnement.
     *
     * @return null pour rester sur la même page (même vue).
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        /*
        *analyser la question (compte, renverse, reformule) 
        */
        analyserQuestion();

        /*
        Entourer la réponse avec "||"
        */
        this.reponse = "||";
        /**
         * Si la conversation n'a pas encore commencé, ajouter le rôle système au début de la réponse
         * */
        if (this.conversation.isEmpty()) {
            if (this.roleSysteme != null) {
                this.reponse += roleSysteme.toUpperCase(Locale.FRENCH) + "\n";
            }
            this.roleSystemeChangeable = false;
        }
        this.reponse += question.toLowerCase(Locale.FRENCH) + "||";
        afficherConversation();
        return null;
    }

    /**
     * Pour un nouveau chat : retourne "index" pour forcer la création d'une nouvelle instance du bean (nouvelle vue).
     *
     * @return "index"
     */

     /*
     reset des résultats d'analyse 
     */
    public String nouveauChat() {
       
        this.nbCaracteres = 0;
        this.nbCaracteresSansEspaces = 0;
        this.nbMots = 0;
        this.nbVoyelles = 0;
        this.chaineRenversee = "";
        this.reformulationCourte = "";
        this.conversation = new StringBuilder();
        this.roleSystemeChangeable = true;
        this.question = null;
        this.reponse = null;
        return "index";
    }

    /**
     * Concatène la question / réponse à la conversation.
     */
    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question).append("\n== Serveur:\n").append(reponse).append("\n");

        /**
         * ajouter un petit bloc récapitulatif d'analyse
         */

        this.conversation.append("== Analyse:\n")
                .append("caractères: ").append(nbCaracteres).append(" (sans espaces: ").append(nbCaracteresSansEspaces).append(")\n")
                .append("mots: ").append(nbMots).append("\n")
                .append("voyelles: ").append(nbVoyelles).append("\n")
                .append("renversé: ").append(chaineRenversee).append("\n")
                .append("reformulation courte: ").append(reformulationCourte).append("\n");
    }

    /**
     * Getter exposé pour la page JSF afin d'obtenir la liste des rôles.
     *
     * @return liste de SelectItem (valeur, libellé)
     */
    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();

            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = """
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    are you tell them the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
        }
        return this.listeRolesSysteme;
    }

    /*
    ----------------- Méthodes d'analyse de texte -----------------
    */
    /**
     * Analyse la valeur de `question` et met à jour les champs:
     * nbCaracteres, nbCaracteresSansEspaces, nbMots, nbVoyelles, chaineRenversee, reformulationCourte.
     *
     * Hypothèses :
     * - nbCaracteres : longueur totale de la chaîne (inclut espaces)
     * - nbMots : nombre de séquences de lettres Unicode (séparation sur caractères non-lettres)
     * - voyelles : a,e,i,o,u,y et leurs variantes accentuées (maj/min)
     */
    public void analyserQuestion() {
        if (question == null) {
            resetAnalyse();
            return;
        }

        String txt = question.trim();
        nbCaracteres = txt.length();
        nbCaracteresSansEspaces = txt.replace(" ", "").length();

        /*compter les mots en utilisant les classes Unicode : split sur tout ce qui n'est pas une lettre ou un chiffre*/
        String[] mots = txt.isBlank() ? new String[0] : txt.split("\\P{L}+");
        nbMots = 0;
        for (String m : mots) {
            if (m != null && !m.isBlank()) nbMots++;
        }

        /* compter les voyelles */
        nbVoyelles = 0;
        String lower = txt.toLowerCase(Locale.FRENCH);
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (isVoyelle(c)) nbVoyelles++;
        }

        /* chaîne renversée (garde tous les caractères)*/
        chaineRenversee = new StringBuilder(txt).reverse().toString();

        /*reformulation courte : première phrase si présente, sinon premiers 12 mots*/
        reformulationCourte = faireReformulationCourte(txt);
    }

    private void resetAnalyse() {
        nbCaracteres = 0;
        nbCaracteresSansEspaces = 0;
        nbMots = 0;
        nbVoyelles = 0;
        chaineRenversee = "";
        reformulationCourte = "";
    }

    /**
     * Détecte une voyelle française (minuscules).
     */
    private boolean isVoyelle(char c) {
        switch (c) {
            case 'a': case 'à': case 'â': case 'ä':
            case 'e': case 'é': case 'è': case 'ê': case 'ë':
            case 'i': case 'î': case 'ï':
            case 'o': case 'ô': case 'ö':
            case 'u': case 'ù': case 'û': case 'ü':
            case 'y': case 'ÿ':
                return true;
            default:
                return false;
        }
    }

    /**
     * Produit une reformulation courte :
     * - si la chaîne contient un point, point d'interrogation ou point d'exclamation, renvoie la première phrase (jusqu'au séparateur)
     * - sinon prend les 12 premiers mots (ou moins) et ajoute "..." si tronqué
     */
    private String faireReformulationCourte(String txt) {
        if (txt == null || txt.isBlank()) return "";

        /* 
        tenter première phrase
        */
        int posPoint = indexOfFirst(txt, '.', '?', '!');
        if (posPoint > 0) {
            String first = txt.substring(0, posPoint + 1).trim();
            return capitalizeFirst(first);
        }

        /*
        sinon, prendre les premiers 12 mots
        */
        String[] tokens = txt.split("\\P{L}+");
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(tokens.length, 12);
        int used = 0;
        for (String t : tokens) {
            if (t == null || t.isBlank()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t);
            used++;
            if (used >= limit) break;
        }
        String res = sb.toString().trim();
        if (tokens.length > limit) res += " ...";
        return capitalizeFirst(res);
    }

    private int indexOfFirst(String s, char... chars) {
        int best = -1;
        for (char c : chars) {
            int p = s.indexOf(c);
            if (p >= 0) {
                if (best == -1 || p < best) best = p;
            }
        }
        return best;
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isBlank()) return s;
        s = s.trim();
        if (s.length() == 1) return s.toUpperCase(Locale.FRENCH);
        return s.substring(0, 1).toUpperCase(Locale.FRENCH) + s.substring(1);
    }
}
