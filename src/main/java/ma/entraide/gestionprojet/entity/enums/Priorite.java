package ma.entraide.gestionprojet.entity.enums;

public enum Priorite {
    BASSE(1),
    MOYENNE(2),
    HAUTE(3),
    CRITIQUE(4);

    private final int valeur;

    Priorite(int valeur) {
        this.valeur = valeur;
    }

    public int getValeur() {
        return valeur;
    }
}
